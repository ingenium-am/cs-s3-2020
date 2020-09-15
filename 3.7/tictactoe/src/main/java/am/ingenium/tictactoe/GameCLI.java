package am.ingenium.tictactoe;

import am.ingenium.tictactoe.data.Player;
import am.ingenium.tictactoe.game.Game;

import java.util.*;

public class GameCLI {

    // Default variables
    private char emptyCell = ' ';

    // Set by constructor
    private final Game game;
    private final Map<Integer, Character> cellMap;

    // Constructor accepts any class implementing Game interface
    public GameCLI(Game game, Map<Integer, Character> cellMap) {
        this.game = game;
        this.cellMap = cellMap;
    }

    // Start game
    public void play() {

        // local variables for reuse
        String exitInput = "exit";
        String prompt = "Enter coordinates: ";

        // Print Title
        System.out.printf("\n%s (type '%s' to exit game)\n", game.getName(), exitInput);

        // Create Scanner for input
        Scanner scanner = new Scanner(System.in);

        // MAIN LOOP - GAME LIFECYCLE
        mainLoop: // label main loop
        while (true) {

            // Print board, player and input-prompt
            System.out.println(getBoardString());
            System.out.println(playerString(game.getCurrentPlayer()));
            System.out.print(prompt);

            // Get input
            String input = scanner.nextLine();

            // Parse input as coordinates (empty means out of board)
            int[] coords = parseInput(input);

            // INPUT LOOP (enter if parsing fails)
            while (coords.length == 0) {

                // Check if input is exit command
                if (input.toLowerCase().equals(exitInput)) {
                    // exit MAIN LOOP (without label - only current)
                    break mainLoop;
                }

                // Print input error and input-prompt
                System.out.println("--\tinvalid input! " + input);
                System.out.println(prompt);

                // Get new input and update variable
                input = scanner.nextLine();

                // Update coordinates for INPUT LOOP check
                coords = parseInput(input);
            }

            // Set board cells (if still in MAIN LOOP - input is valid)
            boolean set = game.setCell(coords[0], coords[1]);

            // Check if set failed (cell is occupied)
            if (!set) {
                System.out.println("Cell is already set!");
                // start next iteration (skip following code of current loop)
                continue;
            }

            // Check if turn wins (if still in MAIN LOOP - successfully set)
            if (game.turnWins()) {
                // Print board, winner and exit MAIN LOOP
                System.out.println(getBoardString());
                System.out.println(game.getCurrentPlayer().getName() + " WINS!");
                break;
            }

            // Check if board is full (if still in MAIN LOOP - draw)
            if (game.boardIsFull()) {
                // Print board, draw message and exit MAIN LOOP
                System.out.println(getBoardString());
                System.out.println("DRAW...");
                break;
            }

            // If still in MAIN LOOP - switch turn and continue playing
            game.switchTurn();
        }

        // EXIT
        System.out.println("\nEXIT\n(Press Enter)");

        // Keep command-prompt open and wait for confirmation
        scanner.nextLine();
    }

    // Create String of game's board
    public String getBoardString() {

        // Get board size
        int width = game.getBoard().getWidth();
        int height = game.getBoard().getHeight();

        StringBuilder bs = new StringBuilder("\n");

        // APPEND blocks to StringBuilder

        bs.append(' ');
        // Loop over X coordinates
        for (int i = 0; i < width; i++) {
            bs.append(String.format(" %d", i));
        }
        bs.append('\n'); // end of X coordinates row

        // Loop over rows with Y coordinates
        for (int j = 0; j < height; j++) {
            int[] row = game.getBoard().getBoardArray()[j];

            // Y coordinates - chars (97 = 'a', 98 = 'b', ...)
            bs.append((char) (j + 97));

            // Loop over cells
            for (int col : row) {
                bs.append(String.format(" %c", cellMap.getOrDefault(col, emptyCell)));
            }
            bs.append("\n"); // end of each row
        }
        return bs.toString();
    }

    public String playerString(Player player) {
        return String.format("%s (%s)",
                player.getName(),
                cellMap.get(player.getId()));
    }

    // Set custom symbol for empty cell
    public void setEmptyCell(char emptyCell) {
        this.emptyCell = emptyCell;
    }

    // Set cell symbols uppercase
    public void setCellUppercase() {
        for (Integer k : cellMap.keySet()) {
            char c = cellMap.get(k);
            cellMap.put(k, Character.toUpperCase(c));
        }
    }

    // Set cell symbols lowercase
    public void setCellLowercase() {
        for (Integer k : cellMap.keySet()) {
            char c = cellMap.get(k);
            cellMap.put(k, Character.toLowerCase(c));
        }
    }


    // PRIVATE METHODS //

    // Parse input and validate for coordinates
    private int[] parseInput(String input) {

        // Check if input is not two-character
        if (input.length() != 2) {
            // Return empty array as failed parsing
            return new int[0];
        }

        // Parse for numeric values
        int x = input.charAt(0) - 97;                  // ASCII code ('a' = 97, 'b' = 98, ...)
        int y;

        try {
            // Try to convert to an integer
            y = Integer.parseInt(input.substring(1));  // Straight conversation ('1' = 1)
        } catch (NumberFormatException e) {
            // Return empty array if failed
            return new int[0];
        }

        // Check if coordinates is not out of the board
        if (x < 0 || x >= game.getBoard().getWidth() || y < 0 || y >= game.getBoard().getHeight()) {
            // Return empty array if one of conditions above is true
            return new int[0];
        }

        // Return validated coordinates
        return new int[]{ x, y };
    }
}
