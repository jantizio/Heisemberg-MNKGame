package player;


import mnkgame.MNKBoard;
import mnkgame.MNKCell;
import mnkgame.MNKCellState;

public class BoardEnhanced
{
    private DoublyLinkedNode nodesBoard[][];
    private int M, N, K;
    public MNKBoard mnkboard;
    private DoublyLinkedList weightArray[];

    public BoardEnhanced(int m, int n, int k, MNKBoard b) {
        this(m, n, k);
        this.mnkboard = b;
    }

    public BoardEnhanced(int M, int N, int K) {
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
                        if(this.nodesBoard[i1][j1].getItem().state==MNKCellState.FREE) this.weightArray[this.nodesBoard[i1][j1].getItem().getWeight()].addNode(this.nodesBoard[i1][j1]);
                    }
                }
            }
        }
    }

    public void unmarkCell(int i, int j) {
        mnkboard.unmarkCell();
        DoublyLinkedNode node=nodesBoard[i][j];
        this.weightArray[node.getItem().getWeight()].addNode(node);
        node.getItem().state = mnkboard.cellState(i, j);
        checkStar(i, j, -1);
    }

    public void markCell(int i, int j) {
        mnkboard.markCell(i, j);
        DoublyLinkedNode node=nodesBoard[i][j];
        DoublyLinkedList.delNode(node);
        node.getItem().state = mnkboard.cellState(i, j);
        checkStar(i, j, 1);
    }

    private boolean inBounds(int x, int y) {
        return ((0 <= x) && (x < M) && (0 <= y) && (y < N));
    }

    public CellEnhanced[] getMoveOrder() {
        CellEnhanced temp[]=new CellEnhanced[M*N-this.mnkboard.getMarkedCells().length];
        int c=0;
        for (int i = this.weightArray.length-1; i >= 0 ; i--) {
            if(this.weightArray[i].isEmpty()) continue;
            DoublyLinkedNode nodo=this.weightArray[i].getNextIteration(true);
            do
            {
                temp[c++]=nodo.getItem();
                nodo=this.weightArray[i].getNextIteration(false);
            } while(nodo!=null && nodo.getItem()!=null);
        }
        return temp;
    }

}
