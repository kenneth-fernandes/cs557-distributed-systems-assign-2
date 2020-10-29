
import org.apache.thrift.TException;

import chord.*;

public class FileStoreHandler implements FileStore.Iface {
    private RFile rFile;
    private NodeID nodeId = new NodeID();

    public void writeFile(RFile rFile) {
        this.rFile = rFile;
    }

    public RFile readFile(java.lang.String filename) {
        return rFile;
    }

    public void setFingertable(java.util.List<NodeID> node_list) {
        System.out.println("setFingertable()");
    }

    public NodeID findSucc(java.lang.String key) {
        return nodeId;
    }

    public NodeID findPred(java.lang.String key) {
        return nodeId;
    }

    public NodeID getNodeSucc() {
        return nodeId;
    }
    
}
