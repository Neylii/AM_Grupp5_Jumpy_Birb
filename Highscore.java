import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Highscore {

    private String name;
    private int score;
    private static String fileName;

    public Highscore(String name, int score) {
        this.name = name;
        this.score = score;
        fileName = "highscore.txt";
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public static void writeScoresToFile(List<Highscore> highscores) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Highscore hs : highscores) {
                writer.write(hs.toString());
            }

        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    @Override
    public String toString() {
        return name + ": " + score + "\n";
    }

}