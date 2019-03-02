package io.github.jgame.game;

import javax.swing.*;
import java.awt.*;

/**
 * Template for the main program.
 * <p>
 * Extension of {@link javax.swing.JFrame}
 */
public class GameRunner extends JFrame {
    public Game game;

    /**
     * Run the game instance!
     *
     * @param gameInst Game instance to run. See {@link Game}
     */
    public GameRunner(Game gameInst) {
        game = gameInst;
        game.setParent(this);
        add(game);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setResizable(true);
        setIconImage(new ImageIcon("dat/sprite.png").getImage());
        setSize(640, 480);
    }

    /**
     * Add our game's main loop to the AWT event loop.
     *
     * @param args Ignored. Doesn't matter.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            GameRunner app = new GameRunner(new Game());
            app.game.run();
            app.setVisible(true);
        });
    }
}