package network.messenger;

import config.Config;
import mpi.MPI;
import mpi.MPIException;
import network.address.MPIAddress;
import network.address.TCPAddress;
import org.pmw.tinylog.Logger;
import protocol.CommandMarshaller;
import protocol.commands.NetworkCommand;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class MessageSender {

    private final NetworkCommand messageToSend;
    private final CommandMarshaller commandMarshaller;

    public MessageSender(NetworkCommand message) {
        this.messageToSend = message;
        this.commandMarshaller = new CommandMarshaller();

        switch (Config.getInstance().getConnectionProtocol()) {
            case TCP_CONNECTION:
                new TCPSender().start();    // starts thread and calls run() method
                break;
            case MPI_CONNECTION:
                new MPISender().start();    // does not start a thread
                break;
        }
    }

    private class TCPSender extends Thread {
        @Override
        public void run() {
            runOnTCP();
        }

        private void runOnTCP() {
            Socket socket = null;
            DataOutputStream dOut = null;
            try {
                TCPAddress receiverAddress = (TCPAddress) messageToSend.resolveReceiverAddress();
                byte[] msg = commandMarshaller.marshall(messageToSend, byte[].class);
                socket = new Socket(receiverAddress.getIp(), receiverAddress.getPortNumber());
                dOut = new DataOutputStream(socket.getOutputStream());

                dOut.writeInt(msg.length); // write length of the message
                dOut.write(msg);           // write the message
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

    private class MPISender {

        private void start(){
            run();
        }
//
//        @Override
        public void run(){
            runOnMPIAsync();
//            runOnMPISync();
        }

        private void runOnMPIAsync() {
            try {
                MPIAddress receiverAddress = (MPIAddress) messageToSend.resolveReceiverAddress();
                int tag = messageToSend.getTag();
                byte[] msg = commandMarshaller.marshall(messageToSend, byte[].class);

                IntBuffer intBuffer = MPI.newIntBuffer(1).put(0, msg.length);
                ByteBuffer byteBuffer = MPI.newByteBuffer(msg.length).put(msg);
                synchronized (MPI.COMM_WORLD) {
//                    MPI.COMM_WORLD.iSend(intBuffer, intBuffer.capacity(), MPI.INT, receiverAddress.getRank(), tag);  //send msg length first
                    MPI.COMM_WORLD.iSend(byteBuffer, byteBuffer.capacity(), MPI.BYTE, receiverAddress.getRank(), tag);
                }
            } catch (MPIException | IOException e) {
                Logger.error(e, "Send err, msg: " + messageToSend);
            }
        }

        private void runOnMPISync(){
            try {
                MPIAddress receiverAddress = (MPIAddress) messageToSend.resolveReceiverAddress();
                int tag = messageToSend.getTag();
                byte[] msg = commandMarshaller.marshall(messageToSend, byte[].class);

                /*
                int[] msgInfo = new int[]{msg.length};
                synchronized (MPI.COMM_WORLD) {
                    MPI.COMM_WORLD.send(msgInfo, msgInfo.length, MPI.INT, receiverAddress.getRank(), tag);  //send msg length first
                }
                */
                synchronized (MPI.COMM_WORLD) {
                    MPI.COMM_WORLD.send(msg, msg.length, MPI.BYTE, receiverAddress.getRank(), tag);
                }
            } catch (MPIException | IOException e) {
                Logger.error(e, "Send err, msg: " + messageToSend);
            }
        }
    }
}
