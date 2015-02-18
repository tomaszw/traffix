/*
 * Created on 2004-08-25
 */

package traffix.ui;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import traffix.Traffix;
import traffix.core.VehicleGroup;
import traffix.core.model.Model;

import java.util.ArrayList;
import java.util.Arrays;

public class GroupCheckboxTable extends Composite {
  private Table m_groupsTable;
  private CheckboxTableViewer m_groupsTableViewer;
  private java.util.List m_selectedGroups = new ArrayList();

  class GroupLabelProvider extends LabelProvider implements ITableLabelProvider {
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }

    public String getColumnText(Object element, int columnIndex) {
      traffix.core.VehicleGroup group = (VehicleGroup) element;
      if (columnIndex == 0)
        return group.getElectricName();
      return "";
    }
  }

  public Table getTable() {
    return m_groupsTable;
  }

  public CheckboxTableViewer getTableViewer() {
    return m_groupsTableViewer;
  }

  public java.util.List getGroups() {
    Object[] elems = m_groupsTableViewer.getCheckedElements();
    return Arrays.asList(elems);
  }

  public void setGroups(java.util.List groups) {
    m_groupsTableViewer.setCheckedElements(groups.toArray());
  }

  public Point computeSize(int wHint, int hHint, boolean changed) {
    if (wHint == SWT.DEFAULT)
      wHint = 100;
    if (hHint == SWT.DEFAULT)
      hHint = 120;
    return new Point(wHint, hHint);
  }

  public GroupCheckboxTable(Composite parent, int style) {
    super(parent, style);
    setLayout(new FillLayout());
    TableColumn col;
    m_groupsTable = new Table(this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL
      | SWT.CHECK | SWT.FULL_SELECTION);
    m_groupsTable.setHeaderVisible(true);
    //m_groupsTable.setLinesVisible(true);
    //m_groupsTable.setSize(200,200);
    col = new TableColumn(m_groupsTable, SWT.LEFT);
    col.setText("Grupa");
    col.setWidth(50);

    m_groupsTableViewer = new CheckboxTableViewer(m_groupsTable);
    m_groupsTableViewer.setContentProvider(createContentProvider());
    m_groupsTableViewer.setLabelProvider(createLabelProvider());
    m_groupsTableViewer.setInput(Traffix.model());
  }

  private IContentProvider createContentProvider() {
    return new IStructuredContentProvider() {
      public void dispose() {
      }

      public Object[] getElements(Object inputElement) {
        Model model = (Model) inputElement;
        traffix.core.VehicleGroup[] groups = new traffix.core.VehicleGroup[model.getNumGroups()];
        for (int i = 0; i < groups.length; ++i)
          groups[i] = model.getGroupByIndex(i);
        return groups;
      }

      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      }
    };
  }

  private ILabelProvider createLabelProvider() {
    return new GroupLabelProvider();
  }
}