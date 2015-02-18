/*
 * Created on 2004-07-10
 */

package traffix.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class ListSelectionDialog extends Dialog {
  int m_index;
  String[] m_items;
  String m_message, m_title;

  public ListSelectionDialog(Shell parentShell, String title, String message) {
    super(parentShell);
    m_title = title;
    m_message = message;
  }

  public int getSelectedIndex() {
    return m_index;
  }

  public void setItems(String[] list) {
    m_items = list;
  }

  public void setMessage(String message) {
    m_message = message;
  }

  public void setSelectedIndex(int index) {
    m_index = index;
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(m_title);
  }

  protected void createButtonsForButtonBar(Composite parent) {
    super.createButtonsForButtonBar(parent);
    getButton(CANCEL).setText("Anuluj");
  }

  protected Control createDialogArea(Composite parent) {
    Composite contents = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(1, false);
    contents.setLayout(layout);

    Label label = new Label(contents, SWT.NONE);
    label.setText(m_message);

    final Combo combo = new Combo(contents, SWT.READ_ONLY);
    combo.setVisibleItemCount(Math.min(m_items.length, 7));
    combo.setItems(m_items);
    combo.select(m_index);
    combo.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        m_index = combo.getSelectionIndex();
      }
    });

    return contents;
  }
}