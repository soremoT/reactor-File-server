package bgu.spl171.net.impl.tftp.packets;

public class RRQ implements Packet {
	private short OP;
	private String fileName;
	
	public RRQ(short OP,String name){
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
