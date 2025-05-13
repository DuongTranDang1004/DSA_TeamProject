package implementations;

import datasets.PuzzleBank;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.*;

/*
 * ============================================
 *       Main Class
 * ============================================
 * User For: Running, benchmarking, and comparing 
 * Sudoku solvers, printing results, and saving them to a CSV file for analysis.
 * Written By: Group 1 in @RMIT - 2025 for Group Project of COSC2469 Algorithm And Analysis Course
 * ============================================
 */

public class Main {

    private static volatile long peakMemoryUsage = 0;
    private static volatile boolean monitoring = false;

    public static long getUsedMemory() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        return memoryBean.getHeapMemoryUsage().getUsed();
    }

    public static void startMemoryMonitor() {
        monitoring = true;
        peakMemoryUsage = getUsedMemory();
        Thread monitor = new Thread(() -> {
            while (monitoring) {
                long current = getUsedMemory();
                if (current > peakMemoryUsage) {
                    peakMemoryUsage = current;
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {}
            }
        });
        monitor.setDaemon(true);
        monitor.start();
    }

    public static void stopMemoryMonitor() {
        monitoring = false;
    }

    public static class PuzzleInfo {
        int[][] puzzle;
        int hintCount;
        double hintVariance;
        double difficultyScore;

        PuzzleInfo(int[][] puzzle) {
            this.puzzle = puzzle;
            this.hintCount = countHints(puzzle);
            this.hintVariance = computeHintSpread(puzzle);
            this.difficultyScore = computeDifficultyScore(puzzle);
        }
    }

    public static class SolveResult {
        boolean solved;
        long[] times;
        int numberOfGuesses;
        int propagationDepth;
        int[][] solvedBoard;
        long peakMemory;

        SolveResult(boolean solved, long[] times, int numberOfGuesses, int propagationDepth, int[][] solvedBoard, long peakMemory) {
            this.solved = solved;
            this.times = times;
            this.numberOfGuesses = numberOfGuesses;
            this.propagationDepth = propagationDepth;
            this.solvedBoard = solvedBoard;
            this.peakMemory = peakMemory;
        }
    }

    public static int countHints(int[][] board) {
        int count = 0;
        for (int[] row : board)
            for (int cell : row)
                if (cell != 0) count++;
        return count;
    }

    public static double computeHintSpread(int[][] board) {
        int size = board.length;
        int blockSize = (int) Math.sqrt(size);
        int[] rowCounts = new int[size];
        int[] colCounts = new int[size];
        int[] blockCounts = new int[size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] != 0) {
                    rowCounts[i]++;
                    colCounts[j]++;
                    blockCounts[(i / blockSize) * blockSize + (j / blockSize)]++;
                }
            }
        }
        return (stddev(rowCounts) + stddev(colCounts) + stddev(blockCounts)) / 3.0;
    }

    public static double stddev(int[] data) {
        double mean = Arrays.stream(data).average().orElse(0.0);
        double variance = 0.0;
        for (int value : data)
            variance += Math.pow(value - mean, 2);
        return Math.sqrt(variance / data.length);
    }

    public static double computeDifficultyScore(int[][] board) {
        int total = board.length * board.length;
        double density = countHints(board) / (double) total;
        double spread = computeHintSpread(board);
        double penalty = Math.min(spread / (board.length / 2.0), 1.0);
        return (1 - density) * 30 + Math.pow(penalty, 1.2) * 50;
    }

    public static int[][] deepCopy(int[][] original) {
        if (original == null) return null;
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++)
            copy[i] = Arrays.copyOf(original[i], original[i].length);
        return copy;
    }

    public static String boardToString(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : board)
            for (int cell : row)
                sb.append(cell);
        return sb.toString();
    }

    public static boolean isValidSolution(int[][] board) {
        if (board == null) return false;
        for (int[] row : board)
            for (int cell : row)
                if (cell < 1) return false;
        return true;
    }

    public static SolveResult solveAndBenchmark(String puzzleName, PuzzleInfo info, String solverName, long timeout) {
        long[] times = new long[5];
        boolean solved = false;
        int numberOfGuesses = 0;
        int propagationDepth = 0;
        int[][] firstSolvedBoard = null;
        long recordedPeakMemory = 0;

        for (int attempt = 0; attempt < 5; attempt++) {
            int[][] copy = deepCopy(info.puzzle);
            long start = System.currentTimeMillis();

            try {
                System.gc();
                startMemoryMonitor();

                int[][] resultBoard = null;

                switch (solverName) {
                    case "Backtracking":
                        BackTrackingSolver back = new BackTrackingSolver(copy.length,false);
                        back.setTimeoutMillis(timeout);
                        resultBoard = back.solve(copy);
                        numberOfGuesses = back.getNumberOfGuesses();
                        propagationDepth = back.getPropagationDepth();
                        break;
                    case "ConstraintPropagation":
                        ConstraintPropagationSolver cp = new ConstraintPropagationSolver(copy.length,false);
                        resultBoard = cp.solve(copy);
                        numberOfGuesses = cp.getNumberOfGuesses();
                        propagationDepth = cp.getPropagationDepth();
                        break;
                    case "DPLLSAT":
                        DPLLSATSolver dpll = new DPLLSATSolver(copy.length,false);
                        resultBoard = dpll.solve(copy);
                        numberOfGuesses = dpll.getNumberOfGuesses();
                        propagationDepth = dpll.getPropagationDepth();
                        break;
                    case "DLX":
                        DLXSolver dlx = new DLXSolver(copy.length,false);
                        resultBoard = dlx.solve(copy);
                        numberOfGuesses = dlx.getNumberOfGuesses();
                        propagationDepth = dlx.getPropagationDepth();
                        break;
                }

                stopMemoryMonitor();
                recordedPeakMemory = Math.max(recordedPeakMemory, peakMemoryUsage);

                if (isValidSolution(resultBoard)) {
                    solved = true;
                    if (attempt == 0) firstSolvedBoard = deepCopy(resultBoard);
                } else {
                    System.out.println("Solver failed to return a valid solution.");
                    break;
                }

            } catch (RuntimeException e) {
                stopMemoryMonitor();
                System.out.println("Solver failed: " + e.getMessage());
                break;
            } catch (Exception e) {
                stopMemoryMonitor();
                System.out.println("Unexpected error: " + e.getMessage());
                break;
            }

            times[attempt] = System.currentTimeMillis() - start;
        }

        return new SolveResult(solved, times, numberOfGuesses, propagationDepth, firstSolvedBoard, recordedPeakMemory);
    }

    public static void printBoard(int[][] board) {
        int N = board.length;
        int boxSize = (int) Math.sqrt(N);

        for (int i = 0; i < N; i++) {
            if (i > 0 && i % boxSize == 0) {
                System.out.println("-".repeat(N * 2 + boxSize - 1)); 
            }
            for (int j = 0; j < N; j++) {
                if (j > 0 && j % boxSize == 0) {
                    System.out.print("| "); 
                }
                System.out.print(board[i][j] == 0 ? ". " : board[i][j] + " ");  
            }
            System.out.println(); 
        }
    }

    public static void main(String[] args) {
        int[][][] puzzles = PuzzleBank.getPuzzles();
        if (puzzles.length == 0) {
            System.out.println("No puzzles found.");
            return;
        }

        List<String[]> records = new ArrayList<>();
        records.add(new String[]{
            "PuzzleName", "Solver", "Solved", "HintCount", "HintVariance", "DifficultyScore",
            "Run1(ms)", "Run2(ms)", "Run3(ms)", "Run4(ms)", "Run5(ms)",
            "BestTime(ms)", "WorstTime(ms)", "AverageTime(ms)",
            "NumberOfGuesses", "PropagationDepth", "PeakMemory(bytes)",
            "OriginalPuzzle", "Solution",
            "InitStartTime(ms)", "InitEndTime(ms)", "InitTime(μs)", "InitMemCost(bytes)"
        });

        int index = 1;
        for (int[][] puzzle : puzzles) {
            String puzzleName = "Puzzle_" + index++;

            for (String solver : List.of("Backtracking", "ConstraintPropagation", "DPLLSAT", "DLX")) {
                System.out.println("Solving " + puzzleName + " with " + solver + "...");

                long initStartTime = System.nanoTime();
                long memBeforeInit = getUsedMemory();

                PuzzleInfo info = new PuzzleInfo(puzzle);

                long initEndTime = System.nanoTime();
                long memAfterInit = getUsedMemory();
                long initializationTime = (initEndTime - initStartTime) / 1_000;
                long initializationMemoryCost = Math.max(memAfterInit - memBeforeInit, 1);

                long timeout = solver.equals("Backtracking") ? 180_000 : 120_000;
                SolveResult result = solveAndBenchmark(puzzleName, info, solver, timeout);

                long best = Arrays.stream(result.times).min().orElse(0);
                long worst = Arrays.stream(result.times).max().orElse(0);
                long avg = (long) Arrays.stream(result.times).average().orElse(0);

                records.add(new String[] {
                    puzzleName, solver,
                    result.solved ? "Yes" : "No",
                    String.valueOf(info.hintCount),
                    String.format("%.2f", info.hintVariance),
                    String.format("%.2f", info.difficultyScore),
                    String.valueOf(result.times[0]),
                    String.valueOf(result.times[1]),
                    String.valueOf(result.times[2]),
                    String.valueOf(result.times[3]),
                    String.valueOf(result.times[4]),
                    String.valueOf(best),
                    String.valueOf(worst),
                    String.valueOf(avg),
                    result.solved ? String.valueOf(result.numberOfGuesses) : "N/A",
                    result.solved ? String.valueOf(result.propagationDepth) : "N/A",
                    String.valueOf(result.peakMemory),
                    boardToString(info.puzzle),
                    result.solved && result.solvedBoard != null ? boardToString(result.solvedBoard) : "N/A",
                    String.valueOf(initStartTime),
                    String.valueOf(initEndTime),
                    String.valueOf(initializationTime),
                    String.valueOf(initializationMemoryCost)
                });

                if (result.solved) {
                    printBoard(result.solvedBoard);
                }
            }
        }

        try (FileWriter writer = new FileWriter("results/puzzle_result_extra.csv", false)) {
            for (String[] record : records) {
                writer.write(String.join(",", record));
                writer.write("\n");
            }
            System.out.println("Results saved to puzzle_result_extra.csv");
        } catch (IOException e) {
            System.out.println("Error writing CSV: " + e.getMessage());
        }
    }
}
