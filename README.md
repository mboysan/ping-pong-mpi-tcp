# Ping-Pong Latency Test With Java Open-MPI Bindings and Java TCP/IP Sockets

This repository is used for measuring the Java Open-MPI bindings vs
 Java TCP socket latencies for a network of distributed 
 processes. Basically, it performs some 
ping-pong tests between a number of processes.

A basic test consists of the following flow:
1. A pinger node is created.
2. Other nodes in the system play the role of ponger.
3. Pinger sends ping request to all the pongers.
4. The time taken from the first ping request to the last pong received is recorded.
5. Steps (3) and (4) is repeated inside a loop.
6. Results from step 5 are recorded and their average is calculated.

## Software Dependencies

It is important the followings are available in your system:
* Open-MPI with Java bindings are set-up,
* Java 8 or above,
* Maven.

## Running Java Open-MPI Latency Tests

Edit the `mpi.sh` script for your needs and run it like:
```bash
$ ./mpi.sh
```

## Running Java TCP Socket Tests

The Java TCP sockets use the mpirun for running the Java
sockets in parallel. This is the main method used for testing/
debugging the application. To use this approach use the 
`socket.sh` script:
```bash
$ ./socket.sh
```
You can also use the class `SocketMainSingleJVM.java` to execute
the tests with an IDE of your choice which makes it easier
to debug the entire application.

# OSU Point-to-Point Latency Tests

The repository also includes the Java converted 
[point-to-point `osu_latency`](http://mvapich.cse.ohio-state.edu/benchmarks/)
tests both for Open-MPI and Java TCP sockets. Use the
following scripts to run these tests:
```bash
$ ./osu_latency_mpi.sh
$ ./osu_latency_socket.sh
```

# Results

To-be-added.