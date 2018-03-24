package role;

import network.address.Address;
import protocol.commands.NetworkCommand;
import protocol.commands.ping.Ping_NC;
import config.Config;
import protocol.commands.ping.Pong_NC;

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
        }
    }

    @Override
    public void handleMessage(NetworkCommand message) {
        //record times etc.
        if(message instanceof Pong_NC){
            System.out.println("Pinger.handleMessage(): " + message);
        }
    }
}
