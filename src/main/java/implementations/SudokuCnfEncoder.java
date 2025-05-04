package implementations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encodes an N×N Sudoku into CNF clauses for a SAT solver.
 * Variables: v(r,c,d) = (r−1)*N*N + (c−1)*N + d
 */
public class SudokuCnfEncoder {
    private final int N;

    public SudokuCnfEncoder(int N) {
        if (Math.sqrt(N) != (int) Math.sqrt(N)) {
            throw new IllegalArgumentException("N must be a perfect square");
        }
        this.N = N;
    }

    private int var(int r, int c, int d) {
        return (r - 1) * N * N + (c - 1) * N + d;
    }

    public int[][] encodeSudoku(int[][] board) {
        List<int[]> clauses = new ArrayList<>();
        addCellConstraints(clauses);
        addUniquenessConstraints(clauses);
        addRowConstraints(clauses);
        addColumnConstraints(clauses);
        addBoxConstraints(clauses);
        addClueConstraints(clauses, board);
        return clauses.toArray(new int[0][]);
    }

    private void addCellConstraints(List<int[]> C) {
        // mỗi ô phải có ít nhất một giá trị
        for (int r = 1; r <= N; r++)
            for (int c = 1; c <= N; c++) {
                int[] cl = new int[N];
                for (int d = 1; d <= N; d++) cl[d - 1] = var(r, c, d);
                C.add(cl);
            }
    }

    private void addUniquenessConstraints(List<int[]> C) {
        // mỗi ô chỉ có một giá trị
        for (int r = 1; r <= N; r++)
            for (int c = 1; c <= N; c++)
                for (int d1 = 1; d1 <= N; d1++)
                    for (int d2 = d1 + 1; d2 <= N; d2++)
                        C.add(new int[]{-var(r, c, d1), -var(r, c, d2)});
    }

    private void addRowConstraints(List<int[]> C) {
        // mỗi hàng phải có mỗi số đúng 1 lần
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

    private void addColumnConstraints(List<int[]> C) {
        // mỗi cột phải có mỗi số đúng 1 lần
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

    private void addBoxConstraints(List<int[]> C) {
        int b = (int) Math.sqrt(N);
        for (int br = 0; br < b; br++)
            for (int bc = 0; bc < b; bc++)
                for (int d = 1; d <= N; d++) {
                    List<Integer> lits = new ArrayList<>();
                    for (int i = 1; i <= b; i++)
                        for (int j = 1; j <= b; j++)
                            lits.add(var(br*b + i, bc*b + j, d));
                    C.add(lits.stream().mapToInt(x->x).toArray());
                    for (int x = 0; x < lits.size(); x++)
                        for (int y = x+1; y < lits.size(); y++)
                            C.add(new int[]{-lits.get(x), -lits.get(y)});
                }
    }

    private void addClueConstraints(List<int[]> C, int[][] board) {
        // các giá trị đã cho
        for (int r = 0; r < N; r++)
            for (int c = 0; c < N; c++) {
                int d = board[r][c];
                if (d > 0) C.add(new int[]{var(r+1, c+1, d)});
            }
    }
}

