import config.Config;
import network.ConnectionProtocol;
import network.address.TCPAddress;
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

        Role[] roles = new Role[]{pinger, ponger1, ponger2};

        Config.getInstance().init(ConnectionProtocol.TCP_CONNECTION, roles);

        for (Role role : roles) {
            role.start();
        }
        pinger.pingAll();

        Thread.sleep(1000);

        System.out.println("DONE!");
    }
}
