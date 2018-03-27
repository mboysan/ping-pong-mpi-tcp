package testframework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResultCollector {
    private static ResultCollector ourinstance = new ResultCollector();

    public static final String PHASE_WARMUP = "warmup";
    public static final String PHASE_FULL_LOAD = "full-load";
    public static final String PHASE_ALL = "all";

    private final Map<String, List<IResult>> resultsMap;

    private ResultCollector() {
        this.resultsMap = new ConcurrentHashMap<>();
    }

    public static ResultCollector getInstance() {
        return ourinstance;
    }

    public void addResult(String group, IResult result){
        List<IResult> results = resultsMap.get(group);
        if(results == null){
            results = new ArrayList<>();
        }
        results.add(result);
        resultsMap.put(group, results);
    }

    public void addResultAsync(String group, IResult result){
        new Thread(() -> addResult(group, result)).start();
    }

    void printOnConsole(String phase){
        if(phase == null){
            //print all
            for (String s : resultsMap.keySet()) {
                printOnConsole(s);
            }
        } else {
            List<IResult> results = resultsMap.get(phase);
            for (IResult result : results) {
                System.out.print(result.printlnCSV(phase));
            }
        }
    }
}
