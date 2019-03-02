package io.github.jgame.tests.window;

import io.github.jgame.math.Vector2;
import io.github.jgame.sprite.Sprite;

import java.awt.image.BufferedImage;

import static io.github.jgame.util.UniversalResources.rand;

/**
 * @deprecated Only use for blitting tests.
 */
@Deprecated
public class Enemy extends Sprite {

    public Enemy(int x, int y, BufferedImage img) {
        super(img);
        pos = new Vector2(x, y);
        updateRect();
    }

    @Override
    public void update() {
        vel = new Vector2(rand.nextInt(3) - 1, rand.nextInt(3) - 1);
        pos = pos.add(vel);
        updateRect();
    }
}