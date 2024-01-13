package tetris;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import static tetris.ConstantesTetris.HAUTEUR_FENETRE;
import static tetris.ConstantesTetris.LARGEUR_FENETRE;


public class ClientTetris {

    private static final String SERVER_IP = "192.168.50.215";
    private static final int SERVER_PORT = 8080;
    private static final int joueurID = 8; 

    private static void traiterPositionsAutresJoueurs(String message) {
        SwingUtilities.invokeLater(() -> {
            traiterPositionsAutresJoueurs(message);
        });

    }

    
    public static void main(String[] args) {
        System.out.println("Client Tetris en exécution...");
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
         
            System.out.println("Programme principal Tetris en exécution...");
            SwingUtilities.invokeLater(() -> {
                JFrame fenetre = new JFrame("Tetris");
                fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                fenetre.setSize(LARGEUR_FENETRE, HAUTEUR_FENETRE);
                fenetre.setResizable(false);

                JeuTetris jeu = new JeuTetris(joueurID);
                fenetre.add(jeu);

                fenetre.addKeyListener(new EcouteurClavierTetris(jeu));

                // Utilisez un Timer pour gérer la descente automatique
                Timer timer = new Timer(1000, e -> {
                    if (!jeu.estFinDePartie()) {
                        jeu.descendre();
                        jeu.repaint(); // Redessinez le jeu après chaque descente
                    }
                });

                fenetre.setVisible(true);

                timer.start();
            });

            // Thread pour lire les messages du serveur
            new Thread(() -> {
                try {
                    String message;
                    while ((message = reader.readLine()) != null) {
                        System.out.println("Message du serveur : " + message);

                        // Traitez les messages du serveur et mettez à jour l'interface graphique du client
                        // en conséquence, par exemple en démarrant le jeu.
                        if (message.equals("DEBUT_PARTIE")) {
                            SwingUtilities.invokeLater(() -> {
                                demarrerJeu();
                            });
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Envoi de messages au serveur
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            String userInput;
            while ((userInput = consoleReader.readLine()) != null) {
                 // Envoi des messages spécifiques pour les actions
                if (userInput.equalsIgnoreCase("ROTATION") || userInput.equalsIgnoreCase("DEPLACEMENT") || userInput.equalsIgnoreCase("DESCENTE")) {
                     writer.println(userInput + " " + joueurID);  // Incluez l'identifiant du joueur dans le message
                } else {
                 
                }
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }   

    private static void demarrerJeu() {
       
        System.out.println("Début du jeu chez le client...");
        JFrame fenetreJeu = new JFrame("Jeu Tetris");
        fenetreJeu.setSize(400, 400);
        fenetreJeu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fenetreJeu.setVisible(true);
    }
}
