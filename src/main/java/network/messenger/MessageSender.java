package network.messenger;

import config.GlobalConfig;
import mpi.MPI;
import mpi.MPIException;
import mpi.Request;
import network.address.MPIAddress;
import network.address.TCPAddress;
import org.pmw.tinylog.Logger;
import protocol.CommandMarshaller;
import protocol.commands.NetworkCommand;
import protocol.commands.ping.SignalEnd_NC;
import testframework.TestFramework;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The message sender wrapper for the communication protocols defined in {@link network.ConnectionProtocol}.
 */
public class MessageSender {

    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static final Object mpiLock = new Object();

    /**
     * The marshaller to marshall the command to send.
     */
    private final CommandMarshaller commandMarshaller = new CommandMarshaller();


    public MessageSender() {
    }

    /**
     * Initializes the message sender. It then creates the appropriate handler to send the message.
     * @param message the command to send
     */
    public void send(NetworkCommand message) {

        //FIXME: remove following call, used only for latency testing.
        message = TestFramework.addAdditionalPayload(message);

        Runnable sender = null;
        switch (GlobalConfig.getInstance().getConnectionProtocol()) {
            case TCP_CONNECTION:
                sender = new TCPSender(message);
                break;
            case MPI_CONNECTION:
                sender = new MPISender(message);
                break;
        }
        if(message instanceof SignalEnd_NC){
            sender.run();
            executor.shutdown();
            if(executor.isShutdown()){
                Logger.debug("Executor shutdown: "+ executor);
            }
        } else {
//            executor.execute(sender);
            new Thread(sender).start();
        }
    }

    /**
     * TCP send handler
     */
    private class TCPSender implements Runnable {

        private final NetworkCommand messageToSend;

        private TCPSender(NetworkCommand messageToSend){
            this.messageToSend = messageToSend;
        }

        @Override
        public void run() {
            runOnTCP();
        }

        /**
         * Send message with TCP
         */
        private void runOnTCP() {
            Socket socket = null;
            DataOutputStream dOut = null;
            try {
                TCPAddress receiverAddress = (TCPAddress) messageToSend.resolveReceiverAddress();
                byte[] msg = commandMarshaller.marshall(messageToSend, byte[].class);
                socket = new Socket(receiverAddress.getIp(), receiverAddress.getPortNumber());
                dOut = new DataOutputStream(socket.getOutputStream());

//                dOut.writeInt(msg.length); // write length of the message
                dOut.write(msg);    // write the message
                dOut.flush();
            } catch (IOException e) {
                Logger.error("Send err, msg: " + messageToSend + ", " + e, e);
            } finally {
                if(dOut != null){
                    try {
                        dOut.close();
                    } catch (IOException e) {
                        Logger.error("dOut close err, msg: " + messageToSend + ", " + e, e);
                    }
                }
                if(socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        Logger.error("Socket close err, msg: " + messageToSend + ", " + e, e);
                    }
                }
            }
        }
    }

    /**
     * MPI send handler
     */
    private class MPISender implements Runnable {

        private final NetworkCommand messageToSend;

        private MPISender(NetworkCommand messageToSend){
            this.messageToSend = messageToSend;
        }

        @Override
        public void run(){
            runOnMPIAsync(false);
//            runOnMPISync();
        }

        /**
         * Send message with MPI. Uses <tt>MPI.COMM_WORLD.iSend()</tt> to send the message in an async manner.
         * Basically, sends the message and forgets.
         */
        private void runOnMPIAsync(boolean waitForRequest) {
            try {
                MPIAddress receiverAddress = (MPIAddress) messageToSend.resolveReceiverAddress();
                byte[] msg = commandMarshaller.marshall(messageToSend, byte[].class);
                ByteBuffer byteBuffer;
                Request r;
                synchronized (MPI.COMM_WORLD) {
                    byteBuffer = MPI.newByteBuffer(msg.length).put(msg);
                    r = MPI.COMM_WORLD.iSend(byteBuffer, byteBuffer.capacity(), MPI.BYTE, receiverAddress.getRank(), receiverAddress.getGroupId());
                }
                if(waitForRequest){
                    r.waitFor();
                    r.free();
                }
            } catch (MPIException | IOException e) {
                Logger.error(e, "Send err, msg: " + messageToSend);
            }
        }

        /**
         * Send message with MPI. Uses <tt>MPI.COMM_WORLD.send()</tt> to send the message in a synced manner.
         */
        private void runOnMPISync(){
            try {
                MPIAddress receiverAddress = (MPIAddress) messageToSend.resolveReceiverAddress();
                byte[] msg = commandMarshaller.marshall(messageToSend, byte[].class);
                ByteBuffer byteBuffer;
                synchronized (MPI.COMM_WORLD) {
                    byteBuffer = MPI.newByteBuffer(msg.length).put(msg);
                    MPI.COMM_WORLD.send(byteBuffer, byteBuffer.capacity(), MPI.BYTE, receiverAddress.getRank(), receiverAddress.getGroupId());
                }
            } catch (MPIException | IOException e) {
                Logger.error(e, "Send err, msg: " + messageToSend);
            }
        }
    }
}
