package role;

import network.address.Address;
import protocol.commands.NetworkCommand;
import network.messenger.MessageReceiverThread;
import network.messenger.MessageSenderThread;

public abstract class Role {

    private final Address myAddress;

    public void start() {
        new MessageReceiverThread(myAddress, this);
    }

    protected Role(Address myAddress) {
        this.myAddress = myAddress;
    }

    public Address getMyAddress() {
        return myAddress;
    }

    abstract public void handleMessage(NetworkCommand message);

    public void sendMessage(NetworkCommand message) {
        new MessageSenderThread(message);
    }

}
