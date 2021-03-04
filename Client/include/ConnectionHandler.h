//
// Created by idan on 12/01/17.
//

#ifndef CLIENT_CONNECTIONHANDLER_H
#define CLIENT_CONNECTIONHANDLER_H

#include <string>
#include <iostream>
#include <boost/asio.hpp>
#include <boost/thread.hpp>  



using boost::asio::ip::tcp;
using namespace std;




enum State {
    NotWaiting,
    RRQ,
    WRQ,
    DIRQ,
    LOGRQ,
    DELRQ,
    DISC,
};
enum OP{
    readOP,
    legalOP
};



class ConnectionHandler {
private:
    const std::string host_;
    const short port_;
    boost::asio::io_service io_service_;   // Provides core I/O functionality
    tcp::socket socket_;
    State state;
    bool shutDown;
    OP opstate;
    short op;
    short expectedBlock;
    bool done;
    short packetSize;
    vector<char> directoryList;
    vector<char>* dataVectorRecieve;
    string nameOfFile;
    bool startedWriting;
    int sendBlockCounter;
    int dataCounter;
    int filesize;
    bool lastAck;
    vector<char>* outputFile;   //vector to save the filerecieved
    bool writerShouldShutDown;
    bool acknowledged;
    bool wait;

public:
    
    //copy constructor(empty implimentation)
   ConnectionHandler(const ConnectionHandler &handler);
   
   //copy operator (empty implementation)
    ConnectionHandler& operator=(const ConnectionHandler &handler);
    
    //state of no commands sent
    void notWaitingForPackets();
    
    //this method is called when a packet arives and we are waiting for DISC
    void discRqst();
    
    //this method is called when a packet arives and we are waiting for DELRQ
    void delrqst();
    
    //this method is called when a packet arives and we are waiting for LOGRQ
    void logrqst();
    
    //this method is called when a packet arives and we are waiting for WRQ and recieve acknowledge
    void wrqAcknowledged();
    
        //this method is called when a packet arives and we are waiting for DIRC
    void dircRqst();
    
        //this method is called when a packet arives and we are waiting for WRQ
    void writeRqst();
    
        //this method is called when a packet arives and we are waiting for RRQ
    void readRqst();
    
    //method to join two char arrays into one
    void joinArrays(char* answer ,char* a  ,char* b,int aLength,int bLength);
    
    //method that stops the run for the main thread
    bool shouldTerminate();
    
    //this is the method that listens on the keyboard
    bool runCommander(std::string& line);
    
    //sends a data packet after recieving ack
    void dataWritePacket();
    
    //constructor
    ConnectionHandler(std::string host, short port);
    
    //distructor
    virtual ~ConnectionHandler();

    // Connect to the remote machine
    bool connect();

    // Read a fixed number of bytes from the server - blocking.
    // Returns false in case the connection is closed before bytesToRead bytes can be read.
    bool getBytes(char bytes[], unsigned int bytesToRead);

    // Send a fixed number of bytes from the client - blocking.
    // Returns false in case the connection is closed before all the data is sent.
    bool sendBytes(const char bytes[], int bytesToWrite);

    // Read an ascii line from the server
    // Returns false in case connection closed before a newline can be read.
    bool getLine(std::string& line);

    // Send an ascii line from the server
    // Returns false in case connection closed before all the data is sent.
    bool sendLine(std::string& line);

    // Get Ascii data from the server until the delimiter character
    // Returns false in case connection closed before null can be read.
    bool getFrameAscii(std::string& frame, char delimiter);

    // Send a message to the remote host.
    // Returns false in case connection is closed before all the data is sent.
    bool sendFrameAscii(const std::string& frame, char delimiter);

    /**
     * new functions
     */
    
    void shortToBytes(short num, char* bytesArr);
    
    
    short bytesToShort(char* bytesArr);

    //checks the contents of the ackPacket
    bool ackPacket();
    
    //transltes the broadCastPacket
    bool broadCastPacket();

    //translates the errorPacket
    bool errorPacket();

    //stops the thread that listens to the socket and closes the socket
    void disconnect();

    //this method is run by the thread that listens on the keyboard
    void runReader();

    //recieves a data packet and sends acknowledge Packet
    void dataRecievePacket();

    //recieves directory packet and sends acknowledge
    void directoryPacket();

    //sends errors to the server if such occured
    void errorSend(short err);
    
    //read the name
    void sendAck(short blockAck);
    /**
     * end new functions
     */

    // Close down the connection properly.
    void close();

}; //class ConnectionHandler

#endif //CLIENT_CONNECTIONHANDLER_H
