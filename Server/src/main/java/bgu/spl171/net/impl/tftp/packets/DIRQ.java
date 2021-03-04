package bgu.spl171.net.impl.tftp.packets;

public class DIRQ implements Packet{
	private short op;
	public DIRQ(){
		this.op=6;
	}

	@Override
	public short getOP() {
		return op;
	}

	

}
