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
import traffix.core.VehicleGroupSet;
import traffix.core.VehicleGroup;

import java.util.Iterator;

public class GroupCombo extends Composite {
  private Combo m_combo;
  private boolean m_allowEmptySel;

  public GroupCombo(Composite parent, int style, boolean allowEmptySel) {
    super(parent, SWT.NONE);
    setLayout(new FillLayout());
    m_combo = new Combo(this, style);

    m_allowEmptySel = allowEmptySel;
    if (allowEmptySel)
      m_combo.add("");
    VehicleGroupSet gps = Traffix.model().getGroups();
    for (Iterator<VehicleGroup> it = gps.iterator(); it.hasNext(); ) {
      VehicleGroup g = it.next();
      m_combo.add(g.getElectricName());
    }
    m_combo.select(0);
  }

  public Combo asCombo() {
    return m_combo;
  }

  public VehicleGroup getSelectedGroup() {
    int i = m_combo.getSelectionIndex();
    if (i == -1)
      return null;
    if (m_allowEmptySel && i==0) return null;
    return Traffix.model().getGroupByIndex(m_allowEmptySel?i-1:i);
  }

  public void select(VehicleGroup g) {
    if (g != null) {
      m_combo.select(m_allowEmptySel?g.getIndex()+1:g.getIndex());
    }
    m_combo.select(-1);
  }
}