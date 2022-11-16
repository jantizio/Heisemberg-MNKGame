package mnkgame;

import java.util.Random;

import javax.sound.midi.Sequence;

public class MinimaxPlayer implements MNKPlayer {
    private Random rand;
    private MNKBoard B;
    private MNKGameState myWin;
    private MNKGameState yourWin;
    private int TIMEOUT;

    private int pesi[];
    private int MIN, MAX;
    private int gameStateCounter, numMosse;
    private int SEARCH_DEPTH;

    /**
     * Default empty constructor
     */
    public MinimaxPlayer() {
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

        MIN = Integer.MIN_VALUE;
        MAX = Integer.MAX_VALUE;

        gameStateCounter = 0;
        numMosse = 0;

        SEARCH_DEPTH = 5;

    }

    public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
        long start = System.currentTimeMillis();

        gameStateCounter = 0;
        numMosse += 1;

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

        debugMessage(false);
        B.markCell(move.i, move.j);
        return move;
    }

    public String playerName() {
        return "minimax"; // TODO: scegliere un nome
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
                bestScore = Math.max(score, bestScore);

            }
        } else {
            bestScore = MAX;
            for (MNKCell c : FC) {
                if ((System.currentTimeMillis() - start) / 1000.0 > TIMEOUT * (99.0 / 100.0))
                    return bestScore;
                B.markCell(c.i, c.j);
                int score = minimax(B, depth + 1, true, start);
                B.unmarkCell();
                bestScore = Math.min(score, bestScore);
            }
        }
        return bestScore;
    }

    private void debugMessage(boolean timeout) {
        if (timeout)
            System.out.print("time ended, ");
        System.out.println(
                "(" + playerName() + ")Stati di gioco valutati alla mossa " + numMosse + ": " + gameStateCounter);
    }

    private double evaluate()   /* TODO: implementare ognuno il proprio evaluate e richamare questo nel metodo minmax, per avere una stessa interfaccia da richiamare */
    {
        return 0.;
    }

    /**
     * match, cost: O(|sequence|).
     * @param board board.
     * @param x pos x.
     * @param y pos y.
     * @param sequence sequence of simbols to find.
     * @param directionX direction x.
     * @param directionY direction y.
     * @param revert sequence revertion.
     * @return true if is thera a sequence, false if else.
     */
    private boolean match(MNKBoard board, int x, int y, MNKCellState[] sequence, int directionX, int directionY, int revert)
    {
        int s= revert>0 ? 0 : sequence.length-1;
        
        if(this.inBounds(board, x+directionX*(sequence.length-1), y+directionY*(sequence.length-1)))
        {
            for(int i=0;i <sequence.length;i++)
            {
                if(board.cellState(x,y) != sequence[s]) return false;
                x+=directionX;
                y+=directionY;
                s+=revert;
            }
            return true;
        }
        return false;
    }

    /**
     * match position FIXED(ideas in meating 15/11/22), cost: O(4|sequence|) ~ O(|sequence|).
     * @param board board.
     * @param x pos x.
     * @param y pos y.
     * @param sequence sequence of simbols to find.
     * @param direction sequence direction.
     * @return number of sequence starting from the specified position.
     */
    private int matchPosition(MNKBoard board, int x, int y, MNKCellState[] sequence, int direction)
    {
        int starting=0;
        if(match(board,x,y,sequence,1,0,direction)) starting++;
        if(match(board,x,y,sequence,0,1,direction)) starting++;
        if(match(board,x,y,sequence,1,1,direction)) starting++;
        if(match(board,x,y,sequence,1,-1,direction)) starting++;
        return starting;
    }

    /**
     * is in the bounds on the board, cost: O(1).
     * @param board board.
     * @param x pos x.
     * @param y pos y.
     * @return true if is in the bound, false if else.
     */
    private boolean inBounds(MNKBoard board,int x, int y) {
		return (0 <= x) && (x < board.N) && (0 <= y) && (y < board.M);
	}
}
