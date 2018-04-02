package testframework;

import org.pmw.tinylog.Logger;
import role.Node;
import testframework.result.LatencyResult;
import testframework.result.OverallLatencyResult;

/**
 * A simple class for keeping track of test data.
 */
public class TestFramework {

    /**
     * Test framework singleton instance.
     */
    private static TestFramework pingTester = new TestFramework();

    /**
     * test result collector
     */
    private final ResultCollector resultCollector = new ResultCollector();

    public static boolean isTesting = false;

    private TestFramework() {
    }

    /**
     * Initializes the ping tests. If old tests exist, creates new ones and does not do anything with the old ones.
     * They continue until they finish.
     * @param pinger         Node that will do all the ping tests
     * @param processCount   total number of processes in system
     * @return this
     */
    public static TestFramework doPingTests(Node pinger, int processCount){
        isTesting = true;
        if(pingTester == null){
            pingTester = new TestFramework();
        }
        pingTester._doPingTests(pinger, processCount);
        return pingTester;
    }

    /**
     * Do ping tests.
     */
    private void _doPingTests(Node pinger, int processCount){
        Logger.info("Starting ping-pong tests...");

        resultCollector.addResult(loopPing("pingAll", TestPhase.PHASE_WARMUP, pinger, processCount));
        resultCollector.addResult(loopPing("pingAll", TestPhase.PHASE_FULL_LOAD, pinger, processCount));

        resultCollector.finalizeCollection();

        Logger.info("Ping tests are done!");
    }

    /**
     * Starts sending ping requests, and when all the pongs are received the results are recorded.
     * This is repeated {@link TestPhase#getIterations()} times and the average latency is recorded.
     *
     * @param testGroupName test group's name
     * @param testPhase test phase.
     * @param pinger    pinger instance
     */
    private OverallLatencyResult loopPing(String testGroupName, TestPhase testPhase, Node pinger, int totalProcesses) {
        int loopCount = testPhase.getIterations();

        int totalPingCount = totalProcesses * loopCount;
        resultCollector.setTaskCountForTest("pingSingle", totalPingCount);

        long[] results = new long[loopCount];
        for (int i = 0; i < loopCount; i++) {
            long start = System.currentTimeMillis();
            pinger.pingAll();
            pinger.waitPongs();
            long end = System.currentTimeMillis() - start;
            results[i] = end;
        }

        resultCollector.waitAllTasksFor("pingSingle");

        return new OverallLatencyResult(testGroupName, testPhase, totalProcesses, results);
    }

    /**
     * Prints the results to console.
     * @param testGroupName test group to print on console
     * @param phase         phase of the test group to print
     */
    public void printOnConsole(String testGroupName, TestPhase phase){
        resultCollector.printOnConsole(testGroupName, phase);
    }

    /**
     * Prints all the results collected to console.
     */
    public void printAllOnConsole(){
        printOnConsole(null,null);
    }

    /**
     * Adds latency result in an async manner.
     * @param latencyResult latency result to add
     */
    public static void addLatencyResult(LatencyResult latencyResult){
        if(pingTester != null){
            pingTester.resultCollector.addResultAsync(latencyResult);
        }
    }
}
