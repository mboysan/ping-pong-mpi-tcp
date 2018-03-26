package testframework;

public class LatencyResult implements IResult {
    private final int numberOfProcesses;
    private final long[] latencies;

    private final long averageLatency;
    private final int resultsTotal;

    public LatencyResult(int numberOfProcesses, long[] latencies) {
        this.numberOfProcesses = numberOfProcesses;
        this.latencies = latencies;

        this.averageLatency = calcAverage(latencies);
        this.resultsTotal = latencies.length;
    }

    public int getNumberOfProcesses() {
        return numberOfProcesses;
    }

    public long[] getLatencies() {
        return latencies;
    }

    public long getAverageLatency() {
        return averageLatency;
    }

    public int getResultsTotal() {
        return resultsTotal;
    }

    /**
     * Calculates average latency with the given results.
     * @param results collection of latency results.
     * @return average latency from the collected results.
     */
    public static long calcAverage(long[] results){
        long avg = 0;
        for (long result : results) {
            avg += result;
        }
        return avg / results.length;
    }

    @Override
    public String printlnCSV() {
        return String.format("%d,%d,%d\r\n", numberOfProcesses, averageLatency, resultsTotal);
    }
}
