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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * A simple panel with a jumpy birb "game" in it.
 *
 */
public class GameSurface extends JPanel implements ActionListener, KeyListener {

    private static final long serialVersionUID = 6260582674762246325L;

    private boolean startScreen;
    private boolean gameOver;
    private Timer timer;
    private List<Rectangle> obstacles;
    private int speed;
    private Rectangle birb;

    private Rectangle ground;

    // Obstacles sizes
    private int columnWidth;
    private int columnGap;

    // Gravity
    private int tickRate;
    private int gravityValue;

    // Score
    private boolean hardMode;
    private boolean obstacleCheck;
    private int score;

    // Score test
    private List<Highscore> highscores;
    private List<Highscore> highscoresHard;
    private HighscoreComparator hsc;

    // Images for birb
    private boolean flap;
    private BufferedImage wingsUp;
    private String pathWingsUp;
    private BufferedImage wingsDown;
    private String pathWingsDown;

    // Images for atmosphere
    private BufferedImage background;
    private String pathBackground;
    private BufferedImage pipe;
    private String pathPipe;
    private BufferedImage imageGround;
    private String pathGround;

    // Image for Startscreen
    private BufferedImage imageStartScreen;
    private String pathStartScreen;

    // Image for Endscreen
    private BufferedImage imageEndScreen;
    private String pathEndScreen;

    public GameSurface(final int width, final int height) {
        this.startScreen = true;
        this.gameOver = false;
        this.obstacles = new ArrayList<>();
        this.speed = -4;
        this.columnWidth = 90;
        this.columnGap = 150;

        this.tickRate = 0;
        this.gravityValue = 0;

        this.obstacleCheck = true;
        this.hardMode = false;
        this.score = 0;

        this.highscores = new ArrayList<>();
        this.highscoresHard = new ArrayList<>();
        this.hsc = new HighscoreComparator();

        this.wingsUp = null;
        this.wingsDown = null;

        this.pathWingsUp = "images/birbWingsUp.png";
        this.pathWingsDown = "images/birbWingsDown.png";
        this.pathBackground = "images/background.png";
        this.pathPipe = "images/obstaclepipe.png";
        this.pathGround = "images/ground.png";
        this.pathStartScreen = "images/startscreen.png";
        this.pathEndScreen = "images/endscreen.png";

        try {
            wingsUp = ImageIO.read(new File(pathWingsUp));
            wingsDown = ImageIO.read(new File(pathWingsDown));
            background = ImageIO.read(new File(pathBackground));
            pipe = ImageIO.read(new File(pathPipe));
            imageGround = ImageIO.read(new File(pathGround));
            imageStartScreen = ImageIO.read(new File(pathStartScreen));
            imageEndScreen = ImageIO.read(new File(pathEndScreen));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error: Could not load images correctly");
            System.exit(-1);
        }

        this.birb = new Rectangle(60, width / 2 - 15, 60, 45);
        this.ground = new Rectangle(0, height - 35, 1600, 35);

        // How many obstacles to spawn
        for (int i = 0; i < 1; ++i) {
            addObstacles(width, height);
        }

        this.timer = new Timer(20, this);
        this.timer.start();
    }

