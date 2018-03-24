package role;

import network.address.Address;
import protocol.NetworkCommand;
import protocol.ping.Ping_NC;
import protocol.ping.Pong_NC;

public class Ponger extends Role {

    public Ponger(Address myAddress) {
        super(myAddress);
    }

    @Override
    public void handleMessage(NetworkCommand message) {
        if (message instanceof Ping_NC) {
            pong(message);
        }
    }

    private void pong(NetworkCommand message) {
        NetworkCommand pong = new Pong_NC()
                .setReceiverAddress(message.getSenderAddress())
                .setSenderAddress(getMyAddress());
        sendMessage(pong);
    }
}
