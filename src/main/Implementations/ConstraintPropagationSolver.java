import java.util.*;

public class ConstraintPropagationSolver {
    private int[][] sudoku;
    private Map<Integer, Set<Integer>> rowConstraints = new HashMap<>();
    private Map<Integer, Set<Integer>> colConstraints = new HashMap<>();
    private Map<Integer, Set<Integer>> boxConstraints = new HashMap<>();
    private Map<String, Set<Integer>> domain = new HashMap<>();

    public ConstraintPropagationSolver(int[][] board) {
        this.sudoku = board;
        for (int i = 0; i < 9; i++) {
            rowConstraints.put(i, new HashSet<>());
            colConstraints.put(i, new HashSet<>());
            boxConstraints.put(i, new HashSet<>());
        }
        initializeConstraintsAndDomain();
    }

    private void initializeConstraintsAndDomain() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int value = sudoku[row][col];
                if (value != 0) {
                    rowConstraints.get(row).add(value);
                    colConstraints.get(col).add(value);
                    boxConstraints.get(getBoxIndex(row, col)).add(value);
                }
            }
        }

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (sudoku[row][col] == 0) {
                    Set<Integer> possible = new HashSet<>();
                    for (int i = 1; i <= 9; i++) {
                        if (!rowConstraints.get(row).contains(i)
                                && !colConstraints.get(col).contains(i)
                                && !boxConstraints.get(getBoxIndex(row, col)).contains(i)) {
                            possible.add(i);
                        }
                    }
                    domain.put(row + "," + col, possible);
                }
            }
        }
    }

    public boolean solve() {
        return backtrack(new HashMap<>(domain));
    }

    private boolean backtrack(Map<String, Set<Integer>> currentDomain) {
        if (currentDomain.isEmpty()) return true;
        for (Set<Integer> values : currentDomain.values()) {
            if (values.isEmpty()) return false;
        }

        String cell = selectCellWithMRV(currentDomain);
        String[] parts = cell.split(",");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);

        for (int value : new HashSet<>(currentDomain.get(cell))) {
            if (rowConstraints.get(row).contains(value)
                    || colConstraints.get(col).contains(value)
                    || boxConstraints.get(getBoxIndex(row, col)).contains(value)) {
                continue;
            }

            sudoku[row][col] = value;
            rowConstraints.get(row).add(value);
            colConstraints.get(col).add(value);
            boxConstraints.get(getBoxIndex(row, col)).add(value);

            Map<String, Set<Integer>> nextDomain = deepCopy(currentDomain);
            nextDomain.remove(cell);
            propagate(row, col, value, nextDomain);

            if (backtrack(nextDomain)) return true;

            sudoku[row][col] = 0;
            rowConstraints.get(row).remove(value);
            colConstraints.get(col).remove(value);
            boxConstraints.get(getBoxIndex(row, col)).remove(value);
        }

        return false;
    }

    private void propagate(int row, int col, int value, Map<String, Set<Integer>> dom) {
        for (int i = 0; i < 9; i++) {
            dom.computeIfPresent(row + "," + i, (k, v) -> { v.remove(value); return v; });
            dom.computeIfPresent(i + "," + col, (k, v) -> { v.remove(value); return v; });
        }

        int startRow = (row / 3) * 3, startCol = (col / 3) * 3;
        for (int r = startRow; r < startRow + 3; r++) {
            for (int c = startCol; c < startCol + 3; c++) {
                dom.computeIfPresent(r + "," + c, (k, v) -> { v.remove(value); return v; });
            }
        }
    }

    private String selectCellWithMRV(Map<String, Set<Integer>> domainMap) {
        return domainMap.entrySet().stream()
                .min(Comparator.comparingInt(e -> e.getValue().size()))
                .map(Map.Entry::getKey)
                .orElseThrow();
    }

    private Map<String, Set<Integer>> deepCopy(Map<String, Set<Integer>> original) {
        Map<String, Set<Integer>> copy = new HashMap<>();
        for (Map.Entry<String, Set<Integer>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copy;
    }

    private int getBoxIndex(int row, int col) {
        return (row / 3) * 3 + (col / 3);
    }

    public void printBoard() {
        for (int[] row : sudoku) {
            for (int num : row) {
                System.out.print(num + " ");
            }
            System.out.println();
        }
    }
}
