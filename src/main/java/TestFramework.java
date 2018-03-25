import org.pmw.tinylog.Logger;
import role.Node;

public class TestFramework {

    public static void loopPing(Node pinger, int loopCount){
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

    public static long calcAverage(long[] results){
        long avg = 0;
        for (long result : results) {
            avg += result;
        }
        return avg /= results.length;
    }

}
