package network.address;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Defines a TCP address
 */
public class TCPAddress extends Address {

    /**
     * Ip address
     */
    private InetAddress ip;
    /**
     * Port number
     */
    private int portNumber;

    /**
     * @param ipStr      ip address as string
     * @param portNumber port number
     * @throws UnknownHostException if the <tt>ipStr</tt> could not be parsed to {@link InetAddress}.
     */
    public TCPAddress(String ipStr, int portNumber) throws UnknownHostException {
        this(InetAddress.getByName(ipStr), portNumber);
    }

    /**
     * @param ip         ip address
     * @param portNumber port number
     */
    public TCPAddress(InetAddress ip, int portNumber) {
        this.ip = ip;
        this.portNumber = portNumber;
    }

    public TCPAddress() {
    }

    /**
     * @param ip sets {@link #ip}
     * @return this
     */
    public TCPAddress setIp(InetAddress ip) {
        this.ip = ip;
        return this;
    }

    /**
     * @param portNumber sets {@link #portNumber}
     * @return this
     */
    public TCPAddress setPortNumber(int portNumber) {
        this.portNumber = portNumber;
        return this;
    }

    /**
     * @return gets {@link #ip}
     */
    public InetAddress getIp() {
        return ip;
    }

    /**
     * @return gets {@link #portNumber}
     */
    public int getPortNumber() {
        return portNumber;
    }

    @Override
    public String resolveAddressId() {
        return getIp() + "_" + getPortNumber();
    }

    @Override
    public boolean isSame(Address other) {
        TCPAddress address = (TCPAddress) other;
        return address.getIp().toString().equals(this.getIp().toString())
                && address.getPortNumber() == this.getPortNumber();
    }

    @Override
    public String toString() {
        return "TCPAddress{" +
                "ip=" + ip +
                ", portNumber=" + portNumber +
                '}';
    }
}
