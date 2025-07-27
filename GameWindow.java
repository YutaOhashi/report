import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class GameWindow extends JFrame {
    private GamePanel gamePanel;
    private JPanel startPanel;

    public GameWindow() {
        setTitle("Shooting Game");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initStartPanel();
        setVisible(true);
    }

    public void initStartPanel() {
        startPanel = new JPanel() {
            private Image backgroundImage;
            {
                try {
                    backgroundImage = ImageIO.read(getClass().getResource("/resources/spacecraft.jpg"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        startPanel.setLayout(new BoxLayout(startPanel, BoxLayout.Y_AXIS));

        // タイトルラベルを追加（中央揃え、大きめフォント）
        JLabel titleLabel = new JLabel("Space Barrage");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        startPanel.add(Box.createVerticalStrut(20));  // 上の余白
        startPanel.add(titleLabel);
        startPanel.add(Box.createVerticalStrut(30));  // タイトルとボタンの間の余白

        // スタートボタンを中央に
        JButton startButton = new JButton("▶ Game Start");
        startButton.setFont(new Font("SansSerif", Font.BOLD, 24));
        startButton.setAlignmentX(CENTER_ALIGNMENT);
        startButton.addActionListener(e -> startGame());
        startPanel.add(Box.createVerticalGlue());  // 上の余白
        startPanel.add(startButton);

        // ルール説明テキストをその下に
        JTextArea rules = new JTextArea();
        rules.setEditable(false);
        rules.setFont(new Font("Monospaced", Font.PLAIN, 14));
        rules.setText(
            "[Rules Explanation]\n\n" +
            "- The player is controlled by the mouse and fires lasers by pressing the spacebar.\n" +
            "- If you fire more than 4 shots per second, the weapon overheats and you cannot shoot for 1.5 seconds.\n" +
            "- Defeat 30 normal enemies to clear the game.\n" +
            "- After hitting enemies 5 times, an invincible red boss (Red Enemy) appears. You cannot defeat it, so keep shooting while avoiding its attacks.\n" +
            "- After hitting enemies 10 times, the red boss disappears and starts firing a rotating laser beam every 5 seconds. If you get hit, it's instant death, so be careful!\n" +
            "- The time limit is 60 seconds. The game ends if time runs out.\n\n" +
            "[Mechanics]\n\n" +
            "- Overheat System: Shooting too many times in a short period causes the weapon to overheat and temporarily disables shooting. Shoot with a good rhythm.\n" +
            "- Red Boss (Red Enemy): An invincible enemy that cannot be damaged. Pay attention to its movements and attack patterns to avoid getting hit.\n" +
            "- Laser Beam (LaserBeam): A rotating laser fired by the boss. Avoid contact as it deals damage.\n" +
            "- Player Blinking: After taking damage, the player blinks for a short time and becomes invincible.\n"
        );
        rules.setLineWrap(true);        // テキストを折り返す
        rules.setWrapStyleWord(true);   // 単語単位で折り返す
        rules.setCaretPosition(0);      // スクロール位置を先頭に設定

        JScrollPane scrollPane = new JScrollPane(rules);
        scrollPane.setAlignmentX(CENTER_ALIGNMENT);
        scrollPane.setMaximumSize(new Dimension(600, 300));  // スクロールペインのサイズ固定

        startPanel.add(Box.createRigidArea(new Dimension(0, 20))); // ボタンと説明の間の余白
        startPanel.add(scrollPane);

        startPanel.add(Box.createVerticalGlue());  // 下の余白

        setContentPane(startPanel);
        revalidate();

        // 右下：ミニゲームボタン
        JButton miniGameButton = new JButton("◎ Mini Game");
        miniGameButton.setFont(new Font("SansSerif", Font.PLAIN, 16));
        miniGameButton.addActionListener(e -> showMiniGameExplanation());

        miniGameButton.setAlignmentX(CENTER_ALIGNMENT);
        startPanel.add(Box.createRigidArea(new Dimension(0, 20)));  // ミニゲームボタンの上に余白
        startPanel.add(miniGameButton);
        
        setContentPane(startPanel);
        revalidate();
    }

    public void startGame() {
        gamePanel = new GamePanel(this);
        setContentPane(gamePanel);
        revalidate();
        gamePanel.startGame();

        // フォーカスはinvokeLaterで安全に要求
        SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());
    }

    public void startMiniGame() {
        gamePanel = new DodgeGamePanel(this); // DodgeGamePanelは後ほど作成
        setContentPane(gamePanel);
        revalidate();
        gamePanel.startGame();
        SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());
    }

    public void showMiniGameExplanation() {
        MiniGameExplanationPanel explanationPanel = new MiniGameExplanationPanel(this);
        setContentPane(explanationPanel);
        explanationPanel.setPreferredSize(new Dimension(800, 600)); // サイズ固定

        // pack() の代わりに setSize() でサイズを明示的に指定
        setSize(800, 600);

        setLocationRelativeTo(null); // 画面中央に表示

        revalidate();
        repaint();
    }

    public void showGameOver(String message) {
        JOptionPane.showMessageDialog(this, message);
        initStartPanel();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameWindow::new);
    }
}