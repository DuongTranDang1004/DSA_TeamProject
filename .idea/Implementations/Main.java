public class Main {

    // Performance test for Backtracking Solver
    public static void solveUsingBackTracking(int[][] sudoku) {
        long startTime = System.currentTimeMillis();
        BackTrackingSolver backTrackingSolver = new BackTrackingSolver(sudoku);
        backTrackingSolver.solve();
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("=====================================");
        System.out.println("Backtracking Solver:");
        System.out.println("Elapsed time: " + elapsedTime + "ms");
        System.out.println("=====================================");
    }

    // Performance test for Constraint Propagation Solver
    public static void solveUsingConstraintPropagation(int[][] sudoku) {
        long startTime = System.currentTimeMillis();
        ConstraintPropagationSolver constraintPropagationSolver = new ConstraintPropagationSolver(sudoku);
        constraintPropagationSolver.solve();
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("=====================================");
        System.out.println("Constraint Propagation Solver:");
        System.out.println("Elapsed time: " + elapsedTime + "ms");
        System.out.println("=====================================");
    }

    // Performance test for DPLL SAT Solver
    public static void solveUsingDPLLSAT(int[][] sudoku) {
        long startTime = System.currentTimeMillis();
        DPLLSATSolver dpllSATSolver = new DPLLSATSolver();
        dpllSATSolver.solve(sudoku);  // Solve using SAT Solver
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("=====================================");
        System.out.println("DPLL SAT Solver:");
        System.out.println("Elapsed time: " + elapsedTime + "ms");
        System.out.println("=====================================");
    }

    // Performance test for DLX Solver
    public static void solveUsingDLX(int[][] sudoku) {
        long startTime = System.currentTimeMillis();
        DLXSolver.solve(sudoku);  // Solve using DLX Solver
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("=====================================");
        System.out.println("DLX Solver:");
        System.out.println("Elapsed time: " + elapsedTime + "ms");
        System.out.println("=====================================");
    }

    public static void main(String[] args) {
        // Example Sudoku puzzle
        int[][] sudoku = {
            {5, 3, 0, 0, 7, 0, 0, 0, 0},
            {6, 0, 0, 1, 9, 5, 0, 0, 0},
            {0, 9, 8, 0, 0, 0, 0, 6, 0},
            {8, 0, 0, 0, 6, 0, 0, 0, 3},
            {4, 0, 0, 8, 0, 3, 0, 0, 1},
            {7, 0, 0, 0, 2, 0, 0, 0, 6},
            {0, 6, 0, 0, 0, 0, 2, 8, 0},
            {0, 0, 0, 4, 1, 9, 0, 0, 5},
            {0, 0, 0, 0, 8, 0, 0, 7, 9}
        };

        // Running all solvers and measuring their performance
        System.out.println("=====================================");
        System.out.println("Running Backtracking Solver...");
        solveUsingBackTracking(sudoku);

        System.out.println("=====================================");
        System.out.println("Running Constraint Propagation Solver...");
        solveUsingConstraintPropagation(sudoku);

        System.out.println("=====================================");
        System.out.println("Running DPLL SAT Solver...");
        solveUsingDPLLSAT(sudoku);

        System.out.println("=====================================");
        System.out.println("Running DLX Solver...");
        solveUsingDLX(sudoku);

        System.out.println("=====================================");
        System.out.println("Running DLX Test...");
        // Run DLX tests
        DLXSudokuTest.main(args);
    }
}
