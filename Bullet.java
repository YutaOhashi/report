import java.awt.*;

public class Bullet {
    private int x, y;
    private final int speed = 8;
    private final int size = 10;

    public Bullet(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        y -= speed;
    }
    
    public void draw(Graphics g) {
        g.setColor(Color.CYAN);
        g.fillOval(x - size/2, y - size/2, size, size);
    }

    public int getX() { return x; }
    public int getY() { return y; }
}