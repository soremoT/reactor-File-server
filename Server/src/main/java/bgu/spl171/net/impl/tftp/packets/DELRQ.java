package bgu.spl171.net.impl.tftp.packets;

public class DELRQ implements Packet {
	private short OP;
	private String fileName;
	
	public DELRQ(short OP,String name){
		this.OP=OP;
		this.fileName=name;
	}
	
	public short getOP(){
		return OP;
	}
	
	public String getName(){
		return fileName;
	}

	

}
