package implementations;

import java.util.*;

public class DPLLSATSolver {
    private final int size;
    private final int maxRecursionDepth;
    private final Map<Integer, Boolean> variableAssignments = new HashMap<>();
    private int maxPropagationDepth = 0;
    private int totalGuessCount = 0;

    public DPLLSATSolver(int size) {
        int root = (int) Math.sqrt(size);
        if (root * root != size) {
            throw new IllegalArgumentException("N must be a perfect square (e.g., 9, 16, 25), but got: " + size);
        }
        this.size = size;
        this.maxRecursionDepth = size * size * size;
    }

    public int[][] solve(int[][] board) {
        if (board.length != size || board[0].length != size) return null;

        SudokuCnfEncoder encoder = new SudokuCnfEncoder(size);
        int[][] cnfClauses = encoder.encodeSudoku(board);

        variableAssignments.clear();
        maxPropagationDepth = 0;
        totalGuessCount = 0;

        boolean satisfiable = runDPLL(cnfClauses, 0);
        return satisfiable ? decodeAssignmentsToBoard() : null;
    }

    private boolean runDPLL(int[][] clauses, int currentDepth) {
        if (currentDepth > maxRecursionDepth) return false;
        maxPropagationDepth = Math.max(maxPropagationDepth, currentDepth);

        if (clauses.length == 0) return true;
        for (int[] clause : clauses) {
            if (clause.length == 0) return false;}

        Integer unitLiteral = findUnitClause(clauses);
        if (unitLiteral != null) {
            assignLiteral(unitLiteral);
            boolean result = runDPLL(simplifyClauses(clauses, unitLiteral), currentDepth + 1);
            if (!result) unassignLiteral(unitLiteral);
            return result;
        }

        Integer chosenLiteral = chooseUnassignedLiteral(clauses);
        if (chosenLiteral == null) return true;

        totalGuessCount++;
        assignLiteral(chosenLiteral);
        if (runDPLL(simplifyClauses(clauses, chosenLiteral), currentDepth + 1)) return true;
        unassignLiteral(chosenLiteral);

        assignLiteral(-chosenLiteral);
        if (runDPLL(simplifyClauses(clauses, -chosenLiteral), currentDepth + 1)) return true;
        unassignLiteral(chosenLiteral);

        return false;
    }

    private Integer findUnitClause(int[][] clauses) {
        for (int[] clause : clauses) {
            if (clause.length == 1) return clause[0];
        }
        return null;
    }

    private Integer chooseUnassignedLiteral(int[][] clauses) {
        for (int[] clause : clauses) {
            for (int literal : clause) {
                if (!variableAssignments.containsKey(Math.abs(literal))) {
                    return literal;
                }
            }
        }
        return null;
    }

    private void assignLiteral(int literal) {
        variableAssignments.put(Math.abs(literal), literal > 0);}

    private void unassignLiteral(int literal) {
        variableAssignments.remove(Math.abs(literal));
    }

    private int[][] simplifyClauses(int[][] clauses, int literal) {
        List<int[]> simplified = new ArrayList<>(clauses.length);
        for (int[] clause : clauses) {
            boolean isSatisfied = false;
            for (int l : clause) {
                if (l == literal) {
                    isSatisfied = true;
                    break; }}
            if (isSatisfied) continue;
            int count = 0;
            for (int l : clause) {
                if (l != -literal) count++;  }
            int[] newClause = new int[count];
            int index = 0;
            for (int l : clause) {
                if (l != -literal) newClause[index++] = l;  }
            simplified.add(newClause);  }
        return simplified.toArray(new int[0][]);
    }

    private int[][] decodeAssignmentsToBoard() {
        int[][] resultBoard = new int[size][size];
        for (Map.Entry<Integer, Boolean> entry : variableAssignments.entrySet()) {
            if (!entry.getValue()) continue;

            int var = entry.getKey() - 1;
            int digit = var % size + 1;
            int col = (var / size) % size + 1;
            int row = var / (size * size) + 1;
            resultBoard[row - 1][col - 1] = digit;
        }
        return resultBoard;
    }

    public int getPropagationDepth() {
        return maxPropagationDepth;
    }

    public int getNumberOfGuesses() {
        return totalGuessCount;
    }
}
