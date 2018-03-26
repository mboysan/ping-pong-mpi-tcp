package config;

import mpi.MPI;
import mpi.MPIException;
import network.ConnectionProtocol;
import network.address.Address;
import network.address.MPIAddress;
import network.address.TCPAddress;
import org.pmw.tinylog.Logger;

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
public class Config {
    /**
     * Singleton instance
     */
    private static Config ourInstance = new Config();

    /**
     * List of addresses of the host processes
     */
    private Set<Address> addresses = Collections.newSetFromMap(new ConcurrentHashMap<>());
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
    public static Config getInstance() {
        return ourInstance;
    }

    private Config() {
    }

    /**
     * Initializes the system for TCP communication.
     * @param addresses list of TCP addresses
     */
    public void initTCP(Address[] addresses) {
        if (addresses == null) {
            throw new IllegalArgumentException("Addresses must not be null");
        }
        for (Address address : addresses) {
            if (!(address instanceof TCPAddress)) {
                throw new IllegalArgumentException("Address must be of type " + TCPAddress.class.toString());
            }
        }
        endLatch = new CountDownLatch(addresses.length);
        init(TCP_CONNECTION, addresses);
    }

    /**
     * Initializes the system for MPI communication.
     * @param args additional arguments for MPI
     * @throws MPIException if MPI could not be initiated
     */
    public void initMPI(String[] args) throws MPIException {
        MPI.Init(args);

        int size = MPI.COMM_WORLD.getSize();
        Address[] addresses = new Address[size];
        for (int i = 0; i < size; i++) {
            Address addr = new MPIAddress(i);
            addresses[i] = addr;
        }

        endLatch = new CountDownLatch(1);   // only 1 receiver per jvm
        init(MPI_CONNECTION, addresses);
    }

    /**
     * @param connectionProtocol sets {@link #connectionProtocol}
     * @param addresses          sets {@link #addresses}
     */
    private void init(ConnectionProtocol connectionProtocol, Address[] addresses) {
        this.connectionProtocol = connectionProtocol;
        for (Address address : addresses) {
            registerAddress(address);
        }
    }

    /**
     * @return the length of the {@link #addresses} array.
     */
    public int getAddressCount(){
        if(addresses != null){
            return addresses.size();
        }
        return -1;
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
     * Registers a given address to the {@link #addresses} collection. Used when a new node is joined.
     * @param address the address to add to {@link #addresses}.
     */
    public void registerAddress(Address address){
        addresses.add(address);
    }

    /**
     * Unregisters a given address from the {@link #addresses} collection. Used when an existing node is removed.
     * @param address the address to remove.
     */
    public void unregisterAddress(Address address){
        addresses.remove(address);
    }

    /**
     * @return gets {@link #connectionProtocol}
     */
    public ConnectionProtocol getConnectionProtocol() {
        return connectionProtocol;
    }
}
