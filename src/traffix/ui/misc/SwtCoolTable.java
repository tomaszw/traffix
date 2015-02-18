/*
 * Created on 2004-09-03
 */

package traffix.ui.misc;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class SwtCoolTable extends LabelProvider
    implements
      ITableLabelProvider,
      IStructuredContentProvider,
      ICellModifier {
  private String[]    m_columnNames;
  private Table       m_table;
  private TableViewer m_tableViewer;

  public SwtCoolTable(Composite parent, int style, String[] colNames) {
    m_table = new Table(parent, style);
    m_table.setHeaderVisible(true);

    m_columnNames = colNames;

    TableColumn[] columns = new TableColumn[m_columnNames.length];
    for (int i = 0; i < m_columnNames.length; ++i) {
      columns[i] = new TableColumn(m_table, SWT.NONE);
      columns[i].setText(colNames[i]);
    }

    m_tableViewer = new TableViewer(m_table);
    m_tableViewer.setColumnProperties(m_columnNames);
    m_tableViewer.setContentProvider(this);
    m_tableViewer.setLabelProvider(this);
    m_tableViewer.setCellModifier(this);
    m_tableViewer.setCellEditors(createEditors());

    for (int i = 0; i < m_columnNames.length; i++) {
      columns[i].pack();
    }
  }

  protected CellEditor[] createEditors() {
    return new CellEditor[0];
  }
  
  public void addListener(ILabelProviderListener listener) {
  }

  public Table asTable() {
    return m_table;
  }

  public void setInput(Object input) {
    m_tableViewer.setInput(input);
  }
  
  public TableViewer asTableViewer() {
    return m_tableViewer;
  }

  public boolean canModify(Object element, String property) {
    return false;
  }

  public void dispose() {
  }

  public Image getColumnImage(Object element, int columnIndex) {
    return null;
  }

  public String getColumnText(Object element, int columnIndex) {
    return null;
  }

  public Object[] getElements(Object inputElement) {
    return null;
  }

  public Object getValue(Object element, String property) {
    return null;
  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }

  public boolean isLabelProperty(Object element, String property) {
    return false;
  }

  public void modify(Object element, String property, Object value) {
  }

  public void removeListener(ILabelProviderListener listener) {
  }
}