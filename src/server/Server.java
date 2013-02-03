package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import observer.Observer;

import communication.ActionManager;
import communication.BasicMessage;
import communication.Message;
import communication.MessageHandler;
import communication.RemoteHost;
import communication.UnicastMessageWrapper;

public class Server extends AbstractServer implements Observer {
    
    protected static Server instance = null;
    
    protected Server(int port, int maxClientsNumber, InetAddress ip,
            MessageHandler messageHandler, ResponseHandler responseHandler) {
        super(port, maxClientsNumber, ip, messageHandler, responseHandler);
        ((OthelloResponseHandler)this.responseHandler).setPlayers(this.hosts);        
    }
    
    public static Server getInstance(int port, int maxClientsNumber, InetAddress ip,
            MessageHandler messageHandler, ResponseHandler responseHandler) {
        if(instance == null) {
            instance = new Server(port, maxClientsNumber, ip, messageHandler, responseHandler);
        }
        return instance;
    }
    
    @Override
    public void addClient (Socket socket) throws IOException {
        super.addClient(socket);
        this.responseHandler.sendResponse(new BasicMessage(ActionManager.getActionNumber("connect")), null);
    }
    
    @Override
    public RemoteHost getHost(Socket socket) throws IOException {
        return new RemotePlayer(socket);
    }

    @Override
    public void update(Object arg) {
        if(arg instanceof RemoteHost) {
            this.removeClient((RemoteHost) arg);
        } else if(arg instanceof UnicastMessageWrapper) {
            UnicastMessageWrapper wrapper = (UnicastMessageWrapper)arg;
            Message response = this.responseHandler.generateResponse(wrapper.getMessage(), wrapper.getRecipient());
            this.responseHandler.sendResponse(response, wrapper.getRecipient());
            if(ActionManager.getActionNumber("play") == response.getReturnCode()) {
                ((OthelloResponseHandler)this.responseHandler).checkGameOver();
            }
        }
    }
}
