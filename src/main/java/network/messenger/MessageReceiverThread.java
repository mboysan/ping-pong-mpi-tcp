package network.messenger;

import config.Config;
import network.ConnectionProtocol;
import network.address.Address;
import network.address.TCPAddress;
import network.messenger.mpi.MPIRecvHandler;
import protocol.CommandMarshaller;
import protocol.commands.NetworkCommand;
import protocol.commands.ping.EndAll_NC;
import role.Role;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class MessageReceiverThread extends Thread {

    private final ConnectionProtocol runOnTCPOrMPI;
    private final Address address;
    private final Role roleInstance;
    private final CommandMarshaller commandMarshaller;

    public MessageReceiverThread(Address address, Role roleInstance) {
        this.address = address;
        this.runOnTCPOrMPI = Config.getInstance().getConnectionProtocol();
        this.roleInstance = roleInstance;
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
                    if(message instanceof EndAll_NC){
                        System.out.println("End signal recv: " + message);
                        break;
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

    private void runOnMPI(){
        MPIRecvHandler.getInstance().registerNode(roleInstance);
    }
}