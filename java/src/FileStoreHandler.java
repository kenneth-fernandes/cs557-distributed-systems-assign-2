import org.apache.thrift.TException;

import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import chord.FileStore;
import chord.NodeID;
import chord.RFile;
import chord.RFileMetadata;
import chord.SystemException;

public class FileStoreHandler implements FileStore.Iface {

    private RFile rFile;
    private RFileMetadata rFileMetaData;
    private NodeID nodeId;
    private List<NodeID> fingerTable;
    private Map<String, Map<String, String>> fileMetaDataHMap;
    private Map<String, String> fileContentHMap;

    public FileStoreHandler(String ipAddr, int portNum) {
        nodeId = new NodeID(getSHA(ipAddr + ":" + port), ipAddr, port);
        fileMetaDataHMap = new HashMap<String, Map<String, String>>();
    }

    public void writeFile(RFile rFile) throws SystemException, TException {
        System.out.println("writeFile()");
        String fileName = rFile.getMeta().getFilename();
        String content = rFile.getContent();

        NodeID newNodeId = findSucc(getSHA(fileName));

        if (!newNodeId.equals(nodeId)) {
            throw (new SystemException()).setMessage("Error: Error in writing file on the server");
        }
        if (fileMetaDataHMap.containsKey(fileName)) {
            fileMetaDataHMap.get(fileName).put("Version_Number",
                    (Integer.parseInt(fileMetaDataHMap.get(fileName).get("Version_Number")) + 1) + "");
        } else {
            Map<String, String> metaDataHMap = new HashMap<String, String>();
            metaDataHMap.put("Filename", fileName);
            metaDataHMap.put("Version_Number", "0");
            fileMetaDataHMap.put(filename, metaDataHMap);

        }

        fileContentHMap.put(fileName, content);

        // BufferedReader br;
        // try {
        // br = new BufferedReader(new FileReader(fileName));
        // String content = br.readLine();

        // while (content != null) {
        // content += "\n" + br.readLine();
        // }
        // br.close();
        // fileContentHMap.put(fileName, content == null ? "" : content);

        // } catch (Exception x) {
        // throw (new SystemException())
        // .setMessage("Error: Error in reading data from file provided by user in
        // writeFile()");
        // } finally {
        // br.close();
        // }

    }

    public RFile readFile(String filename) throws SystemException, TException {
        System.out.println("readFile()");
        NodeID newNodeId = findSucc(getSHA(filename));

        rFile = null;

        if (!newNodeId.equals(nodeId)) {
            throw (new SystemException()).setMessage("Error: Error in reading file from the server");
        }

        if (fileMetaDataHMap.containsKey(filename)) {
            rFile = new RFile();
            Map<String, String> metaData = fileMetaDataHMap.get(filename);
            rFileMetaData = new RFileMetadata();

            rFileMetaData.setFilename(filename);
            rFileMetaData.setFilenameIsSet(true);

            rFileMetaData.setVersion(Integer.parseInt(metaData.get("Version_Number")));
            rFileMetaData.setVersionIsSet(true);

            rFile.setMeta(rFileMetaData);
            rFile.setMetaIsSet(true);

            rFile.setContent(fileContentHMap.get(filename));
            rFile.setContentIsSet(true);

        } else {
            throw (new SystemException()).setMessage("Error: File not present on the server.");
        }

        return rFile;
    }

    public void setFingertable(List<NodeID> node_list) throws SystemException, TException {
        System.out.println("setFingertable()");
        this.fingerTable = node_list;
    }

    public NodeID findSucc(String key) throws SystemException, TException {
        System.out.println("findSucc()");
        NodeID newNodeId = findPred(key);
        try {
            TTransport transport = new TSocket(newNodeId.getIp(), newNodeId.getPort());
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            FileStore.Client client = new FileStore.Client(protocol);
        } catch (TException x) {
            throw x;
        } catch (SystemException x) {
            throw (new SystemException()).setMessage("Error: Unable to open connection at node.");
        }
        return client.getNodeSucc();
    }

    public NodeID findPred(String key) throws SystemException, TException {
        System.out.println("findPred()");
        boolean isNodeInRange;
        int counter;
        if (isFingerTableEmpty(fingerTable)) {
            throw (new SystemException()).setMessage("Error: Finger table is not initialized.");
        }
        isNodeInRange = checkIsNodeInRange(key, nodeId.getId(), fingerTable.get(0).getId());
        counter = fingerTable.size() - 1;
        while (!isNodeInRange && counter > 0) {
            NodeID newNodeId = fingerTable.get(counter);
            if (checkIsNodeInRange(newNodeId.getId(), nodeId.getId(), key)) {
                return openConnectionAtNode(newNodeId, key);
            }
            counter -= 1;
        }
        return nodeId;
    }

    public NodeID getNodeSucc() throws SystemException, TException {
        System.out.println("getNodeSucc()");
        if (isFingerTableEmpty(fingerTable)) {
            throw (new SystemException()).setMessage("Error: Finger table is not initialized.");
        }
        return fingerTable.get(0);
    }

    private String getSHA(String key) {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        BigInteger number = new BigInteger(1, md.digest(key.getBytes(StandardCharsets.UTF_8)));
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }

    private boolean isFingerTableEmpty(List<NodeID> fingerTable) {
        return fingerTable == null;
    }

    private boolean checkIsNodeInRange(String keyNode, String firstNode, String secondNode) {
        boolean flag = false;
        // If Node1 is less than Node2
        if (keyNode.compareTo(firstNode) > 0 && keyNode.compareTo(secondNode) < 0) {
            flag = true;
        }
        // If Node1 is greater than Node2
        else if (firstNode.compareTo(secondNode) > 0) {
            flag = (keyNode.compareTo(firstNode) < 0 && keyNode.compareTo(secondNode) < 0)
                    || (keyNode.compareTo(firstNode) > 0 && keyNode.compareTo(secondNode) > 0);
        }
        return flag;
    }

    private NodeID openConnectionAtNode(NodeID nodeId, String keyNode) throws SystemException, TException {
        try {
            TTransport transport = new TSocket(nodeId.getIp(), nodeId.getPort());
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            FileStore.Client client = new FileStore.Client(protocol);
            return client.findPred(key);
        } catch (TException x) {
            throw x;
        } catch (SystemException x) {
            throw (new SystemException()).setMessage("Error: Unable to open connection at node.");
        }
        return nodeId;
    }
}
