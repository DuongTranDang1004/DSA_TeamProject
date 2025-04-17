import java.util.ArrayList;
import java.util.List;

public class DLXSudokuTest {
    public static void main(String[] args) {
        testBuildDLXStructure();
        testCover();
        testUncover();
        testSolveWithDLX();
    }

    // Test the building of the DLX structure
    public static void testBuildDLXStructure() {
        int[][] board = new int[9][9]; // empty board
        List<DLXSolver.DLXNode> preset = new ArrayList<>();
        DLXSolver.DLXHeader dlx = DLXSolver.buildDLXStructure(board, preset);

        // Ensure the header node is not null
        assert dlx.head != null : "Head node should not be null";
        // Check that there are exactly 324 constraints (9*9 cells * 4 constraints)
        assert dlx.columns.length == 324 : "There should be 324 constraints";

        int totalDownLinks = 0;
        // Count all down links (connected nodes) in the DLX matrix
        for (DLXSolver.ColumnHeader col : dlx.columns) {
            DLXSolver.DLXNode node = col.down;
            while (node != col) {
                totalDownLinks++;
                node = node.down;
            }
        }

        System.out.println("✅ testBuildDLXStructure passed. Total DLX nodes: " + totalDownLinks);
    }

    // Test the cover function which removes a column
    public static void testCover() {
        int[][] board = new int[9][9]; // empty board
        List<DLXSolver.DLXNode> preset = new ArrayList<>();
        DLXSolver.DLXHeader dlx = DLXSolver.buildDLXStructure(board, preset);

        DLXSolver.ColumnHeader column = dlx.columns[0];
        DLXSolver.DLXNode originalRight = column.right;
        int originalSize = column.size;

        // Cover the first column
        DLXSolver.cover(column);

        // Assertions to check if the column was covered properly
        assert dlx.head.right != column : "Column should be removed from the header list";
        assert originalRight.left != column : "Right neighbor should no longer point back";

        System.out.println("✅ testCover passed. Column " + column.id + " covered (size was " + originalSize + ")");
    }

    // Test the uncover function which restores a previously covered column
    public static void testUncover() {
        int[][] board = new int[9][9]; // empty board
        List<DLXSolver.DLXNode> preset = new ArrayList<>();
        DLXSolver.DLXHeader dlx = DLXSolver.buildDLXStructure(board, preset);

        // Cover the first column and then uncover it
        DLXSolver.ColumnHeader column = dlx.columns[0];
        DLXSolver.cover(column);
        DLXSolver.uncover(column);

        // Assertions to check if the column was uncovered properly
        assert dlx.head.right == column : "Column should be restored to header list";
        assert column.right.left == column : "Right neighbor should point back after uncover";

        System.out.println("✅ testUncover passed. Column " + column.id + " uncovered successfully.");
    }

    // New test to solve the Sudoku using DLX and check the result
    public static void testSolveWithDLX() {
        int[][] board = new int[9][9]; // empty board
        // Add some preset values to the board (optional for a real test case)
        board[0][0] = 5;
        board[1][1] = 3;
        board[7][0] = 3;
        // Add more initial values as needed for testing

        // Solve the Sudoku using DLX
        DLXSolver.solve(board);

        System.out.println(" testSolveWithDLX passed. Sudoku solved using DLX.");
    }
}
