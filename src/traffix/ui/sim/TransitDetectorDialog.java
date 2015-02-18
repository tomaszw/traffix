/*
 * Created on 2004-09-02
 */

package traffix.ui.sim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import traffix.Traffix;
import traffix.core.schedule.Schedule;
import traffix.core.sim.entities.FreezeLink;
import traffix.core.sim.entities.IDetector;
import traffix.core.sim.entities.TransitDetector;
import traffix.core.sim.entities.TransitDetector.Entry;
import traffix.ui.VerifiedDialog;
import traffix.ui.misc.SwtCoolTable;

public class TransitDetectorDialog extends VerifiedDialog {
  private Text m_name;
  private Text m_defFreezeTime;
  private TransitDetector m_detector;
  private String[] m_columnNames = {"Zamra¿any program", "sekunda", "Tmax", "Po³ "};
  private List<Entry> m_entries = new ArrayList<Entry>();
  private DetectorDataTable m_ddTable;


  class LinkedDetectorsEditor extends DialogCellEditor {
    public LinkedDetectorsEditor(Composite parent) {
      super(parent);
    }

    protected Object openDialogBox(Control cellEditorWindow) {
      List<FreezeLink> links = (List<FreezeLink>) doGetValue();
      TransitDetectorLinksDialog dlg = new TransitDetectorLinksDialog(getShell(), m_detector, links);
      dlg.open();

      return links;
    }

    protected void updateContents(Object value) {
      super.updateContents(value);
      List<FreezeLink> links = (List<FreezeLink>) value;
      if (value != null)
        getDefaultLabel().setText(Integer.toString(links.size()));
    }
  }

  class DetectorDataTable extends SwtCoolTable {
    public DetectorDataTable(Composite parent, int style) {
      super(parent, style, m_columnNames);
      setInput(TransitDetectorDialog.this);
    }

    protected CellEditor[] createEditors() {
      CellEditor[] editors = new CellEditor[4];
      editors[0] = new ComboBoxCellEditor(asTable(), getScheduleNames());
      editors[1] = new TextCellEditor(asTable());
      editors[2] = new TextCellEditor(asTable());
      editors[3] = new LinkedDetectorsEditor(asTable());
      return editors;
    }

    public String getColumnText(Object element, int columnIndex) {
      TransitDetector.Entry e = (Entry) element;
      switch (columnIndex) {
      case 0:
        return e.schedule.getName();
      case 1:
        return Integer.toString(e.sec + 1);
      case 2:
        return Integer.toString(e.tmax);
      case 3:
        return Integer.toString(e.freezeLinks.size());
      }
      return null;
    }

    public Object[] getElements(Object inputElement) {
      return ((TransitDetectorDialog) inputElement).m_entries.toArray();
    }

    public boolean canModify(Object element, String property) {
      return true;
    }

    public Object getValue(Object element, String property) {
      int col = Arrays.asList(m_columnNames).indexOf(property);
      TransitDetector.Entry e = (Entry) element;
      switch (col) {
      case 0:
        return new Integer(Traffix.scheduleBank().getScheduleIndex(e.schedule));
      case 1:
        return Integer.toString(e.sec + 1);
      case 2:
        return Integer.toString(e.tmax);
      case 3:
        return e.freezeLinks;
      }

      return null;
    }

    public void modify(Object element, String property, Object value) {
      int col = Arrays.asList(m_columnNames).indexOf(property);
      if (element instanceof Item)
        element = ((Item) element).getData();

      TransitDetector.Entry e = (Entry) element;
      switch (col) {
      case 0:
        int idx = ((Integer) value).intValue();
        if (idx != -1) {
          Schedule s = Traffix.scheduleBank().getSchedules()[idx];
          e.schedule = s;
        }
        break;
      case 1:
        try {
          int sec = Integer.parseInt((String) value);
          if (sec >= 1)
            e.sec = sec - 1;
        } catch (NumberFormatException ex) {
        }
        break;
      case 2:
        try {
          int tmax = Integer.parseInt((String) value);
          if (tmax >= 0) {
            e.tmax = tmax;
          }
        } catch (NumberFormatException ex) {
        }
      case 3:
        break;
      }
      asTableViewer().refresh();
    }
  }


