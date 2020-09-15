package am.ingenium.tictactoe.data.board;

import java.util.Arrays;

public abstract class FixedSizeBoard {

    protected final int width;
    protected final int height;

    protected final int[][] boardArray;

    public FixedSizeBoard(int width, int height) {
        this.width = width;
        this.height = height;
        boardArray = new int[height][width];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[][] getBoardArray() {
        return boardArray;
    }

    public boolean isFull() {
        for (int[] row : boardArray) {
            for (int col : row) {
                // If any cell is empty (0) - not full
                if (col == 0) {
                    return false;   // further check is unnecessary
                }
            }
        }
        // If loops are over - there's no empty cell
        return true;
    }

    // Stacked rows string
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("[");
        for (int i = 0; i < boardArray.length; i++) {
            int[] row = boardArray[i];
            b.append(Arrays.toString(row));
            if (i != boardArray.length - 1) {
                b.append(",");
                b.append("\n ");
            }
        }
        b.append("]");
        return b.toString();
    }
}
