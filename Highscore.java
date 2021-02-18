import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

public class Highscore {
    private String name;
    private int score;

    public Highscore(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public static void writeScoresToFile(List<Highscore> highscores, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Highscore hs : highscores) {
                writer.write(hs.getName() + " " + hs.getScore() + "\n");
            }

        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    public static void checkHighscore(List<Highscore> highscores, int score, boolean hardMode, HighscoreComparator hsc) {
        String highScoreName;

        if (highscores.size() < 10) {
            do {
                highScoreName = JOptionPane.showInputDialog("You made it to the highscore list! Enter your name: (1-8 characters)");
                highScoreName = highScoreName.trim();

            } while (highScoreName.length() == 0 || highScoreName.length() > 8);

            highscores.add(new Highscore(highScoreName, score));
            Collections.sort(highscores, hsc);
            if (hardMode) {
                writeScoresToFile(highscores, "highscore-hard.txt");
            } else {
                writeScoresToFile(highscores, "highscore.txt");
            }
        } else if (highscores.size() == 10) {
            boolean change = false;

            for (Highscore hss : highscores) {
                if (score > hss.getScore()) {
                    change = true;
                    break;
                }
            }

            if (change) {
                highScoreName = JOptionPane.showInputDialog("You made it to the highscore list! Enter your name:");
                highscores.remove(highscores.size() - 1);
                highscores.add(new Highscore(highScoreName, score));

                Collections.sort(highscores, hsc);

                if (hardMode) {
                    writeScoresToFile(highscores, "highscore-hard.txt");
                } else {
                    writeScoresToFile(highscores, "highscore.txt");
                }
            }
        }
    }

    @Override
    public String toString() {
        return name + ": " + score;
    }

}