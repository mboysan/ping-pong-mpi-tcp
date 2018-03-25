import config.Config;
import mpi.MPI;
import mpi.MPIException;
import network.address.MPIAddress;
import role.Ender;
import role.Pinger;
import role.Ponger;

public class MPIMain {
    public static void main(String[] args) throws MPIException, InterruptedException {
        Config.getInstance().initMPI(args);

        int rank = MPI.COMM_WORLD.getRank();
        int pingerRank = 0;

        Ponger ponger = new Ponger(new MPIAddress(rank));
        ponger.start();
        if(rank == pingerRank){
            Pinger pinger = new Pinger(new MPIAddress(rank));
            pinger.start();

            pinger.pingAll();
        }

        Thread.sleep(5000);
        if(rank == pingerRank){
            System.out.println("Entering end cycle (5 sec)...");
            Ender ender = new Ender(new MPIAddress(rank));
            ender.start();
            ender.endAll();
        }
        Thread.sleep(5000);

        Config.getInstance().end();

        System.out.println("MPI DONE!");
    }
}
