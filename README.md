# DSA_TeamProject

## Contribution Table

| Student Name             | Student ID | Contribution Score |
|--------------------------|------------|---------------------|
| Tran Dang Duong          | s3979381   | 5                   |
| Luong Thanh Trung        | s3679813   | 5                   |
| Nguyen Pham Tan Hau      | s3978175   | 5                   |
| Nguyen Doan Trung Truc   | s3978175   | 5                   |

## Project Overview

This project implements a **Sudoku Solver** using various solving algorithms such as **Backtracking**, **Constraint Propagation**, **Dancing Links (DLX)**, and **DPLL-SAT**. It provides a **JavaFX UI** for visualizing the step-by-step solving process. The project also includes functionality for benchmarking the solvers and exporting the results in CSV format.

Additionally, **JUnit tests** have been added for unit testing, and there are **Python notebooks** for further analysis and visualization of the solver's performance.

## Project Structure

```plaintext
DSA_TeamProject/
│
├── notebooks/
│   └── images/
│   └── Analysis.ipynb 
│
├── src/
│   └── main/java/implementations/
│       ├── BackTrackingSolver.java
│       ├── ConstraintPropagationSolver.java
│       ├── DLXSolver.java
│       ├── DPLLSATSolver.java
│       ├── Main.java
│       └── SudokuCnfEncoder.java
│   └── ui/
│       └── SudokuSolverUI.java 
│   └── datasets/
│       └── PuzzleBank.java
│
├── test/
│   └── java/implementations/
│       └── MainTest.java
│
├── test-data/
│   └── puzzle_result.csv 
│
├── README.md 
├── LICENSE 
├── pom.xml 
└── requirements.txt 
```

## Explanation

- **notebooks/**: Contains Jupyter notebooks for data analysis and visualizations.
  - **images/**: Stores image files used in the analysis notebooks.
  - **Analysis.ipynb**: A Jupyter notebook for analyzing performance data and visualizing the results.

- **src/**: The main source code for the project.
  - **main/java/implementations/**: Contains the core solver algorithms.
    - **BackTrackingSolver.java**: Implements the backtracking algorithm for solving Sudoku.
    - **ConstraintPropagationSolver.java**: Implements constraint propagation algorithm for Sudoku solving.
    - **DLXSolver.java**: Implements the Dancing Links (DLX) algorithm for Sudoku solving.
    - **DPLLSATSolver.java**: Implements the DPLL SAT solver for Sudoku solving.
    - **Main.java**: Entry point for solving Sudoku puzzles and exporting results.
    - **SudokuCnfEncoder.java**: Converts Sudoku puzzles into CNF (Conjunctive Normal Form) for SAT solving.
  - **ui/**: Contains the user interface for the project.
    - **SudokuSolverUI.java**: JavaFX UI to visualize the step-by-step solution of Sudoku puzzles.
  - **datasets/**: Contains datasets or utilities to generate Sudoku puzzles.
    - **PuzzleBank.java**: Provides methods for generating random Sudoku puzzles.

- **test/**: Contains unit tests for the project.
  - **java/implementations/**: Contains unit tests for the solver algorithms.
    - **MainTest.java**: JUnit tests for testing the Main.java class and solving algorithms.

- **test-data/**: Stores benchmarking results.
  - **puzzle_result.csv**: A CSV file storing benchmark results (e.g., time, memory usage, etc.) for each puzzle solved.

- **README.md**: A markdown file containing the project description and setup instructions.

- **LICENSE**: The project’s license file.

- **pom.xml**: Maven configuration file for building, managing dependencies, and running the project.

- **requirements.txt**: Python dependencies for Jupyter notebooks (e.g., for data analysis).

This structure organizes the project by separating code, tests, data analysis, and other supporting files.

## How to Run the Project

### 1. Running the JavaFX UI

To launch the **Sudoku Solver UI**, use the following Maven command:

```bash
mvn javafx:run
```
This will open the **JavaFX UI,** allowing you to visualize the Sudoku solver's step-by-step solution.

### 2. Solving All Puzzles and Exporting Results to CSV
To solve all puzzles and generate a CSV file with benchmarking results, use the following Maven command:

```bash
mvn exec:java
```
This will execute the **`Main.java`** class, solving all Sudoku puzzles and saving the benchmark results to a **`puzzle_result.csv`** file in the test-data folder. The benchmark results include:
 + Solver performance metrics (time, number of guesses, etc.)
 + Peak memory usage during the solving process
 + Initialization time and memory usage

## Dependencies
### Maven Dependencies
 + JavaFX for building the user interface (javafx-controls, javafx-fxml)
 + JUnit for unit testing the solver algorithms

### Python Dependencies
 + Jupyter Notebook for analyzing performance and visualizing results
 + Matplotlib for plotting graphs in the notebook

### Development Environment
#### Global Requirements
| Requirement | Version |
| ----------- | ------- |
| Git         | latest  |
| Maven       | 4.0.0   |
| OpenJDK     | 17.0.2  |
| Python      | 3.11    |

#### VSCode Requirements
To build and run Java classes:

| Requirement             | Version |
| ----------------------- | ------- |
| Extension Pack for Java | latest  |
| Test Runner for Java    | latest  |


#### For Python notebooks:

| Requirement | Version |
| ----------- | ------- |
| Python      | latest  |
| Jupyter     | latest  |


#### IntelliJ IDEA Requirements
| Requirement   | Version |
| ------------- | ------- |
| Python Plugin | latest  |

To properly set up OpenJDK 17 in IntelliJ IDEA, refer to [the IntelliJ IDEA - SDKs documentation](https://www.jetbrains.com/idea/).

## Build and Execution
### Running the Solver:
To run the puzzle-solving program, call the **`Main.java`** class. This will solve all puzzles and generate the benchmark results.

## JUnit Tests:
All tests are located in **`test/java/implementations/MainTest.java`**. To run the tests, use:

```bash
mvn test
```
## Jupyter Notebook:
The **`Analysis.ipynb`** notebook can be opened in Jupyter. It will analyze the benchmark results and visualize the performance of different solvers.

## Video Demonstration
A video demonstrating the functionality of the project is available on YouTube: [Link].

Implement the backtracking + constrain propagation : Hau 
Impement the backingtracking + KnuthX and Dacing Link: Trung 
Implement the backtracking + SAT solver : Trung 
Implementation Requirements 
You must create a class RMIT_Sudoku_Solver with the following method: 
public class RMIT_Sudoku_Solver { 
 public int[][] solve(int[][] puzzle) { 
 // Implement your solution here 
 } 
}
The method solve(int[][] puzzle): 
• Accepts a 2D integer array (9x9) representing the Sudoku puzzle. 
• Each cell contains an integer from 0 to 9: 
o 1-9 => Pre-filled numbers (must remain unchanged in the solution) 
o 0 => Empty cells that your program must solve. 
• Returns a 9x9 2D array containing the solved Sudoku puzzle. 
• Each cell in the returned array must contain a value from 1 to 9. 
If your program cannot find a solution within 2 minutes (on any computer), it must raise an 
exception. 
You may create additional classes and methods needed to organize your code eectively. 
Each method require unit test.
