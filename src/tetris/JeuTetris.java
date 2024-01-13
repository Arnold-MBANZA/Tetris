package tetris;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class JeuTetris extends JPanel {

    private static final long serialVersionUID = -8715353373678321308L;

    // Tableau tridimensionnel représentant les différentes formes de tétraminos et leurs rotations
    private final Point[][][] Tetrominos = {
            // I-Piece
            { { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1) },
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3) },
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1) },
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3) } },
     
            // Pièce J
            { { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 0) },
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 2) },
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 2) },
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 0) } },

            // Pièce L
            { { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 2) },
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 2) },
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 0) },
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 0) } },

            // Pièce O
            { { new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
                    { new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
                    { new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
                    { new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) } },

            // Pièce S
            { { new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1) },
                    { new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) },
                    { new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1) },
                    { new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) } },

            // Pièce T
            { { new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1) },
                    { new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) },
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(1, 2) },
                    { new Point(1, 0), new Point(1, 1), new Point(2, 1), new Point(1, 2) } },

            // Pièce Z
            { { new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1) },
                    { new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(0, 2) },
                    { new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1) },
                    { new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(0, 2) } } };

     // Couleurs associées à chaque tétraminos
    private final Color[] couleursTetromino = { Color.cyan, Color.lightGray, Color.orange, Color.yellow, Color.green,
            Color.pink,
            Color.red };

    private Point originePiece; // Position d'origine du tétraminos en cours
    private int pieceCourante; // Indice de la pièce en cours dans le tableau Tetrominos
    private int rotation; // Rotation actuelle de la pièce
    private ArrayList<Integer> sac = new ArrayList<>(); // Sac pour le mélange aléatoire des pièces

    private long score; // Score du joueur
    private int lignesEffacees; // Nombre total de lignes effacées
    private Color[][] puit; // Grille représentant le puit

    private boolean finDePartie = false; // Indique si la partie est terminée
    private int joueurID; // Identifiant du joueur
    private ArrayList<Point> positionsAutresJoueurs;
    
    public JeuTetris() {
       initialiser(); // Initialisation du jeu
       this.positionsAutresJoueurs = new ArrayList<>(); // Vous pouvez appeler votre méthode d'initialisation ici
    }

    public JeuTetris(int joueurID) {
        this.joueurID = joueurID;
        this.positionsAutresJoueurs = new ArrayList<>();
        initialiser();
        seConnecterAuServeur();
    }
    
    private void seConnecterAuServeur() {
    Socket socket = null;
    try {
        // Remplacez "localhost" et 12345 par l'adresse IP et le port de votre serveur
        socket = new Socket("192.168.50.88", 8080);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

        // Envoi du message d'inscription avec l'ID du joueur
        writer.println("INSCRIPTION " + joueurID);

        // Créez un thread pour écouter les messages du serveur
        Thread threadEcouteur = new Thread(() -> {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println("Message du serveur: " + message);

                    // Traitez le message du serveur en fonction de vos besoins
                    if (message.startsWith("POSITIONS_AUTRES_JOUEURS")) {
                        // Mettez à jour les positions des autres joueurs
                        ArrayList<Point> nouvellesPositions = new ArrayList<>();
                        String positionsString = message.split(" ")[1];
                        String[] positionsArray = positionsString.substring(1, positionsString.length() - 1).split(",");
                        for (String position : positionsArray) {
                            String[] coordinates = position.trim().split(" ");
                            nouvellesPositions.add(new Point(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1])));
                        }
                        mettreAJourPositionsAutresJoueurs(nouvellesPositions);
                    } else {
                        // Traitement supplémentaire en fonction des besoins
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Démarrez le thread écouteur
        threadEcouteur.start();

        // ... (autres parties de votre code)

    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        // Fermez la connexion lorsque vous avez terminé
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


    // Initialisation du jeu
    private void initialiser() {
        // Remplissage de la grille du puit avec des couleurs
        puit = new Color[ConstantesTetris.LARGEUR_PUIT][ConstantesTetris.HAUTEUR_PUIT];
        for (int i = 0; i < ConstantesTetris.LARGEUR_PUIT; i++) {
            for (int j = 0; j < ConstantesTetris.HAUTEUR_PUIT; j++) {
                // Bords du puit en gris, intérieur en noir
                if (i == 0 || i == ConstantesTetris.LARGEUR_PUIT - 1 || j == ConstantesTetris.HAUTEUR_PUIT - 1) {
                    puit[i][j] = Color.GRAY;
                } else {
                    puit[i][j] = Color.BLACK;
                }
            }
        }
        nouvellePiece(); // Génération de la première pièce
    }

    // Génère une nouvelle pièce
    public void nouvellePiece() {
        // Positionne la pièce au centre en haut du puit
        originePiece = new Point(ConstantesTetris.LARGEUR_PUIT / 2, 2);
        rotation = 0; // Réinitialise la rotation
        // Si le sac est vide, le remplir avec les indices des pièces puis le mélanger
        if (sac.isEmpty()) {
            for (int i = 0; i < Tetrominos.length; i++) {
                sac.add(i);
            }
            Collections.shuffle(sac);
        }
        pieceCourante = sac.remove(0); // Prend la première pièce du sac
    }
    
    public void recevoirMisesAJourAutresJoueurs(String message) {
    // Traitez le message et mettez à jour les positions des autres joueurs
    if (message.startsWith("POSITIONS_AUTRES_JOUEURS")) {
        // Mettez à jour les positions des autres joueurs
        ArrayList<Point> nouvellesPositions = new ArrayList<>();
        String positionsString = message.split(" ")[1];
        String[] positionsArray = positionsString.substring(1, positionsString.length() - 1).split(",");
        for (String position : positionsArray) {
            String[] coordinates = position.trim().split(" ");
            nouvellesPositions.add(new Point(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1])));
        }
        mettreAJourPositionsAutresJoueurs(nouvellesPositions);
    }
}

    // Vérifie si la nouvelle position d'un tétraminos provoque une collision
    private boolean collision(int x, int y, int rotation) {
        // Parcourt les points du tétraminos
        for (Point p : Tetrominos[pieceCourante][rotation]) {
            int nouvelleX = originePiece.x + x + p.x;
            int nouvelleY = originePiece.y + y + p.y;

            // Vérifie les collisions avec les bords du puit
            if (nouvelleX < 0 || nouvelleX >= ConstantesTetris.LARGEUR_PUIT || nouvelleY >= ConstantesTetris.HAUTEUR_PUIT) {
                return true;
            }

            // Vérifie les collisions avec les tétraminos déjà figés dans le puit
            if (nouvelleY >= 0 && puit[nouvelleX][nouvelleY] != Color.BLACK) {
                return true;
            }
        }
        return false;
    }

    // Effectue la rotation d'un tétraminos
    public void rotationner(int i) {
        // Calcule la nouvelle rotation
        int nouvelleRotation = (rotation + i) % 4;
        if (nouvelleRotation < 0) {
            nouvelleRotation = 3;
        }
        // Si la rotation n'entraîne pas de collision, effectue la rotation
        if (!collision(0, 0, nouvelleRotation)) {
            rotation = nouvelleRotation;
        }
        repaint(); // Redessine le jeu
    }

    // Effectue le déplacement latéral d'un tétraminos
    public void deplacer(int i) {
        // Si le déplacement n'entraîne pas de collision, effectue le déplacement
        if (!collision(i, 0, rotation)) {
            originePiece.x += i;
        }
        repaint(); // Redessine le jeu
    }

    // Effectue la descente automatique d'un tétraminos
    public void descendre() {
        // Si la descente n'entraîne pas de collision, effectue la descente
        if (!collision(0, 1, rotation)) {
            originePiece.y += 1;
        } else {
            figerPiece(); // Figement du tétraminos dans le puit
        }
        repaint(); // Redessine le jeu
    }

    // "Figement" d'un tétraminos dans le puit
    public void figerPiece() {
        // Parcourt les points du tétraminos
        for (Point p : Tetrominos[pieceCourante][rotation]) {
            // Place la couleur du tétraminos dans le puit
            puit[originePiece.x + p.x][originePiece.y + p.y] = couleursTetromino[pieceCourante];
        }
        effacerLignes(); // Efface les lignes complètes et met à jour le score
        nouvellePiece(); // Génère une nouvelle pièce
        // Si la nouvelle pièce provoque une collision, la partie est terminée
        //eliminerLigne();
        if (collision(0, 0, 0)) {
            finDePartie(); // Gestion de la fin de partie
        }
    }

    // Supprime une ligne complète dans le puit
public void supprimerLigne(int ligne) {
    for (int j = ligne - 1; j > 0; j--) {
        for (int i = 1; i < ConstantesTetris.LARGEUR_PUIT - 1; i++) {
            puit[i][j + 1] = puit[i][j];
        }
    }
}

// Efface les lignes complètes et met à jour le score
public void effacerLignes() {
    boolean trou;
    int ligneEfface = 0;

    for (int j = ConstantesTetris.HAUTEUR_PUIT - 2; j > 0; j--) {
        trou = false;
        for (int i = 1; i < ConstantesTetris.LARGEUR_PUIT - 1; i++) {
            if (puit[i][j] == Color.BLACK) {
                trou = true;
                break;
            }
        }
        if (!trou) {
            supprimerLigne(j);
            j += 1;
            ligneEfface += 1;
        }
    }

    switch (ligneEfface) {
        case 1:
            score += 100;
            this.lignesEffacees += 1;
            break;
        case 2:
            score += 300;
            this.lignesEffacees += 2;
            break;
        case 3:
            score += 500;
            this.lignesEffacees += 3;
            break;
        case 4:
            score += 800;
            this.lignesEffacees += 4;
            break;
    }
}

    // Dessine le tétraminos en cours
    private void dessinerPiece(Graphics g) {
        // Parcourt les points du tétraminos
        for (Point p : Tetrominos[pieceCourante][rotation]) {
            // Dessine un rectangle de la couleur du tétraminos à la position spécifiée
            g.setColor(couleursTetromino[pieceCourante]);
            g.fillRect((p.x + originePiece.x) * ConstantesTetris.TAILLE_BLOC,
                    (p.y + originePiece.y) * ConstantesTetris.TAILLE_BLOC,
                    ConstantesTetris.TAILLE_BLOC - 1, ConstantesTetris.TAILLE_BLOC - 1);
        }
    }

    // Méthode appelée pour redessiner le composant
    @Override
    public void paintComponent(Graphics g) {
        // Dessine le fond du puit en gris foncé
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, ConstantesTetris.LARGEUR_PUIT * ConstantesTetris.TAILLE_BLOC,
                ConstantesTetris.HAUTEUR_PUIT * ConstantesTetris.TAILLE_BLOC);

        // Parcourt la grille du puit pour dessiner les tétraminos figés
        for (int i = 0; i < ConstantesTetris.LARGEUR_PUIT; i++) {
            for (int j = 0; j < ConstantesTetris.HAUTEUR_PUIT; j++) {
                g.setColor(puit[i][j]);
                g.fillRect(i * ConstantesTetris.TAILLE_BLOC, j * ConstantesTetris.TAILLE_BLOC,
                        ConstantesTetris.TAILLE_BLOC - 1, ConstantesTetris.TAILLE_BLOC - 1);
            }
        }
        
        // Dessine les tétraminos des autres joueurs
        for (Point position : positionsAutresJoueurs) {
            g.setColor(Color.GRAY);
            g.fillRect(position.x * ConstantesTetris.TAILLE_BLOC, position.y * ConstantesTetris.TAILLE_BLOC,
                    ConstantesTetris.TAILLE_BLOC - 1, ConstantesTetris.TAILLE_BLOC - 1);
        }

        // Affiche le score et le nombre de lignes effacées
        g.setColor(Color.WHITE);
        g.drawString("Points: " + lignesEffacees, (ConstantesTetris.LARGEUR_PUIT - 5) * ConstantesTetris.TAILLE_BLOC,
                25);
        g.drawString("Scores: " + score, (ConstantesTetris.LARGEUR_PUIT - 5) * ConstantesTetris.TAILLE_BLOC, 50);

        dessinerPiece(g); // Dessine le tétraminos en cours
    }

    public void mettreAJourPositionsAutresJoueurs(ArrayList<Point> nouvellesPositions) {
        this.positionsAutresJoueurs = nouvellesPositions;
        repaint(); // Redessine le jeu après chaque mise à jour des positions des autres joueurs
    }
    
    // Vérifie si la partie est terminée
    public boolean estFinDePartie() {
        return finDePartie;
    }

    // Gestion de la fin de partie
    public void finDePartie() {
        if (!finDePartie) {
            String message = "Partie terminée!\nNombre de lignes effacées: " + lignesEffacees + "\nScore: " + score;
            String titre = "Fin de partie";

            // Affiche une boîte de dialogue avec le message de fin de partie
            JOptionPane.showMessageDialog(this, message, titre, JOptionPane.INFORMATION_MESSAGE);

            System.exit(0); // Termine l'exécution du programme
        }
        finDePartie = true;
    }
}