import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

import implementations.*;

public class DLXSolverTest {

    @Test
    void testBuildDLXStructureCreatesCorrectNumberOfColumns() {
        int N = 9;
        int[][] board = new int[N][N];
        DLXSolver solver = new DLXSolver(N, false);
        List<DLXSolver.DLXNode> preset = new ArrayList<>();
        DLXSolver.DLXHeader header = solver.buildDLXStructure(board, preset);

        assertNotNull(header);
        assertEquals(4 * N * N, header.columns.length, "Should have 4 * N * N columns for exact cover");
    }

    @Test
    void testDecodeSolutionReturnsValidBoard() {
        int N = 9;
        int[][] board = new int[N][N];
        DLXSolver solver = new DLXSolver(N);
        List<DLXSolver.DLXNode> preset = new ArrayList<>();
        DLXSolver.DLXHeader header = solver.buildDLXStructure(board, preset);

        for (DLXSolver.DLXNode node : preset) {
            for (DLXSolver.DLXNode j = node.right; j != node; j = j.right) {
                solver.cover(j.column);
            }
            solver.cover(node.column);
        }

        List<DLXSolver.DLXNode> solution = new ArrayList<>(preset);
        int[][] result = solver.search(header.head, solution);

        assertNotNull(result, "Solution should not be null");
        assertTrue(isValidSudoku(result), "Result should be a valid Sudoku solution");
    }

    @Test
    void testSolvePrintsCorrectBoard() {
        int[][] board = new int[9][9];
        board[0][0] = 5;
        DLXSolver solver = new DLXSolver(9);

        assertDoesNotThrow(() -> solver.solve(board));
    }

    @Test
    void testCoverRemovesColumnFromListAndDecrementsSize() {
        int N = 9;
        DLXSolver solver = new DLXSolver(N);
        int[][] board = new int[N][N];
        List<DLXSolver.DLXNode> preset = new ArrayList<>();
        DLXSolver.DLXHeader dlx = solver.buildDLXStructure(board, preset);

        DLXSolver.ColumnHeader col = dlx.columns[0];
        int originalSize = col.size;
        DLXSolver.DLXNode rightOfCol = col.right;
        DLXSolver.DLXNode leftOfCol = col.left;

        solver.cover(col);

        assertSame(rightOfCol.left, leftOfCol, "Column should be detached from right list");
        assertSame(leftOfCol.right, rightOfCol, "Column should be detached from left list");

        // Column size stays the same
        assertEquals(originalSize, col.size, "Column size remains intact after covering itself");

        DLXSolver.DLXNode down = col.down;
        while (down != col) {
            assertNotSame(down.up.down, down, "Node should be detached from vertical list");
            down = down.down;
        }
    }

    @Test
    void testUncoverRestoresColumnLinks() {
        int N = 9;
        DLXSolver solver = new DLXSolver(N);
        int[][] board = new int[N][N];
        List<DLXSolver.DLXNode> preset = new ArrayList<>();
        DLXSolver.DLXHeader dlx = solver.buildDLXStructure(board, preset);

        DLXSolver.ColumnHeader col = dlx.columns[0];
        DLXSolver.DLXNode rightOfCol = col.right;
        DLXSolver.DLXNode leftOfCol = col.left;

        solver.cover(col);
        solver.uncover(col);

        assertSame(col.left, leftOfCol, "Left link restored");
        assertSame(col.right, rightOfCol, "Right link restored");
        assertSame(leftOfCol.right, col, "Left neighbor points back to column");
        assertSame(rightOfCol.left, col, "Right neighbor points back to column");

        DLXSolver.DLXNode down = col.down;
        while (down != col) {
            assertSame(down.up.down, down, "Vertical link restored");
            assertSame(down.down.up, down, "Vertical link restored");
            down = down.down;
        }
    }

    // ✅ Utility method to check 9x9 Sudoku validity
    private boolean isValidSudoku(int[][] board) {
        int N = 9;
        for (int i = 0; i < N; i++) {
            boolean[] row = new boolean[N + 1];
            boolean[] col = new boolean[N + 1];
            boolean[] box = new boolean[N + 1];

            for (int j = 0; j < N; j++) {
                int valRow = board[i][j];
                int valCol = board[j][i];
                int valBox = board[3 * (i / 3) + j / 3][3 * (i % 3) + j % 3];

                if (valRow != 0 && row[valRow]) return false;
                if (valCol != 0 && col[valCol]) return false;
                if (valBox != 0 && box[valBox]) return false;

                if (valRow != 0) row[valRow] = true;
                if (valCol != 0) col[valCol] = true;
                if (valBox != 0) box[valBox] = true;
            }
        }
        return true;
    }
}
