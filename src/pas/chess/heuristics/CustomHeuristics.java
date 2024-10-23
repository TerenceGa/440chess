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
// JAVA PROJECT IMPORTS
import src.pas.chess.heuristics.DefaultHeuristics;

public class CustomHeuristics extends Object {

    /**
     * Get the max player from a node
     * 
     * @param node
     * @return
     */
    public static Player getMaxPlayer(DFSTreeNode node) {
        return node.getMaxPlayer();
    }

    /**
     * Get the min player from a node
     * 
     * @param node
     * @return
     */
    public static Player getMinPlayer(DFSTreeNode node) {
        return DefaultHeuristics.getMaxPlayer(node).equals(node.getGame().getCurrentPlayer())
                ? node.getGame().getOtherPlayer()
                : node.getGame().getCurrentPlayer();
    }

    // Enhanced Material Balance with Piece Position Values
    public static double getMaterialBalance(DFSTreeNode node) {
        double maxPlayerMaterial = 0;
        double minPlayerMaterial = 0;

        for (Piece piece : node.getGame().getBoard().getPieces(getMaxPlayer(node))) {
            Coordinate position = node.getGame().getCurrentPosition(piece);
            maxPlayerMaterial += getPieceValue(piece.getType())
                    + getPiecePositionValue(piece, position, getMaxPlayer(node).getPlayerType());
        }

        for (Piece piece : node.getGame().getBoard().getPieces(getMinPlayer(node))) {
            Coordinate position = node.getGame().getCurrentPosition(piece);
            minPlayerMaterial += getPieceValue(piece.getType())
                    + getPiecePositionValue(piece, position, getMinPlayer(node).getPlayerType());
        }

        return maxPlayerMaterial - minPlayerMaterial;
    }

    // More Granular Piece Values
    public static double getPieceValue(PieceType type) {
        switch (type) {
            case PAWN:
                return 1.0;
            case KNIGHT:
                return 3.2;
            case BISHOP:
                return 3.33;
            case ROOK:
                return 5.1;
            case QUEEN:
                return 8.8;
            case KING:
                return 100.0; // Assign a high value to the king
            default:
                return 0.0;
        }
    }

    // Piece-Square Tables for Positional Value
    public static double getPiecePositionValue(Piece piece, Coordinate position, PlayerType playerType) {
        int x = position.getXPosition();
        int y = position.getYPosition();
        
        
        // Validate indices
        if (x < 0 || x >= 8 || y < 0 || y >= 8) {
            System.err.println("Invalid position: (" + x + ", " + y + ") for piece: " + piece.getType());
            return 0.0; // or some default value
        }
        
        double[][] pieceSquareTable = getPieceSquareTable(piece.getType(), playerType);
        return pieceSquareTable[x][y];
    }

    public static double[][] getPieceSquareTable(PieceType type, PlayerType playerType) {
        // Define piece-square tables for each piece type
        // For simplicity, we'll use simplified tables with values between -0.5 and 0.5
        // Flip the table for black pieces
        double[][] table = new double[8][8];
        switch (type) {
            case PAWN:
                table = new double[][] {
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5 },
                        { 0.1, 0.1, 0.2, 0.3, 0.3, 0.2, 0.1, 0.1 },
                        { 0.05, 0.05, 0.1, 0.25, 0.25, 0.1, 0.05, 0.05 },
                        { 0.0, 0.0, 0.0, 0.2, 0.2, 0.0, 0.0, 0.0 },
                        { 0.05, -0.05, -0.1, 0.0, 0.0, -0.1, -0.05, 0.05 },
                        { 0.05, 0.1, 0.1, -0.2, -0.2, 0.1, 0.1, 0.05 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 } };
                break;
            case KNIGHT:
                table = new double[][] {
                        { -0.5, -0.4, -0.3, -0.3, -0.3, -0.3, -0.4, -0.5 },
                        { -0.4, -0.2, 0.0, 0.0, 0.0, 0.0, -0.2, -0.4 },
                        { -0.3, 0.0, 0.1, 0.15, 0.15, 0.1, 0.0, -0.3 },
                        { -0.3, 0.05, 0.15, 0.2, 0.2, 0.15, 0.05, -0.3 },
                        { -0.3, 0.0, 0.15, 0.2, 0.2, 0.15, 0.0, -0.3 },
                        { -0.3, 0.05, 0.1, 0.15, 0.15, 0.1, 0.05, -0.3 },
                        { -0.4, -0.2, 0.0, 0.05, 0.05, 0.0, -0.2, -0.4 },
                        { -0.5, -0.4, -0.3, -0.3, -0.3, -0.3, -0.4, -0.5 } };
                break;
            case BISHOP:
                table = new double[][] {
                        { -0.2, -0.1, -0.1, -0.1, -0.1, -0.1, -0.1, -0.2 },
                        { -0.1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.1 },
                        { -0.1, 0.0, 0.05, 0.1, 0.1, 0.05, 0.0, -0.1 },
                        { -0.1, 0.05, 0.05, 0.1, 0.1, 0.05, 0.05, -0.1 },
                        { -0.1, 0.0, 0.1, 0.1, 0.1, 0.1, 0.0, -0.1 },
                        { -0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, -0.1 },
                        { -0.1, 0.05, 0.0, 0.0, 0.0, 0.0, 0.05, -0.1 },
                        { -0.2, -0.1, -0.1, -0.1, -0.1, -0.1, -0.1, -0.2 } };
                break;
            case ROOK:
                table = new double[][] {
                        { 0.0, 0.0, 0.0, 0.05, 0.05, 0.0, 0.0, 0.0 },
                        { -0.05, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.05 },
                        { -0.05, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.05 },
                        { -0.05, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.05 },
                        { -0.05, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.05 },
                        { -0.05, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.05 },
                        { 0.05, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.05 },
                        { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 } };
                break;
            case QUEEN:
                table = new double[][] {
                        { -0.2, -0.1, -0.1, -0.05, -0.05, -0.1, -0.1, -0.2 },
                        { -0.1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.1 },
                        { -0.1, 0.0, 0.05, 0.05, 0.05, 0.05, 0.0, -0.1 },
                        { -0.05, 0.0, 0.05, 0.05, 0.05, 0.05, 0.0, -0.05 },
                        { 0.0, 0.0, 0.05, 0.05, 0.05, 0.05, 0.0, -0.05 },
                        { -0.1, 0.05, 0.05, 0.05, 0.05, 0.05, 0.0, -0.1 },
                        { -0.1, 0.0, 0.05, 0.0, 0.0, 0.0, 0.0, -0.1 },
                        { -0.2, -0.1, -0.1, -0.05, -0.05, -0.1, -0.1, -0.2 } };
                break;
            case KING:
                // Simplified king safety table
                table = new double[][] {
                        { -0.3, -0.4, -0.4, -0.5, -0.5, -0.4, -0.4, -0.3 },
                        { -0.3, -0.4, -0.4, -0.5, -0.5, -0.4, -0.4, -0.3 },
                        { -0.3, -0.4, -0.4, -0.5, -0.5, -0.4, -0.4, -0.3 },
                        { -0.3, -0.4, -0.4, -0.5, -0.5, -0.4, -0.4, -0.3 },
                        { -0.2, -0.3, -0.3, -0.4, -0.4, -0.3, -0.3, -0.2 },
                        { -0.1, -0.2, -0.2, -0.2, -0.2, -0.2, -0.2, -0.1 },
                        { 0.2, 0.2, 0.0, 0.0, 0.0, 0.0, 0.2, 0.2 },
                        { 0.2, 0.3, 0.1, 0.0, 0.0, 0.1, 0.3, 0.2 } };
                break;
            default:
                table = new double[8][8];
        }

