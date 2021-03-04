package bgu.spl171.net.srv;

import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.impl.tftp.ConnectionsImpl;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private ConnectionsImpl<T> connections;
    private int connectionID;

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol,ConnectionsImpl<T> connections,int connectionID) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.connections=connections;
        this.connectionID=connectionID;
    }

    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;
            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());
            
          //add a connection to the implementation of connections
    		connections.addConection(connectionID, this);
    		//call the start on the protocol send the connections instance with the  connected clients
    		protocol.start(connectionID, connections);
    		
            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    protocol.process(nextMessage);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }

	@Override
	//changed location of write code to send function to implement the interface
	public void send(T msg) {
		if (msg != null) {
            try {
				out.write(encdec.encode(msg));
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
           
		}
	}
}
