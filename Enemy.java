import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;

public class Enemy {
    protected int x, y;
    protected int dx = 2, dy = 2;
    protected final int size = 30;
    private int hp = 20;
    private boolean isLaserMode = false;
    protected BufferedImage image;
    private boolean isBlinking = false;
    private int blinkTimer = 0;
    private final int BLINK_DURATION = 30;
    private boolean isExploding = false;  // ← ここは1回だけ
    private long explosionStartTime = 0;
    private static BufferedImage explosionImage;
    private final long EXPLOSION_DURATION = 100;

    // コンストラクタ：画像付き
    public Enemy(int x, int y, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.image = image;
    }

    // 移動処理
    public void update() {
        if (!isLaserMode) {
            x += dx;
            y += dy;
        }

        if (isBlinking) {
            blinkTimer--;
            if (blinkTimer <= 0) {
                isBlinking = false;
            }
        }
    }

    public static void setExplosionImage(BufferedImage img) {
        explosionImage = img;
    }

    public void setLaserMode(boolean mode) {
        this.isLaserMode = mode;
    }

    // 弾発射
    public List<EnemyBullet> fireBullets() {
        List<EnemyBullet> bullets = new ArrayList<>();
        int speed = 4;
        int[] vx = {0, speed, speed, speed, 0, -speed, -speed, -speed};
        int[] vy = {-speed, -speed, 0, speed, speed, speed, 0, -speed};

        for (int i = 0; i < 8; i++) {
            bullets.add(new EnemyBullet(x, y, vx[i], vy[i]));
        }
        return bullets;
    }

    public List<EnemyBullet> fire16EnemyBullets() {
        List<EnemyBullet> bullets = new ArrayList<>();
        double angleStep = 2 * Math.PI / 16;
        int speed = 4;

        for (int i = 0; i < 16; i++) {
            double angle = i * angleStep;
            int dx = (int) Math.round(speed * Math.cos(angle));
            int dy = (int) Math.round(speed * Math.sin(angle));
            bullets.add(new EnemyBullet(x, y, dx, dy));
        }

        return bullets;
    }

    public List<LaserBeam> fireLaserBeams(double rotationAngle) {
        List<LaserBeam> lasers = new ArrayList<>();
        int groupId = (int) System.currentTimeMillis();

        for (int i = 0; i < 16; i++) {
            double angle = rotationAngle + i * (2 * Math.PI / 16);
            double dx = Math.cos(angle);
            double dy = Math.sin(angle);
            lasers.add(new LaserBeam(getX(), getY(), dx, dy, groupId));
        }

        return lasers;
    }

    // 描画処理（画像を描画）
    public void draw(Graphics g) {
        int drawWidth = 120;
        int drawHeight = 120;

        if (isExploding) {
            if (explosionImage != null) {
                g.drawImage(explosionImage, x - drawWidth / 2, y - drawHeight / 2, drawWidth, drawHeight, null);
            }
            return;
        }

        if (isBlinking && (blinkTimer / 5) % 2 == 0) {
            return; // 点滅中
        }

        if (image != null) {
            g.drawImage(image, x - drawWidth / 2, y - drawHeight / 2, drawWidth, drawHeight, null);
        } else {
            g.setColor(Color.GRAY);
            g.fillRect(x - size / 2, y - size / 2, size, size);
        }
    }

    public boolean isHitBy(Bullet b) {
        int dx = x - b.getX();
        int dy = y - b.getY();
        int radius = size / 2;
        return dx * dx + dy * dy <= radius * radius;
    }

    // 爆発中かどうかを返すメソッド
    public boolean isExploding() {
        if (isExploding) {
            return System.currentTimeMillis() - explosionStartTime < EXPLOSION_DURATION;
        }
        return false;
    }

    public void hit() {
        hp--;
        if (hp <= 0) {
            isExploding = true;
            explosionStartTime = System.currentTimeMillis();
        } else {
            isBlinking = true;
            blinkTimer = BLINK_DURATION;
        }
    }

    public boolean isDefeated() {
        if (isExploding) {
            return System.currentTimeMillis() - explosionStartTime >= EXPLOSION_DURATION;
        }
        return hp <= 0;
    }

    public int getHp() {
        return hp;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getDx() { return dx; }
    public int getDy() { return dy; }
    public void setDx(int dx) { this.dx = dx; }
    public void setDy(int dy) { this.dy = dy; }
}