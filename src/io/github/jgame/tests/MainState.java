package io.github.jgame.tests;

import io.github.jgame.game.State;
import io.github.jgame.image.ImageManager;
import io.github.jgame.logging.GenericLogger;
import io.github.jgame.math.Vector2;
import io.github.jgame.mixer.SoundManager;
import io.github.jgame.sprite.Group;
import io.github.jgame.sprite.Sprite;
import io.github.jgame.sprite.TextSprite;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainState extends State {
    private final Logger logger;
    private final boolean displayRects = true;

    Sprite player;
    Group enemies;
    Group bullets;
    Shape testShape;

    Random rand = new Random();
    SoundManager soundManager = new SoundManager();
    ImageManager imageLoader = new ImageManager();

    public MainState() {
        super();
        GenericLogger.setup(Level.FINE, Level.FINE, Level.OFF);
        logger = Logger.getLogger(this.getClass().getName());
        logger.info("env: " + System.getenv());

        imageLoader.fromFile("assets/sprite.png", "sprite");
        imageLoader.fromFile("assets/bullet.png", "bullet");
        int size = 32;
        testShape = new Polygon(new int[]{0, size, 2 * size}, new int[]{0, 2 * size, 0}, 3);
        player = new TextSprite("HelloWorld!",
                new Font("Arial", Font.PLAIN, 50), Color.BLACK);
        enemies = new Group();
        bullets = new Group();
        enemies.add(new Enemy(640, 480, imageLoader.get("sprite")));
        soundManager.fromFile("assets/fireball.wav", "fireball");
    }

    @Override
    public MouseHandler getMouseHandler() {
        return new MouseHandler();
    }

    @Override
    public KeyHandler getKeyHandler() {
        return new KeyHandler();
    }

    @Override
    public void updateGraphics(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 1440, 900);
        g.setColor(Color.BLACK);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (displayRects) {
            player.blitRect(g2d);
        }
        player.blitRotozoom(player.pos.angleTo(mouseHandler.pos), new double[]{1, 1}, g2d);
        for (Sprite enemy : enemies.sprites) {
            enemy.update();
            if (displayRects) {
                enemy.blitRect(g2d);
            }
            enemy.blitRotozoom(enemy.pos.angleTo(player.pos), new double[]{1, 1}, g2d);
        }
        for (Object bullet : (LinkedList) bullets.sprites.clone()) {
            if (bullet instanceof BulletSprite) {
                BulletSprite bsprite = (BulletSprite) bullet;
                for (Sprite col : bsprite.collidesWith(enemies)) {
                    col.kill();
                    for (int i = 0; i < 2; i++) {
                        enemies.add(new Enemy(rand.nextInt(1440), rand.nextInt(900),
                                imageLoader.get("sprite")));
                    }
                }
                bsprite.update();
                if (displayRects) {
                    bsprite.blitRect(g2d);
                }
                bsprite.blit(g2d);
            }
        }
    }

    @Override
    public void updateLogic() {
        player.pos = player.pos.add(player.vel);
        player.updateRect();
    }

    public class KeyHandler extends State.KeyHandler {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case (KeyEvent.VK_UP): {
                    player.vel.y = -1;
                    break;
                }
                case (KeyEvent.VK_DOWN): {
                    player.vel.y = 1;
                    break;
                }
                case (KeyEvent.VK_LEFT): {
                    player.vel.x = -1;
                    break;
                }
                case (KeyEvent.VK_RIGHT): {
                    player.vel.x = 1;
                    break;
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            switch (e.getKeyCode()) {
                case (KeyEvent.VK_UP):
                case (KeyEvent.VK_DOWN): {
                    player.vel.y = 0;
                    break;
                }
                case (KeyEvent.VK_LEFT):
                case (KeyEvent.VK_RIGHT): {
                    player.vel.x = 0;
                    break;
                }
            }
        }
    }

    public class MouseHandler extends State.MouseHandler {
        Vector2 pos = new Vector2(0, 0);

        @Override
        public void mouseMoved(MouseEvent e) {
            mouseHandler.pos.x = e.getX();
            mouseHandler.pos.y = e.getY();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            bullets.add(new BulletSprite(mouseHandler.pos, player.pos, imageLoader.get("bullet")));
            soundManager.play("fireball", true, 0f);
        }
    }
}
