/*
 * Created on 2006-01-27
 */

package traffix.ui.sim;

import java.util.*;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

import traffix.Traffix;
import traffix.core.sim.entities.*;
import traffix.ui.Colors;
import traffix.ui.Images;
import traffix.ui.VerifiedDialog;

public class MobileToDetectorAssignDialog extends VerifiedDialog {
  private List<IDetector> m_detectors;
  private AssignmentTable m_table;

  public MobileToDetectorAssignDialog(Shell parent) {
    super(parent);

    setShellStyle(getShellStyle() | SWT.TITLE | SWT.MIN | SWT.MAX | SWT.RESIZE
        | SWT.CLOSE);
    m_detectors = Traffix.simManager().getDetectors();
    Iterator<IDetector> iter = m_detectors.iterator();
    while (iter.hasNext()) {
      IDetector d = iter.next();
      if (!(d instanceof TransitDetector || d instanceof PresenceDetector || d instanceof CondClearDetector))
        iter.remove();
    }
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Reakcje detektorów na pojazdy");
  }

  @Override
  public String getValidationError() {
    return null;
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite contents = (Composite) super.createDialogArea(parent);
    contents.setLayout(new FillLayout());
    m_table = new AssignmentTable(contents);
    m_table.viewer().setInput(m_detectors);

    return contents;
  }
}

class AssignmentTable {
  static String[] s_tableCols   = { "Detektor", "N", "C", "A", "T", "P" };
  static int[]    s_tableWidths = { 100, 16, 16, 16, 16, 16 };
  List<IDetector> m_detectors;
  Table           m_table;
  TableViewer     m_viewer;

  class MyContentProvider implements IStructuredContentProvider {
    public void dispose() {
    }

    public Object[] getElements(Object inputElement) {
      List<Object> elems = new ArrayList<Object>((List<Object>) inputElement);
      return (Object[]) elems.toArray(new Object[elems.size()]);
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      m_detectors = (List<IDetector>) newInput;
    }
  }

  class MyLabelProvider extends LabelProvider
      implements
        ITableLabelProvider,
        IColorProvider {
    public Image getColumnImage(Object element, int columnIndex) {

      IDetector d = (IDetector) element;
      Image ch = Images.get("icons/plus.gif");
      Image no = null;
      switch (columnIndex) {
        // case 0:
        // return d.getName();
        case 1 :
          return d.isReactingToMobileType(IMobile.NormalVehicle) ? ch : no;
        case 2 :
          return d.isReactingToMobileType(IMobile.HeavyVehicle) ? ch : no;
        case 3 :
          return d.isReactingToMobileType(IMobile.Bus) ? ch : no;
        case 4 :
          return d.isReactingToMobileType(IMobile.Trolley) ? ch : no;
        case 5 :
          return d.isReactingToMobileType(IMobile.Pedestrian) ? ch : no;
      }
      return null;
    }

    public String getColumnText(Object element, int columnIndex) {
      IDetector d = (IDetector) element;
      switch (columnIndex) {
        case 0 :
          return d.getName();
      }
      return null;
      // case 1:
      // return d.isReactingToMobileType(IMobile.T_NORMAL) ? "T" : "";
      // case 2:
      // return d.isReactingToMobileType(IMobile.T_HEAVY) ? "T" : "";
      // case 3:
      // return d.isReactingToMobileType(IMobile.T_BUS) ? "T" : "";
      // case 4:
      // return d.isReactingToMobileType(IMobile.T_TROLLEY) ? "T" : "";
      // }
      // return null;
    }

    public Color getForeground(Object element) {
      return Colors.system(SWT.COLOR_BLACK);
    }

    public Color getBackground(Object element) {
      return Colors.system(SWT.COLOR_WHITE);
    }
  }

  class MyModifier implements ICellModifier {
    public boolean canModify(Object element, String property) {
      int col = Arrays.asList(s_tableCols).indexOf(property);
      if (col > 0)
        return true;
      return false;
    }

    public Object getValue(Object element, String property) {
      int col = Arrays.asList(s_tableCols).indexOf(property);
      IDetector d = (IDetector) element;
      switch (col) {
        case 1 :
          return d.isReactingToMobileType(IMobile.NormalVehicle);
        case 2 :
          return d.isReactingToMobileType(IMobile.HeavyVehicle);
        case 3 :
          return d.isReactingToMobileType(IMobile.Bus);
        case 4 :
          return d.isReactingToMobileType(IMobile.Trolley);
        case 5 :
          return d.isReactingToMobileType(IMobile.Pedestrian);
      }
      return null;
    }

    public void modify(Object element, String property, Object value) {
      if (element instanceof Item)
        element = ((Item) element).getData();
      int col = Arrays.asList(s_tableCols).indexOf(property);
      IDetector d = (IDetector) element;
      boolean b = (Boolean) value;
      switch (col) {
        case 1 :
          d.setReactingToMobileType(IMobile.NormalVehicle, b);
          break;
        case 2 :
          d.setReactingToMobileType(IMobile.HeavyVehicle, b);
          break;
        case 3 :
          d.setReactingToMobileType(IMobile.Bus, b);
          break;
        case 4 :
          d.setReactingToMobileType(IMobile.Trolley, b);
          break;
        case 5 :
          d.setReactingToMobileType(IMobile.Pedestrian, b);
          break;
      }
      m_table.setSelection(-1);
      viewer().refresh(element);
    }
  }

  public AssignmentTable(Composite parent) {
    m_table = new Table(parent, SWT.FULL_SELECTION | SWT.BORDER);
    m_table.setHeaderVisible(true);
    TableColumn col;
    for (int i = 0; i < s_tableCols.length; i++) {
      col = new TableColumn(m_table, SWT.NONE);
      col.setText(s_tableCols[i]);
      col.setWidth(s_tableWidths[i]);
    }
    // m_table.setLinesVisible(true);
    // m_table.addPaintListener(new PaintListener() {
    // public void paintControl(PaintEvent e) {
    // if (m_table.getItemCount() == 0)
    // return;
    // Rectangle rc1 = m_table.getItem(m_table.getItemCount()-1).getBounds(1);
    // Rectangle rc2 = m_table.getItem(m_table.getItemCount()-1).getBounds(7);
    // rc1.width = rc2.x+rc2.width-rc1.x;
    // e.gc.setLineWidth(2);
    // e.gc.drawLine(rc1.x, rc1.y, rc1.x+rc1.width, rc1.y);
    // e.gc.drawLine(rc1.x, rc1.y+rc1.height, rc1.x+rc1.width, rc1.y+rc1.height);
    // e.gc.setLineWidth(1);
    // }
    //    
    // });

    m_viewer = new TableViewer(m_table);
    m_viewer.setColumnProperties(s_tableCols);
    m_viewer.setContentProvider(new MyContentProvider());
    m_viewer.setLabelProvider(new MyLabelProvider());
    m_viewer.setCellModifier(new MyModifier());
    CellEditor[] editors = new CellEditor[6];
    editors[1] = new CheckboxCellEditor(m_table);
    editors[2] = new CheckboxCellEditor(m_table);
    editors[3] = new CheckboxCellEditor(m_table);
    editors[4] = new CheckboxCellEditor(m_table);
    editors[5] = new CheckboxCellEditor(m_table);
    m_viewer.setCellEditors(editors);
  }

  public Table table() {
    return m_table;
  }

  public TableViewer viewer() {
    return m_viewer;
  }
}
