package tetris;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class EcouteurClavierTetris implements KeyListener {
    private final JeuTetris jeu;

    public EcouteurClavierTetris(JeuTetris jeu) {
        this.jeu = jeu;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Implémentation si nécessaire
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                jeu.rotationner(-1);
                break;
            case KeyEvent.VK_DOWN:
                jeu.rotationner(1);
                break;
            case KeyEvent.VK_LEFT:
                jeu.deplacer(-1);
                break;
            case KeyEvent.VK_RIGHT:
                jeu.deplacer(1);
                break;
            case KeyEvent.VK_SPACE:
                jeu.descendre();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Implémentation si nécessaire
    }
}
