import java.io.*;
import java.util.*;

public class Main {

    public static void solveUsingBackTracking(int[][] sudoku, String puzzleName, FileWriter writer) throws IOException {
        int[][] inputCopy = cloneBoard(sudoku);
        String originalString = boardToCSVString(inputCopy);
        long startTime = System.currentTimeMillis();
        BackTrackingSolver solver = new BackTrackingSolver(sudoku);
        boolean solved = solver.solve();
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        System.out.println("=====================================");
        System.out.println("Backtracking Solver:");
        System.out.println("Elapsed time: " + elapsedTime + "ms");
        if (solved) printBoard(sudoku);
        System.out.println("=====================================");

        String solutionString = solved ? boardToCSVString(sudoku) : "";
        writer.write(String.format("%s,%s,%d,%s,%s,%s\n", puzzleName, "Backtracking", elapsedTime, solved ? "Yes" : "No", originalString, solutionString));
    }

    public static void solveUsingConstraintPropagation(int[][] sudoku, String puzzleName, FileWriter writer) throws IOException {
        int[][] inputCopy = cloneBoard(sudoku);
        String originalString = boardToCSVString(inputCopy);
        long startTime = System.currentTimeMillis();
        ConstraintPropagationSolver solver = new ConstraintPropagationSolver(9);
        int[][] solvedBoard = solver.solve(inputCopy);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        System.out.println("=====================================");
        System.out.println("Constraint Propagation Solver:");
        System.out.println("Elapsed time: " + elapsedTime + "ms");
        if (solvedBoard != null) printBoard(solvedBoard);
        else System.out.println("No solution found using Constraint Propagation.");
        System.out.println("=====================================");

        String solutionString = solvedBoard != null ? boardToCSVString(solvedBoard) : "";
        writer.write(String.format("%s,%s,%d,%s,%s,%s\n", puzzleName, "ConstraintPropagation", elapsedTime, solvedBoard != null ? "Yes" : "No", originalString, solutionString));
    }

    public static void solveUsingDPLLSAT(int[][] sudoku, String puzzleName, FileWriter writer) throws IOException {
        String originalString = boardToCSVString(sudoku);
        long startTime = System.currentTimeMillis();
        DPLLSATSolver solver = new DPLLSATSolver();
        int[][] result = solver.solve(sudoku);
        boolean solved = result != null;
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        System.out.println("=====================================");
        System.out.println("DPLL SAT Solver:");
        System.out.println("Elapsed time: " + elapsedTime + "ms");
        if (solved) {
            System.out.println("Solved Sudoku (DPLL SAT):");
            printBoard(result);
        } else {
            System.out.println("No solution found using DPLL SAT.");
        }
        System.out.println("=====================================");

        String solutionString = solved ? boardToCSVString(result) : "";
        writer.write(String.format("%s,%s,%d,%s,%s,%s\n", puzzleName, "DPLLSAT", elapsedTime, solved ? "Yes" : "No", originalString, solutionString));
    }

    public static void solveUsingDLX(int[][] sudoku, String puzzleName, FileWriter writer) throws IOException {
        String originalString = boardToCSVString(sudoku);
        long startTime = System.currentTimeMillis();

        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        DLXSolver.solve(sudoku);

        System.out.flush();
        System.setOut(originalOut);

        String output = baos.toString();
        boolean solved = !output.contains("No solution");

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        System.out.println("=====================================");
        System.out.println("DLX Solver:");
        System.out.println("Elapsed time: " + elapsedTime + "ms");
        if (solved) {
            System.out.println("Solved Sudoku (DLX):");
            System.out.print(output);
        } else {
            System.out.println("No solution found using DLX.");
        }
        System.out.println("=====================================");

        String solutionString = solved ? extractSudokuFromDLXOutput(output) : "";
        writer.write(String.format("%s,%s,%d,%s,%s,%s\n", puzzleName, "DLX", elapsedTime, solved ? "Yes" : "No", originalString, solutionString));
    }

