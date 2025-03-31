import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Memory extends JFrame {
    private static final int NUM_COLS = 5;
    private static final int NUM_ROWS = 4;
    private static final int TILE_SIZE = 100;
    private static final int GAP_SIZE = 5; // Reduced gap size
    private static final int WINDOW_WIDTH = NUM_COLS * (TILE_SIZE + GAP_SIZE) + GAP_SIZE;
    private static final int WINDOW_HEIGHT = NUM_ROWS * (TILE_SIZE + GAP_SIZE) + GAP_SIZE;
    private static final String[] IMAGE_URLS = {
            "https://www.kasandbox.org/programming-images/avatars/leafers-seed.png",
            "https://www.kasandbox.org/programming-images/avatars/leafers-seedling.png",
            "https://www.kasandbox.org/programming-images/avatars/leafers-sapling.png",
            "https://www.kasandbox.org/programming-images/avatars/leafers-tree.png",
            "https://www.kasandbox.org/programming-images/avatars/leafers-ultimate.png",
            "https://www.kasandbox.org/programming-images/avatars/marcimus.png",
            "https://www.kasandbox.org/programming-images/avatars/mr-pants.png",
            "https://www.kasandbox.org/programming-images/avatars/mr-pink.png",
            "https://www.kasandbox.org/programming-images/avatars/old-spice-man.png",
            "https://www.kasandbox.org/programming-images/avatars/robot_female_1.png"
    };

    private Tile[][] tiles = new Tile[NUM_ROWS][NUM_COLS];
    private List<Tile> flippedTiles = new ArrayList<>();
    private Timer timer;
    private int moveCount = 0;
    private JLabel moveLabel;
    private JLabel timerLabel;
    private int secondsPassed = 0;
    private Timer gameTimer;

    public Memory() {
        setTitle("Memory Game");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT + 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(GAP_SIZE, GAP_SIZE));

        JLabel welcomeLabel = new JLabel("Welcome to the Memory Game!", JLabel.CENTER);
        add(welcomeLabel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(NUM_ROWS, NUM_COLS, GAP_SIZE, GAP_SIZE));
        initTiles(gridPanel);
        add(gridPanel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(1, 2));

        moveLabel = new JLabel("Moves: 0", JLabel.CENTER);
        infoPanel.add(moveLabel);

        timerLabel = new JLabel("Time: 0s", JLabel.CENTER);
        infoPanel.add(timerLabel);

        add(infoPanel, BorderLayout.SOUTH);

        startGameTimer();

        setVisible(true);
    }

    private void initTiles(JPanel panel) {
        List<ImageIcon> images = loadImages();
        Collections.shuffle(images);
        int index = 0;
        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLS; j++) {
                Tile tile = new Tile(images.get(index++));
                tiles[i][j] = tile;
                panel.add(tile);
                tile.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        handleTileClick(tile);
                    }
                });
            }
        }
    }

    private List<ImageIcon> loadImages() {
        List<ImageIcon> images = new ArrayList<>();
        try {
            for (String url : IMAGE_URLS) {
                ImageIcon imageIcon = new ImageIcon(new URL(url));
                images.add(imageIcon);
                images.add(imageIcon); // Add a duplicate for matching
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (images.size() > NUM_COLS * NUM_ROWS) {
            images = images.subList(0, NUM_COLS * NUM_ROWS); // Ensure we have exactly enough images
        }
        return images;
    }

    private void handleTileClick(Tile tile) {
        if (flippedTiles.size() < 2 && !tile.isFaceUp() && !tile.isMatched()) {
            tile.showFace();
            flippedTiles.add(tile);
            if (flippedTiles.size() == 2) {
                moveCount++;
                moveLabel.setText("Moves: " + moveCount);
                checkForMatch();
            }
        }
    }

    private void checkForMatch() {
        Tile first = flippedTiles.get(0);
        Tile second = flippedTiles.get(1);
        if (first.getImage().equals(second.getImage())) {
            first.setMatched(true);
            second.setMatched(true);
            flippedTiles.clear();
            if (isGameWon()) {
                gameTimer.stop();
                JOptionPane.showMessageDialog(this, "Congratulations! You've matched all pairs!");
            }
        } else {
            timer = new Timer(500, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    vibrateTiles(first, second);
                    first.hideFace();
                    second.hideFace();
                    flippedTiles.clear();
                    timer.stop();
                }
            });
            timer.start();
        }
    }

    private boolean isGameWon() {
        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLS; j++) {
                if (!tiles[i][j].isMatched()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void vibrateTiles(Tile first, Tile second) {
        final int VIBRATION_LENGTH = 10;
        final int VIBRATION_DISTANCE = 5;

        Point firstOriginalLocation = first.getLocation();
        Point secondOriginalLocation = second.getLocation();

        Timer vibrationTimer = new Timer(25, new ActionListener() {
            int vibrationCount = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                int offset = (vibrationCount % 2 == 0) ? VIBRATION_DISTANCE : -VIBRATION_DISTANCE;
                first.setLocation(firstOriginalLocation.x + offset, firstOriginalLocation.y);
                second.setLocation(secondOriginalLocation.x + offset, secondOriginalLocation.y);
                vibrationCount++;
                if (vibrationCount >= VIBRATION_LENGTH) {
                    first.setLocation(firstOriginalLocation);
                    second.setLocation(secondOriginalLocation);
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        vibrationTimer.start();
    }

    private void startGameTimer() {
        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                secondsPassed++;
                timerLabel.setText("Time: " + secondsPassed + "s");
            }
        });
        gameTimer.start();
    }

    private class Tile extends JLabel {
        private ImageIcon face;
        private boolean faceUp = false;
        private boolean matched = false;
        private final Color defaultColor = new Color(173, 216, 230); // Light blue color for default box
        private final int BORDER_THICKNESS = 3; // Thickness of the border
        private final int BORDER_ARC = 20; // Border arc

        public Tile(ImageIcon face) {
            this.face = face;
            setIcon(createDefaultImage());
            setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
        }

        private ImageIcon createDefaultImage() {
            BufferedImage bufferedImage = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(defaultColor);
            g2d.fillRoundRect(BORDER_THICKNESS, BORDER_THICKNESS, getWidth() - BORDER_THICKNESS * 2,
                    getHeight() - BORDER_THICKNESS * 2, BORDER_ARC, BORDER_ARC);
            g2d.dispose();
            return new ImageIcon(bufferedImage);
        }

        public ImageIcon getImage() {
            return face;
        }

        public boolean isFaceUp() {
            return faceUp;
        }

        public void showFace() {
            setIcon(face);
            faceUp = true;
        }

        public void hideFace() {
            setIcon(createDefaultImage());
            faceUp = false;
        }

        public void setMatched(boolean matched) {
            this.matched = matched;
        }

        public boolean isMatched() {
            return matched;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(BORDER_THICKNESS));
            g2d.drawRoundRect(BORDER_THICKNESS / 2, BORDER_THICKNESS / 2, getWidth() - BORDER_THICKNESS,
                    getHeight() - BORDER_THICKNESS, BORDER_ARC, BORDER_ARC);

            if (!faceUp) {
                g2d.setColor(defaultColor);
                g2d.fillRoundRect(BORDER_THICKNESS, BORDER_THICKNESS, getWidth() - BORDER_THICKNESS * 2,
                        getHeight() - BORDER_THICKNESS * 2, BORDER_ARC, BORDER_ARC);
            }

            g2d.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Memory::new);
    }
}
