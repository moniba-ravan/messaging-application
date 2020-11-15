package server;

import client.Client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class CheckSocket implements Runnable{
    private ArrayList<Client> users;

    public CheckSocket() {
        users = new ArrayList<>();
    }

    private boolean socketWorks(Client user){
        try {
            Socket check = new Socket(user.getIp(), user.getPort());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(check.getOutputStream()));
            writer.write( "serverChecking\n"); //introducing to this friend
            writer.flush();
            return true;
        } catch (IOException e) {
            return false;
        }

    }
    public int checkUsername(String username ){
        for( int i = 0 ; i < users.size() ; i ++ ) {
            if (users.get(i).getUsername().trim().equals(username.trim())) {
                if( socketWorks(users.get(i)))
                    return i;
                else {
                    users.remove(i);
                    i--;
                }
            }
        }
        return -1;
    }

    public void addUser(Client user){
        users.add(user);
    }

    public Client getUser( int idx ){
        return users.get(idx);
    }
    @Override
    public void run() {
        while( true ){
            for (int i = 0 ; i < users.size() ; i ++ ){
                if( !socketWorks(users.get(i)))
                    users.remove(i);

            }
        }
    }
}
