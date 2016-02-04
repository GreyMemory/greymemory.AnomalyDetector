#!/usr/bin/env python

from twisted.internet.protocol import Protocol, Factory
from twisted.internet import reactor

import zmq


### Protocol Implementation

class SocketToZmq(Protocol):
    def __init__(self, zmq_socket):
        # Prepare our context and publisher
        self.context   = zmq.Context()
        self.publisher = self.context.socket(zmq.PUB)
        self.publisher.bind(zmq_socket)

    def dataReceived(self, data):
        """
        As soon as any data is received, write it into zmq socket.
        """
        print ("RX:")
        print(data)
        
        self.publisher.send(data)

    def connectionMade(self):
        print ("Connection made.")
        

class SocketToZmqFactory(Factory):
    def buildProtocol(self, addr):
        return SocketToZmq(self.zmq_socket);

    def __init__(self, zmq_socket):
        self.zmq_socket = zmq_socket

def main():
    f = SocketToZmqFactory("tcp://*:22622")
    f.protocol = SocketToZmq
    reactor.listenTCP(22623, f)
    print ("Listening...")
    reactor.run()

if __name__ == '__main__':
    main()
