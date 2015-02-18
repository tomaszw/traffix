/*
 * Created on 2004-07-07
 */

package traffix.core.actionframework;


public class Action implements IAction {
  public boolean isUndoable() {
    return true;
  }

  public void run() {
  }

  public void undo() {
  }

  public void redo() {
    run();
  }

  public String getName() {
    return "Akcja";
  }

}
