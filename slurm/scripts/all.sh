#!/bin/bash

# used to run the entire tests in a single batch.

# arg1: id of the active tests

./mpi.sh $1
./socket.sh $1
./mpi_on_tcp.sh $1
