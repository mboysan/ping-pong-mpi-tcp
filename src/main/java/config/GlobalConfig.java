package config;

import mpi.MPI;
import mpi.MPIException;
import network.ConnectionProtocol;
import network.address.Address;
import network.address.MPIAddress;
import network.address.TCPAddress;
import org.pmw.tinylog.Logger;
import role.Role;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
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
    private boolean isSingleJVM;

    /**
     * Count of the total processes in the system.
     */
    private int processCount;

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
    public void initTCP(boolean isSingleJVM, int processCount) {
        init(TCP_CONNECTION, isSingleJVM, processCount);
    }

    /**
     * Initializes the system for MPI communication. The MPI addresses of the processes are registered
     * as a side effect.
     * @param args additional arguments for MPI
     * @throws MPIException if MPI could not be initiated
     */
    public void initMPI(String[] args) throws MPIException {
        MPI.Init(args);

        resetEndLatch(1);   // only 1 receiver per jvm
        init(MPI_CONNECTION, true, MPI.COMM_WORLD.getSize());
    }

    /**
     * @param connectionProtocol sets {@link #connectionProtocol}
     */
    private void init(ConnectionProtocol connectionProtocol, boolean isSingleJVM, int processCount) {
        this.connectionProtocol = connectionProtocol;
        this.isSingleJVM = isSingleJVM;
        this.processCount = processCount;
    }

    /**
     * Registers a {@link Role} by taking it's address and adding it to the {@link #addresses} collection.
     * Used when a new node is joined.
     * If the {@link #connectionProtocol} &eq; {@link ConnectionProtocol#TCP_CONNECTION} resets the {@link #endLatch}.
     * @param role the role to register.
     */
    public void registerRole(Role role){
        //TODO: a more affective way of handling node registering.
        Address roleAddress = role.getAddress();
        if (connectionProtocol == TCP_CONNECTION){
            if(isSingleJVM) {
                addresses.add(roleAddress);
                resetEndLatch(getProcessCount());
            } else {
                /* if not running on the same JVM, then this method is called once, and all the other address
                   need to be added. */
                String ip = "127.0.0.1";
                int port = 8080;
                for (int i = 0; i < processCount; i++) {
                    try {
                        TCPAddress address = new TCPAddress(ip, port + i);
                        addresses.add(address);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
                resetEndLatch(1);
            }
        } else if(connectionProtocol == MPI_CONNECTION){
            for (int i = 0; i < processCount; i++) {
                addresses.add(new MPIAddress(i));
            }
            resetEndLatch(1);
        }

        Logger.debug("Role registered, addresses: " + addressesToString());
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
        endLatch.countDown();
    }

    /**
     * Ends everything.
     * @throws MPIException if MPI could not be finalized
     * @throws InterruptedException in case operations on {@link #endLatch} fails.
     */
    public void end() throws MPIException, InterruptedException {
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
        return processCount;
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
