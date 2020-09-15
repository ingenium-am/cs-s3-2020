package am.ingenium.tictactoe;

import am.ingenium.tictactoe.data.Player;
import am.ingenium.tictactoe.game.Game;
import am.ingenium.tictactoe.game.TicTacToe;

import java.util.HashMap;
import java.util.Map;

public class GameTest {

    public static void main(String[] args) {

        // Create command-line game by preset static method
        GameCLI gameCLI = createGameCLI();

        // Uncomment to Set custom empty cell
        // gameCLI.setEmptyCell('%');

        // Uncomment to set symbols uppercase
        // gameCLI.setCellUppercase();

        // Start game loop
        gameCLI.play();
    }

    // Set command-line config and create game
    private static GameCLI createGameCLI() {

        // Players' IDs must be different and > 0
        Player player1 = new Player(1);
        Player player2 = new Player(2);

        // Create by reference of interface type
        Game ticTacToe = new TicTacToe(player1, player2);

        // Set player-character mapping for CLI
        Map<Integer, Character> cellMap = new HashMap<>();
        cellMap.put(player1.getId(), 'x');
        cellMap.put(player2.getId(), 'o');

        // Create new CLI and return
        return new GameCLI(ticTacToe, cellMap);
    }
}
