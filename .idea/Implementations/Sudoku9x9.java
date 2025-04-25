import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Sudoku9x9 {

    /**
     * Loads Sudoku puzzles from a CSV file.
     * The CSV is assumed to have a header row followed by rows where the first comma-separated column 
     * is an 81-character string representing a 9x9 puzzle.
     *
     * @param filePath the path to the CSV file.
     * @param gridSize the size of the grid (for a standard Sudoku, 9).
     * @return a list of 2D integer arrays, each representing a Sudoku puzzle.
     */
    public static List<int[][]> loadPuzzles(String filePath, int gridSize) {
        List<int[][]> sudokuGrids = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean header = true;  // Skip the header row
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (header) {
                    header = false;
                    continue;
                }
                if (line.isEmpty()) continue;  // Skip blank lines

                // Split the line by commas; expect the puzzle string in the first column.
                String[] parts = line.split(",");
                if (parts.length == 0) continue;
                String quiz = parts[0].trim();

                // Check that the puzzle string has the expected length.
                int expectedLength = gridSize * gridSize;
                if (quiz.length() != expectedLength) {
                    System.err.println("Warning: Expected puzzle length " + expectedLength +
                            ", but got " + quiz.length());
                }

                // Convert the puzzle string into a 2D array.
                int[][] grid = new int[gridSize][gridSize];
                for (int i = 0; i < quiz.length(); i++) {
                    int row = i / gridSize;
                    int col = i % gridSize;
                    // Convert the character to a numeric digit.
                    int num = Character.getNumericValue(quiz.charAt(i));
                    grid[row][col] = num;
                }
                sudokuGrids.add(grid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sudokuGrids;
    }

    /**
     * Prints the provided 2D Sudoku grid in a formatted layout.
     *
     * @param grid     the 2D array representing the puzzle.
     * @param gridSize the grid's size (e.g., 9 for a 9x9 puzzle).
     */
    public static void printGrid(int[][] grid, int gridSize) {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                System.out.print(grid[row][col] + " ");
            }
            System.out.println();
        }
    }

    /**
     * Standalone testing: prints the first puzzle loaded from the CSV.
     */
    public static void main(String[] args) {
        String filePath = "normal_9x9.csv"; // Ensure this CSV file is in your project's working directory.
        int gridSize = 9;
        List<int[][]> puzzles = loadPuzzles(filePath, gridSize);
        System.out.println("Total puzzles loaded: " + puzzles.size());
        if (!puzzles.isEmpty()) {
            System.out.println("First Sudoku Puzzle:");
            printGrid(puzzles.get(0), gridSize);
        } else {
            System.out.println("No puzzles loaded.");
        }
    }
}