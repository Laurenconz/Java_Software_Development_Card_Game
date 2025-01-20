import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.jupiter.api.Test;

public class CardDeck {
    private final ConcurrentLinkedQueue<Card> cards = new ConcurrentLinkedQueue<>(); //uses card

    public synchronized void addCard(Card card) {
        cards.add(card); //add card to the bottom
    }

    public synchronized Card drawCard() {
        return cards.poll(); //remove and return the top card 
    }

    public synchronized boolean isEmpty() {
        return cards.isEmpty();
    }

    @Override
    public synchronized String toString() {
        return "Deck: " + cards;
    }
    public void offer(Card card) {
        cards.offer(card); // Delegate to the internal queue
    }
    public List<Card> getCardsAsList() {
        return new ArrayList<>(cards); // Convert the internal queue to a list
    }
}
