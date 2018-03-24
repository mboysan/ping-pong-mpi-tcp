package protocol;

import network.address.Address;

import java.io.Serializable;

public class NetworkCommand implements Serializable {

    private Address senderAddress;
    private Address receiverAddress;
    private int tag;

    public NetworkCommand() {

    }

    public NetworkCommand setSenderAddress(Address senderAddress) {
        this.senderAddress = senderAddress;
        return this;
    }

    public NetworkCommand setReceiverAddress(Address receiverAddress) {
        this.receiverAddress = receiverAddress;
        return this;
    }

    public Address getSenderAddress() {
        return senderAddress;
    }

    public Address getReceiverAddress() {
        return receiverAddress;
    }

    public int getTag() {
        return tag;
    }

    public NetworkCommand setTag(int tag) {
        this.tag = tag;
        return this;
    }

    @Override
    public String toString() {
        return "NetworkCommand{" +
                "senderAddress=" + senderAddress +
                ", receiverAddress=" + receiverAddress +
                ", tag=" + tag +
                '}';
    }
}
