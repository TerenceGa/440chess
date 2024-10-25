package src.pas.chess.moveorder;

// SYSTEM IMPORTS
import edu.bu.chess.search.DFSTreeNode;

// JAVA IMPORTS
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// PROJECT-SPECIFIC IMPORTS
import edu.bu.chess.game.Game;
import edu.bu.chess.game.move.Move;
import edu.bu.chess.game.move.CaptureMove;
import edu.bu.chess.game.move.CastleMove;
import edu.bu.chess.game.move.MovementMove;
import edu.bu.chess.game.move.PromotePawnMove;
import edu.bu.chess.game.move.MoveType;
import edu.bu.chess.game.piece.Piece;
import edu.bu.chess.game.piece.PieceType;
import edu.bu.chess.game.player.Player;
import edu.bu.chess.utils.Coordinate;
import src.pas.chess.moveorder.DefaultMoveOrderer;

public class CustomMoveOrderer extends DefaultMoveOrderer {

    private static final int MAX_DEPTH = 100;
    private static Move[] pvMoves = new Move[MAX_DEPTH]; // Principal Variation moves
    private static Move[][] killerMoves = new Move[MAX_DEPTH][2]; // Killer moves
    private static Map<String, Integer> historyTable = new HashMap<>(); // History heuristic table

    /**
     * Orders the moves using various heuristics to improve alpha-beta pruning efficiency.
     *
     * @param nodes The list of child nodes to be ordered.
     * @param depth The current depth in the search tree.
     * @return The ordered list of nodes.
     */
    public static List<DFSTreeNode> order(List<DFSTreeNode> nodes, int depth) {
        List<DFSTreeNode> pvMovesList = new ArrayList<>();
        List<DFSTreeNode> killerMovesList = new ArrayList<>();
        List<DFSTreeNode> captureMoves = new ArrayList<>();
        List<DFSTreeNode> checkingMoves = new ArrayList<>();
        List<DFSTreeNode> otherMoves = new ArrayList<>();

        for (DFSTreeNode node : nodes) {
            Move move = node.getMove();

            if (move.equals(pvMoves[depth])) {
                pvMovesList.add(node);
            } else if (isKillerMove(move, depth)) {
                killerMovesList.add(node);
            } else if (move.getType() == MoveType.CAPTUREMOVE) {
                captureMoves.add(node);
            } else if (causesCheck(node)) {
                checkingMoves.add(node);
            } else {
                otherMoves.add(node);
            }
        }

        // Sort capture moves using MVV-LVA
        captureMoves.sort((n1, n2) -> Integer.compare(getMVVLVA(n2), getMVVLVA(n1)));

        // Sort other moves using history heuristic
        otherMoves.sort((n1, n2) -> Integer.compare(getHistoryScore(n2.getMove()), getHistoryScore(n1.getMove())));

        // Combine all the lists
        List<DFSTreeNode> orderedNodes = new ArrayList<>(nodes.size());
        orderedNodes.addAll(pvMovesList);
        orderedNodes.addAll(killerMovesList);
        orderedNodes.addAll(captureMoves);
        orderedNodes.addAll(checkingMoves);
        orderedNodes.addAll(otherMoves);

        return orderedNodes;
    }

    private static boolean isKillerMove(Move move, int depth) {
        return move.equals(killerMoves[depth][0]) || move.equals(killerMoves[depth][1]);
    }

    /**
     * Updates the killer moves when a beta cutoff occurs.
     *
     * @param move  The move that caused the cutoff.
     * @param depth The depth at which the cutoff occurred.
     */
    public static void addKillerMove(Move move, int depth) {
        if (!move.equals(killerMoves[depth][0])) {
            killerMoves[depth][1] = killerMoves[depth][0];
            killerMoves[depth][0] = move;
        }
    }

    /**
     * Calculates the MVV-LVA score for a capture move.
     *
     * @param node The DFSTreeNode containing the move and game state.
     * @return The MVV-LVA score.
     */
    private static int getMVVLVA(DFSTreeNode node) {
        Move move = node.getMove();
        Game game = node.getGame();

        if (move instanceof CaptureMove) {
            CaptureMove captureMove = (CaptureMove) move;

            // Get attacker and victim piece IDs
            int attackerPieceID = captureMove.getAttackingPieceID();
            int victimPieceID = captureMove.getTargetPieceID();

            // Get attacker and victim players
            Player attackerPlayer = captureMove.getAttackingPlayer();
            Player victimPlayer = captureMove.getTargetPlayer();

            // Retrieve the pieces using the protected getPiece method
            Piece attackerPiece = getPiece(game, attackerPlayer, attackerPieceID);
            Piece victimPiece = getPiece(game, victimPlayer, victimPieceID);

            if (attackerPiece != null && victimPiece != null) {
                int victimValue = getPieceValue(victimPiece.getType());
                int attackerValue = getPieceValue(attackerPiece.getType());
                return (victimValue * 10) - attackerValue;
            }
        }
        // Default score for non-capture moves or if pieces are null
        return 0;
    }

