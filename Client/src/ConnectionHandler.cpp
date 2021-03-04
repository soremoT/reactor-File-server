#include "ConnectionHandler.h"
#include"ERROR.h"
using namespace std;
using boost::asio::ip::tcp;

#include <iostream>
#include <fstream>
#include<sstream>
using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;




ConnectionHandler::ConnectionHandler(string host, short port): host_(host), port_(port), io_service_(),
		socket_(io_service_),state(),shutDown(false),opstate(readOP),op(),expectedBlock(0),
		done(true),packetSize(),directoryList(),dataVectorRecieve(new vector<char>),
		nameOfFile(),startedWriting(false),sendBlockCounter(0),dataCounter(0),filesize(),
		lastAck(false),outputFile(new vector<char>),writerShouldShutDown(false),acknowledged(false),wait(false){}

ConnectionHandler::~ConnectionHandler(){
    delete outputFile;
    delete dataVectorRecieve;
    close();
}


ConnectionHandler::ConnectionHandler(const ConnectionHandler &handler):host_(), port_(), io_service_(),
		socket_(io_service_),state(),shutDown(),opstate(),op(),expectedBlock(),
		done(),packetSize(),directoryList(),dataVectorRecieve(nullptr),
		nameOfFile(),startedWriting(false),sendBlockCounter(0),dataCounter(0),filesize(),
		lastAck(false),outputFile(new vector<char>),writerShouldShutDown(false),acknowledged(false),wait(false){}

    ConnectionHandler& ConnectionHandler:: operator=(const ConnectionHandler &handler){
        return *this;
    }

//connection
bool ConnectionHandler::connect() {
    std::cout << "Starting connect to "
              << host_ << ":" << port_ << std::endl;
    try {
        tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
        boost::system::error_code error;
        socket_.connect(endpoint, error);
        if (error)
            throw boost::system::system_error(error);
    }
    catch (std::exception& e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}


void ConnectionHandler::disconnect(){
    shutDown=true;
    close();
}
bool ConnectionHandler::shouldTerminate(){
	return writerShouldShutDown;
}


//get from the server
bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp ) {
            tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);
        }
        if(error)
            throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}



void ConnectionHandler:: joinArrays(char* answer,char* a , char* b,int aLength,int bLength){
    int n=0;
    for(int i=0;i<aLength;i++,n++){
        answer[n]=a[i];
    }
    for(int i=0;i<bLength;i++,n++){
        answer[n]=b[i];
    }
}



//send to the sever
bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp ) {
            tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
        if(error)
            throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}






bool ConnectionHandler::getLine(std::string& line) {


    return getFrameAscii(line, '\0');
}


//getting the command from the keyboard and send it to the server
bool ConnectionHandler::sendLine(std::string& line) {
    return sendFrameAscii(line, '\0');
}

