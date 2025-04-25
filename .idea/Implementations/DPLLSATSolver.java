import java.util.*;

public class DPLLSATSolver {

    private final int N;
    private int[][] cnf;
    private final Map<Integer, Boolean> assignment = new HashMap<>();

    public DPLLSATSolver(int N) {
        if (Math.sqrt(N) != (int) Math.sqrt(N)) {
            throw new IllegalArgumentException("N must be a perfect square.");
        }
        this.N = N;
    }

    public int[][] solve(int[][] sudokuBoard) {
        if (!isValidBoard(sudokuBoard)) {
            throw new IllegalArgumentException("Invalid board: must be a " + N + "×" + N + " square with values in [0, " + N + "]");
        }

        SudokuCnfEncoder encoder = new SudokuCnfEncoder(N);
        this.cnf = encoder.encodeSudoku(sudokuBoard);

        if (solveRecursive(cnf)) {
            validateAssignments(assignment);
            int[][] sudoku = decodeSudokuBoard(assignment);
            return sudoku;
        }
        return null;
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

    private boolean solveRecursive(int[][] cnf) {
        if (cnf.length == 0) return true;

        for (int[] clause : cnf) {
            if (clause.length == 0) return false;
        }

        Integer unitLiteral = findUnitClause(cnf);
        if (unitLiteral != null) {
            applyAssignment(unitLiteral);
            int[][] simplified = simplify(cnf, unitLiteral);
            boolean result = solveRecursive(simplified);
            if (!result) assignment.remove(Math.abs(unitLiteral));
            return result;
        }

        Integer decisionLiteral = chooseLiteral(cnf);
        if (decisionLiteral == null) return true;

        applyAssignment(decisionLiteral);
        if (solveRecursive(simplify(cnf, decisionLiteral))) return true;
        assignment.remove(Math.abs(decisionLiteral));

        applyAssignment(-decisionLiteral);
        if (solveRecursive(simplify(cnf, -decisionLiteral))) return true;
        assignment.remove(Math.abs(decisionLiteral));

        return false;
    }

    private Integer findUnitClause(int[][] cnf) {
        for (int[] clause : cnf) {
            if (clause.length == 1) {
                return clause[0];
            }
        }
        return null;
    }

    private Integer chooseLiteral(int[][] cnf) {
        for (int[] clause : cnf) {
            for (int literal : clause) {
                if (!assignment.containsKey(Math.abs(literal))) {
                    return literal;
                }
            }
        }
        return null;
    }

    private void applyAssignment(int literal) {
        assignment.put(Math.abs(literal), literal > 0);
    }

    private int[][] simplify(int[][] cnf, int literal) {
        List<int[]> simplified = new ArrayList<>();
        for (int[] clause : cnf) {
            boolean satisfied = false;
            for (int lit : clause) {
                if (lit == literal) {
                    satisfied = true;
                    break;
                }
            }
            if (satisfied) continue;

            int[] reduced = remove(clause, -literal);
            if (reduced != null) {
                simplified.add(reduced);
            }
        }
        return simplified.toArray(new int[0][]);
    }

    private int[] remove(int[] clause, int literalToRemove) {
        int count = 0;
        for (int lit : clause) {
            if (lit != literalToRemove) count++;
        }

        if (count == 0) return new int[0]; // conflict

        int[] result = new int[count];
        int i = 0;
        for (int lit : clause) {
            if (lit != literalToRemove) result[i++] = lit;
        }
        return result;
    }

    public int[][] decodeSudokuBoard(Map<Integer, Boolean> assignment) {
        int[][] board = new int[N][N];
        for (Map.Entry<Integer, Boolean> entry : assignment.entrySet()) {
            int var = entry.getKey();
            if (!entry.getValue()) continue;

            int r = var / 100;
            int c = (var / 10) % 10;
            int d = var % 10;

            if (r >= 1 && r <= N && c >= 1 && c <= N && d >= 1 && d <= N) {
                board[r - 1][c - 1] = d;
            }
        }
        return board;
    }

    public void printBoard(int[][] sudoku) {
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

    public void validateAssignments(Map<Integer, Boolean> assignment) {
        for (int r = 1; r <= N; r++) {
            for (int c = 1; c <= N; c++) {
                boolean found = false;
                for (int d = 1; d <= N; d++) {
                    int var = 100 * r + 10 * c + d;
                    if (assignment.getOrDefault(var, false)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    System.out.println("⚠️ Cell (" + r + "," + c + ") has no assigned digit!");
                }
            }
        }
    }
}
