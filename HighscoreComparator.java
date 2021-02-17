import java.util.Comparator;

public class HighscoreComparator implements Comparator<Highscore>{

    @Override
    public int compare(Highscore o1, Highscore o2) {
        return o2.getScore() - o1.getScore();
    }
}
