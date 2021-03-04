 
package bgu.spl171.net.impl.TFTPreactor;
import bgu.spl171.net.impl.tftp.BidiMessagingProtocolImpl;
import bgu.spl171.net.impl.tftp.MessageEncoderDecoderImpl;
import bgu.spl171.net.srv.Server;

public class ReactorMain {

	
	public static void main(String[] args) {
		
		
		Integer port = Integer.valueOf(args[0]);
		
		Server.reactor(7,port, ()->new BidiMessagingProtocolImpl<>(),
				()->new MessageEncoderDecoderImpl<>()).serve();
	}
}