  public TransitDetectorDialog(Shell parentShell, TransitDetector d) {
    super(parentShell);
    m_detector = d;
  }

  public String getValidationError() {
    try {
      int v = Integer.parseInt(m_defFreezeTime.getText());
      if (v < 0)
        throw new NumberFormatException();
    } catch (NumberFormatException e) {
      return "B³êdny czas przed³u¿ania";
    }

    if (m_name.getText().equals(""))
      return "Pusta nazwa";
    List<IDetector> dets = Traffix.simManager().getDetectors();
    for (IDetector d : dets)
      if (d != m_detector && d.getName().equals(m_name.getText()))
        return "Nazwa ju¿ zajêta";
    return null;
  }

  public void fromDetector() {
    m_entries = m_detector.getEntries();
    m_ddTable.asTableViewer().refresh();

    m_defFreezeTime.setText(Integer.toString((int) m_detector.getFreezeTime()));
    m_name.setText(m_detector.getName());
  }

  public void toDetector() {
    m_detector.setEntries(m_entries);
    m_detector.setFreezeTime(Integer.parseInt(m_defFreezeTime.getText()));
    m_detector.setName(m_name.getText());
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Detektor tranzytu");
  }

  protected void okPressed() {
    toDetector();
    Traffix.model().setModified(true);
    super.okPressed();
  }

  protected Control createDialogArea(Composite parent) {
    Composite contents = (Composite) super.createDialogArea(parent);
    GridLayout layout = new GridLayout(2, true);
    contents.setLayout(layout);

    GridData data;

    Label label = new Label(contents, SWT.NONE);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    label.setLayoutData(data);
    setValidationLabel(label);

    label = new Label(contents, SWT.NONE);
    label.setText("Nazwa");

    m_name = new Text(contents, SWT.BORDER);
    m_name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    addVerifiedControl(m_name);

    label = new Label(contents, SWT.NONE);
    label.setText("Czas przed³u¿ania");

    m_defFreezeTime = new Text(contents, SWT.BORDER);
    m_defFreezeTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    addVerifiedControl(m_defFreezeTime);

    m_ddTable = new DetectorDataTable(contents, SWT.FULL_SELECTION);
    data = new GridData(GridData.FILL_BOTH);
    data.horizontalSpan = 2;
    m_ddTable.asTable().setLayoutData(data);
    m_ddTable.asTable().setHeaderVisible(true);

    Composite btnpane = new Composite(contents, SWT.NONE);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    btnpane.setLayoutData(data);
    btnpane.setLayout(new FillLayout());

    Button btn = new Button(btnpane, SWT.NONE);
    btn.setText("Dodaj");
    btn.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        addDefaultEntry();
      }
    });

    btn = new Button(btnpane, SWT.NONE);
    btn.setText("Usuñ");
    btn.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        removeSelectedEntries();
      }
    });

    fromDetector();

    return contents;
  }

  protected void constrainShellSize() {
    super.constrainShellSize();
    Point sz = getShell().getSize();
    sz.y = 350;
    getShell().setSize(sz);
  }

  private void removeSelectedEntries() {
    Object[] sel = ((IStructuredSelection) m_ddTable.asTableViewer().getSelection())
      .toArray();
    m_entries.removeAll(Arrays.asList(sel));
    m_ddTable.asTableViewer().refresh();
  }

  private void addDefaultEntry() {
    TransitDetector.Entry e = new TransitDetector.Entry();
    e.schedule = Traffix.scheduleBank().getSchedules()[0];
    e.sec = 0;
    e.tmax = 15;
    m_entries.add(e);
    m_ddTable.asTableViewer().refresh();
  }

  private String[] getScheduleNames() {
    Schedule[] schs = Traffix.scheduleBank().getSchedules();
    String[] names = new String[schs.length];
    for (int i = 0; i < names.length; i++) {
      names[i] = schs[i].getName();
    }
    return names;
  }

}