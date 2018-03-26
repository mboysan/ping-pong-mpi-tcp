package network.address;

/**
 * Defines an MPI protocol host address
 */
public class MPIAddress extends Address {

    /**
     * Rank of the process
     */
    private int rank;

    /**
     * @param rank rank of the process
     */
    public MPIAddress(int rank) {
        this.rank = rank;
    }

    public MPIAddress(){

    }

    /**
     * @param rank sets {@link #rank}
     * @return this
     */
    public MPIAddress setRank(int rank) {
        this.rank = rank;
        return this;
    }

    /**
     * @return gets {@link #rank}
     */
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
