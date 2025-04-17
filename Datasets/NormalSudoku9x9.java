import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class NormalSudoku9x9 {

    /**
     * Loads puzzles from the given CSV file.
     * Assumes the CSV file has a header row and that each subsequent line represents a puzzle.
     * The first column (quizzes) is expected to be an 81-character string for a 9x9 puzzle.
     *
     * @param filePath The path to the CSV file.
     * @param gridSize The Sudoku grid size (for a normal sudoku, 9).
     * @return A list of 2D arrays, each representing a puzzle.
     */
    public static List<int[][]> loadPuzzles(String filePath, int gridSize) {
        List<int[][]> sudokuGrids = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean header = true; // skip header row
            while ((line = br.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length == 0) continue;

                String quiz = parts[0].trim(); // get the unsolved puzzle string

                // Check that the quiz has the expected length (gridSize * gridSize)
                if (quiz.length() != gridSize * gridSize) {
                    System.err.println("Warning: Expected puzzle length " + (gridSize * gridSize)
                            + ", got " + quiz.length());
                }

                // Convert the quiz string into a 2D array
                int[][] grid = new int[gridSize][gridSize];
                for (int i = 0; i < quiz.length(); i++) {
                    grid[i / gridSize][i % gridSize] = Character.getNumericValue(quiz.charAt(i));
                }

                sudokuGrids.add(grid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sudokuGrids;
    }

    // Standalone testing: print the first puzzle to verify it loads correctly.
    public static void main(String[] args) {
        String filePath = "normal_9x9.csv"; // Path to your CSV file
        int gridSize = 9; // Adjust if needed
        List<int[][]> puzzles = loadPuzzles(filePath, gridSize);
        System.out.println("Total puzzles loaded: " + puzzles.size());
        if (!puzzles.isEmpty()) {
            System.out.println("First Sudoku Puzzle:");
            printGrid(puzzles.get(0), gridSize);
        } else {
            System.out.println("No puzzles loaded.");
        }
    }

    // Helper method to print a 2D array (a puzzle).
    public static void printGrid(int[][] grid, int gridSize) {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                System.out.print(grid[row][col] + " ");
            }
            System.out.println();
        }
    }
}
