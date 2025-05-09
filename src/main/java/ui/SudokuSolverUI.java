package ui;

import javafx.application.Application;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.util.Duration;
import datasets.PuzzleBank;
import implementations.*;

import java.util.List;
import java.util.ArrayList;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

/*
 * ============================================
 *       Sudoku Solver UI Class
 * ============================================
 * User For: Creating a graphical user interface (GUI) for interacting with and visualizing Sudoku solvers, including features for solving, visualizing steps, and benchmarking memory and time performance.
 * Written By: Group 1 in @RMIT - 2025 for Group Project of COSC2469 Algorithm And Analysis Course
 * ============================================
 */

public class SudokuSolverUI extends Application {

    private int[][] currentPuzzle;
    private int[][] originalPuzzle;
    private int[][] previousStep;
    private GridPane sudokuGrid = new GridPane();
    private boolean visualizeSolvingSteps = false;
    private Label statusLabel = new Label("Status: Ready");
    private List<int[][]> solvingSteps;
    private Timeline animationTimeline;
    private double animationSpeed = 1000;
    private int currentStepIndex;
    private ScrollPane gridScrollPane = new ScrollPane();

    private long initStartTime;
    private long initEndTime;
    private long initTime;
    private long initMemCost;
    private long solvingTime;
    private int hintCount;

    private long peakMemoryUsage = 0;
    private boolean monitoring = false;

