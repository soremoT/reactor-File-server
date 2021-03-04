//
// Created by idan on 12/01/17.
//

#include "ERROR.h"
#include <boost/asio.hpp>
#include <boost/thread.hpp>  
#include <stdlib.h>

ERROR::ERROR(short errorcode, string ErrMsg) :op(5),errorcode(errorcode),ErrMsg(ErrMsg){}

ERROR::ERROR(short i):op(5),errorcode(i),ErrMsg("") {
    op=5;
    errorcode=(short)i;
    switch(i){
        case 0:  ErrMsg = "Not defined, see error message (if any)";
            break;
        case 1:  ErrMsg = "File not found – RRQ or DELRQ of non-existing file";
            break;
        case 2:  ErrMsg = "Access violation – File cannot be written, read or deleted.";
            break;
        case 3:  ErrMsg = "Disk full or allocation exceeded – No room in disk.";
            break;
        case 4:  ErrMsg = "Illegal TFTP operation – Unknown Opcode.";
            break;
        case 5:  ErrMsg = "File already exists – File name exists on WRQ.";
            break;
        case 6:  ErrMsg = "User not logged in – Any opcode received before Login completes.";
            break;
        case 7:  ErrMsg = "User already logged in – Login username already connected.";
            break;

    }
}

short ERROR :: getOP(){
    return op;
}
short ERROR :: gerErroeCode(){
    return errorcode;
}
string ERROR :: getMessage(){
    return ErrMsg;

}
