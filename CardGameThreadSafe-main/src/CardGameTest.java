import org.junit.jupiter.api.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CardGameTest {

    private static final String TEMP_PACK_FILE = "test_pack.txt";
    private CardGame cardGame;

    @BeforeEach
    public void setUp() throws IOException {
        createPackFile(32); // 8 cards per player for 4 players
        cardGame = new CardGame(4, TEMP_PACK_FILE);
    }

    @AfterEach
    public void cleanUp() {
        // Delete pack file
        new File(TEMP_PACK_FILE).delete();

        // Delete output files
        for (int i = 0; i < 4; i++) {
            new File("player" + (i + 1) + "_output.txt").delete();
            new File("deck" + (i + 1) + "_output.txt").delete();
        }
    }

    public void createPackFile(int cardCount) throws IOException {
        List<String> cardValues = new ArrayList<>();
        for (int i = 1; i <= cardCount; i++) {
            cardValues.add(String.valueOf(i));
        }
        Files.write(new File(TEMP_PACK_FILE).toPath(), cardValues);
    }

    // Test 1: Does the game log the current hand of players?
    @Test
    public void testLogCurrentHand() {
        List<Card> hand = List.of(new Card(2), new Card(3), new Card(4), new Card(5));
        cardGame.logCurrentHand(0, hand);

        File playerFile = new File("player1_output.txt");
        assertTrue(playerFile.exists(), "Player file should be created.");
        assertDoesNotThrow(() -> {
            List<String> lines = Files.readAllLines(playerFile.toPath());
            assertTrue(lines.get(0).contains("Current hand for player 1: [2, 3, 4, 5]"),
                    "Logged hand should match the expected content.");
        });
    }

    // Test 2: Does the game continue without a winner?
    @Test
    public void testRunGameCompletesWithoutWinner() throws IOException {
        createPackFile(32); // Ensure no immediate winners in the pack
        cardGame = new CardGame(4, TEMP_PACK_FILE);

        // Simulate a short run of the game
        Thread gameThread = new Thread(() -> assertDoesNotThrow(cardGame::startGame, "Game should run without errors."));
        gameThread.start();
        try {
            Thread.sleep(2000); // Let the game run briefly
            gameThread.interrupt();
            gameThread.join();
        } catch (InterruptedException e) {
            fail("Test thread was interrupted unexpectedly.");
        }

        // Verify that the game terminated gracefully
        assertFalse(cardGame.sharedDecks.isEmpty(), "Shared decks should still contain cards.");
    }

    // Test 3: How does the game handle an invalid card in pack?
    @Test
    public void testInvalidCardInPackFile() throws IOException {
        // Create a pack file with an invalid card value
        Files.write(new File(TEMP_PACK_FILE).toPath(), List.of("1", "2", "three", "4"));
        cardGame = new CardGame(4, TEMP_PACK_FILE);

        assertThrows(IOException.class, cardGame::loadPackFile, "Pack file with invalid values should throw IOException.");
    }

    // Test 4: How does the game handle an incomplete pack file
    @Test
    public void testIncompletePackFile() throws IOException {
        // Create a pack file with fewer than the required cards
        Files.write(new File(TEMP_PACK_FILE).toPath(), List.of("1", "2", "3", "4")); // Only 4 cards
        cardGame = new CardGame(4, TEMP_PACK_FILE); // 4 players, 32 cards needed

        // Capture any errors during game start
        assertDoesNotThrow(() -> cardGame.startGame(), "The game should handle incomplete pack files gracefully.");

        // Verify that no output files are created, as the game should not proceed
        for (int i = 1; i <= 4; i++) {
            File playerFile = new File("player" + i + "_output.txt");
            File deckFile = new File("deck" + i + "_output.txt");

            assertFalse(playerFile.exists(), "Player output files should not be created if the pack file is incomplete.");
            assertFalse(deckFile.exists(), "Deck output files should not be created if the pack file is incomplete.");
        }
    }

    // Test 5: How does the game work with multiple winners?
    @Test
    public void testMultipleWinnersDetected() throws IOException {
        // Create a pack file designed to cause multiple immediate winners
        Files.write(new File(TEMP_PACK_FILE).toPath(), List.of(
                "1", "1", "1", "1", "2", "2", "2", "2",
                "3", "3", "3", "3", "4", "4", "4", "4"
        ));
        cardGame = new CardGame(4, TEMP_PACK_FILE);

        assertDoesNotThrow(cardGame::startGame, "Game should start and identify multiple winners.");
    }

    // Test 6: Do output files clear after each game?
    @Test
    public void testFileDeletionOnClearOutputFiles() {
        // Create dummy files to simulate player and deck outputs
        for (int i = 1; i <= 4; i++) {
            File playerFile = new File("player" + i + "_output.txt");
            File deckFile = new File("deck" + i + "_output.txt");
            try {
                Files.writeString(playerFile.toPath(), "Dummy content");
                Files.writeString(deckFile.toPath(), "Dummy content");
            } catch (IOException e) {
                fail("Setup failed to create dummy files for testing.");
            }
        }

        // Call the method to clear output files
        assertDoesNotThrow(cardGame::clearOutputFiles, "Clearing output files should not throw exceptions.");

        // Verify that the files have been deleted
        for (int i = 1; i <= 4; i++) {
            assertFalse(new File("player" + i + "_output.txt").exists(), "Player output file should be deleted.");
            assertFalse(new File("deck" + i + "_output.txt").exists(), "Deck output file should be deleted.");
        }
    }
}
