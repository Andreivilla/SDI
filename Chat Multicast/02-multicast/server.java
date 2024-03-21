import java.io.*;
import java.net.*;

public class server {
    public static void main(String[] args) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName("230.0.0.0");
        int port = 4446;

        try {
            while (true) {
                byte[] buf = new byte[256];
                // Recebe a requisição do cliente
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                // Envia a mensagem para o grupo
                packet = new DatagramPacket(buf, buf.length, group, port);
                socket.send(packet);
            }
        } finally {
            socket.close();
        }
    }
}
