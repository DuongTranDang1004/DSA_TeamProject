public class Main {
    public static void solveUsingBackTracking(int[][] sudoku){
        long startTime = System.currentTimeMillis();
        BackTrackingSolver backTrackingSolver = new BackTrackingSolver(sudoku);
        backTrackingSolver.solve();
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println(elapsedTime);
    }
    public static void solveUsingConstraintPropagation(int[][] sudoku){
        long startTime = System.currentTimeMillis();
        ConstraintPropagationSolver constraintPropagationSolver =
                new ConstraintPropagationSolver(sudoku);
        constraintPropagationSolver.solve();
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println(elapsedTime);
    }
    public static void main(String[] args) {

    }
}
