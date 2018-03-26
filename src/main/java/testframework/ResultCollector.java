package testframework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultCollector {
    static final String PHASE_WARMUP = "Warmup";
    static final String PHASE_FULL_LOAD = "Full Load";

    private final Map<String, List<IResult>> resultsMap;

    ResultCollector() {
        this.resultsMap = new HashMap<>();
    }

    void addResult(String group, IResult result){
        List<IResult> results = resultsMap.get(group);
        if(results == null){
            results = new ArrayList<>();
        }
        results.add(result);
        resultsMap.put(group, results);
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
                System.out.print(result.printlnCSV());
            }
        }
    }
}
