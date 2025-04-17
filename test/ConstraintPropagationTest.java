import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ConstraintPropagationTest {
    @Test
    void testInitializeConstraintsAndDomain() {
        int[][] board = new int[9][9];
        board[0][0] = 1;
        board[0][1] = 2;
        board[1][0] = 3;

        ConstraintPropagationSolver solver = new ConstraintPropagationSolver();
        solver.sudoku = board;
        solver.rowConstraints = new HashMap<>();
        solver.colConstraints = new HashMap<>();
        solver.boxConstraints = new HashMap<>();
        for (int i = 0; i < 9; i++) {
            solver.rowConstraints.put(i, new HashSet<>());
            solver.colConstraints.put(i, new HashSet<>());
            solver.boxConstraints.put(i, new HashSet<>());
        }

        solver.initializeConstraintsAndDomain();

        assertTrue(solver.rowConstraints.get(0).contains(1));
        assertTrue(solver.colConstraints.get(0).contains(1));
        assertTrue(solver.boxConstraints.get(0).contains(1));

        Set<Integer> domainSet = solver.domain.get("0,2");
        assertNotNull(domainSet);
        assertFalse(domainSet.contains(1));
        assertFalse(domainSet.contains(2));
        assertFalse(domainSet.contains(3));
    }

    @Test
    void testBacktrackReturnsFalseOnInvalidDomain() {
        ConstraintPropagationSolver solver = new ConstraintPropagationSolver();
        Map<String, Set<Integer>> invalidDomain = new HashMap<>();
        invalidDomain.put("0,0", new HashSet<>()); // No possible value

        boolean result = solver.backtrack(invalidDomain);
        assertFalse(result);
    }

    @Test
    void testPropagateRemovesValueFromPeers() {
        ConstraintPropagationSolver solver = new ConstraintPropagationSolver();
        Map<String, Set<Integer>> domain = new HashMap<>();
        domain.put("0,1", new HashSet<>(Set.of(1, 2, 3)));
        domain.put("1,0", new HashSet<>(Set.of(1, 2, 3)));
        domain.put("1,1", new HashSet<>(Set.of(1, 2, 3)));

        solver.propagate(0, 0, 2, domain);

        assertFalse(domain.get("0,1").contains(2));
        assertFalse(domain.get("1,0").contains(2));
        assertFalse(domain.get("1,1").contains(2));
    }

    @Test
    void testSelectCellWithMRVReturnsCorrectCell() {
        ConstraintPropagationSolver solver = new ConstraintPropagationSolver();
        Map<String, Set<Integer>> domain = new HashMap<>();
        domain.put("4,4", new HashSet<>(Set.of(1, 2, 3)));
        domain.put("0,0", new HashSet<>(Set.of(9))); // MRV = 1

        String selected = solver.selectCellWithMRV(domain);
        assertEquals("0,0", selected);
    }

    @Test
    void testDeepCopyCreatesIndependentCopy() {
        ConstraintPropagationSolver solver = new ConstraintPropagationSolver();
        Map<String, Set<Integer>> original = new HashMap<>();
        original.put("0,0", new HashSet<>(Set.of(1, 2)));

        Map<String, Set<Integer>> copy = solver.deepCopy(original);
        assertEquals(original.get("0,0"), copy.get("0,0"));

        // Modify copy
        copy.get("0,0").remove(1);

        assertNotEquals(original.get("0,0"), copy.get("0,0"));
    }

    @Test
    void testGetBoxIndexCorrectness() {
        ConstraintPropagationSolver solver = new ConstraintPropagationSolver();

        assertEquals(0, solver.getBoxIndex(0, 0));
        assertEquals(4, solver.getBoxIndex(4, 4));
        assertEquals(8, solver.getBoxIndex(8, 8));
        assertEquals(3, solver.getBoxIndex(3, 0));
        assertEquals(5, solver.getBoxIndex(5, 8));
    }
}
