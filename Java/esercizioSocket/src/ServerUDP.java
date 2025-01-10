import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.SQLOutput;

/*
*
* DA FARE: GESTIONE MULTITHREADING DEI CLIENT
*
*/


public class ServerUDP {

    public static void main(String[] args) {
        int serverPort = 12345;
        byte[] receiveBuffer = new byte[1024];

        {
            try {
                DatagramSocket serverSocket = new DatagramSocket(serverPort);

                while(true) {
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    serverSocket.receive(receivePacket);

                    String receiveData = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
                    System.out.println("Ricevuto: " + receiveData);

                    //Ottenimento indirizzo ip e porta del client
                    InetAddress clientAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();

                    //Legge ogni riga del file per ottenere la quantità del prodotto richiesto dal client
                    BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\danie\\Desktop\\Daniel\\git\\MustoTPSIT\\Java\\esercizioSocket\\src\\ElencoProdotti"));
                    String quantità = "Prodotto non trovato";
                    while(br.ready()){
                        String[] prodotto = br.readLine().split(",");
                        if(receiveData.equalsIgnoreCase(prodotto[1].trim())) {
                            quantità = prodotto[2].trim();
                            break;
                        }
                    }

                    //Invio risposta al client
                    byte[] risposta = quantità.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(risposta, risposta.length, clientAddress, clientPort);
                    serverSocket.send(sendPacket);
                }

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
