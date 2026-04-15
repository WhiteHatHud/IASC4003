import java.util.*;

public class ThreePrisonersDilemma {
	
	/* 
	 This Java program models the two-player Prisoner's Dilemma game.
	 We use the integer "0" to represent cooperation, and "1" to represent 
	 defection. 
	 
	 Recall that in the 2-players dilemma, U(DC) > U(CC) > U(DD) > U(CD), where
	 we give the payoff for the first player in the list. We want the three-player game 
	 to resemble the 2-player game whenever one player's response is fixed, and we
	 also want symmetry, so U(CCD) = U(CDC) etc. This gives the unique ordering
	 
	 U(DCC) > U(CCC) > U(DDC) > U(CDC) > U(DDD) > U(CDD)
	 
	 The payoffs for player 1 are given by the following matrix: */
	
	static int[][][] payoff = {  
		{{6,3},  //payoffs when first and second players cooperate 
		 {3,0}}, //payoffs when first player coops, second defects
		{{8,5},  //payoffs when first player defects, second coops
	     {5,2}}};//payoffs when first and second players defect
	
	/* 
	 So payoff[i][j][k] represents the payoff to player 1 when the first
	 player's action is i, the second player's action is j, and the
	 third player's action is k.
	 
	 In this simulation, triples of players will play each other repeatedly in a
	 'match'. A match consists of about 100 rounds, and your score from that match
	 is the average of the payoffs from each round of that match. For each round, your
	 strategy is given a list of the previous plays (so you can remember what your 
	 opponent did) and must compute the next action.  */
	
	
	abstract class Player {
		// This procedure takes in the number of rounds elapsed so far (n), and 
		// the previous plays in the match, and returns the appropriate action.
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			throw new RuntimeException("You need to override the selectAction method.");
		}
		
