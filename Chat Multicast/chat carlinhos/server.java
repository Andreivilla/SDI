import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class server {
    private static final int TCP_PORT = 12345;
    private static final int UDP_PORT = 6789;
    private static final String MULTICAST_ADDRESS = "224.0.0.1";
    public static int count_id = 0;
    
    public static int increse_id(){
        count_id++;
        return count_id;
    }

    public static void main(String[] args) throws IOException {
        ExecutorService pool = Executors.newFixedThreadPool(20);
        InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);

        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            System.out.println("Server online na porta: " + TCP_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                pool.execute(() -> {
                    try {
                        handleClient(clientSocket, group);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    private static void handleClient(Socket clientSocket, InetAddress group) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String input_line;

        try (MulticastSocket multicastSocket = new MulticastSocket()) {
            multicastSocket.setTimeToLive(0); // Adjust as needed for your network
            
            while ((input_line = in.readLine()) != null) {
                System.out.println(input_line);
                if(input_line.contains("##need_id##")){
                    String[] input_line_parts = input_line.split("##login##");
                    input_line = String.valueOf(increse_id()) + "##login##" + input_line_parts[1] + "##login##" + input_line_parts[2];
                }
                byte[] buffer = input_line.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, UDP_PORT);
                multicastSocket.send(packet);
                
            }
        } finally {
            clientSocket.close();
        }
    }
}
