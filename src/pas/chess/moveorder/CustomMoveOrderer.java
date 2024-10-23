package src.pas.chess.moveorder;

// SYSTEM IMPORTS
import edu.bu.chess.search.DFSTreeNode;

// JAVA IMPORTS
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

// PROJECT-SPECIFIC IMPORTS
import edu.bu.chess.game.move.Move;
import edu.bu.chess.game.move.MoveType;
import src.pas.chess.moveorder.DefaultMoveOrderer;

public class CustomMoveOrderer
    extends Object
{

	/**
	 * TODO: implement me!
	 * This method should perform move ordering. Remember, move ordering is how alpha-beta pruning gets part of its power from.
	 * You want to see nodes which are beneficial FIRST so you can prune as much as possible during the search (i.e. be faster)
	 * @param nodes. The nodes to order (these are children of a DFSTreeNode) that we are about to consider in the search.
	 * @return The ordered nodes.
	 */
    public static List<DFSTreeNode> order(List<DFSTreeNode> nodes) {
        List<DFSTreeNode> highImpactMoves = new LinkedList<>();
        List<DFSTreeNode> castlingMoves = new LinkedList<>();
        List<DFSTreeNode> otherMoves = new LinkedList<>();

        for (DFSTreeNode currentNode : nodes) {
            Move currentMove = currentNode.getMove();

            if (currentMove != null) {
                MoveType moveCategory = currentMove.getType();

                if (isHighImpact(moveCategory)) {
                    highImpactMoves.add(currentNode);
                } else if (isCastlingMove(moveCategory)) {
                    castlingMoves.add(currentNode);
                } else {
                    otherMoves.add(currentNode);
                }
            } else {
                otherMoves.add(currentNode);
            }
        }

        List<DFSTreeNode> orderedNodes = new ArrayList<>(highImpactMoves.size() + castlingMoves.size() + otherMoves.size());
        orderedNodes.addAll(highImpactMoves);
        orderedNodes.addAll(castlingMoves);
        orderedNodes.addAll(otherMoves);

        return orderedNodes;
    }

    /**
     * Determines if a move type is considered high-impact.
     *
     * @param type The type of the move.
     * @return True if the move is high-impact; false otherwise.
     */
    private static boolean isHighImpact(MoveType type) {
        return type == MoveType.CAPTUREMOVE ||
               type == MoveType.PROMOTEPAWNMOVE ||
               type == MoveType.ENPASSANTMOVE;
    }

    /**
     * Determines if a move type is a castling move.
     *
     * @param type The type of the move.
     * @return True if the move is a castling move; false otherwise.
     */
    private static boolean isCastlingMove(MoveType type) {
        return type == MoveType.CASTLEMOVE;
    }
}
