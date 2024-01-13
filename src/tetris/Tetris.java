package tetris;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import static tetris.ConstantesTetris.LARGEUR_FENETRE;
import static tetris.ConstantesTetris.HAUTEUR_FENETRE;

public class Tetris {

    public static void main(String[] args) {
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
}




