# CS557: Assignment 2 - Chord Distributed Hash Table

## Name: Kenneth Peter Fernandes

---

## Tools & Programming Language:
- Tools: Thrift
- Programming Language: Java
- Devlopment IDE: Visual Studio Code.

---

## Compilation and Execution process:
- Navigate to the folder "cs457-557-fall2020-pa2-kenneth-fernandes" and run the following commands:
```commandline
$> bash
$> export PATH=$PATH:/home/cs557-inst/local/bin
```
- Compile the "chord.thrift" IDL file to Java by using the following commands:
```commandline
$> cp /home/cs557-inst/pa2/chord.thrift ./
$> thrift -gen java chord.thrift
```
- Once the gen-java folder is generated, navigate to the following path "cs457-557-fall2020-pa2-kenneth-fernandes/java".

- Compile the programs by executing the following command:
```commandline
$> make
```
- After compiling the programs, we can then run the JavaServer program at multiple instances of the VM by using the following command:
```commandline
$> chmod u+x ./server.sh
$> ./server.sh 9090

[Command: ./server.sh <port_number>]
```
- Once the server program is executed at different VM instances, insert the IP Addresses to nodes.txt.

- Then, we can run the initializer program for populating nodes.txt for fingertables as follows:
```commandline
$> /home/cs557-inst/pa2/init nodes.txt
```
- Once the initializer is executed, we can then execute the client program(Implmented the writeFile and readFile functions) using the below command:
```commandline
$> chmod u+x ./client.sh
$> ./client.sh 128.226.114.202 9090

[Command: ./client.sh <ip_address> <port_number>]
```
---

## Assignment completion status:
- The implementation for FileStoreHandler and JavaServer is complete.
- The testing for the application is incomplete. It performed with the help of JavaClient.
- The application has compiled and executed at the remote.cs.binghamton.edu server (Server Code).
- The client code is implemented but partially executed the test cases.
