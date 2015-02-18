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
import java.util.List;

public class ObjCheckboxTable<T> extends Composite {
  private Table m_table;
  private CheckboxTableViewer m_viewer;
  //private java.util.List<T> m_selection = new ArrayList<T>();

  class ObjLabelProvider extends LabelProvider implements ITableLabelProvider {
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }

    public String getColumnText(Object element, int columnIndex) {
      if (columnIndex == 0)
        return element.toString();
      return "";
    }
  }

  public Table getTable() {
    return m_table;
  }

  public CheckboxTableViewer getTableViewer() {
    return m_viewer;
  }

  public java.util.List<T> getSelection() {
    Object[] elems = m_viewer.getCheckedElements();
    return (List<T>) Arrays.asList(elems);
  }

  public void setSelection(java.util.List<T> sel) {
    m_viewer.setCheckedElements(sel.toArray());
  }

  public Point computeSize(int wHint, int hHint, boolean changed) {
    if (wHint == SWT.DEFAULT)
      wHint = 100;
    if (hHint == SWT.DEFAULT)
      hHint = 120;
    return new Point(wHint, hHint);
  }

  public ObjCheckboxTable(Composite parent, int style) {
    super(parent, style);
    setLayout(new FillLayout());
    TableColumn col;
    m_table = new Table(this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL
      | SWT.CHECK | SWT.FULL_SELECTION);
    m_table.setHeaderVisible(true);
    col = new TableColumn(m_table, SWT.LEFT);
    col.setText("");
    col.setWidth(100);

    m_viewer = new CheckboxTableViewer(m_table);
    m_viewer.setContentProvider(createContentProvider());
    m_viewer.setLabelProvider(createLabelProvider());
  }

  private IContentProvider createContentProvider() {
    return new IStructuredContentProvider() {
      public void dispose() {
      }

      public Object[] getElements(Object inputElement) {
        List objs = (List) inputElement;
        return objs.toArray();
      }

      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      }
    };
  }

  private ILabelProvider createLabelProvider() {
    return new ObjLabelProvider();
  }
}