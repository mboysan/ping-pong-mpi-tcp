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
        Socket socket;
        ObjectOutputStream objectOutputStream = null;
        NetworkCommand message = this.messageToSend;
        TCPAddress receiverAddress = (TCPAddress) message.resolveReceiverAddress();
        try {
            socket = new Socket(receiverAddress.getIp(), receiverAddress.getPortNumber());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(this.messageToSend);
            objectOutputStream.flush();
            objectOutputStream.close();
            socket.close();
        } catch (IOException e) {
            Logger.error("Send err msg: " + messageToSend + ", " + e, e);
        }
    }

    private void runOnMPI() {
        try {
            MPIAddress receiverAddress = (MPIAddress) messageToSend.resolveReceiverAddress();
            int tag = messageToSend.getTag();
            char[] msg = commandMarshaller.marshall(messageToSend, char[].class);
            MPI.COMM_WORLD.send(msg, msg.length, MPI.CHAR, receiverAddress.getRank(), tag);
        } catch (MPIException | IOException e) {
            Logger.error("Send err msg: " + messageToSend + ", " + e, e);
        }
    }
}
