package bgu.spl171.net.api.bidi;

import java.io.IOException;

import bgu.spl171.net.srv.ConnectionHandler;

public interface Connections<T> {
	

    boolean send(int connectionId, T msg);

    void broadcast(T msg);

    void disconnect(int connectionId);
    
}