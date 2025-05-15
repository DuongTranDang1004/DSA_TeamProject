import implementations.BackTrackingSolver;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/*
 * ============================================
 *       BackTrackingSolverTest Class
 * ============================================
 * User For: Unit testing the Backtracking + Constraint Propagation solver for Sudoku. Verifies logic, recursion, timeout, and heuristics.
 * Written By: Group 1 in @RMIT - 2025 for Group Project of COSC2469 Algorithm And Analysis Course
 * ============================================
 */

public class BackTrackingSolverTest {

    private static final int N = 9;

    @Test
    void testSolveValidSudokuCorrectly() {
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

        BackTrackingSolver solver = new BackTrackingSolver(N, false);
        int[][] solved = solver.solve(board);
        assertNotNull(solved);

        for (int i = 0; i < N; i++) {
            Set<Integer> row = new HashSet<>();
            Set<Integer> col = new HashSet<>();
            for (int j = 0; j < N; j++) {
                row.add(solved[i][j]);
                col.add(solved[j][i]);
            }
            assertEquals(N, row.size(), "Row must contain unique digits");
            assertEquals(N, col.size(), "Column must contain unique digits");
        }
    }

    @Test
    void testInvalidBoardSizeThrowsException() {
        int[][] invalid = new int[8][9];
        BackTrackingSolver solver = new BackTrackingSolver(N, false);
        assertThrows(IllegalArgumentException.class, () -> solver.solve(invalid));
    }

    @Test
    void testCellKeyCreatesCorrectString() {
        assertEquals("2,3", BackTrackingSolver.cellKey(2, 3));
    }

    @Test
    void testGetBoxIndexCorrectness() {
        BackTrackingSolver solver = new BackTrackingSolver(9, false);
        assertEquals(0, solver.getBoxIndex(0, 0));
        assertEquals(4, solver.getBoxIndex(4, 4));
        assertEquals(8, solver.getBoxIndex(8, 8));
    }

    @Test
    void testConstraintInitialization() {
        int[][] board = new int[N][N];
        board[0][0] = 1;
        board[1][1] = 2;
        board[2][2] = 3;

        BackTrackingSolver solver = new BackTrackingSolver(N, false);
        solver.sudoku = board;
        solver.findInitialConstraints();

        assertTrue(solver.rowConstraints.get(0).contains(1));
        assertTrue(solver.colConstraints.get(1).contains(2));
        assertTrue(solver.boxConstraints.get(solver.getBoxIndex(2, 2)).contains(3));
    }

    @Test
    void testGuessCountAndDepth() {
        int[][] empty = new int[N][N];
        BackTrackingSolver solver = new BackTrackingSolver(N, false);
        solver.solve(empty);

        assertTrue(solver.getNumberOfGuesses() > 0);
        assertTrue(solver.getPropagationDepth() > 0);
    }

    @Test
    void testStepRecordingWhenUIEnabled() {
        int[][] board = new int[N][N];
        BackTrackingSolver solver = new BackTrackingSolver(N, true);
        solver.solve(board);

        assertEquals(solver.getStepCount(), solver.getSteps().size());
        assertTrue(solver.getStepCount() > 0);
    }

    @Test
    void testIsValidBoardReturnsFalseOnBadBoard() {
        BackTrackingSolver solver = new BackTrackingSolver(N, false);
        assertFalse(solver.isValidBoard(new int[8][9]));
        assertFalse(solver.isValidBoard(new int[9][8]));

        int[][] bad = new int[N][N];
        bad[0][0] = -1;
        assertFalse(solver.isValidBoard(bad));

        bad[0][0] = 10;
        assertFalse(solver.isValidBoard(bad));
    }

    @Test
    void testSetTimeoutMillis() {
        BackTrackingSolver solver = new BackTrackingSolver(N, false);
        solver.setTimeoutMillis(5000);
        assertEquals(5000, solver.timeoutMillis);
    }
}
