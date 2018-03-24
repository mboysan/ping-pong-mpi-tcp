package network.messenger;

import config.Config;
import network.ConnectionProtocol;
import network.address.TCPAddress;
import protocol.NetworkCommand;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SendMessageThread extends Thread {

    private ConnectionProtocol runOnTCPOrMPI = ConnectionProtocol.TCP_CONNECTION;

    private NetworkCommand messageToSend;

    public SendMessageThread(NetworkCommand message) {

        this.runOnTCPOrMPI = Config.getInstance().getConnectionProtocol();
        this.messageToSend = message;
    }

    public synchronized void run() {

        switch (this.runOnTCPOrMPI) {
            case TCP_CONNECTION:
                this.runOnTCP();
                break;
            case MPI_CONNECTION:
                this.runOnMPI();
                break;
        }
    }

    public void runOnTCP() {
        Socket socket;
        ObjectOutputStream objectOutputStream = null;
        NetworkCommand message = this.messageToSend;
        TCPAddress receiverAddress = (TCPAddress) message.getReceiverAddress();
        try {
            socket = new Socket(receiverAddress.getIp(), receiverAddress.getPortNumber());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(this.messageToSend);
            objectOutputStream.flush();
            synchronized (System.out) {
//                System.out.println(this.messageToSend.toString());
//                System.out.flush();
            }
            objectOutputStream.close();
            socket.close();
        } catch (IOException e) {
            synchronized (System.out) {
                e.printStackTrace();
                System.out.println("Message send fail! --- from " + this.messageToSend.getSenderAddress() + " to " + this.messageToSend.getReceiverAddress() + " ---");
            }
        }
    }

    public void runOnMPI() {

    }
}
