import java.awt.*;

public class EnemyBullet {
    private int x, y;
    private int dx, dy;
    private final int size = 10;

    public EnemyBullet(int x, int y, int dx, int dy) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
    }

    public void update() {
        x += dx;
        y += dy;
    }

    public void draw(Graphics g) {
        g.setColor(Color.YELLOW);
        g.drawRect(x - size/2, y - size/2, size, size);
    }

    public int getX() { return x; }
    public int getY() { return y; }
}