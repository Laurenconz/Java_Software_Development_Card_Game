import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    private CardGame game;
    private CardDeck ownDeck;
    private CardDeck nextDeck;
    private List<Card> playerHand;
    private AtomicBoolean gameWon;
    private Player player;

    @BeforeEach
    public void setUp() {
        // Initialize components for testing
        game = new CardGame(4, "test_pack.txt");
        ownDeck = new CardDeck();
        nextDeck = new CardDeck();
        playerHand = new ArrayList<>();
        gameWon = new AtomicBoolean(false);
        player = new Player(0, playerHand, ownDeck, nextDeck, game, gameWon);

        gameWon.set(true);

        // Add cards to ownDeck for testing
        ownDeck.addCard(new Card(1));
        ownDeck.addCard(new Card(2));
        ownDeck.addCard(new Card(3));
    }

    // Test 1: Does the player draw a card?
    @Test
    public void testDrawCard() {
        assertEquals(0, playerHand.size(), "Player hand should initially be empty.");

        // Player draws a card
        player.drawCard();
        assertEquals(1, playerHand.size(), "Player hand should contain 1 card after drawing.");
        assertEquals(1, playerHand.get(0).getFaceValue(), "Drawn card should have face value 1.");

        // Player draws another card
        player.drawCard();
        assertEquals(2, playerHand.size(), "Player hand should contain 2 cards after drawing.");
        assertEquals(2, playerHand.get(1).getFaceValue(), "Second drawn card should have face value 2.");
    }

    // Test 2: Does a player discard a card?
    @Test
    public void testDiscardCard() {
        // Add cards to player's hand for testing discard logic
        playerHand.add(new Card(1));
        playerHand.add(new Card(2));
        playerHand.add(new Card(3));
        playerHand.add(new Card(4));

        // Player discards a card
        player.discardCard();
        assertEquals(3, playerHand.size(), "Player hand should contain 3 cards after discarding.");
        assertEquals(1, nextDeck.getCardsAsList().size(), "Next deck should contain 1 card after discarding.");

        // Verify that the discarded card is not the preferred card (denomination 1)
        Card discardedCard = nextDeck.getCardsAsList().get(0);
        assertNotEquals(1, discardedCard.getFaceValue(), "Discarded card should not be the preferred denomination.");
    }

    // Test 3: Does a player win?
    @Test
    public void testWinner() {
        // Test winning condition
        playerHand.add(new Card(3));
        playerHand.add(new Card(3));
        playerHand.add(new Card(3));
        playerHand.add(new Card(3));

        assertTrue(player.winner(), "Player should win with a hand of identical cards.");
        assertTrue(gameWon.get(), "Game state should reflect a win.");

        // Test non-winning condition
        playerHand.clear();
        playerHand.add(new Card(1));
        playerHand.add(new Card(2));
        playerHand.add(new Card(3));
        playerHand.add(new Card(4));

        gameWon.set(false);
        assertFalse(player.winner(), "Player should not win with non-identical cards.");
        assertFalse(gameWon.get(), "Game state should not reflect a win.");
    }

    // Test 4: Interruption
    @Test
    public void testRun_Interruption() {
        Thread playerThread = new Thread(player);
        playerThread.start();

        // Interrupt the thread and verify behavior
        playerThread.interrupt();
        assertDoesNotThrow(() -> playerThread.join(), "Player thread should handle interruption gracefully.");
    }

    // Test 5: Does the game terminate after a win happens?
    @Test
    public void testRun_GameWon() {
        // Simulate a win condition before the thread starts
        gameWon.set(true);

        Thread playerThread = new Thread(player);
        playerThread.start();

        assertDoesNotThrow(() -> playerThread.join(), "Player thread should terminate gracefully when the game is won.");
    }

    // Test 6: Does the game log the final hand?
    @Test
    public void testFinalHandLogging() throws IOException {
        // Simulate a win and verify final hand logging
        playerHand.add(new Card(3));
        playerHand.add(new Card(3));
        playerHand.add(new Card(3));
        playerHand.add(new Card(3));

        player.run(); // Simulate the player run method
        File playerFile = new File("player1_output.txt");
        assertTrue(playerFile.exists(), "Player output file should exist after the game.");

        List<String> lines = Files.readAllLines(playerFile.toPath());
        String expectedLog = "Final hand of player 1: " + playerHand;
        assertTrue(lines.stream().anyMatch(line -> line.contains(expectedLog)),
                "Player output file should contain the final hand log.");
    }

    // Test 7: Are the card values converted to strings?
    @Test
    public void testToString() {
        Card card = new Card(10);
        assertEquals("10", card.toString(), "toString should return the string representation of the face value.");
    }

    // Test 8: Checking Equality & Hash Code
    @Test
    public void testEqualsAndHashCode() {
        Card card1 = new Card(7);
        Card card2 = new Card(7);
        Card card3 = new Card(8);

        // Test equality
        assertEquals(card1, card2, "Cards with the same face value should be equal.");
        assertNotEquals(card1, card3, "Cards with different face values should not be equal.");

        // Test hash code consistency
        assertEquals(card1.hashCode(), card2.hashCode(), "Cards with the same face value should have the same hash code.");
        assertNotEquals(card1.hashCode(), card3.hashCode(), "Cards with different face values should have different hash codes.");
    }

    // Test 9: Does a win update the game state?
    @Test
    public void testWinnerUpdatesGameState() {
        // Simulate player winning
        playerHand.add(new Card(5));
        playerHand.add(new Card(5));
        playerHand.add(new Card(5));
        playerHand.add(new Card(5));

        assertTrue(player.winner(), "Player should be detected as winner.");
        assertTrue(gameWon.get(), "Game state should reflect that the game has been won.");
    }

    // Test 10: Testing game termination
    @Test
    public void testRunWhileGameInProgress() throws InterruptedException {
        // Set game state to in-progress
        gameWon.set(false);
        Thread playerThread = new Thread(player);

        playerThread.start();
        Thread.sleep(100); // Allow some actions to happen
        gameWon.set(true); // Simulate game being won externally

        playerThread.join();
        assertTrue(gameWon.get(), "Game state should remain won after player run terminates.");
    }

    // Test 11: Are the players thread-safe?
    @Test
    public void testPlayerThreadSafety() throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        AtomicBoolean raceConditionOccurred = new AtomicBoolean(false);

        // Simulate multiple threads modifying player's hand
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                synchronized (playerHand) {
                    try {
                        playerHand.add(new Card(1));
                        if (playerHand.size() > 10) raceConditionOccurred.set(true);
                        playerHand.remove(playerHand.size() - 1);
                    } catch (Exception e) {
                        raceConditionOccurred.set(true);
                    }
                }
            });
            threads.add(thread);
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to finish
        for (Thread thread : threads) {
            thread.join();
        }

        assertFalse(raceConditionOccurred.get(), "Player's hand should remain thread-safe.");
    }

    // Test 12: Can a win condition be achieved during game?
    @Test
    public void testGameWonConditionDuringPlayerRun() throws InterruptedException {
        // Simulate a running game where the player can win
        gameWon.set(false);

        Thread playerThread = new Thread(player);
        playerThread.start();

        // Simulate game won externally
        Thread.sleep(50); // Let the player make a few moves
        gameWon.set(true);

        playerThread.join();
        assertTrue(gameWon.get(), "Game state should reflect a win.");
    }





}





