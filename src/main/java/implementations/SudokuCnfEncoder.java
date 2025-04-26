
package implementations;

import java.util.ArrayList;

import java.util.List;

public class SudokuCnfEncoder {

    private final int N;

    public SudokuCnfEncoder(int N) {
        if (Math.sqrt(N) != (int) Math.sqrt(N)) {
            throw new IllegalArgumentException("N must be a perfect square");
        }
        this.N = N;
    }

    private int convertFromCellToPropositionalValue(int r, int c, int d) {
        return 100 * r + 10 * c + d;
    }

    public int[][] encodeSudoku(int[][] board) {
        List<int[]> clauses = new ArrayList<>();

        addCellConstraints(clauses);
        addCellUniquenessConstraints(clauses);
        addRowConstraints(clauses);
        addColumnConstraints(clauses);
        addBoxConstraints(clauses);
        addClueConstraints(clauses, board);

        return clauses.toArray(new int[0][]);
    }

    private void addCellConstraints(List<int[]> cnf) {
        for (int r = 1; r <= N; r++) {
            for (int c = 1; c <= N; c++) {
                int[] clause = new int[N];
                for (int d = 1; d <= N; d++) {
                    clause[d - 1] = convertFromCellToPropositionalValue(r, c, d);
                }
                cnf.add(clause);
            }
        }
    }

    private void addCellUniquenessConstraints(List<int[]> cnf) {
        for (int r = 1; r <= N; r++) {
            for (int c = 1; c <= N; c++) {
                for (int d1 = 1; d1 <= N; d1++) {
                    for (int d2 = d1 + 1; d2 <= N; d2++) {
                        cnf.add(new int[]{
                                -convertFromCellToPropositionalValue(r, c, d1),
                                -convertFromCellToPropositionalValue(r, c, d2)
                        });
                    }
                }
            }
        }
    }

    private void addRowConstraints(List<int[]> cnf) {
        for (int r = 1; r <= N; r++) {
            for (int d = 1; d <= N; d++) {
                int[] clause = new int[N];
                for (int c = 1; c <= N; c++) {
                    clause[c - 1] = convertFromCellToPropositionalValue(r, c, d);
                }
                cnf.add(clause);

                for (int c1 = 1; c1 <= N; c1++) {
                    for (int c2 = c1 + 1; c2 <= N; c2++) {
                        cnf.add(new int[]{
                                -convertFromCellToPropositionalValue(r, c1, d),
                                -convertFromCellToPropositionalValue(r, c2, d)
                        });
                    }
                }
            }
        }
    }

    private void addColumnConstraints(List<int[]> cnf) {
        for (int c = 1; c <= N; c++) {
            for (int d = 1; d <= N; d++) {
                int[] clause = new int[N];
                for (int r = 1; r <= N; r++) {
                    clause[r - 1] = convertFromCellToPropositionalValue(r, c, d);
                }
                cnf.add(clause);

                for (int r1 = 1; r1 <= N; r1++) {
                    for (int r2 = r1 + 1; r2 <= N; r2++) {
                        cnf.add(new int[]{
                                -convertFromCellToPropositionalValue(r1, c, d),
                                -convertFromCellToPropositionalValue(r2, c, d)
                        });
                    }
                }
            }
        }
    }

    private void addBoxConstraints(List<int[]> cnf) {
        int boxSize = (int) Math.sqrt(N); // computed locally
        for (int boxRow = 0; boxRow < boxSize; boxRow++) {
            for (int boxCol = 0; boxCol < boxSize; boxCol++) {
                for (int d = 1; d <= N; d++) {
                    List<Integer> literals = new ArrayList<>();

                    for (int i = 1; i <= boxSize; i++) {
                        for (int j = 1; j <= boxSize; j++) {
                            int r = boxRow * boxSize + i;
                            int c = boxCol * boxSize + j;
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

    private void addClueConstraints(List<int[]> cnf, int[][] board) {
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                int d = board[r][c];
                if (d != 0) {
                    int literal = convertFromCellToPropositionalValue(r + 1, c + 1, d);
                    cnf.add(new int[]{literal});
                }
            }
        }
    }
}
