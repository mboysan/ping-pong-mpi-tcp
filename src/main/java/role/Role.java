package role;

import config.GlobalConfig;
import network.address.Address;
import org.pmw.tinylog.Logger;
import protocol.commands.NetworkCommand;
import network.messenger.MessageReceiver;
import network.messenger.MessageSender;
import protocol.commands.ping.ConnectOK_NC;
import protocol.commands.ping.Connect_NC;
import protocol.commands.ping.Ping_NC;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

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
    private Address myAddress;
    /**
     * Message sender service.
     */
    private final MessageSender messageSender;
    /**
     * Defines if this role is the leader or not.
     */
    private boolean isLeader = false;

    /**
     * Indicates if the node is ready for registration by calling {@link GlobalConfig#registerRole(Role)}.
     */
    private final CountDownLatch readyLatch = new CountDownLatch(1);

    /**
     * @param baseAddress see {@link #myAddress}. The address may be modified by the {@link MessageReceiver} after
     *                    role has been started.
     */
    Role(Address baseAddress) throws InterruptedException {
        this.myAddress = baseAddress;
        roleId = baseAddress.resolveAddressId();

        this.messageSender = new MessageSender();
        start();

        readyLatch.await();

        GlobalConfig.getInstance().registerRole(this);
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
    public void handleMessage(NetworkCommand message){
        Logger.debug("[" + getAddress() +"] - " + message);
        if(message instanceof Connect_NC){
            NetworkCommand connectOK = new ConnectOK_NC()
                    .setSenderAddress(getAddress())
                    .setReceiverAddress(message.resolveSenderAddress());
            sendMessage(connectOK);
        }
        if(message instanceof ConnectOK_NC){
            GlobalConfig.getInstance().registerAddress(message.resolveSenderAddress(), this);
        }
    }

    /**
     * Sends the network message to another process. Basically passes the message to the message sender service.
     * @param message the network message to send.
     */
    protected void sendMessage(NetworkCommand message) {
        messageSender.send(message);
    }

    public boolean isLeader() {
        return isLeader;
    }

    public void setLeader(boolean leader) {
        isLeader = leader;
    }

    /**
     * @param modifiedAddr address modified by the {@link MessageReceiver} if applicable.
     */
    public void setAddress(Address modifiedAddr){
        this.myAddress = modifiedAddr;
    }

    /**
     * Indicates the role is ready for further state changes.
     */
    public void setReady(){
        readyLatch.countDown();
    }

    @Override
    public String toString() {
        return "Role{" +
                "roleId='" + roleId + '\'' +
                ", myAddress=" + myAddress +
                ", isLeader=" + isLeader +
                '}';
    }
}
