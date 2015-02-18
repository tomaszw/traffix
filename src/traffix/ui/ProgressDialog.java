/*
 * Created on 2004-08-27
 */

package traffix.ui;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


public class ProgressDialog extends ProgressMonitorDialog {

  public ProgressDialog(Shell parent) {
    super(parent);
  }

  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setText("Informacja o postêpie...");
  }

  protected Control createContents(Composite parent) {
    Control contents = super.createContents(parent);
    if (getButton(CANCEL) != null)
      getButton(CANCEL).setText("Anuluj");
    return contents;
  }
}
