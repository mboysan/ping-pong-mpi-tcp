import config.GlobalConfig;
import mpi.MPI;
import mpi.MPIException;
import network.address.MPIAddress;
import org.pmw.tinylog.Logger;
import role.Node;
import testframework.TestFramework;

/**
 * Ping-Pong test for MPI
 */
public class MPIMain {

    public static void main(String[] args) throws MPIException, InterruptedException {
        GlobalConfig.getInstance().initMPI(args);
        TestFramework testFramework = null;

        int rank = MPI.COMM_WORLD.getRank();

        Logger.info("MPI INIT - rank:" + rank);

        if(rank != 0){ // pinger process will be the one with rank = 0, others will be pongers
            Node ponger = new Node(new MPIAddress(rank));
        } else {
            int totalProcesses = GlobalConfig.getInstance().getAddressCount();
            Node pinger = new Node(new MPIAddress(rank), totalProcesses);

            /* start tests */
            testFramework = new TestFramework(pinger, totalProcesses);
            testFramework.initTests();

            /* send end signal to all nodes */
            pinger.endAll();
        }

        GlobalConfig.getInstance().end();

        Logger.info("MPI END - rank:" + rank);
        if(testFramework != null){
            testFramework.printOnConsole(null);
        }
    }
}
