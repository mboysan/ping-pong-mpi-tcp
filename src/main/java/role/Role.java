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
     * Id of the role
     */
    private final String roleId;
    /**
     * The address of the role.
     */
    private final Address myAddress;

    private final MessageSender messageSender;

    /**
     * @param myAddress see {@link #myAddress}
     */
    Role(Address myAddress) {
        this.myAddress = myAddress;
        roleId = myAddress.resolveAddressId();
        GlobalConfig.getInstance().registerRole(this);

        this.messageSender = new MessageSender();
        start();
    }

    /**
     * Starts the role. Basically starts the message receiver service.
     */
    private void start() {
        new MessageReceiver(this);
    }

    /**
     * @return see {@link #myAddress}
     */
    public Address getAddress() {
        return myAddress;
    }

    /**
     * @return see {@link #roleId}
     */
    public String getRoleId() {
        return roleId;
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
        messageSender.send(message);
    }

}
