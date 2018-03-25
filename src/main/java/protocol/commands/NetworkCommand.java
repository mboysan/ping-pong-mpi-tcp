package protocol.commands;

import network.address.Address;
import network.address.MPIAddress;
import network.address.TCPAddress;

import java.io.Serializable;

public class NetworkCommand implements Serializable {

    private TCPAddress receiverAddrTCP;
    private TCPAddress senderAddrTCP;
    private MPIAddress receiverAddrMPI;
    private MPIAddress senderAddrMPI;
    private int tag = MessageTag.ANY_TAG.getTagValue();  //set default tag
    private long timeStamp = System.currentTimeMillis();

    public NetworkCommand() {

    }

    public NetworkCommand setSenderAddress(Address addr) {
        if(addr instanceof TCPAddress){
            setSenderAddrTCP((TCPAddress) addr);
        } else if(addr instanceof MPIAddress){
            setSenderAddrMPI((MPIAddress) addr);
        }
        return this;
    }

    public NetworkCommand setReceiverAddress(Address receiverAddress) {
        if(receiverAddress instanceof TCPAddress){
            setReceiverAddrTCP((TCPAddress) receiverAddress);
        } else if(receiverAddress instanceof MPIAddress){
            setReceiverAddrMPI((MPIAddress) receiverAddress);
        }
        return this;
    }

    public Address resolveSenderAddress() {
        if(getSenderAddrTCP() != null){
            return getSenderAddrTCP();
        } else if(getSenderAddrMPI() != null){
            return getSenderAddrMPI();
        }
        return null;
    }

    public Address resolveReceiverAddress() {
        if(getReceiverAddrTCP() != null){
            return getReceiverAddrTCP();
        } else if(getReceiverAddrMPI() != null){
            return getReceiverAddrMPI();
        }
        return null;
    }

    public TCPAddress getReceiverAddrTCP() {
        return receiverAddrTCP;
    }

    public NetworkCommand setReceiverAddrTCP(TCPAddress receiverAddrTCP) {
        this.receiverAddrTCP = receiverAddrTCP;
        return this;
    }

    public TCPAddress getSenderAddrTCP() {
        return senderAddrTCP;
    }

    public NetworkCommand setSenderAddrTCP(TCPAddress senderAddrTCP) {
        this.senderAddrTCP = senderAddrTCP;
        return this;
    }

    public MPIAddress getReceiverAddrMPI() {
        return receiverAddrMPI;
    }

    public NetworkCommand setReceiverAddrMPI(MPIAddress receiverAddrMPI) {
        this.receiverAddrMPI = receiverAddrMPI;
        return this;
    }

    public MPIAddress getSenderAddrMPI() {
        return senderAddrMPI;
    }

    public NetworkCommand setSenderAddrMPI(MPIAddress senderAddrMPI) {
        this.senderAddrMPI = senderAddrMPI;
        return this;
    }

    public int getTag() {
        return tag;
    }

    public NetworkCommand setTag(int tag) {
        this.tag = tag;
        return this;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return "NetworkCommand{" +
                "receiverAddrTCP=" + receiverAddrTCP +
                ", senderAddrTCP=" + senderAddrTCP +
                ", receiverAddrMPI=" + receiverAddrMPI +
                ", senderAddrMPI=" + senderAddrMPI +
                ", tag=" + tag +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
