import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creating a thread-safe card class.
 * Each card has a face value representing its denomination.
 * Assigns positive integers from 1 to n to each card (thread-safe).
 * Use to string method to convert them into string 
 * 
 * @author 730093467 & 730034362
 * @version 1.0
 */

public class Card {
    private final int faceValue;

    public Card(int faceValue) {
        if (faceValue <= 0) {
            throw new IllegalArgumentException("Face value must be a positive integer.");
        }
        this.faceValue = faceValue;
    }

    public int getFaceValue() {
        return faceValue;
    }

    @Override
    public String toString() {
        return "Card face value: " + faceValue;
    }

    public static List<Card> createDeck(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Invalid number of cards (must be positive).");
        }

        AtomicInteger counter = new AtomicInteger(1);
        List<Card> deck = Collections.synchronizedList(new ArrayList<>(n));

        for (int i = 0; i < n; i++) {
            deck.add(new Card(counter.getAndIncrement()));
        }

        return deck;
    }

}
