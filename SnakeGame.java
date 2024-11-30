import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import javax.swing.*;

public class SnakeGame {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game");
        GamePanel gamePanel = new GamePanel();
        
        // Create a panel to simulate a thick border around the game area
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(Color.BLACK); // The background of the window
        containerPanel.setBorder(BorderFactory.createLineBorder(Color.CYAN, 10)); // Reduced cyan border thickness

        // Add game panel inside the container panel
        containerPanel.add(gamePanel, BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(containerPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static class GamePanel extends JPanel implements ActionListener, KeyListener {
        private static final int WIDTH = 1000;  
        private static final int HEIGHT = 750; // Adjusted height to make space for UI elements
        private static final int TILE_SIZE = 20;
        private static final int GRID_WIDTH = 50; 
        private static final int GRID_HEIGHT = 35; 
        private LinkedList<Point> snake;
        private Point food;
        private String direction;
        private boolean isGameOver;
        private Timer timer;

        private JButton restartButton;
        private JButton quitButton;
        private JButton changeSnakeColorButton;
        private JButton changeFoodColorButton;
        private int score;
        private int highScore;

        private Color snakeColor = Color.GREEN;
        private Color foodColor = Color.RED;
        private String playerName = "Player";

        public GamePanel() {
            setPreferredSize(new Dimension(WIDTH, HEIGHT));
            setBackground(Color.BLACK);
            setFocusable(true);
            addKeyListener(this);

            // Create buttons and place them outside the game area
            restartButton = new JButton("Restart");
            quitButton = new JButton("Quit");
            changeSnakeColorButton = new JButton("Change Snake Color");
            changeFoodColorButton = new JButton("Change Food Color");

            // Button positions adjusted below the game area
            restartButton.setBounds(400, 600, 100, 40);
            quitButton.setBounds(400, 650, 100, 40);
            changeSnakeColorButton.setBounds(350, 500, 180, 40);
            changeFoodColorButton.setBounds(350, 550, 180, 40);

            add(restartButton);
            add(quitButton);
            add(changeSnakeColorButton);
            add(changeFoodColorButton);

            restartButton.addActionListener(e -> restartGame());
            quitButton.addActionListener(e -> System.exit(0));
            changeSnakeColorButton.addActionListener(e -> chooseSnakeColor());
            changeFoodColorButton.addActionListener(e -> chooseFoodColor());

            // Prompt player for name
            playerName = JOptionPane.showInputDialog("Enter your name:");

            initGame();
            startGame();
        }

        private void initGame() {
            snake = new LinkedList<>();
            snake.add(new Point(10, 10));  // Starting position
            food = new Point(15, 10);  // Initial food position
            direction = "RIGHT";
            isGameOver = false;
            score = 0;
            highScore = Math.max(score, highScore); // Initialize with a valid high score
        }

        private void startGame() {
            timer = new Timer(100, this); // Update every 100ms
            timer.start();
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (isGameOver) {
                drawGameOver(g);
            } else {
                drawBoundary(g); // Draw the boundary line with a thick border
                drawSnake(g);
                drawFood(g);
                drawScore(g); // Display score
            }
        }

        private void drawBoundary(Graphics g) {
            g.setColor(Color.CYAN); // Set the color of the border
            int borderThickness = 10; // Reduced border thickness

            // Draw the border (top, left, right, and bottom)
            g.fillRect(0, 0, WIDTH, borderThickness); // Top border
            g.fillRect(0, 0, borderThickness, HEIGHT); // Left border
            g.fillRect(WIDTH - borderThickness, 0, borderThickness, HEIGHT); // Right border
            g.fillRect(0, HEIGHT - borderThickness, WIDTH, borderThickness); // Bottom border
        }

        private void drawSnake(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(snakeColor);
            for (Point p : snake) {
                // Offset by border thickness (10 pixels)
                g2d.fillRoundRect(p.x * TILE_SIZE + 10, p.y * TILE_SIZE + 10, TILE_SIZE, TILE_SIZE, 10, 10); // Uniform body
            }
        }

        private void drawFood(Graphics g) {
            g.setColor(foodColor);
            // Offset by border thickness (10 pixels)
            g.fillOval(food.x * TILE_SIZE + 10, food.y * TILE_SIZE + 10, TILE_SIZE, TILE_SIZE); // Consistent food size and color
        }

        private void drawScore(Graphics g) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 20));

            // Draw player name at the top-left corner
            g.drawString("Player: " + playerName, 10, 30);

            // Draw score just below the player's name
            g.drawString("Score: " + score, 10, 60);

            // Draw high score just below the score
            g.drawString("High Score: " + highScore, 10, 90);
        }

