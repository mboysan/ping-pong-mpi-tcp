package network.messenger;

import config.Config;
import mpi.MPI;
import mpi.MPIException;
import network.ConnectionProtocol;
import network.address.Address;
import network.address.TCPAddress;
import org.pmw.tinylog.Logger;
import protocol.CommandMarshaller;
import protocol.commands.NetworkCommand;
import protocol.commands.ping.EndAll_NC;
import role.Role;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


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
                        Config.getInstance().readyEnd();
                        break;
                    }
                }
                this.roleInstance.handleMessage(message);
            }
        } catch (Exception e) {
            Logger.error("Recv err: " + e, e);
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception ex) {
                Logger.error("Socket close err: " + ex, ex);
            }
        }
    }

    private void runOnMPI(){
        try {
            while (true){
                byte[] msg= new byte[1024];
                MPI.COMM_WORLD.recv(msg, 1024, MPI.BYTE, MPI.ANY_SOURCE, MPI.ANY_TAG);
                NetworkCommand message = commandMarshaller.unmarshall(new String(msg, StandardCharsets.UTF_8));
                if(message instanceof EndAll_NC){
                    Logger.info("End signal recv: " + message);
                    Config.getInstance().readyEnd();
                    break;
                }
                roleInstance.handleMessage(message);
            }
        } catch (IOException | MPIException e) {
            Logger.error("Recv err: " + e, e);
        }
    }
}