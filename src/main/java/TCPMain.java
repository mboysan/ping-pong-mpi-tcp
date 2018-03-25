import config.Config;
import mpi.MPIException;
import network.address.Address;
import network.address.TCPAddress;
import org.pmw.tinylog.Logger;
import role.*;
import config.LoggerConfig;

import java.io.IOException;

public class TCPMain {

    public static void main(String[] args) throws IOException, InterruptedException, MPIException {
        new LoggerConfig();
        Logger.info("INIT (TCP).");

        int totalNodes = 5;
        if(args != null && args.length > 0){
            totalNodes = Integer.parseInt(args[0]);
        }

        Role[] roles = new Role[totalNodes];
        Address[] addresses = new Address[roles.length];
        int port = 8080;
        for (int i = 1; i < totalNodes; i++) {  // first index will be reserved to pinger
            Node ponger = new Node(new TCPAddress("127.0.0.1", port++));

            roles[i] = ponger;
            addresses[i] = ponger.getMyAddress();
        }
        Node pinger = new Node(new TCPAddress("127.0.0.1", port++), totalNodes);

        roles[0] = pinger;
        addresses[0] = pinger.getMyAddress();

        Config.getInstance().initTCP(addresses);

        /* start nodes */
        for (Role role : roles) {
            role.start();
        }

        Logger.info("Starting ping-pong tests...");
        
        TestFramework.loopPing(pinger, 1000);

        Logger.info("Entering end cycle...");

        pinger.endAll();

        Config.getInstance().end();

        Logger.info("DONE (TCP).");
    }
}
