package implementations;

import java.util.*;

public class DLXSolver {
    private final int N;
    private final int CONSTRAINTS;

    private int[][] sudoku;
    private int[][] initialPuzzle; // Store initial puzzle to preserve given numbers
    private int propagationDepth = 0;
    private int numberOfGuesses = 0;
    private boolean isRunningInUI = false;
    private int stepCount = 0;
    private List<int[][]> steps = new ArrayList<>();

    public DLXSolver(int N, boolean isRunningInUI) {
        if (Math.sqrt(N) != (int) Math.sqrt(N)) {
            throw new IllegalArgumentException("N must be a perfect square (e.g., 4, 9, 16).");
        }
        this.N = N;
        this.CONSTRAINTS = 4 * N * N;
        this.isRunningInUI = isRunningInUI;
    }

    class DLXNode {
        DLXNode left, right, up, down;
        ColumnHeader column;
        int rowId;

        public DLXNode() {
            left = right = up = down = this;
        }

        public void linkRight(DLXNode node) {
            node.right = this.right;
            node.right.left = node;
            this.right = node;
            node.left = this;
        }

        public void linkDown(DLXNode node) {
            node.down = this.down;
            node.down.up = node;
            this.down = node;
            node.up = this;
        }
    }

    class ColumnHeader extends DLXNode {
        int id;
        int size;

        public ColumnHeader(int id) {
            super();
            this.id = id;
            this.column = this;
        }
    }

    class DLXHeader {
        ColumnHeader head;
        ColumnHeader[] columns;

        public DLXHeader() {
            head = new ColumnHeader(-1);
            columns = new ColumnHeader[CONSTRAINTS];

            ColumnHeader prev = head;
            for (int i = 0; i < CONSTRAINTS; i++) {
                ColumnHeader col = new ColumnHeader(i);
                columns[i] = col;
                prev.linkRight(col);
                prev = col;
            }
        }
    }

