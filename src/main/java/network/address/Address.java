package network.address;

import java.io.Serializable;

public abstract class Address implements Serializable {

    public Address() {
    }

    @Override
    public String toString() {
        return "Address{}";
    }
}
