package config;

import mpi.MPI;
import mpi.MPIException;
import network.ConnectionProtocol;
import network.address.*;
import network.messenger.Multicaster;
import org.pmw.tinylog.Logger;
import protocol.commands.NetworkCommand;
import protocol.commands.ping.Connect_NC;
import role.Role;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static network.ConnectionProtocol.MPI_CONNECTION;
import static network.ConnectionProtocol.TCP_CONNECTION;

/**
 * Global configuration class.
 */
public class GlobalConfig {
    static {
        LoggerConfig.configureLogger();
    }

    /**
     * Singleton instance
     */
    private static GlobalConfig ourInstance = new GlobalConfig();

    /**
     * indicates if the java process runs on a single JVM or not.
     */
    private boolean isSingleJVM = false;
    /**
     * List of addresses of the host processes
     */
    private Set<Address> addresses;
    /**
     * Connection protocol to use
     */
    private ConnectionProtocol connectionProtocol;
    /**
     * Latch for keeping track of the end cycle.
     */
    private CountDownLatch endLatch;

    private Multicaster multicaster;

    /**
     * @return singleton instance, i.e. {@link #ourInstance}
     */
    public static GlobalConfig getInstance() {
        return ourInstance;
    }

    private GlobalConfig() {
        this.addresses = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    /**
     * Initializes the system for TCP communication.
     */
    public void initTCP(boolean isSingleJVM) {
        init(TCP_CONNECTION, isSingleJVM);
    }

    /**
     * Initializes the system for MPI communication. The MPI addresses of the processes are registered
     * as a side effect.
     * @param args additional arguments for MPI
     * @throws MPIException if MPI could not be initiated
     */
    public void initMPI(String[] args) throws MPIException {
        MPI.Init(args);
        init(MPI_CONNECTION, true);
    }

    /**
     * @param connectionProtocol sets {@link #connectionProtocol}
     */
    private void init(ConnectionProtocol connectionProtocol, boolean isSingleJVM) {
        this.connectionProtocol = connectionProtocol;
        this.isSingleJVM = isSingleJVM;
        resetEndLatch(1);   // only 1 receiver per jvm
    }

    /**
     * Registers a {@link Role}. Used when a new node is joined.
     * @param role the role to register.
     */
    public void registerRole(Role role){
        Logger.info("Registering role: " + role);
        if (connectionProtocol == TCP_CONNECTION){
            if(isSingleJVM) {
                /* No multicasting is needed, just add addresses */
                registerAddress(role.getAddress());
            } else {
                NetworkCommand connect = new Connect_NC()
                        .setSenderAddress(role.getAddress());
                for (int i = 0; i < 5; i++) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Logger.error(e);
                    }
                    Logger.debug("Multicasting nodes: " + connect);
                    getMulticaster(role).multicast(connect);
                }
            }
        } else if(connectionProtocol == MPI_CONNECTION){
            for (int i = 0; i < getProcessCount(); i++) {
                registerAddress(new MPIAddress(i));
            }
        }
    }

    /**
     * If the active connection is TCP connection, creates a multicaster that will send broadcast messages.
     * @param role role for handling multicast messages received.
     * @return created multicaster if applicable, null otherwise.
     */
    private Multicaster getMulticaster(Role role){
        if(connectionProtocol == TCP_CONNECTION){
            if(multicaster == null){
                try {
                    multicaster = new Multicaster(
                            new MulticastAddress("233.0.0.0", 9999), role);
                } catch (UnknownHostException e) {
                    Logger.error(e, "multicaster could not be started.");
                }
            }
        }
        return multicaster;
    }

    /**
     * Adds address to the set of address
     * @param toRegister address to register
     */
    public synchronized void registerAddress(Address toRegister){
        boolean isNew = true;
        for (Address address : addresses) {
            if(address.isSame(toRegister)){
                isNew = false;
                break;
            }
        }
        if(isNew){
            Logger.info("Address registered: " + toRegister);
            addresses.add(toRegister);
            if(isSingleJVM){
                resetEndLatch(getProcessCount());
            } else {
                resetEndLatch(1);
            }
        }
    }

    /**
     * Resets the {@link #endLatch} with the given <tt>count</tt>.
     * @param count the count of the existing processes to wait for,
     */
    private synchronized void resetEndLatch(int count){
        endLatch = new CountDownLatch(count);
    }

    /**
     * Signal the end cycle.
     */
    public void readyEnd() {
        if(multicaster != null) {
            multicaster.shutdown();
        }
        endLatch.countDown();
    }

    /**
     * Ends everything.
     * @throws MPIException if MPI could not be finalized
     * @throws InterruptedException in case operations on {@link #endLatch} fails.
     */
    public void end() throws MPIException, InterruptedException {
        Logger.debug("Waiting end cycle with count: " + endLatch.getCount());
        endLatch.await(1, TimeUnit.MINUTES);
        if (connectionProtocol == MPI_CONNECTION) {
            MPI.Finalize();
            Logger.info("MPI finalized!");
        } else {
            Logger.info("TCP finalized!");
        }
    }

    /**
     * @return gets {@link #addresses}
     */
    public Set<Address> getAddresses() {
        return addresses;
    }

    /**
     * @return the length of the {@link #addresses} array.
     */
    public int getProcessCount(){
        try{
            if(connectionProtocol == MPI_CONNECTION){
                synchronized (MPI.COMM_WORLD) {
                    return MPI.COMM_WORLD.getSize();
                }
            } else if(connectionProtocol == TCP_CONNECTION) {
                return getAddresses().size();
            }
        } catch (Exception e) {
            Logger.error(e, "Could not determine process count");
        }
        return -1;
    }

    /**
     * @return gets {@link #connectionProtocol}
     */
    public ConnectionProtocol getConnectionProtocol() {
        return connectionProtocol;
    }

    private String addressesToString(){
        String[] arr = new String[getAddresses().size()];
        int idx = 0;
        for (Address address : getAddresses()) {
            arr[idx] = address.toString();
            idx++;
        }
        return Arrays.toString(arr);
    }
}
