import java.util.Iterator;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import java.awt.Graphics2D;

public class DodgeGamePanel extends GamePanel {
    private int survivalTime;
    private java.util.List<LaserBeam> laserBeams = new ArrayList<>();
    private boolean laserActive = false;
    private boolean isLaserMode = false;
    private int modeSwitchTimer = 0;    
    private final int MODE_SWITCH_INTERVAL = 5000; // 5秒ごとにモード切り替え


    public DodgeGamePanel(GameWindow parent) {
        super(parent);
    }

    protected void dodgeUpdateGame() {
        timeElapsed += 16;
        enemyFireTimer += 16;

        if (player != null) {
            player.update();
        }

        // 敵の移動（RedEnemyは40秒で消えるので、その後enemiesからいなくなる）
        for (Enemy enemy : enemies) {
            if (!(laserActive && isLaserMode)) {
               enemy.update();
            }
            if (enemy.getX() < 15) enemy.setDx(Math.abs(enemy.getDx()));
            if (enemy.getX() > getWidth() - 15) enemy.setDx(-Math.abs(enemy.getDx()));
            if (enemy.getY() < 15) enemy.setDy(Math.abs(enemy.getDy()));
            if (enemy.getY() > getHeight() - 15) enemy.setDy(-Math.abs(enemy.getDy()));
        }

        // 弾の移動・削除
        enemyBullets.removeIf(b -> {
            b.update();
            return b.getY() > getHeight() || b.getX() < 0 || b.getX() > getWidth();
        });

        // レーザーの更新（40秒以降）
        if (laserActive) {
            // モード切り替えタイマー更新
            modeSwitchTimer += 16;
            if (modeSwitchTimer >= MODE_SWITCH_INTERVAL) {
                modeSwitchTimer = 0;
                isLaserMode = !isLaserMode;

                if (isLaserMode) {
                    fireLaserBeams(); // レーザー再生成
                } else {
                    laserBeams.clear(); // レーザーを一時停止
                }
            }

            // レーザーの位置更新（現在のビームだけ）
            Iterator<LaserBeam> it = laserBeams.iterator();
            while (it.hasNext()) {
                LaserBeam lb = it.next();
                lb.update();
                if (lb.isExpired()) {
                    it.remove();
                }
            }
        }

        // ===== 敵の弾発射 =====
        if ((!laserActive || !isLaserMode) && enemyFireTimer >= enemyFireInterval) {
            enemyFireTimer = 0;
            for (Enemy enemy : enemies) {
                enemyBullets.addAll(enemy.fireBullets());
            }
        }

        // 被弾チェック（敵弾＋レーザー）
        ArrayList<EnemyBullet> toRemove = new ArrayList<>();
        for (EnemyBullet eb : enemyBullets) {
            if (player.isHitBy(eb)) {
                playerLife--;
                player.startBlinking(60);
                toRemove.add(eb);
                if (playerLife <= 0 && !player.isExploding()) {
                    playerLife = 0;
                    player.startExplosion();

                    timer.stop();
                    repaint();
                    new java.util.Timer().schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            SwingUtilities.invokeLater(() -> 
                                parent.showGameOver("Game Over: You were hit!")
                            );
                        }
                    }, 100); // 0.1秒後にゲームオーバー表示
                    return;
                }
            }
        }
        enemyBullets.removeAll(toRemove);

        // レーザーに被弾判定を追加（GamePanel と同じ方式）
        if (laserActive && isLaserMode) {
            for (LaserBeam lb : laserBeams) {
                boolean hit = lb.isHitPlayer(player.getX(), player.getY(), 10); // 半径10でヒット判定
                if (hit) {
                    playerLife = 0;
                    player.startBlinking(60);  // 点滅（演出）
                    player.startExplosion();   // 爆発（演出）

                    timer.stop();
                    repaint();
                    new java.util.Timer().schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            SwingUtilities.invokeLater(() ->
                                parent.showGameOver("Game Over: You were hit by Laser!")
                            );
                        }
                    }, 100); // 0.1秒後に表示
                    return;
                }
            }
        }

        // 20秒後 RedEnemy登場
        if (timeElapsed >= 20000 && timeElapsed < 40000 && enemies.stream().noneMatch(e -> e instanceof RedEnemy)) {
            enemies.add(new RedEnemy(400, 200, redenemyImage));
        }

        // 40秒経過時の処理：RedEnemy削除＋レーザー開始
        if (timeElapsed >= 40000 && !laserActive) {
            System.out.println("40秒経過: RedEnemy削除開始");

            System.out.println("削除前 enemiesリスト:");
            for (Enemy e : enemies) {
                System.out.println(e + " instanceof RedEnemy? " + (e instanceof RedEnemy));
            }

            int before = enemies.size();
            enemies.removeIf(e -> {
                boolean isRed = e instanceof RedEnemy;
                if (isRed) System.out.println("RedEnemyを削除: " + e);
                return isRed;
            });
            int after = enemies.size();
            System.out.println("削除前: " + before + ", 削除後: " + after);

            // レーザー関連初期化
            laserActive = true;
            modeSwitchTimer = 0;
            isLaserMode = true;
            fireLaserBeams();
        }

        // 60秒経過でクリア
        if (timeElapsed >= 60000) {
            timer.stop();
            parent.showGameOver("You survived! Congratulations!");
        }
    }

    private void fireLaserBeams() {
        laserBeams.clear();

        Enemy mainEnemy = enemies.stream().findFirst().orElse(null);
        if (mainEnemy == null) return;

        int centerX = mainEnemy.getX();
        int centerY = mainEnemy.getY();
        int laserCount = 16;
        double angleStep = 2 * Math.PI / laserCount;

        for (int i = 0; i < laserCount; i++) {
            double angle = i * angleStep;
            double dx = Math.cos(angle);
            double dy = Math.sin(angle);
            laserBeams.add(new LaserBeam(centerX, centerY, dx, dy, i));
        }
    }

    public void actionPerformed(ActionEvent e) {
        dodgeUpdateGame();
        repaint();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.ORANGE);
        g.drawString("[Dodge Mode]", getWidth() - 120, 20);

        // レーザー描画（40秒以降）
        if (laserActive) {
            for (LaserBeam lb : laserBeams) {
                lb.draw(g);
            }
        }

        if (player != null) {
            player.draw((Graphics2D) g, false);
        }
    }

    public void keyPressed(KeyEvent e) {
        // 弾を撃てないように無効化
    }

    public void keyReleased(KeyEvent e) {
        // 何もしない
    }

    public void keyTyped(KeyEvent e){}
}