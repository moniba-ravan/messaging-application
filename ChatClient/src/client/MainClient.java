package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainClient {
    private Client user;
    private DatagramSocket UDPSocket;
    ExecutorService pool;
    public MainClient() throws SocketException {
         UDPSocket= new DatagramSocket();
         pool = Executors.newFixedThreadPool(5);
    }
    private String sendDataToServer(String sendData) throws IOException {
        DatagramPacket req;
        req = new DatagramPacket(sendData.getBytes(), sendData.getBytes().length, InetAddress.getByName("localhost"), 50000);
        UDPSocket.send(req);

        byte[] buffer;
        buffer = new byte[1024];
        DatagramPacket packet;
        packet = new DatagramPacket(buffer, buffer.length);
        UDPSocket.receive(packet);

        String res = new String(packet.getData());
        return res.trim();
    }

    private String getPort(){
        try {
            DatagramSocket temp = new DatagramSocket();
            String port = Integer.toString(temp.getLocalPort());
            temp.close();
            return port.trim();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;

    }
    private int register() throws IOException {

        System.out.println("Enter username:");
        System.out.println("    !!Enter \"exit\" to exit ");
        while( true ){
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("> ");
            String input = console.readLine().trim();

            if( input.startsWith("exit"))
                return -1; // exit

            if( input.indexOf(' ') >= 0 ){
                System.out.println("Please Enter a username without whitespace(\" \"):");
                continue;
            }
            String port = getPort(); // get a open port for TCP socket
            String ip = InetAddress.getLocalHost().getHostAddress();
            String sendData = "register " + input + " " + ip +" " + port;

            String response = sendDataToServer(sendData);

            if( response.startsWith("101")){ //registering has done
                user = new Client(input, ip, Integer.parseInt(port) );
                System.out.println("You are registered with " + input + ".");
                UDPSocket.close();
                return 1; // registered
            }else System.out.println("Username already exits!!" +
                    "\nEnter another username:");

        }
    }


    public void run(){
        try {
            int option = register(); //Register
            if( option == 1) {
                Chat chat = new Chat(user);//chat room
                ListeningServer listeningServer = new ListeningServer(user, chat); // listening to the other request
                pool.execute(listeningServer);
                chat.run();
                listeningServer.close();
                pool.shutdownNow();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
        MainClient mainClient = null;
        try {
            mainClient = new MainClient();
            mainClient.run();
            System.out.println("logout");
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

}
