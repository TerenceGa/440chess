package src.pas.chess.heuristics;


import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;

import edu.bu.chess.game.move.Move;
import edu.bu.chess.game.piece.Piece;
import edu.bu.chess.game.piece.PieceType;
import edu.bu.chess.game.player.Player;
import edu.bu.chess.game.player.PlayerType;
// SYSTEM IMPORTS
import edu.bu.chess.search.DFSTreeNode;
import edu.bu.chess.utils.Coordinate;
import edu.cwru.sepia.util.Direction;
// JAVA PROJECT IMPORTS
import src.pas.chess.heuristics.DefaultHeuristics;


public class CustomHeuristics
    extends Object
{
		/**
	 * Get the max player from a node
	 * @param node
	 * @return
	 */
	public static Player getMaxPlayer(DFSTreeNode node)
	{
		return node.getMaxPlayer();
	}

	/**
	 * Get the min player from a node
	 * @param node
	 * @return
	 */
	public static Player getMinPlayer(DFSTreeNode node)
	{
		return DefaultHeuristics.getMaxPlayer(node).equals(node.getGame().getCurrentPlayer()) ? node.getGame().getOtherPlayer() : node.getGame().getCurrentPlayer();
	}

	// 1
	// Material Balance
	// Evaluate the difference in total piece values between the max player and the min player.
	public static int getMaterialBalance(DFSTreeNode node) {
    int maxPlayerMaterial = 0;
    int minPlayerMaterial = 0;

    for (Piece piece : node.getGame().getBoard().getPieces(getMaxPlayer(node))) {
        maxPlayerMaterial += Piece.getPointValue(piece.getType());
    }

    for (Piece piece : node.getGame().getBoard().getPieces(getMinPlayer(node))) {
        minPlayerMaterial += Piece.getPointValue(piece.getType());
    }

    return maxPlayerMaterial - minPlayerMaterial;
}
	// 2 
	// mobility
	// Consider the number of legal moves available to each player.
    public static int getMobility(DFSTreeNode node, Player player) {
        int mobility = 0;
        for (Piece piece : node.getGame().getBoard().getPieces(player)) {
            mobility += piece.getAllMoves(node.getGame()).size();
        }
        return mobility;
    }



	// 3
    // compute pawn structure score
	// Analyze pawn formations, including isolated pawns, doubled pawns, and passed pawns.
    public static int getPawnStructureScore(DFSTreeNode node, Player player) {
        int score = 0;
        score -= countIsolatedPawns(node, player) * 10;
        score -= countDoubledPawns(node, player) * 10;
        score += countPassedPawns(node, player) * 20;
        return score;
    }

    private static int countIsolatedPawns(DFSTreeNode node, Player player) {
        int isolatedPawns = 0;
        Set<Piece> pawns = node.getGame().getBoard().getPieces(player, PieceType.PAWN);
        Set<Integer> pawnFiles = new HashSet<>();
        for (Piece pawn : pawns) {
            Coordinate pos = node.getGame().getCurrentPosition(pawn);
            pawnFiles.add(pos.getYPosition());
        }
        for (Piece pawn : pawns) {
            Coordinate pos = node.getGame().getCurrentPosition(pawn);
            int col = pos.getYPosition();
            boolean hasAdjacentPawn = pawnFiles.contains(col - 1) || pawnFiles.contains(col + 1);
            if (!hasAdjacentPawn) {
                isolatedPawns++;
            }
        }
        return isolatedPawns;
    }

    private static int countDoubledPawns(DFSTreeNode node, Player player) {
        int doubledPawns = 0;
        int[] pawnCounts = new int[8]; // There are 8 files (columns)
        Set<Piece> pawns = node.getGame().getBoard().getPieces(player, PieceType.PAWN);
        for (Piece pawn : pawns) {
            Coordinate pos = node.getGame().getCurrentPosition(pawn);
            int col = pos.getYPosition();
            pawnCounts[col]++;
        }
        for (int count : pawnCounts) {
            if (count > 1) {
                doubledPawns += (count - 1); // Count the extra pawns
            }
        }
        return doubledPawns;
    }

    private static int countPassedPawns(DFSTreeNode node, Player player) {
        int passedPawns = 0;
        Set<Piece> myPawns = node.getGame().getBoard().getPieces(player, PieceType.PAWN);
        Set<Piece> enemyPawns = node.getGame().getBoard().getPieces(getMinPlayer(node), PieceType.PAWN);

        for (Piece myPawn : myPawns) {
            Coordinate pos = node.getGame().getCurrentPosition(myPawn);
            int myRow = pos.getXPosition();
            int myCol = pos.getYPosition();
            boolean isPassed = true;
            for (Piece enemyPawn : enemyPawns) {
                Coordinate enemyPos = node.getGame().getCurrentPosition(enemyPawn);
                int enemyRow = enemyPos.getXPosition();
                int enemyCol = enemyPos.getYPosition();

                // For white pawns, rows decrease as they advance; for black, rows increase.
                if (player.getPlayerType().equals(PlayerType.WHITE)) {
                    if (enemyRow < myRow && Math.abs(enemyCol - myCol) <= 1) {
                        isPassed = false;
                        break;
                    }
                } else {
                    if (enemyRow > myRow && Math.abs(enemyCol - myCol) <= 1) {
                        isPassed = false;
                        break;
                    }
                }
            }
            if (isPassed) {
                passedPawns++;
            }
        }
        return passedPawns;
    }


	/**
	 * TODO: implement me! The heuristics that I wrote are useful, but not very good for a good chessbot.
	 * Please use this class to add your heuristics here! I recommend taking a look at the ones I provided for you
	 * in DefaultHeuristics.java (which is in the same directory as this file)
	 */
	public static double getMaxPlayerHeuristicValue(DFSTreeNode node)
	{
		// please replace this!
        Player maxPlayer = getMaxPlayer(node);
        Player minPlayer = getMinPlayer(node);

        // Material balance
        int materialBalance = getMaterialBalance(node);

        // Mobility
        int mobilityMax = getMobility(node, maxPlayer);
        int mobilityMin = getMobility(node, minPlayer);

        // Pawn structure
        int pawnStructureMax = getPawnStructureScore(node, maxPlayer);
        int pawnStructureMin = getPawnStructureScore(node, minPlayer);

        // Combine the scores with weights
        double heuristicValue = 0;
        heuristicValue += (materialBalance) * 10; // Material is crucial, assign higher weight
        heuristicValue += (mobilityMax - mobilityMin) * 0.1;
        heuristicValue += (pawnStructureMax - pawnStructureMin) * 0.5;

        return heuristicValue;
		//return DefaultHeuristics.getMaxPlayerHeuristicValue(node);
	}

}
