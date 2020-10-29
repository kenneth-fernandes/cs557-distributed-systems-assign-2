
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
    public static FileStoreHandler.Processor processor;
    public static int port;

  public static void main(String [] args) {
    try {
      handler = new FileStoreHandler();
      processor = new FileStoreHandler.Processor(handler);
      port= Integer.valueOf(args[0]);
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

  public static void simple(FileStoreHandler.Processor processor) {
    try {
      TServerTransport serverTransport = new TServerSocket(9090);
      TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));

      // Use this for a multithreaded server
      // TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

      System.out.println("Starting the simple server...");
      server.serve();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
