

all: TFTPclient
	g++ -o bin/TFTPclient bin/ConnectionHandler.o bin/TFTPclient.o bin/ERROR.o -lboost_system -lboost_locale -lboost_thread
	
TFTPclient: bin/ConnectionHandler.o bin/TFTPclient.o bin/ERROR.o
	
	
	
bin/ConnectionHandler.o: src/ConnectionHandler.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/ConnectionHandler.o src/ConnectionHandler.cpp

bin/TFTPclient.o: src/TFTPclient.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/TFTPclient.o src/TFTPclient.cpp
	
bin/ERROR.o:src/ERROR.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/ERROR.o src/ERROR.cpp
	
clean:
	rm -f bin/*
