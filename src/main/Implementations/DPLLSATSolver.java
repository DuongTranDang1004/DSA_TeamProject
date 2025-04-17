import java.util.*;

    public class DPLLSATSolver {

        private int[][] cnf;
        private final Map<Integer, Boolean> assignment = new HashMap<>();

        public DPLLSATSolver() {}

        public int[][] solve(int[][] sudokuBoard) {
            this.cnf = SudokuCnfEncoder.encodeSudoku(sudokuBoard);
            if (solveRecursive(cnf)) {
                validateAssignments(assignment);
                int[][] sudoku = decodeSudokuBoard(assignment);
                printBoard(sudoku);
                return sudoku;
            }
            return null;
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
            assignment.remove(Math.abs(decisionLiteral)); // 🧹 undo

            applyAssignment(-decisionLiteral);
            if (solveRecursive(simplify(cnf, -decisionLiteral))) return true;
            assignment.remove(Math.abs(decisionLiteral)); // 🧹 undo

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

        public static int[][] decodeSudokuBoard(Map<Integer, Boolean> assignment) {
            int[][] board = new int[9][9];
            for (Map.Entry<Integer, Boolean> entry : assignment.entrySet()) {
                int var = entry.getKey();
                if (!entry.getValue()) continue;

                int r = var / 100;
                int c = (var / 10) % 10;
                int d = var % 10;

                board[r - 1][c - 1] = d;
            }
            return board;
        }

        public void printBoard(int[][] sudoku) {
            for (int i = 0; i < 9; i++) {
                if (i % 3 == 0 && i != 0) System.out.println("------+-------+------");
                for (int j = 0; j < 9; j++) {
                    if (j % 3 == 0 && j != 0) System.out.print("| ");
                    System.out.print(sudoku[i][j] == 0 ? ". " : sudoku[i][j] + " ");
                }
                System.out.println();
            }
        }

        public void validateAssignments(Map<Integer, Boolean> assignment) {
            for (int r = 1; r <= 9; r++) {
                for (int c = 1; c <= 9; c++) {
                    boolean found = false;
                    for (int d = 1; d <= 9; d++) {
                        int var = 100 * r + 10 * c + d;
                        if (assignment.getOrDefault(var, false)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        System.out.println("Cell (" + r + "," + c + ") has no " +
                                "assigned digit!");
                    }
                }
            }
        }
    }
