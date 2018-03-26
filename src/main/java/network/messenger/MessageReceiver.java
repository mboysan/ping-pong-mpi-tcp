package network.messenger;

import config.GlobalConfig;
import mpi.MPI;
import network.address.Address;
import network.address.TCPAddress;
import org.pmw.tinylog.Logger;
import protocol.CommandMarshaller;
import protocol.commands.NetworkCommand;
import protocol.commands.ping.SignalEnd_NC;
import role.Role;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * The message receiver wrapper for the communication protocols defined in {@link network.ConnectionProtocol}.
 */
public class MessageReceiver {

    /**
     * The address to receive message from.
     */
    private final Address address;
    /**
     * The {@link Role} to send the received message for processing.
     */
    private final Role roleInstance;
    /**
     * Command marshaller to unmarshall the received message.
     */
    private final CommandMarshaller commandMarshaller;

    /**
     * Initializes the message receiver. It then creates the appropriate handler to handle the received message.
     * @param address      sets {@link #address}
     * @param roleInstance sets {@link #roleInstance}
     */
    public MessageReceiver(Address address, Role roleInstance) {
        this.address = address;
        this.roleInstance = roleInstance;
        this.commandMarshaller = new CommandMarshaller();

        switch (GlobalConfig.getInstance().getConnectionProtocol()) {
            case TCP_CONNECTION:
                new TCPReceiver().start();
                break;
            case MPI_CONNECTION:
                new MPIReceiver().start();
                break;
        }
    }

    /**
     * TCP recv handler
     */
    private class TCPReceiver extends Thread {
        @Override
        public void run() {
            runOnTCP();
        }

        /**
         * Recv message with TCP
         */
        private void runOnTCP() {
            ServerSocket serverSocket;
            Socket socket = null;
            try {
                TCPAddress addr = (TCPAddress) address;
                serverSocket = new ServerSocket(addr.getPortNumber());
                while (true) {
                    socket = serverSocket.accept();
                    DataInputStream dIn = new DataInputStream(socket.getInputStream());
                    int length = dIn.readInt(); // read length of incoming message
                    byte[] msg = null;
                    if(length>0) {
                        msg = new byte[length];
                        dIn.readFully(msg, 0, msg.length); // read the message
                    }
                    if(msg != null){
                        NetworkCommand message = commandMarshaller.unmarshall(new String(msg, StandardCharsets.UTF_8));
                        if(message != null){
                            if(message instanceof SignalEnd_NC){
                                Logger.debug("End signal recv: " + message);
                                GlobalConfig.getInstance().readyEnd();
                                break;
                            }
                            roleInstance.handleMessage(message);
                        }
                    }
                }
            } catch (Exception e) {
                Logger.error(e, "Recv err");
            } finally {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (Exception ex) {
                    Logger.error(ex, "Socket close err");
                }
            }
        }
    }

    /**
     * MPI recv handler
     */
    private class MPIReceiver extends Thread {
        @Override
        public void run() {
            runOnMPI();
        }

        /**
         * Recv message with MPI
         */
        private void runOnMPI(){
            try {
                while (true){
                    int[] msgInfo = new int[1];
//                  MPI.COMM_WORLD.recv(msgInfo, msgInfo.length, MPI.INT, MPI.ANY_SOURCE, MPI.ANY_TAG);   // receive msg length first.
                    msgInfo[0] = 1024;
                    byte[] msg = new byte[msgInfo[0]];
                    MPI.COMM_WORLD.recv(msg, msg.length, MPI.BYTE, MPI.ANY_SOURCE, MPI.ANY_TAG);

                    NetworkCommand message = commandMarshaller.unmarshall(new String(msg, StandardCharsets.UTF_8));
                    if(message instanceof SignalEnd_NC){
                        Logger.debug("End signal recv: " + message);
                        GlobalConfig.getInstance().readyEnd();
                        break;
                    }
                    roleInstance.handleMessage(message);
                }
            } catch (Exception e) {
                Logger.error(e, "Recv err");
            }
        }
    }
}