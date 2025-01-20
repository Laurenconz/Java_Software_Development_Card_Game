import java.util.List;
//import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Random;
//import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Player implements Runnable {
    private final int playerID;
    private final List<Card> hand;
    private final CardDeck ownDeck;
    private final CardDeck nextDeck;
    private final CardGame game;
    private final AtomicBoolean gameWon;
    private final Random random = new Random();
    private final int preferredDenomination; // Player's preferred denomination

    public Player(int playerID, List<Card> hand, CardDeck ownDeck,
              CardDeck nextDeck, CardGame game, AtomicBoolean gameWon) {
        this.playerID = playerID;
        this.hand = hand;
        this.ownDeck = ownDeck;
        this.nextDeck = nextDeck;
        this.game = game;
        this.gameWon = gameWon;
        this.preferredDenomination = playerID + 1; // Preferred denomination is index + 1
    }

    /**
     * Draws a card from the player's deck and logs it.
     */
    Card drawCard() {
        Card drawnCard = ownDeck.drawCard();
        if (drawnCard != null) {
            hand.add(drawnCard);
            String message = "Player " + (playerID + 1) + " draws " + drawnCard + " from Deck " + (playerID + 1);
            System.out.println(message);
            game.writePlayerFile(playerID, message);
        }
        return drawnCard;
    }

    /**
     * Discards a card (if available) that is not the preferred card and logs it.
     */
    void discardCard() {
        if (hand.isEmpty()) {
            String message = "Player " + (playerID + 1) + " has no cards to discard.";
            System.out.println(message);
            game.writePlayerFile(playerID, message);
            return;
        }
    
        // Filter hand to find cards that are NOT the preferred denomination
        List<Card> nonPreferredCards = hand.stream()
                                            .filter(card -> card.getFaceValue() != preferredDenomination)
                                            .toList();
    
        if (!nonPreferredCards.isEmpty()) {
            Card discardedCard = nonPreferredCards.get(random.nextInt(nonPreferredCards.size())); // Select random card
            hand.remove(discardedCard); // Remove card from hand
            nextDeck.offer(discardedCard); // Add card to next player's deck
    
            String message = "Player " + (playerID + 1) + " discards " + discardedCard + " to Deck " + ((playerID + 1) % game.n + 1);
            System.out.println(message);
            game.writePlayerFile(playerID, message);
        } else {
            // No available card to discard, skip discard
            String message = "Player " + (playerID + 1) + " has only preferred cards and skips discard.";
            System.out.println(message);
            game.writePlayerFile(playerID, message);
        }
    }
    

    /**
     * Checks if the player has won and logs it if true.
     */
    boolean winner() {
        boolean hasWon = hand.size() == 4 && hand.stream().map(Card::getFaceValue).distinct().count() == 1;
        if (hasWon) {
            String message = "Player " + (playerID + 1) + " wins with hand: " + hand;
            System.out.println(message);
            game.writePlayerFile(playerID, message);
            gameWon.set(true);
        }
        return hasWon;
    }

    /**
     * Executes the player's turn, drawing, discarding, and checking for a win.
     */
    private void playTurn() {
        drawCard();
        discardCard();
        game.logCurrentHand(playerID, hand); // Log current hand state
        winner();
    }

    @Override
    public void run() {
        while (!gameWon.get() && !Thread.currentThread().isInterrupted()) {
            try {
                playTurn();
                Thread.sleep(100); // Simulate gameplay pace
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                String message = "Player " + (playerID + 1) + " was interrupted.";
                System.out.println(message);
                game.writePlayerFile(playerID, message);
            }
        }

        // Final logging when the game ends
        if (gameWon.get()) {
            String message = "Player " + (playerID + 1) + " exits after the game ends.";
            System.out.println(message);
            game.writePlayerFile(playerID, message);

            String finalHandMessage = "Final hand of player " + (playerID +1) + ": " + hand;
            System.out.println(finalHandMessage);
            game.writePlayerFile(playerID, finalHandMessage);
        }
    }
}


