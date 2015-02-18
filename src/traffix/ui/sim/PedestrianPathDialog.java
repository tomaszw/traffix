/*
 * Created on 2004-08-24
 */

package traffix.ui.sim;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import traffix.Traffix;
import traffix.core.sim.entities.PedestrianPath;
import traffix.ui.VerifiedDialog;


public class PedestrianPathDialog extends VerifiedDialog {
  private PedestrianPath m_path;
  Combo m_groups;

  public PedestrianPathDialog(Shell parentShell, PedestrianPath path) {
    super(parentShell);
    m_path = path;
  }

  public String getValidationError() {
    return null;
  }

  public void toPedestrianPath() {
    int gp = m_groups.getSelectionIndex();
    if (gp != -1)
      m_path.setGroup(Traffix.model().getGroupByIndex(gp));
  }

  private GridData createGriddata() {
    return new GridData(GridData.FILL_BOTH);
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Przej�cie dla pieszych");
  }

  protected void okPressed() {
    toPedestrianPath();
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
    label.setText("Grupa piesza");

    m_groups = new Combo(contents, SWT.READ_ONLY);
    m_groups.setLayoutData(createGriddata());
    for (int i = 0; i < Traffix.model().getNumGroups(); ++i)
      m_groups.add(Traffix.model().getGroupByIndex(i).getElectricName());
    m_groups.select(Traffix.model().getGroupIndex(m_path.getGroup()));

    return contents;
  }

}
