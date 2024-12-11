
import java.net.*;
import java.util.Scanner;

public class UDPClient {
    public static void main(String[] args) {
        String serverHost = "localhost";
        int serverPort = 9876;

        try (DatagramSocket clientSocket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(serverHost);
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.print("Indovina un numero (1-100): ");
                String guess = scanner.nextLine();

                // Invia il messaggio al server
                byte[] sendBuffer = guess.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, serverPort);
                clientSocket.send(sendPacket);

                // Riceve la risposta dal server
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                clientSocket.receive(receivePacket);

                String serverResponse = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("Risposta del server: " + serverResponse);

                // Se il numero è corretto, termina il gioco
                if (serverResponse.contains("Corretto")) {
                    System.out.println("Hai vinto!");
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}