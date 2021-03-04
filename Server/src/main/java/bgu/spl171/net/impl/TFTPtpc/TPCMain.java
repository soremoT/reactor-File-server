 
package bgu.spl171.net.impl.TFTPtpc;


import bgu.spl171.net.impl.tftp.BidiMessagingProtocolImpl;
import bgu.spl171.net.impl.tftp.MessageEncoderDecoderImpl;
import bgu.spl171.net.srv.Server;

public class TPCMain {
	

	public static  void main(String[] args) {

		
		Integer port = Integer.valueOf(args[0]);
		Server.threadPerClient(port, ()->new BidiMessagingProtocolImpl<>(),()->new MessageEncoderDecoderImpl<>()).serve();
	}
	 
}
