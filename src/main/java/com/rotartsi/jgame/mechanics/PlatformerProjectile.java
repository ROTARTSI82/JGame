package com.rotartsi.jgame.mechanics;

import com.rotartsi.jgame.game.Game;
import com.rotartsi.jgame.math.Vector2;

import java.awt.*;
import java.awt.image.BufferedImage;

import static com.rotartsi.jgame.Constants.rand;

/**
 * Projectiles. Support for despawning, blitting, updates, blume, etc
 */
public class PlatformerProjectile extends PlatformerEntity {
    /**
     * For how many milliseconds would the projectile last before it despawns?
     */
    public long projectileLife;

    /**
     * When was the projectile "born"? The difference between this and the current time would be used
     * to determine if the projectile should be despawned.
     */
    public long born;

    /**
     * The degree of random noise that should be added to the velocity every call to {@link #update()}
     */
    private Vector2 wobble;

    private Vector2 target;

    /**
     * Speed of the projectile.
     */
    private double speed;

    /**
     * The degree of random noice that would be added to the velocity every call to {@link #recalculate(boolean, boolean)}
     */
    private Vector2 blume;

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
    public PlatformerProjectile(BufferedImage img, Vector2 target, Vector2 position, double speed, long life, Vector2 blume, Game game) {
        super(img, game.runner.getBounds());
        pos = position;

        projectileLife = life;
        born = System.currentTimeMillis();
        this.blume = blume;

        rot = pos.angleTo(target);
        vel = pos.velocityTo(target, speed).add(
                new Vector2((rand.nextBoolean() ? 1 : -1) * (rand.nextDouble() * blume.x),
                        (rand.nextBoolean() ? 1 : -1) * (rand.nextDouble() * blume.y)));
        this.speed = speed;
        this.target = target;
        wobble = new Vector2(0, 0);
    }

    /**
     * Set the attributes of the projectile. Call {@link #recalculate(boolean, boolean)} to update the
     * Velocity and rotation.
     *
     * @param target Target pos
     * @param wobble Wobble (added every frame)
     * @param speed speed
     * @param blume Blume (added on {@link #recalculate(boolean, boolean)}
     */
    public void setAttributes(Vector2 target, Vector2 wobble, double speed, Vector2 blume) {
        this.target = target;
        this.wobble = wobble;
        this.speed = speed;
        this.blume = blume;
    }

    /**
     * Called when the PlatformerProjectile needs to be killed, such as when it hits a wall.
     *
     * Override this function if the projectile could only be killed in certain conditions.
     */
    public void requestKill() {
        kill();
    }

    /**
     * Recalculate position and/or velocity.
     * (NOTE: Blume would be added to the velocity. Use {@link #setAttributes(Vector2, Vector2, double, Vector2)})
     * And set blume to Vector2[0, 0])
     *
     * @param rotation Should recalculate rot
     * @param velocity Should recalculate vel
     */
    public void recalculate(boolean rotation, boolean velocity) {
        if (rotation) {
            rot = pos.angleTo(this.target);
        }
        if (velocity) {
            vel = pos.velocityTo(this.target, this.speed).add(
                    new Vector2((rand.nextBoolean() ? 1 : -1) * (rand.nextDouble() * this.blume.x),
                            (rand.nextBoolean() ? 1 : -1) * (rand.nextDouble() * this.blume.y)));
        }
    }

    /**
     * Accelerate towards the specified point. Negative speed will force it to accelerate in the opposite direction.
     *
     * @param target Target pos
     * @param speed  speed (can be negative)
     * @param blume  How accurately to accelerate towards the target. {@code Vector2(0, 0)} means 100% accuracy,
     *               while {@code Vector2(1, 1)} means the velocity is accurate up to 1 unit.
     */
    public void accelerateTowards(Vector2 target, double speed, Vector2 blume) {
        Vector2 accel = pos.velocityTo(target, speed).add(new Vector2((rand.nextBoolean() ? 1 : -1) *
                (rand.nextDouble() * blume.x), (rand.nextBoolean() ? 1 : -1) * (rand.nextDouble() * blume.y)));
        vel = vel.add(accel);
    }

    /**
     * Add the wobble to the velocity, and add the velocity to the position.
     *
     * Automatically despawns projectile after {@code projectileLife} milliseconds have passed since the
     * constructor was called.
     */
    @Override
    public void update() {
        super.update();

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