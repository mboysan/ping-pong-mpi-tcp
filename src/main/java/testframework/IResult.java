package testframework;

public interface IResult {
    /**
     * @return The recorded phase of the test result
     */
    TestPhase getTestPhase();
    /**
     * @return The test's group name that is recorded.
     */
    String getTestGroupName();

    /**
     * @return get the result as a CSV result preferably ended with <tt>System.getProperty("line.separator")</tt>
     */
    String CSVLine();
}
