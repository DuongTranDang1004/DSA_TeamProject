import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BackTrackingSolver {
    public int[][] sudoku;
    public Map<Integer, Set<Integer>> rowConstraints = new HashMap<>();
    public Map<Integer, Set<Integer>> colConstraints = new HashMap<>();
    public Map<Integer, Set<Integer>> boxConstraints = new HashMap<>();
    public Map<String, Set<Integer>> domain;

    private void findInitialRowConstraint(){
        for (int row = 0; row < 9; row++){
            Set<Integer> rowConstraint = new HashSet<>();
            for (int col = 0; col < 9; col++){
                if (sudoku[row][col] != 0){
                    rowConstraint.add(sudoku[row][col]);
                }
            }
            rowConstraints.put(row, rowConstraint);
        }
    }

    private void findInitialColConstraint(){
        for (int col = 0; col < 9; col++){
            Set<Integer> colConstraint = new HashSet<>();
            for (int row = 0; row < 9; row++){
                if (sudoku[row][col] != 0){
                    colConstraint.add(sudoku[row][col]);
                }

            }
            colConstraints.put(col, colConstraint);
        }
    }

    private void findInitialBoxConstraint() {
        for (int row = 0; row < 9; row += 3) {
            for (int col = 0; col < 9; col += 3) {
                Set<Integer> boxConstraint = new HashSet<>();
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        int value = sudoku[row + i][col + j];
                        if (value != 0) {
                            boxConstraint.add(value);
                        }
                    }
                }
                int boxIndex = (row / 3) * 3 + (col / 3);
                boxConstraints.put(boxIndex, boxConstraint);
            }
        }
    }

    private static String cellKey(int row, int col) {
        return row + "," + col;
    }

    private static int[] extractRowAndCol(String cellKey) {
        String[] parts = cellKey.split(",");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);
        return new int[] { row, col };
    }

    public HashMap<String, Set<Integer>> initializeDomainFromConstraints() {
        HashMap<String, Set<Integer>> domain = new HashMap<>();

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (sudoku[row][col] != 0) continue; // Skip already filled cells

                Set<Integer> domainSet = new HashSet<>();
                for (int val = 1; val <= 9; val++) {
                    boolean inRow = rowConstraints.getOrDefault(row, Set.of()).contains(val);
                    boolean inCol = colConstraints.getOrDefault(col, Set.of()).contains(val);
                    int boxIndex = (row / 3) * 3 + (col / 3);
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

    public BackTrackingSolver(int[][] sudoku) {
        this.sudoku = sudoku;
        findInitialRowConstraint();
        findInitialColConstraint();
        findInitialBoxConstraint();
    }
    private boolean guessingCell(int row, int col) {
        if (row == 9) return true; // Finished all rows

        int nextRow = (col == 8) ? row + 1 : row;
        int nextCol = (col == 8) ? 0 : col + 1;

        if (sudoku[row][col] != 0) {
            return guessingCell(nextRow, nextCol);
        }

        int currentBox = (row / 3) * 3 + (col / 3);
        Set<Integer> rowSet = rowConstraints.get(row);
        Set<Integer> colSet = colConstraints.get(col);
        Set<Integer> boxSet = boxConstraints.get(currentBox);

        for (int i = 1; i <= 9; i++) {
            if (!rowSet.contains(i) && !colSet.contains(i) && !boxSet.contains(i)) {
                // Tentatively place number
                sudoku[row][col] = i;
                rowSet.add(i);
                colSet.add(i);
                boxSet.add(i);

                if (guessingCell(nextRow, nextCol)) {
                    return true;
                }

                // Backtrack
                sudoku[row][col] = 0;
                rowSet.remove(i);
                colSet.remove(i);
                boxSet.remove(i);
            }
        }

        return false;
    }

    public boolean solve(){
        boolean solvable = guessingCell(0, 0);
        for (int i = 0; i < sudoku.length; i++) {
            for (int j = 0; j < sudoku[i].length; j++) {
                System.out.print(sudoku[i][j] + " ");
            }
            System.out.println(); // Move to the next line after each row
        }
        return solvable;
    }
}
