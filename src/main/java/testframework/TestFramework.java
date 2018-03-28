package testframework;

import org.pmw.tinylog.Logger;
import role.Node;

/**
 * A simple class for keeping track of test data.
 */
public class TestFramework {

    private final TestResultCollector testResultCollector;

    public TestFramework() {
        this.testResultCollector = TestResultCollector.getInstance();
    }

    /**
     * Initializes the ping tests
     * @param pinger         Node that will do all the ping tests
     * @param processCount   total number of processes in system
     * @return this
     */
    public TestFramework initPingTests(Node pinger, int processCount){
        Logger.info("Starting ping-pong tests...");

        testResultCollector.addResult(loopPing("pingAll", TestPhase.PHASE_WARMUP, pinger, processCount));
        testResultCollector.addResult(loopPing("pingAll", TestPhase.PHASE_FULL_LOAD, pinger, processCount));

        Logger.info("Tests are done!");
        return this;
    }

    /**
     * Prints the results to console.
     * @param testGroupName test group to print on console
     * @param phase         phase of the test group to print
     */
    public void printOnConsole(String testGroupName, TestPhase phase){
        testResultCollector.printOnConsole(testGroupName, phase);
    }

    /**
     * Prints all the results collected to console.
     */
    public void printAllOnConsole(){
        printOnConsole(null,null);
    }

    /**
     * Starts sending ping requests, and when all the pongs are received the results are recorded.
     * This is repeated {@link TestPhase#getIterations()} times and the average latency is recorded.
     *
     * @param testGroupName test group's name
     * @param testPhase test phase.
     * @param pinger    pinger instance
     */
    public OverallLatencyResult loopPing(String testGroupName, TestPhase testPhase, Node pinger, int totalProcesses) {
        int loopCount = testPhase.getIterations();
        long[] results = new long[loopCount];
        for (int i = 0; i < loopCount; i++) {
            long start = System.currentTimeMillis();
            pinger.pingAll();
            pinger.waitPongs();
            long end = System.currentTimeMillis() - start;
            results[i] = end;
        }
        return new OverallLatencyResult(testGroupName, testPhase, totalProcesses, results);
    }
}
