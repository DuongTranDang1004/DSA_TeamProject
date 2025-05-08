package implementations;

import java.util.*;

/*
 * ============================================
 *       ConstraintPropagationSolver Class
 * ============================================
 * User For: Solving Sudoku by propagating constraints to reduce possible values for each cell.
 * Written By: Group 1 in @RMIT - 2025 for Group Project of COSC2469 Algorithm And Analysis Course
 * ============================================
 */

public class ConstraintPropagationSolver {
    private final int N;
    private int[][] sudoku;
    private Map<Integer, Set<Integer>> rowConstraints = new HashMap<>();
    private Map<Integer, Set<Integer>> colConstraints = new HashMap<>();
    private Map<Integer, Set<Integer>> boxConstraints = new HashMap<>();
    private Map<String, Set<Integer>> domain = new HashMap<>();

    private int propagationDepth = 0;
    private int numberOfGuesses = 0;

    public ConstraintPropagationSolver(int N) {
        if (Math.sqrt(N) != (int) Math.sqrt(N)) {
            throw new IllegalArgumentException("N must be a perfect square.");
        }
        this.N = N;
    }

    public void initializeConstraintsAndDomain() {
        int boxSize = (int) Math.sqrt(N);
        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                int value = sudoku[row][col];
                if (value != 0) {
                    rowConstraints.get(row).add(value);
                    colConstraints.get(col).add(value);
                    boxConstraints.get(getBoxIndex(row, col, boxSize)).add(value);
                }
            }
        }

        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                if (sudoku[row][col] == 0) {
                    Set<Integer> possible = new HashSet<>();
                    for (int i = 1; i <= N; i++) {
                        if (!rowConstraints.get(row).contains(i)
                                && !colConstraints.get(col).contains(i)
                                && !boxConstraints.get(getBoxIndex(row, col, boxSize)).contains(i)) {
                            possible.add(i);
                        }
                    }
                    domain.put(row + "," + col, possible);
                }
            }
        }
    }

    public int[][] solve(int[][] sudoku) {
        if (!isValidBoard(sudoku)) {
            throw new IllegalArgumentException("Board must be " + N + "x" + N + " and contain values from 0 to " + N);
        }

        this.sudoku = sudoku;
        this.propagationDepth = 0;
        this.numberOfGuesses = 0;

        for (int i = 0; i < N; i++) {
            rowConstraints.put(i, new HashSet<>());
            colConstraints.put(i, new HashSet<>());
            boxConstraints.put(i, new HashSet<>());
        }

        initializeConstraintsAndDomain();

        if (backtrack(new HashMap<>(domain), 0)) {
            return sudoku;
        } else {
            return null;
        }
    }

    public boolean backtrack(Map<String, Set<Integer>> currentDomain, int currentDepth) {
        int boxSize = (int) Math.sqrt(N);

        if (currentDomain.isEmpty()) return true;

        for (Set<Integer> values : currentDomain.values()) {
            if (values.isEmpty()) return false;
        }

        propagationDepth = Math.max(propagationDepth, currentDepth);

        String cell = selectCellWithMRV(currentDomain);
        String[] parts = cell.split(",");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);

        Set<Integer> valuesToTry = new HashSet<>(currentDomain.get(cell));
        if (valuesToTry.size() > 1) numberOfGuesses++;

        for (int value : valuesToTry) {
            if (rowConstraints.get(row).contains(value)
                    || colConstraints.get(col).contains(value)
                    || boxConstraints.get(getBoxIndex(row, col, boxSize)).contains(value)) {
                continue;
            }

            sudoku[row][col] = value;
            rowConstraints.get(row).add(value);
            colConstraints.get(col).add(value);
            boxConstraints.get(getBoxIndex(row, col, boxSize)).add(value);

            Map<String, Set<Integer>> nextDomain = deepCopy(currentDomain);
            nextDomain.remove(cell);
            propagate(row, col, value, nextDomain, boxSize);

            if (backtrack(nextDomain, currentDepth + 1)) return true;

            sudoku[row][col] = 0;
            rowConstraints.get(row).remove(value);
            colConstraints.get(col).remove(value);
            boxConstraints.get(getBoxIndex(row, col, boxSize)).remove(value);
        }

        return false;
    }

    public void propagate(int row, int col, int value, Map<String, Set<Integer>> dom, int boxSize) {
        for (int i = 0; i < N; i++) {
            dom.computeIfPresent(row + "," + i, (k, v) -> { v.remove(value); return v; });
            dom.computeIfPresent(i + "," + col, (k, v) -> { v.remove(value); return v; });
        }

        int startRow = (row / boxSize) * boxSize;
        int startCol = (col / boxSize) * boxSize;

        for (int r = startRow; r < startRow + boxSize; r++) {
            for (int c = startCol; c < startCol + boxSize; c++) {
                dom.computeIfPresent(r + "," + c, (k, v) -> { v.remove(value); return v; });
            }
        }
    }

    public String selectCellWithMRV(Map<String, Set<Integer>> domainMap) {
        return domainMap.entrySet().stream()
                .min(Comparator.comparingInt(e -> e.getValue().size()))
                .map(Map.Entry::getKey)
                .orElseThrow();
    }

    public Map<String, Set<Integer>> deepCopy(Map<String, Set<Integer>> original) {
        Map<String, Set<Integer>> copy = new HashMap<>();
        for (Map.Entry<String, Set<Integer>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copy;
    }

    public int getBoxIndex(int row, int col, int boxSize) {
        return (row / boxSize) * boxSize + (col / boxSize);
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

    public int getPropagationDepth() {
        return propagationDepth;
    }

    public int getNumberOfGuesses() {
        return numberOfGuesses;
    }
}
