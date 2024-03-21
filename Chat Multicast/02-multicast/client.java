import java.io.*;
import java.net.*;
import java.util.Scanner;

public class client {
    public static void main(String[] args) throws IOException {
        InetAddress group = InetAddress.getByName("230.0.0.0");
        int port = 4446;
        MulticastSocket socket = new MulticastSocket(port);

        try {
            socket.joinGroup(group);
            Thread listenerThread = new Thread(() -> {
                try {
                    while (true) {
                        byte[] buf = new byte[256];
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        String received = new String(packet.getData(), 0, packet.getLength());
                        System.out.println( received);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            listenerThread.start();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("Digite uma mensagem para enviar:");
                String message = scanner.nextLine();

                byte[] buf = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, group, port);
                socket.send(packet);
            }
        } finally {
            socket.leaveGroup(group);
            socket.close();
        }
    }
}
