// Main.java - Version cập nhật lưu NumberOfGuesses và PropagationDepth

package implementations;

import datasets.PuzzleBank;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {

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

        SolveResult(boolean solved, long[] times, int numberOfGuesses, int propagationDepth, int[][] solvedBoard) {
            this.solved = solved;
            this.times = times;
            this.numberOfGuesses = numberOfGuesses;
            this.propagationDepth = propagationDepth;
            this.solvedBoard = solvedBoard;
        }
    }

    public static int countHints(int[][] board) {
        int count = 0;
        for (int[] row : board) {
            for (int cell : row) {
                if (cell != 0) count++;
            }
        }
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
        for (int value : data) {
            variance += Math.pow(value - mean, 2);
        }
        return Math.sqrt(variance / data.length);
    }

    public static double computeDifficultyScore(int[][] board) {
        int size = board.length;
        int totalCells = size * size;
        int hintCount = countHints(board);
        double hintDensity = hintCount / (double) totalCells;
        double spread = computeHintSpread(board);
        double spreadPenalty = Math.min(spread / (size / 2.0), 1.0);

        return (1 - hintDensity) * 30 + Math.pow(spreadPenalty, 1.2) * 50;
    }

    public static int[][] deepCopy(int[][] original) {
        if (original == null) return null;
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return copy;
    }

    public static String boardToString(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : board) {
            for (int cell : row) {
                sb.append(cell);
            }
        }
        return sb.toString();
    }

    public static SolveResult solveAndBenchmark(String puzzleName, PuzzleInfo info, String solverName) {
        long[] times = new long[5];
        boolean solved = false;
        int numberOfGuesses = 0;
        int propagationDepth = 0;
        int[][] firstSolvedBoard = null;

        for (int attempt = 0; attempt < 5; attempt++) {
            int[][] puzzleCopy = deepCopy(info.puzzle);
            long startTime = System.currentTimeMillis();

            try {
                switch (solverName) {
                    case "Backtracking":
                        BackTrackingSolver backSolver = new BackTrackingSolver(puzzleCopy.length);
                        int[][] backSolved = backSolver.solve(puzzleCopy);
                        solved = backSolved != null;
                        numberOfGuesses = backSolver.getNumberOfGuesses();
                        propagationDepth = backSolver.getPropagationDepth();
                        if (attempt == 0 && solved) firstSolvedBoard = deepCopy(backSolved);
                        break;
                    case "ConstraintPropagation":
                        ConstraintPropagationSolver constraintSolver = new ConstraintPropagationSolver(puzzleCopy.length);
                        int[][] constraintSolved = constraintSolver.solve(puzzleCopy);
                        solved = constraintSolved != null;
                        numberOfGuesses = constraintSolver.getNumberOfGuesses();
                        propagationDepth = constraintSolver.getPropagationDepth();
                        if (attempt == 0 && solved) firstSolvedBoard = deepCopy(constraintSolved);
                        break;
                    case "DPLLSAT":
                        DPLLSATSolver dpllSolver = new DPLLSATSolver(puzzleCopy.length);
                        int[][] dpllSolved = dpllSolver.solve(puzzleCopy);
                        solved = dpllSolved != null;
                        numberOfGuesses = dpllSolver.getNumberOfGuesses();
                        propagationDepth = dpllSolver.getPropagationDepth();
                        if (attempt == 0 && solved) firstSolvedBoard = deepCopy(dpllSolved);
                        break;
                    case "DLX":
                        DLXSolver dlxSolver = new DLXSolver(puzzleCopy.length);
                        int[][] dlxSolved = dlxSolver.solve(puzzleCopy);
                        solved = dlxSolved != null;
                        numberOfGuesses = dlxSolver.getNumberOfGuesses();
                        propagationDepth = dlxSolver.getPropagationDepth();
                        if (attempt == 0 && solved) firstSolvedBoard = deepCopy(dlxSolved);
                        break;
                }
            } catch (Exception e) {
                solved = false;
            }

            long elapsed = System.currentTimeMillis() - startTime;
            times[attempt] = elapsed;
        }

        return new SolveResult(solved, times, numberOfGuesses, propagationDepth, firstSolvedBoard);
    }

    public static void main(String[] args) {
        int[][][] puzzles = PuzzleBank.getPuzzles();

        if (puzzles.length == 0) {
            System.out.println("No puzzles found.");
            return;
        }

        List<String[]> records = new ArrayList<>();
        records.add(new String[]{"PuzzleName", "Solver", "Solved", "HintCount", "HintVariance", "DifficultyScore",
                "Run1(ms)", "Run2(ms)", "Run3(ms)", "Run4(ms)", "Run5(ms)",
                "BestTime(ms)", "WorstTime(ms)", "AverageTime(ms)",
                "NumberOfGuesses", "PropagationDepth", "OriginalPuzzle", "Solution"});

        int index = 1;
        for (int[][] puzzle : puzzles) {
            PuzzleInfo info = new PuzzleInfo(puzzle);
            String puzzleName = "Puzzle_" + index++;

            for (String solver : List.of("Backtracking", "ConstraintPropagation", "DPLLSAT", "DLX")) {
                SolveResult result = solveAndBenchmark(puzzleName, info, solver);

                long bestTime = Arrays.stream(result.times).min().orElse(0);
                long worstTime = Arrays.stream(result.times).max().orElse(0);
                long avgTime = (long) Arrays.stream(result.times).average().orElse(0);

                records.add(new String[]{
                        puzzleName,
                        solver,
                        result.solved ? "Yes" : "No",
                        String.valueOf(info.hintCount),
                        String.format("%.2f", info.hintVariance),
                        String.format("%.2f", info.difficultyScore),
                        String.valueOf(result.times[0]),
                        String.valueOf(result.times[1]),
                        String.valueOf(result.times[2]),
                        String.valueOf(result.times[3]),
                        String.valueOf(result.times[4]),
                        String.valueOf(bestTime),
                        String.valueOf(worstTime),
                        String.valueOf(avgTime),
                        String.valueOf(result.numberOfGuesses),
                        String.valueOf(result.propagationDepth),
                        boardToString(info.puzzle),
                        result.solved && result.solvedBoard != null ? boardToString(result.solvedBoard) : ""
                });
            }
        }

        try (FileWriter writer = new FileWriter("puzzle_result.csv")) {
            for (String[] record : records) {
                writer.write(String.join(",", record));
                writer.write("\n");
            }
            System.out.println("Results saved to puzzle_result.csv");
        } catch (IOException e) {
            System.out.println("Error writing CSV: " + e.getMessage());
        }
    }
}
