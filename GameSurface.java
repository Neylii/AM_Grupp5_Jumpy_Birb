import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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

    public GameSurface(final int width, final int height) {
        this.gameOver = false;
        this.obstacles = new ArrayList<>();

        this.ticks = 0;
        this.gravityValue = 0;

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
            g.setColor(Color.red);
            g.fillRect(0, 0, d.width, d.height);
            g.setColor(Color.black);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("Game over!", 20, d.width / 2 - 24);
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
        g.setColor(Color.black);
        g.fillRect(birb.x, birb.y, birb.width, birb.height);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // this will trigger on the timer event
        // if the game is not over yet it will
        // update the positions of all aliens
        // and check for collision with the birb

        if (gameOver) {
            timer.stop();
            return;
        }

        Dimension d = getSize();

        ticks++;
        // make birb fall exponentially
        if (ticks % 2 == 0 && gravityValue < 12) {
            gravityValue++;
        }

        final List<Rectangle> toRemove = new ArrayList<>();
        int max_x = 0;
        for (Rectangle obstacle : obstacles) {
            // move obstacle -4px along x-axis
            obstacle.translate(-4, 0);
            // save the obstacle x position thats furthers to the left
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

        // Spawn new obstacles when the furthest one on the left passes half the game surface
        if (max_x < d.width / 2) {
            addObstacles(d.width, d.height);
        }
        // removes all obstacles that have gone of the game surface
        if (toRemove.size() > 0) {
            obstacles.removeAll(toRemove);
        }

        // check collision with ground
        if (birb.y + birb.height > d.height) {
            gameOver = true;
        }

        // makes birb fall
        gravity();

        this.repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // this event triggers when we release a key and then
        // we will move the birb if the game is not over yet

        if (gameOver) {
            return;
        }

        final int minHeight = 10;
        final int kc = e.getKeyCode();

        if (kc == KeyEvent.VK_SPACE && birb.y > minHeight) {
            gravityValue = -9;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // do nothing
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // do nothing
    }
}
