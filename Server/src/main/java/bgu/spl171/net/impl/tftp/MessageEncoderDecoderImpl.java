package bgu.spl171.net.impl.tftp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.impl.tftp.packets.ACK;
import bgu.spl171.net.impl.tftp.packets.BCAST;
import bgu.spl171.net.impl.tftp.packets.DATA;
import bgu.spl171.net.impl.tftp.packets.DELRQ;
import bgu.spl171.net.impl.tftp.packets.DIRQ;
import bgu.spl171.net.impl.tftp.packets.DISC;
import bgu.spl171.net.impl.tftp.packets.ERROR;
import bgu.spl171.net.impl.tftp.packets.LOGRQ;
import bgu.spl171.net.impl.tftp.packets.Packet;
import bgu.spl171.net.impl.tftp.packets.RRQ;
import bgu.spl171.net.impl.tftp.packets.WRQ;
/**
 * 
 * @author romavic 
 *
 * @param <T> Packet type objects
 */
public class MessageEncoderDecoderImpl<T> implements MessageEncoderDecoder<Packet> {
	//op variables
	private byte [] opCode=new byte[2];
	private int opLength=0;
	private short op;
	//string variables
	private byte[] name = new byte[500];
	private int nameLength=0;
	//data decode variables
	private short expectedDataPacketSize;
	private short blocknum;
	private byte [] packet;
	private ByteArrayOutputStream file = new ByteArrayOutputStream();
	private byte[] DataSize=new byte[2];
	private byte[] block=new byte[2];
	private int sizelength=0;
	private int blocklength=0;
	private int counter=0;
	private boolean done=true;
	private DataState datastate=DataState.packetsize;
	private State state=State.ReadOP;
	//error variable
	int errorState=0;
	short errorCode;
	//private short errorCode;
	//data encode variables
	private byte[] sendData;
	private int sendDataCounter=0;
	private int blockSize;
	private int lastPacketToSend;
	private int blockCounter=0;
	private boolean writing=false;

	
	
	
	public enum State{
		ReadOP,
		LegalOP,
		
	}
	public enum DataState{
		packetsize,
		block,
		data
	}
	
	
	@Override
	public Packet decodeNextByte(byte nextByte) {
		switch (state) {
			case ReadOP:
				//increase the op byte array
				opCode[opLength]=nextByte;
				opLength++;	
				//check if op is legal
				if(opLength==2){
					op=bytesToShort(opCode);
					//if op illegal return error and initialize opLength
					if(op>10||op<1||op==9){
						opLength=0;
						return  new ERROR((short)4);
					}
					else{
						state=State.LegalOP;  
						opLength=0;	
						if(op==10){
							state=State.ReadOP;
							return new DISC();
							}
					}
					if(op==6){
						state=State.ReadOP;
						return new DIRQ();
						}
					
				}
				
				break;
			case LegalOP:
				switch(op){
				case(1):return readRqst(nextByte);
				case(2):return writeRqst(nextByte);
				case(3):return dataRead(nextByte);
				case(4):return ack(nextByte);
				case(5): return error(nextByte);//need implementation
				case(7):return logRQ(nextByte);
				case(8):return delete(nextByte);
				}
		break;
	}return null;
	}
	
	/**
	 * 
	 * @param nextByte  the last byte recieved
	 * @return  DELRQ packet command
	 */
	private Packet delete(byte nextByte) {
		if (nextByte == '\0') {
			state=State.ReadOP;
			return new DELRQ((short)9,popString());
		}
		 pushByte(nextByte);
	        return null;
	}

	/**
	 * 
	 * @param nextByte
	 * @return returns a command of LOGRQ
	 */
	private Packet logRQ(byte nextByte) {
		if (nextByte == '\0') {
			state=State.ReadOP;
			return new LOGRQ((short)8,popString());
		}
		 pushByte(nextByte);
	        return null;	
	}
	

