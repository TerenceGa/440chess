package src.pas.chess.heuristics;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;


// SYSTEM IMPORTS
import edu.bu.chess.game.Game;
import edu.bu.chess.game.move.Move;
import edu.bu.chess.game.move.MoveType;
import edu.bu.chess.game.move.CaptureMove;
import edu.bu.chess.game.move.CastleMove;
import edu.bu.chess.game.move.MovementMove;
import edu.bu.chess.game.move.PromotePawnMove;
import edu.bu.chess.game.piece.Piece;
import edu.bu.chess.game.piece.PieceType;
import edu.bu.chess.game.player.Player;
import edu.bu.chess.game.player.PlayerType;
import edu.bu.chess.search.DFSTreeNode;
import edu.bu.chess.utils.Coordinate;
import edu.bu.chess.game.move.MovementMove;
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
            return 0.0; // or some default value
        }

        double[][] pieceSquareTable = getPieceSquareTable(piece.getType(), playerType);
        return pieceSquareTable[x][y];
    }

    public static double[][] getPieceSquareTable(PieceType type, PlayerType playerType) {
        // Define piece-square tables for each piece type
        // For simplicity, we'll use simplified tables with values between -0.5 and 0.5
        double[][] table;
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
            List<Move> moves = node.getGame().getAllMovesForPiece(player, piece);
            int moveCount = moves.size();
            double pieceValue = getPieceValue(piece.getType());
            // Penalize if mobility is zero
            if (moveCount == 0) {
                mobility -= pieceValue * 0.1;
            } else {
                mobility += moveCount * (pieceValue / 10.0); // Normalize the influence
            }
        }
        return mobility;
    }

    // Improved Pawn Structure Evaluation
    public static double getPawnStructureScore(DFSTreeNode node, Player player) {
        double score = 0;
        score -= countIsolatedPawns(node, player) * 0.5;
        score -= countDoubledPawns(node, player) * 0.5;
        score -= countBackwardPawns(node, player) * 0.5;
        score += countPawnChains(node, player) * 0.2;
        score += countAdvancedPawns(node, player) * 0.2;
        score += countPassedPawns(node, player) * 1.0;
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

    // Count Isolated Pawns
    private static int countIsolatedPawns(DFSTreeNode node, Player player) {
        int isolatedPawns = 0;
        Set<Piece> pawns = node.getGame().getBoard().getPieces(player, PieceType.PAWN);
        boolean[] filesWithPawns = new boolean[8];
        for (Piece pawn : pawns) {
            Coordinate pos = node.getGame().getCurrentPosition(pawn);
            int file = pos.getYPosition();
            filesWithPawns[file] = true;
        }
        for (Piece pawn : pawns) {
            Coordinate pos = node.getGame().getCurrentPosition(pawn);
            int file = pos.getYPosition();
            boolean hasAdjacentPawn = false;
            if (file > 0 && filesWithPawns[file - 1]) {
                hasAdjacentPawn = true;
            }
            if (file < 7 && filesWithPawns[file + 1]) {
                hasAdjacentPawn = true;
            }
            if (!hasAdjacentPawn) {
                isolatedPawns++;
            }
        }
        return isolatedPawns;
    }

    // Count Doubled Pawns
    private static int countDoubledPawns(DFSTreeNode node, Player player) {
        int doubledPawns = 0;
        int[] pawnCounts = new int[8];
        Set<Piece> pawns = node.getGame().getBoard().getPieces(player, PieceType.PAWN);
        for (Piece pawn : pawns) {
            Coordinate pos = node.getGame().getCurrentPosition(pawn);
            int file = pos.getYPosition();
            pawnCounts[file]++;
        }
        for (int count : pawnCounts) {
            if (count > 1) {
                doubledPawns += count - 1;
            }
        }
        return doubledPawns;
    }

    // Count Backward Pawns
    private static int countBackwardPawns(DFSTreeNode node, Player player) {
        int backwardPawns = 0;
        // Implement logic to count backward pawns
        // This requires analyzing pawn structures and potential advances
        return backwardPawns;
    }

    // Count Pawn Chains
    private static int countPawnChains(DFSTreeNode node, Player player) {
        int pawnChains = 0;
        // Implement logic to count pawn chains
        // This can be complex and may require advanced analysis
        return pawnChains;
    }

    // Count Passed Pawns
    private static int countPassedPawns(DFSTreeNode node, Player player) {
        int passedPawns = 0;
        // Implement logic to count passed pawns
        // This involves checking for opposing pawns in front of the pawn
        return passedPawns;
    }

    // King Safety Evaluation
    public static double getKingSafetyScore(DFSTreeNode node, Player player) {
        double score = 0;
        Coordinate kingPosition = node.getGame().getCurrentPosition(node.getGame().getBoard().getPieces(player, PieceType.KING).iterator().next());
        // Evaluate pawn shield around the king
        int pawnShield = countPawnShield(node, player, kingPosition);
        score += pawnShield * 0.3;

        // Penalize open files near the king
        int openFiles = countOpenFilesNearKing(node, player, kingPosition);
        score -= openFiles * 0.2;

        // Penalize proximity of enemy pieces
        int enemyThreats = countEnemyThreatsToKing(node, player, kingPosition);
        score -= enemyThreats * 0.5;

        return score;
    }

    // Count Pawn Shield
    private static int countPawnShield(DFSTreeNode node, Player player, Coordinate kingPosition) {
        int shield = 0;
        int x = kingPosition.getXPosition();
        int y = kingPosition.getYPosition();
        int direction = (player.getPlayerType() == PlayerType.WHITE) ? -1 : 1;

        for (int dy = -1; dy <= 1; dy++) {
            int nx = x + direction;
            int ny = y + dy;
            if (nx >= 0 && nx < 8 && ny >= 0 && ny < 8) {
                Piece piece = node.getGame().getBoard().getPieceAtPosition(new Coordinate(nx, ny));
                if (piece != null && piece.getType() == PieceType.PAWN && piece.getPlayer().equals(player)) {
                    shield++;
                }
            }
        }
        return shield;
    }

    // Count Open Files Near King
    private static int countOpenFilesNearKing(DFSTreeNode node, Player player, Coordinate kingPosition) {
        int openFiles = 0;
        int y = kingPosition.getYPosition();
        for (int dy = -1; dy <= 1; dy++) {
            int file = y + dy;
            if (file >= 0 && file < 8) {
                boolean isOpen = true;
                for (int x = 0; x < 8; x++) {
                    Piece piece = node.getGame().getBoard().getPieceAtPosition(new Coordinate(x, file));
                    if (piece != null && piece.getType() == PieceType.PAWN) {
                        isOpen = false;
                        break;
                    }
                }
                if (isOpen) {
                    openFiles++;
                }
            }
        }
        return openFiles;
    }

    // Count Enemy Threats to King
    private static int countEnemyThreatsToKing(DFSTreeNode node, Player player, Coordinate kingPos) {
        int threats = 0;
        Player opponent = node.getGame().getOtherPlayer(player);
        Set<Piece> opponentPieces = node.getGame().getBoard().getPieces(opponent);
        for (Piece piece : opponentPieces) {
            List<Move> moves = piece.getAllMoves(node.getGame());
            if (moves.contains(kingPos)) {
                threats++;
            }
        }
        return threats;
    }

    // Control of the Center
    public static double getCenterControlScore(DFSTreeNode node, Player player) {
        double score = 0;
        Set<Coordinate> centralSquares = new HashSet<>(Arrays.asList(
                new Coordinate(3, 3), new Coordinate(3, 4),
                new Coordinate(4, 3), new Coordinate(4, 4)
        ));
        for (Piece piece : node.getGame().getBoard().getPieces(player)) {
            List<Move> moves = piece.getAllMoves(node.getGame());
            for (Move move : moves) {
                if (centralSquares.contains(move)) {
                    score += 0.1;
                }
            }
        }
        return score;
    }

    // Piece Development Evaluation
    public static double getDevelopmentScore(DFSTreeNode node, Player player) {
        double score = 0;
        for (Piece piece : node.getGame().getBoard().getPieces(player)) {
            if (isUndeveloped(piece, node.getGame())) {
                score -= getPieceValue(piece.getType()) * 0.1;
            }
        }
        return score;
    }

    private static boolean isUndeveloped(Piece piece, Game game) {
        Coordinate pos = game.getCurrentPosition(piece);
        // Assuming initial positions are undeveloped for minor pieces
        if (piece.getType() == PieceType.KNIGHT || piece.getType() == PieceType.BISHOP) {
            if (piece.getPlayer().getPlayerType() == PlayerType.WHITE) {
                return pos.getXPosition() == 7;
            } else {
                return pos.getXPosition() == 0;
            }
        }
        return false;
    }

    // Threats and Tactical Opportunities
    public static double getThreatScore(DFSTreeNode node, Player player) {
        double score = 0;

        return score;
    }

    // Piece Coordination and Harmony
    public static double getPieceCoordinationScore(DFSTreeNode node, Player player) {
        double score = 0;

        return score;
    }

    // Control of Open Files and Diagonals
    public static double getOpenLinesScore(DFSTreeNode node, Player player) {
        double score = 0;
        // Implement logic to identify open and half-open files and diagonals
        // Reward rooks and bishops accordingly
        return score;
    }

    // Threatened Pieces
    public static double getThreatenedPieceScore(DFSTreeNode node, Player player) {
        double score = 0.0;
        Player opponent = node.getGame().getOtherPlayer(player);
        Set<Piece> opponentPieces = node.getGame().getBoard().getPieces(opponent);
        Set<Coordinate> opponentAttackedSquares = new HashSet<>();

        // Collect all squares attacked by the opponent
        for (Piece piece : opponentPieces) {
            List<Move> moves = piece.getAllMoves(node.getGame());
            for (Move move : moves) {
                Coordinate destination = extractDestination(move);
                if (destination != null) {
                    opponentAttackedSquares.add(destination);
                }
            }
        }

        // Iterate through player's pieces and check if they are under threat
        Set<Piece> playerPieces = node.getGame().getBoard().getPieces(player);
        for (Piece piece : playerPieces) {
            Coordinate pos = node.getGame().getCurrentPosition(piece);
            if (opponentAttackedSquares.contains(pos)) {
                double pieceValue = getPieceValue(piece.getType());
                score -= pieceValue * 0.5; // Adjust weight as necessary
            }
        }

        return score;
    }

    /**
     * Extracts the destination Coordinate from a Move object based on its type.
     *
     * @param move The Move object from which to extract the destination.
     * @return The destination Coordinate if applicable; otherwise, null.
     */
    private static Coordinate extractDestination(Move move) {
        if (move instanceof MovementMove) {
            return ((MovementMove) move).getTargetPosition();
        } 
        // CastleMove and other move types are ignored for threat detection
        return null;
    }

    // Game Phase Determination
    public static GamePhase getGamePhase(DFSTreeNode node) {
        double totalMaterial = 0;
        for (Piece piece : node.getGame().getBoard().getPieces(node.getGame().getPlayer(PlayerType.WHITE))) {
            totalMaterial += getPieceValue(piece.getType());
        }
        for (Piece piece : node.getGame().getBoard().getPieces(node.getGame().getPlayer(PlayerType.BLACK))) {
            totalMaterial += getPieceValue(piece.getType());
        }
        if (totalMaterial > 40) {
            return GamePhase.OPENING;
        } else if (totalMaterial > 20) {
            return GamePhase.MIDDLEGAME;
        } else {
            return GamePhase.ENDGAME;
        }
    }

    public enum GamePhase {
        OPENING,
        MIDDLEGAME,
        ENDGAME
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

        // King safety
        double kingSafetyMax = getKingSafetyScore(node, maxPlayer);
        double kingSafetyMin = getKingSafetyScore(node, minPlayer);

        // Development
        double developmentMax = getDevelopmentScore(node, maxPlayer);
        double developmentMin = getDevelopmentScore(node, minPlayer);

        // Center control
        double centerControlMax = getCenterControlScore(node, maxPlayer);
        double centerControlMin = getCenterControlScore(node, minPlayer);

        // Threats
        double threatScoreMax = getThreatScore(node, maxPlayer);
        double threatScoreMin = getThreatScore(node, minPlayer);

        // Piece coordination
        double coordinationScoreMax = getPieceCoordinationScore(node, maxPlayer);
        double coordinationScoreMin = getPieceCoordinationScore(node, minPlayer);

        // Open lines
        double openLinesScoreMax = getOpenLinesScore(node, maxPlayer);
        double openLinesScoreMin = getOpenLinesScore(node, minPlayer);

        // Threatened pieces
        double threatenedScoreMax = getThreatenedPieceScore(node, maxPlayer);
        double threatenedScoreMin = getThreatenedPieceScore(node, minPlayer);

        // Determine game phase
        GamePhase phase = getGamePhase(node);

        // Adjust weights based on game phase
        double materialWeight = 10.0;
        double mobilityWeight = 0.5;
        double pawnStructureWeight = 2.0;
        double kingSafetyWeight = 5.0;
        double developmentWeight = 1.0;
        double centerControlWeight = 1.0;
        double threatWeight = 1.0;
        double coordinationWeight = 0.5;
        double openLinesWeight = 0.5;
        double threatenedPieceWeight = 1.0;

        if (phase == GamePhase.ENDGAME) {
            kingSafetyWeight = 2.0;
            developmentWeight = 0.5;
            // Adjust other weights as appropriate
        }

        // Combine the scores with weights
        double heuristicValue = 0;
        heuristicValue += (materialBalance) * materialWeight;
        heuristicValue += (mobilityMax - mobilityMin) * mobilityWeight;
        heuristicValue += (pawnStructureMax - pawnStructureMin) * pawnStructureWeight;
        heuristicValue += (kingSafetyMax - kingSafetyMin) * kingSafetyWeight;
        heuristicValue += (developmentMax - developmentMin) * developmentWeight;
        heuristicValue += (centerControlMax - centerControlMin) * centerControlWeight;
        heuristicValue += (threatScoreMax - threatScoreMin) * threatWeight;
        heuristicValue += (coordinationScoreMax - coordinationScoreMin) * coordinationWeight;
        heuristicValue += (openLinesScoreMax - openLinesScoreMin) * openLinesWeight;
        heuristicValue += (threatenedScoreMin - threatenedScoreMax) * threatenedPieceWeight; // Note subtraction

        return heuristicValue;
    }
}


