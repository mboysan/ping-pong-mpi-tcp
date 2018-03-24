package network.messenger;

import config.Config;
import network.ConnectionProtocol;
import network.address.Address;
import network.address.TCPAddress;
import protocol.NetworkCommand;
import role.Role;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class MessageReceiverThread extends Thread {

    private ConnectionProtocol runOnTCPOrMPI = ConnectionProtocol.TCP_CONNECTION;

    private final Address address;
    private Role roleInstance;

    public MessageReceiverThread(Address address, Role roleInstance) {
        this.address = address;
        this.runOnTCPOrMPI = Config.getInstance().getConnectionProtocol();
        this.roleInstance = roleInstance;

        this.start();
    }

    public synchronized void run() {
        switch (this.runOnTCPOrMPI) {
            case TCP_CONNECTION:
                this.runOnTCP();
                break;
            case MPI_CONNECTION:
                this.runOnUDP();
                break;
        }
    }

    public void runOnTCP() {

        ServerSocket serverSocket;
        Socket socket = null;
        ObjectInputStream objectInputStream;

        NetworkCommand message = null;

        TCPAddress addr = (TCPAddress) address;

        try {
            serverSocket = new ServerSocket(addr.getPortNumber());

            while (true) {
                socket = serverSocket.accept();
                objectInputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                Object acceptedObject = objectInputStream.readObject();
                if (acceptedObject != null) {
                    message = (NetworkCommand) acceptedObject;
                }
                synchronized (System.out) {
                    if (message != null) {
//                        System.out.println(message.toString());
//                        System.out.flush();
                    }
                }
                this.roleInstance.handleMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void runOnUDP() {

    }
}