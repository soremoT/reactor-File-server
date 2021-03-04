package bgu.spl171.net.impl.tftp;
/**
 * 
 * @author Victor
 * this object defines the status of a connected client
 */
public class ConnectionObject {
	private String name="";
	private boolean logedIn=false;;
	private int connectionId;
	
	public ConnectionObject(int connectionId){
		this.connectionId=connectionId;
	}
	
	public void setConnection(String name){
		this.name=name;
		logedIn=true;
		name.length();
	}
	public String getName(){
		return name;
	}
	public boolean isLogedIn(){
		return logedIn;
	}
	public int getID(){
		return connectionId;
	}
	
	
}
