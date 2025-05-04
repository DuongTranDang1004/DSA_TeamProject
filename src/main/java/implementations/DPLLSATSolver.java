package implementations;

import java.util.*;

public class DPLLSATSolver {
    private final int N;
    private final int MAX_DEPTH;
    private final Map<Integer,Boolean> assignment = new HashMap<>();
    private int propagationDepth = 0;
    private int numberOfGuesses  = 0;

    public DPLLSATSolver(int N) {
        if (Math.sqrt(N) != (int)Math.sqrt(N))
            throw new IllegalArgumentException("N must be a perfect square.");
        this.N = N;
        this.MAX_DEPTH = N*N*N;
    }

    public int[][] solve(int[][] board) {
        if (board.length!=N || board[0].length!=N) return null;
        SudokuCnfEncoder enc = new SudokuCnfEncoder(N);
        int[][] clauses = enc.encodeSudoku(board);
        assignment.clear();
        propagationDepth = 0;
        numberOfGuesses  = 0;
        boolean sat = dpll(clauses, 0);
        // 4) decode or return null
        return sat ? decode(board) : null;
    }

    private boolean dpll(int[][] C, int depth) {
        if (depth > MAX_DEPTH) return false;
        propagationDepth = Math.max(propagationDepth, depth);

        // empty CNF => satisfied
        if (C.length == 0) return true;
        // any empty clause => unsatisfied
        for (int[] cl : C) if (cl.length == 0) return false;

        // --- unit propagation with backtrack ---
        Integer unit = findUnit(C);
        if (unit != null) {
            assign(unit);
            boolean ok = dpll(simplify(C, unit), depth + 1);
            if (!ok) unassign(unit);   // <-- backtrack unit assignment
            return ok;
        }

        // --- choose a literal & branch ---
        Integer lit = chooseLiteral(C);
        if (lit == null) return true;  // no literals left

        numberOfGuesses++;
        // try lit = true
        assign(lit);
        if (dpll(simplify(C, lit), depth + 1)) return true;
        unassign(lit);

        // try lit = false
        assign(-lit);
        if (dpll(simplify(C, -lit), depth + 1)) return true;
        unassign(-lit);

        return false;
    }

    private Integer findUnit(int[][] C) {
        for (int[] cl : C) {
            if (cl.length == 1) return cl[0];
        }
        return null;
    }

    private Integer chooseLiteral(int[][] C) {
        for (int[] cl : C) {
            for (int lit : cl) {
                if (!assignment.containsKey(Math.abs(lit)))
                    return lit;
            }
        }
        return null;
    }

    private void assign(int lit)   { assignment.put(Math.abs(lit), lit > 0); }
    private void unassign(int lit) { assignment.remove(Math.abs(lit)); }

    private int[][] simplify(int[][] C, int lit) {
        List<int[]> out = new ArrayList<>();
        for (int[] cl : C) {
            boolean sat = false;
            for (int x : cl) if (x == lit) { sat = true; break; }
            if (sat) continue;
            List<Integer> tmp = new ArrayList<>();
            for (int x : cl) if (x != -lit) tmp.add(x);
            out.add(tmp.stream().mapToInt(i->i).toArray());
        }
        return out.toArray(new int[0][]);
    }

    private int[][] decode(int[][] orig) {
        int[][] res = new int[N][N];
        for (Map.Entry<Integer,Boolean> e : assignment.entrySet()) {
            if (!e.getValue()) continue;
            int v = e.getKey() - 1;
            int d = v % N + 1;
            int c = (v / N) % N + 1;
            int r = v / (N*N) + 1;
            res[r-1][c-1] = d;
        }
        return res;
    }

    public int getPropagationDepth() { return propagationDepth; }
    public int getNumberOfGuesses()   { return numberOfGuesses; }
}
