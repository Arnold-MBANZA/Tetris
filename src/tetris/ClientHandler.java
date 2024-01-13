package tetris;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ServeurTetris serveurTetris;
    private BufferedReader reader;
    private PrintWriter writer;

    private int joueurID;  // Nouvelle variable pour stocker l'ID du joueur

    public ClientHandler(Socket clientSocket, ServeurTetris serveurTetris) {
        this.clientSocket = clientSocket;
        this.serveurTetris = serveurTetris;

        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream(), true);

            // Envoi du message d'inscription avec l'ID du joueur
            writer.println("INSCRIPTION " + joueurID);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public int getJoueurID() {
        return joueurID;
    }

    @Override
    public void run() {
    try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
        String message;
        
        while ((message = reader.readLine()) != null) {
            System.out.println("Message du client " + joueurID + ": " + message);
            // Diffusez les mises à jour aux autres clients
            ServeurTetris.diffuserMessage(message);
            // Traiter les messages reçus du client
            if (message.startsWith("POSITIONS_JOUEUR")) {
                // Extrait et traite les positions envoyées par le client
                String[] parts = message.split(" ");
                int joueurID = Integer.parseInt(parts[1]);
                ArrayList<Point> positions = new ArrayList<>();
                for (int i = 2; i < parts.length; i += 2) {
                    int x = Integer.parseInt(parts[i]);
                    int y = Integer.parseInt(parts[i + 1]);
                    positions.add(new Point(x, y));
                }
                serveurTetris.ajouterPositionsJoueur(joueurID, positions);
            } else {
                
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        // Retirer le client de la liste des clients connectés
        serveurTetris.retirerClient(this);

        // Fermer les flux et le socket
        try {
            reader.close();
            writer.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


    public void sendMessage(String message) {
        writer.println(message);
    }
}
