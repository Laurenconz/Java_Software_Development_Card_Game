import java.util.LinkedList;
import java.util.Queue;

public class CardDeck {
    private final Queue<Integer> cards = new LinkedList<>();

    public synchronized void addCard(Integer card) {
        cards.add(card);
    }

    public synchronized Integer drawCard() {
        return cards.poll();
    }

    public synchronized boolean isEmpty() {
        return cards.isEmpty();
    }

    @Override
    public synchronized String toString() {
        return "Deck: " + cards;
    }
}