    public static int[][] cloneBoard(int[][] board) {
        int[][] copy = new int[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, 9);
        }
        return copy;
    }

    public static void printBoard(int[][] board) {
        for (int i = 0; i < 9; i++) {
            if (i % 3 == 0 && i != 0) System.out.println("------+-------+------");
            for (int j = 0; j < 9; j++) {
                if (j % 3 == 0 && j != 0) System.out.print("| ");
                System.out.print(board[i][j] == 0 ? ". " : board[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static String boardToCSVString(int[][] board) {
        if (board == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int[] row : board) {
            for (int num : row) {
                sb.append(num);
            }
        }
        return sb.toString();
    }

    public static String extractSudokuFromDLXOutput(String output) {
        StringBuilder sb = new StringBuilder();
        for (String line : output.split("\n")) {
            if (line.contains("-") || line.trim().isEmpty()) continue;
            for (String token : line.trim().split("\\s+")) {
                if (token.equals(".")) sb.append("0");
                else if (token.matches("\\d")) sb.append(token);
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        List<int[][]> puzzles = new ArrayList<>();

        puzzles.add(new int[][] {
            {1, 0, 0, 0, 3, 0, 0, 0, 0},
            {0, 0, 0, 6, 0, 0, 0, 9, 0},
            {0, 8, 4, 0, 5, 2, 0, 3, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 7},
            {2, 0, 5, 0, 0, 9, 4, 8, 0},
            {0, 0, 0, 0, 2, 0, 0, 5, 0},
            {0, 1, 0, 0, 0, 0, 6, 0, 5},
            {0, 7, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 9, 4, 0, 0, 0, 2, 0}
        });

        puzzles.add(new int[][] {
            {7, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 5, 3, 0, 0, 0, 0},
            {0, 8, 6, 0, 9, 0, 0, 0, 0},
            {0, 0, 9, 0, 0, 0, 0, 2, 0},
            {0, 0, 0, 0, 0, 8, 0, 0, 0},
            {3, 2, 0, 4, 0, 0, 0, 1, 0},
            {6, 0, 0, 0, 0, 0, 4, 5, 0},
            {0, 0, 0, 0, 0, 0, 1, 3, 0},
            {0, 4, 7, 0, 8, 0, 2, 0, 0}
        });

        puzzles.add(new int[][] {
            {0, 0, 7, 4, 2, 0, 0, 0, 0},
            {0, 1, 0, 9, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 3, 0},
            {0, 0, 0, 8, 0, 0, 7, 2, 0},
            {0, 4, 0, 0, 7, 0, 5, 8, 0},
            {3, 0, 0, 0, 0, 0, 0, 1, 0},
            {0, 3, 0, 0, 6, 0, 0, 0, 0},
            {9, 0, 0, 0, 3, 0, 0, 5, 0},
            {0, 0, 0, 0, 0, 9, 0, 0, 1}
        });

        puzzles.add(new int[][] {
            {0, 0, 2, 0, 0, 8, 6, 0, 9},
            {0, 5, 0, 3, 0, 0, 0, 0, 0},
            {0, 0, 4, 1, 0, 5, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 7, 6},
            {0, 0, 0, 0, 0, 2, 0, 4, 0},
            {0, 7, 0, 0, 0, 0, 8, 0, 5},
            {0, 6, 0, 9, 0, 0, 0, 0, 0},
            {0, 2, 0, 0, 1, 0, 0, 0, 0},
            {9, 1, 0, 0, 5, 0, 7, 0, 0}
        });

        puzzles.add(new int[][] {
            {4, 0, 8, 2, 0, 0, 0, 0, 0},  // Row 1
            {2, 0, 9, 7, 0, 0, 0, 0, 0},  // Row 2 ✅ fixed
            {0, 3, 0, 1, 0, 0, 0, 0, 0},  // Row 3
            {0, 0, 0, 3, 0, 5, 1, 2, 0},  // Row 4
            {0, 4, 1, 0, 0, 0, 6, 0, 9},  // Row 5
            {0, 0, 0, 0, 0, 0, 0, 0, 0},  // Row 6
            {0, 0, 0, 0, 0, 0, 4, 1, 8},  // Row 7
            {0, 0, 7, 0, 2, 0, 0, 9, 0},  // Row 8
            {5, 0, 0, 0, 0, 0, 0, 0, 2}                                                             
        });

        puzzles.add(new int[][] {
            {0, 0, 6, 9, 0, 3, 0, 0, 8},  // Row 1
            {0, 0, 0, 0, 0, 0, 2, 0, 0},  // Row 2
            {0, 0, 0, 0, 5, 0, 9, 6, 0},  // Row 3
            {0, 0, 0, 0, 6, 5, 0, 0, 0},  // Row 4
            {6, 0, 0, 0, 0, 4, 0, 0, 1},  // Row 5
            {0, 0, 5, 0, 0, 9, 0, 2, 0},  // Row 6
            {0, 0, 0, 0, 9, 0, 0, 0, 3},  // Row 7
            {3, 0, 1, 0, 0, 0, 0, 0, 0},  // Row 8
            {4, 2, 0, 0, 1, 0, 6, 8, 0}   // Row 9                                                
        });

        puzzles.add(new int[][] {
            {0, 0, 7, 0, 0, 6, 0, 9, 0},  // Row 1
            {0, 0, 0, 0, 0, 0, 2, 0, 0},  // Row 2
            {0, 0, 5, 0, 0, 9, 0, 0, 0},  // Row 3
            {3, 2, 0, 0, 0, 0, 0, 0, 0},  // Row 4
            {0, 0, 1, 0, 9, 0, 0, 0, 8},  // Row 5
            {0, 0, 0, 2, 0, 8, 3, 0, 7},  // Row 6
            {0, 1, 0, 4, 6, 0, 7, 0, 0},  // Row 7
            {0, 8, 0, 0, 0, 0, 0, 3, 0},  // Row 8
            {0, 0, 0, 0, 7, 0, 0, 0, 6}   // Row 9
        });

        puzzles.add(new int[][] {
            {0, 0, 9, 0, 0, 0, 2, 0, 0},  // Row 1
            {0, 8, 0, 2, 0, 0, 6, 0, 0},  // Row 2
            {0, 0, 0, 1, 0, 7, 0, 0, 0},  // Row 3
            {5, 0, 0, 0, 0, 6, 0, 4, 0},  // Row 4
            {0, 0, 0, 0, 0, 0, 0, 0, 0},  // Row 5
            {0, 2, 0, 5, 0, 4, 9, 6, 0},  // Row 6
            {0, 0, 0, 0, 4, 0, 0, 1, 3},  // Row 7
            {7, 0, 2, 0, 0, 0, 0, 0, 0},  // Row 8
            {0, 3, 0, 8, 0, 0, 0, 0, 5}   // Row 9
         }); 

        puzzles.add(new int[][] {
            {0, 0, 0, 0, 1, 0, 0, 2, 0},  // Row 1
            {0, 0, 0, 0, 0, 0, 5, 8, 4},  // Row 2
            {0, 0, 0, 7, 0, 0, 0, 0, 0},  // Row 3
            {5, 0, 9, 0, 0, 0, 0, 0, 3},  // Row 4
            {8, 0, 0, 6, 0, 9, 2, 0, 0},  // Row 5
            {0, 0, 6, 0, 0, 5, 1, 0, 0},  // Row 6
            {0, 0, 0, 5, 0, 0, 0, 0, 0},  // Row 7
            {3, 0, 0, 0, 2, 1, 9, 7, 0},  // Row 8
            {0, 8, 0, 0, 4, 0, 0, 1, 0}   // Row 9
        });

        puzzles.add(new int[][] {
            {8, 0, 0, 0, 2, 0, 0, 0, 0},  // Row 1
            {3, 9, 1, 0, 0, 0, 0, 0, 0},  // Row 2
            {0, 6, 0, 3, 0, 9, 0, 5, 0},  // Row 3
            {1, 8, 0, 0, 0, 0, 7, 0, 0},  // Row 4
            {0, 0, 9, 8, 0, 0, 2, 0, 0},  // Row 5
            {0, 0, 0, 4, 0, 0, 5, 0, 0},  // Row 6
            {0, 0, 0, 0, 8, 0, 0, 0, 0},  // Row 7
            {0, 5, 0, 0, 0, 7, 0, 8, 0},  // Row 8
            {4, 0, 0, 0, 0, 0, 0, 1, 0}   // Row 9
        });

        try (FileWriter writer = new FileWriter("sudoku_benchmark.csv")) {
            writer.write("Puzzle,Solver,Time,Solved,Original,Solution\n");

            for (int i = 0; i < puzzles.size(); i++) {
                String name = "Puzzle_" + (i + 1);
                int[][] puzzle = puzzles.get(i);
                System.out.println("Running: " + name);

                try {
                    solveUsingBackTracking(cloneBoard(puzzle), name, writer);
                } catch (Exception e) {
                    System.out.println("❌ Backtracking failed for " + name + ": " + e.getMessage());
                }
                
                try {
                    solveUsingConstraintPropagation(cloneBoard(puzzle), name, writer);
                } catch (Exception e) {
                    System.out.println("❌ ConstraintPropagation failed for " + name + ": " + e.getMessage());
                }
                
                try {
                    solveUsingDPLLSAT(cloneBoard(puzzle), name, writer);
                } catch (Exception e) {
                    System.out.println("❌ DPLLSAT failed for " + name + ": " + e.getMessage());
                }
                
                try {
                    solveUsingDLX(cloneBoard(puzzle), name, writer);
                } catch (Exception e) {
                    System.out.println("❌ DLX failed for " + name + ": " + e.getMessage());
                }
                
            }

            System.out.println("\u2705 Done. Benchmark written to sudoku_benchmark.csv");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}