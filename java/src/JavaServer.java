
import java.net.InetAddress;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;

import chord.*;


public class JavaServer {
    
    public static FileStoreHandler handler;
    public static FileStore.Processor processor;
    public static int portNum;
    public static String ipAddr;

  public static void main(String [] args) {
    try {

      portNum = Integer.valueOf(args[0]);
      ipAddr = InetAddress.getLocalHost().getHostAddress();
      handler = new FileStoreHandler(ipAddr, portNum);
      processor = new FileStore.Processor(handler);

      Runnable simple = new Runnable() {
        public void run() {
          simple(processor);
        }
      };      
      /*
      Runnable secure = new Runnable() {
        public void run() {
          secure(processor);
        }
      };
      */

      new Thread(simple).start();
      // new Thread(secure).start();
    } catch (Exception x) {
      x.printStackTrace();
    }
  }

  public static void simple(FileStore.Processor processor) {
    try {
      TServerTransport serverTransport = new TServerSocket(port);
      TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));

      // Use this for a multithreaded server
      // TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

      System.out.println("Starting the simple server at " + ipAddr + ":" + port + " ...");
      server.serve();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
