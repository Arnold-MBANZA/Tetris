package tetris;


import java.awt.Point;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import static tetris.ConstantesTetris.HAUTEUR_FENETRE;
import static tetris.ConstantesTetris.LARGEUR_FENETRE;

public class ServeurTetris {
 
    private static List<ClientHandler> clients = new ArrayList<>();
    
    private List<ArrayList<Point>> positionsAutresJoueurs = new ArrayList<>();
    private Map<ClientHandler, PrintWriter> clientWriters = new HashMap<>();

    private ServerSocket serverSocket;
    private int port = 8080;

    public ServeurTetris() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Serveur Tetris en attente de connexions sur le port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouvelle connexion acceptée.");

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);

                Thread threadClient = new Thread(clientHandler);
                threadClient.start();
                
                System.out.println("Programme principal Tetris en exécution...");
                SwingUtilities.invokeLater(() -> {
                    JFrame fenetre = new JFrame("Tetris");
                    fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    fenetre.setSize(LARGEUR_FENETRE, HAUTEUR_FENETRE);
                    fenetre.setResizable(false);

                    JeuTetris jeu = new JeuTetris();
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
  public int ajouterNouveauJoueur(ClientHandler client, PrintWriter writer) {
    int joueurID = clients.size() + 1;  
    clients.add(client);
    positionsAutresJoueurs.add(new ArrayList<>());
    
    // Ajoutez le writer du client à la map
    clientWriters.put(client, writer);

    return joueurID;
}  
        
    public void retirerClient(ClientHandler client) {
        clients.remove(client);}

    public synchronized void ajouterPositionsJoueur(int joueurID, ArrayList<Point> positions) {
    while (positionsAutresJoueurs.size() <= joueurID) {
        positionsAutresJoueurs.add(new ArrayList<>());
    }
    positionsAutresJoueurs.set(joueurID, positions);
    diffuserPositionsAutresJoueurs();
}

   private void diffuserPositionsAutresJoueurs() {
    for (ClientHandler client : clients) {
        List<ArrayList<Point>> positionsJoueursAutres = new ArrayList<>(positionsAutresJoueurs);
        positionsJoueursAutres.remove(client.getJoueurID()); // Retirer les positions du joueur actuel

        // Envoyez les positions mises à jour à ce client spécifique
        client.sendMessage("POSITIONS_AUTRES_JOUEURS " + positionsJoueursAutres.toString());

        // Envoyez également les positions mises à jour à tous les autres clients
        envoyerPositionsMisesAJourAuxAutres(client, positionsJoueursAutres);
    }
}

    private void envoyerPositionsMisesAJourAuxAutres(ClientHandler sender, List<ArrayList<Point>> positionsJoueursAutres) {
    for (ClientHandler client : clients) {
        if (client != sender) {
            PrintWriter writer = clientWriters.get(client);
            writer.println("POSITIONS_AUTRES_JOUEURS " + positionsJoueursAutres.toString());
        }
    }
}
    
    public static void main(String[] args) {
        System.out.println("Serveur Tetris en exécution...");
        ServeurTetris serveur = new ServeurTetris();
        
        Thread threadEcoute = new Thread(() -> serveur.ecouterConnexions());
        threadEcoute.start();
    }

    public void ecouterConnexions() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouvelle connexion acceptée.");

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);

                Thread threadClient = new Thread(clientHandler);
                threadClient.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void diffuserMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}
