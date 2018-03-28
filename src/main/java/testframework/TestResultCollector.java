package testframework;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used for collecting the test results.
 */
public class TestResultCollector {
    /**
     * Singleton instance
     */
    private static TestResultCollector ourinstance = new TestResultCollector();

    /**
     * Map that contains the results collected. Grouped by test group name
     */
    private final Map<String, List<IResult>> resultsMap;

    private TestResultCollector() {
        this.resultsMap = new ConcurrentHashMap<>();
    }

    public static TestResultCollector getInstance() {
        return ourinstance;
    }

    /**
     * Adds result to the {@link #resultsMap} based on its properties. It adds the result in a synchronized manner.
     * @param result result to add
     */
    public void addResult(IResult result){
        String testGroupName = result.getTestGroupName();
        if(testGroupName == null || result.getTestPhase() == null){
            throw new IllegalArgumentException("testGroupName or testPhase should not be null");
        }

        List<IResult> results = resultsMap.get(testGroupName);
        if(results == null){
            results = new ArrayList<>();
        }
        results.add(result);
        resultsMap.put(testGroupName, results);
    }

    /**
     * Adds result to the {@link #resultsMap} based on its properties. It adds the result in a async manner.
     * @param result result to add
     */
    public void addResultAsync(IResult result){
        new Thread(() -> addResult(result)).start();
    }

    /**
     * Prints the results collected to console.
     * @param testGroupName name of the test group to print. If null, prints all.
     * @param phase         result phase to print. If null, then prints all the phases.
     */
    void printOnConsole(String testGroupName, TestPhase phase){
        if(phase == null){
            //print all grouped by phase
            for (TestPhase testPhase : TestPhase.values()) {
                if(testGroupName == null){
                    for (String s : resultsMap.keySet()) {
                        printOnConsole(s, testPhase);
                    }
                } else {
                    printOnConsole(testGroupName, testPhase);
                }
            }
        } else {
            List<IResult> results = resultsMap.get(testGroupName);
            for (IResult result : results) {
                if(result.getTestPhase() == phase){
                    System.out.print(result.CSVLine());
                }
            }
        }
    }
}
