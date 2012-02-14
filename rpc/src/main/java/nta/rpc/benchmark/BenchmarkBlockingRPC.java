package nta.rpc.benchmark;

import java.net.InetSocketAddress;

import nta.rpc.NettyRpc;
import nta.rpc.ProtoParamRpcServer;
import nta.rpc.RemoteException;

import nta.rpc.protocolrecords.PrimitiveProtos.StringProto;

public class BenchmarkBlockingRPC {

  public static class ClientWrapper extends Thread {
    @SuppressWarnings("unused")
    public void run() {
      InetSocketAddress addr = new InetSocketAddress("localhost", 15001);
      BenchmarkInterface proxy = null;
      proxy =
          (BenchmarkInterface) NettyRpc.getProtoParamBlockingRpcProxy(
              BenchmarkInterface.class, addr);

      long start = System.currentTimeMillis();
      StringProto ps = StringProto.newBuilder().setValue("ABCD").build();
      for (int i = 0; i < 100000; i++) {
        try {
          StringProto response = proxy.shoot(ps);
        } catch (RemoteException e1) {
          System.out.println(e1.getMessage());
        }
      }
      long end = System.currentTimeMillis();
      System.out.println("elapsed time: " + (end - start) + "msc");

    }
  }

  public static interface BenchmarkInterface {
    public StringProto shoot(StringProto l) throws RemoteException;
  }

  public static class BenchmarkImpl implements BenchmarkInterface {
    @Override
    public StringProto shoot(StringProto l) {
      return l;
    }
  }

  public static void main(String[] args) throws InterruptedException,
      RemoteException {

    ProtoParamRpcServer server =
        NettyRpc.getProtoParamRpcServer(new BenchmarkImpl(), BenchmarkInterface.class,
            new InetSocketAddress("localhost", 15001));

    server.start();
    Thread.sleep(1000);

    int numThreads = 1;
    ClientWrapper client[] = new ClientWrapper[numThreads];
    for (int i = 0; i < numThreads; i++) {
      client[i] = new ClientWrapper();
    }

    for (int i = 0; i < numThreads; i++) {
      client[i].start();
    }

    for (int i = 0; i < numThreads; i++) {
      client[i].join();
    }

    server.shutdown();
    System.exit(0);
  }
}
