package bgu.spl171.net.impl.tftp.packets;

public class ERROR implements Packet{
	short op;
	short errorcode;
	String ErrMsg;
	
	public ERROR(short errorcode,String ErrMsg){
		this.ErrMsg=ErrMsg;
		this.errorcode=errorcode;
	}
	
	
	public ERROR(int i){
		this.op=5;
		this.errorcode=(short) i;
	        switch (i) {
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
	
	public short getErrorCode(){
		return errorcode;
	}
	public String getMessage(){
		return ErrMsg;
	}

	@Override
	public short getOP() {
		
		return op;
	}

	
		
	
	
	

}
