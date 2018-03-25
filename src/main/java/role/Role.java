package role;

import network.address.Address;
import protocol.commands.NetworkCommand;
import network.messenger.MessageReceiver;
import network.messenger.MessageSender;

public abstract class Role {

    private final Address myAddress;

    public void start() {
        new MessageReceiver(myAddress, this);
    }

    protected Role(Address myAddress) {
        this.myAddress = myAddress;
    }

    public Address getMyAddress() {
        return myAddress;
    }

    abstract public void handleMessage(NetworkCommand message);

    public void sendMessage(NetworkCommand message) {
        new MessageSender(message);
    }

}
