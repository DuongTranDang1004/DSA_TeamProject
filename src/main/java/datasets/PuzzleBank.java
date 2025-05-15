package datasets;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
 * ============================================
 *               PuzzleBank Class
 * ============================================
 * User For: Saving all Sudoku Puzzle for Testing in 2D Array FormatFormat
 * Written By: Group 1 in @RMIT - 2025 for Group Project of COSC2469 Algorithm And Analysis Course
 * ============================================
 */

public class PuzzleBank {
  public static int[][][] getPuzzles() {
        String filePath = "src\\main\\java\\datasets\\sudoku_puzzles_test.csv";
        List<int[][]> puzzleList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine(); 

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length < 3) continue;

                int size = Integer.parseInt(parts[1]);
                String[] numbers = parts[2].trim().split("\\s+");

                int[][] puzzle = new int[size][size];
                for (int i = 0; i < numbers.length; i++) {
                    int row = i / size;
                    int col = i % size;
                    puzzle[row][col] = Integer.parseInt(numbers[i]);
                }

                puzzleList.add(puzzle);
            }
        } catch (IOException e) {
            System.err.println("Failed to read puzzles: " + e.getMessage());
            return new int[0][][];
        }

        return puzzleList.toArray(new int[0][][]);
    }

     public static int[][] getRandomPuzzle() {
        Random random = new Random();
        int[][][] puzzles = getPuzzles();
        return puzzles[random.nextInt(puzzles.length)];
    }

      public static void exportPuzzlesFromMethod(String filename) throws IOException {
        int[][][] puzzles = PuzzleBank.getPuzzles(); 

        FileWriter writer = new FileWriter(filename);
        writer.write("id,size,data\n"); 
        for (int i = 0; i < puzzles.length; i++) {
            int[][] puzzle = puzzles[i];
            int size = puzzle.length;
            StringBuilder line = new StringBuilder();

            line.append(size).append("_").append(i + 1).append(","); 
            line.append(size).append(","); 

            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    line.append(puzzle[row][col]).append(" ");
                }
            }

            writer.write(line.toString().trim() + "\n"); 
        }

        writer.close();
        System.out.println("Exported " + puzzles.length + " puzzles to " + filename);
    }
}
