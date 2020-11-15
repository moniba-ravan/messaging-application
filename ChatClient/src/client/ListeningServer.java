package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLOutput;

public class ListeningServer implements Runnable {
    int status;
    Client user;
    ServerSocket serverSocket;
    String username;
    Socket friend;
    Chat chat;

    public ListeningServer(String username, Socket friend, Chat chat) {
        this.status = 1;
        this.username = username;
        this.friend = friend;
        this.chat = chat;
    }

    public ListeningServer(Client user, Chat chat) throws IOException {
        this.status = 2;
        this.username = "server";
        this.user = user;
        this.chat = chat;
        serverSocket = new ServerSocket(user.getPort());
    }
    public void close(){
        if(status == 2 ) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    @Override
    public void run(){
        if( status == 1 ){ //waiting for a new message of this friend
            while( true ) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(friend.getInputStream()));
                    String message = reader.readLine();
                    if( message == null ) {
                        chat.removeFriend(username);
                        break;
                    }
                    String [] messages = message.split("#");
                    for (String message1 : messages) chat.addMessage(username, message1);
                }catch (IOException e) {
                    break;
                }
            }

        }else if( status == 2 ) { //Server is waiting for a new user to connect
            while (true) {
                try {
                    Socket friend = serverSocket.accept();
                    chat.addFriend(friend);
                } catch (SocketException e) {
                    break;
                }catch (IOException e) {
                    System.out.println("Error in Listening Server/status 2");
                    break;
                }
            }
        }
    }
}
