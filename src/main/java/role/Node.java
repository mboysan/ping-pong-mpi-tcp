package role;

import config.Config;
import network.address.Address;
import org.pmw.tinylog.Logger;
import protocol.commands.NetworkCommand;
import protocol.commands.ping.SignalEnd_NC;
import protocol.commands.ping.Ping_NC;
import protocol.commands.ping.Pong_NC;

import java.util.concurrent.CountDownLatch;

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
            NetworkCommand signalEnd = new SignalEnd_NC()
                    .setReceiverAddress(receiverAddress)
                    .setSenderAddress(getMyAddress());
            sendMessage(signalEnd);
        }
    }

    public void waitPongs(){
        if(pongLatch != null){
            try {
                pongLatch.await();
                pongLatch = new CountDownLatch(Config.getInstance().getAddresses().length);
            } catch (InterruptedException e) {
                Logger.error(e);
            }
        }
    }

    @Override
    public void handleMessage(NetworkCommand message) {
        Logger.debug("[" + getMyAddress() +"] - " + message);
        if (message instanceof Ping_NC) {
            pong(message);
        }
        if (message instanceof Pong_NC) {
            if(pongLatch != null){
                pongLatch.countDown();
            }
        }
    }
}
