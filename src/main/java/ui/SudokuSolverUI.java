package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import datasets.PuzzleBank;

public class SudokuSolverUI extends Application {

    private int[][] currentPuzzle = new int[9][9];
    private GridPane sudokuGrid = new GridPane();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sudoku Solver");

        VBox mainLayout = new VBox(10);
        HBox controlsLayout = new HBox(10);
        HBox buttonLayout = new HBox(10);

        Label puzzleLabel = new Label("Origin Puzzle:");
        Label statusLabel = new Label("Status: Waiting...");

        TextField puzzleTextField = new TextField();
        puzzleTextField.setPromptText("Enter Sudoku as a string (81 digits)");

        Button generatePuzzleButton = new Button("Generate Random Puzzle");
        Button submitPuzzleButton = new Button("Submit Puzzle");

        Button backtrackingButton = new Button("Backtracking");
        Button constraintPropagationButton = new Button("Constraint Propagation");
        Button dpllSatButton = new Button("DPLL-SAT");
        Button dlxButton = new Button("DLX");

        generatePuzzleButton.setOnAction(e -> generateRandomPuzzle());
        submitPuzzleButton.setOnAction(e -> submitPuzzle(puzzleTextField.getText()));

        backtrackingButton.setOnAction(e -> solveWithBacktracking());
        constraintPropagationButton.setOnAction(e -> solveWithConstraintPropagation());
        dpllSatButton.setOnAction(e -> solveWithDPLLSAT());
        dlxButton.setOnAction(e -> solveWithDLX());

        controlsLayout.getChildren().addAll(puzzleLabel, puzzleTextField, generatePuzzleButton, submitPuzzleButton);
        buttonLayout.getChildren().addAll(backtrackingButton, constraintPropagationButton, dpllSatButton, dlxButton);

        mainLayout.getChildren().addAll(controlsLayout, sudokuGrid, statusLabel, buttonLayout);
        mainLayout.setPadding(new Insets(10));

        Scene scene = new Scene(mainLayout, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void generateRandomPuzzle() {
        int[][] puzzle = PuzzleBank.getRandomPuzzle();
        displayPuzzle(puzzle);
    }

    private void submitPuzzle(String input) {
        int size = (int) Math.sqrt(input.length());
        if (input.length() == size * size) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    currentPuzzle[i][j] = Character.getNumericValue(input.charAt(i * size + j));
                }
            }
            displayPuzzle(currentPuzzle);
        }
    }

    private void displayPuzzle(int[][] puzzle) {
        sudokuGrid.getChildren().clear();
        sudokuGrid.setGridLinesVisible(true);
        int size = puzzle.length;

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                TextField cell = new TextField();
                cell.setText(puzzle[row][col] == 0 ? "" : String.valueOf(puzzle[row][col]));
                cell.setPrefWidth(40);
                cell.setPrefHeight(40);
                cell.setStyle("-fx-font-size: 16; -fx-alignment: center;");
                sudokuGrid.add(cell, col, row);
            }
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
        StringBuilder statusMessage = new StringBuilder("Solving with " + solverType + "...");
        statusMessage.append("\nSolved!");
        displayPuzzle(currentPuzzle);
    }
}
