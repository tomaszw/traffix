/*
 * Created on 2004-07-06
 */

package traffix.core.actionframework;

public interface IAction {
  String getName();

  boolean isUndoable();

  void redo();

  void run();

  void undo();
}