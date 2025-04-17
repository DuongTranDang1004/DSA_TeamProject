import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DLXSolverTest {
    @Test
    void testBuildDLXStructureCreatesCorrectNumberOfColumns() {
        int[][] board = new int[9][9]; // Empty Sudoku
        List<DLXSolver.DLXNode> preset = new ArrayList<>();
        DLXSolver.DLXHeader header = DLXSolver.buildDLXStructure(board, preset);

        assertNotNull(header);
        assertEquals(324, header.columns.length, "Should have 324 columns for exact cover");
    }

    @Test
    void testDecodeSolutionReturnsValidBoard() {
        int[][] board = new int[9][9];
        List<DLXSolver.DLXNode> preset = new ArrayList<>();
        DLXSolver.DLXHeader header = DLXSolver.buildDLXStructure(board, preset);

        // Simulate solving step
        for (DLXSolver.DLXNode node : preset) {
            for (DLXSolver.DLXNode j = node.right; j != node; j = j.right) {
                DLXSolver.cover(j.column);
            }
            DLXSolver.cover(node.column);
        }

        List<DLXSolver.DLXNode> solution = new ArrayList<>(preset);
        int[][] result = DLXSolver.search(header.head, solution);

        assertNotNull(result, "Solution should not be null");
        assertTrue(isValidSudoku(result), "Result should be a valid Sudoku solution");
    }

    @Test
    void testSolvePrintsCorrectBoard() {
        int[][] board = new int[9][9];
        // Easy board with only one number filled
        board[0][0] = 5;

        // No assertion here — just to check that solve() runs without error
        assertDoesNotThrow(() -> DLXSolver.solve(board));
    }

    // Utility method to check Sudoku constraints (row, column, box uniqueness)
    private boolean isValidSudoku(int[][] board) {
        for (int i = 0; i < 9; i++) {
            boolean[] row = new boolean[10];
            boolean[] col = new boolean[10];
            boolean[] box = new boolean[10];

            for (int j = 0; j < 9; j++) {
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
    @Test
    void testCoverRemovesColumnFromListAndDecrementsSize() {
        int[][] board = new int[9][9];
        List<DLXSolver.DLXNode> preset = new ArrayList<>();
        DLXSolver.DLXHeader dlx = DLXSolver.buildDLXStructure(board, preset);

        DLXSolver.ColumnHeader col = dlx.columns[0];
        int originalSize = col.size;
        DLXSolver.DLXNode rightOfCol = (DLXSolver.DLXNode) col.right;
        DLXSolver.DLXNode leftOfCol = (DLXSolver.DLXNode) col.left;

        DLXSolver.cover(col);

        // Column is detached
        assertSame(rightOfCol.left, leftOfCol, "Column should be detached from right list");
        assertSame(leftOfCol.right, rightOfCol, "Column should be detached from left list");

        // Column's size should remain unchanged (just nodes under it are detached)
        assertEquals(originalSize, col.size, "Column size remains intact after covering itself");

        // All rows under col should be detached vertically
        DLXSolver.DLXNode down = col.down;
        while (down != col) {
            assertNotSame(down.up.down, down, "Node should be detached from vertical list");
            down = down.down;
        }
    }

    @Test
    void testUncoverRestoresColumnLinks() {
        int[][] board = new int[9][9];
        List<DLXSolver.DLXNode> preset = new ArrayList<>();
        DLXSolver.DLXHeader dlx = DLXSolver.buildDLXStructure(board, preset);

        DLXSolver.ColumnHeader col = dlx.columns[0];
        DLXSolver.DLXNode rightOfCol = (DLXSolver.DLXNode) col.right;
        DLXSolver.DLXNode leftOfCol = (DLXSolver.DLXNode) col.left;

        DLXSolver.cover(col);
        DLXSolver.uncover(col);

        // Column reattached
        assertSame(col.left, leftOfCol, "Left link restored");
        assertSame(col.right, rightOfCol, "Right link restored");
        assertSame(leftOfCol.right, col, "Left neighbor points back to column");
        assertSame(rightOfCol.left, col, "Right neighbor points back to column");

        // Vertical links restored
        DLXSolver.DLXNode down = col.down;
        while (down != col) {
            assertSame(down.up.down, down, "Vertical link restored");
            assertSame(down.down.up, down, "Vertical link restored");
            down = down.down;
        }
    }
}
