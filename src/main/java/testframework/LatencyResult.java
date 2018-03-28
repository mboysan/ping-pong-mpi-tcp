package testframework;

public class LatencyResult implements IResult {
    /**
     * test group's name
     */
    private final String testGroupName;
    /**
     * test phase to record the result for
     */
    private final TestPhase testPhase;

    /**
     * Time stamp that the result is collected at.
     */
    private final long currentTimestamp;

    /**
     * Name of the result (may take the process id)
     */
    private final String name;
    /**
     * calculated latency
     */
    private final long latency;

    /**
     * @param testGroupName    test group's name
     * @param testPhase        test phase to record the result for
     * @param name             Name of the result (may take the process id)
     * @param currentTimestamp Time stamp that the result is collected at.
     * @param startTime        beginning time of the request
     * @param endTime          end time of the request
     */
    public LatencyResult(
            String testGroupName,
            TestPhase testPhase,
            String name,
            long currentTimestamp,
            long startTime,
            long endTime) {

        this.testGroupName = testGroupName;
        this.testPhase = testPhase;
        this.name = name;
        this.currentTimestamp = currentTimestamp;
        this.latency = calcLatency(startTime, endTime);
    }

    /**
     * Calculates the latency.
     * @param startTime beginning time of the request
     * @param endTime   end time of the request
     * @return
     */
    private long calcLatency(long startTime, long endTime){
        return endTime - startTime;
    }

    @Override
    public String getTestGroupName() {
        return testGroupName;
    }

    @Override
    public TestPhase getTestPhase() {
        return testPhase;
    }

    @Override
    public String CSVLine(){
        String lineSep = System.getProperty("line.separator");
        return String.format("%s,%s,%d,%d,%s,%d%n",
                testGroupName, testPhase.getName(), testPhase.getIterations(), currentTimestamp, name, latency);
    }
}
