import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.ArrayList;

public class Cloud {
    private int x;
    private int y;
    private int speed;
    private BufferedImage cloudPic;
    static List<Cloud> toRemoveClouds;

    Cloud(int x, int y, String cloudPath, int speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;

        toRemoveClouds = new ArrayList<>();
        
        try {
            cloudPic = ImageIO.read(new File(cloudPath));
        } catch (IOException ex) {
        }

    }

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public BufferedImage getCloudPic() {
		return cloudPic;
	}

	public int getSpeed() {
		return speed;
	}

    public static int moveCloud(List<Cloud> clouds) {
        int maxCloud_x = 0;
        for (Cloud cloud : clouds) {

            cloud.setX(cloud.getX() + cloud.getSpeed());

            if (maxCloud_x < cloud.getX()) {
                maxCloud_x = cloud.getX();
            }

            if (cloud.getX() + 200 < 0) {
                toRemoveClouds.add(cloud);
            }
        }
        return maxCloud_x;
    }

    public static void addCloud(List<Cloud> clouds, int speed) {
        int randomCloudHeight = ThreadLocalRandom.current().nextInt(150, 500);
        if (Math.random() > 0.5) {
            clouds.add(new Cloud(800, randomCloudHeight, "images/smallCloud.png", speed));
        } else {
            clouds.add(new Cloud(800, randomCloudHeight, "images/bigCloud.png", speed));
        }
    }

	public static List<Cloud> getToRemoveClouds() {
		return toRemoveClouds;
	}
}
