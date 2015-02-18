/*
 * Created on 2004-08-24
 */

package traffix.ui.sim;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import traffix.Traffix;
import traffix.core.sim.entities.Light;
import traffix.ui.VerifiedDialog;


public class LightDialog extends VerifiedDialog {
  private Light m_light;
  Combo m_groups;

  public LightDialog(Shell parentShell, Light light) {
    super(parentShell);
    m_light = light;
  }

  public String getValidationError() {
    return null;
  }

  public void toLight() {
    int gp = m_groups.getSelectionIndex();
    if (gp != -1)
      m_light.setGroup(Traffix.model().getGroupByIndex(gp));
  }

  private GridData createGriddata() {
    return new GridData(GridData.FILL_BOTH);
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Sygnalizator drogowy");
  }

  protected void okPressed() {
    toLight();
    Traffix.model().setModified(true);
    super.okPressed();
  }

  protected Control createContents(Composite parent) {
    Control res = super.createContents(parent);
    getButton(CANCEL).setText("Anuluj");
    return res;
  }

  protected Control createDialogArea(Composite parent) {
    Composite contents = (Composite) super.createDialogArea(parent);
    GridLayout layout = new GridLayout(2, true);
    contents.setLayout(layout);

    GridData data;

    Label label = new Label(contents, SWT.NONE);
    data = createGriddata();
    data.horizontalSpan = 2;
    label.setLayoutData(data);
    setValidationLabel(label);

    label = new Label(contents, SWT.NONE);
    label.setText("Grupa steruj¹ca");

    m_groups = new Combo(contents, SWT.READ_ONLY);
    m_groups.setLayoutData(createGriddata());
    for (int i = 0; i < Traffix.model().getNumGroups(); ++i)
      m_groups.add(Traffix.model().getGroupByIndex(i).getElectricName());
    m_groups.select(Traffix.model().getGroupIndex(m_light.getGroup()));

    return contents;
  }

}
