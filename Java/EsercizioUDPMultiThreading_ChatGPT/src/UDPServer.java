import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UDPServer {
    public static void main(String[] args) {
        int port = 9876;
        Random random = new Random();

        // Mappa per tenere traccia dei numeri da indovinare per ciascun client
        Map<String, Integer> clientTargetNumbers = new HashMap<>();

        System.out.println("Server avviato.");

        try (DatagramSocket serverSocket = new DatagramSocket(port)) {
            byte[] receiveBuffer = new byte[1024];
            byte[] sendBuffer;

            while (true) {
                // Ricezione dei dati dal client
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                serverSocket.receive(receivePacket);

                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                String clientKey = clientAddress.toString() + ":" + clientPort;

                String clientMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("Ricevuto dal client (" + clientKey + "): " + clientMessage);

                // Recupera il numero da indovinare per questo client
                int targetNumber = clientTargetNumbers.getOrDefault(clientKey, -1);

                // Se il client non ha ancora un numero da indovinare, gli viene assegnato uno nuovo
                if (targetNumber == -1) {
                    targetNumber = random.nextInt(100) + 1;
                    clientTargetNumbers.put(clientKey, targetNumber);
                    System.out.println("Nuovo numero da indovinare per il client (" + clientKey + "): " + targetNumber);
                }

                // Analisi del messaggio del client
                int guessedNumber;
                try {
                    guessedNumber = Integer.parseInt(clientMessage.trim());
                } catch (NumberFormatException e) {
                    guessedNumber = -1; // Valore non valido
                }

                String response;
                if (guessedNumber == targetNumber) {
                    response = "Corretto! Hai indovinato!";
                    System.out.println("Client " + clientKey + " ha indovinato il numero!");
                    // Assegna un nuovo numero da indovinare al client
                    targetNumber = random.nextInt(100) + 1;
                    clientTargetNumbers.put(clientKey, targetNumber);
                    System.out.println("Nuovo numero da indovinare per il client (" + clientKey + "): " + targetNumber);
                } else if (guessedNumber < targetNumber) {
                    response = "Troppo basso!";
                } else {
                    response = "Troppo alto!";
                }

                // Invio della risposta al client
                sendBuffer = response.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, clientPort);
                serverSocket.send(sendPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}