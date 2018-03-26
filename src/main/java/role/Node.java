package role;

import config.Config;
import network.address.Address;
import org.pmw.tinylog.Logger;
import protocol.commands.NetworkCommand;
import protocol.commands.ping.SignalEnd_NC;
import protocol.commands.ping.Ping_NC;
import protocol.commands.ping.Pong_NC;

import java.util.concurrent.CountDownLatch;

/**
 * The main processor (a.k.a process) that sends specified messages and handles the received ones.
 */
public class Node extends Role {
    private CountDownLatch pongLatch;

    public Node(Address myAddress) {
        this(myAddress, -1);
    }

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
        for (Address receiverAddress : Config.getInstance().getAddresses()) {
            NetworkCommand ping = new Ping_NC()
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
                .setReceiverAddress(message.resolveSenderAddress())
                .setSenderAddress(getMyAddress());
        sendMessage(pong);
    }

    /**
     * Sends {@link SignalEnd_NC} command to all the processes.
     */
    public void endAll() {
        for (Address receiverAddress : Config.getInstance().getAddresses()) {
            NetworkCommand signalEnd = new SignalEnd_NC()
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
                pongLatch = new CountDownLatch(Config.getInstance().getAddressCount());
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
            if(pongLatch != null){
                pongLatch.countDown();
            }
        }
    }
}
