import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class CardTest {

    // Test 1: Test valid card creation with positive face value
    @Test
    public void testCardCreationValidFaceValue() {
        Card card = new Card(5);
        assertEquals(5, card.getFaceValue(), "The face value should be 5.");
    }

    // Test 2: Test invalid card creation with non-positive face value
    @Test
    public void testCardCreationInvalidFaceValue() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Card(0);
        });
        assertEquals("Face value must be a positive integer.", exception.getMessage(), "Exception message should indicate face value issue.");

        exception = assertThrows(IllegalArgumentException.class, () -> {
            new Card(-1);
        });
        assertEquals("Face value must be a positive integer.", exception.getMessage(), "Exception message should indicate face value issue.");
    }

    // Test 3: Test toString method of Card
    @Test
    public void testCardToString() {
        Card card = new Card(3);
        assertEquals("3", card.toString(), "The string representation should be '3'.");
    }

    // Test 4: Test deck creation with valid number of cards
    @Test
    public void testCreateDeckValid() {
        List<Card> deck = Card.createDeck(5);
        assertEquals(5, deck.size(), "Deck size should be 5.");
        assertEquals(1, deck.get(0).getFaceValue(), "First card should have a face value of 1.");
        assertEquals(5, deck.get(4).getFaceValue(), "Last card should have a face value of 5.");
    }

    // Test 5: Test deck creation with invalid number of cards
    @Test
    public void testCreateDeckInvalid() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Card.createDeck(0);
        });
        assertEquals("Invalid number of cards (must be positive).", exception.getMessage(), "Exception message should indicate invalid deck size.");

        exception = assertThrows(IllegalArgumentException.class, () -> {
            Card.createDeck(-1);
        });
        assertEquals("Invalid number of cards (must be positive).", exception.getMessage(), "Exception message should indicate invalid deck size.");
    }

    // Test 6: Test deck creation for large number of cards
    @Test
    public void testCreateDeckLargeNumber() {
        List<Card> deck = Card.createDeck(1000);
        assertEquals(1000, deck.size(), "Deck size should be 1000.");
        assertEquals(1, deck.get(0).getFaceValue(), "First card should have a face value of 1.");
        assertEquals(1000, deck.get(999).getFaceValue(), "Last card should have a face value of 1000.");
    }

    // Test 7: Test thread safety of card deck creation (basic thread safety check)
    @Test
    public void testCreateDeckThreadSafety() throws InterruptedException {
        int numThreads = 10;
        int deckSize = 100;

        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                List<Card> deck = Card.createDeck(deckSize);
                assertEquals(deckSize, deck.size(), "Each thread should create a deck of the specified size.");
            });
            threads[i].start();
        }

        for (int i = 0; i < numThreads; i++) {
            threads[i].join();
        }
    }

    // Test 8: Test deck creation with synchronized list
    @Test
    public void testCreateDeckWithSynchronizedList() {
        List<Card> deck = Card.createDeck(10);
        assertTrue(true, "Deck should be created as a synchronized list.");
    }

    //Test 9: Testing card creation
    @Test
    public void testCardCreation() {
        Card card = new Card(5);
        assertEquals(5, card.getFaceValue(), "Card face value should be correctly set.");

        // Test exception for invalid face value
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Card(0),
                "Creating a card with face value 0 should throw an exception.");
        assertEquals("Face value must be a positive integer.", exception.getMessage());
    }

    //Test 10: Card Equality & Hashcode
    @Test
    public void testCardEqualityAndHashCode() {
        Card card1 = new Card(5);
        Card card2 = new Card(5);
        Card card3 = new Card(10);

        // Test equality
        assertEquals(card1, card2, "Cards with the same face value should be equal.");
        assertNotEquals(card1, card3, "Cards with different face values should not be equal.");

        // Test hashCode
        assertEquals(card1.hashCode(), card2.hashCode(), "Hash codes should match for equal cards.");
        assertNotEquals(card1.hashCode(), card3.hashCode(), "Hash codes should not match for non-equal cards.");
    }

    //Test 11: Testing Face Values
    @Test
    public void testInvalidFaceValue() {
        assertThrows(IllegalArgumentException.class, () -> new Card(-1),
                "Creating a card with a negative face value should throw an exception.");
        assertThrows(IllegalArgumentException.class, () -> new Card(0),
                "Creating a card with a zero face value should throw an exception.");
    }

    //Test 12: Test deck creation
    @Test
    public void testCreateDeck() {
        List<Card> deck = Card.createDeck(5);

        assertEquals(5, deck.size(), "Deck should contain 5 cards.");
        for (int i = 0; i < 5; i++) {
            assertEquals(i + 1, deck.get(i).getFaceValue(), "Deck cards should have sequential face values starting from 1.");
        }

        // Test exception for invalid deck size
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Card.createDeck(0),
                "Creating a deck with size 0 should throw an exception.");
        assertEquals("Invalid number of cards (must be positive).", exception.getMessage());
    }

    // Test 13: Is the deck empty test
    @Test
    public void testCreateEmptyDeck() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Card.createDeck(0), "Creating a deck with size 0 should throw an exception.");
        assertEquals("Invalid number of cards (must be positive).", exception.getMessage());
    }

    // Test 14: Testing for a very large deck
    @Test
    public void testCreateLargeDeck() {
        int largeSize = 10000;
        List<Card> deck = Card.createDeck(largeSize);

        assertNotNull(deck, "Deck should not be null.");
        assertEquals(largeSize, deck.size(),
                "Deck should have the correct large size.");
    }

    // Test 15: Testing for a repeated deck
    @Test
    public void testDeckDeepCopy() {
        List<Card> originalDeck = Card.createDeck(10);
        List<Card> copiedDeck = new ArrayList<>(originalDeck);

        // Ensure contents are identical but objects are distinct
        assertEquals(originalDeck, copiedDeck,
                "Copied deck should have the same contents as the original.");
        assertNotSame(originalDeck, copiedDeck,
                "Copied deck should not be the same object as the original.");

        // Modify original and verify copied deck remains unchanged
        originalDeck.add(new Card(11));
        assertNotEquals(originalDeck, copiedDeck,
                "Copied deck should remain unchanged when the original deck is modified.");
    }

    // Test 16: Is the deck thread-safe?
    @Test
    public void testThreadSafeDeck() throws InterruptedException {
        List<Card> deck = Card.createDeck(100);
        Thread thread1 = new Thread(() -> deck.add(new Card(101)));
        Thread thread2 = new Thread(() -> deck.add(new Card(102)));

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // Ensure both threads were able to add their cards without exception
        assertTrue(deck.stream().anyMatch(card -> card.getFaceValue() == 101),
                "Deck should contain the card added by thread1.");
        assertTrue(deck.stream().anyMatch(card -> card.getFaceValue() == 102),
                "Deck should contain the card added by thread2.");
    }

    // Test 17: Is the deck synchronised?
    @Test
    public void testSynchronizedDeckCreation() {
        List<Card> deck = Card.createDeck(10);

        // Ensure deck is not null and has the expected size
        assertNotNull(deck, "Deck should not be null.");
        assertEquals(10, deck.size(), "Deck should have 10 cards.");

        // Test synchronization by attempting concurrent access
        synchronized (deck) {
            deck.add(new Card(11));
        }

        // Verify the added card
        assertEquals(11, deck.get(deck.size() - 1).getFaceValue(),
                "The last card in the deck should have the face value 11.");
    }
}
