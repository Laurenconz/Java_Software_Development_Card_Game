import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CardGame class. Handles the initialization and execution of a multiplayer card game.
 */
public class CardGame {

    private int n; // Number of players
    private String packFilePath; // Path to the pack file

    public CardGame(int n, String packFilePath) {
        this.n = n;
        this.packFilePath = packFilePath;
    }

    // Main method: tries command line, plays the game
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter the number of players: ");
            int n = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            System.out.print("Enter the pack file path: ");
            String packFilePath = scanner.nextLine();

            CardGame game = new CardGame(n, packFilePath);
            game.startGame(); // Start the game
        }
    }

    // Unified method for writing to a file
    private void writeToFile(String fileName, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(content);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to file " + fileName + ": " + e.getMessage());
        }
    }

    // Writes output to a player file
    public void writePlayerFile(int playerIndex, String content) {
        String fileName = "player" + (playerIndex + 1) + "_output.txt";
        writeToFile(fileName, content);
    }

    // Logs the current state of a player's hand
    public void logCurrentHand(int playerIndex, List<Integer> hand) {
        String content = "Current hand for player " + (playerIndex + 1) + ": " + hand.toString();
        System.out.println(content);
        writePlayerFile(playerIndex, content);
    }

    // Logs final deck states
    public void logFinalDecks(List<List<Integer>> decks) {
        for (int i = 0; i < decks.size(); i++) {
            StringBuilder content = new StringBuilder("Final contents of deck " + (i + 1) + ": ");
            for (Integer card : decks.get(i)) {
                content.append(card).append(" ");
            }
            writeToFile("deck" + (i + 1) + "_output.txt", content.toString());
        }
    }

    // Playing game method: initializes pack, cards, players, decks, and handles gameplay
    public void startGame() {
        while (true) {
            try {
                List<Integer> cards = loadPackFile();
                int expectedCardCount = 8 * n;
                if (cards.size() != expectedCardCount) {
                    throw new IOException("Invalid number of cards. The pack must contain exactly " + expectedCardCount + " cards.");
                }

                System.out.println("Pack successfully loaded with " + cards.size() + " cards.");

                // Distribute cards into player hands and decks
                List<List<Integer>> hands = distributeHands(cards);
                List<List<Integer>> decks = distributeDecks(cards);

                // Check for immediate win
                for (int i = 0; i < n; i++) {
                    if (immediateWin(hands.get(i))) {
                        System.out.println("Player " + (i + 1) + " Wins!");
                        writePlayerFile(i, "Player " + (i + 1) + " wins with an immediate win!");
                        logFinalDecks(decks);
                        return;
                    }
                }

                // Run the main game loop
                boolean gameWon = runGame(hands, decks);
                if (gameWon) {
                    logFinalDecks(decks); // Ensure decks are logged after the game ends
                    return;
                }

            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
                System.out.println("Restarting game due to error...");
            }
        }
    }

    // Loads the pack file and validates its contents
    private List<Integer> loadPackFile() throws IOException {
        List<Integer> cards = new ArrayList<>();
        try (Scanner scanfile = new Scanner(new File(packFilePath))) {
            while (scanfile.hasNext()) {
                if (scanfile.hasNextInt()) {
                    cards.add(scanfile.nextInt());
                } else {
                    throw new IOException("Invalid File: Contains non-integer values.");
                }
            }
        }
        return cards;
    }

    // Distributes cards into player hands
    public List<List<Integer>> distributeHands(List<Integer> cards) {
        List<List<Integer>> playerHands = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            playerHands.add(new ArrayList<>());
        }

        for (int i = 0; i < 4 * n; i++) {
            int playerIndex = i % n;
            playerHands.get(playerIndex).add(cards.get(i));
        }

        cards.subList(0, 4 * n).clear(); // Remove cards dealt to hands
        return playerHands;
    }

    // Distributes remaining cards into decks
    public List<List<Integer>> distributeDecks(List<Integer> cards) {
        List<List<Integer>> decks = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            decks.add(new ArrayList<>());
        }

        for (int i = 0; i < cards.size(); i++) {
            int playerIndex = i % n;
            decks.get(playerIndex).add(cards.get(i));
        }
        return decks;
    }

    // Checks for an immediate win condition
    public boolean immediateWin(List<Integer> playerHand) {
        return playerHand.size() == 4 && playerHand.stream().distinct().count() == 1;
    }

    // Runs the main game loop
    private boolean runGame(List<List<Integer>> hands, List<List<Integer>> decks) {
        List<Thread> playerThreads = new ArrayList<>();
        AtomicBoolean gameWon = new AtomicBoolean(false);

        // Initialize and start threads for players
        for (int i = 0; i < n; i++) {
            Stack<Integer> leftDeck = new Stack<>();
            Stack<Integer> rightDeck = new Stack<>();

            leftDeck.addAll(decks.get((i - 1 + n) % n));
            rightDeck.addAll(decks.get((i + 1) % n));

            Player player = new Player(i, hands.get(i), decks.get(i), leftDeck, rightDeck, this, n, gameWon);
            Thread playerThread = new Thread(player);
            playerThreads.add(playerThread);
            playerThread.start();

            writePlayerFile(i, "Starting hand for player " + (i + 1) + ": " + hands.get(i));
            logCurrentHand(i, hands.get(i));
        }

        // Monitor game status
        while (!gameWon.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Main game loop interrupted.");
                break;
            }
        }

        // Interrupt and join all threads
        for (Thread thread : playerThreads) {
            if (thread.isAlive()) thread.interrupt();
        }

        for (Thread thread : playerThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted while waiting for termination: " + e.getMessage());
            }
        }

        return gameWon.get();
    }
}
