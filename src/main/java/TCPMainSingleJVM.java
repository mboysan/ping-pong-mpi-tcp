import config.GlobalConfig;
import mpi.MPIException;
import network.address.TCPAddress;
import org.pmw.tinylog.Logger;
import role.Node;
import testframework.SystemInfoGenerator;
import testframework.TestFramework;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Ping-Pong test for TCP
 */
public class TCPMainSingleJVM {

    public static void main(String[] args) throws IOException, InterruptedException, MPIException {
        SystemInfoGenerator.collectEvery(500, TimeUnit.MILLISECONDS);

        int totalNodes = 3;
        if(args != null && args.length > 0){
            totalNodes = Integer.parseInt(args[0]);
        }

        GlobalConfig.getInstance().initTCP(true, totalNodes);
        Logger.info("TCP INIT (Single JVM)");

        /* Start pinger and pongers */
        int port = 8080;
        for (int i = 1; i < totalNodes; i++) {  // first index will be reserved to pinger
            Node ponger = new Node(new TCPAddress("127.0.0.1", port++));
        }
        Node pinger = new Node(new TCPAddress("127.0.0.1", port++), totalNodes);

        /* start tests */
        TestFramework testFramework = TestFramework.getInstance().initPingTests(pinger, totalNodes);

        /* send end signal to all nodes */
        pinger.signalEndToAll();

        GlobalConfig.getInstance().end();

        Logger.info("TCP END (Single JVM)");

        SystemInfoGenerator.end();
        testFramework.printOnConsole("sysResult", null);
        testFramework.printAllOnConsole();
    }
}
