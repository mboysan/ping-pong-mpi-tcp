package role;

import network.address.Address;
import protocol.commands.NetworkCommand;
import protocol.commands.ping.Ping_NC;
import protocol.commands.ping.Pong_NC;

public class Ponger extends Role {

    public Ponger(Address myAddress) {
        super(myAddress);
    }

    private void pong(NetworkCommand message) {
        NetworkCommand pong = new Pong_NC()
                .setReceiverAddress(message.resolveSenderAddress())
                .setSenderAddress(getMyAddress());
        sendMessage(pong);
    }

    @Override
    public void handleMessage(NetworkCommand message) {
        if (message instanceof Ping_NC) {
            pong(message);
            System.out.println("Ponger.handleMessage(): " + message);
        }
    }
}
