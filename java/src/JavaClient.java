
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.nio.file.Paths;

public class JavaClient {
  public static void main(String[] args) {

    if (args.length != 2) {
      System.out.println("Please enter ip/host port");
      System.exit(0);
    }

    try {
      TTransport transport;

      transport = new TSocket(args[0], Integer.valueOf(args[1]));
      transport.open();

      TProtocol protocol = new TBinaryProtocol(transport);
      FileStore.Client client = new FileStore.Client(protocol);

      perform(client);

      transport.close();

    } catch (Exception x) {
      x.printStackTrace();
    }
  }

  private static void perform(FileStore.Client client) throws SystemException, TException, IOException {
    writeFileOnClient(client);

    readFileOnClient(client);
  }

  /**
   * Function that writes data to the server
   * 
   * @param client
   * @throws SystemException
   * @throws TException
   * @throws IOException
   */
  private static void writeFileOnClient(FileStore.Client client) throws SystemException, TException, IOException {
    String fileName = "sample.txt";

    String content = "Content";

    try {

      RFile rFile = new RFile();
      RFileMetadata rFileMetaData = new RFileMetadata();

      rFileMetaData.setFilename(fileName);
      rFileMetaData.setFilenameIsSet(true);

      rFile.setMeta(rFileMetaData);
      rFile.setMetaIsSet(true);

      rFile.setContent(content);
      rFile.setContentIsSet(true);

      client.writeFile(rFile);

    } catch (TException x) {
      throw x;
    }
  }

  /**
   * Function to read file from the server
   * 
   * @param client
   * @throws SystemException
   * @throws TException
   */
  private static void readFileOnClient(FileStore.Client client) throws SystemException, TException {
    String fileName = "sample.txt";
    NodeID nodeId = client.findSucc(getSHA(fileName));
    TTransport transport = new TSocket(nodeId.getIp(), nodeId.getPort());
    transport.open();

    TProtocol protocol = new TBinaryProtocol(transport);
    FileStore.Client readFileClient = new FileStore.Client(protocol);

    RFile rFile = readFileClient.readFile(fileName);
    System.out.println("Filename - " + rFile.getMeta().getFilename());
    System.out.println("Version Number - " + rFile.getMeta().getVersion());
    System.out.println("Content - " + rFile.getContent());
    transport.close();
  }

  /**
   * Function to get SHA-256 of a string value
   * 
   * @param key
   * @return
   * @throws SystemException
   * @throws TException
   */
  private static String getSHA(String key) throws SystemException, TException {
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
    } finally {
      return "";
    }
  }

}
