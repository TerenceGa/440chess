package src.pas.chess.heuristics;


// SYSTEM IMPORTS
import edu.bu.chess.search.DFSTreeNode;


// JAVA PROJECT IMPORTS
import src.pas.chess.heuristics.DefaultHeuristics;


public class CustomHeuristics
    extends Object
{

	/**
	 * TODO: implement me! The heuristics that I wrote are useful, but not very good for a good chessbot.
	 * Please use this class to add your heuristics here! I recommend taking a look at the ones I provided for you
	 * in DefaultHeuristics.java (which is in the same directory as this file)
	 */
	public static double getMaxPlayerHeuristicValue(DFSTreeNode node)
	{
		// please replace this!
		return DefaultHeuristics.getMaxPlayerHeuristicValue(node);
	}

}
