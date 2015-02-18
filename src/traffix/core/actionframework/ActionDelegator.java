/*
 * Created on 2004-07-10
 */

package traffix.core.actionframework;


public class ActionDelegator implements IAction {
  IAction m_action;

  public ActionDelegator(IAction delegate) {
    m_action = delegate;
  }

  public String getName() {
    return m_action.getName();
  }

  public boolean isUndoable() {
    return m_action.isUndoable();
  }

  public void redo() {
    m_action.redo();
  }

  public void run() {
    m_action.run();
  }

  public void undo() {
    m_action.undo();
  }

}
