package testframework;

import testframework.result.SystemInfoResult;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Class that generates current system utilization values. Results are collected with {@link TestResultCollector}
 * under testGroupName "sysResult".
 */
public class SystemInfoGenerator {

    private static SystemInfoGenerator ourinstance;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    /**
     * Period to generate results for.
     */
    private final long time;
    /**
     * Time unit for results generated
     */
    private final TimeUnit timeUnit;

    /**
     * @param time     sets {@link #time}
     * @param timeUnit sets {@link #timeUnit}
     */
    public static void collectEvery(long time, TimeUnit timeUnit) {
        if(ourinstance == null){
            ourinstance =  new SystemInfoGenerator(time, timeUnit);
        } else {
            end();
            collectEvery(time, timeUnit);
        }
    }

    private SystemInfoGenerator(long time, TimeUnit timeUnit){
        this.time = time;
        this.timeUnit = timeUnit;
        exec();
    }

    /**
     * generates a {@link SystemInfoResult} at fixed rate and collects it with {@link TestResultCollector}.
     */
    private void exec() {
        Runnable task = () -> {
            TestResultCollector.getInstance()
                    .addResultAsyncUnbounded(
                            new SystemInfoResult("sysResult", TestPhase.PHASE_CUSTOM)
                    );
        };
        executor.scheduleAtFixedRate(task, 0, time, timeUnit);
    }

    public static synchronized void end(){
        if(ourinstance == null){
            return;
        }
        ourinstance.executor.shutdownNow();
        ourinstance = null;
    }

    public static void main(String[] args) throws InterruptedException {
        SystemInfoGenerator.collectEvery(1, TimeUnit.SECONDS);
        System.out.println("collecting");
        Thread.sleep(5000);
        SystemInfoGenerator.end();
        TestResultCollector.getInstance().printOnConsole("sysResult", null);
        TestResultCollector.getInstance().finalizeCollection();
    }
}
