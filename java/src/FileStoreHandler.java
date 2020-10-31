import org.apache.thrift.TException;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.BufferedReader;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

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

    public FileStoreHandler(String ipAddr, int portNum) throws SystemException, TException {
        nodeId = new NodeID(getSHA(ipAddr + ":" + portNum), ipAddr, portNum);
        fileMetaDataHMap = new HashMap<String, Map<String, String>>();
    }

    /**
     * Function to write file to server node
     * 
     * @param rFile
     * @throws SystemException
     * @throws TException
     */
    @Override
    public void writeFile(RFile rFile) throws SystemException, TException {
        System.out.println("writeFile()");
        String fileName = rFile.getMeta().getFilename();
        String content = rFile.getContent();

        NodeID newNodeId = findSucc(getSHA(fileName));
        System.out.println("Writefile after findSucc");

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
            fileMetaDataHMap.put(fileName, metaDataHMap);
        }
        fileContentHMap.put(fileName, content);
    }

    /**
     * Function to read file that is stored on the server
     * 
     * @param filename
     * @return
     * @throws SystemException
     * @throws TException
     */
    @Override
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

    /**
     * Function to update finger table of the node
     * 
     * @param node_list
     * @throws SystemException
     * @throws TException
     */
    @Override
    public void setFingertable(List<NodeID> node_list) throws SystemException, TException {
        System.out.println("setFingertable()");
        this.fingerTable = node_list;
    }

    /**
     * Function to find successor node based on key
     * 
     * @param key
     * @return
     * @throws SystemException
     * @throws TException
     */
    @Override
    public NodeID findSucc(String key) throws SystemException, TException {
        System.out.println("findSucc()");
        NodeID newNodeId = findPred(key);
        NodeID finalNodeId = null;
        FileStore.Client client = null;
        if (newNodeId.getId().compareTo(nodeId.getId()) != 0) {
            try {
                TTransport transport = new TSocket(newNodeId.getIp(), newNodeId.getPort());
                transport.open();
                TProtocol protocol = new TBinaryProtocol(transport);
                client = new FileStore.Client(protocol);
                return client.getNodeSucc();
            } catch (TException x) {
                throw (new SystemException()).setMessage("Error: Unable to open connection at node.");
            } finally {
                // this.getNodeSucc();
            }
        } else {
            return this.getNodeSucc();
        }

    }

    /**
     * Function to find predecessor node by key
     * 
     * @param key
     * @return
     * @throws SystemException
     * @throws TException
     */
    @Override
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

    /**
     * Function to that returns the closest node that follows the current node
     * 
     * @return
     * @throws SystemException
     * @throws TException
     */
    @Override
    public NodeID getNodeSucc() throws SystemException, TException {
        System.out.println("getNodeSucc()");
        if (isFingerTableEmpty(fingerTable)) {
            throw (new SystemException()).setMessage("Error: Finger table is not initialized.");
        }
        return fingerTable.get(0);
    }

    /**
     * Function to get SHA-256 of a string value
     * 
     * @param key
     * @return
     * @throws SystemException
     * @throws TException
     */
    private String getSHA(String key) throws SystemException, TException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            BigInteger number = new BigInteger(1, md.digest(key.getBytes(StandardCharsets.UTF_8)));
            StringBuilder hexString = new StringBuilder(number.toString(16));
            while (hexString.length() < 32) {
                hexString.insert(0, '0');
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException x) {
            throw (new SystemException()).setMessage("Error: Error in getting SHA-256");
        }
    }

    /**
     * Function to check if finger table of node is empty
     * 
     * @param fingerTable
     * @return
     */
    private boolean isFingerTableEmpty(List<NodeID> fingerTable) {
        return fingerTable == null;
    }

    /**
     * Function to find is node is in range on firstNode and secondNode
     * 
     * @param keyNode
     * @param firstNode
     * @param secondNode
     * @return
     */
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

    /**
     * Function to open connection at a node
     * 
     * @param nodeId
     * @param keyNode
     * @return
     * @throws SystemException
     * @throws TException
     */
    private NodeID openConnectionAtNode(NodeID nodeId, String keyNode) throws SystemException, TException {
        try {
            TTransport transport = new TSocket(nodeId.getIp(), nodeId.getPort());
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            FileStore.Client client = new FileStore.Client(protocol);
            return client.findPred(keyNode);
        } catch (TException x) {
            throw (new SystemException()).setMessage("Error: Unable to open connection at node.");
        } finally {
            return nodeId;
        }

    }
}
