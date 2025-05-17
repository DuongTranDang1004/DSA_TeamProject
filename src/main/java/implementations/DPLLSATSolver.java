package implementations;

import java.util.*;

/*
 * ============================================
 *       DPLLSATSolver Class
 * ============================================
 * User For: Solving Sudoku using the DPLL SAT solver, which uses Boolean satisfiability to find a solution.
 * Written By: Group 1 in @RMIT - 2025 for Group Project of COSC2469 Algorithm And Analysis Course
 * ============================================
 */

public class DPLLSATSolver {
    public final int N;
    public final int maxRecursionDepth;
    public final Map<Integer, Boolean> variableAssignments = new HashMap<>();
    public int maxPropagationDepth = 0;
    public int totalGuessCount = 0;
    public boolean isRunningInUI = false;
    public int stepCount = 0;
    public List<int[][]> steps = new ArrayList<>();  

    public DPLLSATSolver(int N, boolean isRunningInUI) {
        int root = (int) Math.sqrt(N);
        if (root * root != N) {
            throw new IllegalArgumentException("Board must be " + N + "x" + N + " and contain values from 0 to " + N);
        }
        this.N = N;
        this.maxRecursionDepth = N * N * N;
        this.isRunningInUI = isRunningInUI;
    }

    public int[][] solve(int[][] board) {
        if (board.length != N || board[0].length != N) return null;

        SudokuCnfEncoder encoder = new SudokuCnfEncoder(N);
        int[][] cnfClauses = encoder.encodeSudoku(board);

        variableAssignments.clear();
        maxPropagationDepth = 0;
        totalGuessCount = 0;

        boolean satisfiable = runDPLL(cnfClauses, 0);
        return satisfiable ? decodeAssignmentsToBoard() : null;
    }

    public boolean runDPLL(int[][] clauses, int currentDepth) {
        if (currentDepth > maxRecursionDepth) return false;
        maxPropagationDepth = Math.max(maxPropagationDepth, currentDepth);

        if (clauses.length == 0) return true;

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

    public Integer findUnitClause(int[][] clauses) {
        for (int[] clause : clauses) {
            if (clause.length == 1) return clause[0];
        }
        return null;
    }

    public Integer chooseUnassignedLiteral(int[][] clauses) {
        for (int[] clause : clauses) {
            for (int literal : clause) {
                if (!variableAssignments.containsKey(Math.abs(literal))) {
                    return literal;
                }
            }
        }
        return null;
    }

    public void assignLiteral(int literal) {
        variableAssignments.put(Math.abs(literal), literal > 0);
        if (isRunningInUI) storeStep(); 
    }

    public void unassignLiteral(int literal) {
        variableAssignments.remove(Math.abs(literal));
    }

    public int[][] simplifyClauses(int[][] clauses, int literal) {
        List<int[]> simplified = new ArrayList<>(clauses.length);
        for (int[] clause : clauses) {
            boolean isSatisfied = false;
            for (int l : clause) {
                if (l == literal) {
                    isSatisfied = true;
                    break;
                }
            }
            if (isSatisfied) continue;

            int count = 0;
            for (int l : clause) {
                if (l != -literal) count++;
            }

            int[] newClause = new int[count];
            int index = 0;
            for (int l : clause) {
                if (l != -literal) newClause[index++] = l;
            }
            simplified.add(newClause);
        }
        return simplified.toArray(new int[0][]);
    }

    public int[][] decodeAssignmentsToBoard() {
        int[][] resultBoard = new int[N][N];
        for (Map.Entry<Integer, Boolean> entry : variableAssignments.entrySet()) {
            if (!entry.getValue()) continue;

            int var = entry.getKey() - 1;
            int digit = var % N + 1;
            int col = (var / N) % N + 1;
            int row = var / (N * N) + 1;
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
    
    public void storeStep() {
        int[][] step = new int[N][N];
        for (Map.Entry<Integer, Boolean> entry : variableAssignments.entrySet()) {
            int var = entry.getKey(); 
            int value = entry.getValue() ? 1 : 0; 
            int row = (var / N) % N;  
            int col = var % N;      
            step[row][col] = value + 1;
        }
        steps.add(step);  
        stepCount++;
    }
    

    public int getStepCount() {
        return stepCount; 
    }

    public List<int[][]> getSteps() {
        return steps;  
    }
}
