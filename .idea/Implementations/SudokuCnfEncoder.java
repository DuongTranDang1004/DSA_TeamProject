import java.util.ArrayList;
import java.util.List;

public class SudokuCnfEncoder {

    private static int convertFromCellToPropositionalValue(int r, int c, int d) {
        return 100 * r + 10 * c + d;
    }

    public static int[][] encodeSudoku(int[][] board) {
        List<int[]> clauses = new ArrayList<>();

        addCellConstraints(clauses);
        addCellUniquenessConstraints(clauses);
        addRowConstraints(clauses);
        addColumnConstraints(clauses);
        addBoxConstraints(clauses);
        addClueConstraints(clauses, board);

        return clauses.toArray(new int[0][]);
    }

    private static void addCellConstraints(List<int[]> cnf) {
        for (int r = 1; r <= 9; r++) {
            for (int c = 1; c <= 9; c++) {
                int[] clause = new int[9];
                for (int d = 1; d <= 9; d++) {
                    clause[d - 1] = convertFromCellToPropositionalValue(r, c, d);
                }
                cnf.add(clause);
            }
        }
    }

    private static void addCellUniquenessConstraints(List<int[]> cnf) {
        for (int r = 1; r <= 9; r++) {
            for (int c = 1; c <= 9; c++) {
                for (int d1 = 1; d1 <= 9; d1++) {
                    for (int d2 = d1 + 1; d2 <= 9; d2++) {
                        cnf.add(new int[]{
                                -convertFromCellToPropositionalValue(r, c, d1),
                                -convertFromCellToPropositionalValue(r, c, d2)
                        });
                    }
                }
            }
        }
    }

    private static void addRowConstraints(List<int[]> cnf) {
        for (int r = 1; r <= 9; r++) {
            for (int d = 1; d <= 9; d++) {
                int[] clause = new int[9];
                for (int c = 1; c <= 9; c++) {
                    clause[c - 1] = convertFromCellToPropositionalValue(r, c, d);
                }
                cnf.add(clause);

                for (int c1 = 1; c1 <= 9; c1++) {
                    for (int c2 = c1 + 1; c2 <= 9; c2++) {
                        cnf.add(new int[]{
                                -convertFromCellToPropositionalValue(r, c1, d),
                                -convertFromCellToPropositionalValue(r, c2, d)
                        });
                    }
                }
            }
        }
    }

    private static void addColumnConstraints(List<int[]> cnf) {
        for (int c = 1; c <= 9; c++) {
            for (int d = 1; d <= 9; d++) {
                int[] clause = new int[9];
                for (int r = 1; r <= 9; r++) {
                    clause[r - 1] = convertFromCellToPropositionalValue(r, c, d);
                }
                cnf.add(clause);

                for (int r1 = 1; r1 <= 9; r1++) {
                    for (int r2 = r1 + 1; r2 <= 9; r2++) {
                        cnf.add(new int[]{
                                -convertFromCellToPropositionalValue(r1, c, d),
                                -convertFromCellToPropositionalValue(r2, c, d)
                        });
                    }
                }
            }
        }
    }

    private static void addBoxConstraints(List<int[]> cnf) {
        for (int boxRow = 0; boxRow < 3; boxRow++) {
            for (int boxCol = 0; boxCol < 3; boxCol++) {
                for (int d = 1; d <= 9; d++) {
                    List<Integer> literals = new ArrayList<>();

                    for (int i = 1; i <= 3; i++) {
                        for (int j = 1; j <= 3; j++) {
                            int r = boxRow * 3 + i;
                            int c = boxCol * 3 + j;
                            literals.add(convertFromCellToPropositionalValue(r, c, d));
                        }
                    }

                    cnf.add(literals.stream().mapToInt(x -> x).toArray());

                    for (int i = 0; i < literals.size(); i++) {
                        for (int j = i + 1; j < literals.size(); j++) {
                            cnf.add(new int[]{
                                    -literals.get(i),
                                    -literals.get(j)
                            });
                        }
                    }
                }
            }
        }
    }

    /**
     *  Adds unit clauses for pre-filled cells (clues) from the board.
     */
    private static void addClueConstraints(List<int[]> cnf, int[][] board) {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int d = board[r][c];
                if (d != 0) {
                    int literal = convertFromCellToPropositionalValue(r + 1, c + 1, d);
                    cnf.add(new int[]{literal});
                }
            }
        }
    }
}

