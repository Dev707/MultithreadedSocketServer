# MultithreadedSocketServer
Online Quiz Application

## Contents


- Introduction
- How Does TCP/IP Work?
- The goal


## Introduction

The Transmission Control Protocol (TCP) is a communications standard that enables application programs and computing devices to exchange messages over a network. It is designed to send packets across the internet and ensure the successful delivery of data and messages over networks.
TCP is one of the basic standards that define the rules of the internet and is included within the standards defined by the Internet Engineering Task Force (IETF). It is one of the most commonly used protocols within digital network communications and ensures end-to-end data delivery.
TCP organizes data so that it can be transmitted between a server and a client. It guarantees the integrity of the data being communicated over a network. Before it transmits data, TCP establishes a connection between a source and its destination, which it ensures remains live until communication begins. It then breaks large amounts of data into smaller packets, while ensuring data integrity is in place throughout the process.


## How Does TCP/IP Work?

The TCP/IP model was developed by the United States Department of Defense to enable the accurate and correct transmission of data between devices. It breaks messages into packets to avoid having to resend the entire message in case it encounters a problem during transmission. Packets are reassembled once they reach their destination. Every packet can take a different route between the source and the destination computer, depending on whether the original route used becomes congested or unavailable.


## The goal

In this project, we will implement a simple online quiz application using TCP/IP protocol, to understand the behavior of the TCP/IP protocol. We will develop two processes. One client process and other server process.


Project Features:

```
•	A User need to register itself as a teacher or student on the server.
•	A User can login on the server
```
