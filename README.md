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
|   ├── test/
|       ├── BacktrackingTest.java
|       ├── ConstraintPropagationTest.java
|       ├── DLXSolverTest.java
|       ├── SATSolverTest.java
├── results
|      ├── puzzle_result_extra.csv
|      ├── puzzle_result_extra_16x16.csv
|      ├── puzzle_result_extra_25x25.csv
├── README.md 
├── LICENSE 
└── pom.xml 
```

## Explanation

- **notebooks/**: Contains Jupyter notebooks for data analysis and visualizations.
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
    - **BacktrackingTest.java**: Tests the classic backtracking implementation.
    - **ConstraintPropagationTest.java**: Tests constraint propagation with MRV heuristic.
    - **DLXSolverTest.java**: Tests the Dancing Links (DLX) algorithm.
    - **SATSolverTest.java**: Tests the SAT-based solver (DPLL).

- **results/**: Contains CSV files documenting the experimental results for the different Sudoku puzzle sizes (9x9, 16x16, and 25x25).
    - **puzzle_result_extra.csv**
    - **puzzle_result_extra_16x16.csv**
    - **puzzle_result_extra_25x25.csv**

- **README.md**: A markdown file containing the project description and setup instructions.

- **LICENSE**: The project’s license file.

- **pom.xml**: Maven configuration file for building, managing dependencies, and running the project.


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
All tests are located in **`test`**. To run the tests, use:

```bash
mvn test
```
## Jupyter Notebook:
The **`Analysis.ipynb`** notebook can be opened in Jupyter or JupyterLab. It is designed to analyze the benchmark results generated in the `results/` folder and visualize the performance of different Sudoku solvers. The notebook performs the following tasks:
- **Data Loading**: Reads benchmark CSV files (for 9x9, 16x16, and 25x25 puzzles) from the results/ directory using relative paths.

- **Data Analysis**: Computes key performance metrics (runtime, memory usage, initialization time, etc.) for each solver.

- **Visualization**: Generates various plots (e.g., grouped bar charts, scatterplots, box plots) to compare solver performance and scalability.

- **Insights**: Provides visual insights into trade-offs between runtime efficiency, memory consumption, and initialization overhead across solvers.

**Requirements**
- Python 3
- Jupyter Notebook or JupyterLab
- Libraries: Pandas, Matplotlib, Seaborn

**How to Use**
1. Navigate to the notebooks/ directory in your terminal.

2. Launch Jupyter Notebook by running:
      ```bash
      jupyter notebook
      ```

3. Open the **`Analysis.ipynb`** file.

4. Run the cells sequentially to load the CSV data and generate visualizations.

## Video Demonstration
A video demonstrating the functionality of the project is available on [OneDrive](https://rmiteduau-my.sharepoint.com/:v:/g/personal/s3974820_rmit_edu_vn/EXKQZg1V3v9KiKSW23Ch5doB9lcR686WZZxYeBzQbr14QA?nav=eyJyZWZlcnJhbEluZm8iOnsicmVmZXJyYWxBcHAiOiJTdHJlYW1XZWJBcHAiLCJyZWZlcnJhbFZpZXciOiJTaGFyZURpYWxvZy1MaW5rIiwicmVmZXJyYWxBcHBQbGF0Zm9ybSI6IldlYiIsInJlZmVycmFsTW9kZSI6InZpZXcifX0%3D&e=fi0B5h)
