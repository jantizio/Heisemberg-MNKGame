package mnkgame;

import java.util.Random;

public class QuasiRandomPlayer implements MNKPlayer {
	private Random rand;
	private MNKBoard B;
	private MNKGameState myWin;
	private MNKGameState yourWin;
	private int TIMEOUT;

	/**
	 * Default empty constructor
	 */
	public QuasiRandomPlayer() {
	}


	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		// New random seed for each game
		rand    = new Random(System.currentTimeMillis()); 
		B       = new MNKBoard(M,N,K);
		myWin   = first ? MNKGameState.WINP1 : MNKGameState.WINP2; 
		yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;
		TIMEOUT = timeout_in_secs;	
	}

	/**
	 * Selects a position among those listed in the <code>FC</code> array.
   * <p>
   * Selects a winning cell (if any) from <code>FC</code>, otherwise
   * selects a cell (if any) that prevents the adversary to win 
   * with his next move. If both previous cases do not apply, selects
   * a random cell in <code>FC</code>.
	 * </p>
   */
	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		long start = System.currentTimeMillis();
		if(MC.length > 0) {
			MNKCell c = MC[MC.length-1]; // Recover the last move from MC
			B.markCell(c.i,c.j);         // Save the last move in the local MNKBoard
		}
		// If there is just one possible move, return immediately
		if(FC.length == 1)
			return FC[0];
		
		// Check whether there is single move win 
		for(MNKCell d : FC) {
			// If time is running out, select a random cell
			if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(99.0/100.0)) {
				MNKCell c = FC[rand.nextInt(FC.length)];
				B.markCell(c.i,c.j);
				return c;
			} else if(B.markCell(d.i,d.j) == myWin) {
				return d;  
			} else {
				B.unmarkCell();
			}
		}
		
		// Check whether there is a single move loss:
		// 1. mark a random position
		// 2. check whether the adversary can win
		// 3. if he can win, select his winning position 
		int pos   = rand.nextInt(FC.length); 
		MNKCell c = FC[pos]; // random move
		B.markCell(c.i,c.j); // mark the random position	
		for(int k = 0; k < FC.length; k++) {
			// If time is running out, return the randomly selected  cell
      if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(99.0/100.0)) {
				return c;
			} else if(k != pos) {     
				MNKCell d = FC[k];
				if(B.markCell(d.i,d.j) == yourWin) {
					B.unmarkCell();        // undo adversary move
					B.unmarkCell();	       // undo my move	 
					B.markCell(d.i,d.j);   // select his winning position
					return d;							 // return his winning position
				} else {
					B.unmarkCell();	       // undo adversary move to try a new one
				}	
			}	
		}
		// No win or loss, return the randomly selected move
		return c;
	}

	public String playerName() {
		return "LOOOOL";
	}
}
