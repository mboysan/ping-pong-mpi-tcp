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

Detailed description of the work and the testing procedure
can be read in the following paper: [paper.pdf](url). I
encourage you to read the paper first before diving in
the following sections.

The final data collected can be found in 
`./slurm/data/all_combined.ods` file. Using an office
program of your choice to open it.

## Data Collection

A script located in `./slurm/scripts/work.sh` is used
to run the tests on the University of Tartu's 
HPC Rocket cluster.

The test results produced in csv files are in the 
following format.

**Ping-Pong Tests**

The format and a sample of the csv files for the 
*Ping-Pong Roundtrip latency test* is as follows:

| Test Name           | Test Phase | Num Iterations | Leader Node Name | Timestamp        | Time         | Latency (ms) |
|:-------------------:|:----------:|:--------------:|:----------------:|:----------------:|:------------:|:------------:|
| pingAllIntermediate | full-load  | 500            | p0g31            | 1526646657332    | 15:30:57.332 | 5            |

The format and a sample of the csv files for the 
*Ping-Pong Point-to-Point latency test* is as follows:

| Test Name           | Test Phase | Num Iterations | Leader Node Name | Timestamp        | Time         | Latency (ms) |
|:-------------------:|:----------:|:--------------:|:----------------:|:----------------:|:------------:|:------------:|
| pingSingle          | N/A        | -1             | p0g31            | 1526646655664    | 15:30:55.664 | 3            |

**OSU Latency Tests**

A single OSU Latency test result csv file is of the
following form (1 line provided for a sample):

| Size(bytes) | latency (us) |
|:-----------:|:------------:|
| 2           | 240          |

## Data Processing

**Ping-Pong Tests**

The data is processed using `R`. Scripts located in 
`./slurm/data/ping-pong-tests/process/` folder are
used to process the data. Basically, the 
`./slurm/data/ping-pong-tests/process/run.sh` script
was submitted to the job scheduler of the cluster.
Following is the procedure:

1. After collecting the data, all the relevant latency 
results are grouped together based on the number of nodes.
2. Outliers are removed.
3. The mean of the latency values are calculated and
recorded.

**OSU Latency Tests**

No special arrangement is done for the OSU latency
tests since these are standard. The collected data
was just printed in their corresponding csv file.