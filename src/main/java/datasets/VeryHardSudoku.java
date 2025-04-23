package datasets;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class VeryHardSudoku {

    public static void main(String[] args) {
        String filePath = "very_hard.csv"; // Path to your CSV file
        int gridSize = 9; // Adjust this to 16 or 25 for larger grids
        List<int[][]> sudokuGrids = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Skip the header row
                if (line.startsWith("quizzes")) continue;

                // Split the line into its components
                String[] parts = line.split(",");
                String quiz = parts[0]; // Get the unsolved Sudoku puzzle
                
                // Convert the quiz string into a 2D array
                int[][] grid = new int[gridSize][gridSize];
                for (int i = 0; i < quiz.length(); i++) {
                    grid[i / gridSize][i % gridSize] = Character.getNumericValue(quiz.charAt(i));
                }
                
                // Add the grid to the list
                sudokuGrids.add(grid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Print the first puzzle to verify
        System.out.println("First Sudoku Puzzle:");
        printGrid(sudokuGrids.get(0), gridSize);
    }

    // Helper method to print a 2D array
    public static void printGrid(int[][] grid, int gridSize) {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                System.out.print(grid[row][col] + " ");
            }
            System.out.println();
        }
    }
}
