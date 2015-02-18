/*
 * Created on 2004-09-02
 */

package traffix.ui.sim;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import traffix.Traffix;
import traffix.core.sim.entities.CondClearDetector;
import traffix.core.sim.entities.IDetector;
import traffix.ui.GroupCombo;
import traffix.ui.ScheduleCheckboxTable;
import traffix.ui.VerifiedDialog;

public class CondClearDetectorDialog extends VerifiedDialog {
  private CondClearDetector m_detector;
  private Text m_name;

  public CondClearDetectorDialog(Shell parentShell, CondClearDetector d) {
    super(parentShell);
    m_detector = d;
  }

  public String getValidationError() {
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
  }

  public void toDetector() {
    m_detector.setName(m_name.getText());
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Detektor kasuj¹cy");
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
    
    fromDetector();

    return contents;
  }
}