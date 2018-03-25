package config;

import mpi.MPI;
import mpi.MPIException;
import network.ConnectionProtocol;
import network.address.Address;
import network.address.MPIAddress;
import network.address.TCPAddress;
import org.pmw.tinylog.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static network.ConnectionProtocol.MPI_CONNECTION;
import static network.ConnectionProtocol.TCP_CONNECTION;

public class Config {
    private static Config ourInstance = new Config();

    private Address[] addresses;
    private ConnectionProtocol connectionProtocol;
    private CountDownLatch endLatch;

    public static Config getInstance() {
        return ourInstance;
    }

    private Config() {
    }

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

    private void init(ConnectionProtocol connectionProtocol, Address[] addresses) {
        this.connectionProtocol = connectionProtocol;
        this.addresses = addresses;
    }

    public void readyEnd() {
        endLatch.countDown();
    }

    public void end() throws MPIException, InterruptedException {
        endLatch.await(10, TimeUnit.SECONDS);
        if (connectionProtocol == MPI_CONNECTION) {
            MPI.Finalize();
            Logger.info("MPI finalized!");
        } else {
            Logger.info("TCP finalized!");
        }
    }

    public Address[] getAddresses() {
        return addresses;
    }

    public ConnectionProtocol getConnectionProtocol() {
        return connectionProtocol;
    }
}
