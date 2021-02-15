import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * A simple panel with a jumpy birb "game" in it.
 *
 */
public class GameSurface extends JPanel implements ActionListener, KeyListener {

    private static final long serialVersionUID = 6260582674762246325L;

    private boolean gameOver;
    private Timer timer;
    private List<Rectangle> obstacles;
    private Rectangle birb;

    // For gravity
    private int ticks;
    private int gravityValue;

    // score
    private boolean obstacleCheck;
    private int score;
    private int highScore;
    private String highScoreName;

    private JButton restartButton;

    // picture for birb
    private boolean flap;
    private boolean imageNotFound;
    private BufferedImage wingsUp;
    private BufferedImage wingsDown;
    private String imagePathUp;
    private String imagePathDown;

    public GameSurface(final int width, final int height) {
        this.gameOver = false;
        this.obstacles = new ArrayList<>();

        this.ticks = 0;
        this.gravityValue = 0;

        this.obstacleCheck = true;
        this.score = 0;
        this.highScore = 0;

        this.imageNotFound = false;
        this.wingsUp = null;
        this.wingsDown = null;
        this.imagePathUp = "images/birbWingsUp.png";
        this.imagePathDown = "images/birbWingsDown.png";

        try {
            wingsUp = ImageIO.read(new File(imagePathUp));
            wingsDown = ImageIO.read(new File(imagePathDown));
        } catch (IOException e) {
            imageNotFound = true;
            JOptionPane.showMessageDialog(null, "Error: Could not load image for birb correctly");
        }

        restartButton = new JButton();
        restartButton.addActionListener(this);
        restartButton.setVisible(false);
        this.add(restartButton);

        this.birb = new Rectangle(60, width / 2 - 15, 40, 30);

        // how many obstacles to spawn
        for (int i = 0; i < 1; ++i) {
            addObstacles(width, height);
        }

        this.timer = new Timer(20, this);
        this.timer.start();
    }

    private void gravity() {
        birb.translate(0, gravityValue);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        repaint(g);
    }

    /**
     * Add obstacles one on the top, one on the bottom and a gap in the middle.
     *
     * @param width  on screen
     * @param height on screen
     */
    private void addObstacles(final int width, final int height) {
        int columnWidth = 80;
        int columnGap = 150;
        int obstacleHeight = ThreadLocalRandom.current().nextInt(50, (height - columnGap));
        obstacles.add(new Rectangle(width, 0, columnWidth, obstacleHeight));

        int obstacleHeight2 = height - obstacleHeight - columnGap;
        obstacles.add(new Rectangle(width, obstacleHeight + columnGap, columnWidth, obstacleHeight2));
    }

    /**
     * Call this method when the graphics needs to be repainted on the graphics
     * surface.
     *
     * @param g the graphics to paint on
     */
    private void repaint(Graphics g) {
        final Dimension d = this.getSize();

        if (gameOver) {
            gameOverScreen(g, d);
            return;
        }

        // fill the background
        g.setColor(Color.cyan);
        g.fillRect(0, 0, d.width, d.height);

        // draw the obstacles
        for (Rectangle obstacle : obstacles) {
            g.setColor(Color.green.darker());
            g.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
        }

        // draw the birb
        if (imageNotFound) {
            g.setColor(Color.black);
            g.fillRect(birb.x, birb.y, birb.width, birb.height);
        } else {
            if (flap) {
                g.drawImage(wingsDown, birb.x, birb.y, birb.width, birb.height, null);
            } else {
                g.drawImage(wingsUp, birb.x, birb.y, birb.width, birb.height, null);
            }
        }

        // draw the score
        g.setColor(Color.white);
        g.setFont(new Font("Consolas", Font.BOLD, 48));
        g.drawString("" + score, d.width / 2, d.height / 4);
    }

