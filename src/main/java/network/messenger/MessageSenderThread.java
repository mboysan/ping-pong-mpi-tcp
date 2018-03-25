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

public class MessageSenderThread extends Thread {

    private final ConnectionProtocol runOnTCPOrMPI;
    private final NetworkCommand messageToSend;
    private final CommandMarshaller commandMarshaller;

    public MessageSenderThread(NetworkCommand message) {
        this.runOnTCPOrMPI = Config.getInstance().getConnectionProtocol();
        this.messageToSend = message;
        this.commandMarshaller = new CommandMarshaller();

        start();
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
            String msg = commandMarshaller.marshall(messageToSend, String.class);

            socket = new Socket(receiverAddress.getIp(), receiverAddress.getPortNumber());
            dOut = new DataOutputStream(socket.getOutputStream());
            dOut.writeUTF(msg); // write the message
            dOut.flush();
        } catch (IOException e) {
            Logger.error(e, "Send err, msg: " + messageToSend);
        } finally {
            if(dOut != null){
                try {
                    dOut.close();
                } catch (IOException e) {
                    Logger.error(e, "dOut close err, msg: " + messageToSend);
                }
            }
            if(socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    Logger.error(e, "Socket close err, msg: " + messageToSend);
                }
            }
        }


    }

    private void runOnMPI() {
        try {
            MPIAddress receiverAddress = (MPIAddress) messageToSend.resolveReceiverAddress();
            int tag = messageToSend.getTag();
            byte[] msg = commandMarshaller.marshall(messageToSend, byte[].class);
            MPI.COMM_WORLD.send(msg, msg.length, MPI.BYTE, receiverAddress.getRank(), tag);
        } catch (MPIException | IOException e) {
            Logger.error(e, "Send err, msg: " + messageToSend);
        }
    }
}
