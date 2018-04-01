import config.GlobalConfig;
import mpi.MPIException;
import network.address.MulticastAddress;
import network.address.TCPAddress;
import org.pmw.tinylog.Logger;
import role.Node;
import testframework.SystemInfo;
import testframework.TestFramework;
import testframework.TestPhase;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Assumes a single JVM is running per Node.
 */
public class TCPMainMultiJVM {

    public static void main(String[] args) throws UnknownHostException, InterruptedException, MPIException {
        SystemInfo sysInfo = SystemInfo.collectEvery(500, TimeUnit.MILLISECONDS);

        TestFramework testFramework = null;

        InetAddress ip = TCPAddress.resolveIpAddress();

        Logger.info("Args received: " + Arrays.toString(args));
        int totalProcesses = 3;
        int rank = 0;
        String multicastGroup = "all-systems.mcast.net";
        if(args != null){
            if(args.length >= 1){
                totalProcesses = Integer.parseInt(args[0]);
            }
            if(args.length >= 2){
                rank = Integer.parseInt(args[1]);
            }
            if(args.length >= 3){
                multicastGroup = args[2];
            }
        }
        int port = 9090 + rank;

        GlobalConfig.getInstance().initTCP(false, new MulticastAddress(multicastGroup, 9999));
        Logger.info("TCP INIT (Multi JVM)");

        Node node = new Node(new TCPAddress(ip, port));

        Logger.info("Node created: " + node);

        if(node.isLeader()){    // the node is pinger.
            /* start tests */
            testFramework = TestFramework.doPingTests(node, totalProcesses);

            /* send end signal to all nodes */
            node.signalEndToAll();
        }

        GlobalConfig.getInstance().end();

        Logger.info("TCP END - rank:" + rank);
        if(testFramework != null){
//            testFramework.printOnConsole("pingAll", TestPhase.PHASE_FULL_LOAD);
            testFramework.printAllOnConsole();
        }

        sysInfo.printOnConsole();
    }
}
