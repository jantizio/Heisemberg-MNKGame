package player;

import java.util.Arrays;

import mnkgame.MNKBoard;
import mnkgame.MNKCell;

public class BoardEnhanced2
{
    private DoublyLinkedNode nodesBoard[][];
    private int M, N, K;
    public MNKBoard mnkboard;
    private DoublyLinkedList weightArray[];

    public BoardEnhanced2(int m, int n, int k, MNKBoard b) {
        this(m, n, k);
        this.mnkboard = b;
    }

    public BoardEnhanced2(int M, int N, int K) {
        this.M = M;
        this.N = N;
        this.K = K;
        this.mnkboard = new MNKBoard(M, N, K);
        this.nodesBoard = new DoublyLinkedNode[M][N];
        int max_dim=4*this.K*(this.K+1);
        this.weightArray=new DoublyLinkedList[max_dim];
        for (int i = 0; i < max_dim; i++) this.weightArray[i]=new DoublyLinkedList();
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                this.nodesBoard[i][j] = new DoublyLinkedNode(new CellEnhanced(new MNKCell(i, j)));
                this.weightArray[0].addNode(this.nodesBoard[i][j]);
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
                    {
                        int weight=d * (K - k + 1);
                        DoublyLinkedList.delNode(nodesBoard[i1][j1]);
                        this.nodesBoard[i1][j1].getItem().addWeight(weight);
                        this.weightArray[this.nodesBoard[i1][j1].getItem().getWeight()].addNode(this.nodesBoard[i1][j1]);
                    }
                }
            }
        }
    }

    public void unmarkCell(int i, int j) {
        mnkboard.unmarkCell();
        nodesBoard[i][j].getItem().state = mnkboard.cellState(i, j);
        checkStar(i, j, -1);
    }

    public void markCell(int i, int j) {
        mnkboard.markCell(i, j);
        nodesBoard[i][j].getItem().state = mnkboard.cellState(i, j);
        checkStar(i, j, 1);
    }

    private boolean inBounds(int x, int y) {
        return ((0 <= x) && (x < M) && (0 <= y) && (y < N)) ? true : false;
    }

    private boolean inBounds(int p) {
        return ((0 <= p) && (p < M * N)) ? true : false;
    }

    public void printMatrix() {
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) System.out.printf("%-4s : %-3d", nodesBoard[i][j].getItem().state, nodesBoard[i][j].getItem().getWeight());
            System.out.println();
        }
    }

    public CellEnhanced[] getMoveOrder() {
        CellEnhanced temp[]=new CellEnhanced[M*N];
        int c=M*N-1;
        for (int i = 0; i < this.weightArray.length; i++) {
            if(this.weightArray[i].isEmpty()) continue;
            DoublyLinkedNode nodo=this.weightArray[i].getNextIteration(true);
            do
            {
                temp[c]=nodo.getItem();
                c--;
                nodo=this.weightArray[i].getNextIteration(false);
            } while(nodo!=null && nodo.getItem()!=null);
        }
        return temp;
    }

    public static void main(String[] args) {
        BoardEnhanced2 b = new BoardEnhanced2(3, 3, 3);

        b.printMatrix();
        b.markCell(0, 2);
        b.printMatrix();
        b.markCell(2, 2);
        b.printMatrix();

        System.out.println(Arrays.toString(b.getMoveOrder()));
        System.out.println();
        b.markCell(0, 0);

        for (int i = 0; i < b.M; i++) {
            for (int j = 0; j < b.N; j++) {
                System.out.print(b.mnkboard.cellState(i, j) + " ");
            }
            System.out.println();
        }

        System.out.println(Arrays.toString(b.getMoveOrder()));

        b.unmarkCell(0, 0);
        b.printMatrix();
        System.out.println(Arrays.toString(b.getMoveOrder()));

        b.unmarkCell(2, 2);
        b.unmarkCell(0, 2);
        b.printMatrix();
        System.out.println(Arrays.toString(b.getMoveOrder()));
        for (int i = 0; i < b.M; i++) {
            for (int j = 0; j < b.N; j++) {
                System.out.print(b.mnkboard.cellState(i, j) + " ");
            }
            System.out.println();
        }
    }

}
