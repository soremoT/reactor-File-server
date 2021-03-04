package bgu.spl171.net.impl.tftp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.impl.tftp.packets.ACK;
import bgu.spl171.net.impl.tftp.packets.BCAST;
import bgu.spl171.net.impl.tftp.packets.DATA;
import bgu.spl171.net.impl.tftp.packets.DELRQ;
import bgu.spl171.net.impl.tftp.packets.DIRQ;
import bgu.spl171.net.impl.tftp.packets.DISC;
import bgu.spl171.net.impl.tftp.packets.ERROR;
import bgu.spl171.net.impl.tftp.packets.LOGRQ;
import bgu.spl171.net.impl.tftp.packets.Packet;
import bgu.spl171.net.impl.tftp.packets.RRQ;
import bgu.spl171.net.impl.tftp.packets.WRQ;
/**
 * 
 * @author romavic
 *
 * @param <T> packet object to process
 */
public class BidiMessagingProtocolImpl<T> implements BidiMessagingProtocol<Packet> {
	private int connectionId;
	private Connections<Packet> connections;
	//vector to hold the names and status of all the connections
	private HashMap<Integer,ConnectionObject> clientStatusOfConnection;
	private String writeName;
	private String readName;
	boolean shouldTerminate;
	@Override
	public void start(int connectionId, Connections<Packet> connections) {
		this.connectionId=connectionId;
		this.connections=connections;
		ConnectionHolderSingleton ObjectOfConnections=ConnectionHolderSingleton.getInstance();
		clientStatusOfConnection=ObjectOfConnections.getListOfClients();
		shouldTerminate=false;
	}

	@Override
	public void process(Packet message) {
		//error from reading bytes illegal code
		if(message instanceof ERROR){
			connections.send(connectionId, message);
			return;
			}
		//login case
		if(message instanceof LOGRQ){
			logrq((LOGRQ)message);
			return;
		}
		if(!clientStatusOfConnection.get(connectionId).isLogedIn()){
			connections.send(connectionId, new ERROR(6));
			return;
		}
		if(message instanceof DELRQ){
			if(fileExists(((DELRQ)message).getName()))
				removeFile(((DELRQ)message).getName());
			else connections.send(connectionId, new ERROR(2));
			return;
		}
		if(message instanceof DISC){
			if(clientStatusOfConnection.get(connectionId).isLogedIn()){
			connections.send(connectionId, new ACK(0,false));
			connections.disconnect(connectionId);
			shouldTerminate=true;
			
			}else connections.send(connectionId, new ERROR(6));
			return;
		}
		if(message instanceof DIRQ){
			getDirectory();
			return;
		}
		if(message instanceof ACK){
				connections.send(connectionId, message);
				return;
		}
		
		if(message instanceof RRQ){
			if(fileExists(((RRQ) message).getName())){
				readName=((RRQ) message).getName();
				connections.send(connectionId,getFile());
			}else connections.send(connectionId, new ERROR(1));
			return;
		}
		
		if(message instanceof WRQ){
			if(!fileExists(((WRQ) message).getName())){
				writeName=((WRQ) message).getName();
				connections.send(connectionId, new ACK(0,false));
			}else connections.send(connectionId, new ERROR(5));
			return;
		}
		
		if(message instanceof DATA){
			connections.send(connectionId, new ACK(((DATA)message).getBlock(),false));
			addFile((DATA)message);
			return;
		}
		
	}
	/**
	 * 
	 * @return the full file inside the data package
	 */
	private Packet getFile() {
		File file = new File("Files"+File.separator+readName);
		if(file.isFile()){
			byte[] bytesArray = new byte[(int) file.length()];
			try(FileInputStream fis = new FileInputStream(file)){
			fis.read(bytesArray); //read file into bytes[]
			} catch (IOException e) {
				return new ERROR(1);
			}
			return new DATA(1,bytesArray);
		}
		else return new ERROR(2);
	}
	/**
	 * sends the directory list
	 */
	private void getDirectory() {
		File folder = new File("Files"+File.separator);
		File[] listOfFiles = folder.listFiles();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		for (File file : listOfFiles) {
    		if (file.isFile()) {
        		byte[] name=(file.getName()+"\0").getBytes();
        		try {
					outputStream.write(name);
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
		}
		connections.send(connectionId, new DATA(1,outputStream.toByteArray()));
	}

	
	/**
	 * check if the client is logedIn in
	 * @param message return error or acknowledge depending on the client log in state
	 */
	private void logrq(LOGRQ message){	
		Collection<ConnectionObject> nameCheck=clientStatusOfConnection.values();
		for(ConnectionObject name:nameCheck){
			if(name.getName().equals(message.getName())){
				connections.send(connectionId,  new ERROR(7));
				return;
			}
		}
		if(!clientStatusOfConnection.get(connectionId).isLogedIn()){
			clientStatusOfConnection.get(connectionId).setConnection(message.getName());
			connections.send(connectionId,  new ACK(0,false));
		}//user already logedIN
		else {connections.send(connectionId,  new ERROR(7));
		}
	}

	@Override
	public boolean shouldTerminate() {
		
		return shouldTerminate;
	}
	
	private void removeFile(String name){
		File f = new File("Files"+File.separator+name); 
		if(f.delete()){
			connections.send(connectionId, new ACK(0,false));
			Packet msg=new BCAST("0",name);
			Collection<ConnectionObject> broadcastList=clientStatusOfConnection.values();
			for(ConnectionObject curr:broadcastList){
				if(curr.isLogedIn())
					connections.send(curr.getID(), msg);
			}
		}else connections.send(connectionId, new ERROR(2));
	}
	
	
	/**
	 * check if this file is even in the folder
	 * @param name of the file to find
	 * @return true if file exists
	 */
	private boolean fileExists(String name){
		File f = new File("Files"+File.separator+name); 
		return f.exists();
	}
	
	/**
	 * adds a file to the files folder
	 * @param file writes the whole array of bytes to the Files folder
	 */
	private void addFile(DATA file){
		try (FileOutputStream fos = new FileOutputStream("Files"+File.separator+writeName)){
		fos.write(file.getData());	
		} catch ( IOException e) {
			//failed to upload
			connections.send(connectionId, new ERROR(3));
			return;
		}
		Packet msg=new BCAST("1",writeName);
		Collection<ConnectionObject> broadcastList=clientStatusOfConnection.values();
		for(ConnectionObject curr:broadcastList){
			if(curr.isLogedIn())
				connections.send(curr.getID(), msg);
		}
	}
}
