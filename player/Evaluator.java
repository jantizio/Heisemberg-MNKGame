package player;

import mnkgame.*;

public class Evaluator {
    private int M, N, K;
    private MNKCellState me;
    private MNKCellState opponent;
    private MNKGameState myWin;
    private MNKGameState yourWin;

    private int[][] positionWeights;
    private MNKCellState[][] openEndSequence; // FREE P1 FREE FREE K=3
    private MNKCellState[][] threatSequence; // FREE P1 P1 K=3

    private int currentMove;
    private int[][] myScores;
    private int[][] opponentScores;
    private final int[] evalWeights = { 1000000, 100, 10, 15, 20 };

    public Evaluator(int m, int n, int k, boolean first) {
        M = m;
        N = n;
        K = k;

        myScores = new int[20][5];
        opponentScores = new int[20][5];
        for (int i = 0; i < myScores.length; i++) {
            for (int j = 0; j < myScores[0].length; j++) {
                myScores[i][j] = 0;
                opponentScores[i][j] = 0;
            }
        }
        currentMove = 0;
        myWin = first ? MNKGameState.WINP1 : MNKGameState.WINP2;
        yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;
        me = first ? MNKCellState.P1 : MNKCellState.P2;
        opponent = first ? MNKCellState.P2 : MNKCellState.P1;

        createPositionWeights();
        createOpenEndSequence();
        createThreatSequence();
    }

