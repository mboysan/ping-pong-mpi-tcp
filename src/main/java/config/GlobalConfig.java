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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
     * indicates if there are more than one node running on a single JVM. Meaning, if true, the nodes are initiated
     * in a single JVM and tests are done in that JVM. Otherwise, each node is assumed to have its own dedicated JVM.
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
    private MulticastAddress multicastAddress;

    /**
     * @return singleton instance, i.e. {@link #ourInstance}
     */
    public static GlobalConfig getInstance() {
        return ourInstance;
    }

    private GlobalConfig() {
        this.addresses = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public void initTCP(boolean isSingleJVM){
        initTCP(isSingleJVM, null);
    }

    /**
     * Initializes the system for TCP communication.
     */
    public void initTCP(boolean isSingleJVM, MulticastAddress multicastAddress) {
        this.multicastAddress = multicastAddress;
        init(TCP_CONNECTION, isSingleJVM);
    }

    /**
     * Initializes the system for MPI communication. The MPI addresses of the processes are registered
     * as a side effect.
     * @param args additional arguments for MPI
     * @throws MPIException if MPI could not be initiated
     */
    public void initMPI(String[] args) throws MPIException {
//        MPI.Init(args);
        int provided = MPI.InitThread(args, MPI.THREAD_SINGLE);
        Logger.info("Thread support level: " + provided);
        init(MPI_CONNECTION, false);
    }

    /**
     * @param connectionProtocol sets {@link #connectionProtocol}
     */
    private void init(ConnectionProtocol connectionProtocol, boolean isSingleJVM) {
        Logger.info(String.format("Init [%s, isSingleJVM:%s]", connectionProtocol.toString(), isSingleJVM));
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
                registerAddress(role.getAddress(), role);
            } else {
                NetworkCommand connect = new Connect_NC()
                        .setSenderAddress(role.getAddress());
                for (int i = 0; i < 5; i++) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        Logger.error(e);
                    }
                    Logger.debug("Multicasting nodes: " + connect);
                    getMulticaster(role).multicast(connect);
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        Logger.error(e);
                    }
                }
            }
        } else if(connectionProtocol == MPI_CONNECTION){
            /* first register self */
            registerAddress(role.getAddress(), role);
            for (int i = 0; i < getProcessCount(); i++) {
                /* register others. If address is same with self, returns anyways. */
                registerAddress(new MPIAddress(i, ((MPIAddress)role.getAddress()).getGroupId()), role);
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
                    multicaster = new Multicaster(multicastAddress, role);
                } catch (IllegalArgumentException e){
                    Logger.error(e);
                }
            }
        }
        return multicaster;
    }

    /**
     * Adds address to the set of address. If address already exist returns without doing anything.
     * @param toRegister address to register
     * @param roleRef    reference to the role. Used to modify its properties based on the addresses change.
     */
    public synchronized void registerAddress(Address toRegister, Role roleRef){
        boolean isNew = true;
        for (Address address : addresses) {
            if(address.isSame(toRegister)){
                isNew = false;
                break;
            }
        }
        if(isNew){
            addresses.add(toRegister);
            electAsLeader(roleRef);
            if(isSingleJVM){
                resetEndLatch(getProcessCount());
            } else {
                resetEndLatch(1);
            }
            Logger.info(String.format("Address [%s] registered on role [%s]", toRegister, roleRef));
        }
    }

    /**
     * Assigns the role as the leader if its address is in index 0 of the sorted addresses.
     * @param role role to assign as leader if applicable.
     */
    private void electAsLeader(Role role) {
        List<String> addressesAsStrings = addresses.stream()
                .map(Address::toString).sorted(new Comparator<String>() {
                    public int compare(String o1, String o2) {
                        return o1.compareTo(o2);
                    }
                }).collect(Collectors.toList());
        if(addressesAsStrings.get(0).equals(role.getAddress().toString())){
            role.setLeader(true);
        } else {
            role.setLeader(false);
        }
    }

    /**
     * Resets the {@link #endLatch} with the given <tt>count</tt>.
     * @param count the count of the existing processes to wait for,
     */
    private synchronized void resetEndLatch(int count){
        Logger.debug("Resetting endLatch with count: " + count);
        endLatch = new CountDownLatch(count);
    }

    /**
     * Signal the end cycle.
     */
    public void readyEnd() {
        Logger.debug(String.format("Entering end cycle [%s]", connectionProtocol));
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
        }
        Logger.info(String.format("Finalized [%s]", connectionProtocol));
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
