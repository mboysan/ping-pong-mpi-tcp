package testframework;

import org.pmw.tinylog.Logger;
import role.Node;

/**
 * A simple class for keeping track of test data.
 */
public class TestFramework {

    private final Node pinger;
    private final int totalProcesses;
    private final ResultCollector resultCollector;

    public TestFramework(Node pinger, int totalProcesses) {
        this.pinger = pinger;
        this.totalProcesses = totalProcesses;
        this.resultCollector = new ResultCollector();
    }

    public void initTests(){
        Logger.info("Starting ping-pong tests...");

        resultCollector.addResult(ResultCollector.PHASE_WARMUP, loopPing(pinger, 10));
        resultCollector.addResult(ResultCollector.PHASE_FULL_LOAD, loopPing(pinger, 100));

        Logger.info("Tests are done!");
    }

    public void printOnConsole(String phase){
        resultCollector.printOnConsole(phase);
    }

    /**
     * Starts sending ping requests, and when all the pongs are received the results are recorded.
     * This is repeated <tt>loopCount</tt> times and the average latency is recorded.
     *
     * @param pinger    pinger instance
     * @param loopCount number of times to send ping-pong requests
     */
    public LatencyResult loopPing(Node pinger, int loopCount) {
        long[] results = new long[loopCount];
        for (int i = 0; i < loopCount; i++) {
            long start = System.currentTimeMillis();
            pinger.pingAll();
            pinger.waitPongs();
            long end = System.currentTimeMillis() - start;
            results[i] = end;
        }
        return new LatencyResult(totalProcesses, results);
    }
}
