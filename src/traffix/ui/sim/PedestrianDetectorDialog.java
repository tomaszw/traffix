/*
 * Created on 2004-09-02
 */

package traffix.ui.sim;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import traffix.Traffix;
import traffix.core.sim.entities.PedestrianDetector;
import traffix.core.sim.entities.IDetector;
import traffix.ui.ScheduleCheckboxTable;
import traffix.ui.ScheduleCombo;
import traffix.ui.VerifiedDialog;
import traffix.ui.GroupCombo;

import java.util.*;

public class PedestrianDetectorDialog extends VerifiedDialog {
  private PedestrianDetector m_detector;
  private ScheduleCheckboxTable m_scheduleTable;
  private Text m_interval;
  private Text m_name;
  private GroupCombo m_clearedBy;

  public PedestrianDetectorDialog(Shell parentShell, PedestrianDetector d) {
    super(parentShell);
    m_detector = d;
  }

  public String getValidationError() {
    try {
      int i = Integer.parseInt(m_interval.getText());
      if (i <= 0)
        throw new NumberFormatException();
    } catch (NumberFormatException e) {
      return "B³êdny odstêp";
    }
    if (m_name.getText().equals(""))
      return "Pusta nazwa";
    java.util.List<IDetector> dets = Traffix.simManager().getDetectors();
    for(IDetector d : dets)
      if (d != m_detector && d.getName().equals(m_name.getText()))
        return "Nazwa ju¿ zajêta";

    return null;
  }

  public void fromDetector() {
    m_name.setText(m_detector.getName());
    m_interval.setText(Integer.toString(m_detector.getInterval()));
    m_scheduleTable.setSchedules(m_detector.getSchedules());
    m_clearedBy.select(m_detector.getClearedBy());
  }

  public void toDetector() {
    m_detector.setName(m_name.getText());
    m_detector.setInterval(Integer.parseInt(m_interval.getText()));
    m_detector.setSchedules(m_scheduleTable.getSchedules());
    m_detector.setClearedBy(m_clearedBy.getSelectedGroup());
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Detektor pieszych");
  }

  protected void okPressed() {
    toDetector();
    Traffix.model().setModified(true);
    super.okPressed();
  }

  protected Control createDialogArea(Composite parent) {
    Composite contents = (Composite) super.createDialogArea(parent);
    GridLayout layout = new GridLayout(2, true);
    contents.setLayout(layout);

    GridData data;

    Label label = new Label(contents, SWT.NONE);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    label.setLayoutData(data);
    setValidationLabel(label);

    label = new Label(contents, SWT.NONE);
    label.setText("Nazwa");
    m_name = new Text(contents, SWT.NONE);
    m_name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    addVerifiedControl(m_name);
    
    label = new Label(contents, SWT.NONE);
    label.setText("Odstêp (s)");

    m_interval = new Text(contents, SWT.NONE);
    m_interval.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    addVerifiedControl(m_interval);

    label = new Label(contents, SWT.NONE);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    label.setLayoutData(data);
    label.setText("Przywo³ywane programy");

    m_scheduleTable = new ScheduleCheckboxTable(contents, SWT.NONE);
    data = new GridData(GridData.FILL_BOTH);
    data.horizontalSpan = 2;
    m_scheduleTable.setLayoutData(data);

    label = new Label(contents, SWT.NONE);
    label.setText("Grupa kasuj¹ca");

    m_clearedBy = new GroupCombo(contents, SWT.READ_ONLY, false);
    m_clearedBy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    fromDetector();

    return contents;
  }
}