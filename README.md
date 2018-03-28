# Ping-Pong Latency Test With MPI and TCP

This repo is used for measuring the TCP vs MPI latencies for 
a network of distributed processes. Basically, it performs some 
ping-pong tests between a number of processes.

A basic test consists of the following flow:
1. A pinger node is created.
2. Other nodes in the system play the role of ponger.
3. Pinger sends ping request to all the pongers.
4. The time taken from the first ping request to the last pong received is recorded.
5. Steps (3) and (4) is repeated inside a loop.
6. Results from step 5 are recorded and their average is calculated.

## Running the Tests

It is important the followings are available in your system:
* MPI set-up
* Java 8 or above
* Maven

## Running MPI Tests

Edit the `mpi.sh` script for your needs and run it like:
```bash
$ ./mpi.sh
```

## Running TCP Tests

TCP has 2 versions. One uses a single JVM for all the nodes. This
version is intended to test the overall behavior, so will not be
used for performance evaluations. The second version runs all the
nodes in separate JVMs. The performance evaluations will be done
with that.

To run the single JVM version of the TCP tests, use script:
```bash
$ ./tcp_single_jvm.sh
```
You can also use the class `TCPMainSingleJVM.java` to execute
the tests with an IDE of your choice.

To run multi JVM version of the TCP tests, use script:
```bash
$ ./tcp.sh
```