/*
 * Created on 2004-08-31
 */

package traffix.ui;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import traffix.Traffix;
import traffix.core.VehicleGroup;
import traffix.core.VehicleGroupSet;
import traffix.core.model.Model;

public class GroupSelectionDialog extends VerifiedDialog {
  private GroupCheckboxTable m_groupsTable;
  private VehicleGroupSet m_set = new VehicleGroupSet();

  public GroupSelectionDialog(Shell parentShell) {
    super(parentShell);
  }

  public VehicleGroupSet getGroups() {
    return m_set;
  }

  public String getValidationError() {
    return null;
  }

  public void setGroups(VehicleGroupSet set) {
    m_set.assign(set);
    updateTable();
  }

  protected void okPressed() {
    updateSet();
    super.okPressed();
  }

  protected Control createDialogArea(Composite parent) {
    Composite contents = (Composite) super.createDialogArea(parent);
    contents.setLayout(new GridLayout(3, false));
    m_groupsTable = new GroupCheckboxTable(contents, SWT.NONE);
    m_groupsTable.getTableViewer().addCheckStateListener(new ICheckStateListener() {
      public void checkStateChanged(CheckStateChangedEvent event) {
        updateSet();
      }
    });

    GridData data = new GridData(GridData.FILL_BOTH);//
    data.horizontalSpan = 3;
    m_groupsTable.setLayoutData(data);

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
    m_set.clear();
    updateTable();
  }

  private void selectAll() {
    m_set.clear();
    Model m = Traffix.model();
    for (int i = 0; i < m.getNumGroups(); ++i)
      m_set.add(m.getGroupByIndex(i));
    updateTable();
  }

  private void updateSet() {
    if (m_groupsTable == null)
      return;

    Object[] checked = m_groupsTable.getTableViewer().getCheckedElements();
    m_set.clear();
    for (int i = 0; i < checked.length; i++) {
      m_set.add((VehicleGroup) checked[i]);
    }
  }

  private void updateTable() {
    if (m_groupsTable != null)
      m_groupsTable.getTableViewer().setCheckedElements(m_set.toArray());
  }

}