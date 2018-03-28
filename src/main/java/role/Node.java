package role;

import config.GlobalConfig;
import network.address.Address;
import org.pmw.tinylog.Logger;
import protocol.commands.NetworkCommand;
import protocol.commands.ping.Ping_NC;
import protocol.commands.ping.Pong_NC;
import protocol.commands.ping.SignalEnd_NC;
import testframework.LatencyResult;
import testframework.TestResultCollector;

import java.util.concurrent.CountDownLatch;

import static testframework.TestPhase.PHASE_CUSTOM;

/**
 * The main processor (a.k.a process) that sends specified messages and handles the received ones.
 */
public class Node extends Role {
    /**
     * Latch to wait for all the pong responses.
     */
    private CountDownLatch pongLatch;

    /**
     * Currently only used to initialize the Ponger node.
     * @param myAddress address of the node.
     */
    public Node(Address myAddress) {
        this(myAddress, -1);
    }

    /**
     * Currently only used to initializes the Pinger node.
     * @param myAddress  address of the node.
     * @param totalNodes number of nodes present in the group.
     */
    public Node(Address myAddress, int totalNodes){
        super(myAddress);
        pongLatch = null;
        if(totalNodes > 0){
            pongLatch = new CountDownLatch(totalNodes);
        }
    }

    /**
     * Sends {@link Ping_NC} request to all processes.
     */
    public void pingAll() {
        //TODO: synchronize addresses? But it will be too slow. Some other method of address looping might be needed.
        for (Address receiverAddress : GlobalConfig.getInstance().getAddresses()) {
            NetworkCommand ping = new Ping_NC()
                    .setSenderId(getRoleId())
                    .setReceiverAddress(receiverAddress)
                    .setSenderAddress(getMyAddress());
            sendMessage(ping);
        }
    }

    /**
     * Sends {@link Pong_NC} response to source process.
     * @param message the ping request received.
     */
    private void pong(Ping_NC message) {
        NetworkCommand pong = new Pong_NC()
                .setSenderId(getRoleId())
                .setReceiverAddress(message.resolveSenderAddress())
                .setSenderAddress(getMyAddress());
        sendMessage(pong);
    }

    /**
     * Sends {@link SignalEnd_NC} command to all the processes.
     */
    public void signalEndToAll() {
        for (Address receiverAddress : GlobalConfig.getInstance().getAddresses()) {
            NetworkCommand signalEnd = new SignalEnd_NC()
                    .setSenderId(getRoleId())
                    .setReceiverAddress(receiverAddress)
                    .setSenderAddress(getMyAddress());
            sendMessage(signalEnd);
        }
    }

    /**
     * Waits until all the {@link Pong_NC} responses are received. Resets the {@link #pongLatch} if all are received.
     */
    public void waitPongs(){
        if(pongLatch != null){
            try {
                pongLatch.await();
                pongLatch = new CountDownLatch(GlobalConfig.getInstance().getProcessCount());
            } catch (InterruptedException e) {
                Logger.error(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleMessage(NetworkCommand message) {
        Logger.debug("[" + getMyAddress() +"] - " + message);
        if (message instanceof Ping_NC) {
            pong((Ping_NC) message);
        }
        if (message instanceof Pong_NC) {
            /* collect latency result and add it to test result collector */
            long currTime = System.currentTimeMillis();
            TestResultCollector.getInstance().addResultAsync(new LatencyResult(
                    "pingSingle",
                    PHASE_CUSTOM,
                    message.getSenderId(),
                    currTime,
                    message.getTimeStamp(),
                    currTime));
            if (pongLatch != null) {
                pongLatch.countDown();
            }
        }
    }
}
