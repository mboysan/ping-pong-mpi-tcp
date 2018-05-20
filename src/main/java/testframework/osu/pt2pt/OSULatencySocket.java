package testframework.osu.pt2pt;

import java.io.DataInputStream;
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

public class OSULatencySocket {

    private static final int MAX_MSG_SIZE = 131072;
    private static final int MIN_MSG_SIZE = 1*2;

    private static String MULTICAST_GROUP = "all-systems.mcast.net";
    private static int MULTICAST_PORT = 9090;

    private static InetAddress ipAddr = resolveIpAddress();

    private boolean isLeader = false;
    private String pongerIp;
    private InetAddress pongerIpInetAddr;
    private int pongerPort;

    private String myAddr;

    private CountDownLatch readyLatch = new CountDownLatch(1);

    private ServerSocket serverSocket;

    public static void main(String[] args) throws Exception {
        System.out.println("args: " + Arrays.toString(args));

        OSULatencySocket pinger = new OSULatencySocket();
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
        int minMsgSize = MIN_MSG_SIZE;
        int maxMsgSize = MAX_MSG_SIZE;
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
            if(args.length >= 3){
                minMsgSize = Integer.parseInt(args[2]);
            }
            if(args.length >= 4){
                maxMsgSize = Integer.parseInt(args[3]);
            }
        }
        skip = (skip == -1) ? iterations/2 : skip;

        try{
            /* Latency test start */
            if(pinger.isLeader){
                System.out.println("size(bytes),latency(us)");
            }
            byte[] s_buf;
            byte[] r_buf;
            long t_start = 0, t_end = 0;
            for (int size = minMsgSize; size <= maxMsgSize; size*=2) {
                s_buf = new byte[size];
                r_buf = new byte[size];
                Arrays.fill(s_buf, (byte) 1);

                if(pinger.isLeader){
                    for (int i = 0; i < iterations + skip; i++) {
                        if(i == skip){
                            t_start = System.nanoTime();
                        }
                        Socket s = pinger.sendByClient(s_buf);
                        pinger.recvByClient(r_buf, s.getInputStream());
                        pinger.closeSocket(s);
                    }
                    t_end = System.nanoTime();
                }
                else {
                    for (int i = 0; i < iterations + skip; i++) {
                        Socket s = pinger.recvByServer(r_buf);
                        pinger.sendByServer(s_buf, s.getOutputStream());
                    }
                }

                if(pinger.isLeader) {
                    double latency = ((t_end - t_start) / 1e3) / (2.0 * iterations);
                    int sizeActual = size;
                    System.out.println(String.format("%d,%.3f",sizeActual,latency));
                }
            }
            /* Latency test end */

            pinger.endAll(0);
        } catch (Exception e) {
            e.printStackTrace();
            pinger.endAll(-1);
            throw  e;
        }
    }

    public int initServer() throws IOException {
        serverSocket = new ServerSocket(0);
        return serverSocket.getLocalPort();
    }

    public void endAll(int exitStatus) throws IOException {
        System.out.println("end all on " + myAddr + ", status=" + exitStatus);
        serverSocket.close();
    }

    public Socket sendByClient(byte[] msg) throws IOException {
        Socket socket = new Socket(pongerIpInetAddr, pongerPort);
        socket.setTcpNoDelay(true);
        OutputStream dOut = socket.getOutputStream();
        dOut.write(msg,0,msg.length);    // write the message
        return socket;
    }

    public void recvByClient(byte[] r_buf, InputStream inputStream) throws IOException {
        DataInputStream dIn = new DataInputStream(inputStream);
        dIn.readFully(r_buf);
    }

    public Socket recvByServer(byte[] r_buf) throws IOException {
        Socket socket = serverSocket.accept();
        socket.setTcpNoDelay(true);
        InputStream dIn = socket.getInputStream();
        dIn.read(r_buf,0,r_buf.length);
        return socket;
    }

    public void sendByServer(byte[] msg, OutputStream outputStream) throws IOException {
        OutputStream dOut = outputStream;
        dOut.write(msg,0,msg.length);    // write the message
    }

    public void closeSocket(Socket socket) throws IOException {
        if(socket != null){
            if(socket.getOutputStream() != null){
                socket.getOutputStream().close();
            }
            socket.close();
        }
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

    public boolean resolveAddress(String msg) throws UnknownHostException {
        String[] prot = msg.trim().split(",");
        String _msg = prot[0] + "," + prot[1];
        if(!_msg.equals(myAddr)){
            pongerIp = prot[0].substring(1);
            pongerIpInetAddr = InetAddress.getByName(pongerIp);
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
