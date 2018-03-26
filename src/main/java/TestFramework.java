import org.pmw.tinylog.Logger;
import role.Node;

/**
 * A simple class for keeping track of test data.
 */
public class TestFramework {

    /**
     * Starts sending ping requests, and when all the pongs are received the results are recorded.
     * This is repeated <tt>loopCount</tt> times and the average latency is recorded.
     *
     * @param pinger    pinger instance
     * @param loopCount number of times to send ping-pong requests
     */
    public static void loopPing(Node pinger, int loopCount) {
        long[] results = new long[loopCount];
        for (int i = 0; i < loopCount; i++) {
            long start = System.currentTimeMillis();
            pinger.pingAll();
            pinger.waitPongs();
            long end = System.currentTimeMillis() - start;
            results[i] = end;
        }
        long avgLatency = calcAverage(results);
        Logger.info("loopCount= " + loopCount + ", avg latency (ms): " + avgLatency);
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

}
