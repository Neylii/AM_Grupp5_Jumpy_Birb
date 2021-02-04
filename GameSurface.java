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

    public GameSurface(final int width, final int height) {
        this.gameOver = false;
        this.obstacles = new ArrayList<>();

        for (int i = 0; i < 5; ++i) {
            addObstacles(width, height);
        }

        this.birb = new Rectangle(20, width/2-15, 30, 20);

        this.timer = new Timer(20, this);
        this.timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        repaint(g);
    }

    private void addObstacles(final int width, final int height) {
        int x = ThreadLocalRandom.current().nextInt(width / 2, width - 30);
        int y = ThreadLocalRandom.current().nextInt(20, height - 30);
        obstacles.add(new Rectangle(x, y, 10, 10));
    }

    /**
     * Call this method when the graphics needs to be repainted
     * on the graphics surface.
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
            g.drawString("Game over!", 20, d.width/2-24);
            return;
        }

        // fill the background
        g.setColor(Color.cyan);
        g.fillRect(0, 0, d.width, d.height);

        // draw the obstacles
        for (Rectangle obstacle : obstacles) {
            g.setColor(Color.red);
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

        final List<Rectangle> toRemove = new ArrayList<>();

        for (Rectangle obstacle : obstacles) {
            obstacle.translate(-1, 0);
            if (obstacle.x + obstacle.width < 0) {
                // we add to another list and remove later
                // to avoid concurrent modification in a for-each loop
                toRemove.add(obstacle);
            }

            if (obstacle.intersects(birb)) {
                gameOver = true;
            }
        }

        obstacles.removeAll(toRemove);

        // add new obstacle for every one that was removed
        for (int i = 0; i < toRemove.size(); ++i) {
            Dimension d = getSize();
            addObstacles(d.width, d.height);
        }

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
        final int maxHeight = this.getSize().height - birb.height - 10;
        final int kc = e.getKeyCode();

        if (kc == KeyEvent.VK_UP && birb.y > minHeight) {
            birb.translate(0, -10);
        }
        else if (kc == KeyEvent.VK_DOWN && birb.y < maxHeight) {
            birb.translate(0, 10);
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
