/*
 * Created on 2004-08-31
 */

package traffix.ui;

import java.util.*;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class ObjSelectionDialog<T> extends VerifiedDialog {
  private ObjCheckboxTable<T> m_table;
  private Set<T> m_selected = new HashSet<T>();
  private java.util.List<T> m_objs = new ArrayList<T>();
  
  public ObjSelectionDialog(Shell parentShell, java.util.List<T> objs) {
    super(parentShell);
    m_objs = new ArrayList<T>(objs);
    Collections.sort(m_objs, new Comparator<T>() {

      public int compare(T o1, T o2) {
        return o1.toString().compareTo(o2.toString());
      }
    });
  }

  public Set<T> getSelection() {
    return m_selected;
  }
  
  public void setSelection(Set<T> set) {
    m_selected = new HashSet<T>(set);
    updateTable();
  }

  public String getValidationError() {
    return null;
  }

  protected void okPressed() {
    updateSet();
    super.okPressed();
  }

  protected Control createDialogArea(Composite parent) {
    Composite contents = (Composite) super.createDialogArea(parent);
    contents.setLayout(new GridLayout(3, false));
    m_table = new ObjCheckboxTable<T>(contents, SWT.NONE);
    m_table.getTableViewer().addCheckStateListener(new ICheckStateListener() {
      public void checkStateChanged(CheckStateChangedEvent event) {
        updateSet();
      }
    });
    m_table.getTableViewer().setInput(new ArrayList<T>(m_objs));
    
    GridData data = new GridData(GridData.FILL_BOTH);//
    data.horizontalSpan = 3;
    m_table.setLayoutData(data);

    Button btn = new Button(contents, SWT.NONE);
    btn.setText("Zaznacz");
    btn.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        selectAll();
      }
    });

    btn = new Button(contents, SWT.NONE);
    btn.setText("Odznacz");
    btn.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        deselectAll();
      }
    });

    Label label = new Label(contents, SWT.NONE);
    label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    updateTable();

    return contents;
  }

  private void deselectAll() {
    m_selected.clear();
    updateTable();
  }

  private void selectAll() {
    m_selected = new HashSet<T>(m_objs);
    updateTable();
  }

  private void updateSet() {
    if (m_table == null)
      return;

    Object[] checked = m_table.getTableViewer().getCheckedElements();
    m_selected.clear();
    for (int i = 0; i < checked.length; i++) {
      m_selected.add((T) checked[i]);
    }
  }

  private void updateTable() {
    if (m_table != null)
      m_table.getTableViewer().setCheckedElements(m_selected.toArray());
  }
}