package io.github.jgame.mechanics;

import io.github.jgame.math.Vector2;
import io.github.jgame.sprite.Sprite;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Projectiles. Support for despawning, blitting, updates, blume, etc
 */
public class Projectile extends Sprite {
    public Vector2 wobble;
    private Vector2 myTarget;
    private double mySpeed;
    private Random rand;

    private int projectileLife;
    private long born;
    private Vector2 myBlume;

    /**
     * Projectiles!
     *
     * @param img      Image to blit for projectile
     * @param target   Target position
     * @param position current position
     * @param speed    Speed
     * @param life     Life in milliseconds until projectile despawns
     * @param blume    Max offset of initial velocity
     */
    public Projectile(BufferedImage img, Vector2 target, Vector2 position, double speed, int life, Vector2 blume) {
        super(img);
        pos = position;

        projectileLife = life;
        born = System.currentTimeMillis();
        rand = new Random();
        myBlume = blume;

        rot = pos.angleTo(target);
        vel = pos.velocityTo(target, speed).add(
                new Vector2((rand.nextBoolean() ? 1 : -1) * (rand.nextDouble() * blume.x),
                        (rand.nextBoolean() ? 1 : -1) * (rand.nextDouble() * blume.y)));
        myTarget = target;
        mySpeed = speed;
        setWobble(0, 0);
    }

    /**
     * Set the wobble (which is added directly to velocity every frame)
     *
     * @param x Max x offset
     * @param y Max y offset
     */
    public void setWobble(double x, double y) {
        wobble = new Vector2(x, y);
    }

    /**
     * Set the blume (which is added to the velocity every time {@link #recalculate(boolean, boolean) is called}
     *
     * @param x Max x offset
     * @param y Max y offset
     */
    public void setBlume(double x, double y) {
        myBlume = new Vector2(x, y);
    }

    /**
     * Called when the Projectile needs to be killed, such as when it hits a wall.
     *
     * Override this function if the projectile could only be killed in certain conditions.
     */
    public void requestKill() {
        kill();
    }

    /**
     * Recalculate position and/or velocity.
     * (NOTE: Blume would be added to the velocity. To disable this, {@link #setBlume(double, double)} to zero.)
     *
     * @param rotation Should recalculate rot
     * @param velocity Should recalculate vel
     */
    public void recalculate(boolean rotation, boolean velocity) {
        if (rotation) {
            rot = pos.angleTo(myTarget);
        }
        if (velocity) {
            vel = pos.velocityTo(myTarget, mySpeed).add(
                    new Vector2((rand.nextBoolean() ? 1 : -1) * (rand.nextDouble() * myBlume.x),
                            (rand.nextBoolean() ? 1 : -1) * (rand.nextDouble() * myBlume.y)));
        }
    }

    /**
     * Add the wobble to the velocity, and add the velocity to the position.
     *
     * Automatically despawns projectile after {@code projectileLife} milliseconds have passed since the
     * constructor was called.
     */
    @Override
    public void update() {
        vel = vel.add(new Vector2((rand.nextBoolean() ? 1 : -1) * (rand.nextDouble() * wobble.x),
                (rand.nextBoolean() ? 1 : -1) * (rand.nextDouble() * wobble.y)));
        pos = pos.add(vel);
        updateRect();

        if (System.currentTimeMillis() - born >= projectileLife) {
            requestKill();
        }
    }

    /**
     * Draw to screen
     *
     * @param screen Graphics to draw to.
     */
    @Override
    public void blit(Graphics2D screen) {
        blitTo(screen);
    }

}
