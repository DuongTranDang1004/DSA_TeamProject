import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class BacktrackingTest {

    private static final int N = 9;

    @Test
    void testFindInitialRowConstraint() {
        int[][] board = new int[N][N];
        board[0][0] = 5;

        BackTrackingSolver solver = new BackTrackingSolver(N);
        solver.sudoku = board;
        solver.findInitialRowConstraint();

        assertTrue(solver.rowConstraints.get(0).contains(5));
        assertFalse(solver.rowConstraints.get(1).contains(5));
    }

    @Test
    void testFindInitialColConstraint() {
        int[][] board = new int[N][N];
        board[0][1] = 4;

        BackTrackingSolver solver = new BackTrackingSolver(N);
        solver.sudoku = board;
        solver.findInitialColConstraint();

        assertTrue(solver.colConstraints.get(1).contains(4));
        assertFalse(solver.colConstraints.get(0).contains(4));
    }

    @Test
    void testFindInitialBoxConstraint() {
        int[][] board = new int[N][N];
        board[1][1] = 9;  // box 0
        board[4][4] = 6;  // box 4
        board[8][8] = 3;  // box 8

        BackTrackingSolver solver = new BackTrackingSolver(N);
        solver.sudoku = board;
        solver.findInitialBoxConstraint();

        assertTrue(solver.boxConstraints.get(0).contains(9));
        assertTrue(solver.boxConstraints.get(4).contains(6));
        assertTrue(solver.boxConstraints.get(8).contains(3));
    }

    @Test
    void testInitializeDomainFromConstraints() {
        int[][] board = new int[N][N];
        board[0][0] = 1;
        board[0][1] = 2;
        board[1][0] = 3;
        board[1][1] = 4;

        BackTrackingSolver solver = new BackTrackingSolver(N);
        solver.sudoku = board;
        solver.findInitialRowConstraint();
        solver.findInitialColConstraint();
        solver.findInitialBoxConstraint();

        Map<String, Set<Integer>> domain = solver.initializeDomainFromConstraints();
        Set<Integer> domainSet = domain.get("0,2");

        assertNotNull(domainSet);
        assertFalse(domainSet.contains(1));
        assertFalse(domainSet.contains(2));
        assertFalse(domainSet.contains(3));
        assertFalse(domainSet.contains(4));
    }

    @Test
    void testCellKeyAndExtractRowAndCol() {
        String key = BackTrackingSolver.cellKey(4, 7);
        assertEquals("4,7", key);

        int[] rc = BackTrackingSolver.extractRowAndCol(key);
        assertEquals(4, rc[0]);
        assertEquals(7, rc[1]);
    }
}
