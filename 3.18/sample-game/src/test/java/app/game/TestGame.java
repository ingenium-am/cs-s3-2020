package app.game;

import app.data.Board;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestGame {


    @Test
    public void testGame01() {
        Game testGame = createTestGame(1, (byte) 9);

        int[][] testTurns = new int[][]{
                new int[]{ 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                new int[]{ 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                new int[]{ 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                new int[]{ 0, 0, 0, 0, 0, 5, 7, 0, 0 },
                new int[]{ 0, 0, 0, 0, 0, 3, 0, 0, 0 },
                new int[]{ 0, 0, 0, 0, 1, 9, 0, 0, 0 },
                new int[]{ 0, 8, 6, 4,10, 2, 0, 0, 0 },
                new int[]{ 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                new int[]{ 0, 0, 0, 0, 0, 0, 0, 0, 0 },
        };

        setGameState(testGame, testTurns);

        assertTrue("Check horizontal (white wins)",
                testGame.getState() == GameState.CLOSED &&
                        testGame.win() &&
                        testGame.getTurn() % 2 == 0);   // black's turn - white's move is the last
    }

    @Test
    public void testGame02() {

        // TODO - more tests
    }


    // * PRIVATE METHODS * //

    private void setGameState(Game game, int[][] turnsArray) {

        System.out.printf("Start %dx%<d\n\n", game.getBoard().getSize());

        // Turn number and coordinates
        Map<Integer, Byte[]> turns = new HashMap<>();

        for (byte y = 0; y < turnsArray.length; y++) {
            for (byte x = 0; x < turnsArray[y].length; x++) {

                int turn = turnsArray[y][x];
                if (turn > 0) {
                    turns.put(turn, new Byte[]{ x, y });
                }
            }
        }

        // Make sorted list of turns
        List<Integer> turnSequence = turns.keySet().stream().sorted().collect(Collectors.toList());

        System.out.println("Moves: " + turns.size());

        // Do move
        for (int t : turnSequence) {
            Byte[] coords = turns.get(t);

            if (game.getState() != GameState.CLOSED) {
                game.doMove(coords[0], coords[1], new Date());  // DO MOVE
            } else {
                break;
            }

            System.out.printf("> move %d [x:%d y:%d]\n", t, coords[0], coords[1]);
        }
    }

    private Game createTestGame(long tempId, byte size) {
        return Game.load(tempId, new Board(size), new Date(), 1, 2, GameState.STARTED);
    }
}
