/*
 * Created on 2005-09-24
 */

package traffix.ui.sim;

import java.util.*;
import java.util.List;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

import traffix.core.accident.*;
import traffix.core.sim.graph.Node;
import traffix.ui.Colors;
import traffix.ui.VerifiedDialog;

public class AccidentPartsDialog extends VerifiedDialog {
  String[]                             m_colNames  = new String[] { "Lp", "Typ", "Nazwa",
    "Kolor", "Czas", "Miejsce"                    };
  Map<RGB, Image>                      m_colorImgs = new HashMap<RGB, Image>();
  int[]                                m_colWidths = { 25, 80, 80, 45, 40, 70 };
  java.util.List<IAccidentParticipant> m_participants;

  Table                                m_table;
  TableViewer                          m_viewer;
  private IAccidentModel               m_accModel;

  class LabProv extends LabelProvider implements ITableLabelProvider {

    public Image getColumnImage(Object element, int columnIndex) {
      IAccidentParticipant e = (IAccidentParticipant) element;
      switch (columnIndex) {
        case 3 :
          return getColorImg(e.getColor());
      }
      return null;
    }

    public String getColumnText(Object element, int columnIndex) {
      IAccidentParticipant e = (IAccidentParticipant) element;
      switch (columnIndex) {
        case 0 :
          return Integer.toString(m_participants.indexOf(e) + 1);
        case 1 :
          return APTypeNames.getName(e.getType());
        case 2 :
          return e.getName();
        case 3 :
          return "";
        case 4 :
          return Integer.toString((int) e.getArriveTime());
        case 5 :
          Node n = e.getArriveNode();
          if (n != null)
            return n.getName() != null ? n.getName() : "";
      }

      return "";
    }
  }

  public AccidentPartsDialog(Shell parentShell,
      java.util.List<IAccidentParticipant> parts, IAccidentModel am) {
    super(parentShell);
    m_accModel = am;
    m_participants = new ArrayList<IAccidentParticipant>();
    for (IAccidentParticipant p : parts)
      m_participants.add(p.clone());
  }

  public List<IAccidentParticipant> getParticipants() {
    return m_participants;
  }

  @Override
  public String getValidationError() {
    return null;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Uczestnicy wypadku");
    Point sz = newShell.getSize();
    newShell.setSize(400, 300);
    newShell.addDisposeListener(new DisposeListener() {

      public void widgetDisposed(DisposeEvent arg0) {
        for (Image img : m_colorImgs.values())
          img.dispose();
      }
    });
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    Button b = createButton(parent, -1, "&Nowy", false);
    b.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        IAccidentParticipant p = new AccidentParticipant();
        m_participants.add(p);
        m_viewer.refresh();
      }

    });
    b = createButton(parent, -1, "&Usuñ", false);

    super.createButtonsForButtonBar(parent);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite contents = (Composite) super.createDialogArea(parent);
    contents.setLayout(new FillLayout());

    m_table = new Table(contents, SWT.FULL_SELECTION);
    TableColumn c;
    for (int i = 0; i < m_colNames.length; ++i) {
      c = new TableColumn(m_table, SWT.NONE);
      c.setText(m_colNames[i]);
      c.setWidth(m_colWidths[i]);
      
      if (i==0) {
        c.setAlignment(SWT.LEFT);
      }

    }
    m_viewer = new TableViewer(m_table);
    m_viewer.setColumnProperties(m_colNames);
    buildTabComponents();
    m_viewer.setInput(m_participants);

    m_table.setHeaderVisible(true);
    m_table.setLinesVisible(true);

    return contents;
  }

  private void buildTabComponents() {
    m_viewer.setLabelProvider(new LabProv());

    m_viewer.setContentProvider(new IStructuredContentProvider() {
      public void dispose() {
      }

      public Object[] getElements(Object inputElement) {
        java.util.List<IAccidentParticipant> parts = (java.util.List<IAccidentParticipant>) inputElement;
        return (IAccidentParticipant[]) parts.toArray(new IAccidentParticipant[parts
            .size()]);
      }

      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      }
    });

    m_viewer.setCellModifier(new ICellModifier() {

      public boolean canModify(Object element, String property) {
        int idx = Arrays.asList(m_colNames).indexOf(property);
        switch (idx) {
          case 1 :
            return true;
          case 2 :
            return true;
          case 3 :
            return true;
          case 4 :
            return true;
          case 5 :
            return true;
        }
        return false;
      }

      public Object getValue(Object element, String property) {
        if (element instanceof TableItem)
          element = ((TableItem) element).getData();
        IAccidentParticipant p = (IAccidentParticipant) element;
        int idx = Arrays.asList(m_colNames).indexOf(property);
        switch (idx) {
          case 1 :
            return new Integer(Arrays.asList(APTypeNames.getNames()).indexOf(
                APTypeNames.getName(p.getType())));
          case 2 :
            return p.getName();

          case 3 :
            return p.getColor();

          case 4 :
            return Integer.toString((int)p.getArriveTime());

          case 5 :
            String name = p.getArriveNode() != null ? p.getArriveNode().getName() : null;
            if (name != null)
              return new Integer(m_accModel.getNodeNames().indexOf(name));
            return -1;
        }
        return null;
      }

      public void modify(Object element, String property, Object value) {
        if (element instanceof TableItem)
          element = ((TableItem) element).getData();
        IAccidentParticipant p = (IAccidentParticipant) element;
        int idx = Arrays.asList(m_colNames).indexOf(property);
        switch (idx) {
          case 1 :
            int idx2 = (Integer) value;
            if (idx2 != -1)
              p.setType(APTypeNames.getAPType(APTypeNames.getNames()[(Integer) value]));
            break;
          case 2 :
            p.setName((String) value);
            break;
          case 3 :
            p.setColor((RGB) value);
            break;
          case 4 :
            try {
              p.setArriveTime(Integer.parseInt((String) value));
            } catch (NumberFormatException e) {}
            break;
          case 5 :
            idx2 = (Integer) value;
            if (idx2 != -1) {
              String name = m_accModel.getNodeNames().get(idx2);
              Node n = m_accModel.getNode(name);
              p.setArriveNode(n);
            }
            break;

        }
        m_viewer.refresh(element);
      }
    });

    CellEditor[] editors = new CellEditor[6];
    editors[1] = new ComboBoxCellEditor(m_viewer.getTable(), APTypeNames.getNames(), SWT.READ_ONLY);
    editors[2] = new TextCellEditor(m_viewer.getTable());
    editors[4] = new TextCellEditor(m_viewer.getTable());
    editors[3] = new ColorCellEditor(m_viewer.getTable());
    List<String> nodeNames = m_accModel.getNodeNames();
    String[] nnames = (String[]) nodeNames.toArray(new String[nodeNames.size()]);
    editors[5] = new ComboBoxCellEditor(m_viewer.getTable(), nnames, SWT.READ_ONLY);
    m_viewer.setCellEditors(editors);
  }

  private Image getColorImg(RGB color) {
    Image im = m_colorImgs.get(color);
    if (im != null)
      return im;
    int w = m_colWidths[3];
    int h = m_table.getItemHeight();
    im = new Image(Display.getDefault(), w, h);
    GC gc = new GC(im);
    gc.setBackground(Colors.get(color));
    gc.fillRectangle(0, 0, w - 1, h - 1);
    gc.setForeground(Colors.system(SWT.COLOR_BLACK));
    gc.drawRectangle(0, 0, w - 1, h - 1);
    gc.dispose();
    m_colorImgs.put(color, im);
    return im;
  }

}
