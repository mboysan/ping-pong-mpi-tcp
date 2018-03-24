package role;

import network.address.Address;
import protocol.NetworkCommand;
import protocol.ping.Ping_NC;
import config.Config;

public class Pinger extends Role {

    public Pinger(Address myAddress) {
        super(myAddress);
    }

    public void pingAll() {
        for (Address receiverAddress : Config.getInstance().getAddresses()) {
            NetworkCommand ping = new Ping_NC()
                    .setReceiverAddress(receiverAddress)
                    .setSenderAddress(getMyAddress());
            sendMessage(ping);
            System.out.println("ping sent: " + ping);
        }
    }

    @Override
    public void handleMessage(NetworkCommand message) {
        //record times etc.
        System.out.println("pong received: " + message);
    }
}
