package implementations;

import java.util.ArrayList;
import java.util.List;

/*
 * ============================================
 *       SudokuCnfEncoder Class
 * ============================================
 * User For: Transforming Sudoku into a form suitable for SAT solvers to process and solve.
 * Written By: Group 1 in @RMIT - 2025 for Group Project of COSC2469 Algorithm And Analysis Course
 * ============================================
 */

public class SudokuCnfEncoder {
    private final int N, blockH, blockW;

    public SudokuCnfEncoder(int N) {
        int s = (int) Math.sqrt(N);
        if (s * s != N) {
            throw new IllegalArgumentException("Board must be " + N + "x" + N + " and contain values from 0 to " + N);
        }
        this.N = N;
        this.blockH = s;
        this.blockW = s;
    }

    private int var(int r, int c, int d) {
        return (r - 1) * N * N + (c - 1) * N + d;
    }

    public int[][] encodeSudoku(int[][] board) {
        List<int[]> C = new ArrayList<>();
        addCell(C);
        addUniqueness(C);
        addRow(C);
        addCol(C);
        addBox(C);
        addClues(C, board);
        return C.toArray(new int[0][]);
    }

    private void addCell(List<int[]> C) {
        for (int r = 1; r <= N; r++) {
            for (int c = 1; c <= N; c++) {
                int[] cl = new int[N];
                for (int d = 1; d <= N; d++) cl[d - 1] = var(r, c, d);
                C.add(cl);
            }
        }
    }

    private void addUniqueness(List<int[]> C) {
        for (int r = 1; r <= N; r++)
            for (int c = 1; c <= N; c++)
                for (int d1 = 1; d1 <= N; d1++)
                    for (int d2 = d1 + 1; d2 <= N; d2++)
                        C.add(new int[]{-var(r, c, d1), -var(r, c, d2)});
    }

    private void addRow(List<int[]> C) {
        for (int r = 1; r <= N; r++)
            for (int d = 1; d <= N; d++) {
                int[] cl = new int[N];
                for (int c = 1; c <= N; c++) cl[c - 1] = var(r, c, d);
                C.add(cl);
                for (int c1 = 1; c1 <= N; c1++)
                    for (int c2 = c1 + 1; c2 <= N; c2++)
                        C.add(new int[]{-var(r, c1, d), -var(r, c2, d)});
            }
    }

    private void addCol(List<int[]> C) {
        for (int c = 1; c <= N; c++)
            for (int d = 1; d <= N; d++) {
                int[] cl = new int[N];
                for (int r = 1; r <= N; r++) cl[r - 1] = var(r, c, d);
                C.add(cl);
                for (int r1 = 1; r1 <= N; r1++)
                    for (int r2 = r1 + 1; r2 <= N; r2++)
                        C.add(new int[]{-var(r1, c, d), -var(r2, c, d)});
            }
    }

    private void addBox(List<int[]> C) {
        for (int br = 0; br < blockH; br++) {
            for (int bc = 0; bc < blockW; bc++) {
                for (int d = 1; d <= N; d++) {
                    List<Integer> lits = new ArrayList<>();
                    for (int i = 1; i <= blockH; i++) {
                        for (int j = 1; j <= blockW; j++) {
                            int r = br * blockH + i;
                            int c = bc * blockW + j;
                            lits.add(var(r, c, d));
                        }
                    }
                    C.add(lits.stream().mapToInt(x->x).toArray());
                    for (int x = 0; x < lits.size(); x++)
                        for (int y = x + 1; y < lits.size(); y++)
                            C.add(new int[]{-lits.get(x), -lits.get(y)});
                }
            }
        }
    }

    private void addClues(List<int[]> C, int[][] board) {
        for (int r = 0; r < N; r++)
            for (int c = 0; c < N; c++) {
                int d = board[r][c];
                if (d > 0) {
                    C.add(new int[]{var(r + 1, c + 1, d)});
                }
            }
    }
}
