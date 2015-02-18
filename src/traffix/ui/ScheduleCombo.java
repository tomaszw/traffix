/*
 * Created on 2004-09-05
 */

package traffix.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import traffix.Traffix;
import traffix.core.schedule.Schedule;

public class ScheduleCombo extends Composite {
  private Combo m_combo;

  public ScheduleCombo(Composite parent, int style) {
    super(parent, SWT.NONE);
    setLayout(new FillLayout());
    m_combo = new Combo(this, style);

    Schedule[] schs = getSchedules();

    for (int i = 0; i < schs.length; ++i) {
      m_combo.add(schs[i].getName());
    }
    m_combo.select(0);
  }

  public Combo asCombo() {
    return m_combo;
  }

  public Schedule getSelectedSchedule() {
    int i = m_combo.getSelectionIndex();
    if (i == -1)
      return null;
    return Traffix.scheduleBank().get(m_combo.getItem(i));
  }

  public void select(Schedule s) {
    if (s != null) {
      String[] items = m_combo.getItems();
      for (int i = 0; i < items.length; i++) {
        if (items[i].equals(s.getName())) {
          m_combo.select(i);
          return;
        }
      }
    }
    m_combo.select(-1);
  }

  private Schedule[] getSchedules() {
    return Traffix.scheduleBank().getSchedules();
  }
}