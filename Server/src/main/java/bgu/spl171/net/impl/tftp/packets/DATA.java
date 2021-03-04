package bgu.spl171.net.impl.tftp.packets;

public class DATA implements Packet {
	private byte[] data;
	private short op;
	private short block;
	
	public DATA(int block,byte[] data){
		this.data=data;
		this.block=(short)block;
		op=3;
	}
	public short getOP(){
		return op;
	}
	public byte[] getData(){
		return data;
	}
	public short getBlock(){
		return block;
	}
	
	public short getErrorCode() {
		return 0;
	}
	
}