		// Used to extract the name of this player class.
		final String name() {
			String result = getClass().getName();
			return result.substring(result.indexOf('$')+1);
		}
	}
	
	/* Here are four simple strategies: */
	
	class NicePlayer extends Player {
		//NicePlayer always cooperates
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			return 0; 
		}
	}
	
	class NastyPlayer extends Player {
		//NastyPlayer always defects
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			return 1; 
		}
	}
	
	class RandomPlayer extends Player {
		//RandomPlayer randomly picks his action each time
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (Math.random() < 0.5)
				return 0;  //cooperates half the time
			else
				return 1;  //defects half the time
		}
	}
	
	class TolerantPlayer extends Player {
		//TolerantPlayer looks at his opponents' histories, and only defects
		//if at least half of the other players' actions have been defects
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			int opponentCoop = 0;
			int opponentDefect = 0;
			for (int i=0; i<n; i++) {
				if (oppHistory1[i] == 0)
					opponentCoop = opponentCoop + 1;
				else
					opponentDefect = opponentDefect + 1;
			}
			for (int i=0; i<n; i++) {
				if (oppHistory2[i] == 0)
					opponentCoop = opponentCoop + 1;
				else
					opponentDefect = opponentDefect + 1;
			}
			if (opponentDefect > opponentCoop)
				return 1;
			else
				return 0;
		}
	}
	
	class FreakyPlayer extends Player {
		//FreakyPlayer determines, at the start of the match, 
		//either to always be nice or always be nasty. 
		//Note that this class has a non-trivial constructor.
		int action;
		FreakyPlayer() {
			if (Math.random() < 0.5)
				action = 0;  //cooperates half the time
			else
				action = 1;  //defects half the time
		}
		
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			return action;
		}	
	}

	class T4TPlayer extends Player {
		//Picks a random opponent at each play,
		//and uses the 'tit-for-tat' strategy against them
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n==0) return 0; //cooperate by default
			if (Math.random() < 0.5)
				return oppHistory1[n-1];
			else
				return oppHistory2[n-1];
		}
	}

	class BinMuhammadTaufiq_Hudzaifah_Player extends Player {

		int[][][] payoff = {{{6,3},{3,0}},{{8,5},{5,2}}};
		int r; int[] myHist, opp1Hist, opp2Hist;
		int myScore=0, opp1Score=0, opp2Score=0;
		int opp1Coop=0, opp2Coop=0;
		final double LENIENT_THRESHOLD = 0.705;
		final double STRICT_THRESHOLD = 0.500;

		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n==0) return 0;
			this.r=n-1; this.myHist=myHistory; this.opp1Hist=oppHistory1; this.opp2Hist=oppHistory2;
			int myLA=myHistory[r], opp1LA=oppHistory1[r], opp2LA=oppHistory2[r];
			this.myScore+=payoff[myLA][opp1LA][opp2LA];
			this.opp1Score+=payoff[opp1LA][opp2LA][myLA];
			this.opp2Score+=payoff[opp2LA][opp1LA][myLA];
			opp1Coop+=getOppAction(opp1Hist[r]); opp2Coop+=getOppAction(opp2Hist[r]);
			double opp1CoopProb=(double)opp1Coop/opp1Hist.length;
			double opp2CoopProb=(double)opp2Coop/opp2Hist.length;
			if (n>5&&(opp1Coop==0||opp2Coop==0)) return 1;
			if ((n>5)&&(opp1CoopProb<STRICT_THRESHOLD)&&(opp2CoopProb<STRICT_THRESHOLD)) return applyNoise(1,99);
			if ((opp1LA+opp2LA==0)&&(opp1CoopProb>LENIENT_THRESHOLD)&&(opp2CoopProb>LENIENT_THRESHOLD)) return applyNoise(0,99);
			if (myScore>=opp1Score&&myScore>=opp2Score) return 0;
			return 1;
		}
		private int applyNoise(int intended, int pct) {
			Map<Integer,Integer> map=new HashMap<Integer,Integer>(){{put(intended,pct);put(getOppAction(intended),1-pct);}};
			LinkedList<Integer> list=new LinkedList<>();
			for (Map.Entry<Integer,Integer> e:map.entrySet()) for (int i=0;i<e.getValue();i++) list.add(e.getKey());
			Collections.shuffle(list); return list.pop();
		}
		private int getOppAction(int a){return a==1?0:1;}
	}

	class WinStayLoseShift extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n==0) return 0;
			int r = n-1;
			int myLA = myHistory[r], o1 = oppHistory1[r], o2 = oppHistory2[r];
			if (payoff[myLA][o1][o2] >= 5) return myLA;
			return myLA == 1 ? 0 : 1;
		}
	}

	class SoreLoser extends Player {
		int myScore=0, opp1Score=0, opp2Score=0;
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n==0) return 0;
			int i = n-1;
			myScore   += payoff[myHistory[i]][oppHistory1[i]][oppHistory2[i]];
			opp1Score += payoff[oppHistory1[i]][oppHistory2[i]][myHistory[i]];
			opp2Score += payoff[oppHistory2[i]][myHistory[i]][oppHistory1[i]];
			if (myScore >= opp1Score && myScore >= opp2Score) return 0;
			return 1;
		}
	}

	class Trigger extends Player {
		boolean triggered = false;
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n==0) return 0;
			if (oppHistory1[n-1] + oppHistory2[n-1] == 2) triggered = true;
			return triggered ? 1 : 0;
		}
	}

	
	/* In our tournament, each pair of strategies will play one match against each other. 
	 This procedure simulates a single match and returns the scores. */
	float[] scoresOfMatch(Player A, Player B, Player C, int rounds) {
		int[] HistoryA = new int[0], HistoryB = new int[0], HistoryC = new int[0];
		float ScoreA = 0, ScoreB = 0, ScoreC = 0;
		
		for (int i=0; i<rounds; i++) {
			int PlayA = A.selectAction(i, HistoryA, HistoryB, HistoryC);
			int PlayB = B.selectAction(i, HistoryB, HistoryC, HistoryA);
			int PlayC = C.selectAction(i, HistoryC, HistoryA, HistoryB);
			ScoreA = ScoreA + payoff[PlayA][PlayB][PlayC];
			ScoreB = ScoreB + payoff[PlayB][PlayC][PlayA];
			ScoreC = ScoreC + payoff[PlayC][PlayA][PlayB];
			HistoryA = extendIntArray(HistoryA, PlayA);
			HistoryB = extendIntArray(HistoryB, PlayB);
			HistoryC = extendIntArray(HistoryC, PlayC);
		}
		float[] result = {ScoreA/rounds, ScoreB/rounds, ScoreC/rounds};
		return result;
	}
	
