package role;

import config.Config;
import network.address.Address;
import org.pmw.tinylog.Logger;
import protocol.commands.NetworkCommand;
import protocol.commands.ping.EndAll_NC;
import protocol.commands.ping.Ping_NC;
import protocol.commands.ping.Pong_NC;

public class Node extends Role {
    public Node(Address myAddress) {
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

    private void pong(NetworkCommand message) {
        NetworkCommand pong = new Pong_NC()
                .setReceiverAddress(message.resolveSenderAddress())
                .setSenderAddress(getMyAddress());
        sendMessage(pong);
    }

    public void endAll() {
        for (Address receiverAddress : Config.getInstance().getAddresses()) {
            NetworkCommand ping = new EndAll_NC()
                    .setReceiverAddress(receiverAddress)
                    .setSenderAddress(getMyAddress());
            sendMessage(ping);
        }
    }

    @Override
    public void handleMessage(NetworkCommand message) {
        Logger.info("[" + getMyAddress() +"] - " + message);
        if (message instanceof Ping_NC) {
            pong(message);
        }
        if (message instanceof Pong_NC) {
            //recordings...
        }
    }
}
