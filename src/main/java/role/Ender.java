package role;

import config.Config;
import network.address.Address;
import protocol.commands.NetworkCommand;
import protocol.commands.ping.EndAll_NC;

public class Ender extends Role{

    public Ender(Address myAddress) {
        super(myAddress);
    }

    public void endAll(){
        for (Address receiverAddress : Config.getInstance().getAddresses()) {
            NetworkCommand ping = new EndAll_NC()
                    .setReceiverAddress(receiverAddress)
                    .setSenderAddress(getMyAddress());
            sendMessage(ping);
        }
    }

    @Override
    public void handleMessage(NetworkCommand message) {}
}
