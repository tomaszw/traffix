/*
 * Created on 2004-07-12
 */

package traffix.core.schedule;

import traffix.Traffix;
import traffix.core.actionframework.Action;
import traffix.core.actionframework.IAction;
import traffix.core.model.Model;

public class ScheduleActions {
  static void fireTempScheduleChanged() {
    Traffix.model().fireUpdated(Model.EVT_CHANGE_TEMPSCHEDULE, null);
  }

  private static class Extend extends Action {
    Schedule m_schedule;
    int m_time;

    public Extend(Schedule s, int t) {
      m_schedule = s;
      m_time = t;
    }

    public String getName() {
      return "Modyfikacja programu";
    }

    public boolean isUndoable() {
      return true;
    }

    public void run() {
      m_schedule.extend(m_time, 1);
      //Traffix.getModel().setModified(true);
      fireTempScheduleChanged();
    }

    public void undo() {
      m_schedule.shrink(m_time, 1);
      //Traffix.getModel().setModified(true);
      fireTempScheduleChanged();
    }
  }

  private static class Scroll extends Action {
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    int m_dir;
    Schedule m_sched;
    int m_jun=-1;
    
    public Scroll(Schedule sched, int dir, int jun) {
      m_sched = sched;
      m_dir = dir;
      m_jun = jun;
    }

    public String getName() {
      return "Przesuniêcie programów";
    }

    public boolean isUndoable() {
      return true;
    }

    public void run() {
      if (m_jun != -1 && m_jun != 0)
        m_sched.moveJunctionZeroMarker(m_jun, m_dir == LEFT ? -1 : 1);
        
      for (int i = 0; i < m_sched.getNumGroups(); ++i) {
        GroupProgram prog = m_sched.getProgram(i);
        if (m_jun != -1 && m_jun != m_sched.getGroup(i).getJunctionIndex())
          continue;
        if (m_dir == LEFT)
          prog.scrollLeft();
        else
          prog.scrollRight();
      }
      //Traffix.getModel().setModified(true);
      fireTempScheduleChanged();
    }

    public void undo() {
      if (m_jun != -1 && m_jun != 0)
        m_sched.moveJunctionZeroMarker(m_jun, m_dir == LEFT ? 1 : -1);

      for (int i = 0; i < m_sched.getNumGroups(); ++i) {
        GroupProgram prog = m_sched.getProgram(i);
        if (m_jun != -1 && m_jun != m_sched.getGroup(i).getJunctionIndex())
          continue;
        if (m_dir == LEFT)
          prog.scrollRight();
        else
          prog.scrollLeft();
      }
      //Traffix.getModel().setModified(true);
      fireTempScheduleChanged();
    }
  }

  private static class Shrink extends Action {
    int[] m_cache;
    Schedule m_schedule;
    int m_time;

    public Shrink(Schedule s, int t) {
      m_schedule = s;
      m_time = t;
    }

    public String getName() {
      return "Modyfikacja programu";
    }

    public boolean isUndoable() {
      return true;
    }

    public void run() {
      m_cache = m_schedule.getColumn(m_time);
      m_schedule.shrink(m_time, 1);
      //Traffix.getModel().setModified(true);
      fireTempScheduleChanged();
    }

    public void undo() {
      m_schedule.extend(m_time, 1);
      m_schedule.putColumn(m_cache, m_time);
      //Traffix.getModel().setModified(true);
      fireTempScheduleChanged();
    }
  }

  public static IAction extend(Schedule s, int time) {
    return new Extend(s, time);
  }

  public static IAction scroll(Schedule s, boolean left) {
    int dir = left ? Scroll.LEFT : Scroll.RIGHT;
    return new Scroll(s, dir, -1);
  }

  public static IAction scrollJunctionGroups(Schedule s, boolean left, int jun) {
    int dir = left ? Scroll.LEFT : Scroll.RIGHT;
    return new Scroll(s, dir, jun);
  }
  public static IAction shrink(Schedule s, int time) {
    return new Shrink(s, time);
  }
}