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
import java.util.Collections;
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

    private boolean startScreen;
    private boolean gameOver;
    private Timer timer;
    private List<Rectangle> obstacles;
    private int obstacleSpeed;
    private Rectangle birb;

    private Rectangle ground;

    // Obstacles sizes
    private int columnWidth;
    private int columnGap;

    // For gravity
    private int ticks;
    private int gravityValue;

    // Score
    private boolean hardMode;
    private boolean obstacleCheck;
    private int score;

    // score test
    private List<Highscore> highscores;
    private List<Highscore> highscoresHard;
    private HighscoreComparator hsc;

    // Buttons
    private JButton normalDifficulty;
    private JButton hardDifficulty;

    // Picture for birb
    private boolean flap;
    private boolean imageNotFound;
    private BufferedImage wingsUp;
    private BufferedImage wingsDown;
    private String imagePathUp;
    private String imagePathDown;

    //picture for atmosphere
    private BufferedImage background;
    private String imageBackground;
    private BufferedImage pipe;
    private String imagePipe;
    private BufferedImage groundBuff;
    private String imageGround;

    // start screen
    private BufferedImage startScreenBuff;
    private String imageStartScreen;

    public GameSurface(final int width, final int height) {
        this.startScreen = true;
        this.gameOver = false;
        this.obstacles = new ArrayList<>();
        this.obstacleSpeed = -4;
        this.columnWidth = 90;
        this.columnGap = 150;

        this.ticks = 0;
        this.gravityValue = 0;

        this.obstacleCheck = true;
        this.hardMode = false;
        this.score = 0;

        this.highscores = new ArrayList<>();
        this.highscoresHard = new ArrayList<>();
        this.hsc = new HighscoreComparator();

        this.imageNotFound = false;
        this.wingsUp = null;
        this.wingsDown = null;
        this.imagePathUp = "images/birbWingsUp.png";
        this.imagePathDown = "images/birbWingsDown.png";

        this.imageBackground = "images/background.png";
        this.imagePipe = "images/obstaclepipe.png";
        this.imageGround = "images/ground.png";
        this.imageStartScreen = "images/startscreen.png";
        
        try {
            wingsUp = ImageIO.read(new File(imagePathUp));
            wingsDown = ImageIO.read(new File(imagePathDown));
            background = ImageIO.read(new File(imageBackground));
            pipe = ImageIO.read(new File(imagePipe));
            groundBuff = ImageIO.read(new File(imageGround));
            startScreenBuff = ImageIO.read(new File(imageStartScreen));
        } catch (IOException e) {
            imageNotFound = true;
            JOptionPane.showMessageDialog(null, "Error: Could not load image for birb correctly");
        }
        normalDifficulty = new JButton();
        normalDifficulty.addActionListener(this);
        normalDifficulty.setVisible(false);
        this.add(normalDifficulty);

        hardDifficulty = new JButton();
        hardDifficulty.addActionListener(this);
        hardDifficulty.setVisible(false);
        this.add(hardDifficulty);

        this.birb = new Rectangle(60, width / 2 - 15, 40, 30);
        this.ground = new Rectangle(0, height-35, 1600, 35);

        // how many obstacles to spawn
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
        // top obstacle
        obstacles.add(new Rectangle(width, 0, columnWidth, obstacleHeight));

        // bottom obstacle
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

        // draw the background
        g.drawImage(background, 0, 0, null);

        // draw the obstacles
        for (Rectangle obstacle : obstacles) {
            g.drawImage(pipe, obstacle.x, obstacle.y, obstacle.width, obstacle.height, null);
        }

        // reposition ground
        if (ground.x < -800) {
            ground.x = 0;
        }
        //draw ground
        g.drawImage(groundBuff, ground.x, d.height - ground.height, null);

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

    private void startScreen(Graphics g, final Dimension d) {
        g.drawImage(startScreenBuff, 0, 0, null);

        normalDifficulty.setText("Normal");
        normalDifficulty.setSize(100, 50);
        normalDifficulty.setLocation((d.width / 4) - 60, d.height - 200);
        normalDifficulty.setVisible(true);

        hardDifficulty.setText("Hard");
        hardDifficulty.setSize(100, 50);
        hardDifficulty.setLocation((d.width / 2) - 60, d.height - 200);
        hardDifficulty.setVisible(true);
    }

    private void gameOverScreen(Graphics g, final Dimension d) {
        if (hardMode) {
            Highscore.checkHighscore(highscoresHard, score, hardMode, hsc);
        } else {
            Highscore.checkHighscore(highscores, score, hardMode, hsc);
        }

        g.setColor(Color.PINK);
        g.fillRect(0, 0, d.width, d.height);
        g.setColor(Color.black);
        g.setFont(new Font("Consolas", Font.BOLD, 56));
        g.drawString("Game over!", 250, d.height / 6);

        g.setFont(new Font("Consolas", Font.BOLD, 48));
        g.drawString("Your score: " + score, (d.width / 2) - 180, (d.height / 4) + 50);

        String difficulty = "";
        if (hardMode) {
            difficulty = " (Hard)";
        } else {
            difficulty = " (Normal)";
        }
        g.drawString("Highscores:" + difficulty, (d.width / 2) - 140, (d.height / 2) - 50);

        if (hardMode) {
            showHighscoresOnScreen(g, highscoresHard);
        } else {
            showHighscoresOnScreen(g, highscores);
        }

        g.setFont(new Font("Consolas", Font.BOLD, 24));
        g.drawString("Press \"space\" to restart", (d.width / 2) - 150, (d.height / 2) + 250);
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
        // this will trigger on the timer event
        // if the game is not over yet it will
        // update the positions of all aliens
        // and check for collision with the birb

        Dimension d = getSize();

        if (e.getSource() == normalDifficulty) {
            readFromFile(highscores, "highscore.txt");
            hardMode = false;
            restart(d);
        }

        if (e.getSource() == hardDifficulty) {
            readFromFile(highscoresHard, "highscore-hard.txt");
            obstacleSpeed = -6;
            columnGap = 130;
            hardMode = true;
            restart(d);
        }
        if (startScreen) {
            timer.stop();
            return;
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

        //move ground
        ground.translate(obstacleSpeed, 0);
        
        // check collision with ground
        if (birb.y + birb.height > d.height - 35) {
            gameOver = true;
        }
        
        final List<Rectangle> toRemove = new ArrayList<>();
        int max_x = 0;
        for (Rectangle obstacle : obstacles) {
            // move obstacle -4px along x-axis
            obstacle.translate(obstacleSpeed, 0);
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

        
        
        

        // makes birb fall
        gravity();

        this.repaint();
    }

    private void restart(Dimension d) {
        startScreen = false;
        gameOver = false;
        obstacles.removeAll(obstacles);

        ticks = 0;
        gravityValue = 0;

        obstacleCheck = true;
        score = 0;

        normalDifficulty.setVisible(false);
        hardDifficulty.setVisible(false);

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
