/*
 * Created on 2004-09-06
 */

package traffix.ui.sim;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import traffix.Traffix;
import traffix.core.schedule.Schedule;
import traffix.core.sim.AccomodationMatrix;
import traffix.ui.ScheduleCheckboxTable;
import traffix.ui.VerifiedDialog;

import java.util.Arrays;

public class AccSelectionDialog extends VerifiedDialog {
  private ScheduleCheckboxTable m_table;

  public AccSelectionDialog(Shell parentShell) {
    super(parentShell);
  }

  private AccomodationMatrix matrix() {
    return Traffix.simScheduleManager().accMatrix();
  }

  public void fromMatrix() {
    m_table.setSchedules(Arrays.asList(matrix().getSchedules()));
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Wybierz programy");
  }

  public void toMatrix() {
    java.util.List schs = m_table.getSchedules();
    matrix().setSchedules((Schedule[]) schs.toArray(new Schedule[schs.size()]));
  }

  public String getValidationError() {
    return null;
  }

  protected Control createDialogArea(Composite parent) {
    Composite contents = (Composite) super.createDialogArea(parent);
    contents.setLayout(new FillLayout());
    m_table = new ScheduleCheckboxTable(contents, SWT.NONE);
    fromMatrix();
    return contents;
  }

  protected void okPressed() {
    toMatrix();
    Traffix.model().setModified(true);
    super.okPressed();
  }

}