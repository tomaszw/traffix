/*
 * Created on 2004-07-06
 */

package traffix.core.actionframework;

public interface IActionManager {
  void addListener(IActionManagerListener l);

  boolean canRedo();

  boolean canUndo();

  void clearUndoHistory();

  IAction getTopRedoAction();

  IAction getTopUndoAction();

  void redo();

  void removeListener(IActionManagerListener l);

  IAction renameAction(IAction action, final String name);

  void run(IAction action);

  void seqBegin();

  void seqEnd();

  void undo();

}