/*
 * Created on 2004-09-02
 */

package traffix.ui.sim;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import traffix.Traffix;
import traffix.core.sim.entities.SlowdownDetector;
import traffix.ui.GroupCombo;
import traffix.ui.ScheduleCheckboxTable;
import traffix.ui.VerifiedDialog;

public class SlowdownDetectorDialog extends VerifiedDialog {
  private SlowdownDetector m_detector;
  private Text m_speed;

  public SlowdownDetectorDialog(Shell parentShell, SlowdownDetector d) {
    super(parentShell);
    m_detector = d;
  }

  public String getValidationError() {
    try {
      int i = Integer.parseInt(m_speed.getText());
      if (i <= 0)
        throw new NumberFormatException();
    } catch (NumberFormatException e) {
      return "B³êdna prêdkoœæ";
    }
    return null;
  }

  public void fromDetector() {
    m_speed.setText(Integer.toString(Math.round(m_detector.getSpeed()*3.6f)));
  }

  public void toDetector() {
    m_detector.setSpeed(Integer.parseInt(m_speed.getText())/3.6f);
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Detektor spowalniaj¹cy");
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
    label.setText("Prêdkoœæ max (km/h)");
    m_speed = new Text(contents, SWT.NONE);
    m_speed.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    addVerifiedControl(m_speed);
    
    fromDetector();

    return contents;
  }
}