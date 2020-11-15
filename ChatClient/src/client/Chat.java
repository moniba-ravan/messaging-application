package client;


import java.io.*;
import java.net.*;
import java.util.ArrayList;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Chat{
    private Client user;
    private ArrayList<Friend> friends;
    private DatagramSocket UDPSocket;
    private ExecutorService pool;
    private
    BufferedReader console;

    public Chat(Client user) throws SocketException {
        this.user = user;
        friends = new ArrayList<>();
        UDPSocket= new DatagramSocket();
        pool = Executors.newFixedThreadPool(100);
        console = new BufferedReader(new InputStreamReader( System.in ));
    }

    private String sendDataToServer(String sendData) throws IOException {

        DatagramPacket sentPacket = new DatagramPacket(sendData.getBytes(), sendData.getBytes().length,
                InetAddress.getByName("localhost"), 50000);
        UDPSocket.send(sentPacket);

        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        UDPSocket.receive(packet);

        String response = new String(packet.getData());
        return response;

    }

    void addFriend(Socket friend) throws IOException {
        BufferedReader reader = new BufferedReader( new InputStreamReader(friend.getInputStream() ));
        String username = reader.readLine().trim();
        if(username.trim().equals("serverChecking".trim()))
            return;
        friends.add(new Friend(username, friend));
        ListeningServer listeningServer = new ListeningServer(username, friend, this);
        pool.execute(listeningServer);
        System.out.println("\n" + username + " is online now!");
    }
    void addMessage(String username, String message){
        for (Friend friend : friends) {
            if (friend.getUsername().equals(username)) {
                friend.addMessage(message);
                break;
            }
        }
    }


    private void readAllTheMessages(){
        boolean hasMessage = false;
        for (int i = 0 ; i < friends.size() ; i ++) {
            ArrayList<String> message = friends.get(i).getMessages();
            if (!message.isEmpty()) {
                hasMessage = true;
                System.out.println("\n>>> Messages from " + friends.get(i).getUsername());
            }
            for (String s : message) {
                System.out.println("    " + s);
            }
            if(!friends.get(i).isConnect()){
                friends.remove(i);
                i--;
            }
        }
        if( !hasMessage ){
            System.out.println("\n!!There is no new message!");
        }

    }
    private void online(){

        for(Friend friend: friends ){
            if(friend.isConnect())
                System.out.println("  " + friend.getUsername());
        }
    }
    private int found(String username ){
        for(int i = 0 ; i < friends.size(); i ++  ){
            if( friends.get(i).getUsername().trim().equals(username.trim())){
                if(friends.get(i).isConnect()) {
                    return i;
                }
                return -1;
            }
        }
        return -1;
    }
    private void sendMessage(){
        Socket friend;
        while( true ) {
            System.out.println("Send a message to");
            online();
            System.out.println("  A new username");
            System.out.println("or Enter \'exit\' to exit.");
            try {
                System.out.print("> ");
                String username = console.readLine();
                if (username.startsWith("exit")) {
                    return;
                }
                if( user.getUsername().equals(username.trim())){
                    System.out.println("You can't send a message to yourself!");
                    continue;
                }

                int idx = found(username);
                if (idx != -1) {
                    friend = friends.get(idx).getSocket(); // was connected
                } else {
                    String re = "connect " + username;
                    re = sendDataToServer(re);
                    if (re.startsWith("403")) {
                        System.out.println("This username doesn't exist");
                        continue;
                    } else {
                        String[] tokens = re.split(" ");
                        String ip = tokens[1].trim();
                        int port = Integer.parseInt(tokens[2].trim());
                        friend = new Socket(ip, port); // new connected socket
                        friends.add(new Friend(tokens[0].trim(), friend)); // add to friends
                        pool.execute(new ListeningServer(username, friend, this)); // any new message from this friend

                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(friend.getOutputStream()));
                        writer.write(user.getUsername() + "\n"); //introducing to this friend
                        writer.flush();
                    }
                }
                System.out.println("Enter your message:");
                System.out.println("    !!Enter \"end\" in a new line at the end of your message!!");
                String message = "", temp;

                while (true) {
                    System.out.print("> ");
                    temp = console.readLine();
                    if (temp.startsWith("end"))
                        break;
                    message = message + temp + "#";
                }
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(friend.getOutputStream()));
                writer.write(message + "\n");
                writer.flush();
                System.out.println("Your message to " + username + " has sent!");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void removeFriend(String username){
        for( int i = 0 ; i < friends.size() ; i ++ ){
            if (friends.get(i).getUsername().trim().equals(username.trim())){
                    if(friends.get(i).haveMessage() )
                        friends.get(i).setConnect(false);
                    else {
                        try {
                            friends.get(i).getSocket().close();
                            friends.remove(i);
                        } catch (IOException ignored) {
                        }
                    }
                System.out.println("\n" + username + " is offline now!");
                break;
            }
        }
    }
    private void logout(){
        try {
            for (Friend friend : friends) {
                friend.getSocket().close();
            }
            UDPSocket.close();
            pool.shutdownNow();
            console.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void run() {

        while( true ){
            System.out.println("Choose an option: ");
            System.out.println(" 1)show the new messages");
            System.out.println(" 2)send a message to..");
            System.out.println(" 3)logout");
            int option = 0;
            BufferedReader console = new BufferedReader(new InputStreamReader( System.in ));

            try {
                System.out.print("> ");
                option = Integer.parseInt(console.readLine().trim());
                switch ( option ) {
                    case 1:
                        readAllTheMessages();
                        break;
                    case 2:
                        sendMessage();
                        break;
                    case 3:
                        logout();
                        return;

                    default:
                        System.out.println("Enter a valid number!");

                }
            } catch (NumberFormatException | IOException e) {
                System.out.println("Enter a valid number!");
            }


        }
    }
}
