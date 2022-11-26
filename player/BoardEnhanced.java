package player;

import java.util.Arrays;

import mnkgame.MNKBoard;
import mnkgame.MNKCell;

public class BoardEnhanced {
    private CellEnhanced board[];
    private CellEnhanced moveOrder[];
    private int M, N, K;
    public MNKBoard mnkboard;

    public BoardEnhanced(int m, int n, int k, MNKBoard b) {
        this(m, n, k);
        this.mnkboard = b;
    }

    public BoardEnhanced(int M, int N, int K) {
        this.M = M;
        this.N = N;
        this.K = K;
        mnkboard = new MNKBoard(M, N, K);
        board = new CellEnhanced[M * N];
        moveOrder = new CellEnhanced[M * N];
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                int p = matrixToArrayCoords(i, j);
                board[p] = new CellEnhanced(new MNKCell(i, j));
                moveOrder[p] = board[p];
            }
        }
    }

    private void checkStar(int i, int j, int d) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (x == 0 && y == 0)
                    continue;
                for (int k = 1; k < K; k++) {
                    int i1 = i + k * x, j1 = j - k * y;
                    if (inBounds(i1, j1))
                        board[matrixToArrayCoords(i1, j1)].addWeight(d * (K - k + 1));
                }
            }
        }
    }

    public void unmarkCell(int i, int j) {
        mnkboard.unmarkCell();
        board[matrixToArrayCoords(i, j)].state = mnkboard.cellState(i, j);
        checkStar(i, j, -1);
    }

    public void markCell(int i, int j) {
        mnkboard.markCell(i, j);
        board[matrixToArrayCoords(i, j)].state = mnkboard.cellState(i, j);
        checkStar(i, j, 1);
    }

    private int matrixToArrayCoords(int i, int j) {
        return i * N + j;
    }

    private boolean inBounds(int x, int y) {
        return ((0 <= x) && (x < M) && (0 <= y) && (y < N)) ? true : false;
    }

    private boolean inBounds(int p) {
        return ((0 <= p) && (p < M * N)) ? true : false;
    }

    private void printMatrix() {
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                System.out.print(board[matrixToArrayCoords(i, j)].getWeight() + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public CellEnhanced[] getMoveOrder() {
        Arrays.sort(moveOrder);
        return moveOrder;
    }

    public static void main(String[] args) {
        BoardEnhanced b = new BoardEnhanced(3, 3, 3);

        b.printMatrix();
        b.markCell(0, 2);
        b.printMatrix();
        b.markCell(1, 2);
        b.printMatrix();

        System.out.println(Arrays.toString(b.getMoveOrder()));
        System.out.println();
        b.markCell(0, 0);

        System.out.println(Arrays.toString(b.getMoveOrder()));
    }

}
