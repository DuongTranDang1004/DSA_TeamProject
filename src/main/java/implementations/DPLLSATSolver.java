package implementations;

import java.util.*;

public class DPLLSATSolver {
    private final int N;
    private final int MAX_DEPTH;
    private final Map<Integer,Boolean> assignment = new HashMap<>();
    private int propagationDepth = 0;
    private int numberOfGuesses  = 0;

    public DPLLSATSolver(int N) {
        int s = (int)Math.sqrt(N);
        if (s*s != N) {
            throw new IllegalArgumentException("N phải là số chính phương (9,16,25…) nhưng N=" + N);
        }
        this.N = N;
        this.MAX_DEPTH = N*N*N;
    }

    public int[][] solve(int[][] board) {
        if (board.length != N || board[0].length != N) return null;
        SudokuCnfEncoder enc = new SudokuCnfEncoder(N);
        int[][] clauses = enc.encodeSudoku(board);

        assignment.clear();
        propagationDepth = 0;
        numberOfGuesses  = 0;

        boolean sat = dpll(clauses, 0);
        return sat ? decode() : null;
    }

    private boolean dpll(int[][] C, int depth) {
        if (depth > MAX_DEPTH) return false;
        propagationDepth = Math.max(propagationDepth, depth);

        if (C.length == 0) return true;
        for (int[] cl : C) if (cl.length == 0) return false;

        Integer unit = findUnit(C);
        if (unit != null) {
            assign(unit);
            boolean ok = dpll(simplify(C, unit), depth + 1);
            if (!ok) unassign(unit);
            return ok;
        }

        Integer lit = chooseLiteral(C);
        if (lit == null) return true;

        numberOfGuesses++;
        assign(lit);
        if (dpll(simplify(C, lit), depth + 1)) return true;
        unassign(lit);

        assign(-lit);
        if (dpll(simplify(C, -lit), depth + 1)) return true;
        unassign(lit);

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
                if (!assignment.containsKey(Math.abs(lit))) {
                    return lit;
                }
            }
        }
        return null;
    }

    private void assign(int lit)   { assignment.put(Math.abs(lit), lit > 0); }
    private void unassign(int lit) { assignment.remove(Math.abs(lit)); }

    /**  
     * Simplify CNF C by setting lit=true:  
     *  - loại bỏ mọi clause chứa lit  
     *  - trong các clause còn lại, remove -lit  
     */
    private int[][] simplify(int[][] C, int lit) {
        List<int[]> out = new ArrayList<>(C.length);
        for (int[] cl : C) {
            boolean sat = false;
            // nếu clause chứa lit → bỏ hẳn
            for (int x : cl) {
                if (x == lit) { sat = true; break; }
            }
            if (sat) continue;

            // còn lại thì đếm size mới = số literal != -lit
            int cnt = 0;
            for (int x : cl) {
                if (x != -lit) cnt++;
            }
            // build clause mới
            int[] newCl = new int[cnt];
            int idx = 0;
            for (int x : cl) {
                if (x != -lit) newCl[idx++] = x;
            }
            out.add(newCl);
        }
        return out.toArray(new int[out.size()][]);
    }

    /** decode assignment → board */
    private int[][] decode() {
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