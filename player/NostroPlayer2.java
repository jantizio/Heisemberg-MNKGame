package player;

import mnkgame.*;
import java.util.Random;

public class NostroPlayer2 implements MNKPlayer {
	private Random rand;
	private BoardEnhanced BE;
	private int TIMEOUT;
	private int M, N, K;
	
	private Evaluator evaluator;

	private MNKCell bestMove;
	private MNKCell globalBestMove;
	private long timerStart;
	private final int INITIAL_DEPTH = 2;
	private boolean timedOut; // is true if the search got interrupted beacause of timeout
	private boolean isTreeCompleted; // is true if the search completed the tree
	private int currentDepth;


	/**
	 * Default empty constructor
	 */
	public NostroPlayer2() {
	}

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		// New random seed for each game
		rand = new Random(System.currentTimeMillis());
		BE = new BoardEnhanced(M, N, K);
		evaluator = new Evaluator(M, N, K, first);
		TIMEOUT = timeout_in_secs;
		this.M = M;
		this.N = N;
		this.K = K;
		isTreeCompleted = true;
	}

	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		timerStart = System.currentTimeMillis();
		timedOut = false;
		isTreeCompleted = true;

		if (MC.length > 0) {
			MNKCell c = MC[MC.length - 1]; // Recover the last move from MC
			BE.markCell(c.i, c.j); // Save the last move in the local MNKBoard
			evaluator.calculateIncidence(BE.mnkboard, c.i, c.j);
		}
		// If there is just one possible move, return immediately
		if (FC.length == 1)
			return FC[0];

		evaluator.rebaseStore();
		
		bestMove = globalBestMove = FC[rand.nextInt(FC.length)]; // select random move

		// iterative deepening search
		for (int depth = 0;; depth++) {
			currentDepth = INITIAL_DEPTH + depth;
			
			int searchResult = alphabeta(BE, currentDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
			evaluator.resetStore();

			// if the time is over stop the loop and the best move is the previous one
			if (timedOut) 
				break;
			

			globalBestMove = bestMove; // update the best move
			
			// if the tree is completed the search is over for this move
			// if the score is higher than the value of the win,
			// i found a winning move i can stop the search
			if (isTreeCompleted || searchResult >= evaluator.evalWeights[0] / 2) 
				break;
			
			isTreeCompleted = true; // if we are here then the flag was false,
			// need to set to true for the next loop
		}

		BE.markCell(globalBestMove.i, globalBestMove.j);
		evaluator.calculateIncidence(BE.mnkboard, globalBestMove.i, globalBestMove.j);
		return globalBestMove;
	}

	public String playerName() {
		return "Heisenberg-sorting";
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
	private int alphabeta(BoardEnhanced b, int depth, int alpha, int beta, boolean isMaximazing) {
		MNKGameState result = b.mnkboard.gameState();
		int bestScore;
		// if we are in terminal state or the depth is reached stop the recursion and
		// evaluate the score of the current board
		if (result != MNKGameState.OPEN || depth == 0) {
			if (depth <= 0)
				isTreeCompleted = false;
			return evaluator.eval(b.mnkboard);
		}

		CellEnhanced moves[] = b.getMoveOrder();
		if (isMaximazing) {
			bestScore = Integer.MIN_VALUE;
			for (CellEnhanced c : moves) {
				if (c.state != MNKCellState.FREE)
					continue;
				if ((System.currentTimeMillis() - timerStart) / 1000.0 > TIMEOUT * (98.0 / 100.0)) {
					timedOut = true;
					return bestScore;
				}

				b.markCell(c.i, c.j);
				evaluator.calculateIncidence(b.mnkboard, c.i, c.j);

				int score = alphabeta(b, depth - 1, alpha, beta, false);
				
				b.unmarkCell(c.i, c.j);
				evaluator.undoIncidence();

				if (score > bestScore) {
					bestScore = score;
					if (depth == currentDepth)
						bestMove = new MNKCell(c.i, c.j);
				}
				alpha = Math.max(alpha, bestScore);
				if (alpha >= beta)
					break;

			}
		} else {
			bestScore = Integer.MAX_VALUE;
			for (CellEnhanced c : moves) {
				if (c.state != MNKCellState.FREE)
					continue;
				if ((System.currentTimeMillis() - timerStart) / 1000.0 > TIMEOUT * (99.0 / 100.0)) {
					timedOut = true;
					return bestScore;
				}
				b.markCell(c.i, c.j);
				evaluator.calculateIncidence(b.mnkboard, c.i, c.j);
				
				int score = alphabeta(b, depth - 1, alpha, beta, true);
				
				b.unmarkCell(c.i, c.j);
				evaluator.undoIncidence();

				if (score < bestScore) {
					bestScore = score;
					if (depth == currentDepth)
						bestMove = new MNKCell(c.i, c.j);
				}
				beta = Math.min(beta, bestScore);
				if (alpha >= beta)
					break;
			}
		}
		return bestScore;
	}
}