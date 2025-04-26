package implementations;

import java.util.*;

public class DLXSolver {
    private final int N;
    private final int CONSTRAINTS;

    private int propagationDepth = 0;
    private int numberOfGuesses = 0;

    public DLXSolver(int N) {
        if (Math.sqrt(N) != (int) Math.sqrt(N)) {
            throw new IllegalArgumentException("N must be a perfect square (e.g., 4, 9, 16).");
        }
        this.N = N;
        this.CONSTRAINTS = 4 * N * N;
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
            throw new IllegalArgumentException("Invalid Sudoku board: must be " + N + "x" + N + " and only contain values from 0 to " + N);
        }

        List<DLXNode> preset = new ArrayList<>();
        DLXHeader dlx = buildDLXStructure(board, preset);
        List<DLXNode> solution = new ArrayList<>();

        for (DLXNode node : preset) {
            for (DLXNode j = node.right; j != node; j = j.right)
                cover(j.column);
            cover(node.column);
            solution.add(node);
        }

        // ➡️ RESET lại biến đếm
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
            return decodeSolution(solution);
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

            numberOfGuesses++; // ➡️ mỗi lần chọn dòng là 1 lần thử chọn.

            solution.add(n);
            for (DLXNode j = row.right; j != row; j = j.right) cover(j.column);

            int[][] result = search(head, solution, depth + 1);
            if (result != null) return result;

            for (DLXNode j = row.left; j != row; j = j.left) uncover(j.column);
            solution.remove(solution.size() - 1);
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

    public void printBoard(int[][] board) {
        int boxSize = (int) Math.sqrt(N);
        for (int i = 0; i < N; i++) {
            if (i % boxSize == 0 && i != 0) {
                System.out.println("-".repeat(N * 2 + boxSize - 1));
            }
            for (int j = 0; j < N; j++) {
                if (j % boxSize == 0 && j != 0) System.out.print("| ");
                System.out.print(board[i][j] == 0 ? ". " : board[i][j] + " ");
            }
            System.out.println();
        }
    }

    public int[][] decodeSolution(List<DLXNode> solution) {
        int[][] board = new int[N][N];
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

    // ➡️ Getter để lấy thông số
    public int getPropagationDepth() {
        return propagationDepth;
    }

    public int getNumberOfGuesses() {
        return numberOfGuesses;
    }
}