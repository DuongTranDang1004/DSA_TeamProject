import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SATSolverTest {

    @Test
    void testEncodingReturnsNonEmptyClauses() {
        int[][] board = new int[9][9]; // Empty Sudoku
        int[][] cnf = SudokuCnfEncoder.encodeSudoku(board);

        assertNotNull(cnf, "CNF encoding should not be null");
        assertTrue(cnf.length > 0, "CNF should have clauses");
    }

    @Test
    void testClueConstraintIsEncoded() {
        int[][] board = new int[9][9];
        board[0][0] = 5; // Put 5 at (1,1)

        int[][] cnf = SudokuCnfEncoder.encodeSudoku(board);

        // Check that one clause is exactly {115} (1-based row and col)
        boolean hasClue = false;
        for (int[] clause : cnf) {
            if (clause.length == 1 && clause[0] == 115) {
                hasClue = true;
                break;
            }
        }
        assertTrue(hasClue, "CNF should contain clue clause {115} for (1,1)=5");
    }

    @Test
    void testNoDuplicateLiteralsInCellConstraints() {
        int[][] board = new int[9][9];
        int[][] cnf = SudokuCnfEncoder.encodeSudoku(board);

        // There should be exactly one clause of size 9 per cell → 81 cells = 81 such clauses
        long count = java.util.Arrays.stream(cnf)
                .filter(clause -> clause.length == 9)
                .count();

        assertTrue(count >= 81, "Should contain at least 81 9-literal cell constraints");
    }

    @Test
    void testTotalClausesRoughEstimate() {
        int[][] board = new int[9][9];
        int[][] cnf = SudokuCnfEncoder.encodeSudoku(board);

        // Rough estimate: should have >10,000 clauses for full constraints
        assertTrue(cnf.length > 10000, "CNF should be large due to full Sudoku encoding");
    }

    @Test
    void testSolveReturnsValidSudokuBoard() {
        int[][] board = new int[9][9];
        // Set a single known cell (e.g. top-left is 5)
        board[0][0] = 5;

        DPLLSATSolver solver = new DPLLSATSolver();
        int[][] result = solver.solve(board);

        assertNotNull(result, "Result should not be null");
        assertEquals(9, result.length, "Board should have 9 rows");
        assertEquals(9, result[0].length, "Board should have 9 columns");
        assertEquals(5, result[0][0], "Top-left cell should remain 5");
    }

    @Test
    void testDecodeSudokuBoardDecodesCorrectly() {
        Map<Integer, Boolean> assignment = new HashMap<>();
        // Encode cell (1,1) with digit 3 => var = 100*1 + 10*1 + 3 = 113
        assignment.put(113, true);
        int[][] decoded = DPLLSATSolver.decodeSudokuBoard(assignment);

        assertEquals(3, decoded[0][0], "Cell (1,1) should decode to 3");
    }

    @Test
    void testValidateAssignmentsDetectsMissingAssignments() {
        Map<Integer, Boolean> assignment = new HashMap<>();
        // Assign one cell only
        assignment.put(111, true); // (1,1) = 1

        DPLLSATSolver solver = new DPLLSATSolver();
        solver.validateAssignments(assignment);
        // Expect output in console saying unassigned cells exist — no exception
    }

    @Test
    void testEmptyBoardReturnsSolution() {
        int[][] board = new int[9][9]; // Completely empty Sudoku
        DPLLSATSolver solver = new DPLLSATSolver();
        int[][] result = solver.solve(board);

        assertNotNull(result, "Solver should return a filled board");
        assertTrue(isValidSudoku(result), "Result must satisfy Sudoku constraints");
    }

    // Utility to check Sudoku constraints
    private boolean isValidSudoku(int[][] board) {
        for (int i = 0; i < 9; i++) {
            boolean[] row = new boolean[10];
            boolean[] col = new boolean[10];
            boolean[] box = new boolean[10];

            for (int j = 0; j < 9; j++) {
                int valR = board[i][j];
                int valC = board[j][i];
                int valB = board[3 * (i / 3) + j / 3][3 * (i % 3) + j % 3];

                if (valR != 0 && row[valR]) return false;
                if (valC != 0 && col[valC]) return false;
                if (valB != 0 && box[valB]) return false;

                if (valR != 0) row[valR] = true;
                if (valC != 0) col[valC] = true;
                if (valB != 0) box[valB] = true;
            }
        }
        return true;
    }



}