    private void readFromFile(List<Highscore> highscores, String fileToRead) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileToRead))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                highscores.add(new Highscore(parts[0], Integer.parseInt(parts[1])));
            }

        } catch (IOException ex) {
            System.err.println(ex);
        }
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
        int obstacleHeight = ThreadLocalRandom.current().nextInt(100, (height - columnGap) - 50);
        // Top obstacle
        obstacles.add(new Rectangle(width, 0, columnWidth, obstacleHeight));

        // Bottom obstacle
        int bottomObstacle = height - obstacleHeight - columnGap;
        obstacles.add(new Rectangle(width, obstacleHeight + columnGap, columnWidth, bottomObstacle));
    }

    /**
     * Call this method when the graphics needs to be repainted on the graphics
     * surface.
     *
     * @param g the graphics to paint on
     */
    private void repaint(Graphics g) {
        final Dimension d = this.getSize();

        if (startScreen) {
            startScreen(g, d);
            return;
        }

        if (gameOver) {
            gameOverScreen(g, d);
            return;
        }

        drawImages(g, d);

        // Reposition ground
        if (ground.x < -828) {
            ground.x = 0;
        }

        // Draw score
        g.setColor(Color.white);
        g.setFont(new Font("Consolas", Font.BOLD, 48));
        g.drawString("" + score, d.width / 2, d.height / 4);

    }

    private void drawImages(Graphics g, Dimension d) {
        // Draw background
        g.drawImage(background, 0, 0, null);

        // Draw obstacles
        for (Rectangle obstacle : obstacles) {
            g.drawImage(pipe, obstacle.x, obstacle.y, obstacle.width, obstacle.height, null);
        }

        // Draw obstacles
        for (Rectangle obstacle : obstacles) {
            g.drawImage(pipe, obstacle.x, obstacle.y, obstacle.width, obstacle.height, null);
        }

        // Draw ground
        g.drawImage(imageGround, ground.x, d.height - ground.height, null);

        // Draw birb
        if (flap) {
            g.drawImage(wingsDown, birb.x, birb.y, birb.width, birb.height, null);
        } else {
            g.drawImage(wingsUp, birb.x, birb.y, birb.width, birb.height, null);
        }
    }

    private void startScreen(Graphics g, final Dimension d) {
        g.drawImage(imageStartScreen, -9, -39, null);
    }

    private void gameOverScreen(Graphics g, final Dimension d) {
        
        String difficulty = "";
        if (hardMode) {
            Highscore.checkHighscore(highscoresHard, score, hardMode, hsc);
            difficulty = " Hard";
        } else {
            Highscore.checkHighscore(highscores, score, hardMode, hsc);
            difficulty = " Normal";
        }
        g.drawImage(imageEndScreen, -9, -39, null);
        g.setFont(new Font("Consolas", Font.BOLD, 48));
        g.drawString("Your score: " + score, (d.width / 2) - 180, (d.height / 4) + 50);

        g.drawString("Highscores:" + difficulty, (d.width / 2) - 200, (d.height / 2) - 50);

        if (hardMode) {
            showHighscoresOnScreen(g, highscoresHard);
        } else {
            showHighscoresOnScreen(g, highscores);
        }
    }

    private void showHighscoresOnScreen(Graphics g, List<Highscore> highscores) {
        g.setFont(new Font("Consolas", Font.BOLD, 30));

        int scoreHeight = 400;
        int scoreWidth = 150;
        int scorePlacement = 1;

        for (Highscore highscore : highscores) {
            if (scorePlacement == 6) {
                scoreWidth = 450;
                scoreHeight = 400;
            }
            if (scorePlacement == 10) {
                g.drawString(scorePlacement + "." + highscore.toString(), scoreWidth, scoreHeight);
                break;
            }
            g.drawString(scorePlacement + ". " + highscore.toString(), scoreWidth, scoreHeight);
            scoreHeight += 35;
            scorePlacement++;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // This will trigger on the timer event
        // if the game is not over yet it will
        // update the positions of all aliens
        // and check for collision with the birb
        Dimension d = getSize();

        if (startScreen || gameOver) {
            timer.stop();
            return;
        }

        tickRate++;
        // Make birb fall exponentially
        if (tickRate % 2 == 0 && gravityValue < 12) {
            gravityValue++;
        }

        // Give score when passing obstacle
        if (birb.x > obstacles.get(0).x && obstacleCheck) {
            score = score + 10;
            obstacleCheck = false;
        }

        // Move ground
        ground.translate(speed, 0);

        // Check collision with ground
        if (birb.y + birb.height > d.height - 35) {
            gameOver = true;
        }

        final List<Rectangle> toRemove = new ArrayList<>();
        int max_x = moveObstacle(toRemove);

        // Spawn new obstacles when the furthest one on the left passes half the game
        // surface
        if (max_x < d.width / 2) {
            addObstacles(d.width, d.height);
        }
        // Removes all obstacles that have gone of the game surface
        if (toRemove.size() > 0) {
            obstacles.removeAll(toRemove);
            obstacleCheck = true;
        }

        // Makes birb fall
        gravity();
        this.repaint();
    }

    private int moveObstacle(final List<Rectangle> toRemove) {
        int max_x = 0;
        for (Rectangle obstacle : obstacles) {
            // Move obstacle along x-axis
            obstacle.translate(speed, 0);
            // Save the obstacle x-position thats furthers to the right
            if (max_x < obstacle.x) {
                max_x = obstacle.x;
            }
            // If the obstacle has gone off the game surface, add it to the remove list
            if (obstacle.x + obstacle.width < 0) {
                toRemove.add(obstacle);
            }
            collisionCheck(obstacle);
        }
        return max_x;
    }

    private void collisionCheck(Rectangle obstacle) {
        if (obstacle.intersects(birb)) {
            gameOver = true;
        }
    }

    private void restart(Dimension d) {
        startScreen = false;
        gameOver = false;
        obstacles.removeAll(obstacles);

        tickRate = 0;
        gravityValue = 0;

        obstacleCheck = true;
        score = 0;

        birb.x = 60;
        birb.y = d.width / 2 - 15;

        // How many obstacles to spawn
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
        // Do nothing
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // This event triggers when we press a key and then
        // we will move the birb if the game is not over yet

        final int kc = e.getKeyCode();
        if (gameOver) {
            if (kc == KeyEvent.VK_SPACE) {
                Dimension d = getSize();
                restart(d);
            }
        }

        if (startScreen) {
            setDifficulty(kc);
        }

        final int minHeight = 10;
        if (kc == KeyEvent.VK_SPACE && birb.y > minHeight) {
            gravityValue = -9;
            score++;
            flap = true;
        }
    }

    private void setDifficulty(final int kc) {
        Dimension d = getSize();
        if (kc == KeyEvent.VK_1) {
            readFromFile(highscores, "highscore.txt");
            hardMode = false;
            restart(d);
        } else if (kc == KeyEvent.VK_2) {
            readFromFile(highscoresHard, "highscore-hard.txt");
            speed = -6;
            columnGap = 130;
            hardMode = true;
            restart(d);
        }
    }
}
