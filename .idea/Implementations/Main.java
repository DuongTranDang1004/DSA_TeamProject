import java.util.Arrays;
import java.util.List;
import Datasets.PuzzleBank;

public class Main {
    // Returns the approximate used memory (in bytes) from the runtime.
    public static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // Request GC to minimize noise
        return runtime.totalMemory() - runtime.freeMemory();
    }

    // Variables for the memory monitor thread.
    private static volatile long peakMemoryUsage = 0;
    private static volatile boolean monitoring = false;

    // Starts a memory monitor thread that polls used memory every 50ms.
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
                    Thread.sleep(50); // sampling interval
                } catch (InterruptedException e) {
                    // Ignore interruption.
                }
            }
        });
        monitor.setDaemon(true);
        monitor.start();
    }

    // Stops the memory monitor.
    public static void stopMemoryMonitor() {
        monitoring = false;
    }

    // Performance test for Backtracking Solver
    public static void solveUsingBackTracking(int[][] sudoku) {
        long startTime = System.currentTimeMillis();
        BackTrackingSolver backTrackingSolver =
                new BackTrackingSolver(sudoku.length);
        backTrackingSolver.solve(sudoku);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("=====================================");
        System.out.println("Backtracking Solver:");
        System.out.println("Elapsed time: " + elapsedTime + "ms");
        System.out.println("=====================================");
        backTrackingSolver.printBoard();

    }

    // Performance test for Constraint Propagation Solver
    public static void solveUsingConstraintPropagation(int[][] sudoku) {
        long startTime = System.currentTimeMillis();
        ConstraintPropagationSolver constraintPropagationSolver =
                new ConstraintPropagationSolver(sudoku.length);
        constraintPropagationSolver.solve(sudoku);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("=====================================");
        System.out.println("Constraint Propagation Solver:");
        System.out.println("Elapsed time: " + elapsedTime + "ms");
        System.out.println("=====================================");
        constraintPropagationSolver.printBoard();
    }

    // Performance test for DPLL SAT Solver
    public static void solveUsingDPLLSAT(int[][] sudoku) {
        long startTime = System.currentTimeMillis();
        DPLLSATSolver dpllSATSolver = new DPLLSATSolver(sudoku.length);
        int[][] solvedSudoku = dpllSATSolver.solve(sudoku);  // Solve using SAT
        // Solver
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("=====================================");
        System.out.println("DPLL SAT Solver:");
        System.out.println("Elapsed time: " + elapsedTime + "ms");
        System.out.println("=====================================");
        dpllSATSolver.printBoard(solvedSudoku);
    }

    // Performance test for DLX Solver
    public static void solveUsingDLX(int[][] sudoku) {
        long startTime = System.currentTimeMillis();
        DLXSolver dlxSolver = new DLXSolver(sudoku.length);
        int[][] solvedSudoku = dlxSolver.solve(sudoku);  // Solve using DLX
        // Solver
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("=====================================");
        System.out.println("DLX Solver:");
        System.out.println("Elapsed time: " + elapsedTime + "ms");
        System.out.println("=====================================");
        dlxSolver.printBoard(solvedSudoku);
    }

    public static int[][] deepCopy(int[][] original) {
        if (original == null) return null;

        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return copy;
    }

    public static void main(String[] args) {
        // Try to load the puzzles from the CSV using the loader utility.
        // Option 2: Load large puzzles (16x16 or 25x25) from hardcoded strings:
        // List<int[][]> puzzles = NormalSudokuLargeGrids.loadLargeGrids();//Modify the CSV file path to run different datasets 
        // int[][] sudoku;
        
        // if (!puzzles.isEmpty()) {
        //     sudoku = puzzles.get(2); // Use the first loaded puzzle, modify the number to run different puzzles
        // } else {
        //     // Example Sudoku puzzle
        //     sudoku = new int[][]  {
        //             {4, 0, 0, 0, 0, 0, 8, 0, 5},
        //             {0, 3, 0, 0, 0, 0, 0, 0, 0},
        //             {0, 0, 0, 7, 0, 0, 0, 0, 0},
        //             {0, 2, 0, 0, 0, 0, 0, 6, 0},
        //             {0, 0, 0, 0, 8, 0, 4, 0, 0},
        //             {0, 0, 0, 0, 1, 0, 0, 0, 0},
        //             {0, 0, 0, 6, 0, 3, 0, 7, 0},
        //             {5, 0, 0, 2, 0, 0, 0, 0, 0},
        //             {1, 0, 4, 0, 0, 0, 0, 0, 0}
        //     };
        // }
        long memBeforeInit = getUsedMemory();
        long initStartTime = System.currentTimeMillis();

        int[][][] puzzles = PuzzleBank.getPuzzles();
        int[][] sudoku;

        if (puzzles.length > 0) {
            sudoku = puzzles[0]; // Select the first puzzle in the list
        } else {
            System.out.println("No puzzles found.");
            return;
        }

        long initEndTime = System.currentTimeMillis();
        long memAfterInit = getUsedMemory();
        long initializationTime = initEndTime - initStartTime;
        long initializationMemoryCost = memAfterInit - memBeforeInit;

        System.out.println("=====================================");
        System.out.println("Initialization time: " + initializationTime + " ms");
        System.out.println("Initialization memory cost: " + initializationMemoryCost + " bytes");
        System.out.println("Initial memory footprint before solving: " + memAfterInit + " bytes");
        System.out.println("=====================================");

        // ---------------------------
        // Run Each Solver with Peak Memory Monitoring
        // ---------------------------
        // For each solver, start memory monitoring, run the solver, then stop monitoring
        // and print out the peak memory usage recorded during solving.

        System.out.println("Running Backtracking Solver...");
        startMemoryMonitor();
        long btStart = System.currentTimeMillis();
        solveUsingBackTracking(deepCopy(sudoku));
        long btEnd = System.currentTimeMillis();
        stopMemoryMonitor();
        System.out.println("Backtracking Solver peak memory usage: " + peakMemoryUsage + " bytes");
        System.out.println("Total Backtracking Solver time: " + (btEnd - btStart) + " ms");
        System.out.println("=====================================");

        System.out.println("Running Constraint Propagation Solver...");
        startMemoryMonitor();
        long cpStart = System.currentTimeMillis();
        solveUsingConstraintPropagation(deepCopy(sudoku));
        long cpEnd = System.currentTimeMillis();
        stopMemoryMonitor();
        System.out.println("Constraint Propagation Solver peak memory usage: " + peakMemoryUsage + " bytes");
        System.out.println("Total Constraint Propagation Solver time: " + (cpEnd - cpStart) + " ms");
        System.out.println("=====================================");

        System.out.println("Running DPLL SAT Solver...");
        startMemoryMonitor();
        long dpllStart = System.currentTimeMillis();
        solveUsingDPLLSAT(deepCopy(sudoku));
        long dpllEnd = System.currentTimeMillis();
        stopMemoryMonitor();
        System.out.println("DPLL SAT Solver peak memory usage: " + peakMemoryUsage + " bytes");
        System.out.println("Total DPLL SAT Solver time: " + (dpllEnd - dpllStart) + " ms");
        System.out.println("=====================================");

        System.out.println("Running DLX Solver...");
        startMemoryMonitor();
        long dlxStart = System.currentTimeMillis();
        solveUsingDLX(deepCopy(sudoku));
        long dlxEnd = System.currentTimeMillis();
        stopMemoryMonitor();
        System.out.println("DLX Solver peak memory usage: " + peakMemoryUsage + " bytes");
        System.out.println("Total DLX Solver time: " + (dlxEnd - dlxStart) + " ms");
        System.out.println("=====================================");
    }
}
