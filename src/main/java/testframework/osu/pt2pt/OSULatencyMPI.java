package testframework.osu.pt2pt;

import mpi.MPI;
import mpi.MPIException;

import java.util.Arrays;

public class OSULatencyMPI {

    private static final int MAX_MSG_SIZE = 6400000;
    private static final int MIN_MSG_SIZE = 1;

    public static void main(String[] args) throws MPIException {
        int myid = -1;
        int numprocs = -1;
        char[] s_buf, r_buf;
        long t_start = 0, t_end = 0;

        MPI.Init(args);
        myid = MPI.COMM_WORLD.getRank();
        numprocs = MPI.COMM_WORLD.getSize();

        if(numprocs != 2) {
            if (myid == 0) {
                System.err.println("This test requires exactly two processes");
            }
            endAll(-1);
        }

        int iterations = 100;
        int skip = -1;
        if(args != null){
            if(args.length >= 1){
                iterations = Integer.parseInt(args[0]);
                if(iterations < 0){
                    System.err.println("iterations cannot be negative");
                    endAll(-1);
                }
            }
            if(args.length >= 2){
                skip = Integer.parseInt(args[1]);
                if(skip < 0 || skip > iterations){
                    System.err.println("skip value provided is unsupported.");
                    endAll(-1);
                }
            }
        }
        skip = (skip == -1) ? iterations/2 : skip;

        /* Latency test start */
        if(myid == 0){
            System.out.println("size(bytes),latency(us)");
        }

        for (int size = MIN_MSG_SIZE; size < MAX_MSG_SIZE; size*=2) {
            s_buf = new char[size];
            r_buf = new char[size];
            Arrays.fill(s_buf, 'a');

            MPI.COMM_WORLD.barrier();

            if(myid == 0){
                for (int i = 0; i < iterations + skip; i++) {
                    if(i == skip){
                        t_start = System.nanoTime();
                    }
                    MPI.COMM_WORLD.send(s_buf, size, MPI.CHAR, 1, 1);
                    MPI.COMM_WORLD.recv(r_buf, size, MPI.CHAR, 1, 1);
                }
                t_end = System.nanoTime();
            }
            else if(myid == 1){
                for (int i = 0; i < iterations + skip; i++) {
                    MPI.COMM_WORLD.recv(r_buf, size, MPI.CHAR, 0, 1);
                    MPI.COMM_WORLD.send(s_buf, size, MPI.CHAR, 0, 1);
                }
            }

            if(myid == 0) {
                double latency = ((t_end - t_start) / 1e3) / (2.0 * iterations);
                int sizeActual = size * 2;
                System.out.println(String.format("%d,%.3f",sizeActual,latency));
            }
        }
        /* Latency test end */

        endAll(0);
    }

    private static void endAll(int exitStatus) throws MPIException {
        MPI.Finalize();
        System.exit(exitStatus);
    }
}
