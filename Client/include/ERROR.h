#ifndef ERROR_H_
#define ERROR_H_


#include <string>
#include <iostream>
using namespace std;

class ERROR{
private:
	short op;
	short errorcode;
	string ErrMsg;

public:
	//constructor for special message
	ERROR(short errorcode, string ErrMsg);
	//constructor for default message
	ERROR(short i);
	//return the number of packet
	short getOP();
	//return the number of error
	short gerErroeCode();
	//return the error string
	string getMessage();

};
#endif /* ERROR_H_ */
 
