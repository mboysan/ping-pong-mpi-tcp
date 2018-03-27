package testframework;

public class LatencyResult implements IResult {

    private final long startTime;
    private final long endTime;

    private final String name;
    private final long latency;

    public LatencyResult(String name, long startTime, long endTime) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.latency = calcLatency(startTime, endTime);
    }

    private long calcLatency(long startTime, long endTime){
        return endTime - startTime;
    }

    public String printlnCSV(String phase){
        return name + "," + latency + "," + phase + "\r\n";
    }
}
