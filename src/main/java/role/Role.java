package role;

import config.GlobalConfig;
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
     * @param myAddress see {@link #myAddress}
     */
    Role(Address myAddress) {
        this.myAddress = myAddress;
        GlobalConfig.getInstance().registerRole(this);
        start();
    }

    /**
     * Starts the role. Basically starts the message receiver service.
     */
    private void start() {
        new MessageReceiver(myAddress, this);
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
    protected void sendMessage(NetworkCommand message) {
        new MessageSender(message);
    }

}
