
package traffix.ui.sim;

import traffix.ui.VerifiedDialog;
import traffix.ui.FreezeLinksCheckboxTable;
import traffix.core.sim.entities.TransitDetector;
import traffix.core.sim.entities.FreezeLink;
import traffix.Traffix;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import java.util.List;

/**
 * Author: tomek
 * Date: 2005-07-11
 */
public class TransitDetectorLinksDialog extends VerifiedDialog {
  private TransitDetector m_detector;
  private List<FreezeLink> m_freezeLinks;
  private FreezeLinksCheckboxTable m_linksTable;

  public TransitDetectorLinksDialog(Shell parentShell, TransitDetector d, List<FreezeLink> links) {
    super(parentShell);
    m_detector = d;
    m_freezeLinks = links;
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Po³¹czenia zamra¿ania");
  }

  public String getValidationError() {
    return null;
  }

  private void toLinks() {
    List<FreezeLink> links = m_linksTable.getFreezeLinks();
    m_freezeLinks.clear();
    for (FreezeLink l : links)
      m_freezeLinks.add(l);
  }

  private void fromLinks() {
    m_linksTable.setFreezeLinks(m_freezeLinks);
    m_linksTable.asViewer().refresh();
  }

  protected void okPressed() {
    toLinks();
    Traffix.model().setModified(true);
    super.okPressed();
  }

  protected Control createDialogArea(Composite parent) {
    Composite contents = (Composite) super.createDialogArea(parent);
    GridLayout layout = new GridLayout(1, true);
    contents.setLayout(layout);

    GridData data;

    Label label = new Label(contents, SWT.NONE);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 1;
    label.setLayoutData(data);
    setValidationLabel(label);

    m_linksTable = new FreezeLinksCheckboxTable(contents, SWT.FULL_SELECTION);
    data = new GridData(GridData.FILL_BOTH);
    data.horizontalSpan = 1;
    m_linksTable.asTable().setLayoutData(data);
    m_linksTable.asTable().setHeaderVisible(true);

    fromLinks();

    return contents;
  }
}
