package network.address;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TCPAddress extends Address {

    private InetAddress ip;
    private int portNumber;

    public TCPAddress(String ipStr, int portNumber) throws UnknownHostException {
        this(InetAddress.getByName(ipStr), portNumber);
    }

    public TCPAddress(InetAddress ip, int portNumber) {
        this.ip = ip;
        this.portNumber = portNumber;
    }

    public TCPAddress() {
    }

    public TCPAddress setIp(InetAddress ip) {
        this.ip = ip;
        return this;
    }

    public TCPAddress setPortNumber(int portNumber) {
        this.portNumber = portNumber;
        return this;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPortNumber() {
        return portNumber;
    }

    @Override
    public String toString() {
        return "TCPAddress{" +
                "ip=" + ip +
                ", portNumber=" + portNumber +
                '}';
    }
}
