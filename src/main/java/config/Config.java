package config;

import network.ConnectionProtocol;
import network.address.Address;
import role.Role;

public class Config {
    private static Config ourInstance = new Config();

    private Address[] addresses;
    private ConnectionProtocol connectionProtocol;

    public static Config getInstance() {
        return ourInstance;
    }

    private Config() {
    }

    public void init(ConnectionProtocol connectionProtocol, Role[] roles) {
        Address[] addresses = new Address[roles.length];
        for (int i = 0; i < roles.length; i++) {
            addresses[i] = roles[i].getMyAddress();
        }
        init(connectionProtocol, addresses);
    }

    public void init(ConnectionProtocol connectionProtocol, Address[] addresses) {
        this.connectionProtocol = connectionProtocol;
        this.addresses = addresses;
    }

    public Address[] getAddresses() {
        return addresses;
    }

    public ConnectionProtocol getConnectionProtocol() {
        return connectionProtocol;
    }
}
