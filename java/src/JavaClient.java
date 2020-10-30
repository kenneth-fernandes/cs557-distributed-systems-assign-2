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

public class JavaClient {
  public static void main(String [] args) {

    if (args.length != 2) {
      System.out.println("Please enter ip/host port");
      System.exit(0);
    }

    try {
      TTransport transport;
      
      transport = new TSocket(args[1], Integer.valueOf(args[2]));
      transport.open();
      
      TProtocol protocol = new  TBinaryProtocol(transport);
      FileStore.Client client = new FileStore.Client(protocol);

      perform(client);

      transport.close();
      
    } catch (SystemException | TException x) {
      x.printStackTrace();
    } 
  }

  private static void perform(FileStore.Client client) throws TException
  {

    String fileName = "sample.txt";

    // client.ping();
    // System.out.println("ping()");

    // int sum = client.add(1,1);
    // System.out.println("1+1=" + sum);

    // Work work = new Work();

    // work.op = Operation.DIVIDE;
    // work.num1 = 1;
    // work.num2 = 0;
    // try {
    //   int quotient = client.calculate(1, work);
    //   System.out.println("Whoa we can divide by 0");
    // } catch (InvalidOperation io) {
    //   System.out.println("Invalid operation: " + io.why);
    // }

    // work.op = Operation.SUBTRACT;
    // work.num1 = 15;
    // work.num2 = 10;
    // try {
    //   int diff = client.calculate(1, work);
    //   System.out.println("15-10=" + diff);
    // } catch (InvalidOperation io) {
    //   System.out.println("Invalid operation: " + io.why);
    // }

    // SharedStruct log = client.getStruct(1);
    // System.out.println("Check log: " + log.value);
  }

  private static void writeFile(){
    RFile rFile = new RFile();
    RFileMetadata rFileMetaData = new RFileMetadata();
  }
}
