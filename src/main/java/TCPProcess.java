import java.io.File;
import java.io.IOException;

/**
 * Used to start each TCP process in a separate JVM. Uses the script ./tcp_multi_jvm.sh to initiate a single process.
 */
public class TCPProcess {

    public static void main(String[] args) throws IOException, InterruptedException {
        int processCount = 0;
        if(args != null && args.length > 0){
            processCount = Integer.parseInt(args[0]);
        }
        ProcessBuilder[] processBuilders = new ProcessBuilder[processCount];
        for (int i = 0; i < processCount; i++) {
            String pCount = processCount + "";
            String rank = i + "";
            ProcessBuilder pb = new ProcessBuilder("/bin/bash","-c", "./tcp_multi_jvm.sh " + pCount + " " + rank);
            pb.directory(new File(System.getProperty("user.dir")));
            pb.redirectErrorStream(true); // redirect error stream to output stream
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            processBuilders[i] = pb;
        }

        System.out.println("Starting child processes...");
        Process[] processes = new Process[processCount];
        for (int i = 0; i < processBuilders.length; i++) {
            processes[i] = processBuilders[i].start();
        }
        for (Process process : processes) {
            process.waitFor();
        }
        System.out.println("Ending all.");
    }
}
