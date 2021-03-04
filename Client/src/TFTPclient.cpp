//
// Created by idan on 13/01/17.
//
    #include <stdlib.h>
    #include <boost/thread.hpp>  
    #include <boost/asio.hpp>
    #include "ConnectionHandler.h"
    using boost::asio::ip::tcp;

/**
    * This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
    */
    int main (int argc, char *argv[]) {
        if (argc < 3) {
            std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
            return -1;
        }
        std::string host = argv[1];
        short port = atoi(argv[2]);

        ConnectionHandler connectionHandler(host, port);
        if (!connectionHandler.connect()) {
            std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
            return 1;
        }

        boost::thread th1(&ConnectionHandler::runReader,&connectionHandler);

        //From here we will see the rest of the ehco client implementation:
        while (!connectionHandler.shouldTerminate()) {
            const short bufsize = 1024;
            char buf[bufsize];
            std::cin.getline(buf, bufsize);//get input
            std::string line(buf);//pass the input to string
            connectionHandler.runCommander(line);
        }
        th1.join();
        return 0;
    }
