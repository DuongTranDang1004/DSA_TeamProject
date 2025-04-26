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

    public static String[] solveAndBenchmark(String puzzleName, PuzzleInfo info, String solverName) {
        List<Long> times = new ArrayList<>();
        boolean solved = false;
        int[][] firstSolvedBoard = null;

        for (int attempt = 0; attempt < 5; attempt++) {
            int[][] puzzleCopy = deepCopy(info.puzzle);
            long startTime = System.currentTimeMillis();

            try {
                switch (solverName) {
                    case "Backtracking":
                        BackTrackingSolver backSolver = new BackTrackingSolver(puzzleCopy.length);
                        int[][] backSolved =  backSolver.solve(puzzleCopy);
                        solved = backSolved != null;
                        if (attempt == 0 && solved) firstSolvedBoard = deepCopy(puzzleCopy);
                        break;
                    case "ConstraintPropagation":
                        ConstraintPropagationSolver constraintSolver = new ConstraintPropagationSolver(puzzleCopy.length);
                        int[][] constraintSolved = constraintSolver.solve(puzzleCopy);
                        solved = constraintSolved != null;
                        if (attempt == 0 && solved) firstSolvedBoard = constraintSolved;
                        break;
                    case "DPLLSAT":
                        DPLLSATSolver dpllSolver = new DPLLSATSolver(puzzleCopy.length);
                        int[][] dpllSolved = dpllSolver.solve(puzzleCopy);
                        solved = dpllSolved != null;
                        if (attempt == 0 && solved) firstSolvedBoard = dpllSolved;
                        break;
                    case "DLX":
                        DLXSolver dlxSolver = new DLXSolver(puzzleCopy.length);
                        int[][] dlxSolved = dlxSolver.solve(puzzleCopy);
                        solved = dlxSolved != null;
                        if (attempt == 0 && solved) firstSolvedBoard = dlxSolved;
                        break;
                }
            } catch (Exception e) {
                solved = false;
            }

            long elapsed = System.currentTimeMillis() - startTime;
            times.add(elapsed);
        }

        long bestTime = Collections.min(times);
        long worstTime = Collections.max(times);
        long avgTime = (long) times.stream().mapToLong(Long::longValue).average().orElse(0);

        return new String[]{
                puzzleName,
                solverName,
                String.valueOf(times.get(0)),
                solved ? "Yes" : "No",
                String.valueOf(info.hintCount),
                String.format("%.2f", info.hintVariance),
                String.format("%.2f", info.difficultyScore),
                String.valueOf(bestTime),
                String.valueOf(worstTime),
                String.valueOf(avgTime),
                boardToString(info.puzzle),
                solved && firstSolvedBoard != null ? boardToString(firstSolvedBoard) : ""
        };
    }

    public static void main(String[] args) {
        int[][][] puzzles = PuzzleBank.getPuzzles();

        if (puzzles.length == 0) {
            System.out.println("No puzzles found.");
            return;
        }

        List<String[]> records = new ArrayList<>();
        records.add(new String[]{"PuzzleName", "Solver", "Time(ms)", "Solved", "HintCount", "HintVariance", "DifficultyScore", "BestTime(ms)", "WorstTime(ms)", "AverageTime(ms)", "OriginalPuzzle", "Solution"});

        int index = 1;
        for (int[][] puzzle : puzzles) {
            PuzzleInfo info = new PuzzleInfo(puzzle);
            String puzzleName = "Puzzle_" + index++;

            records.add(solveAndBenchmark(puzzleName, info, "Backtracking"));
            records.add(solveAndBenchmark(puzzleName, info, "ConstraintPropagation"));
            records.add(solveAndBenchmark(puzzleName, info, "DPLLSAT"));
            records.add(solveAndBenchmark(puzzleName, info, "DLX"));
        }

        try (FileWriter writer = new FileWriter("puzzle_result.csv")) {
            for (String[] record : records) {
                writer.write(String.join(",", record));
                writer.write("\n");
            }
            System.out.println("✅ Results saved to puzzle_result.csv");
        } catch (IOException e) {
            System.out.println("❌ Error writing CSV: " + e.getMessage());
        }
    }
}