bool ConnectionHandler::runCommander(std::string& line){
    char opB[2];
    string buff;
    stringstream ss(line);
    ss>>buff;
    if(buff!="RRQ"&&buff!="WRQ"&&buff!="DIRQ"&&buff!="LOGRQ"&&buff!="DELRQ"&&buff!="DISC")
    	cout<<"Illegal input"<<endl;

    if(buff=="RRQ") {
    	ss>>buff;
    	nameOfFile=buff;
        state=RRQ;
        expectedBlock=1;
        shortToBytes(1,opB);
        char sendArr[3+nameOfFile.length()];
        char str[nameOfFile.size()];
        
        for(int unsigned i=0;i<nameOfFile.size();i++){
            str[i]=nameOfFile[i];
        }
           sendArr[2+nameOfFile.length()]='\0';
        joinArrays(sendArr,opB,str,2,nameOfFile.size());
        sendBytes(sendArr,3+nameOfFile.length());
        nameOfFile = nameOfFile.substr(0, nameOfFile.size());
    }

    if(buff=="WRQ") {
    	ss>>buff;
    	nameOfFile=buff;
        ifstream file(nameOfFile, ios::in|ios::binary|ios::ate);
               if(file.good()==0){
                   cout<<"File Does Not Exist"<<endl;
                   state=NotWaiting;
                   return false;
            }
        //TODO:
        state=WRQ;
        expectedBlock=0;
        shortToBytes(2,opB);
        char sendArr[3+nameOfFile.length()];
        char str[nameOfFile.size()];
        for(int unsigned i=0;i<nameOfFile.size();i++){
            str[i]=nameOfFile[i];
        }
           sendArr[2+nameOfFile.length()]='\0';
        joinArrays(sendArr,opB,str,2,nameOfFile.size());
        sendBytes(sendArr,3+nameOfFile.length());
        nameOfFile = nameOfFile.substr(0, nameOfFile.size());
        
    }

    if(buff=="DIRQ") {
        state=DIRQ;
        expectedBlock=1;
        shortToBytes(6,opB);
        sendBytes(opB,2);
    }

    if(buff=="LOGRQ") {
        state=LOGRQ;
    	ss>>buff;
        nameOfFile=buff;
        if(nameOfFile!="LOGRQ"){
            expectedBlock=0;
            shortToBytes(7,opB);
            char sendArr[2+nameOfFile.length()+1];
            char str[nameOfFile.size()];
        
            for(int unsigned i=0;i<nameOfFile.size();i++){
                str[i]=nameOfFile[i];
            }
            sendArr[2+nameOfFile.length()]='\0';
            joinArrays(sendArr,opB,str,2,nameOfFile.size());
            sendBytes(sendArr,3+nameOfFile.length());
        }else
            cout<<"Illegal input"<<endl;
        
    }
    if(buff=="DELRQ") {
        
        state=DELRQ;
    	ss>>buff;
        nameOfFile=buff;//get name
        expectedBlock=0;
        shortToBytes(8,opB);//convet the op to bytes
        char sendArr[3+nameOfFile.length()];//create char arry as thr size if the total bytes
        char str[nameOfFile.size()];//passibng the name od the filr to char array 
        for(int unsigned i=0;i<nameOfFile.size();i++){
            str[i]=nameOfFile[i];
        }
           sendArr[2+nameOfFile.length()]='\0';
        joinArrays(sendArr,opB,str,2,nameOfFile.size());
        sendBytes(sendArr,3+nameOfFile.length());
    }

    if(buff=="DISC") {
        state=DISC;
        expectedBlock=0;
        shortToBytes(10,opB);
        sendBytes(opB,2);
        wait=false;
        while(!wait){}
        if(shutDown)
        writerShouldShutDown=true;
    }
    return true;;
}