    /**
     * Protected method to retrieve a Piece given the game, player, and piece ID.
     *
     * @param game     The current game state.
     * @param player   The player owning the piece.
     * @param pieceID  The ID of the piece.
     * @return The Piece object, or null if not found.
     */
    protected static Piece getPiece(Game game, Player player, Integer pieceID) {
        // Implementation depends on your game architecture.
        // For example, you might access the board to find the piece.
        return game.getBoard().getPiece(player, pieceID);
    }

    /**
     * Returns the piece value for the given piece type.
     *
     * @param type The piece type.
     * @return The value of the piece.
     */
    private static int getPieceValue(PieceType type) {
        switch (type) {
            case PAWN:
                return 1;
            case KNIGHT:
            case BISHOP:
                return 3;
            case ROOK:
                return 5;
            case QUEEN:
                return 9;
            case KING:
                return 1000; // Arbitrary high value
            default:
                return 0;
        }
    }

    /**
     * Determines if the move results in the opponent's king being in check.
     *
     * @param node The node containing the move and game state.
     * @return True if the move causes a check; false otherwise.
     */
    private static boolean causesCheck(DFSTreeNode node) {
        // Implement logic to determine if the move puts the opponent in check
        // This will depend on your game implementation
        return node.getGame().isInCheck(node.getGame().getOtherPlayer());
    }

    /**
     * Retrieves the history heuristic score for a move.
     *
     * @param move The move to retrieve the score for.
     * @return The history heuristic score.
     */
    private static int getHistoryScore(Move move) {
        String key = generateHistoryKey(move);
        return historyTable.getOrDefault(key, 0);
    }

    /**
     * Generates a unique key for the history table based on the move details.
     *
     * @param move The move for which to generate the key.
     * @return A string representing the unique key for the move.
     */
    private static String generateHistoryKey(Move move) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(move.getActorPieceID()).append("-").append(move.getType());

        switch (move.getType()) {
            case MOVEMENTMOVE:
                // For MovementMove, get the target position
                if (move instanceof MovementMove) {
                    MovementMove movementMove = (MovementMove) move;
                    Coordinate targetPosition = movementMove.getTargetPosition();
                    keyBuilder.append("-").append(targetPosition.toString());
                }
                break;
            case CAPTUREMOVE:
                // For CaptureMove, use the target piece ID
                if (move instanceof CaptureMove) {
                    CaptureMove captureMove = (CaptureMove) move;
                    int targetPieceID = captureMove.getTargetPieceID();
                    keyBuilder.append("-").append(targetPieceID);
                }
                break;
            case CASTLEMOVE:
                // For CastleMove, include the rook piece ID
                if (move instanceof CastleMove) {
                    CastleMove castleMove = (CastleMove) move;
                    int rookPieceID = castleMove.getRookPieceID();
                    keyBuilder.append("-").append(rookPieceID);
                }
                break;
            case PROMOTEPAWNMOVE:
                // For PromotePawnMove, include the promoted piece type
                if (move instanceof PromotePawnMove) {
                    PromotePawnMove promoteMove = (PromotePawnMove) move;
                    PieceType promotedType = promoteMove.getPromotedPieceType();
                    keyBuilder.append("-").append(promotedType.toString());
                }
                break;
            default:
                // Handle other move types if any
                break;
        }

        return keyBuilder.toString();
    }

    /**
     * Updates the history table when a move causes a beta cutoff.
     *
     * @param move  The move that caused the cutoff.
     * @param depth The depth at which the cutoff occurred.
     */
    public static void addHistoryScore(Move move, int depth) {
        String key = generateHistoryKey(move);
        int currentScore = historyTable.getOrDefault(key, 0);
        historyTable.put(key, currentScore + depth * depth);
    }

    /**
     * Updates the principal variation move at the given depth.
     *
     * @param move  The best move found at the current depth.
     * @param depth The depth at which the move is the best.
     */
    public static void updatePVMove(Move move, int depth) {
        pvMoves[depth] = move;
    }
}