    private void gameOverScreen(Graphics g, final Dimension d) {
        if (score > highScore) {
            highScore = score;
            highScoreName = JOptionPane.showInputDialog("New highscore! Enter your name:");
        }

        g.setColor(Color.red);
        g.fillRect(0, 0, d.width, d.height);
        g.setColor(Color.black);
        g.setFont(new Font("Consolas", Font.BOLD, 48));
        g.drawString("Game over!", 250, d.height / 6);

        g.drawString("Score:", (d.width / 2) - 75, d.height / 4);
        g.setFont(new Font("Consolas", Font.BOLD, 48));
        g.drawString("" + score, (d.width / 2) - 20, d.height / 3);

        g.drawString("Highscore:", (d.width / 2) - 130, (d.height / 2) - 40);
        g.setFont(new Font("Consolas", Font.BOLD, 48));
        g.drawString(highScoreName + ": " + highScore, (d.width / 2) - 20, (d.height / 2) + 20);

        restartButton.setText("Restart");
        restartButton.setSize(100, 50);
        restartButton.setLocation((d.width / 2) - 60, d.height - 200);
        restartButton.setVisible(true);
        return;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // this will trigger on the timer event
        // if the game is not over yet it will
        // update the positions of all aliens
        // and check for collision with the birb

        Dimension d = getSize();

        if (e.getSource() == restartButton) {
            restart(d);
        }

        if (gameOver) {
            timer.stop();
            return;
        }

        ticks++;
        // make birb fall exponentially
        if (ticks % 2 == 0 && gravityValue < 12) {
            gravityValue++;
        }

        // give score when passing obstacle
        if (birb.x > obstacles.get(0).x && obstacleCheck) {
            score = score + 10;
            obstacleCheck = false;
        }

        final List<Rectangle> toRemove = new ArrayList<>();
        int max_x = 0;
        for (Rectangle obstacle : obstacles) {
            // move obstacle -4px along x-axis
            obstacle.translate(-4, 0);
            // save the obstacle x position thats furthers to the right
            if (max_x < obstacle.x) {
                max_x = obstacle.x;
            }
            // if the obstacle has gone off the game surface, add it to the remove list
            if (obstacle.x + obstacle.width < 0) {
                toRemove.add(obstacle);
            }
            // Check for collision
            if (obstacle.intersects(birb)) {
                gameOver = true;
            }
        }

        // Spawn new obstacles when the furthest one on the left passes half the game
        // surface
        if (max_x < d.width / 2) {
            addObstacles(d.width, d.height);
        }
        // removes all obstacles that have gone of the game surface
        if (toRemove.size() > 0) {
            obstacles.removeAll(toRemove);
            obstacleCheck = true;
        }

        // check collision with ground
        if (birb.y + birb.height > d.height) {
            gameOver = true;
        }

        // makes birb fall
        gravity();

        this.repaint();
    }

    private void restart(Dimension d) {
        gameOver = false;
        obstacles.removeAll(obstacles);

        ticks = 0;
        gravityValue = 0;

        obstacleCheck = true;
        score = 0;

        restartButton.setVisible(false);

        this.birb = new Rectangle(60, d.width / 2 - 15, 40, 30);

        // how many obstacles to spawn
        for (int i = 0; i < 1; ++i) {
            addObstacles(d.width, d.height);
        }

        timer.restart();

        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            return;
        }

        final int minHeight = 10;
        final int kc = e.getKeyCode();

        if (kc == KeyEvent.VK_SPACE && birb.y > minHeight) {
            flap = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // do nothing
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // this event triggers when we press a key and then
        // we will move the birb if the game is not over yet

        final int kc = e.getKeyCode();

        if (gameOver) {
            if (kc == KeyEvent.VK_SPACE) {
                Dimension d = getSize();
                restart(d);
            }
            return;
        }

        final int minHeight = 10;
        if (kc == KeyEvent.VK_SPACE && birb.y > minHeight) {
            gravityValue = -9;
            score++;
            flap = true;
        }
    }
}
