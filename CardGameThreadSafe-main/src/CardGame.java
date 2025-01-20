import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

public class CardGame {

    public final int n; // Number of players
    private final String packFilePath; // Path to the pack file
    private final AtomicBoolean gameWon = new AtomicBoolean(false); // Tracks game status
    final List<CardDeck> sharedDecks = new ArrayList<>();

    public CardGame(int n, String packFilePath) {
        this.n = n;
        this.packFilePath = packFilePath;

        // Initialize shared decks for cyclic sharing
        for (int i = 0; i < n; i++) {
            sharedDecks.add(new CardDeck());
        }
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter the number of players: ");
            int n = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            if (n <= 1) {
                System.err.println("Number of players must be greater than 1.");
                return;
            }

            System.out.print("Enter the pack file path: ");
            String packFilePath = scanner.nextLine();

            File file = new File(packFilePath);
            if (!file.exists() || !file.isFile()) {
                System.err.println("Pack file does not exist or is not valid.");
                return;
            }

            CardGame game = new CardGame(n, packFilePath);
            game.startGame(); // Start the game
        }
    }
    public void logCurrentHand(int playerIndex, List<Card> hand) {
        String content = "Current hand for player " + (playerIndex + 1) + ": " + hand;
        
        // Print to console
        System.out.println(content);
        
        // Write to player's file
        writePlayerFile(playerIndex, content);
    }

    public void startGame() {
        clearOutputFiles();

        try {
            List<Card> cards = loadPackFile();
            int expectedCardCount = 8 * n; // 4 cards per hand + 4 cards for shared decks
            if (cards.size() < expectedCardCount) {
                throw new IOException("Invalid number of cards. The pack must contain exactly " + expectedCardCount + " cards.");
            }

            // Distribute hands and initialize shared decks
            List<List<Card>> playerHands = distributeHands(cards);
            initializeSharedDecks(cards);

            // Display initial hands and decks
            displayInitialHandsAndDecks(playerHands);

            // Check for immediate win condition
            for (int i = 0; i < n; i++) {
                if (immediateWin(playerHands.get(i))) {
                    System.out.println("Player " + (i + 1) + " immediately wins!");
                    writePlayerFile(i, "Player " + (i + 1) + " wins with an immediate win!");
                    gameWon.set(true);
                }
            }

            // Start the game
            runGame(playerHands);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }


    List<Card> loadPackFile() throws IOException {
        List<Card> cards = new ArrayList<>();
        try (Scanner scanfile = new Scanner(new File(packFilePath))) {
            while (scanfile.hasNext()) {
                if (scanfile.hasNextInt()) {
                    int cardValue = scanfile.nextInt();
                    cards.add(new Card(cardValue)); // Create a Card object and add it
                } else {
                    throw new IOException("Invalid File: Contains non-integer values.");
                }
            }
        }
        return cards;
    }
    

    List<List<Card>> distributeHands(List<Card> cards) {
        List<List<Card>> playerHands = new ArrayList<>();

        // Initialize hands
        for (int i = 0; i < n; i++) {
            playerHands.add(new ArrayList<>());
        }

        // Distribute first 4 * n cards to player hands
        for (int i = 0; i < 4 * n; i++) {
            int playerIndex = i % n;
            playerHands.get(playerIndex).add(cards.get(i));
        }

        // Remove distributed cards from the pack
        cards.subList(0, 4 * n).clear();

        return playerHands;
    }

    void initializeSharedDecks(List<Card> cards) {
        // Distribute remaining cards to shared decks in a cyclic manner
        for (int i = 0; i < cards.size(); i++) {
            int deckIndex = i % n;
            sharedDecks.get(deckIndex).offer(cards.get(i));
        }
    }

    private void displayInitialHandsAndDecks(List<List<Card>> playerHands) {
        System.out.println("Initial Hands:");
        for (int i = 0; i < playerHands.size(); i++) {
            System.out.println("Player " + (i + 1) + ": " + playerHands.get(i));
        }

        
    }

    public boolean immediateWin(List<Card> playerHand) {
        return playerHand.size() == 4 && playerHand.stream().distinct().count() == 1;
    }

    private void runGame(List<List<Card>> hands) {
        List<Thread> playerThreads = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            int nextDeckIndex = (i + 1) % n; // Each player shares a deck with the next player
            Player player = new Player(i, hands.get(i), sharedDecks.get(i), sharedDecks.get(nextDeckIndex), this, gameWon);
            Thread playerThread = new Thread(player);
            playerThreads.add(playerThread);
            playerThread.start();

            writePlayerFile(i, "Starting hand for player " + (i + 1) + ": " + hands.get(i));
        }

        // Wait for a winner to be declared
        while (!gameWon.get()) {
            try {
                Thread.sleep(100); // Small delay for main thread
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Stop all player threads after a winner is found
        for (Thread thread : playerThreads) {
            thread.interrupt();
        }

        for (Thread thread : playerThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Output final deck states
        writeFinalDecks();

        System.out.println("Game over!");
    }

    void clearOutputFiles() {
        for (int i = 0; i < n; i++) {
            String playerFileName = "player" + (i + 1) + "_output.txt";
            String deckFileName = "deck" + (i + 1) + "_output.txt";
            new File(playerFileName).delete();
            new File(deckFileName).delete();
        }
    }

    public void writePlayerFile(int playerIndex, String content) {
        String fileName = "player" + (playerIndex + 1) + "_output.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(content);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to file " + fileName + ": " + e.getMessage());
        }
    }

    void writeFinalDecks() {
    for (int i = 0; i < n; i++) {
        String fileName = "deck" + (i + 1) + "_output.txt";
        List<Card> finalDeck = sharedDecks.get(i).getCardsAsList(); // Fetch cards as a list
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("Final Deck " + (i + 1) + ": " + finalDeck);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing final deck to file " + fileName + ": " + e.getMessage());
        }
    }
}

}
