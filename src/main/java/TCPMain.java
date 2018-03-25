import config.Config;
import network.ConnectionProtocol;
import network.address.Address;
import network.address.TCPAddress;
import role.Ender;
import role.Pinger;
import role.Ponger;
import role.Role;

import java.io.IOException;

public class TCPMain {

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 8080;
        Pinger pinger = new Pinger(new TCPAddress("127.0.0.1", port++));
        Ponger ponger1 = new Ponger(new TCPAddress("127.0.0.1", port++));
        Ponger ponger2 = new Ponger(new TCPAddress("127.0.0.1", port++));
        Ender ender = new Ender(new TCPAddress("127.0.0.1", port));

        Role[] roles = new Role[]{pinger, ponger1, ponger2, ender};

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
        System.out.println("Entering end cycle...");
        ender.endAll();
        Thread.sleep(2000);

        System.out.println("TCP DONE!");

    }
}
