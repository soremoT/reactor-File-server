package bgu.spl171.net.impl.tftp;

import java.util.Collection;
import java.util.HashMap;

import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.srv.ConnectionHandler;
/**
 * 
 * @author Victor
 *	Implementation of the connection Interface 
 * @param <T> the message received from the client
 */
public class ConnectionsImpl<T> implements Connections<T> {
	//list of connections
	private HashMap<Integer,ConnectionHandler<T>> listOfClients=new HashMap<Integer,ConnectionHandler<T>>();
	//list of logedIN connections
	private ConnectionHolderSingleton ObjectOfConnections=ConnectionHolderSingleton.getInstance();

	@Override//succeeded to send or not
	public synchronized boolean send(int connectionId, T msg) {
		
		if(listOfClients.get(connectionId)!=null){
			listOfClients.get(connectionId).send(msg);
			return true;
			}
		else return false;
	}

	@Override//send the same message to all clients
	public synchronized void broadcast(T msg) {
		Collection<ConnectionHandler<T>> allTheClients=listOfClients.values();
		for(ConnectionHandler<T> link:allTheClients){
			link.send(msg);
		}
	}

	@Override//disconnect specific client from both the connections and logedinConnections
	public synchronized void disconnect(int connectionId) {
			listOfClients.remove(connectionId);
			ObjectOfConnections.deleteConnection(connectionId);
	}
	/**
	 * only the server can see this function:adds a new connection
	 * @param connectionId the id given by the server
	 * @param client the link to send files thru
	 */
	public synchronized void addConection(int connectionId,ConnectionHandler<T> client){
		listOfClients.put(connectionId,client);
		ObjectOfConnections.setConnection(connectionId);
	}
}