	/**
	 * method to handle acknowledgement from client
	 * @param nextByte 
	 * @return  ACK command that the send can continue
	 */
	private Packet ack(byte nextByte) {
		
		if(blocklength!=1){
			block[blocklength]=nextByte;
			blocklength++;	
		}
		else{
		block[blocklength]=nextByte;
		blocklength=0;
		blocknum=bytesToShort(block);
		state=State.ReadOP;
		if(blocknum!=0&&!writing)
			return null;
		return new ACK(blocknum,true);
		}
		return null;
	}

	/**
	 * method to handle errors if one is sent and  we are in the middle of data sending  it will stop
	 * @param nextByte
	 */
	private Packet error(byte nextByte) {
			if(errorState==0){
				if(blocklength!=1){
				block[blocklength]=nextByte;
				blocklength++;	
				}
				else{
					block[blocklength]=nextByte;
					blocklength=0;
					errorCode=bytesToShort(block);
					errorState++;
					return null;
					
				}}
			if(errorState==1){
				if (nextByte == '\0') {
					errorState=0;
					popString();
					state=State.ReadOP;
					if(!done){
						sizelength=0;
						blocklength=0;
						counter=0;
						datastate=DataState.packetsize;
						file.reset();
					}
					if(writing==true){
						sendData=null;
						sendDataCounter=0;
						blockCounter=0;
						writing=false;
					}
				}else pushByte(nextByte);
	}return null;
	}

	/**
	 * method to read the read request
	 * @param nextByte
	 * @return RRQ command
	 */
	private Packet readRqst(byte nextByte){
		if (nextByte == '\0') {
			state=State.ReadOP;
			return new RRQ((short)1,popString());
		}
		 pushByte(nextByte);
	        return null;	
	}
	
	/**
	 * pushes the byte to the string
	 * @param nextByte
	 */
	 private void pushByte(byte nextByte) {
	        name[nameLength]= nextByte;
	        nameLength++;
	    }
	 /**
	  * 
	  * @return the name of file
	  */
	 private String popString() {
	        String result = new String(name, 0, nameLength, StandardCharsets.UTF_8);
	        nameLength = 0;
	        return result;
	    }
	 /**
	  * function to read the write request
	  * @param nextByte
	  * @return WRQ command
	  */
	 private Packet writeRqst(byte nextByte){
		 if (nextByte == '\0') {
			 state=State.ReadOP;
			 return new WRQ((short)1,popString());
		 }
		 pushByte(nextByte);
		 return null;	
	}
	 /**
	  * method that reads the data file
	  * @param nextByte
	  * @return DATA packet with a byte array containing the file
	  */
	 private Packet dataRead(byte nextByte){
		switch(datastate){
			case packetsize:
					if(sizelength!=1){
						DataSize[sizelength]=nextByte;
						sizelength++;	
					}
					else{
					DataSize[sizelength]=nextByte;
					sizelength=0;
					expectedDataPacketSize=bytesToShort(DataSize);
					datastate=DataState.block;
					packet=new byte[expectedDataPacketSize];
					if(expectedDataPacketSize==512)
						done=false;
					if(expectedDataPacketSize<512)
						done=true;
					}
					break;
			case block:
					if(blocklength!=1){
						block[blocklength]=nextByte;
						blocklength++;	
					}
					else{
					block[blocklength]=nextByte;
					blocklength=0;
					blocknum=bytesToShort(block);
					datastate=DataState.data;
					}
					break;
			case data:	
					packet[counter]=nextByte;
					counter++;
					if(counter==expectedDataPacketSize){
						try {
							file.write(packet);
						} catch (IOException e) {
							e.printStackTrace();
						}
						sizelength=0;
						blocklength=0;
						counter=0;
						datastate=DataState.packetsize;
						state=State.ReadOP;
						if(!done){
							return new ACK(blocknum,false);
						}
						else{
							Packet data=new DATA(blocknum,file.toByteArray());
							file.reset();
							return data;
						}	
					}	
					break;	
		}	
		return null;	 
	}	 

