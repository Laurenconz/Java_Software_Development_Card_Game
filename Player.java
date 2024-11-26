import java.util.List;
import java.util.Random; 
import java.util.concurrent.atomic.AtomicBoolean; 
import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.ArrayList;

/**
 * Player class represents a player in the card game.
 * Handles drawing, discarding cards, and checking win conditions in a multithreaded environment.
 */
public class Player implements Runnable {
    private int playerID;
    private int preferredDenomination;
    private List<Integer> hand;
    private Stack<Integer> leftDeck;
    private Stack<Integer> rightDeck;
    private CardGame game;
    private int n;
    private AtomicBoolean gameWon;
    private Random random = new Random();
    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();

    public Player(int playerID, List<Integer> hand, List<Integer> deck, Stack<Integer> leftDeck, Stack<Integer> rightDeck, CardGame game, int n, AtomicBoolean gameWon) {
        this.playerID = playerID;
        this.hand = hand;
        this.preferredDenomination = playerID; // Set preferred denomination to player ID
        this.leftDeck = leftDeck;
        this.rightDeck = rightDeck;
        this.game = game;
        this.n = n;
        this.gameWon = gameWon;

        // Log and print initial hand
        String initialHand = "Player " + (playerID + 1) + " initial hand: " + formatHand();
        System.out.println(initialHand);
        game.writePlayerFile(playerID, initialHand);
    }

    // Draws a card from the left deck, waiting if necessary
    private Integer drawCard() throws InterruptedException {
        lock.lock();
        try {
            while (leftDeck.isEmpty() && !gameWon.get()) {
                notEmpty.await();
            }
            if (!gameWon.get() && !leftDeck.isEmpty()) {
                Integer drawnCard = leftDeck.pop();
                hand.add(drawnCard);
                return drawnCard;
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    // Discards a random non-preferred card to the right deck
    private Integer discardCard() {
        lock.lock();
        try {
            if (hand.isEmpty()) {
                return null; // No cards to discard
            }

            // Filter cards to find non-preferred ones
            List<Integer> nonPreferredCards = new ArrayList<>();
            for (Integer card : hand) {
                if (card != preferredDenomination) {
                    nonPreferredCards.add(card);
                }
            }

            // If no non-preferred cards are found, player has a full preferred hand
            if (nonPreferredCards.isEmpty()) {
                return null;
            }

            // Randomly discard a card from the non-preferred list
            Integer discardedCard = nonPreferredCards.get(random.nextInt(nonPreferredCards.size()));
            hand.remove(discardedCard);
            rightDeck.push(discardedCard);
            notEmpty.signalAll(); // Notify waiting threads
            return discardedCard;
        } finally {
            lock.unlock();
        }
    }

    // Checks if the player has a winning hand
    private boolean winner() {
        if (hand.size() != 4) {
            return false; // A valid hand must have exactly 4 cards
        }
        int firstCard = hand.get(0); // Get the first card in the hand
        for (int card : hand) {
            if (card != firstCard) {
                return false; // If any card is different, the player is not a winner
            }
        }
        return true; // All cards are the same
    }

    // Formats the player's hand for logging
    private String formatHand() {
        return hand.toString();
    }

    // Executes the player's draw and discard actions
    private void drawAndDiscard() throws InterruptedException {
        // Perform the draw action
        Integer drawnCard = drawCard();
        if (drawnCard != null) {
            String drawMessage = "Player " + (playerID + 1) + " draws " + drawnCard + " from deck " + (playerID + 1);
            System.out.println(drawMessage); 
            game.writePlayerFile(playerID, drawMessage);
        }

        // Perform the discard action
        Integer discardedCard = discardCard();
        if (discardedCard != null) {
            String discardMessage = "Player " + (playerID + 1) + " discards " + discardedCard + " to deck " + ((playerID + 1) % n + 1);
            System.out.println(discardMessage); 
            game.writePlayerFile(playerID, discardMessage);
            game.logCurrentHand(playerID, hand); // Log the current hand after discarding

            // Check for a win after discarding
            if (winner()) {
                String winMessage = "Player " + (playerID + 1) + " wins!";
                System.out.println(winMessage); // Print to console
                game.writePlayerFile(playerID, winMessage);
                gameWon.set(true);
            }

        }
    }

    // The player's main execution loop
    @Override
    public void run() {
        while (!gameWon.get() && !Thread.currentThread().isInterrupted()) {
            try {
                drawAndDiscard();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Gracefully handle interruption
                break;
            }
        }

        // Final logging for this player
        if (gameWon.get()) {
            String finalMessage;
            if (winner()) {
                finalMessage = "Player " + (playerID + 1) + " wins! Final hand: " + formatHand();
            } else {
                finalMessage = "Player " + (playerID + 1) + " exits after another player's win. Final hand: " + formatHand();
            }
            System.out.println(finalMessage); // Print to console
            game.writePlayerFile(playerID, finalMessage);
        }
    }
}