    /**
     * It initialize the integer matrix so that every cell is the number of K
     * symbols sequence that the cell is in
     */
    private void createPositionWeights() {
        positionWeights = new int[M][N];

        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                if (inBounds(i + (K - 1), j)) {
                    for (int l = 0; l < K; l++) {
                        positionWeights[i + l][j]++;
                    }
                }

                if (inBounds(i, j + (K - 1))) {
                    for (int l = 0; l < K; l++) {
                        positionWeights[i][j + l]++;
                    }
                }

                if (inBounds(i + (K - 1), j + (K - 1))) {
                    for (int l = 0; l < K; l++) {
                        positionWeights[i + l][j + l]++;
                    }
                }

                if (inBounds(i + (K - 1), j - (K - 1))) {
                    for (int l = 0; l < K; l++) {
                        positionWeights[i + l][j - l]++;
                    }
                }
            }
        }
    }

    private void createOpenEndSequence() {
        openEndSequence = new MNKCellState[MNKCellState.FREE.ordinal() + 1][K + 1];
        for (MNKCellState i : MNKCellState.values()) {
            openEndSequence[i.ordinal()][0] = MNKCellState.FREE;
            for (int j = 1; j < K - 1; j++) {
                openEndSequence[i.ordinal()][j] = i;
            }
            openEndSequence[i.ordinal()][K - 1] = MNKCellState.FREE;
            openEndSequence[i.ordinal()][K] = MNKCellState.FREE;
        }
    }

    private void createThreatSequence() {
        threatSequence = new MNKCellState[MNKCellState.FREE.ordinal() + 1][K];
        for (MNKCellState i : MNKCellState.values()) {
            threatSequence[i.ordinal()][0] = MNKCellState.FREE;
            for (int j = 1; j < K; j++) {
                threatSequence[i.ordinal()][j] = i;
            }
        }
    }

    /**
     * tries to match the sequence given at the position [i,j]
     * 
     * @param board
     * @param i          row coordinate
     * @param j          column coordinate
     * @param sequence   sequence of simbols to match.
     * @param directionI if to scan upwards or downwards on the board
     * @param directionJ if to scan to the right or to the left on the board
     * @param revert     whether to scan the sequence forward or backwards
     * @return true if is thera a sequence, false if else.
     * @implNote cost: O(|sequence|) = O(K).
     */
    private boolean match(MNKBoard board, int i, int j, MNKCellState[] sequence, int directionI, int directionJ,
            int revert) {
        int s = revert > 0 ? 0 : sequence.length - 1; // should i start the sequence from the beginning or the end?

        if (!inBounds(i, j)
                || !inBounds(i + directionI * (sequence.length - 1), j + directionJ * (sequence.length - 1)))
            return false; // if a part of the sequence is outside the board, it's not worth checking

        for (int h = 0; h < sequence.length; h++) {
            if (board.cellState(i, j) != sequence[s])
                return false;
            i += directionI;
            j += directionJ;
            s += revert;
        }
        return true;
    }

    /**
     * is in the bounds on the board
     * 
     * @param x pos x.
     * @param y pos y.
     * @return true if is in the bound, false if else.
     * @implNote cost: O(1).
     */
    private boolean inBounds(int x, int y) {
        return ((0 <= x) && (x < M) && (0 <= y) && (y < N)) ? true : false;
    }

    /**
     * Counts how many sequence the cell [i, j] is in.
     * 
     * @param b        the board to scan
     * @param i        row coordinate
     * @param j        column coordinate
     * @param sequence the sequence to search
     * @implNote Cost: O(8K*|sequence|) ~ O(8K^2)
     */
    private int countSequence(MNKBoard b, int i, int j, MNKCellState sequence[]) {
        int count = 0;
        int k = sequence.length;
        // iteration number equal to 2K-1
        for (int h = -(k - 1); h < k; h++) {
            // forward check of the sequence
            if (match(b, i + h, j, sequence, 1, 0, 1))
                count += 1;
            if (match(b, i, j + h, sequence, 0, 1, 1))
                count += 1;
            if (match(b, i + h, j + h, sequence, 1, 1, 1))
                count += 1;
            if (match(b, i + h, j - h, sequence, 1, -1, 1))
                count += 1;

            // backwards check of the sequence
            if (match(b, i + h, j, sequence, 1, 0, -1))
                count += 1;
            if (match(b, i, j + h, sequence, 0, 1, -1))
                count += 1;
            if (match(b, i + h, j + h, sequence, 1, 1, -1))
                count += 1;
            if (match(b, i + h, j - h, sequence, 1, -1, -1))
                count += 1;
        }
        return count;
    }

    /**
     * calculate the new evaluation provided the position of the new move
     * 
     * @param b the board
     * @param i row coordinate
     * @param j column coordinate
     * @implNote O(8*8K^2) ~ O(K^2)
     */
    public void calculateIncidence(MNKBoard b, int i, int j) {
        currentMove += 1;

        // set the new sequence to score of the previous one
        for (EvalType type : EvalType.values()) {
            myScores[currentMove][type.ordinal()] = myScores[currentMove - 1][type.ordinal()];
            opponentScores[currentMove][type.ordinal()] = opponentScores[currentMove - 1][type.ordinal()];
        }

        // add the weight of the new cell
        int cellWeight = positionWeights[i][j] * evalWeights[EvalType.WEIGHT.ordinal()]; // O(1)
        if (b.cellState(i, j) == me)
            myScores[currentMove][EvalType.WEIGHT.ordinal()] += cellWeight;
        if (b.cellState(i, j) == opponent)
            opponentScores[currentMove][EvalType.WEIGHT.ordinal()] += cellWeight;

        // adding new sequence
        myScores[currentMove][EvalType.THREAT.ordinal()] += countSequence(b, i, j,
                threatSequence[me.ordinal()]) * evalWeights[EvalType.THREAT.ordinal()]; // O(8K^2)
        opponentScores[currentMove][EvalType.THREAT.ordinal()] += countSequence(b, i, j,
                threatSequence[opponent.ordinal()]) * evalWeights[EvalType.THREAT.ordinal()]; // O(8K^2)

        myScores[currentMove][EvalType.OPENEND.ordinal()] += countSequence(b, i, j,
                openEndSequence[me.ordinal()]) * evalWeights[EvalType.OPENEND.ordinal()]; // O(8K^2)
        opponentScores[currentMove][EvalType.OPENEND.ordinal()] += countSequence(b, i, j,
                openEndSequence[opponent.ordinal()]) * evalWeights[EvalType.OPENEND.ordinal()]; // O(8K^2)

        // subtracting blocked sequence
        b.unmarkCell();
        myScores[currentMove][EvalType.THREAT.ordinal()] -= countSequence(b, i, j,
                threatSequence[me.ordinal()]) * evalWeights[EvalType.THREAT.ordinal()]; // O(8K^2)
        opponentScores[currentMove][EvalType.THREAT.ordinal()] -= countSequence(b, i, j,
                threatSequence[opponent.ordinal()]) * evalWeights[EvalType.THREAT.ordinal()]; // O(8K^2)

        myScores[currentMove][EvalType.OPENEND.ordinal()] -= countSequence(b, i, j,
                openEndSequence[me.ordinal()]) * evalWeights[EvalType.OPENEND.ordinal()]; // O(8K^2)
        opponentScores[currentMove][EvalType.OPENEND.ordinal()] -= countSequence(b, i, j,
                openEndSequence[opponent.ordinal()]) * evalWeights[EvalType.OPENEND.ordinal()]; // O(8K^2)
        b.markCell(i, j);
    }

    public void undoIncidence() {
        currentMove -= 1;
    }

    // riparte dallo stato iniziale ovvero quello in posizione 0
    public void resetStore() {
        currentMove = 0;
    }

    // imposta l'ultimo stato come stato iniziale
    public void rebaseStore() {
        for (int i = 0; i < myScores[0].length; i++) {
            myScores[0][i] = myScores[currentMove][i];
            opponentScores[0][i] = opponentScores[currentMove][i];
        }

        resetStore();
    }

    public int eval(MNKBoard b) {
        int eval = 0;

        if (b.gameState() == myWin)
            eval = evalWeights[EvalType.WIN.ordinal()];
        if (b.gameState() == yourWin)
            eval = -evalWeights[EvalType.WIN.ordinal()];

        for (int i = 0; i < myScores[0].length; i++)
            eval += myScores[currentMove][i] - opponentScores[currentMove][i];

        return eval;
    }

    private void printSequence(MNKCellState[][] seq) {
        for (MNKCellState[] mnkCellStates : seq) {
            for (MNKCellState s : mnkCellStates) {
                System.out.print(s + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Evaluator e = new Evaluator(7, 7, 3, true);
        // e.printSequence(e.openEndSequence);
        // System.out.println();
        // e.printSequence(e.sevenTrapSequence);
        // System.out.println();
        // e.printSequence(e.threatSequence);
        MNKBoard prova = new MNKBoard(7, 7, 3);
        prova.markCell(1, 3);
        e.calculateIncidence(prova, 1, 3);

        prova.markCell(4, 1);
        e.calculateIncidence(prova, 4, 1);

        prova.markCell(1, 4);
        e.calculateIncidence(prova, 1, 4);

        prova.markCell(5, 2);
        e.calculateIncidence(prova, 5, 2);

        prova.markCell(4, 6);
        e.calculateIncidence(prova, 4, 6);

        e.undoIncidence();

        for (int i = 0; i < e.myScores[0].length; i++) {
            System.out.print(EvalType.values()[i] + ": " + e.myScores[e.currentMove][i] + " - "
                    + e.opponentScores[e.currentMove][i] + "\t");
        }
        System.out.println();
        int eval = 0;
        for (int i = 0; i < e.myScores[0].length; i++) {
            eval += e.myScores[e.currentMove][i] - e.opponentScores[e.currentMove][i];
        }
        System.out.println(eval);

        // for (int i = 0; i < e.myScores.length; i++) {
        // for (int j = 0; j < e.myScores[0].length; j++) {
        // System.out.print(e.myScores[i][j] + "-" + e.opponentScores[i][j] + "\t");
        // }
        // System.out.println();
        // }
    }

}
