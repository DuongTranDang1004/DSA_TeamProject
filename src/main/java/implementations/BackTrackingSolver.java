package implementations;

import java.util.*;

public class BackTrackingSolver {
    private final int N;

    public int[][] sudoku;
    public Map<Integer, Set<Integer>> rowConstraints = new HashMap<>();
    public Map<Integer, Set<Integer>> colConstraints = new HashMap<>();
    public Map<Integer, Set<Integer>> boxConstraints = new HashMap<>();
    public Map<String, Set<Integer>> domain;

    // ➡️ MỚI THÊM
    public int propagationDepth = 0;
    public int numberOfGuesses = 0;

    public BackTrackingSolver(int N) {
        if (Math.sqrt(N) != (int) Math.sqrt(N)) {
            throw new IllegalArgumentException("N must be a perfect square.");
        }
        this.N = N;
    }

    public static String cellKey(int row, int col) {
        return row + "," + col;
    }

    public static int[] extractRowAndCol(String cellKey) {
        String[] parts = cellKey.split(",");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);
        return new int[]{row, col};
    }

    public int getBoxIndex(int row, int col) {
        int boxSize = (int) Math.sqrt(N);
        return (row / boxSize) * boxSize + (col / boxSize);
    }

    public void findInitialRowConstraint() {
        for (int row = 0; row < N; row++) {
            Set<Integer> rowConstraint = new HashSet<>();
            for (int col = 0; col < N; col++) {
                if (sudoku[row][col] != 0) {
                    rowConstraint.add(sudoku[row][col]);
                }
            }
            rowConstraints.put(row, rowConstraint);
        }
    }

    public void findInitialColConstraint() {
        for (int col = 0; col < N; col++) {
            Set<Integer> colConstraint = new HashSet<>();
            for (int row = 0; row < N; row++) {
                if (sudoku[row][col] != 0) {
                    colConstraint.add(sudoku[row][col]);
                }
            }
            colConstraints.put(col, colConstraint);
        }
    }

    public void findInitialBoxConstraint() {
        int boxSize = (int) Math.sqrt(N);
        for (int boxRow = 0; boxRow < boxSize; boxRow++) {
            for (int boxCol = 0; boxCol < boxSize; boxCol++) {
                Set<Integer> boxConstraint = new HashSet<>();
                for (int i = 0; i < boxSize; i++) {
                    for (int j = 0; j < boxSize; j++) {
                        int r = boxRow * boxSize + i;
                        int c = boxCol * boxSize + j;
                        int value = sudoku[r][c];
                        if (value != 0) {
                            boxConstraint.add(value);
                        }
                    }
                }
                int boxIndex = boxRow * boxSize + boxCol;
                boxConstraints.put(boxIndex, boxConstraint);
            }
        }
    }

    public HashMap<String, Set<Integer>> initializeDomainFromConstraints() {
        HashMap<String, Set<Integer>> domain = new HashMap<>();
        int boxSize = (int) Math.sqrt(N);

        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                if (sudoku[row][col] != 0) continue;

                Set<Integer> domainSet = new HashSet<>();
                for (int val = 1; val <= N; val++) {
                    boolean inRow = rowConstraints.getOrDefault(row, Set.of()).contains(val);
                    boolean inCol = colConstraints.getOrDefault(col, Set.of()).contains(val);
                    int boxIndex = (row / boxSize) * boxSize + (col / boxSize);
                    boolean inBox = boxConstraints.getOrDefault(boxIndex, Set.of()).contains(val);

                    if (!inRow && !inCol && !inBox) {
                        domainSet.add(val);
                    }
                }
                domain.put(cellKey(row, col), domainSet);
            }
        }
        return domain;
    }

    // ➡️ CHỈNH ĐỂ GHI NHẬN depth và số lần guess
    public boolean guessingCell(int row, int col, int currentDepth) {
        int boxSize = (int) Math.sqrt(N);
        if (row == N) return true;

        int nextRow = (col == N - 1) ? row + 1 : row;
        int nextCol = (col == N - 1) ? 0 : col + 1;

        if (sudoku[row][col] != 0) {
            return guessingCell(nextRow, nextCol, currentDepth);
        }

        propagationDepth = Math.max(propagationDepth, currentDepth);

        List<Integer> candidates = new ArrayList<>();
        for (int val = 1; val <= N; val++) {
            if (!rowConstraints.getOrDefault(row, Set.of()).contains(val)
                    && !colConstraints.getOrDefault(col, Set.of()).contains(val)
                    && !boxConstraints.getOrDefault(getBoxIndex(row, col), Set.of()).contains(val)) {
                candidates.add(val);
            }
        }

        if (candidates.size() > 1) numberOfGuesses++;  // ➡️ có nhiều hơn 1 chọn lựa thì tính là 1 lần guess

        for (int i : candidates) {
            sudoku[row][col] = i;
            rowConstraints.get(row).add(i);
            colConstraints.get(col).add(i);
            boxConstraints.get(getBoxIndex(row, col)).add(i);

            if (guessingCell(nextRow, nextCol, currentDepth + 1)) return true;

            sudoku[row][col] = 0;
            rowConstraints.get(row).remove(i);
            colConstraints.get(col).remove(i);
            boxConstraints.get(getBoxIndex(row, col)).remove(i);
        }

        return false;
    }

    public void printBoard() {
        int boxSize = (int) Math.sqrt(N);
        for (int i = 0; i < N; i++) {
            if (i % boxSize == 0 && i != 0) {
                System.out.println("-".repeat(N * 2 + boxSize - 1));
            }
            for (int j = 0; j < N; j++) {
                if (j % boxSize == 0 && j != 0) System.out.print("| ");
                System.out.print(sudoku[i][j] == 0 ? ". " : sudoku[i][j] + " ");
            }
            System.out.println();
        }
    }

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

    public int[][] solve(int[][] sudoku) {
        if (!isValidBoard(sudoku)) {
            throw new IllegalArgumentException("Invalid board: must be " + N + "x" + N + " and contain values in range [0," + N + "]");
        }

        this.sudoku = sudoku;
        this.propagationDepth = 0; // ➡️ reset
        this.numberOfGuesses = 0;  // ➡️ reset

        findInitialRowConstraint();
        findInitialColConstraint();
        findInitialBoxConstraint();

        boolean solvable = guessingCell(0, 0, 0); // ➡️ depth ban đầu = 0
        if (solvable) {
            printBoard();
            return sudoku;
        } else {
            System.out.println("No solution found");
            return null;
        }
    }

    // ➡️ Getter để lấy thông số ra ngoài
    public int getPropagationDepth() {
        return propagationDepth;
    }

    public int getNumberOfGuesses() {
        return numberOfGuesses;
    }
}