    public DLXHeader buildDLXStructure(int[][] board, List<DLXNode> presetRows) {
        DLXHeader dlx = new DLXHeader();
        int boxSize = (int) Math.sqrt(N);

        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                for (int d = 1; d <= N; d++) {
                    if (board[r][c] != 0 && board[r][c] != d) continue;

                    int cellCol = r * N + c;
                    int rowCol = N * N + r * N + (d - 1);
                    int colCol = 2 * N * N + c * N + (d - 1);
                    int boxId = (r / boxSize) * boxSize + (c / boxSize);
                    int boxCol = 3 * N * N + boxId * N + (d - 1);

                    DLXNode[] nodes = new DLXNode[4];
                    int[] constraintIndices = {cellCol, rowCol, colCol, boxCol};
                    for (int i = 0; i < 4; i++) {
                        nodes[i] = new DLXNode();
                        nodes[i].rowId = r * N * N + c * N + (d - 1);
                        ColumnHeader col = dlx.columns[constraintIndices[i]];
                        nodes[i].column = col;
                        col.linkDown(nodes[i]);
                        col.size++;
                    }

                    nodes[0].right = nodes[1]; nodes[1].left = nodes[0];
                    nodes[1].right = nodes[2]; nodes[2].left = nodes[1];
                    nodes[2].right = nodes[3]; nodes[3].left = nodes[2];
                    nodes[3].right = nodes[0]; nodes[0].left = nodes[3];

                    if (board[r][c] == d) {
                        presetRows.add(nodes[0]);
                    }
                }
            }
        }
        return dlx;
    }

    public int[][] solve(int[][] board) {
        if (!isValidBoard(board)) {
            throw new IllegalArgumentException("Board must be " + N + "x" + N + " and contain values from 0 to " + N);
        }

        this.initialPuzzle = copyBoard(board); // Store initial puzzle
        this.sudoku = copyBoard(board); // Initialize sudoku with a copy
        if (isRunningInUI) {
            storeStep(); // Store initial state
        }
        List<DLXNode> preset = new ArrayList<>();
        DLXHeader dlx = buildDLXStructure(sudoku, preset);
        List<DLXNode> solution = new ArrayList<>();

        for (DLXNode node : preset) {
            for (DLXNode j = node.right; j != node; j = j.right)
                cover(j.column);
            cover(node.column);
            solution.add(node);
        }
        // Store state after all presets are processed
        if (isRunningInUI) {
            updateSudokuFromSolution(solution);
            storeStep();
        }

        propagationDepth = 0;
        numberOfGuesses = 0;

        int[][] solved = search(dlx.head, solution, 0);
        return solved;
    }

    private boolean isValidBoard(int[][] board) {
        if (board.length != N) return false;
        for (int[] row : board) {
            if (row.length != N) return false;
            for (int cell : row) {
                if (cell < 0 || cell > N) return false;
            }
        }
        return true;
    }

    public void cover(ColumnHeader column) {
        column.right.left = column.left;
        column.left.right = column.right;

        for (DLXNode row = column.down; row != column; row = row.down) {
            for (DLXNode node = row.right; node != row; node = node.right) {
                node.down.up = node.up;
                node.up.down = node.down;
                node.column.size--;
            }
        }
    }

    public void uncover(ColumnHeader column) {
        for (DLXNode row = column.up; row != column; row = row.up) {
            for (DLXNode node = row.left; node != row; node = node.left) {
                node.column.size++;
                node.down.up = node;
                node.up.down = node;
            }
        }
        column.right.left = column;
        column.left.right = column;
    }

    public int[][] search(ColumnHeader head, List<DLXNode> solution, int depth) {
        propagationDepth = Math.max(propagationDepth, depth);

        if (head.right == head) {
            int[][] result = decodeSolution(solution);
            if (isRunningInUI) {
                sudoku = copyBoard(result);
                storeStep();
            }
            return result;
        }

        ColumnHeader col = chooseColumnWithFewestNodes(head);
        if (col.size == 0) return null;

        cover(col);
        for (DLXNode row = col.down; row != col; row = row.down) {
            DLXNode n = row;
            do {
                if (n.column.id < N * N) break;
                n = n.right;
            } while (n != row);

            numberOfGuesses++;

            solution.add(n);
            if (isRunningInUI) {
                updateSudokuFromSolution(solution);
                storeStep();
            }

            for (DLXNode j = row.right; j != row; j = j.right) cover(j.column);

            int[][] result = search(head, solution, depth + 1);
            if (result != null) return result;

            for (DLXNode j = row.left; j != row; j = j.left) uncover(j.column);
            solution.remove(solution.size() - 1);
            if (isRunningInUI) {
                updateSudokuFromSolution(solution);
                storeStep();
            }
        }

        uncover(col);
        return null;
    }

    private ColumnHeader chooseColumnWithFewestNodes(ColumnHeader head) {
        ColumnHeader best = null;
        int minSize = Integer.MAX_VALUE;
        for (ColumnHeader col = (ColumnHeader) head.right; col != head; col = (ColumnHeader) col.right) {
            if (col.size < minSize) {
                minSize = col.size;
                best = col;
            }
        }
        return best;
    }

    private void updateSudokuFromSolution(List<DLXNode> solution) {
        sudoku = copyBoard(initialPuzzle); // Start with initial puzzle
        for (DLXNode node : solution) {
            DLXNode cellNode = node;
            DLXNode temp = node;

            do {
                if (temp.column.id < N * N) {
                    cellNode = temp;
                    break;
                }
                temp = temp.right;
            } while (temp != node);

            int rowId = cellNode.rowId;
            int r = rowId / (N * N);
            int c = (rowId / N) % N;
            int d = rowId % N + 1;
            sudoku[r][c] = d;
        }
    }

    public int[][] decodeSolution(List<DLXNode> solution) {
        int[][] board = copyBoard(initialPuzzle); // Start with initial puzzle
        for (DLXNode node : solution) {
            DLXNode cellNode = node;
            DLXNode temp = node;

            do {
                if (temp.column.id < N * N) {
                    cellNode = temp;
                    break;
                }
                temp = temp.right;
            } while (temp != node);

            int rowId = cellNode.rowId;
            int r = rowId / (N * N);
            int c = (rowId / N) % N;
            int d = rowId % N + 1;
            board[r][c] = d;
        }
        return board;
    }

    public int getPropagationDepth() {
        return propagationDepth;
    }

    public int getNumberOfGuesses() {
        return numberOfGuesses;
    }

    public void storeStep() {
        int[][] step = copyBoard(sudoku);
        if (steps.isEmpty() || !Arrays.deepEquals(steps.get(steps.size() - 1), step)) {
            steps.add(step);
            stepCount++;
        }
    }

    public int getStepCount() {
        return stepCount;
    }

    public List<int[][]> getSteps() {
        return steps;
    }

    private int[][] copyBoard(int[][] board) {
        int[][] copy = new int[N][N];
        for (int i = 0; i < N; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, N);
        }
        return copy;
    }
}