        // Flip the table for black pieces
        if (playerType == PlayerType.BLACK) {
            double[][] flippedTable = new double[8][8];
            for (int i = 0; i < 8; i++) {
                flippedTable[i] = table[7 - i];
            }
            return flippedTable;
        }
        return table;
    }

    // Refined Mobility - Weighted by Piece Type
    public static double getMobility(DFSTreeNode node, Player player) {
        double mobility = 0;
        for (Piece piece : node.getGame().getBoard().getPieces(player)) {
            int moveCount = piece.getAllMoves(node.getGame()).size();
            double pieceValue = getPieceValue(piece.getType());
            mobility += moveCount * (pieceValue / 10.0); // Normalize the influence
        }
        return mobility;
    }

    // Improved Pawn Structure Evaluation
    public static double getPawnStructureScore(DFSTreeNode node, Player player) {
        double score = 0;
        score += countAdvancedPawns(node, player) * 0.2;
        return score;
    }


    // Count Advanced Pawns
    private static int countAdvancedPawns(DFSTreeNode node, Player player) {
        int advancedPawns = 0;
        Set<Piece> pawns = node.getGame().getBoard().getPieces(player, PieceType.PAWN);
        for (Piece pawn : pawns) {
            Coordinate pos = node.getGame().getCurrentPosition(pawn);
            int row = pos.getXPosition();
            if ((player.getPlayerType() == PlayerType.WHITE && row <= 3) ||
                (player.getPlayerType() == PlayerType.BLACK && row >= 4)) {
                advancedPawns++;
            }
        }
        return advancedPawns;
    }

    // King Safety Evaluation
    public static double getKingSafetyScore(DFSTreeNode node, Player player) {
        double score = 0;

        return score;
    }



    private static int countEnemyThreatsToKing(DFSTreeNode node, Player player, Coordinate kingPos) {
        int threats = 0;

        return threats;
    }


    // Control of the Center
    public static double getCenterControlScore(DFSTreeNode node, Player player) {
        double score = 0;

        return score;
    }

    // Adjusted Heuristic Value with Combined Scores
    public static double getMaxPlayerHeuristicValue(DFSTreeNode node) {
        Player maxPlayer = getMaxPlayer(node);
        Player minPlayer = getMinPlayer(node);

        // Material balance
        double materialBalance = getMaterialBalance(node);

        // Mobility
        double mobilityMax = getMobility(node, maxPlayer);
        double mobilityMin = getMobility(node, minPlayer);

        // Pawn structure
        double pawnStructureMax = getPawnStructureScore(node, maxPlayer);
        double pawnStructureMin = getPawnStructureScore(node, minPlayer);





        // Center control
        double centerControlMax = getCenterControlScore(node, maxPlayer);
        double centerControlMin = getCenterControlScore(node, minPlayer);

        // Combine the scores with weights
        double heuristicValue = 0;
        heuristicValue += (materialBalance) * 10.0; // Material is crucial
        heuristicValue += (mobilityMax - mobilityMin) * 0.5;
        heuristicValue += (pawnStructureMax - pawnStructureMin) * 2.0;
        heuristicValue += (centerControlMax - centerControlMin) * 1.0;

        return heuristicValue;
    }
}

