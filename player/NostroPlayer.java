package player;

import mnkgame.*;
import java.util.Random;

import javax.lang.model.util.ElementScanner6;

public class NostroPlayer implements MNKPlayer {
	private Random rand;
	private MNKBoard B;
	private MNKGameState myWin;
	private MNKGameState yourWin;
	private int TIMEOUT;
	private int M, N, K;

	private int pesi[];
	private MNKCell bestMove;
	private MNKCell globalBestMove;
	private long timerStart;
	private final int INITIAL_DEPTH = 2;
	private boolean timedOut; // is true if the search got interrupted beacause of timeout
	private boolean isTreeCompleted; // is true if the search completed the tree
	private int currentDepth;

	private Evaluator evaluator;

	private int gameStateCounter, numMosse; // debug variables

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
		this.M = M;
		this.N = N;
		this.K = K;
		isTreeCompleted = true;

		pesi = new int[MNKGameState.WINP2.ordinal() + 1];
		pesi[myWin.ordinal()] = 1;
		pesi[MNKGameState.DRAW.ordinal()] = 0;
		pesi[yourWin.ordinal()] = -1;

		evaluator = new Evaluator(M, N, K, first);

		// debug variables
		gameStateCounter = 0;
		numMosse = 0;
	}

	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		timerStart = System.currentTimeMillis();
		timedOut = false;
		isTreeCompleted = true;

		gameStateCounter = 0;
		numMosse += 1;

		if (MC.length > 0) {
			MNKCell c = MC[MC.length - 1]; // Recover the last move from MC
			B.markCell(c.i, c.j); // Save the last move in the local MNKBoard
			evaluator.calculateIncidence(B, c.i, c.j);
		}
		// If there is just one possible move, return immediately
		if (FC.length == 1)
			return FC[0];

		evaluator.rebaseStore();

		bestMove = globalBestMove = FC[rand.nextInt(FC.length)]; // select random move

		// iterative deepening search
		for (int depth = 0;; depth++) {
			currentDepth = INITIAL_DEPTH + depth;
			int searchResult = alphabeta(B, currentDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
			evaluator.resetStore();
			// if the time is over stop the loop and the best move is the previous one
			if (timedOut)
				break;

			globalBestMove = bestMove; // update the best move
			// System.out.println("Completed search with depth " + currentDepth + ". Best
			// move so far: " + globalBestMove);
			// if the tree is completed the search is over for this move
			// if the score is higher than the value of the win,
			// i found a winning move i can stop the search
			if (isTreeCompleted || searchResult >= evaluator.evalWeights[0] / 2)
				break;
			isTreeCompleted = true; // if we are here then the flag was false,
									// need to set to true for the next loop
		}

		// System.out.println();
		B.markCell(globalBestMove.i, globalBestMove.j);
		evaluator.calculateIncidence(B, globalBestMove.i, globalBestMove.j);
		return globalBestMove;
	}

	public MNKCell bigSelect(MNKCell[] FC, MNKCell[] MC){
		MNKCell lastMove = MC[MC.length - 1];
		int totalPos = 8;
		int matrAroundPos[][] = {
			{-1, -1},
			{0, -1},
			{1, -1},
			{-1, 0},
			{1, 0},
			{-1, 1},
			{0, 1},
			{1, 1}
		};
		int matrDirectionAround[][] = {
			{1, 1},
			{0, 1},
			{-1, 1},
			{1, 0},
			{-1, 0},
			{1, -1},
			{0, -1},
			{-1, -1}
		};
		//case first: take one of the central cells
		if(MC.length == 0)
			return new MNKCell(Math.ceilDiv(M, 2)-1, Math.ceilDiv(N, 2)-1);
		//case second: take one of the central cells or near to the center if, the center one is taken
		if(MC.length == 1)
		{
			int temp_m=Math.ceilDiv(M, 2), temp_n=Math.ceilDiv(N, 2);
			if(B.cellState(temp_m-1,temp_n-1)==MNKCellState.FREE) return new MNKCell(temp_m-1, temp_n-1);
			return new MNKCell(temp_m, temp_n);
		}
		//middle game cases
		else{
			//case near to lost: FREE [k-2 opponent] FREE
			MNKCellState opponentSequence[] = new MNKCellState[K];
			opponentSequence[0] = opponentSequence[K-1] = MNKCellState.FREE;
			for (int i = 1; i < opponentSequence.length - 1; i++)
				opponentSequence[i] = opponent;
			for (int i = 0; i < totalPos; i++) {
				if(match(B, lastMove.j + matrAroundPos[i][0], lastMove.i + matrAroundPos[i][1], opponentSequence, matrDirectionAround[i][0], matrDirectionAround[i][1], 1))
					return new MNKCell(lastMove.j + matrAroundPos[i][0], lastMove.i + matrAroundPos[i][1]); 
			}
		}

		return MC[0];
	}

	public String playerName() {
		return "TicTacToe PRO"; // TODO: scegliere un nome
	}

	/**
	 * Performs standard alphabeta pruning algorithm
	 * 
	 * @param b            current game board
	 * @param depth        depth of the tree you want to reach
	 * @param alpha        best possible score for the maximazing player
	 * @param beta         best possible score for the minimizer player
	 * @param isMaximazing whether the current player is the min or max player
	 * @return the score of the given board
	 */
	private int alphabeta(MNKBoard b, int depth, int alpha, int beta, boolean isMaximazing) {
		gameStateCounter += 1;
		MNKGameState result = b.gameState();
		MNKCell FC[] = b.getFreeCells();
		int bestScore;
		// if we are in terminal state or the depth is reached stop the recursion and
		// evaluate the score of the current board
		if (result != MNKGameState.OPEN || depth == 0) {
			if (depth <= 0)
				isTreeCompleted = false;
			return evaluator.eval(b);
		}

		if (isMaximazing) {
			bestScore = Integer.MIN_VALUE;
			for (MNKCell c : FC) {
				if ((System.currentTimeMillis() - timerStart) / 1000.0 > TIMEOUT * (98.0 / 100.0)) {
					timedOut = true;
					return bestScore;
				}

				b.markCell(c.i, c.j);
				evaluator.calculateIncidence(b, c.i, c.j);
				int score = alphabeta(b, depth - 1, alpha, beta, false);
				b.unmarkCell();
				evaluator.undoIncidence();

				if (score > bestScore) {
					bestScore = score;
					if (depth == currentDepth)
						bestMove = c;
				}
				alpha = Math.max(alpha, bestScore);
				if (alpha >= beta)
					break;

			}
		} else {
			bestScore = Integer.MAX_VALUE;
			for (MNKCell c : FC) {
				if ((System.currentTimeMillis() - timerStart) / 1000.0 > TIMEOUT * (99.0 / 100.0)) {
					timedOut = true;
					return bestScore;
				}
				b.markCell(c.i, c.j);
				evaluator.calculateIncidence(b, c.i, c.j);
				int score = alphabeta(b, depth - 1, alpha, beta, true);
				b.unmarkCell();
				evaluator.undoIncidence();

				if (score < bestScore) {
					bestScore = score;
					if (depth == currentDepth)
						bestMove = c;
				}
				beta = Math.min(beta, bestScore);
				if (beta <= alpha)
					break;
			}
		}
		return bestScore;

	}

	private void printBoard(MNKBoard b) {
		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				switch (b.cellState(i, j)) {
					case P1:
						System.out.print("X ");
						break;
					case P2:
						System.out.print("O ");
						break;
					case FREE:
						System.out.print("# ");
						break;
					default:
						break;
				}
			}
			System.out.println();
		}
		System.out.println();
	}


	private void debugMessage(boolean timeout) {
		if (timeout)
			System.out.print("time ended, ");
		System.out.println(
				"(" + playerName() + ")Stati di gioco valutati alla mossa " + numMosse + ": " + gameStateCounter);
	}
}