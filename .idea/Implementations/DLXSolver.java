import java.util.*;

public class DLXSolver {
    private static final int N = 9;
    private static final int CONSTRAINTS = 4 * N * N;

    static class DLXNode {
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

    static class ColumnHeader extends DLXNode {
        int id;
        int size;

        public ColumnHeader(int id) {
            super();
            this.id = id;
            this.column = this;
        }
    }

    static class DLXHeader {
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

    public static DLXHeader buildDLXStructure(int[][] board, List<DLXNode> presetRows) {
        DLXHeader dlx = new DLXHeader();

        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                for (int d = 1; d <= N; d++) {
                    if (board[r][c] != 0 && board[r][c] != d) continue;

                    int cellCol = r * N + c;
                    int rowCol = N * N + r * N + (d - 1);
                    int colCol = 2 * N * N + c * N + (d - 1);
                    int boxSize = (int) Math.sqrt(N);
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

    public static void solve(int[][] board) {
        List<DLXNode> preset = new ArrayList<>();
        DLXHeader dlx = buildDLXStructure(board, preset);

        List<DLXNode> solution = new ArrayList<>();

        for (DLXNode node : preset) {
            for (DLXNode j = node.right; j != node; j = j.right)
                cover(j.column);
            cover(node.column);
            solution.add(node); // 🟢 Add to solution
        }

        int[][] solvedSudoku = search(dlx.head, solution);
        printBoard(solvedSudoku);
    }

    public static void cover(ColumnHeader column) {
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

    public static void uncover(ColumnHeader column) {
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

    public static int[][] search(ColumnHeader head, List<DLXNode> solution) {
        if (head.right == head) {
            return decodeSolution(solution);
        }

        ColumnHeader col = chooseColumnWithFewestNodes(head);
        if (col.size == 0) return null;

        cover(col);

        for (DLXNode row = col.down; row != col; row = row.down) {
            DLXNode n = row;
            do {
                if (n.column.id < N * N) break; // cell constraint
                n = n.right;
            } while (n != row);
            solution.add(n); // always add the node with cell info

            for (DLXNode j = row.right; j != row; j = j.right) {
                cover(j.column);
            }

            int[][] result = search(head, solution);
            if (result != null) return result; // ✅ found a solution, return it

            for (DLXNode j = row.left; j != row; j = j.left) {
                uncover(j.column);
            }

            solution.remove(solution.size() - 1);
        }

        uncover(col);
        return null;
    }

    private static ColumnHeader chooseColumnWithFewestNodes(ColumnHeader head) {
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

    public static void printBoard(int[][] sudoku) {
        for (int i = 0; i < 9; i++) {
            if (i % 3 == 0 && i != 0) System.out.println("------+-------+------");
            for (int j = 0; j < 9; j++) {
                if (j % 3 == 0 && j != 0) System.out.print("| ");
                System.out.print(sudoku[i][j] == 0 ? ". " : sudoku[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static int[][] decodeSolution(List<DLXNode> solution) {
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
}
