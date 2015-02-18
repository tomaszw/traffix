/*
 * Created on 2004-07-06
 */

package traffix.core.actionframework;

import java.util.Vector;

public class ActionManager implements IActionManager {
  public static final int MAX_UNDO_SIZE = 99;

  private Vector<IActionManagerListener> m_amListeners = new Vector<IActionManagerListener>();
  private boolean m_recordingSeq = false;
  private Vector<IAction> m_redoList = new Vector<IAction>();
  private Sequence m_seq;
  private Vector<IAction> m_undoList = new Vector<IAction>();

  static class Sequence extends Action {
    private Vector<IAction> m_actions = new Vector<IAction>();

    public Sequence() {
    }

    public void add(IAction a) {
      m_actions.add(a);
    }

    public String getName() {
      if (m_actions.size() != 0)
        return getAction(0).getName();
      return "Sekwencja";
    }

    public int getSize() {
      return m_actions.size();
    }

    public boolean isUndoable() {
      return true;
    }

    public void run() {
      for (int i = 0; i < m_actions.size(); ++i) {
        getAction(i).run();
      }
    }

    public void undo() {
      for (int i = m_actions.size() - 1; i >= 0; --i) {
        getAction(i).undo();
      }
    }

    private IAction getAction(int i) {
      return m_actions.get(i);
    }
  }

  public IAction renameAction(IAction action, final String name) {
    return new ActionDelegator(action) {
      public String getName() {
        return name;
      }
    };
  }

  public void addListener(IActionManagerListener l) {
    m_amListeners.add(l);
  }

  public boolean canRedo() {
    return !m_redoList.isEmpty();
  }

  public boolean canUndo() {
    return !m_undoList.isEmpty() || (m_recordingSeq && m_seq.getSize() != 0);
  }

  public void clearUndoHistory() {
    m_undoList.clear();
    m_redoList.clear();
    m_recordingSeq = false;
  }

  public void flush() {
    if (m_undoList.size() > MAX_UNDO_SIZE) {
      while (m_undoList.size() != MAX_UNDO_SIZE)
        m_undoList.remove(m_undoList.size() - 1);
    }
  }

  public IAction getTopRedoAction() {
    if (canRedo())
      return m_redoList.get(0);
    return null;
  }

  public IAction getTopUndoAction() {
    if (m_recordingSeq && m_seq.getSize() != 0)
      return m_seq;
    if (!m_undoList.isEmpty())
      return m_undoList.get(0);
    return null;
  }

  public void redo() {
    seqEnd();
    if (canRedo()) {
      IAction action = m_redoList.remove(0);
      action.redo();
      m_undoList.add(0, action);

      for (int i = 0; i < getNumListeners(); ++i)
        getListener(i).redoPerformed();
    }
  }

  public void removeListener(IActionManagerListener l) {
    m_amListeners.remove(l);
  }

  public void run(IAction action) {
    m_redoList.clear();
    action.run();
    if (action.isUndoable()) {
      if (m_recordingSeq)
        m_seq.add(action);
      else
        m_undoList.add(0, action);
    }
    flush();
    for (int i = 0; i < getNumListeners(); ++i)
      getListener(i).actionPerformed();
  }

  public void seqBegin() {
    if (!m_recordingSeq) {
      m_recordingSeq = true;
      m_seq = new Sequence();
    }
  }

  public void seqEnd() {
    if (m_recordingSeq) {
      m_recordingSeq = false;
      m_undoList.add(0, m_seq);
      m_seq = null;
    }
  }

  public void undo() {
    seqEnd();
    if (canUndo()) {
      IAction action = m_undoList.remove(0);
      action.undo();
      m_redoList.add(0, action);
      for (int i = 0; i < getNumListeners(); ++i)
        getListener(i).undoPerformed();
    }
  }

  private IActionManagerListener getListener(int i) {
    return m_amListeners.get(i);
  }

  private int getNumListeners() {
    return m_amListeners.size();
  }
}