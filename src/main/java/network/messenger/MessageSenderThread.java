package network.messenger;

import config.Config;
import mpi.MPI;
import mpi.MPIException;
import network.ConnectionProtocol;
import network.address.MPIAddress;
import network.address.TCPAddress;
import org.pmw.tinylog.Logger;
import protocol.CommandMarshaller;
import protocol.commands.NetworkCommand;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MessageSenderThread extends Thread {

    private final ConnectionProtocol runOnTCPOrMPI;
    private final NetworkCommand messageToSend;
    private final CommandMarshaller commandMarshaller;

    public MessageSenderThread(NetworkCommand message) {
        this.runOnTCPOrMPI = Config.getInstance().getConnectionProtocol();
        this.messageToSend = message;
        this.commandMarshaller = new CommandMarshaller();

        if(runOnTCPOrMPI == ConnectionProtocol.MPI_CONNECTION){
            runOnMPI();
        } else {
            start();
        }
    }

    public synchronized void run() {
        switch (this.runOnTCPOrMPI) {
            case TCP_CONNECTION:
                runOnTCP();
                break;
            case MPI_CONNECTION:
                runOnMPI();
                break;
        }
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

    private void runOnMPI() {
        try {
            MPIAddress receiverAddress = (MPIAddress) messageToSend.resolveReceiverAddress();
            int tag = messageToSend.getTag();
            byte[] msg = commandMarshaller.marshall(messageToSend, byte[].class);

            int[] msgLen = new int[]{msg.length};

            MPI.COMM_WORLD.send(msgLen, 1, MPI.INT, receiverAddress.getRank(), tag);  //send msg length first
            MPI.COMM_WORLD.send(msg, msg.length, MPI.BYTE, receiverAddress.getRank(), tag);
        } catch (MPIException | IOException e) {
            Logger.error(e, "Send err, msg: " + messageToSend);
        }
    }
}
