import implementations.DLXSolver;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/*
 * ============================================
 *       DLXSolverTest Class
 * ============================================
 * User For: Unit testing Knuth's Algorithm X and Dancing Links (DLX) based Sudoku solver. Includes node linking, covering/uncovering, and decoding.
 * Written By: Group 1 in @RMIT - 2025 for Group Project of COSC2469 Algorithm And Analysis Course
 * ============================================
 */

public class DLXSolverTest {

    private static final int N = 9;

    @Test
    void testSolveReturnsCorrectSudoku() {
        int[][] board = {
                {5, 3, 0, 0, 7, 0, 0, 0, 0},
                {6, 0, 0, 1, 9, 5, 0, 0, 0},
                {0, 9, 8, 0, 0, 0, 0, 6, 0},
                {8, 0, 0, 0, 6, 0, 0, 0, 3},
                {4, 0, 0, 8, 0, 3, 0, 0, 1},
                {7, 0, 0, 0, 2, 0, 0, 0, 6},
                {0, 6, 0, 0, 0, 0, 2, 8, 0},
                {0, 0, 0, 4, 1, 9, 0, 0, 5},
                {0, 0, 0, 0, 8, 0, 0, 7, 9}
        };

        DLXSolver solver = new DLXSolver(N, false);
        int[][] solved = solver.solve(board);

        assertNotNull(solved);
        for (int i = 0; i < N; i++) {
            Set<Integer> row = new HashSet<>();
            Set<Integer> col = new HashSet<>();
            for (int j = 0; j < N; j++) {
                row.add(solved[i][j]);
                col.add(solved[j][i]);
            }
            assertEquals(N, row.size());
            assertEquals(N, col.size());
        }
    }

    @Test
    void testInvalidBoardThrowsException() {
        int[][] board = new int[8][8];
        DLXSolver solver = new DLXSolver(N, false);
        assertThrows(IllegalArgumentException.class, () -> solver.solve(board));
    }

    @Test
    void testCopyBoardCreatesCorrectCopy() {
        DLXSolver solver = new DLXSolver(N, false);
        int[][] board = new int[N][N];
        board[0][0] = 9;

        int[][] copy = solver.copyBoard(board);
        assertEquals(9, copy[0][0]);

        board[0][0] = 1;
        assertNotEquals(copy[0][0], board[0][0]);
    }

    @Test
    void testStepRecordingWhenUIEnabled() {
        int[][] board = new int[N][N];
        board[0][0] = 5;

        DLXSolver solver = new DLXSolver(N, true);
        solver.solve(board);

        assertTrue(solver.getStepCount() > 0);
        assertEquals(solver.getStepCount(), solver.getSteps().size());
    }

    @Test
    void testGuessCountAndPropagationDepth() {
        int[][] board = new int[N][N];
        DLXSolver solver = new DLXSolver(N, false);
        solver.solve(board);

        assertTrue(solver.getNumberOfGuesses() >= 0);
        assertTrue(solver.getPropagationDepth() > 0);
    }

   @Test
    void testDecodeSolutionUsingDLXNodes() {
        int[][] board = new int[N][N];
        board[0][0] = 5;

        DLXSolver solver = new DLXSolver(N, false);
        solver.initialPuzzle = solver.copyBoard(board); 
        List<DLXSolver.DLXNode> preset = new ArrayList<>();
        solver.buildDLXStructure(board, preset);

        int[][] decoded = solver.decodeSolution(preset);

        assertEquals(5, decoded[0][0], "decodeSolution should reflect preset value");
    }


    @Test
    void testInitialPuzzlePreserved() {
        int[][] board = new int[N][N];
        board[4][4] = 7;

        DLXSolver solver = new DLXSolver(N, false);
        solver.solve(board);

        assertEquals(7, solver.initialPuzzle[4][4]);
        assertEquals(7, solver.sudoku[4][4]);
    }

    @Test
    void testCoverAndUncoverRemoveAndRestoreRows() {
        DLXSolver solver = new DLXSolver(4, false);
        int[][] board = new int[4][4];
        List<DLXSolver.DLXNode> preset = new ArrayList<>();
        DLXSolver.DLXHeader header = solver.buildDLXStructure(board, preset);

        DLXSolver.ColumnHeader col = header.columns[0];
        int originalSize = col.size;

        assertTrue(originalSize > 0);
        assertNotSame(col.down, col);

        List<DLXSolver.DLXNode> originalDownNodes = new ArrayList<>();
        for (DLXSolver.DLXNode node = col.down; node != col; node = node.down) {
            originalDownNodes.add(node);
        }

        solver.cover(col);

        assertNotSame(col.right.left, col);
        assertNotSame(col.left.right, col);
        assertEquals(originalSize, col.size);

        solver.uncover(col);

        assertSame(col.right.left, col);
        assertSame(col.left.right, col);

        DLXSolver.DLXNode restored = col.down;
        for (DLXSolver.DLXNode expected : originalDownNodes) {
            assertSame(expected, restored);
            restored = restored.down;
        }

        assertSame(restored, col);
    }
}
