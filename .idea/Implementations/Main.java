import java.util.Arrays;

public class Main {

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
    }

    // Performance test for DPLL SAT Solver
    public static void solveUsingDPLLSAT(int[][] sudoku) {
        long startTime = System.currentTimeMillis();
        DPLLSATSolver dpllSATSolver = new DPLLSATSolver(sudoku.length);
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
        DLXSolver dlxSolver = new DLXSolver(sudoku.length);
        dlxSolver.solve(sudoku);  // Solve using DLX Solver
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("=====================================");
        System.out.println("DLX Solver:");
        System.out.println("Elapsed time: " + elapsedTime + "ms");
        System.out.println("=====================================");
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
        // Example Sudoku puzzle
        int[][] sudoku =  {
                {4, 0, 0, 0, 0, 0, 8, 0, 5},
                {0, 3, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 7, 0, 0, 0, 0, 0},
                {0, 2, 0, 0, 0, 0, 0, 6, 0},
                {0, 0, 0, 0, 8, 0, 4, 0, 0},
                {0, 0, 0, 0, 1, 0, 0, 0, 0},
                {0, 0, 0, 6, 0, 3, 0, 7, 0},
                {5, 0, 0, 2, 0, 0, 0, 0, 0},
                {1, 0, 4, 0, 0, 0, 0, 0, 0}
        };
        // Running all solvers and measuring their performance
        System.out.println("=====================================");
        System.out.println("Running Backtracking Solver...");
        solveUsingBackTracking(deepCopy(sudoku));

        System.out.println("=====================================");
        System.out.println("Running Constraint Propagation Solver...");
        solveUsingConstraintPropagation(deepCopy(sudoku));

        System.out.println("=====================================");
        System.out.println("Running DPLL SAT Solver...");
        solveUsingDPLLSAT(deepCopy(sudoku));

        System.out.println("=====================================");
        System.out.println("Running DLX Solver...");
        solveUsingDLX(deepCopy(sudoku));


    }
}
