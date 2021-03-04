package bgu.spl171.net.impl.tftp.packets;

public class BCAST implements Packet{
	private String statusOffile;
	private String fileName;
	
	public BCAST(String statusOffile,String fileName){
		this.statusOffile=statusOffile;
		this.fileName=fileName;
	}
	public short getOP(){
		return (short)9;
	}
	
	public String getStatus(){
		return statusOffile;
	}
	
	public String getName(){
		return fileName;
	}

}