    public long getUsedMemory() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        return memoryBean.getHeapMemoryUsage().getUsed();
    }

    public void startMemoryMonitor() {
        monitoring = true;
        peakMemoryUsage = getUsedMemory();
        Thread monitor = new Thread(() -> {
            while (monitoring) {
                long current = getUsedMemory();
                if (current > peakMemoryUsage) {
                    peakMemoryUsage = current;
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {}
            }
        });
        monitor.setDaemon(true);
        monitor.start();
    }

    public void stopMemoryMonitor() {
        monitoring = false;
    }

    public long getPeakMemoryUsage() {
        return peakMemoryUsage;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sudoku Solver");

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(15));

        HBox inputSection = createInputSection();
        HBox optionsSection = createOptionsSection();
        HBox solverButtonsSection = createSolverButtonsSection();
        HBox animationControlsSection = createAnimationControlsSection();
        animationControlsSection.setVisible(false);

        configureGridScrollPane();

        mainLayout.getChildren().addAll(
            inputSection,
            new Separator(),
            gridScrollPane,
            statusLabel,
            optionsSection,
            solverButtonsSection,
            animationControlsSection
        );

        Scene scene = new Scene(mainLayout, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    private HBox createInputSection() {
        HBox inputSection = new HBox(10);
        Label puzzleLabel = new Label("Puzzle:");
        TextField puzzleTextField = new TextField();
        puzzleTextField.setPromptText("Enter Sudoku as a string (0 for empty cells)");
        puzzleTextField.setPrefWidth(300);

        Button generateBtn = new Button("Generate Random");
        Button submitBtn = new Button("Submit");

        generateBtn.setOnAction(e -> generateRandomPuzzle());
        submitBtn.setOnAction(e -> submitPuzzle(puzzleTextField.getText()));

        inputSection.getChildren().addAll(puzzleLabel, puzzleTextField, generateBtn, submitBtn);
        return inputSection;
    }

    private HBox createOptionsSection() {
        HBox optionsSection = new HBox(15);
        Label modeLabel = new Label("Mode:");

        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton runMode = new RadioButton("Run");
        RadioButton visualizeMode = new RadioButton("Visualize Steps");
        runMode.setToggleGroup(modeGroup);
        visualizeMode.setToggleGroup(modeGroup);
        runMode.setSelected(true);

        runMode.setOnAction(e -> visualizeSolvingSteps = false);
        visualizeMode.setOnAction(e -> visualizeSolvingSteps = true);

        optionsSection.getChildren().addAll(modeLabel, runMode, visualizeMode);
        return optionsSection;
    }

    private HBox createSolverButtonsSection() {
        HBox solverButtons = new HBox(10);

        Button backtrackingBtn = new Button("Backtracking");
        Button constraintBtn = new Button("Constraint Propagation");
        Button dpllBtn = new Button("DPLL-SAT");
        Button dlxBtn = new Button("DLX");

        backtrackingBtn.setOnAction(e -> solveWithBacktracking());
        constraintBtn.setOnAction(e -> solveWithConstraintPropagation());
        dpllBtn.setOnAction(e -> solveWithDPLLSAT());
        dlxBtn.setOnAction(e -> solveWithDLX());

        solverButtons.getChildren().addAll(backtrackingBtn, constraintBtn, dpllBtn, dlxBtn);
        return solverButtons;
    }

    private HBox createAnimationControlsSection() {
        HBox animationControls = new HBox(10);

        Button pauseBtn = new Button("Pause");
        Button resumeBtn = new Button("Resume");
        Button prevBtn = new Button("Previous");
        Button nextBtn = new Button("Next");
        Label speedLabel = new Label("Speed:");
        Slider speedSlider = new Slider(500, 1500, 1000);
        speedSlider.setShowTickLabels(true);
        speedSlider.setMajorTickUnit(250);

        pauseBtn.setOnAction(e -> pauseAnimation());
        resumeBtn.setOnAction(e -> resumeAnimation());
        prevBtn.setOnAction(e -> showPreviousStep());
        nextBtn.setOnAction(e -> showNextStep());
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            animationSpeed = newVal.doubleValue();
            restartAnimationIfRunning();
        });

        animationControls.getChildren().addAll(pauseBtn, resumeBtn, prevBtn, nextBtn, speedLabel, speedSlider);
        return animationControls;
    }

    private void configureGridScrollPane() {
        gridScrollPane.setContent(sudokuGrid);
        gridScrollPane.setFitToWidth(true);
        gridScrollPane.setFitToHeight(true);
        gridScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        gridScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        gridScrollPane.setPrefViewportHeight(400);
    }

    private void generateRandomPuzzle() {
        long startTime = System.nanoTime();
        int[][] puzzle = PuzzleBank.getRandomPuzzle();
        originalPuzzle = copyPuzzle(puzzle);
        currentPuzzle = copyPuzzle(puzzle);
        previousStep = null;
        displayPuzzle(puzzle);

        startMemoryMonitor();
        initStartTime = System.nanoTime();

        long initMemBefore = getUsedMemory();

        initEndTime = System.nanoTime();
        long initMemAfter = getUsedMemory();
        initTime = (initEndTime - initStartTime) / 1_000;  
        initMemCost = Math.max(initMemAfter - initMemBefore, 1);  

        hintCount = countHints(puzzle);

        statusLabel.setText("Random puzzle generated");
    }

    private void submitPuzzle(String input) {
        if (input == null || input.isEmpty()) {
            statusLabel.setText("Error: Input is empty");
            return;
        }

        int size = (int) Math.sqrt(input.length());
        int expectedLength = size * size;
        if (expectedLength != input.length()) {
            int diff = expectedLength - input.length();
            if (diff == 1) {
                input = input + "0";  
                size = (int) Math.sqrt(input.length());
                statusLabel.setText("Input was one digit short; padded with a 0");
            } else {
                statusLabel.setText("Error: Input length must be a perfect square (e.g., 81 for 9x9, 256 for 16x16, 625 for 25x25). Got: " + input.length());
                return;
            }
        }

        if (!input.matches("[0-9]+")) {
            statusLabel.setText("Error: Input must contain only digits 0-9");
            return;
        }

        originalPuzzle = new int[size][size];
        currentPuzzle = new int[size][size];
        previousStep = null;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                originalPuzzle[i][j] = Character.getNumericValue(input.charAt(i * size + j));
                currentPuzzle[i][j] = originalPuzzle[i][j];
            }
        }

        hintCount = countHints(currentPuzzle);

        displayPuzzle(currentPuzzle);
        statusLabel.setText("Puzzle submitted (" + size + "x" + size + ")");
    }

    private int countHints(int[][] board) {
        int count = 0;
        for (int[] row : board)
            for (int cell : row)
                if (cell != 0) count++;
        return count;
    }

    private void displayPuzzle(int[][] puzzle) {
        sudokuGrid.getChildren().clear();
        sudokuGrid.getColumnConstraints().clear();
        sudokuGrid.getRowConstraints().clear();
        sudokuGrid.setGridLinesVisible(false);

        int size = puzzle.length;
        double maxCellSize = 60;
        double cellSize = Math.min(maxCellSize, 800.0 / size);

        int subgridSize = (int) Math.sqrt(size);
        if (subgridSize * subgridSize != size) {
            subgridSize = 3;
        }

        for (int i = 0; i < size; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints(cellSize);
            RowConstraints rowConstraints = new RowConstraints(cellSize);
            sudokuGrid.getColumnConstraints().add(colConstraints);
            sudokuGrid.getRowConstraints().add(rowConstraints);
        }

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                TextField cell = createSudokuCell(puzzle[row][col], originalPuzzle[row][col]);
                StringBuilder borderStyle = new StringBuilder();
                if (row % subgridSize == 0) {
                    borderStyle.append("-fx-border-width: 2 1 1 1; ");
                } else {
                    borderStyle.append("-fx-border-width: 1 1 1 1; ");
                }
                if (col % subgridSize == 0) {
                    borderStyle.append("-fx-border-width: ").append(row % subgridSize == 0 ? "2" : "1").append(" 1 1 2; ");
                }
                if ((col + 1) % subgridSize == 0 && col != size - 1) {
                    borderStyle.append("-fx-border-width: ").append(row % subgridSize == 0 ? "2" : "1").append(" 2 1 ").append(col % subgridSize == 0 ? "2" : "1").append("; ");
                }
                if ((row + 1) % subgridSize == 0 && row != size - 1) {
                    borderStyle.append("-fx-border-width: ").append(row % subgridSize == 0 ? "2" : "1").append(" ").append((col + 1) % subgridSize == 0 ? "2" : "1").append(" 2 ").append(col % subgridSize == 0 ? "2" : "1").append("; ");
                }
                borderStyle.append("-fx-border-color: black; ");
                cell.setStyle(cell.getStyle() + borderStyle.toString());
                GridPane.setColumnIndex(cell, col);
                GridPane.setRowIndex(cell, row);
                sudokuGrid.add(cell, col, row);
            }
        }
    }

    private TextField createSudokuCell(int value, int originalValue) {
        TextField cell = new TextField();
        cell.setPrefSize(40, 40);
        cell.setEditable(false);
        updateCellAppearance(cell, value, originalValue, false);
        return cell;
    }

    private void updateCellAppearance(TextField cell, int value, int originalValue, boolean isChanged) {
        cell.setText(value == 0 ? "" : String.valueOf(value));
        double fontSize = originalPuzzle.length <= 9 ? 18 :
                        originalPuzzle.length <= 16 ? 14 : 10;
        String baseStyle = String.format("-fx-font-size: %.0f; -fx-alignment: center;", fontSize);

        if (isChanged) {
            String color = (value == 0 && previousStep != null &&
                          previousStep[GridPane.getRowIndex(cell)][GridPane.getColumnIndex(cell)] != 0)
                          ? "orange" : "lightblue"; 
            cell.setStyle("-fx-background-color: " + color + "; -fx-font-weight: bold;" + baseStyle);
            new Timeline(new KeyFrame(Duration.millis(1500), e -> {
                String currentStyle = cell.getStyle();
                cell.setStyle((originalValue != 0 ? "-fx-font-weight: bold;" : "") + baseStyle +
                              currentStyle.substring(currentStyle.indexOf("-fx-border-width")));
            })).play();
        } else {
            cell.setStyle((originalValue != 0 ? "-fx-font-weight: bold;" : "") + baseStyle);
        }
    }

    private void solveWithBacktracking() {
        solvePuzzle("Backtracking");
    }

    private void solveWithConstraintPropagation() {
        solvePuzzle("Constraint Propagation");
    }

    private void solveWithDPLLSAT() {
        solvePuzzle("DPLL-SAT");
    }

    private void solveWithDLX() {
        solvePuzzle("DLX");
    }

    private void solvePuzzle(String solverType) {
        if (currentPuzzle == null) {
            statusLabel.setText("Error: No puzzle loaded");
            return;
        }

        statusLabel.setText("Solving with " + solverType + "...");

        long startSolvingTime = System.nanoTime();

        if (visualizeSolvingSteps) {
            visualizeSteps(solverType);
        } else {
            solveAndDisplayPuzzle(solverType);
        }

        solvingTime = (System.nanoTime() - startSolvingTime) / 1_000;  
    }

    private void solveAndDisplayPuzzle(String solverType) {
        int[][] solvedPuzzle = copyPuzzle(currentPuzzle);
        long peakMemory = 0; 

        if (solverType.equals("Backtracking")) {
            BackTrackingSolver solver = new BackTrackingSolver(solvedPuzzle.length, false);
            solver.setTimeoutMillis(120_000);
            solvedPuzzle = solver.solve(solvedPuzzle);
        } else if (solverType.equals("Constraint Propagation")) {
            ConstraintPropagationSolver solver = new ConstraintPropagationSolver(solvedPuzzle.length, false);
            solvedPuzzle = solver.solve(solvedPuzzle);
        } else if (solverType.equals("DPLL-SAT")) {
            DPLLSATSolver solver = new DPLLSATSolver(solvedPuzzle.length, false);
            solvedPuzzle = solver.solve(solvedPuzzle);
        } else if (solverType.equals("DLX")) {
            DLXSolver solver = new DLXSolver(solvedPuzzle.length, false);
            solvedPuzzle = solver.solve(solvedPuzzle);
        }

        peakMemory = getPeakMemoryUsage();

        if (solvedPuzzle != null) {
            currentPuzzle = solvedPuzzle;
            previousStep = null;
            displayPuzzle(currentPuzzle);
            statusLabel.setText("Solved with " + solverType + " (Memory: " + peakMemory + " bytes)" +
                                "| Hint Count: "+ hintCount + " hints| Solving time: " + solvingTime + "μs | Init Time: " + initTime + "μs");
        } else {
            statusLabel.setText("Failed to solve with " + solverType);
        }
    }

    private void visualizeSteps(String solverType) {
        int[][] puzzleToSolve = copyPuzzle(currentPuzzle);
        if (solverType.equals("Backtracking")) {
            BackTrackingSolver solver = new BackTrackingSolver(puzzleToSolve.length, true);
            solver.solve(puzzleToSolve);
            solvingSteps = solver.getSteps();
        } else if (solverType.equals("Constraint Propagation")) {
            ConstraintPropagationSolver solver = new ConstraintPropagationSolver(puzzleToSolve.length, true);
            solver.solve(puzzleToSolve);
            solvingSteps = solver.getSteps();
        } else if (solverType.equals("DPLL-SAT")) {
            DPLLSATSolver solver = new DPLLSATSolver(puzzleToSolve.length, true);
            solver.solve(puzzleToSolve);
            solvingSteps = solver.getSteps();
        } else if (solverType.equals("DLX")) {
            DLXSolver solver = new DLXSolver(puzzleToSolve.length, true);
            solver.solve(puzzleToSolve);
            solvingSteps = solver.getSteps();
        }

        if (solvingSteps == null || solvingSteps.isEmpty()) {
            statusLabel.setText("No steps available for visualization");
            return;
        }

        List<int[][]> filteredSteps = filterPresetSteps(solvingSteps);

        if (filteredSteps.isEmpty()) {
            statusLabel.setText("Error: No valid steps after filtering");
            return;
        }

        solvingSteps = filteredSteps;
        visualizeStepsWithAnimation(filteredSteps, solverType);
    }

    private List<int[][]> filterPresetSteps(List<int[][]> steps) {
        List<int[][]> filteredSteps = new ArrayList<>();
        boolean foundCompletePreset = false;

        List<int[]> presetPositions = new ArrayList<>();
        int presetCount = 0;
        for (int i = 0; i < originalPuzzle.length; i++) {
            for (int j = 0; j < originalPuzzle[i].length; j++) {
                if (originalPuzzle[i][j] != 0) {
                    presetPositions.add(new int[]{i, j, originalPuzzle[i][j]});
                    presetCount++;
                }
            }
        }

        for (int s = 0; s < steps.size(); s++) {
            int[][] step = steps.get(s);
            int nonZeroCount = 0;
            boolean matchesAllPresets = true;

            for (int i = 0; i < step.length; i++) {
                for (int j = 0; j < step[i].length; j++) {
                    if (step[i][j] != 0) {
                        nonZeroCount++;
                    }
                }
            }

            for (int[] preset : presetPositions) {
                int row = preset[0], col = preset[1], value = preset[2];
                if (step[row][col] != value) {
                    matchesAllPresets = false;
                    break;
                }
            }

            if (!foundCompletePreset && matchesAllPresets && nonZeroCount >= presetCount) {
                foundCompletePreset = true;
                filteredSteps.add(step);
            } else if (foundCompletePreset) {
                filteredSteps.add(step);
            }
        }

        return filteredSteps;
    }

    private void visualizeStepsWithAnimation(List<int[][]> steps, String solverType) {
        ((HBox) gridScrollPane.getParent().getChildrenUnmodifiable().get(6)).setVisible(true);
        previousStep = copyPuzzle(currentPuzzle);
        currentStepIndex = 0;

        if (animationTimeline != null) {
            animationTimeline.stop();
        }

        animationTimeline = new Timeline();

        updateGridWithStep(steps.get(0));
        statusLabel.setText("Step 1/" + steps.size() + " (" + solverType + ")");
        currentStepIndex++;

        for (int i = 1; i < steps.size(); i++) {
            final int[][] step = steps.get(i);
            final int stepNum = i;
            KeyFrame keyFrame = new KeyFrame(Duration.millis(animationSpeed * i), e -> {
                updateGridWithStep(step);
                currentStepIndex = stepNum;
                statusLabel.setText("Step " + (currentStepIndex + 1) + "/" + steps.size() + " (" + solverType + ")");
            });
            animationTimeline.getKeyFrames().add(keyFrame);
        }

        animationTimeline.setOnFinished(e -> {
            statusLabel.setText(solverType + " Solved!");
            ((HBox) gridScrollPane.getParent().getChildrenUnmodifiable().get(6)).setVisible(false);
            animationTimeline = null;
            currentPuzzle = steps.get(steps.size() - 1);
        });

        animationTimeline.setCycleCount(1);
        animationTimeline.play();
    }

    private void pauseAnimation() {
        if (animationTimeline != null) {
            animationTimeline.pause();
            statusLabel.setText("Visualization paused at Step " + (currentStepIndex + 1) + "/" + solvingSteps.size());
        }
    }

    private void resumeAnimation() {
        if (animationTimeline != null) {
            animationTimeline.play();
            statusLabel.setText("Visualizing Step " + (currentStepIndex + 1) + "/" + solvingSteps.size());
        }
    }

    private void showPreviousStep() {
        if (animationTimeline != null && solvingSteps != null && currentStepIndex > 0) {
            animationTimeline.pause();
            currentStepIndex--;
            updateGridWithStep(solvingSteps.get(currentStepIndex));
            statusLabel.setText("Step " + (currentStepIndex + 1) + "/" + solvingSteps.size() + " (" + statusLabel.getText().split(" ")[2]);
        }
    }

    private void showNextStep() {
        if (animationTimeline != null && solvingSteps != null && currentStepIndex < solvingSteps.size() - 1) {
            animationTimeline.pause();
            currentStepIndex++;
            updateGridWithStep(solvingSteps.get(currentStepIndex));
            statusLabel.setText("Step " + (currentStepIndex + 1) + "/" + solvingSteps.size() + " (" + statusLabel.getText().split(" ")[2]);
        }
    }

    private void restartAnimationIfRunning() {
        if (animationTimeline != null && animationTimeline.getStatus() == Timeline.Status.RUNNING) {
            String solverType = statusLabel.getText().split(" ")[2].replace("(", "").replace(")", "");
            visualizeStepsWithAnimation(solvingSteps, solverType);
        }
    }

    private void updateGridWithStep(int[][] step) {
        int size = step.length;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (previousStep == null || previousStep[row][col] != step[row][col]) {
                    TextField cell = (TextField) getNodeFromGridPane(sudokuGrid, col, row);
                    if (cell != null) {
                        updateCellAppearance(cell, step[row][col], originalPuzzle[row][col], true);
                    }
                }
            }
        }
        previousStep = copyPuzzle(step);
    }

    private Node getNodeFromGridPane(GridPane grid, int column, int row) {
        for (Node node : grid.getChildren()) {
            Integer colIndex = GridPane.getColumnIndex(node);
            Integer rowIndex = GridPane.getRowIndex(node);
            if (colIndex == null) {
                colIndex = 0;
            }
            if (rowIndex == null) {
                rowIndex = 0;
            }
            if (colIndex != null && rowIndex != null && colIndex == column && rowIndex == row) {
                return node;
            }
        }
        return null;
    }

    private int[][] copyPuzzle(int[][] puzzle) {
        int size = puzzle.length;
        int[][] copy = new int[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(puzzle[i], 0, copy[i], 0, size);
        }
        return copy;
    }
}
