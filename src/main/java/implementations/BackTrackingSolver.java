package implementations;

import java.util.*;

/*
 * ============================================
 *       BackTrackingSolver Class
 * ============================================
 * User For: Solving Sudoku using the Backtracking algorithm to find solutions step-by-step.
 * Written By: Group 1 in @RMIT - 2025 for Group Project of COSC2469 Algorithm And Analysis Course
 * ============================================
 */

 public class BackTrackingSolver {
     private final int N;
     public int[][] sudoku;
     public Map<Integer, Set<Integer>> rowConstraints = new HashMap<>();
     public Map<Integer, Set<Integer>> colConstraints = new HashMap<>();
     public Map<Integer, Set<Integer>> boxConstraints = new HashMap<>();
 
     private int propagationDepth = 0;
     private int numberOfGuesses = 0;
     private long startTime;
     public long timeoutMillis = 120_000;
     private int recursionCounter = 0;
     private boolean isRunningInUI = false;
     private int stepCount = 0;
     private List<int[][]> steps = new ArrayList<>();  
     
     public BackTrackingSolver(int N, boolean isRunningInUI) {
         if (Math.sqrt(N) != (int) Math.sqrt(N)) {
             throw new IllegalArgumentException("Board must be " + N + "x" + N + " and contain values from 0 to " + N);
         }
         this.N = N;
         this.isRunningInUI = isRunningInUI;
     }
 
     public static String cellKey(int row, int col) {
         return row + "," + col;
     }
 
     public int getBoxIndex(int row, int col) {
         int boxSize = (int) Math.sqrt(N);
         return (row / boxSize) * boxSize + (col / boxSize);
     }
 
     public void findInitialConstraints() {
         for (int i = 0; i < N; i++) {
             rowConstraints.put(i, new HashSet<>());
             colConstraints.put(i, new HashSet<>());
             boxConstraints.put(i, new HashSet<>());
         }
         for (int row = 0; row < N; row++) {
             for (int col = 0; col < N; col++) {
                 int value = sudoku[row][col];
                 if (value != 0) {
                     rowConstraints.get(row).add(value);
                     colConstraints.get(col).add(value);
                     boxConstraints.get(getBoxIndex(row, col)).add(value);
                 }
             }
         }
     }
 
     public int[][] solve(int[][] sudoku) {
         if (!isValidBoard(sudoku)) {
             throw new IllegalArgumentException("Invalid board: must be " + N + "x" + N + " and contain values 0.." + N);
         }
 
         this.sudoku = sudoku;
         this.propagationDepth = 0;
         this.numberOfGuesses = 0;
         this.recursionCounter = 0;
         this.startTime = System.currentTimeMillis();
 
         findInitialConstraints();
 
         boolean solvable = guessCell(0, 0, 0);
         if (solvable) {
             return sudoku;
         } else {
             return null;
         }
     }
 
     private boolean guessCell(int row, int col, int currentDepth) {
         recursionCounter++;
         if (recursionCounter % 50 == 0) {
             if (System.currentTimeMillis() - startTime >= timeoutMillis) {
                 throw new RuntimeException("Timeout exceeded (" + (timeoutMillis / 1000) + " seconds)");
             }
         }
 
         if (row == N) return true;
 
         int nextRow = (col == N - 1) ? row + 1 : row;
         int nextCol = (col == N - 1) ? 0 : col + 1;
 
         if (sudoku[row][col] != 0) {
             return guessCell(nextRow, nextCol, currentDepth);
         }
 
         propagationDepth = Math.max(propagationDepth, currentDepth);
 
         List<Integer> candidates = new ArrayList<>();
         for (int val = 1; val <= N; val++) {
             if (!rowConstraints.get(row).contains(val) && !colConstraints.get(col).contains(val)
                     && !boxConstraints.get(getBoxIndex(row, col)).contains(val)) {
                 candidates.add(val);
             }
         }
 
         if (candidates.size() > 1) numberOfGuesses++;
 
         for (int val : candidates) {
             sudoku[row][col] = val;
             rowConstraints.get(row).add(val);
             colConstraints.get(col).add(val);
             boxConstraints.get(getBoxIndex(row, col)).add(val);
 
             if (isRunningInUI) {
                 storeStep();  
             }
 
             if (guessCell(nextRow, nextCol, currentDepth + 1)) return true;
 
             sudoku[row][col] = 0;
             rowConstraints.get(row).remove(val);
             colConstraints.get(col).remove(val);
             boxConstraints.get(getBoxIndex(row, col)).remove(val);
         }
 
         return false;
     }
 
     public boolean isValidBoard(int[][] board) {
         if (board == null || board.length != N) return false;
         for (int[] row : board) {
             if (row == null || row.length != N) return false;
             for (int val : row) {
                 if (val < 0 || val > N) return false;
             }
         }
         return true;
     }
 
     private void storeStep() {
         int[][] step = new int[N][N];
         for (int i = 0; i < N; i++) {
             System.arraycopy(sudoku[i], 0, step[i], 0, N);
         }
         steps.add(step);  
         stepCount++;
     }
 
     public int getPropagationDepth() {
         return propagationDepth;
     }
 
     public int getNumberOfGuesses() {
         return numberOfGuesses;
     }
 
     public List<int[][]> getSteps() {
         return steps;  
     }
 
     public int getStepCount() {
         return stepCount;  
     }
 
     public void setTimeoutMillis(long millis) {
         this.timeoutMillis = millis;
     }
 }
 