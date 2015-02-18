/*
 * Created on 2005-09-24
 */

package traffix.ui.sim;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import traffix.Traffix;
import traffix.ui.VerifiedDialog;

public class NewAccidentDialog extends VerifiedDialog {
  Text m_name;
  public String name;
  
  public NewAccidentDialog(Shell parentShell) {
    super(parentShell);
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Nowy wypadek");
  }
  
  @Override
  public String getValidationError() {
    String n = m_name.getText();
    if (n.equals(""))
      return "Pusta nazwa";
    for (String n2 : Traffix.model().getSimManagerNames()) {
      if (n.equals(n2))
        return "Nazwa nie jest unikalna";
    }
    return null;
  }

  @Override
  protected void okPressed() {
    name = m_name.getText();
    super.okPressed();
  }
  
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite contents = (Composite) super.createDialogArea(parent);
    contents.setLayout(new GridLayout(2, true));
    Label lab = new Label(contents, SWT.NONE);
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.horizontalSpan = 2;
    lab.setLayoutData(gd);
    setValidationLabel(lab);
    new Label(contents, SWT.NONE).setText("Nazwa");
    m_name = new Text(contents, SWT.NONE);
    addVerifiedControl(m_name);
    return contents;
  }

  @Override
  protected Control createContents(Composite parent) {
    Control c = super.createContents(parent);
    getButton(CANCEL).setText("Anuluj");
    return c;
  }
  
  
}
