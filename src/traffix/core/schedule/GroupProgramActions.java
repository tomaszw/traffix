/*
 * Created on 2004-07-12
 */

package traffix.core.schedule;

import traffix.Traffix;
import traffix.core.actionframework.Action;
import traffix.core.actionframework.IAction;
import traffix.core.model.Model;

public class GroupProgramActions {
  static void fireTempScheduleChanged() {
    Traffix.model().fireUpdated(Model.EVT_CHANGE_TEMPSCHEDULE, null);
  }

  static private class Copy extends Action {
    GroupProgram m_dest, m_src, m_backup;

    public Copy(GroupProgram dest, GroupProgram src) {
      m_dest = dest;
      m_src = src;
    }

    public String getName() {
      return "Nadpisanie œwiat³a";
    }

    public void run() {
      m_backup = new GroupProgram(m_dest);
      System.arraycopy(m_src.m_data, 0, m_dest.m_data, 0, m_src.m_data.length);
      // Traffix.getModel().setModified(true);
      fireTempScheduleChanged();
    }

    public void undo() {
      System.arraycopy(m_backup.m_data, 0, m_dest.m_data, 0, m_dest.m_data.length);
      // Traffix.getModel().setModified(true);
      fireTempScheduleChanged();
    }
  }

  private static class FloodFill extends Modify {
    int m_light;
    int m_pos;

    public FloodFill(GroupProgram prog, int pos, int light) {
      super(prog);
      m_pos = pos;
      m_light = light;
    }

    public String getName() {
      return "Zamiana œwiat³a";
    }

    public void run() {
      super.run();
      getProgram().floodFill(m_pos, m_light);
      // Traffix.getModel().setModified(true);
      fireTempScheduleChanged();
    }

    public void undo() {
      super.undo();
      // Traffix.getModel().setModified(true);
      fireTempScheduleChanged();
    }
  }

  static private class SetInterval extends Modify {
    int min, max, light;

    public SetInterval(GroupProgram prog, int min, int max, int light) {
      super(prog);
      this.min = min;
      this.max = max;
      this.light = light;
    }

    public String getName() {
      return "Wstawienie œwiat³a";
    }

    public void run() {
      super.run();
      getProgram().set(min, max, light);

      // Traffix.getModel().setModified(true);
      fireTempScheduleChanged();
    }

    public void undo() {
      super.undo();

      // Traffix.getModel().setModified(true);
      fireTempScheduleChanged();
    }
  }

  static private abstract class Modify extends Action {
    private GroupProgram m_program;
    private int[]        m_programBackup;

    public Modify(GroupProgram prog) {
      m_program = prog;
    }

    public String getName() {
      return "Modyfikacja programu";
    }

    public GroupProgram getProgram() {
      return m_program;
    }

    public boolean isUndoable() {
      return true;
    }

    public void run() {
      int len = m_program.getLength();
      m_programBackup = new int[len];
      for (int i = 0; i < len; ++i)
        m_programBackup[i] = m_program.get(i);
    }

    public void undo() {
      m_program.setLength(m_programBackup.length);
      for (int i = 0; i < m_programBackup.length; ++i)
        m_program.set(i, i, m_programBackup[i]);
    }
  }

  static private class Reset extends Modify {
    public Reset(GroupProgram prog) {
      super(prog);
    }

    public String getName() {
      return "Reset programu";
    }

    public void run() {
      super.run();
      getProgram().clear();
      // Traffix.getModel().setModified(true);
      fireTempScheduleChanged();
    }

    public void undo() {
      super.undo();
      // Traffix.getModel().setModified(true);
      fireTempScheduleChanged();
    }
  }

  static private class Scroll extends Action {
    public static final int LEFT  = 0;
    public static final int RIGHT = 1;
    int                     m_dir;
    GroupProgram            m_prog;

    public Scroll(GroupProgram prog, int dir) {
      m_prog = prog;
      m_dir = dir;
    }

    public String getName() {
      return "Przesuniêcie programu";
    }

    public boolean isUndoable() {
      return true;
    }

    public void run() {
      if (m_dir == LEFT)
        m_prog.scrollLeft();
      else
        m_prog.scrollRight();
      // Traffix.getModel().setModified(true);
      fireTempScheduleChanged();
    }

    public void undo() {
      if (m_dir == LEFT)
        m_prog.scrollRight();
      else
        m_prog.scrollLeft();
      // Traffix.getModel().setModified(true);
      fireTempScheduleChanged();
    }
  }

  public static IAction copy(GroupProgram dest, GroupProgram src) {
    return new Copy(dest, src);
  }

  public static IAction floodFill(GroupProgram p, int time, int light) {
    return new FloodFill(p, time, light);
  }

  public static IAction setInterval(GroupProgram p, int beg, int end, int light) {
    return new SetInterval(p, beg, end, light);
  }

  public static IAction reset(GroupProgram p) {
    return new Reset(p);
  }

  public static IAction scroll(GroupProgram p, boolean left) {
    int dir = left ? Scroll.LEFT : Scroll.RIGHT;
    return new Scroll(p, dir);
  }
}