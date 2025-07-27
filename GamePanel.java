import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseMotionListener {
    protected Timer timer;
    protected Player player;
    protected List<Enemy> enemies;
    protected List<Bullet> playerBullets;
    protected List<EnemyBullet> enemyBullets;
    protected GameWindow parent;
    protected int timeElapsed;  // ミリ秒
    private int enemyHitCount;
    protected int playerLife;
    protected int enemyFireInterval = 1000; // 敵弾発射間隔（ms）
    protected int enemyFireTimer;
    private java.util.List<Long> shotTimes = new ArrayList<>(); // 弾を撃った時刻（ミリ秒）
    private boolean isCooldown = false;  // クールダウン中フラグ
    private long cooldownStartTime = 0L; // クールダウン開始時刻
    private final int COOLDOWN_DURATION = 1500; // クールダウン時間（ミリ秒）
    private final int MAX_SHOTS_PER_SECOND = 4; // 1秒あたりの最大発射数
    private boolean spaceKeyDown = false;
    // フィールドに追加（1回だけ発動させるため）
    private boolean fired16FromEnemy = false;
    // モード管理用
    private boolean isLaserMode = false;   // 現在レーザーモードかどうか
    private long modeSwitchTimer = 0L;     // モード切替用タイマー（ミリ秒）
    private final long MODE_SWITCH_INTERVAL = 5000L;  // 5秒ごとに切り替え
    private double laserRotationAngle = 0.0;  // レーザービームの回転角度（ラジアン）
    private long lastLaserDamageTime = 0;
    boolean touchingLaser = false;
    private BufferedImage backgroundImage;
    protected BufferedImage enemyImage;  // ← フィールドに追加
    protected BufferedImage redenemyImage; // フィールド
    protected BufferedImage spaceshipImage; 
    private BufferedImage coolSpaceshipImage;
    private long lastLaserStartTime = 0;  // 最後にレーザーを発射した時間
    private List<LaserBeam> activeLasers = new ArrayList<>();
    private BufferedImage explosionImage; 

    public GamePanel(GameWindow parent) {
        this.parent = parent;
        setFocusable(true);
        addKeyListener(this);
        addMouseMotionListener(this);

        enemies = new ArrayList<>();
        playerBullets = new ArrayList<>();
        enemyBullets = new ArrayList<>();

        timer = new Timer(16, this);  // 約60FPS

        try {
            backgroundImage = ImageIO.read(getClass().getResource("/resources/space.jpg"));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("背景画像の読み込みに失敗しました: " + e.getMessage());
            backgroundImage = null;
        }

        try {
            spaceshipImage = ImageIO.read(getClass().getResource("/resources/spaceship.png"));
        } catch (IOException e) {
            System.err.println("宇宙船画像の読み込みに失敗しました: " + e.getMessage());
        }

        try {
            enemyImage = ImageIO.read(getClass().getResource("/resources/enemy_ship.png"));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("敵画像の読み込みに失敗しました: " + e.getMessage());    
        }

        try {
            redenemyImage = ImageIO.read(getClass().getResource("/resources/redenemy_ship.png"));
            System.out.println("redenemyImage: " + redenemyImage);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("RedEnemy画像の読み込みに失敗しました: " + e.getMessage());
        }

        try {
            coolSpaceshipImage = ImageIO.read(getClass().getResource("/resources/cool_spaceship.png"));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("クールダウン中の宇宙船画像の読み込みに失敗しました: " + e.getMessage());
        }

        try {
            explosionImage = ImageIO.read(getClass().getResource("/resources/explosion.png"));
            Enemy.setExplosionImage(explosionImage);
        } catch (IOException e) {
            System.err.println("爆発画像の読み込みに失敗しました: " + e.getMessage());
        }

        player = new Player(400, 500, spaceshipImage);
        player.setExplosionImage(explosionImage);  // フィールドの explosionImage を使うので問題なし
        enemies.add(new Enemy(300, 100, enemyImage));
        enemies.add(new RedEnemy(200, 150, redenemyImage));
    }

    public void startGame() {
        timeElapsed = 0;
        enemyHitCount = 0;
        playerLife = 10;
        enemyFireTimer = 0;
        enemies.clear();
        playerBullets.clear();
        enemyBullets.clear();

        enemies.add(new Enemy(400, 300, enemyImage));

        timer.start();
    }

    
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
    }

    private void updateGame() {
        timeElapsed += 16;
        enemyFireTimer += 16;

        Enemy mainEnemy = enemies.stream()
            .filter(e -> !(e instanceof RedEnemy))
            .findFirst()
            .orElse(null);

        if (mainEnemy == null) return;

        if (isCooldown && System.currentTimeMillis() - cooldownStartTime >= COOLDOWN_DURATION) {
            isCooldown = false;
            shotTimes.clear();
        }

        if (isCooldown) {
            player.setImage(coolSpaceshipImage);
        } else {
            player.setImage(spaceshipImage);
        }

        // プレイヤー弾の移動と画面外削除
        playerBullets.removeIf(bullet -> {
            bullet.update();
            return bullet.getY() < 0;
        });

        // 敵弾の移動と画面外削除
        enemyBullets.removeIf(bullet -> {
            bullet.update();

            // LaserBeamなら寿命チェック
            if (bullet instanceof LaserBeam) {
                LaserBeam laser = (LaserBeam) bullet;
                if (laser.isExpired()) {
                    return true;  // 寿命切れなら削除
                }
            }

            return bullet.getY() > getHeight() || bullet.getX() < 0 || bullet.getX() > getWidth();
        });

        // 敵の移動
        for (Enemy enemy : enemies) {
            if (enemy.isExploding()) {
                continue; // 爆発中は update も移動もさせない
            }

            enemy.update();

            // 壁で反射
            if (enemy.getX() < 15) enemy.setDx(Math.abs(enemy.getDx()));
            if (enemy.getX() > getWidth() - 15) enemy.setDx(-Math.abs(enemy.getDx()));
            if (enemy.getY() < 15) enemy.setDy(Math.abs(enemy.getDy()));
            if (enemy.getY() > getHeight() - 15) enemy.setDy(-Math.abs(enemy.getDy()));
        }

        // 爆発終了した敵をリストから削除
        enemies.removeIf(enemy -> enemy.isDefeated());

        // 弾発射処理
        if (mainEnemy.getHp() > 10) {
            // 通常モード（8方向弾）
            if (enemyFireTimer >= enemyFireInterval) {
                enemyFireTimer = 0;
                for (Enemy enemy : enemies) {
                    enemyBullets.addAll(enemy.fireBullets());
                }
            }
        } else {
            // 特殊モード（HP <= 10）→ 5秒ごとに通常弾・レーザーを切り替え
            enemies.removeIf(e -> e instanceof RedEnemy);  // RedEnemy は削除

            long now = System.currentTimeMillis();

            if (modeSwitchTimer == 0L) {
                modeSwitchTimer = now;
            }

            if (now - modeSwitchTimer >= MODE_SWITCH_INTERVAL) {
                modeSwitchTimer = now;
                isLaserMode = !isLaserMode;
            }

            if (enemyFireTimer >= enemyFireInterval) {
                enemyFireTimer = 0;

                if (isLaserMode) {
                    laserRotationAngle += Math.toRadians(10);
                    laserRotationAngle %= 2 * Math.PI;

                } else {
                    enemyBullets.addAll(mainEnemy.fire16EnemyBullets());
                }
            }
        }

        // 弾が敵に当たる
        List<Bullet> bulletsToRemove = new ArrayList<>();

        for (Bullet pb : playerBullets) {
            for (Enemy enemy : enemies) {
                if (enemy.isHitBy(pb)) {
                    if (!(enemy instanceof RedEnemy)) {
                        int beforeHp = enemy.getHp();
                        enemy.hit();
                        if (enemy.getHp() < beforeHp) {
                            enemyHitCount++; // ダメージが通ったときのみ加算
                        }
                    }
                    bulletsToRemove.add(pb);
                }
            }
        }

        playerBullets.removeAll(bulletsToRemove);

        // 敵弾がプレイヤーに当たる
        List<EnemyBullet> enemyBulletsToRemove = new ArrayList<>();
        Integer laserGroupToRemove = null;

        for (EnemyBullet eb : enemyBullets) {
            boolean hit = false;

            if (eb instanceof LaserBeam) {
                LaserBeam laser = (LaserBeam) eb;
                hit = laser.isHitPlayer(player.getX(), player.getY(), 10);

                if (hit) {
                    playerLife = 0;
                    player.startBlinking(60); // 例：60フレーム（1秒）点滅
                }
            } else {
                hit = player.isHitBy(eb);
                if (hit) {
                    enemyBulletsToRemove.add(eb);
                    playerLife--;
                    player.startBlinking(60); // 点滅開始
                }
            }

            if (playerLife <= 0) {
                player.startExplosion();  // 爆発開始

                timer.stop();
                repaint();  // 体力0を描画
                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(() ->
                            parent.showGameOver("Game Over: You were defeated!")
                        );
                    }
                }, 100); // 0.1秒後に表示
                return;
            }
        }

        // 10秒経過したレーザーも削除
        for (EnemyBullet eb : enemyBullets) {
            if (eb instanceof LaserBeam && ((LaserBeam) eb).isExpired()) {
                enemyBulletsToRemove.add(eb);
            }
        }

        enemyBullets.removeAll(enemyBulletsToRemove);

        // Enemyが5回ヒットされたらRedEnemyを追加（1回のみ）
        if (enemyHitCount >= 5 && enemies.stream().noneMatch(e -> e instanceof RedEnemy)) {
            enemies.add(new RedEnemy(400, 200, redenemyImage));
        }

        // hp が10以下の間はRedEnemyを削除し続ける
        if (mainEnemy.getHp() <= 10) {
            enemies.removeIf(e -> e instanceof RedEnemy);

            long now = System.currentTimeMillis();

            if (modeSwitchTimer == 0L) {
                modeSwitchTimer = now;
            }

            if (now - modeSwitchTimer >= MODE_SWITCH_INTERVAL) {
                modeSwitchTimer = now;
                isLaserMode = !isLaserMode;
                activeLasers.clear();
            }

            // ここだけでfireLaserBeams()を呼ぶ】
            if (isLaserMode && (activeLasers.isEmpty() || now - lastLaserStartTime >= 5000)) {
                activeLasers.clear();
                laserRotationAngle += Math.toRadians(10);
                laserRotationAngle %= 2 * Math.PI;

                activeLasers.addAll(mainEnemy.fireLaserBeams(laserRotationAngle));
                System.out.println("Laser beams generated: " + activeLasers.size());
                lastLaserStartTime = now;
            }

            // 毎フレームレーザーをenemyBulletsに入れ直す
            if (isLaserMode) {
                enemyBullets.removeIf(b -> b instanceof LaserBeam);
                enemyBullets.addAll(activeLasers);
            }

            mainEnemy.setLaserMode(isLaserMode);
        }

        // 時間切れ
        if (timeElapsed >= 60000) {
            timer.stop();
            parent.showGameOver("Game Over: Time Over!");
            return;
        }

        // クリア判定は爆発中の敵を除いて判定
        boolean allDefeated = enemies.stream()
            .filter(e -> !(e instanceof RedEnemy))
            .allMatch(e -> e.isDefeated());

        if (allDefeated) {
            timer.stop();
            parent.showGameOver("Game Clear! Congratulations!");
            return;
        }

        player.update();
    }

    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;  // ここでキャスト

        // 背景
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        // プレイヤー
        BufferedImage currentPlayerImage = isCooldown ? coolSpaceshipImage : spaceshipImage;

        player.draw(g2d, isCooldown);

        // 敵
        for (Enemy enemy : enemies) enemy.draw(g);

        // 弾
        for (Bullet b : playerBullets) b.draw(g);
        for (EnemyBullet eb : enemyBullets) eb.draw(g);

        // UI
        g.setColor(Color.WHITE);
        g.drawString("Time: " + (60 - timeElapsed/1000) + "s", 10, 20);
        g.drawString("Player Life: " + playerLife, 10, 40);
        Enemy mainEnemy = enemies.stream()
            .filter(e -> !(e instanceof RedEnemy))
            .findFirst().orElse(null);

        if (mainEnemy != null) {
            g.drawString("Enemy HP: " + mainEnemy.getHp() + " / 20", 10, 60);
        }
    }

    // キー操作    
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!spaceKeyDown) {  // 初めて押されたときだけ反応
                spaceKeyDown = true;

                long now = System.currentTimeMillis();

                // クールダウン解除判定
                if (isCooldown && now - cooldownStartTime >= COOLDOWN_DURATION) {
                    isCooldown = false;
                    shotTimes.clear();
                }

                if (!isCooldown) {
                    shotTimes.removeIf(time -> now - time > 1000);

                    if (shotTimes.size() >= MAX_SHOTS_PER_SECOND) {
                        isCooldown = true;
                        cooldownStartTime = now;
                        System.out.println("クールダウン開始: 1.5秒間撃てません");
                    } else {
                        Bullet newBullet = player.shoot();
                        playerBullets.add(newBullet);
                        shotTimes.add(now);
                    }
                }
            }
        }
    }

    
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            spaceKeyDown = false;
        }
    }

    public void keyTyped(KeyEvent e) {}

    // マウス移動
    public void mouseMoved(MouseEvent e) {
        player.move(e.getX(), e.getY());
    }
    public void mouseDragged(MouseEvent e) {}
}