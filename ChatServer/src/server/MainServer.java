package server;

import client.Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServer {
    private DatagramSocket serverSocket;
    ExecutorService pool;
    CheckSocket checkSocket;
    private MainServer() throws SocketException {
        serverSocket = new DatagramSocket( 50000);
        pool = Executors.newFixedThreadPool(5);
        checkSocket = new CheckSocket();
    }

    public void run() throws IOException {
        pool.execute(checkSocket); //check the sockets


        while (true) {
            byte[] buffer = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            System.out.println("\nserver is listening...");
            serverSocket.receive(packet);
            String req = new String(packet.getData());
            String response = "";
            if (req.startsWith("register")) {
                // registering
                String[] tokens = req.split(" ");
                response = register(tokens[1].trim(), tokens[2].trim(), Integer.parseInt(tokens[3].trim()) );

            }else if( req.startsWith("connect")){
                //find another user
                String [] tokens = req.split(" ");
                response = findUsername(tokens[1].trim());
            }
            DatagramPacket sendPacket = new DatagramPacket(response.getBytes(), response.getBytes().length,
                    packet.getAddress(), packet.getPort());
            serverSocket.send(sendPacket);
        }
    }

    private String register( String username , String ip, int port ){

        System.out.println("\n" + username + " wants to register!!");

        if (checkSocket.checkUsername(username) == -1) {
            checkSocket.addUser(new Client(username.trim(), ip.trim(), port));

            System.out.println("User with "+ username + " username ,"+ ip + " IP ,"+ port + " Port is registered.");

            return "101"; // registered

        }
        System.out.println("Username already exits.");
        return "401"; //username already exists
    }

    private String findUsername(String username ){

        System.out.println("\nAsk to connect to " + username);

        int idx = checkSocket.checkUsername(username);
        if( idx == -1) {

            System.out.println("Username doesn't exist");

            return "403"; // username doesn't exist
        }
        Client temp = checkSocket.getUser(idx);
        String response = temp.getUsername() + " " + temp.getIp() + " " +temp.getPort();

        System.out.println(username + " exists");

        return response;
    }


    public static void main(String [] args ){
        try {
            MainServer mainServer = new MainServer();
            mainServer.run();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
