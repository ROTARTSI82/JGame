package io.github.jgame.sprite;

import io.github.jgame.math.Vector2;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.LinkedList;

public class Sprite {
    public Rectangle rect;
    public Image image;
    public Vector2 pos;
    public Vector2 absPos;
    public Vector2 size;
    public Vector2 vel;
    LinkedList<Group> groups = new LinkedList<>();

    public Sprite(Image spriteImage) {
        image = spriteImage;
        size = new Vector2(image.getWidth(null), image.getHeight(null));
        rect = new Rectangle(100, 100, (int) size.x, (int) size.y);
        pos = new Vector2(100, 100);
        absPos = pos.subtract(new Vector2(size.x / 2, size.y / 2));
        vel = new Vector2(0, 0);
    }

    public void blit(Graphics2D screen) {
    }

    public void updateRect() {
        absPos = pos.subtract(new Vector2(size.x / 2, size.y / 2));
        rect = new Rectangle((int) absPos.x, (int) absPos.y, (int) size.x, (int) size.y);
    }

    public void blitRotozoom(double rot, double[] zoom, Graphics2D screen) {
        AffineTransform id = AffineTransform.getTranslateInstance(this.absPos.x, this.absPos.y);
        AffineTransform trans = AffineTransform.getTranslateInstance(this.absPos.x, this.absPos.y);
        trans.setTransform(id);
        Vector2 center = new Vector2(this.image.getWidth(null) / 2d,
                this.image.getHeight(null) / 2d);

        trans.translate(center.x, center.y);
        trans.rotate(Math.toRadians(rot));
        trans.scale(zoom[0], zoom[1]);
        trans.translate(-center.x, -center.y);
        size.x = center.x * 2 * zoom[0];
        size.y = center.y * 2 * zoom[1];
        updateRect();
        screen.drawImage(this.image, trans, null);
    }

    public void blitRotozoomto(double rot, double[] zoom, Graphics2D screen) {
        AffineTransform id = AffineTransform.getTranslateInstance(this.absPos.x, this.absPos.y);
        AffineTransform trans = AffineTransform.getTranslateInstance(this.absPos.x, this.absPos.y);
        trans.setTransform(id);
        Vector2 center = new Vector2(this.image.getWidth(null) / 2d,
                this.image.getHeight(null) / 2d);
        trans.translate(center.x, center.y);
        trans.rotate(Math.toRadians(rot));
        trans.setToScale(zoom[0], zoom[1]);
        trans.translate(-center.x, -center.y);  // anchor
        size.x = zoom[0];
        size.y = zoom[1];
        updateRect();
        screen.drawImage(this.image, trans, null);
    }

    public void blitRect(Graphics2D screen) {
        screen.drawRect(rect.x, rect.y, rect.width, rect.height);
    }

    public LinkedList<Sprite> collidesWith(Group sprites) {
        LinkedList<Sprite> ret = new LinkedList<>();
        for (Sprite s : sprites.sprites) {
            if (s.rect.intersects(rect)) {
                ret.add(s);
            }
        }
        return ret;
    }

    public void update() {
    }

    public void kill() {
        for (Object g : (LinkedList) groups.clone()) {
            if (g instanceof Group) {
                Group g2 = (Group) g;
                g2.remove(this);
            }
        }
    }
}
