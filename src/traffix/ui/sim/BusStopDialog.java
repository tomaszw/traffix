/*
 * Created on 2004-08-31
 */

package traffix.ui.sim;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import traffix.Traffix;
import traffix.core.sim.entities.BusStop;
import traffix.ui.VerifiedDialog;

public class BusStopDialog extends VerifiedDialog {
  private Button m_busCheck, m_trolleyCheck;
  private Text m_loadTimeText;
  private BusStop m_stop;

  public BusStopDialog(Shell parentShell, BusStop stop) {
    super(parentShell);
    m_stop = stop;
  }

  public void fromBusStop() {
    m_busCheck.setSelection(m_stop.m_busStop);
    m_trolleyCheck.setSelection(m_stop.m_trolleyStop);
    m_loadTimeText.setText(Integer.toString((int) m_stop.m_maxUnloadingTime));
  }

  public String getValidationError() {
    try {
      int t = Integer.parseInt(m_loadTimeText.getText());
      if (t < 0)
        throw new NumberFormatException();
    } catch (NumberFormatException e) {
      return "B³êdny czas postoju";
    }
    return null;
  }

  public void toBusStop() {
    m_stop.m_busStop = m_busCheck.getSelection();
    m_stop.m_trolleyStop = m_trolleyCheck.getSelection();
    m_stop.m_maxUnloadingTime = Integer.parseInt(m_loadTimeText.getText());
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Przystanek");
  }

  protected Control createDialogArea(Composite parent) {
    Composite contents = (Composite) super.createDialogArea(parent);
    GridLayout layout = new GridLayout(2, true);
    contents.setLayout(layout);

    GridData data;

    Label label = new Label(contents, SWT.NONE);
    setValidationLabel(label);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    label.setLayoutData(data);

    label = new Label(contents, SWT.NONE);
    label.setText("Autobusowy");

    m_busCheck = new Button(contents, SWT.CHECK);
    m_busCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    label = new Label(contents, SWT.NONE);
    label.setText("Tramwajowy");

    m_trolleyCheck = new Button(contents, SWT.CHECK);
    m_trolleyCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    label = new Label(contents, SWT.NONE);
    label.setText("Czas postoju (s)");

    m_loadTimeText = new Text(contents, SWT.NONE);
    m_loadTimeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    addVerifiedControl(m_loadTimeText);

    fromBusStop();

    return contents;
  }

  protected void okPressed() {
    toBusStop();
    Traffix.model().setModified(true);
    super.okPressed();
  }
}