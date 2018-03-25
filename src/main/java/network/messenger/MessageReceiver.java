package network.messenger;

import config.Config;
import mpi.MPI;
import network.address.Address;
import network.address.TCPAddress;
import org.pmw.tinylog.Logger;
import protocol.CommandMarshaller;
import protocol.commands.NetworkCommand;
import protocol.commands.ping.EndAll_NC;
import role.Role;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


public class MessageReceiver {

    private final Address address;
    private final Role roleInstance;
    private final CommandMarshaller commandMarshaller;

    public MessageReceiver(Address address, Role roleInstance) {
        this.address = address;
        this.roleInstance = roleInstance;
        this.commandMarshaller = new CommandMarshaller();

        switch (Config.getInstance().getConnectionProtocol()) {
            case TCP_CONNECTION:
                new TCPReceiver().start();
                break;
            case MPI_CONNECTION:
                new MPIReceiver().start();
                break;
        }
    }

    private class TCPReceiver extends Thread {
        @Override
        public void run() {
            runOnTCP();
        }

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
                            if(message instanceof EndAll_NC){
                                Logger.debug("End signal recv: " + message);
                                Config.getInstance().readyEnd();
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

    private class MPIReceiver extends Thread {
        @Override
        public void run() {
            runOnMPI();
        }

        private void runOnMPI(){
            try {
                while (true){
                    int[] msgInfo = new int[1];
//                  MPI.COMM_WORLD.recv(msgInfo, msgInfo.length, MPI.INT, MPI.ANY_SOURCE, MPI.ANY_TAG);   // receive msg length first.
                    msgInfo[0] = 1024;
                    byte[] msg = new byte[msgInfo[0]];
                    MPI.COMM_WORLD.recv(msg, msg.length, MPI.BYTE, MPI.ANY_SOURCE, MPI.ANY_TAG);

                    NetworkCommand message = commandMarshaller.unmarshall(new String(msg, StandardCharsets.UTF_8));
                    if(message instanceof EndAll_NC){
                        Logger.debug("End signal recv: " + message);
                        Config.getInstance().readyEnd();
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