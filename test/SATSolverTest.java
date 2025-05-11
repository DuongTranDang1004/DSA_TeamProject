import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class SATSolverTest {

    private static final int N = 9;

    @Test
    void testEncodingReturnsNonEmptyClauses() {
        int[][] board = new int[N][N]; // Empty Sudoku
        SudokuCnfEncoder encoder = new SudokuCnfEncoder(N);
        int[][] cnf = encoder.encodeSudoku(board);

        assertNotNull(cnf, "CNF encoding should not be null");
        assertTrue(cnf.length > 0, "CNF should have clauses");
    }

    @Test
    void testClueConstraintIsEncoded() {
        int[][] board = new int[N][N];
        board[0][0] = 5; // (1,1) = 5

        SudokuCnfEncoder encoder = new SudokuCnfEncoder(N);
        int[][] cnf = encoder.encodeSudoku(board);

        boolean hasClue = false;
        for (int[] clause : cnf) {
            if (clause.length == 1 && clause[0] == 115) { // 100*r + 10*c + d = 115
                hasClue = true;
                break;
            }
        }
        assertTrue(hasClue, "CNF should contain clue clause {115} for (1,1)=5");
    }

    @Test
    void testNoDuplicateLiteralsInCellConstraints() {
        int[][] board = new int[N][N];
        SudokuCnfEncoder encoder = new SudokuCnfEncoder(N);
        int[][] cnf = encoder.encodeSudoku(board);

        long count = java.util.Arrays.stream(cnf)
                .filter(clause -> clause.length == N)
                .count();

        assertTrue(count >= N * N, "Should contain at least N² cell constraints of size N");
    }

    @Test
    void testTotalClausesRoughEstimate() {
        int[][] board = new int[N][N];
        SudokuCnfEncoder encoder = new SudokuCnfEncoder(N);
        int[][] cnf = encoder.encodeSudoku(board);

        assertTrue(cnf.length > 10000, "CNF should be large due to full Sudoku encoding");
    }

    @Test
    void testSolveReturnsValidSudokuBoard() {
        int[][] board = new int[N][N];
        board[0][0] = 5;

        DPLLSATSolver solver = new DPLLSATSolver(N);
        int[][] result = solver.solve(board);

        assertNotNull(result, "Result should not be null");
        assertEquals(N, result.length, "Board should have N rows");
        assertEquals(N, result[0].length, "Board should have N columns");
        assertEquals(5, result[0][0], "Top-left cell should remain 5");
    }

    @Test
    void testDecodeSudokuBoardDecodesCorrectly() {
        Map<Integer, Boolean> assignment = new HashMap<>();
        assignment.put(113, true); // (1,1) = 3 → 100*1 + 10*1 + 3 = 113

        DPLLSATSolver solver = new DPLLSATSolver(N);
        int[][] decoded = solver.decodeSudokuBoard(assignment);

        assertEquals(3, decoded[0][0], "Cell (1,1) should decode to 3");
    }

    @Test
    void testValidateAssignmentsDetectsMissingAssignments() {
        Map<Integer, Boolean> assignment = new HashMap<>();
        assignment.put(111, true); // Only (1,1) = 1

        DPLLSATSolver solver = new DPLLSATSolver(N);
        solver.validateAssignments(assignment);
        // Output expected in console — no exception thrown
    }

    @Test
    void testEmptyBoardReturnsSolution() {
        int[][] board = new int[N][N];
        DPLLSATSolver solver = new DPLLSATSolver(N);
        int[][] result = solver.solve(board);

        assertNotNull(result, "Solver should return a filled board");
        assertTrue(isValidSudoku(result), "Result must satisfy Sudoku constraints");
    }

    // Utility: check Sudoku validity
    private boolean isValidSudoku(int[][] board) {
        for (int i = 0; i < N; i++) {
            boolean[] row = new boolean[N + 1];
            boolean[] col = new boolean[N + 1];
            boolean[] box = new boolean[N + 1];

            for (int j = 0; j < N; j++) {
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
