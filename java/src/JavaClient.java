
/**
 * JavaClient
 */
import chord.*;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

import java.nio.file.Paths;

public class JavaClient {
  public static void main(String[] args) {

    if (args.length != 2) {
      System.out.println("Please enter ip/host port");
      System.exit(0);
    }

    try {
      TTransport transport;

      transport = new TSocket(args[1], Integer.valueOf(args[2]));
      transport.open();

      TProtocol protocol = new TBinaryProtocol(transport);
      FileStore.Client client = new FileStore.Client(protocol);

      perform(client);

      transport.close();

    } catch (SystemException | TException x) {
      x.printStackTrace();
    }
  }

  private static void perform(FileStore.Client client) throws SystemException, TException {
    writeFile(client);

    readFile(client);
  }

  private static void writeFile(FileStore.Client client) throws SystemException, TException {
    String fileName = "sample.txt";

    String content = "";

    BufferedReader br;
    try {
      br = new BufferedReader(new FileReader(Paths.get(fileName)));
      content = br.readLine();

      while (content != null) {
        content += "\n" + br.readLine();
      }
      br.close();
      content = content == null ? "" : content;

      RFile rFile = new RFile();
      RFileMetadata rFileMetaData = new RFileMetadata();

      rFileMetaData.setFilename(filename);
      rFileMetaData.setFilenameIsSet(true);

      rFile.setMeta(rFileMetaData);
      rFile.setMetaIsSet(true);

      rFile.setContent(content);
      rFile.setContentIsSet(true);

      client.writeFile(rFile);

    } catch (Exception x) {
      throw x;
    } finally {
      br.close();
    }
  }

  private static void readFile(FileStore.Client client) throws SystemException, TException {
    String fileName = "sample.txt";
    RFile rFile = client.readFile(filename);
    System.out.println("Filename - " + rFile.getMeta().getFilename());
    System.out.println("Version Number - " + rFile.getMeta().getVersion());
    System.out.println("Content - " + rFile.getContent());
  }

}