        private void drawGameOver(Graphics g) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Game Over!", WIDTH / 2 - 100, HEIGHT / 2 - 40);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press 'R' to Restart", WIDTH / 2 - 100, HEIGHT / 2);
        }

        public void actionPerformed(ActionEvent e) {
            if (isGameOver) return;

            moveSnake();
            checkCollision();
            checkFood();
            repaint();
        }

        private void moveSnake() {
            Point head = snake.getFirst();
            Point newHead = null;

            switch (direction) {
                case "UP": newHead = new Point(head.x, head.y - 1); break;
                case "DOWN": newHead = new Point(head.x, head.y + 1); break;
                case "LEFT": newHead = new Point(head.x - 1, head.y); break;
                case "RIGHT": newHead = new Point(head.x + 1, head.y); break;
            }

            snake.addFirst(newHead);
            snake.removeLast();  // Remove the tail
        }

        private void checkCollision() {
            Point head = snake.getFirst();

            // Collision with walls
            if (head.x < 0 || head.x >= GRID_WIDTH || head.y < 0 || head.y >= GRID_HEIGHT) {
                gameOver();
            }

            // Collision with itself
            for (int i = 1; i < snake.size(); i++) {
                if (head.equals(snake.get(i))) {
                    gameOver();
                }
            }
        }

        private void checkFood() {
            Point head = snake.getFirst();
            if (head.equals(food)) {
                snake.addFirst(new Point(food.x, food.y));
                score += 10;
                if (score > highScore) {
                    highScore = score; // Update high score if current score exceeds it
                }
                generateNewFood();
            }
        }

        private void generateNewFood() {
            int x = (int) (Math.random() * GRID_WIDTH);
            int y = (int) (Math.random() * GRID_HEIGHT);
            food = new Point(x, y);
        }

        private void gameOver() {
            isGameOver = true;
            timer.stop();
            requestFocusInWindow();  // Ensure that we can still handle input after game over
        }

        private void restartGame() {
            isGameOver = false;
            score = 0;
            initGame();
            startGame();
            repaint();
            requestFocusInWindow();  // Ensure that we can still handle input after restarting
        }

        private void chooseSnakeColor() {
            String colorName = JOptionPane.showInputDialog("Enter snake color (e.g., red, green, blue):");
            if (colorName != null) {
                switch (colorName.toLowerCase()) {
                    case "red": snakeColor = Color.RED; break;
                    case "green": snakeColor = Color.GREEN; break;
                    case "blue": snakeColor = Color.BLUE; break;
                    case "yellow": snakeColor = Color.YELLOW; break;
                    case "black": snakeColor = Color.BLACK; break;
                    case "white": snakeColor = Color.WHITE; break;
                    default: 
                        JOptionPane.showMessageDialog(this, "Invalid color name! Using default color.");
                        snakeColor = Color.GREEN;
                }
            }
            requestFocusInWindow();  // Ensure focus is back to game after changing color
        }

        private void chooseFoodColor() {
            String colorName = JOptionPane.showInputDialog("Enter food color (e.g., red, green, blue):");
            if (colorName != null) {
                switch (colorName.toLowerCase()) {
                    case "red": foodColor = Color.RED; break;
                    case "green": foodColor = Color.GREEN; break;
                    case "blue": foodColor = Color.BLUE; break;
                    case "yellow": foodColor = Color.YELLOW; break;
                    case "black": foodColor = Color.BLACK; break;
                    case "white": foodColor = Color.WHITE; break;
                    default: 
                        JOptionPane.showMessageDialog(this, "Invalid color name! Using default color.");
                        foodColor = Color.RED;
                }
            }
            requestFocusInWindow();  // Ensure focus is back to game after changing color
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (key == KeyEvent.VK_UP && !direction.equals("DOWN")) {
                direction = "UP";
            } else if (key == KeyEvent.VK_DOWN && !direction.equals("UP")) {
                direction = "DOWN";
            } else if (key == KeyEvent.VK_LEFT && !direction.equals("RIGHT")) {
                direction = "LEFT";
            } else if (key == KeyEvent.VK_RIGHT && !direction.equals("LEFT")) {
                direction = "RIGHT";
            } else if (key == KeyEvent.VK_R && isGameOver) {
                restartGame();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }
    }
}