	@Override
	public byte[] encode(Packet message) {
			
		//acknowledgement message
		if(message instanceof ACK){
			if(!((ACK)message).ACKisFromClient()){
			return mergeByteArrays(shortToBytes(((ACK)message).getOP()),shortToBytes(((ACK)message).getBlock()));
			}
			else {
				if(writing)
				return encodeAfterAck((ACK)message);
				}
		}
		//error message	
		if(message instanceof ERROR)
			return encodeError((ERROR)message);
		//broadcast message
		if(message instanceof BCAST){
			return mergeByteArrays(shortToBytes((short)9),(((BCAST)message).getStatus()).getBytes(),(((BCAST)message).getName()+'\0').getBytes());
		}
		if(message instanceof DATA){
			return encodeDataFirst((DATA)message);	
		}
		return null;
	}
	//only called once the data arrives and then continues when acknowledgement with blocks arrive
	private byte[] encodeDataFirst(DATA message){
		sendData=message.getData();//byte array of data
		blockSize=sendData.length/512;
		lastPacketToSend=sendData.length%512;
		if(blockSize==0){
			byte[] currPacket=new byte[lastPacketToSend];
			for(int i=0;i<lastPacketToSend;i++,sendDataCounter++){
				currPacket[i]=sendData[sendDataCounter];
			}
			sendData=null;
			sendDataCounter=0;
			writing=false;;
			return mergeByteArrays(shortToBytes(message.getOP()),shortToBytes((short) lastPacketToSend),shortToBytes((short) 1),currPacket);
		}
		else{
			byte[] currPacket=new byte[512];
			for(int i=0;i<512;i++,sendDataCounter++){
				currPacket[i]=sendData[sendDataCounter];
			}
			writing=true;
			blockCounter++;
			return mergeByteArrays(shortToBytes(message.getOP()),shortToBytes((short) 512),shortToBytes((short) 1),currPacket);
		}
	}
	/**
	 * 
	 * @param message receives the acknowledge and continues to send the next packet
	 * @return the byte data of the packet we wish to send
	 */
	private byte[] encodeAfterAck(ACK message){
			if(blockCounter==blockSize){
				byte[] currPacket=new byte[lastPacketToSend];
				for(int i=0;i<lastPacketToSend;i++,sendDataCounter++){
					currPacket[i]=sendData[sendDataCounter];
				}
				sendData=null;
				sendDataCounter=0;
				blockCounter=0;
				writing=false;
				//need the return block to be one  bigger than the previous block
				message.incBlock();
				return mergeByteArrays(shortToBytes((short)3),shortToBytes((short) lastPacketToSend),shortToBytes(message.getBlock()),currPacket);
			}else{
				byte[] currPacket=new byte[512];
				for(int i=0;i<512;i++,sendDataCounter++){
					currPacket[i]=sendData[sendDataCounter];
				}
				writing=true;
				blockCounter++;
				message.incBlock();
				return mergeByteArrays(shortToBytes((short)3),shortToBytes((short) 512),shortToBytes(message.getBlock()),currPacket);
			}
	}
	/**
	 * 
	 * @param message
	 * @return encodes and sends an error to the client
	 */
	private byte[]encodeError(ERROR message){
		byte [] op=shortToBytes((short) 5);
		byte[] error=shortToBytes(message.getErrorCode());
		byte[] string=(message.getMessage()+'\0').getBytes();
		return mergeByteArrays(op,error,string);
	}
	
	/**
	 * merge a collection of byte arrays to one
	 * @param arrays recieves a collection of arrays and merges them
	 * @return return the merged array
	 */
	private byte[] mergeByteArrays(byte[] ... arrays){
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		try {
			for(byte[] arr:arrays){
			outputStream.write( arr );
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return outputStream.toByteArray();
	}
	/**
	 * Given function to convert bytes to short
	 * @param byteArr array with bytes
	 * @return the converted value of bytes to  short
	 */
	private short bytesToShort(byte[] byteArr){
	    short result = (short)((byteArr[0] & 0xff) << 8);
	    result += (short)(byteArr[1] & 0xff);
	    return result;
	}
	/**
	 * Given function to convert short to bytes
	 * @param num the number to covert to bytes
	 * @return the bytes array
	 */
	private byte[] shortToBytes(short num){
	    byte[] bytesArr = new byte[2];
	    bytesArr[0] = (byte)((num >> 8) & 0xFF);
	    bytesArr[1] = (byte)(num & 0xFF);
	    return bytesArr;
	}

}
