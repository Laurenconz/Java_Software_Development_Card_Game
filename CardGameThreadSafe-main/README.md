# Card Game Simulation

This project simulates a multi-threaded card game for a specified number of players. The program reads a card pack from a file, distributes the cards to players and decks, and proceeds through gameplay until a winner is determined or the game ends. The project emphasizes thread safety, modularity, and extensibility.

## Features

1. **Thread-Safe Design**  
   - The `Card`, `CardDeck`, and game logic ensure thread safety using synchronized methods and concurrent data structures.

2. **Card Creation**  
   - Cards are created with unique face values, ensuring each card is distinct.

3. **Deck Management**  
   - Cards are stored in shared decks, enabling players to draw and discard cards cyclically.

4. **Gameplay**  
   - Players draw and discard cards in their turn. The game checks for winning conditions based on a player's hand.

5. **Logging and File Output**  
   - Game actions (e.g., drawing and discarding cards) are logged to the console and written to output files for each player and deck.

6. **Immediate Win Condition**  
   - Players with an initial hand containing four cards of the same face value win immediately.

---

## Installation and Setup

1. **Requirements**
   - Java Development Kit (JDK) 8 or higher.
   - A valid text file containing the card pack with one integer per line.

2. **Compilation**
   ```bash
   javac *.java
   ```

3. **Execution**
   ```bash
   java CardGame
   ```

---

## Usage

1. Run the program.
2. Enter the number of players (greater than 1).
3. Provide the path to the pack file containing the cards.

The program will:
- Validate the input.
- Distribute cards to players and decks.
- Simulate the card game until a winner is found or the game ends.

---

## Classes Overview

### `Card`
- Represents a single card with a face value.
- Provides methods to create cards and a deck.

### `CardDeck`
- A thread-safe deck to hold `Card` objects.
- Supports adding and removing cards.

### `CardGame`
- Main class orchestrating the game logic.
- Loads card packs, distributes cards, and starts the game threads.

### `Player`
- Represents an individual player.
- Manages the player's hand and their actions (e.g., draw and discard).

---

## File Outputs

- **Player Output Files**  
  - `playerX_output.txt` (where X is the player's number) contains logs of the player's actions during the game.

- **Deck Output Files**  
  - `deckX_output.txt` (where X is the deck's number) contains the final state of the respective deck.

---

## Example Pack File

The pack file should contain integers, each representing a card's face value. For example:

```
1
2
3
4
5
6
7
8
...
```

---

---

## Authors

- **730093467**  
- **730034362**  

Version 10.0
