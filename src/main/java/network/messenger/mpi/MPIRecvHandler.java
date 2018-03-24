package network.messenger.mpi;

import io.netty.util.internal.ConcurrentSet;
import mpi.MPI;
import mpi.MPIException;
import protocol.CommandMarshaller;
import protocol.commands.NetworkCommand;
import protocol.commands.ping.EndAll_NC;
import role.Role;

import java.io.IOException;

public class MPIRecvHandler extends Thread {

    private static MPIRecvHandler ourInstance = new MPIRecvHandler();

    private final ConcurrentSet<Role> roles;
    private final CommandMarshaller commandMarshaller;

    public static MPIRecvHandler getInstance() {
        return ourInstance;
    }

    private MPIRecvHandler() {
        roles = new ConcurrentSet<>();
        commandMarshaller = new CommandMarshaller();
        start();
    }

    public void registerNode(Role role){
        roles.add(role);
    }

    @Override
    public void run() {
        try {
            while (true){
                char[] msg= new char[1024];
                MPI.COMM_WORLD.recv(msg, 1024, MPI.BYTE, MPI.ANY_SOURCE, MPI.ANY_TAG);
                NetworkCommand message = commandMarshaller.unmarshall(new String(msg));
                if(message instanceof EndAll_NC){
                    System.out.println("End signal recv: " + message);
                    break;
                }
                for (Role role : roles) {
                    new Thread(() -> role.handleMessage(message)).start();
                }
            }
        } catch (IOException | MPIException e) {
            e.printStackTrace();
        }
    }
}
