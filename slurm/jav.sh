#!/usr/bin/env bash

javac -d ./target/mpi/ -cp ./target/classes/:./target/dependency/* -sourcepath $(find ./src -name '*.java')
