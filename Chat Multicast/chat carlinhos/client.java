import java.io.*;
import java.net.*;

public class client {
    private static final String SERVER_HOST = "localhost";
    private static final int TCP_PORT = 12345;
    private static final int UDP_PORT = 6789;
    private static final String MULTICAST_ADDRESS = "224.0.0.1";
    public static int user_id = -1;

    public static void set_user_id(int id){
        user_id = id;
    }

    public static int get_user_id(){
        return user_id;
    }

    public static void main(String[] args) throws IOException {
        InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        
        System.out.println("Digite seu nome");
        String user_name = stdIn.readLine();

        // Start a thread to listen to multicast messages
        new Thread(() -> {
            try (MulticastSocket multicastSocket = new MulticastSocket(UDP_PORT)) {
                multicastSocket.joinGroup(group);
                byte[] buffer = new byte[1000];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    multicastSocket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    String[] received_parts = received.split("##login##");
                    
                    if(get_user_id() == -1){
                        //received_parts[2] = received_parts[2].replace("##user_id##", "");
                        //System.out.println();
                        //user_id = String.valueOf(received_parts[3]);
                        //System.err.println("oque tem no 3 " + received_parts[2]);
                        set_user_id(Integer.parseInt(received_parts[0]));
                    }
                        //user_id = String.valueOf(received);
                        //System.out.println(get_user_id() != Integer.parseInt(received_parts[0]));
                    if(get_user_id() != Integer.parseInt(received_parts[0])){
                        
                        System.out.println(received_parts[1] + ": " + (received_parts[2].replace("##need_id##", "")));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Connect to the server and send messages
        try (Socket socket = new Socket(SERVER_HOST, TCP_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            stdIn) {
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                if(get_user_id() == -1){
                    userInput = get_user_id() + "##login##" + user_name + "##login##" + userInput + "##need_id##";    
                }else{
                    userInput = get_user_id() + "##login##" + user_name + "##login##" + userInput;
                }
                out.println(userInput);
            }
        }
    }
}

