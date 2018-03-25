import config.Config;
import mpi.MPI;
import mpi.MPIException;
import network.address.MPIAddress;
import org.pmw.tinylog.Logger;
import role.Node;
import util.logging.LoggerConfig;

public class MPIMain {
    public static void main(String[] args) throws MPIException, InterruptedException {
        new LoggerConfig();
        Logger.info("INIT (MPI).");
        Config.getInstance().initMPI(args);

        int rank = MPI.COMM_WORLD.getRank();
        int pingerRank = 0;

        if(rank != pingerRank){
            Node ponger = new Node(new MPIAddress(rank));
            ponger.start();
        } else {
            Node pinger = new Node(new MPIAddress(rank));
            pinger.start();

            Thread.sleep(1000); // let all nodes start
            pinger.pingAll();

            Thread.sleep(1000); // wait pongs
            Logger.info("Entering end cycle...");
            pinger.endAll();
        }

        Config.getInstance().end();

        Logger.info("DONE (MPI).");
    }
}
