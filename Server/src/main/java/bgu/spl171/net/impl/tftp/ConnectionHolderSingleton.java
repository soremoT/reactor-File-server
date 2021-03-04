package bgu.spl171.net.impl.tftp;

import java.util.HashMap;
/**
 * 
 * @author Victor
 *A class to duplicate the connections vector but only for loggedIN users
 */
public class ConnectionHolderSingleton {
	//make singleton to avoid new objects of this type
	private HashMap<Integer,ConnectionObject> listOFClients;
	private static ConnectionHolderSingleton instance=new ConnectionHolderSingleton();
	//the duplicate vector
	private ConnectionHolderSingleton(){
		listOFClients=new HashMap<Integer,ConnectionObject>();
	}
	
	public static ConnectionHolderSingleton getInstance() {
	      return instance;
	   }
	
	public HashMap<Integer,ConnectionObject> getListOfClients(){
		return  listOFClients;
	}
	/**
	 * client logged in with this id need to create new object to save his status 
	 * @param connectionId
	 */
	public void setConnection(int connectionId){
		listOFClients.put(connectionId, new ConnectionObject(connectionId));
	}
	
	public void deleteConnection(int connectionId){
		listOFClients.remove(connectionId);
	}
	
	
	
	

}
