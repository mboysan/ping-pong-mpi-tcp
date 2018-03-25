import config.Config;
import mpi.MPIException;
import network.ConnectionProtocol;
import network.address.Address;
import network.address.TCPAddress;
import org.pmw.tinylog.Logger;
import role.*;
import util.logging.LoggerConfig;

import java.io.IOException;

public class TCPMain {

    public static void main(String[] args) throws IOException, InterruptedException, MPIException {
        new LoggerConfig();
        Logger.info("INIT (TCP).");

        int port = 8080;
        Node pinger = new Node(new TCPAddress("127.0.0.1", port++));
        Node ponger1 = new Node(new TCPAddress("127.0.0.1", port++));
        Node ponger2 = new Node(new TCPAddress("127.0.0.1", port++));

        Role[] roles = new Role[]{pinger, ponger1, ponger2};

        Address[] addresses = new Address[roles.length];
        for (int i = 0; i < roles.length; i++) {
            addresses[i] = roles[i].getMyAddress();
        }

        Config.getInstance().initTCP(addresses);

        for (Role role : roles) {
            role.start();
        }
        pinger.pingAll();

        Thread.sleep(2000);
        Logger.info("Entering end cycle...");
        pinger.endAll();

        Config.getInstance().end();

        Logger.info("DONE (TCP).");
    }
}
