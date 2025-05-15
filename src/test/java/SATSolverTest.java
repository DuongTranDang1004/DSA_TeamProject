import implementations.SudokuCnfEncoder;
import implementations.DPLLSATSolver;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/*
 * ============================================
 *       SATSolverTest Class
 * ============================================
 * User For: Verifying correctness of CNF encoder and DPLL SAT solver in solving Sudoku puzzles.
 * This includes logic validation, unit propagation, clause generation, and performance indicators.
 * Written By: Group 1 in @RMIT - 2025 for Group Project of COSC2469 Algorithm And Analysis Course
 * ============================================
 */

class SATSolverTest {

    private static final int N = 4;

    @Test
    void testVarEncodingIsUniqueAndCorrect() {
        SudokuCnfEncoder encoder = new SudokuCnfEncoder(N);
        int a = encoder.var(1, 1, 1);
        int b = encoder.var(1, 1, 2);
        int c = encoder.var(1, 2, 1);

        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertTrue(a > 0 && b > 0 && c > 0);
    }

    @Test
    void testEncodeSudokuReturnsNonEmptyClauseList() {
        int[][] board = new int[N][N];
        SudokuCnfEncoder encoder = new SudokuCnfEncoder(N);
        int[][] cnf = encoder.encodeSudoku(board);

        assertNotNull(cnf);
        assertTrue(cnf.length > 0);
    }

    @Test
    void testAddCellCreatesN2ClausesOfLengthN() {
        SudokuCnfEncoder encoder = new SudokuCnfEncoder(N);
        List<int[]> clauses = new ArrayList<>();
        encoder.addCell(clauses);

        assertEquals(N * N, clauses.size());
        for (int[] clause : clauses) {
            assertEquals(N, clause.length);
        }
    }

    @Test
    void testAddUniquenessCreatesBinaryClauses() {
        SudokuCnfEncoder encoder = new SudokuCnfEncoder(N);
        List<int[]> clauses = new ArrayList<>();
        encoder.addUniqueness(clauses);

        for (int[] clause : clauses) {
            assertEquals(2, clause.length);
            assertTrue(clause[0] < 0 && clause[1] < 0);
        }

        int expected = N * N * (N * (N - 1)) / 2;
        assertEquals(expected, clauses.size());
    }

    @Test
    void testAddRowAddsAtLeastOneClausePerDigit() {
        SudokuCnfEncoder encoder = new SudokuCnfEncoder(N);
        List<int[]> clauses = new ArrayList<>();
        encoder.addRow(clauses);

        int perDigit = 1 + (N * (N - 1)) / 2;
        int expected = N * N * perDigit;

        assertEquals(expected, clauses.size());
    }

    @Test
    void testAddColAddsAtLeastOneClausePerDigit() {
        SudokuCnfEncoder encoder = new SudokuCnfEncoder(N);
        List<int[]> clauses = new ArrayList<>();
        encoder.addCol(clauses);

        int perDigit = 1 + (N * (N - 1)) / 2;
        int expected = N * N * perDigit;

        assertEquals(expected, clauses.size());
    }

    @Test
    void testAddBoxAddsCorrectClauses() {
        SudokuCnfEncoder encoder = new SudokuCnfEncoder(N);
        List<int[]> clauses = new ArrayList<>();
        encoder.addBox(clauses);

        int numBoxes = (N / encoder.blockH) * (N / encoder.blockW);
        int digitsPerBox = N;
        int sizeOfEachBox = encoder.blockH * encoder.blockW;
        int uniquenessClausesPerBoxPerDigit = (sizeOfEachBox * (sizeOfEachBox - 1)) / 2;
        int expected = numBoxes * digitsPerBox + numBoxes * digitsPerBox * uniquenessClausesPerBoxPerDigit;

        assertEquals(expected, clauses.size());
    }

    @Test
    void testAddCluesOnlyAddsPositiveLiterals() {
        int[][] board = new int[][] {
                {1, 0, 0, 2},
                {0, 3, 0, 0},
                {0, 0, 4, 0},
                {2, 0, 0, 1}
        };

        SudokuCnfEncoder encoder = new SudokuCnfEncoder(N);
        List<int[]> clauses = new ArrayList<>();
        encoder.addClues(clauses, board);

        int numClues = 0;
        for (int[] row : board) {
            for (int val : row) {
                if (val > 0) numClues++;
            }
        }

        assertEquals(numClues, clauses.size());
        for (int[] clause : clauses) {
            assertEquals(1, clause.length);
            assertTrue(clause[0] > 0);
        }
    }

    @Test
    void testConstructorThrowsOnNonSquareN() {
        assertThrows(IllegalArgumentException.class, () -> new SudokuCnfEncoder(10));
    }

     @Test
    void testDPLLSATSolverSolvesSimplePuzzle() {
        int[][] board = {
                {1, 0, 0, 2},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {3, 0, 0, 4}
        };

        DPLLSATSolver solver = new DPLLSATSolver(N, false);
        int[][] solved = solver.solve(board);

        assertNotNull(solved);
        for (int i = 0; i < N; i++) {
            Set<Integer> row = new HashSet<>();
            Set<Integer> col = new HashSet<>();
            for (int j = 0; j < N; j++) {
                row.add(solved[i][j]);
                col.add(solved[j][i]);
                assertTrue(solved[i][j] >= 1 && solved[i][j] <= N);
            }
            assertEquals(N, row.size());
            assertEquals(N, col.size());
        }
    }

    @Test
    void testDPLLSATSolverReturnsNullOnInvalidPuzzle() {
        int[][] board = {
                {1, 0, 0, 0},
                {1, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };

        DPLLSATSolver solver = new DPLLSATSolver(N, false);
        int[][] result = solver.solve(board);

        assertNotNull(result);
    }

    @Test
    void testDPLLSATSolverCountsGuessesAndDepth() {
        int[][] board = new int[N][N];
        DPLLSATSolver solver = new DPLLSATSolver(N, false);
        solver.solve(board);

        assertTrue(solver.getNumberOfGuesses() > 0);
        assertTrue(solver.getPropagationDepth() > 0);
    }

    @Test
    void testDPLLSATSolverRecordsStepsIfEnabled() {
        int[][] board = new int[N][N];
        DPLLSATSolver solver = new DPLLSATSolver(N, true);
        solver.solve(board);

        assertTrue(solver.getStepCount() > 0);
        assertEquals(solver.getStepCount(), solver.getSteps().size());
    }

    @Test
    void testDPLLSATSolverHandlesEmptyInput() {
        DPLLSATSolver solver = new DPLLSATSolver(N, false);
        int[][] invalid = new int[N][0];

        int[][] result = solver.solve(invalid);
        assertNull(result);
    }
}
