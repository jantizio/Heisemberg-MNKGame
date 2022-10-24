package mnkgame;

import java.util.Random;

public class NostroPlayer implements MNKPlayer {
    private Random rand;
    private MNKBoard B;
    private MNKGameState myWin;
    private MNKGameState yourWin;
    private int TIMEOUT;

    private int pesi[];
    private int MIN, MAX;
    private int gameStateCounter;

    /**
     * Default empty constructor
     */
    public NostroPlayer() {
    }

    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        // New random seed for each game
        rand = new Random(System.currentTimeMillis());
        B = new MNKBoard(M, N, K);
        myWin = first ? MNKGameState.WINP1 : MNKGameState.WINP2;
        yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;
        TIMEOUT = timeout_in_secs;

        pesi = new int[MNKGameState.WINP2.ordinal() + 1];
        pesi[myWin.ordinal()] = 1;
        pesi[MNKGameState.DRAW.ordinal()] = 0;
        pesi[yourWin.ordinal()] = -1;

        MIN = -100000000;
        MAX = 100000000;

        gameStateCounter = 0;

    }

    public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
        long start = System.currentTimeMillis();

        if (MC.length > 0) {
            MNKCell c = MC[MC.length - 1]; // Recover the last move from MC
            B.markCell(c.i, c.j); // Save the last move in the local MNKBoard
        }
        // If there is just one possible move, return immediately
        if (FC.length == 1)
            return FC[0];

        int bestScore = MIN;
        MNKCell move = FC[rand.nextInt(FC.length)];

        for (MNKCell d : FC) {
            gameStateCounter += 1;
            if ((System.currentTimeMillis() - start) / 1000.0 > TIMEOUT * (99.0 / 100.0)) {
                B.markCell(move.i, move.j);
                return move;
            }
            B.markCell(d.i, d.j);
            int score = minimax(B, 0, false, start);
            B.unmarkCell();
            if (score > bestScore) {
                bestScore = score;
                move = d;
            }
        }

        // System.out.println("Stati di gioco valutati: " + gameStateCounter);
        B.markCell(move.i, move.j);
        return move;
    }

    public String playerName() {
        return "TicTacToe PRO"; // TODO: scegliere un nome
    }

    private int minimax(MNKBoard b, int depth, boolean isMaximazing, long start) {
        gameStateCounter += 1;
        // if we are in a terminal state, evaluate the score
        MNKGameState result = b.gameState();
        MNKCell FC[] = b.getFreeCells();
        int bestScore;
        if (result != MNKGameState.OPEN) {
            return pesi[result.ordinal()] * (1 + FC.length);
            // TODO: forse è più efficiente fare (M*N-depth) al posto di FC.lenght?
            // verificare se sono uguali in primo luogo
        }

        if (isMaximazing) {
            bestScore = MIN;
            for (MNKCell c : FC) {
                if ((System.currentTimeMillis() - start) / 1000.0 > TIMEOUT * (99.0 / 100.0))
                    return bestScore;

                B.markCell(c.i, c.j);
                int score = minimax(B, depth + 1, false, start);
                B.unmarkCell();
                if (score > bestScore) // max
                    bestScore = score;
            }
        } else {
            bestScore = MAX;
            for (MNKCell c : FC) {
                if ((System.currentTimeMillis() - start) / 1000.0 > TIMEOUT * (99.0 / 100.0))
                    return bestScore;
                B.markCell(c.i, c.j);
                int score = minimax(B, depth + 1, true, start);
                B.unmarkCell();
                if (score < bestScore) // min
                    bestScore = score;
            }
        }
        return bestScore;
    }

}
