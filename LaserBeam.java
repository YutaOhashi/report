import java.awt.*;

public class LaserBeam extends EnemyBullet {
    private double dx, dy;
    private long createTime;
    private final long lifespan = 5000;
    private int groupId;

    public LaserBeam(int x, int y, double dx, double dy, int groupId) {
        super(x, y, 0, 0);
        this.dx = dx;
        this.dy = dy;
        this.groupId = groupId;
        this.createTime = System.currentTimeMillis();
    }

    public void update() {
        rotate(Math.toRadians(0.2));  // 毎フレーム1度回転（調整可能）
    }

    public void rotate(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double newDx = dx * cos - dy * sin;
        double newDy = dx * sin + dy * cos;
        dx = newDx;
        dy = newDy;
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(3));

        int x1 = getX();
        int y1 = getY();
        int length = 2000;

        int x2 = (int) (x1 + dx * length);
        int y2 = (int) (y1 + dy * length);

        g2.drawLine(x1, y1, x2, y2);
    }

    public boolean isHitPlayer(int px, int py, int radius) {
        int x1 = getX();
        int y1 = getY();
        int x2 = (int) (x1 + dx * 2000);
        int y2 = (int) (y1 + dy * 2000);

        double dist = pointToSegmentDistance(px, py, x1, y1, x2, y2);
        return dist <= radius;
    }

    private double pointToSegmentDistance(int px, int py, int x1, int y1, int x2, int y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;

        if (dx == 0 && dy == 0) {
            dx = px - x1;
            dy = py - y1;
            return Math.sqrt(dx * dx + dy * dy);
        }

        double t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));
        double projX = x1 + t * dx;
        double projY = y1 + t * dy;
        return Math.hypot(projX - px, projY - py);
    }

    public int getGroupId() {
        return groupId;
    }

    // ここを追加！
    public boolean isExpired() {
        return System.currentTimeMillis() - createTime > lifespan;
    }
}