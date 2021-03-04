package bgu.spl171.net.impl.tftp.packets;

public class LOGRQ implements Packet{
	private short OP;
	private String userName;
	
	public LOGRQ(short OP,String name){
		this.OP=OP;
		this.userName=name;
	}
	
	public short getOP(){
		return OP;
	}
	
	public String getName(){
		return userName;
	}

	


}