//	This is a helper function needed by scoresOfMatch.
	int[] extendIntArray(int[] arr, int next) {
		int[] result = new int[arr.length+1];
		for (int i=0; i<arr.length; i++) {
			result[i] = arr[i];
		}
		result[result.length-1] = next;
		return result;
	}
	
	/* The procedure makePlayer is used to reset each of the Players 
	 (strategies) in between matches. When you add your own strategy,
	 you will need to add a new entry to makePlayer, and change numPlayers.*/
	
	int numPlayers = 10;
	Player makePlayer(int which) {
		switch (which) {
		case 0: return new NicePlayer();
		case 1: return new NastyPlayer();
		case 2: return new RandomPlayer();
		case 3: return new TolerantPlayer();
		case 4: return new FreakyPlayer();
		case 5: return new T4TPlayer();
		case 6: return new BinMuhammadTaufiq_Hudzaifah_Player();
		case 7: return new WinStayLoseShift();
		case 8: return new SoreLoser();
		case 9: return new Trigger();
		}
		throw new RuntimeException("Bad argument passed to makePlayer");
	}
	
	/* Finally, the remaining code actually runs the tournament. */
	
	public static void main (String[] args) {
		int TOURNAMENT_RUNS = 10000;
		ThreePrisonersDilemma instance = new ThreePrisonersDilemma();

		String border = "================================================================";
		String thin   = "----------------------------------------------------------------";

		System.out.println("\n" + border);
		System.out.println("   THREE PRISONERS DILEMMA — TOURNAMENT SIMULATION");
		System.out.println("   Players : " + instance.numPlayers);
		System.out.println("   Runs    : " + TOURNAMENT_RUNS);
		System.out.println("   Rounds  : 90–110 per match (randomised)");
		System.out.println(border);

		int[] rankSum = new int[instance.numPlayers];
		for (int run = 0; run < TOURNAMENT_RUNS; run++) {
			if (run % 1000 == 0 && run > 0)
				System.out.printf("   ... completed %d / %d tournaments%n", run, TOURNAMENT_RUNS);
			int[] sortedOrder = instance.runTournament();
			for (int rank = 0; rank < instance.numPlayers; rank++)
				rankSum[sortedOrder[rank]] += (rank + 1);
		}
		System.out.printf("   ... completed %d / %d tournaments%n", TOURNAMENT_RUNS, TOURNAMENT_RUNS);

		double[] avgRank = new double[instance.numPlayers];
		for (int i = 0; i < instance.numPlayers; i++)
			avgRank[i] = (double) rankSum[i] / TOURNAMENT_RUNS;

		Integer[] order = new Integer[instance.numPlayers];
		for (int i = 0; i < instance.numPlayers; i++) order[i] = i;
		java.util.Arrays.sort(order, (a, b) -> Double.compare(avgRank[a], avgRank[b]));

		// Summed Rankings
		System.out.println("\n" + border);
		System.out.println("   SUMMED RANKINGS (lower = better)");
		System.out.println(border);
		System.out.printf("   %-5s %-35s %s%n", "Rank", "Player", "Summed Score");
		System.out.println(thin);
		for (int i = 0; i < instance.numPlayers; i++) {
			int p = order[i];
			System.out.printf("   %-5d %-35s %d%n", (i+1), instance.makePlayer(p).name(), rankSum[p]);
		}

		// Average Rankings
		System.out.println("\n" + border);
		System.out.println("   AVERAGE RANKINGS over " + TOURNAMENT_RUNS + " tournaments");
		System.out.println(border);
		System.out.printf("   %-5s %-35s %-12s %s%n", "Rank", "Player", "Avg Rank", "Performance");
		System.out.println(thin);
		String[] medals = {"1st", "2nd", "3rd"};
		for (int i = 0; i < instance.numPlayers; i++) {
			int p = order[i];
			String medal = i < 3 ? medals[i] : (i+1) + "th";
			String bar = "";
			int barLen = (int)((instance.numPlayers - i) * 2.5);
			for (int b = 0; b < barLen; b++) bar += "#";
			System.out.printf("   %-5s %-35s %-12.4f [%s]%n",
				medal, instance.makePlayer(p).name(), avgRank[p], bar);
		}
		System.out.println(border);
		System.out.println("   WINNER: " + instance.makePlayer(order[0]).name());
		System.out.println(border + "\n");
	}

	int[] runTournament() {
		float[] totalScore = new float[numPlayers];

		for (int i=0; i<numPlayers; i++) for (int j=i; j<numPlayers; j++) for (int k=j; k<numPlayers; k++) {
			Player A = makePlayer(i);
			Player B = makePlayer(j);
			Player C = makePlayer(k);
			int rounds = 90 + (int)Math.rint(20 * Math.random());
			float[] matchResults = scoresOfMatch(A, B, C, rounds);
			totalScore[i] = totalScore[i] + matchResults[0];
			totalScore[j] = totalScore[j] + matchResults[1];
			totalScore[k] = totalScore[k] + matchResults[2];
		}
		int[] sortedOrder = new int[numPlayers];
		for (int i=0; i<numPlayers; i++) {
			int j=i-1;
			for (; j>=0; j--) {
				if (totalScore[i] > totalScore[sortedOrder[j]])
					sortedOrder[j+1] = sortedOrder[j];
				else break;
			}
			sortedOrder[j+1] = i;
		}
		return sortedOrder;
	} // end of runTo