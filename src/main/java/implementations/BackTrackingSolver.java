package implementations;

import java.util.*;

public class BackTrackingSolver {
    private final int N;
    private int[][] sudoku;

    private Map<Integer, Set<Integer>> rowConstraints = new HashMap<>();
    private Map<Integer, Set<Integer>> colConstraints = new HashMap<>();
    private Map<Integer, Set<Integer>> boxConstraints = new HashMap<>();

    // Metrics
    private int propagationDepth = 0;
    private int numberOfGuesses = 0;

    // Timeout Control
    private long startTime;
    private long timeoutMillis = 120_000; // Default 2 minutes
    private int recursionCounter = 0; // count recursive calls

    public BackTrackingSolver(int N) {
        if (Math.sqrt(N) != (int) Math.sqrt(N)) {
            throw new IllegalArgumentException("N must be a perfect square.");
        }
        this.N = N;
    }

    // Helpers
    public static String cellKey(int row, int col) {
        return row + "," + col;
    }

    public int getBoxIndex(int row, int col) {
        int boxSize = (int) Math.sqrt(N);
        return (row / boxSize) * boxSize + (col / boxSize);
    }

    // Initialize constraints
    public void findInitialConstraints() {
        int boxSize = (int) Math.sqrt(N);

        for (int i = 0; i < N; i++) {
            rowConstraints.put(i, new HashSet<>());
            colConstraints.put(i, new HashSet<>());
            boxConstraints.put(i, new HashSet<>());
        }

        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                int value = sudoku[row][col];
                if (value != 0) {
                    rowConstraints.get(row).add(value);
                    colConstraints.get(col).add(value);
                    boxConstraints.get(getBoxIndex(row, col)).add(value);
                }
            }
        }
    }

    // Main solving function
    public int[][] solve(int[][] sudoku) {
        if (!isValidBoard(sudoku)) {
            throw new IllegalArgumentException("Invalid board: must be " + N + "x" + N + " and contain values 0.." + N);
        }

        this.sudoku = sudoku;
        this.propagationDepth = 0;
        this.numberOfGuesses = 0;
        this.recursionCounter = 0;
        this.startTime = System.currentTimeMillis();

        findInitialConstraints();

        boolean solvable = guessingCell(0, 0, 0);
        if (solvable) {
            printBoard();
            return sudoku;
        } else {
            System.out.println("No solution found");
            return null;
        }
    }

    // Recursive backtracking search
    private boolean guessingCell(int row, int col, int currentDepth) {
        // ➡️ Check timeout every 500 calls
        recursionCounter++;
        if (recursionCounter % 500 == 0) {
            if (System.currentTimeMillis() - startTime >= timeoutMillis) {
                throw new RuntimeException("Timeout exceeded (" + (timeoutMillis / 1000) + " seconds)");
            }
        }

        // Finished all rows
        if (row == N) return true;

        // Move to next cell
        int nextRow = (col == N - 1) ? row + 1 : row;
        int nextCol = (col == N - 1) ? 0 : col + 1;

        // Skip filled cells
        if (sudoku[row][col] != 0) {
            return guessingCell(nextRow, nextCol, currentDepth);
        }

        propagationDepth = Math.max(propagationDepth, currentDepth);

        List<Integer> candidates = new ArrayList<>();
        for (int val = 1; val <= N; val++) {
            if (!rowConstraints.get(row).contains(val)
                    && !colConstraints.get(col).contains(val)
                    && !boxConstraints.get(getBoxIndex(row, col)).contains(val)) {
                candidates.add(val);
            }
        }

        if (candidates.size() > 1) numberOfGuesses++;

        for (int val : candidates) {
            sudoku[row][col] = val;
            rowConstraints.get(row).add(val);
            colConstraints.get(col).add(val);
            boxConstraints.get(getBoxIndex(row, col)).add(val);

            if (guessingCell(nextRow, nextCol, currentDepth + 1)) return true;

            sudoku[row][col] = 0;
            rowConstraints.get(row).remove(val);
            colConstraints.get(col).remove(val);
            boxConstraints.get(getBoxIndex(row, col)).remove(val);
        }

        return false;
    }

    // Validate input board
    private boolean isValidBoard(int[][] board) {
        if (board == null || board.length != N) return false;
        for (int[] row : board) {
            if (row == null || row.length != N) return false;
            for (int val : row) {
                if (val < 0 || val > N) return false;
            }
        }
        return true;
    }

    // Pretty print the board
public void printBoard() {
    // int boxSize = (int) Math.sqrt(N);

    // for (int i = 0; i < N; i++) {
    //     if (i > 0 && i % boxSize == 0) {
    //         System.out.println("-".repeat(N * 2 + boxSize - 1));
    //     }
    //     for (int j = 0; j < N; j++) {
    //         if (j > 0 && j % boxSize == 0) {
    //             System.out.print("| ");
    //         }
    //         if (sudoku[i][j] == 0) {
    //             System.out.print(". ");
    //         } else {
    //             System.out.print(sudoku[i][j] + " ");
    //         }
    //     }
    //     System.out.println();
    // }
}


    // Public getters
    public int getPropagationDepth() {
        return propagationDepth;
    }

    public int getNumberOfGuesses() {
        return numberOfGuesses;
    }

    public void setTimeoutMillis(long millis) {
        this.timeoutMillis = millis;
    }
}
