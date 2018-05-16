package testframework.osu.pt2pt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class OSULatencySocket2 {

    private static final int MAX_MSG_SIZE = 131072;
    private static final int MIN_MSG_SIZE = 1*2;

    private static String MULTICAST_GROUP = "all-systems.mcast.net";
    private static int MULTICAST_PORT = 9090;

    private static InetAddress ipAddr = resolveIpAddress();

    private boolean isLeader = false;
    private String pongerIp;
    private int pongerPort;

    private String myAddr;

    private CountDownLatch readyLatch = new CountDownLatch(1);

    private ServerSocket serverSocket;

    public static void main(String[] args) throws Exception {
        System.out.println("args: " + Arrays.toString(args));

        OSULatencySocket2 pinger = new OSULatencySocket2();
        int localPort = pinger.initServer();

        pinger.myAddr = ipAddr + "," + localPort;

        ExecutorService executor = Executors.newFixedThreadPool(1);

        executor.execute(() -> pinger.multicastRecv());
        System.out.println("multicasting...");
        for (int i = 0; i < 5; i++) {
            pinger.multicast(pinger.myAddr + ",");
            TimeUnit.MILLISECONDS.sleep(500);
        }

        pinger.readyLatch.await();
        executor.shutdown();

        System.out.println("Ponger addr: " + pinger.pongerIp + "," + pinger.pongerPort);

        if(localPort < pinger.pongerPort){
            pinger.isLeader = true;
            System.out.println("leader found: " + pinger.myAddr);
        }

        int iterations = 100;
        int skip = -1;
        if(args != null){
            if(args.length >= 1){
                iterations = Integer.parseInt(args[0]);
                if(iterations < 0){
                    System.err.println("iterations cannot be negative");
                    pinger.endAll(-1);
                }
            }
            if(args.length >= 2){
                skip = Integer.parseInt(args[1]);
                if(skip < 0 || skip > iterations){
                    System.err.println("skip value provided is unsupported.");
                    pinger.endAll(-1);
                }
            }
        }
        skip = (skip == -1) ? iterations/2 : skip;

        executor = Executors.newFixedThreadPool(1);
        AtomicReference<InputStream> dInAtomic = new AtomicReference<>(null);
        executor.execute(() -> {
            try {
                dInAtomic.set(pinger.accept());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        TimeUnit.SECONDS.sleep(1);
        OutputStream dOut = pinger.connect(pinger.pongerIp, pinger.pongerPort);
        TimeUnit.SECONDS.sleep(1);
        InputStream dIn = dInAtomic.get();

        executor.shutdown();

        /* Latency test start */
        if(pinger.isLeader){
            System.out.println("size(bytes),latency(us)");
        }
        byte[] s_buf;
        byte[] r_buf;
        long t_start = 0, t_end = 0;
        for (int size = MIN_MSG_SIZE; size < MAX_MSG_SIZE; size*=2) {
            s_buf = new byte[size];
            r_buf = new byte[size];
            Arrays.fill(s_buf, (byte) 1);

            if(pinger.isLeader){
                for (int i = 0; i < iterations + skip; i++) {
                    if(i == skip){
                        t_start = System.nanoTime();
                    }
                    pinger.send(s_buf, dOut);
                    pinger.recv(r_buf, dIn);
                }
                t_end = System.nanoTime();
            }
            else {
                for (int i = 0; i < iterations + skip; i++) {
                    pinger.recv(r_buf, dIn);
                    pinger.send(s_buf, dOut);
                }
            }

            if(pinger.isLeader) {
                double latency = ((t_end - t_start) / 1e3) / (2.0 * iterations);
                int sizeActual = size;
                System.out.println(String.format("%d,%.3f",sizeActual,latency));
            }
        }
        /* Latency test end */
        pinger.close(dOut, dIn);
        pinger.endAll(0);
    }

    public int initServer() throws IOException {
        serverSocket = new ServerSocket(0);
        return serverSocket.getLocalPort();
    }

    public void endAll(int exitStatus) throws IOException {
        System.out.println("end all on " + myAddr);
        serverSocket.close();
        System.exit(exitStatus);
    }

    private OutputStream connect(String destIp, int destPort) throws IOException {
        Socket socket = new Socket(InetAddress.getByName(destIp), destPort);
        socket.setTcpNoDelay(true);
        return socket.getOutputStream();
    }

    private InputStream accept() throws IOException {
        Socket socket = serverSocket.accept();
        socket.setTcpNoDelay(true);
        return socket.getInputStream();
    }

    private void close(OutputStream dOut, InputStream dIn) throws IOException {
        dOut.close();
        dIn.close();
    }

    public void send(byte[] msg, OutputStream dOut) throws IOException {
        dOut.write(msg,0,msg.length);    // write the message
        dOut.flush();
    }

    public void recv(byte[] r_buf, InputStream dIn) throws IOException {
        dIn.read(r_buf, 0, r_buf.length);
    }

    public void multicast(String message) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            int port = MULTICAST_PORT;
            byte[] msg = message.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(msg, msg.length, group, port);
            socket.send(packet);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean resolveAddress(String msg){
        String[] prot = msg.trim().split(",");
        String _msg = prot[0] + "," + prot[1];
        if(!_msg.equals(myAddr)){
            pongerIp = prot[0].substring(1);
            pongerPort = Integer.parseInt(prot[1]);
            readyLatch.countDown();
            return true;
        }
        return false;
    }

    public void multicastRecv() {
        MulticastSocket socket = null;
        InetAddress group = null;
        try {
            socket = new MulticastSocket(MULTICAST_PORT);
            group = InetAddress.getByName(MULTICAST_GROUP);
            socket.joinGroup(group);
            byte[] msg = new byte[512];    //fixed size byte[]
            while (true){
                DatagramPacket packet = new DatagramPacket(msg, msg.length);
                socket.receive(packet);
                String recv = new String(packet.getData(), StandardCharsets.UTF_8);
                if(resolveAddress(recv)){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(socket != null){
                try {
                    if(group != null){
                        socket.leaveGroup(group);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                socket.close();
            }
        }
    }


    public static InetAddress resolveIpAddress(){
        for (int i = 0; i < 5; i++) {
            try(final DatagramSocket socket = new DatagramSocket()){
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                String ipStr = socket.getLocalAddress().getHostAddress();
                if(ipStr != null){
                    return InetAddress.getByName(ipStr);
                }
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (SocketException | UnknownHostException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
