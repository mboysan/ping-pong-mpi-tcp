import config.GlobalConfig;
import mpi.MPI;
import mpi.MPIException;
import network.address.MPIAddress;
import org.pmw.tinylog.Logger;
import role.Node;
import testframework.SystemInfo;
import testframework.TestFramework;

import java.util.concurrent.TimeUnit;

/**
 * Ping-Pong test for MPI
 */
public class MPIMain {

    public static void main(String[] args) throws MPIException, InterruptedException {
        SystemInfo sysInfo = SystemInfo.collectEvery(500, TimeUnit.MILLISECONDS);

        GlobalConfig.getInstance().initMPI(args);
        TestFramework testFramework = null;

        int rank = MPI.COMM_WORLD.getRank();

        Logger.info("MPI INIT - rank:" + rank);

        if(rank != 0){ // pinger process will be the one with rank = 0, others will be pongers
            Node ponger = new Node(new MPIAddress(rank));
        } else {
            int totalProcesses = GlobalConfig.getInstance().getProcessCount();
            Node pinger = new Node(new MPIAddress(rank));

            /* start tests */
//            testFramework = TestFramework.doPingTests(pinger, totalProcesses);
            pinger.pingAll();
            pinger.waitPongs();

            /* send end signal to all nodes */
            pinger.signalEndToAll();
        }

        GlobalConfig.getInstance().end();

        Logger.info("MPI END - rank:" + rank);
        if(testFramework != null){
            testFramework.printAllOnConsole();
        }

        sysInfo.printOnConsole();
    }
}
