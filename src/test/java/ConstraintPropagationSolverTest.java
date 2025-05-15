import implementations.ConstraintPropagationSolver;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/*
 * ============================================
 *       ConstraintPropagationSolverTest Class
 * ============================================
 * User For: Testing the constraint propagation + backtracking based Sudoku solver. Verifies correctness of constraint setup, 
 * domain pruning, MRV heuristic, and solver performance metrics.
 * Written By: Group 1 in @RMIT - 2025 for Group Project of COSC2469 Algorithm And Analysis Course
 * ============================================
 */

public class ConstraintPropagationSolverTest {

    private static final int N = 9;

    @Test
    void testSolveValidSudoku() {
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

        ConstraintPropagationSolver solver = new ConstraintPropagationSolver(N, false);
        int[][] result = solver.solve(board);

        assertNotNull(result);
        for (int i = 0; i < N; i++) {
            Set<Integer> row = new HashSet<>();
            Set<Integer> col = new HashSet<>();
            for (int j = 0; j < N; j++) {
                row.add(result[i][j]);
                col.add(result[j][i]);
            }
            assertEquals(N, row.size());
            assertEquals(N, col.size());
        }
    }

    @Test
    void testInvalidBoardSizeReturnsFalse() {
        ConstraintPropagationSolver solver = new ConstraintPropagationSolver(N, false);
        assertThrows(IllegalArgumentException.class, () -> solver.solve(new int[8][8]));
    }

    @Test
    void testInitializeConstraintsAndDomain() {
        ConstraintPropagationSolver solver = new ConstraintPropagationSolver(N, false);
        solver.sudoku = new int[N][N];
        solver.sudoku[0][0] = 5;
        solver.sudoku[1][1] = 3;

        for (int i = 0; i < N; i++) {
            solver.rowConstraints.put(i, new HashSet<>());
            solver.colConstraints.put(i, new HashSet<>());
            solver.boxConstraints.put(i, new HashSet<>());
        }

        solver.initializeConstraintsAndDomain();

        assertTrue(solver.rowConstraints.get(0).contains(5));
        assertTrue(solver.colConstraints.get(1).contains(3));
        assertTrue(solver.boxConstraints.get(solver.getBoxIndex(1, 1, 3)).contains(3));
        assertFalse(solver.domain.containsKey("0,0"));
    }

    @Test
    void testBacktrackEmptyBoardSucceeds() {
        ConstraintPropagationSolver solver = new ConstraintPropagationSolver(N, false);
        solver.sudoku = new int[N][N];
        for (int i = 0; i < N; i++) {
            solver.rowConstraints.put(i, new HashSet<>());
            solver.colConstraints.put(i, new HashSet<>());
            solver.boxConstraints.put(i, new HashSet<>());
        }
        solver.initializeConstraintsAndDomain();
        boolean solved = solver.backtrack(new HashMap<>(solver.domain), 0);
        assertTrue(solved);
    }

    @Test
    void testPropagationRemovesFromDomain() {
        ConstraintPropagationSolver solver = new ConstraintPropagationSolver(4, false);
        solver.sudoku = new int[4][4];
        for (int i = 0; i < 4; i++) {
            solver.rowConstraints.put(i, new HashSet<>());
            solver.colConstraints.put(i, new HashSet<>());
            solver.boxConstraints.put(i, new HashSet<>());
        }

        Map<String, Set<Integer>> dom = new HashMap<>();
        dom.put("0,0", new HashSet<>(Set.of(1, 2, 3)));
        dom.put("0,1", new HashSet<>(Set.of(2, 3)));
        dom.put("1,0", new HashSet<>(Set.of(1, 3)));

        solver.propagate(0, 0, 3, dom, 2);

        assertFalse(dom.get("0,0").contains(3));
        assertFalse(dom.get("0,1").contains(3));
        assertFalse(dom.get("1,0").contains(3));
    }

    @Test
    void testSelectCellWithMRVReturnsMinDomainKey() {
        ConstraintPropagationSolver solver = new ConstraintPropagationSolver(4, false);
        Map<String, Set<Integer>> dom = new HashMap<>();
        dom.put("0,0", Set.of(1, 2, 3));
        dom.put("1,1", Set.of(2));
        dom.put("2,2", Set.of(1, 2));

        String key = solver.selectCellWithMRV(dom);
        assertEquals("1,1", key);
    }

    @Test
    void testDeepCopyCreatesIndependentDomain() {
        ConstraintPropagationSolver solver = new ConstraintPropagationSolver(4, false);
        Map<String, Set<Integer>> original = new HashMap<>();
        original.put("0,0", new HashSet<>(Set.of(1, 2)));

        Map<String, Set<Integer>> copy = solver.deepCopy(original);
        assertEquals(original.get("0,0"), copy.get("0,0"));
        copy.get("0,0").remove(1);
        assertNotEquals(original.get("0,0"), copy.get("0,0"));
    }

    @Test
    void testGetBoxIndexIsCorrect() {
        ConstraintPropagationSolver solver = new ConstraintPropagationSolver(9, false);
        assertEquals(0, solver.getBoxIndex(1, 1, 3));
        assertEquals(4, solver.getBoxIndex(4, 4, 3));
        assertEquals(8, solver.getBoxIndex(8, 8, 3));
    }

    @Test
    void testStepTrackingWhenUIEnabled() {
        int[][] board = new int[N][N];
        ConstraintPropagationSolver solver = new ConstraintPropagationSolver(N, true);
        solver.solve(board);

        assertTrue(solver.getStepCount() > 0);
        assertEquals(solver.getStepCount(), solver.getSteps().size());
    }

    @Test
    void testGuessCountAndPropagationDepthTracked() {
        int[][] board = new int[N][N];
        ConstraintPropagationSolver solver = new ConstraintPropagationSolver(N, false);
        solver.solve(board);

        assertTrue(solver.getNumberOfGuesses() >= 0);
        assertTrue(solver.getPropagationDepth() > 0);
    }
}
