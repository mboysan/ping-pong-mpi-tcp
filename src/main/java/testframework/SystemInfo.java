package testframework;

import testframework.result.SystemInfoResult;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class that generates current system utilization values. Results are collected with {@link TestResultCollector}
 * under testGroupName "sysResult".
 */
public class SystemInfo {

    private static SystemInfo ourinstance;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private final TestResultCollector resultCollector = new TestResultCollector();

    /**
     * Indicates if the generation ended or not.
     */
    private boolean isEnded = false;

    /**
     * Period to generate results for.
     */
    private final long time;
    /**
     * Time unit for results generated
     */
    private final TimeUnit timeUnit;

    /**
     * Starts collecting system info results with the period defined. If there are any old generator initialized,
     * ends it then initializes new generator.
     *
     * @param time     sets {@link #time}
     * @param timeUnit sets {@link #timeUnit}
     * @return the created sys info collector.
     */
    public static SystemInfo collectEvery(long time, TimeUnit timeUnit) {
        init(time, timeUnit);
        ourinstance.collectTask();
        return ourinstance;
    }

    /**
     * Starts printing system info results with the period defined.If there are any old generator initialized,
     * ends it then initializes new generator.
     *
     * @param time     sets {@link #time}
     * @param timeUnit sets {@link #timeUnit}
     * @return the created sys info collector.
     */
    public static SystemInfo printOnConsoleEvery(long time, TimeUnit timeUnit){
        init(time, timeUnit);
        ourinstance.printConsoleTask();
        return ourinstance;
    }

    private static void init(long time, TimeUnit timeUnit){
        if(ourinstance != null){
            ourinstance.end();
        }
        ourinstance = new SystemInfo(time, timeUnit);
    }

    private SystemInfo(long time, TimeUnit timeUnit){
        this.time = time;
        this.timeUnit = timeUnit;
    }

    /**
     * generates a {@link SystemInfoResult} at fixed rate and collects it with {@link TestResultCollector}.
     */
    private void collectTask() {
        Runnable task = () -> {
            resultCollector
                    .addResultAsyncUnbounded(
                            new SystemInfoResult("sysResult", TestPhase.PHASE_CUSTOM)
                    );
        };
        executor.scheduleAtFixedRate(task, 0, time, timeUnit);
    }

    /**
     * Used with {@link #printConsoleTask()}
     */
    private AtomicBoolean printHeader = new AtomicBoolean(true);
    /**
     * generates a {@link SystemInfoResult} and prints on the console with a fixed rate.
     */
    private void printConsoleTask() {
        Runnable task = () -> {
            SystemInfoResult sysInf = new SystemInfoResult("sysResult", TestPhase.PHASE_CUSTOM);
            System.out.print(sysInf.CSVLine(printHeader.getAndSet(false)));
        };
        executor.scheduleAtFixedRate(task, 0, time, timeUnit);
    }

    /**
     * Ends result generation and collection.
     */
    public synchronized void end(){
        if(!isEnded){
            executor.shutdownNow();
            resultCollector.finalizeCollection();

            isEnded = true;
        }
    }

    /**
     * Ends the result generation, then prints results on console.
     */
    public synchronized void printOnConsole(){
        end();
        resultCollector.printOnConsole("sysResult", null);
    }
}
