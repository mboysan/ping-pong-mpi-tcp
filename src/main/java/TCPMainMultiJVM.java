import config.GlobalConfig;
import mpi.MPIException;
import network.address.TCPAddress;
import org.pmw.tinylog.Logger;
import role.Node;
import testframework.SystemInfo;
import testframework.TestFramework;
import testframework.TestPhase;

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

        Logger.info("Args received: " + Arrays.toString(args));
        int totalProcesses = 3;
        int rank = 0;
        if(args != null && args.length > 0){
            totalProcesses = Integer.parseInt(args[0]);
            rank = Integer.parseInt(args[1]);
        }
        int port = 8080 + rank;

        GlobalConfig.getInstance().initTCP(false, totalProcesses);
        Logger.info("TCP INIT (Multi JVM)");

        if(rank != 0){
            Node ponger = new Node(new TCPAddress("127.0.0.1", port));
        } else {
            Node pinger = new Node(new TCPAddress("127.0.0.1", port), totalProcesses);

            /* start tests */
            testFramework = TestFramework.doPingTests(pinger, totalProcesses);

            /* send end signal to all nodes */
            pinger.signalEndToAll();
        }

        GlobalConfig.getInstance().end();

        Logger.info("TCP END - rank:" + rank);
        if(testFramework != null){
            testFramework.printOnConsole("pingAll", TestPhase.PHASE_FULL_LOAD);
        }

        sysInfo.printOnConsole();
    }
}
