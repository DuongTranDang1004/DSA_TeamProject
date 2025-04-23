package implementations;

import java.io.*;
import java.util.*;
import datasets.PuzzleBank;

public class Main {

    static class PuzzleData {
        int[][] puzzle;
        int hint;
        int size;
        double variance;
        double score;

        PuzzleData(int[][] puzzle) {
            this.puzzle = puzzle;
            this.hint = countHints(puzzle);
            this.size = puzzle.length;
            this.variance = computeHintSpread(puzzle);
            this.score = getDifficultyScore(puzzle);
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
        int n = data.length;
        double mean = Arrays.stream(data).average().orElse(0.0);
        double variance = 0.0;
        for (int value : data) {
            variance += Math.pow(value - mean, 2);
        }
        return Math.sqrt(variance / n);
    }

    public static double getDifficultyScore(int[][] board) {
        int size = board.length;
        int totalCells = size * size;
        int hintCount = countHints(board);
        double hintDensity = hintCount / (double) totalCells;

        double spread = computeHintSpread(board);
        double spreadPenalty = Math.min(spread / (size / 2.0), 1.0);

        int[] rowCounts = new int[size];
        int[] colCounts = new int[size];
        int[] blockCounts = new int[size];
        int blockSize = (int) Math.sqrt(size);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] != 0) {
                    rowCounts[i]++;
                    colCounts[j]++;
                    blockCounts[(i / blockSize) * blockSize + (j / blockSize)]++;
                }
            }
        }

        int sparseUnits = 0;
        for (int i = 0; i < size; i++) {
            if (rowCounts[i] <= 3) sparseUnits++;
            if (colCounts[i] <= 3) sparseUnits++;
            if (blockCounts[i] <= 3) sparseUnits++;
        }
        double sparsePenalty = Math.min(sparseUnits / (double)(3 * size), 1.0);

        return (1 - hintDensity) * 30 + Math.pow(spreadPenalty, 1.2) * 50 + Math.pow(sparsePenalty, 1.8) * 20;
    }

    public static String getDifficultyLabel(int hint, double variance) {
        if (hint <= 22) return "expert";
        else if (hint <= 25) {
            return variance < 1.0 ? "highschool" : "expert";
        } else {
            return "kindergarten";
        }
    }

    public static String[] solve(int[][] board, String puzzleName, String solver, int hint, double variance) {
        long startTime = System.currentTimeMillis();
        int[][] clone = cloneBoard(board);
        int[][] solved = null;
        boolean success = false;

        if (solver.equals("Backtracking")) {
            BackTrackingSolver s = new BackTrackingSolver(clone);
            success = s.solve();
            solved = clone;
        } else if (solver.equals("ConstraintPropagation")) {
            ConstraintPropagationSolver s = new ConstraintPropagationSolver(clone.length);
            solved = s.solve(clone);
            success = solved != null;
        } else if (solver.equals("DPLLSAT")) {
            DPLLSATSolver s = new DPLLSATSolver();
            solved = s.solve(clone);
            success = solved != null;
        } else if (solver.equals("DLX")) {
            PrintStream originalOut = System.out;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));
            DLXSolver.solve(clone);
            System.out.flush();
            System.setOut(originalOut);
            String output = baos.toString();
            success = !output.contains("No solution");
            if (success) {
                solved = cloneBoard(clone);
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        String original = boardToCSVString(board);
        String solvedStr = success ? boardToCSVString(solved) : "";
        double score = getDifficultyScore(board);
        return new String[]{puzzleName, solver, String.valueOf(elapsed), success ? "Yes" : "No", String.valueOf(hint), String.format("%.2f", variance), String.format("%.2f", score), original, solvedStr};
    }

    public static int[][] cloneBoard(int[][] board) {
        int[][] copy = new int[board.length][board.length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, board.length);
        }
        return copy;
    }

    public static String boardToCSVString(int[][] board) {
        int size = board.length;
        boolean is9x9 = size == 9;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                sb.append(board[i][j]);
                if (!is9x9 && j < size - 1) sb.append(",");
            }
            if (!is9x9 && i < size - 1) sb.append("\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        List<PuzzleData> puzzles = new ArrayList<>();
        for (int[][] puzzle : PuzzleBank.getPuzzles()) {
            puzzles.add(new PuzzleData(puzzle));
        }

        puzzles.sort(Comparator.comparingDouble(p -> p.score));
        Map<String, Integer> nameCounter = new HashMap<>();
        List<String[]> allResults = new ArrayList<>();

        for (PuzzleData p : puzzles) {
            String difficulty = getDifficultyLabel(p.hint, p.variance);
            String key = p.size + "x" + p.size + "_" + difficulty;
            int num = nameCounter.getOrDefault(key, 1);
            nameCounter.put(key, num + 1);
            String puzzleName = String.format("Puzzle_%s_%s_%d", p.size + "x" + p.size, difficulty, num);

            List<String> solvers = Arrays.asList("Backtracking", "ConstraintPropagation", "DPLLSAT", "DLX");
            List<String[]> results = new ArrayList<>();
            for (String solver : solvers) {
                results.add(solve(p.puzzle, puzzleName, solver, p.hint, p.variance));
            }
            results.sort(Comparator.comparingInt(r -> Integer.parseInt(r[2])));
            allResults.addAll(results);
        }

        try (FileWriter writer = new FileWriter("sudoku_benchmark.csv")) {
            writer.write("Puzzle,Solver,Time(ms),Solved,HintCount,HintVariance,DifficultyScore,Original,Solution\n");
            for (String[] row : allResults) {
                writer.write("\"" + String.join("\",\"", row) + "\"\n");
            }
            System.out.println("✅ Results written to sudoku_benchmark.csv");
        } catch (IOException e) {
            System.out.println("❌ Error writing file: " + e.getMessage());
        }
    }
}
