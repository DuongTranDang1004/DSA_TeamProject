package implementations;

import java.util.ArrayList;
import java.util.List;

/**
 * Mã hóa Sudoku N×N thành CNF.  
 * Tự động tìm blockH×blockW sao cho blockH*blockW == N.
 */
public class SudokuCnfEncoder {
    private final int N, blockH, blockW;

    public SudokuCnfEncoder(int N) {
        // bắt buộc N = perfect square
        int s = (int) Math.sqrt(N);
        if (s * s != N) {
            throw new IllegalArgumentException("N phải là số chính phương (9,16,25,…) nhưng N=" + N);
        }
        this.N = N;
        // ở đây blockH=blockW=s (ví dụ 25 → 5×5)
        this.blockH = s;
        this.blockW = s;
    }

    /** Biến số v(r,c,d) = (r-1)*N² + (c-1)*N + d, với 1≤r,c,d≤N */
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
        // mỗi ô có ít nhất 1 số
        for (int r = 1; r <= N; r++) {
            for (int c = 1; c <= N; c++) {
                int[] cl = new int[N];
                for (int d = 1; d <= N; d++) cl[d - 1] = var(r, c, d);
                C.add(cl);
            }
        }
    }

    private void addUniqueness(List<int[]> C) {
        // mỗi ô chỉ có 1 số
        for (int r = 1; r <= N; r++)
            for (int c = 1; c <= N; c++)
                for (int d1 = 1; d1 <= N; d1++)
                    for (int d2 = d1 + 1; d2 <= N; d2++)
                        C.add(new int[]{-var(r, c, d1), -var(r, c, d2)});
    }

    private void addRow(List<int[]> C) {
        // mỗi hàng có đủ 1..N
        for (int r = 1; r <= N; r++)
            for (int d = 1; d <= N; d++) {
                // at least once
                int[] cl = new int[N];
                for (int c = 1; c <= N; c++) cl[c - 1] = var(r, c, d);
                C.add(cl);
                // at most once
                for (int c1 = 1; c1 <= N; c1++)
                    for (int c2 = c1 + 1; c2 <= N; c2++)
                        C.add(new int[]{-var(r, c1, d), -var(r, c2, d)});
            }
    }

    private void addCol(List<int[]> C) {
        // mỗi cột có đủ 1..N
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
        // mỗi blockH×blockW có đủ 1..N
        for (int br = 0; br < blockH; br++) {
            for (int bc = 0; bc < blockW; bc++) {
                for (int d = 1; d <= N; d++) {
                    // tập literals trong block
                    List<Integer> lits = new ArrayList<>();
                    for (int i = 1; i <= blockH; i++) {
                        for (int j = 1; j <= blockW; j++) {
                            int r = br * blockH + i;
                            int c = bc * blockW + j;
                            lits.add(var(r, c, d));
                        }
                    }
                    // ít nhất 1
                    C.add(lits.stream().mapToInt(x->x).toArray());
                    // tối đa 1
                    for (int x = 0; x < lits.size(); x++)
                        for (int y = x + 1; y < lits.size(); y++)
                            C.add(new int[]{-lits.get(x), -lits.get(y)});
                }
            }
        }
    }

    private void addClues(List<int[]> C, int[][] board) {
        // các ô đã có trước
        for (int r = 0; r < N; r++)
            for (int c = 0; c < N; c++) {
                int d = board[r][c];
                if (d > 0) {
                    C.add(new int[]{var(r + 1, c + 1, d)});
                }
            }
    }
}
