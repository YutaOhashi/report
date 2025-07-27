import java.awt.*;
import java.awt.image.BufferedImage;

public class Player {
    private int x, y;
    private final int size = 50;
    private BufferedImage image;
    private boolean isExploding = false;
    private long explosionStartTime;
    private static final long EXPLOSION_DURATION = 100; // 2秒
    private BufferedImage explosionImage; // 爆発画像

    // 点滅用フラグ・タイマー
    private boolean isBlinking = false;
    private int blinkTimer = 0;  // 点滅残りフレーム数など

    // 点滅開始用メソッド
    public void startBlinking(int durationFrames) {
        isBlinking = true;
        blinkTimer = durationFrames;
    }

    // update用（GamePanel側で呼ぶ）
    public void update() {
        if (isBlinking) {
            blinkTimer--;
            if (blinkTimer <= 0) {
                isBlinking = false;
            }
        }

        if (isExploding && System.currentTimeMillis() - explosionStartTime >= EXPLOSION_DURATION) {
            isExploding = false; // 爆発が終わったら状態をリセット（または削除待ち）
        }
    }

    public Player(int startX, int startY, BufferedImage image) {
        x = startX;
        y = startY;
        this.image = image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public void move(int mouseX, int mouseY) {
        x = mouseX;
        y = mouseY;
    }

    public Bullet shoot() {
        return new Bullet(x, y - size/2);
    }

    public void draw(Graphics2D g, boolean isCooldown) {
        // 爆発中なら爆発画像を表示して終了
        if (isExploding && explosionImage != null) {
            g.drawImage(explosionImage, x - size / 2, y - size / 2, size, size, null);
            return;
        }

        // 点滅中は描画/非描画を繰り返す（例：5フレームごとにON/OFF）
        if (isBlinking && (blinkTimer / 5) % 2 == 0) {
            // 何も描画しない → 点滅で見えなくする
            return;
        }

        if (image != null) {
            g.drawImage(image, x - size / 2, y - size / 2, size, size, null);
        } else {
            g.setColor(isCooldown ? Color.GREEN : Color.WHITE);
            g.fillOval(x - size/2, y - size/2, size, size);
        }
    }

    public boolean isHitBy(EnemyBullet eb) {
        int dx = x - eb.getX();
        int dy = y - eb.getY();
        int distanceSq = dx*dx + dy*dy;
        int radius = size / 2;
        return distanceSq <= radius*radius;
    }

    public void setExplosionImage(BufferedImage image) {
        this.explosionImage = image;
    }

    public void startExplosion() {
        isExploding = true;
        isBlinking = false; // 点滅やめる
        image = explosionImage;
    }

    public boolean isExploding() {
        return isExploding;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSize() {
        return size;
    }
}