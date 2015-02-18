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
import traffix.core.model.Model;
import traffix.core.schedule.Schedule;

import java.util.ArrayList;
import java.util.Arrays;

public class ScheduleCheckboxTable extends Composite {
  private Table m_scheduleTable;
  private CheckboxTableViewer m_scheduleTableViewer;
  private java.util.List m_selectedSchedules = new ArrayList();

  class ScheduleLabelProvider extends LabelProvider implements ITableLabelProvider {
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }

    public String getColumnText(Object element, int columnIndex) {
      Schedule sch = (Schedule) element;
      if (columnIndex == 0)
        return sch.getName();
      return "";
    }
  }

  public Table getTable() {
    return m_scheduleTable;
  }

  public CheckboxTableViewer getTableViewer() {
    return m_scheduleTableViewer;
  }

  public java.util.List getSchedules() {
    Object[] elems = m_scheduleTableViewer.getCheckedElements();
    return Arrays.asList(elems);
  }

  public void setSchedules(java.util.List schedules) {
    m_scheduleTableViewer.setCheckedElements(schedules.toArray());
  }

  public Point computeSize(int wHint, int hHint, boolean changed) {
    if (wHint == SWT.DEFAULT)
      wHint = 150;
    if (hHint == SWT.DEFAULT)
      hHint = 160;
    return new Point(wHint, hHint);
  }

  public ScheduleCheckboxTable(Composite parent, int style) {
    super(parent, style);
    setLayout(new FillLayout());
    TableColumn col;
    m_scheduleTable = new Table(this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL
      | SWT.CHECK | SWT.FULL_SELECTION);
    m_scheduleTable.setHeaderVisible(true);
    //m_groupsTable.setLinesVisible(true);
    //m_groupsTable.setSize(200,200);
    col = new TableColumn(m_scheduleTable, SWT.LEFT);
    col.setText("Program");
    //col.setWidth(150);
    m_scheduleTableViewer = new CheckboxTableViewer(m_scheduleTable);
    m_scheduleTableViewer.setContentProvider(createContentProvider());
    m_scheduleTableViewer.setLabelProvider(createLabelProvider());
    m_scheduleTableViewer.setInput(Traffix.model());
    col.pack();
  }

  private IContentProvider createContentProvider() {
    return new IStructuredContentProvider() {
      public void dispose() {
      }

      public Object[] getElements(Object inputElement) {
        Model model = (Model) inputElement;
        Schedule[] schedules = model.getScheduleBank().getSchedules();
        return schedules;
      }

      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      }
    };
  }

  private ILabelProvider createLabelProvider() {
    return new ScheduleLabelProvider();
  }
}