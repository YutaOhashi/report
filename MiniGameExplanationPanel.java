import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;

public class MiniGameExplanationPanel extends JPanel {
    private GameWindow parent;
    private Image backgroundImage;

    public MiniGameExplanationPanel(GameWindow parent) {
        this.parent = parent;

        try (InputStream is = getClass().getResourceAsStream("/resources/spacecraft.jpg")) {
            if (is == null) {
                System.out.println("画像のリソースが見つかりません");
            } else {
                backgroundImage = ImageIO.read(is);
                System.out.println("背景画像の読み込みに成功しました");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        setLayout(new BorderLayout());
        setOpaque(true);

        setPreferredSize(new Dimension(800, 600));

        // ← Backボタン（左上）
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);   // 子パネルも透明に
        JButton backButton = new JButton("← Back");
        backButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        backButton.addActionListener(e -> parent.initStartPanel());
        topPanel.add(backButton);
        add(topPanel, BorderLayout.NORTH);

        // 中央：説明文とStartボタン（縦並び）
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);  // ← 透明にする（重要）
        centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea explanation = new JTextArea();
        explanation.setEditable(false);
        explanation.setFont(new Font("Monospaced", Font.PLAIN, 16));
        explanation.setText(
            "[Mini Game Rules]\n\n" +
            "- Avoid enemy bullets for 60 seconds\n" +
            "- If your life reaches 0, it's Game Over\n" +
            "- Game Clear if you survive for the full 60 seconds\n"
        );
        explanation.setOpaque(false);  // JTextAreaの背景透明化
        explanation.setForeground(Color.WHITE); 
        explanation.setBackground(new Color(0,0,0,0)); // 透明に
        explanation.setOpaque(false);
        explanation.setFocusable(false);
        explanation.setMaximumSize(new Dimension(600, 150));
        explanation.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ▶ Startボタン（中央）
        JButton startButton = new JButton("▶ Start");
        startButton.setFont(new Font("SansSerif", Font.BOLD, 20));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.addActionListener(e -> parent.startMiniGame());

        // スペースをあける
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(explanation);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        centerPanel.add(startButton);
        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);
    }

    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
        System.out.println("paintComponent: width=" + getWidth() + ", height=" + getHeight());
    }
}