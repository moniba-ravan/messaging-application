package client;


import java.net.Socket;
import java.util.ArrayList;

public class Friend {
    private boolean connect = false;
    private String username;
    private Socket socket;
    private ArrayList<String> messages;
    public Friend(String username, Socket socket) {
        this.connect = true;
        this.username = username;
        this.socket = socket;
        messages = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public Socket getSocket() {
        return socket;
    }

    public ArrayList<String> getMessages() {
        ArrayList<String> temp =new ArrayList<>( messages);
        messages.clear();
        return temp;
    }

    public boolean isConnect() {
        return connect;
    }

    public void setConnect(boolean connect) {
        this.connect = connect;
    }
    public boolean haveMessage(){
        if(messages.isEmpty())
            return false;
        return true;
    }
    public void addMessage(String message){
        messages.add(message);
    }
}
