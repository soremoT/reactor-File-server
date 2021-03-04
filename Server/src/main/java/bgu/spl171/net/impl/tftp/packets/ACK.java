package bgu.spl171.net.impl.tftp.packets;

public class ACK implements Packet {
	private short block;
	private short op;
	private boolean serverACK;
	
	public ACK(int block,boolean serverACK){
		this.op=4;
		this.block=(short)block;
		this.serverACK=serverACK;
	}
	public boolean ACKisFromClient(){
		return serverACK;
	}
	public void incBlock(){
		block++;
	}
	public short getOP(){
		return op;
	}
	public short getBlock(){
		return block;
	}

	

}
