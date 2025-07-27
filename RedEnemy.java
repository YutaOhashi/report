import java.awt.*;
import java.awt.image.BufferedImage;

public class RedEnemy extends Enemy {
    public RedEnemy(int x, int y, BufferedImage image) {
        super(x, y, image);
    }

    public void draw(Graphics g) {
        if (image != null) {
            super.draw(g);
        } else {
            g.setColor(Color.RED);
            g.fillRect(x - size / 2, y - size / 2, size, size);
        }
    }

    public boolean isHitBy(Bullet b) {
        return false; // 当たっても無敵
    }
}