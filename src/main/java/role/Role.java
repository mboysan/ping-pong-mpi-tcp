package role;

import network.address.Address;
import protocol.commands.NetworkCommand;
import network.messenger.MessageReceiver;
import network.messenger.MessageSender;

/**
 * Each node is defined as a role.
 */
public abstract class Role {

    /**
     * The address of the role.
     */
    private final Address myAddress;

    /**
     * Starts the role. Basically starts the message receiver service.
     */
    public void start() {
        new MessageReceiver(myAddress, this);
    }

    /**
     * @param myAddress see {@link #myAddress}
     */
    Role(Address myAddress) {
        this.myAddress = myAddress;
    }

    /**
     * @return see {@link #myAddress}
     */
    public Address getMyAddress() {
        return myAddress;
    }

    /**
     * Handles the provided network message. Each role implements its own message handling mechanism.
     * @param message the network message to handle.
     */
    abstract public void handleMessage(NetworkCommand message);

    /**
     * Sends the network message to another process. Basically passes the message to the message sender service.
     * @param message the network message to send.
     */
    public void sendMessage(NetworkCommand message) {
        new MessageSender(message);
    }

}
