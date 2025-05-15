import implementations.SudokuCnfEncoder;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SudokuCnfEncoderTest {

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
        int[][] board = new int[N][N]; // Empty board
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
            assertTrue(clause[0] < 0 && clause[1] < 0, "Should be negative literals");
        }

        // Expected number of uniqueness clauses:
        // N² cells × (N choose 2) unique digit pairs per cell
        int expected = N * N * (N * (N - 1)) / 2;
        assertEquals(expected, clauses.size());
    }

    @Test
    void testAddRowAddsAtLeastOneClausePerDigit() {
        SudokuCnfEncoder encoder = new SudokuCnfEncoder(N);
        List<int[]> clauses = new ArrayList<>();
        encoder.addRow(clauses);

        // Should include 1 clause of length N and (N choose 2) binary exclusions for each digit per row
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
        SudokuCnfEncoder encoder = new SudokuCnfEncoder(N); // For 4x4, blocks are 2x2
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
}
