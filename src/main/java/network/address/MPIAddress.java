package network.address;

public class MPIAddress extends Address {

    private int rank;

    public MPIAddress(int rank) {
        this.rank = rank;
    }

    public MPIAddress(){

    }

    public MPIAddress setRank(int rank) {
        this.rank = rank;
        return this;
    }

    public int getRank() {
        return rank;
    }

    @Override
    public String toString() {
        return "MPIAddress{" +
                "rank=" + rank +
                '}';
    }
}