//get from the server
bool ConnectionHandler::getFrameAscii(std::string& frame, char delimiter) {
    char ch;
    // Stop when we encounter the null character.
    // Notice that the null character is not appended to the frame string.
   
    try {
        do{
            getBytes(&ch, 1);
            if (delimiter != ch) {
                frame.append(1, ch);
            }
        }while (delimiter != ch);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}





//send to the sever
bool ConnectionHandler::sendFrameAscii(const std::string& frame, char delimiter) {
        char str[frame.size()];
        for(int unsigned i=0;i<frame.size();i++){
            str[i]=frame[i];
        }
	bool result=sendBytes(str,frame.size());
	if(!result) return false;
	return sendBytes(&delimiter,1);
}

void ConnectionHandler::readRqst(){
        if(op==3){
            dataRecievePacket();
        }
        if(op==4){
            errorSend(0);
        }
        if(op==5){
            errorPacket();
            dataVectorRecieve->clear();//if server sends error stop recieving packets
            state=NotWaiting;
            expectedBlock=0;
        }
        if(op==9){
            broadCastPacket();
        }
}

void ConnectionHandler::writeRqst(){
        if(op==3){
            errorSend(0);
        }
       if(op==4){
            acknowledged=true;
            if(ackPacket())
            wrqAcknowledged();
       }
        if(op==5){
                errorPacket();
                outputFile->clear();
                dataCounter=0;
                startedWriting=false;
                state=NotWaiting;
        }
        if(op==9){
            broadCastPacket();
        }     
}

void ConnectionHandler::dircRqst(){
        if(op==3){
            directoryPacket();
        }
        if(op==4){
            errorSend(0);
        }
       if(op==5){
            errorPacket();
            directoryList.clear();//if server sends error stop recieving packets
            state=NotWaiting;
            expectedBlock=0;
       }
        if(op==9){
            broadCastPacket();
        }
}


void ConnectionHandler::logrqst(){
        if(op==3){
            errorSend(0);
        }
        if(op==4){
            ackPacket();
            state=NotWaiting;
        }
        if(op==5){
           errorPacket();
        }
        if(op==9){
            broadCastPacket();
        }
}

void ConnectionHandler::delrqst(){
        if(op==3){
            errorSend(0);
        }
        if(op==4){
            ackPacket();
            state=NotWaiting;
        }
        if(op==5){
           errorPacket();
        }
        if(op==9){
            broadCastPacket();
        }
}

void ConnectionHandler::discRqst(){
        if(op==3){
            errorSend(0);
        }
        if(op==4){
            ackPacket();
            disconnect();
            wait=true;
            state=NotWaiting;
        }
        if(op==5){
           errorPacket();
           wait=true;
        }
        if(op==9){
            broadCastPacket();
        }
}

void ConnectionHandler::notWaitingForPackets(){
        if(op==3){
            errorSend(0);
        }
       if(op==4){
             errorSend(0);//TODO: maybe add ackPacket();
       }
       if(op==5){
           errorPacket();
       }
       if(op==9){
            broadCastPacket();
       }
}



/**
 *
 * new functions
 */
    //this method runs the thread that reads from the socket
void ConnectionHandler::runReader(){
    while(!shutDown){
        switch(opstate){
            case(readOP):
                char opBytes[2];
                getBytes(opBytes,2);///try
                op=bytesToShort(opBytes);

                if(op>10||op<3||op==6||op==7||op==8){
                    errorSend(4);
                }
                else opstate=legalOP;
                break;
            case(legalOP):
                switch(state){
                    case(NotWaiting):
                        notWaitingForPackets();
                        opstate=readOP;
                        break;
                    case(RRQ):
                        readRqst();
                        opstate=readOP;
                        break;
                        
                        
                    case(WRQ):
                        writeRqst();
                    	opstate=readOP;
                        break;

                    case(DIRQ):
                        dircRqst();
                        opstate=readOP;
                        break;
                    case(LOGRQ):
                        logrqst();
                        opstate=readOP;
                        break;

                    case(DELRQ):
                        delrqst();
                        opstate=readOP;
                        break;

                    case(DISC):
                        discRqst();
                        opstate=readOP;
                        break;
                }
        }
    }
}

void ConnectionHandler::dataWritePacket(){
	if(!startedWriting){
	streampos size;
	ifstream file(nameOfFile, ios::in|ios::binary|ios::ate);
                   
		if (file.is_open())
		{ 
                    acknowledged=true;
                    char* memblock;
			size = file.tellg();
			memblock = new char [size];
			file.seekg (0, ios::beg);
			file.read (memblock, size);
			file.close();
                        for(int i=0;i<size;i++){
                            outputFile->push_back(memblock[i]);
                        }
                       delete[] memblock;
                        
                   filesize=size;     
		}
		 sendBlockCounter=filesize/512;
		 startedWriting=true;
	}
	
            if(acknowledged){  
		if(sendBlockCounter==0){
			int n=filesize%512;
			char a[2];
			shortToBytes(3,a);//op
                        char b[2];
                        char q[4];
                        shortToBytes(n,b);//size
                        joinArrays(q,a,b,2,2);
                        char w[6];
			shortToBytes(expectedBlock,a);
                        joinArrays(w,q,a,4,2);
			char packet[n];
			for(int i=0;i<n;i++,dataCounter++){
				packet[i]=outputFile->at(dataCounter);
			}
			char packet2[n+6];
                        joinArrays(packet2,w,packet,6,n);
                                   
			sendBytes(packet2,n+6);
			//fields to resize
			outputFile->clear();
			dataCounter=0;
			startedWriting=false;
			lastAck=true;
		}else{
                        sendBlockCounter--;
                        int n=512;
                        char a[2];
			shortToBytes(3,a);//op
                        char b[2];
                        char q[4];
                        shortToBytes(n,b);//size
                        joinArrays(q,a,b,2,2);
                        char w[6];
			shortToBytes(expectedBlock,a);
                        joinArrays(w,q,a,4,2);
			char packet[n];
			for(int i=0;i<n;i++,dataCounter++){
				packet[i]=outputFile->at(dataCounter);
			}
			char packet2[n+6];
                        joinArrays(packet2,w,packet,6,n);
                                   
			sendBytes(packet2,n+6);
		}}
}





void ConnectionHandler::dataRecievePacket(){
	 if(op==3){
	        char ps[2];
	        getBytes(ps,2);
	        packetSize=bytesToShort(ps);
	        char bl[2];
	        getBytes(bl,2);
	        short currBlock=bytesToShort(bl);
	        //if the block received is the wrong block TODO:add error description
	        if(currBlock!=expectedBlock){
	            errorSend(0);
	            dataVectorRecieve->clear();
	            state=NotWaiting;
	            return;
	        }else expectedBlock++;
	        if(packetSize==512)
	            done=false;
	        else done=true;
	        //make a char array for the current packet
	        char packet[packetSize];
	        getBytes(packet,packetSize);
	        	//add the current array to the directory list
	        for(int i=0;i<packetSize;i++){
	        	dataVectorRecieve->push_back( packet[i]);
	        }
	        sendAck(currBlock);
	        if(done){
                    char*  memblock=new char [dataVectorRecieve->size()];
                    for(int unsigned i=0;i<dataVectorRecieve->size();i++){
                        memblock[i]=dataVectorRecieve->at(i);
                    }
                    //write the received file to memory
                ofstream myFile (nameOfFile, ios::out | ios::binary);
                myFile.write (memblock,dataVectorRecieve->size());
                myFile.close();
                dataVectorRecieve->clear();
                cout<<"RRQ "<<nameOfFile<<" "<<"complete"<<endl;
                    delete[] memblock; 
	            dataVectorRecieve->clear();
                     state=NotWaiting;
	        }
	    }
	}

void ConnectionHandler::directoryPacket(){
        char ps[2];
        getBytes(ps,2);
        packetSize=bytesToShort(ps);
        char bl[2];
        getBytes(bl,2);
        short currBlock=bytesToShort(bl);
        if(currBlock!=expectedBlock){
            errorSend(0);
            directoryList.clear();
            state=NotWaiting;
            return;
        }else expectedBlock++;
        if(packetSize==512)
            done=false;
        else done=true;
        //make a char array for the current packet
        char packet[packetSize];
        getBytes(packet,packetSize);
        	//add the current array to the directory list
        for(int i=0;i<packetSize;i++){
        	directoryList.push_back( packet[i]);
        }
        sendAck(currBlock);
        if(done){
            for(unsigned int i=0;i<directoryList.size();i++){
                if(directoryList.at(i)=='\0'){
                    cout<<endl;
                }else cout<<directoryList.at(i);
            }
            directoryList.clear();
            state=NotWaiting;
        }
}



void ConnectionHandler::sendAck(short blockAck){
    char a[2];
    char b[2];
    shortToBytes(4,a);
    shortToBytes(blockAck,b);
    char send[4];
    joinArrays(send,a,b,2,2);
    sendBytes(send,4);
}

bool ConnectionHandler::ackPacket(){
        char Bytes[2];
        getBytes(Bytes,2);
        short recievedBlock=bytesToShort(Bytes);
        if(expectedBlock==recievedBlock){
        	expectedBlock++;
            cout<<"ACK "<<recievedBlock<<endl;
            return true;
        }
        else {
            cout<<"ACK "<<recievedBlock<<endl;
            errorSend(0);
            return true;
        }
    }
    
void ConnectionHandler::wrqAcknowledged(){
    if(lastAck){
                acknowledged=false;
                expectedBlock=0;
                lastAck=false;
                cout<<"WRQ "<<nameOfFile<<" "<<"complete"<<endl;
                state=NotWaiting;
            }else dataWritePacket();
}
   


void ConnectionHandler::errorSend(short err){
    ERROR *er=new ERROR(err);
    char a[2];
    char b[2];
    shortToBytes(er->getOP(),a);
    shortToBytes(err,b);
    char c[4];
    joinArrays(c,a,b,2,2);
    string str=er->getMessage();
    char msg[str.length()+1];
    for(int unsigned i=0;i<str.size();i++){
            msg[i]=str[i];
        }
        msg[str.length()]='\0';
    char send[5+str.length()];
    joinArrays(send,c,msg,4,str.length()+1);
    sendBytes(send,str.length()+5);
    delete er;
}




bool ConnectionHandler::broadCastPacket(){
        char status;
        getBytes(&status,1);
        string fileName;
        getLine(fileName);
        if(status=='0'){
            cout<<"BCAST "<<"del "<<fileName<<endl;
        }else cout<<"BCAST "<<"add "<<fileName<<endl;
        opstate=readOP;
        return true;
}

bool ConnectionHandler::errorPacket(){
        char Bytes[2];
        getBytes(Bytes,2);
        short error =bytesToShort(Bytes);
        string errmsg;
        getLine(errmsg);
        cout<<"Error "<<error<<endl;
        state=NotWaiting;
        return true;
}
/**
 *end new
 */




short ConnectionHandler:: bytesToShort(char* bytesArr)
{
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}

void  ConnectionHandler:: shortToBytes(short num, char* bytesArr)
{
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}


// Close down the connection properly.
void ConnectionHandler::close() {
    try{
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}
