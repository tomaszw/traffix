/*
 * Created on 2005-09-01
 */

package traffix.ui.sim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import traffix.Traffix;
import traffix.core.VehicleGroupSet;
import traffix.core.sim.Route;
import traffix.core.sim.RouteInfo;
import traffix.core.sim.graph.IGraphPath;
import traffix.core.sim.graph.Node;
import traffix.ui.*;

public class RouteEditDialog extends VerifiedDialog {
  private Button[]     m_cmdButtons;
  private EditingTable m_editingTable;
  private HidingTable  m_hidingTable;
  private IMapEditor   m_mapEditor;
  private Node         m_node;
  private List<Route>  m_routes;
  private TabFolder    m_tabFolder;

  public RouteEditDialog(Shell parentShell, Node node, IMapEditor mapEditor) {
    super(parentShell);
    m_node = node;
    m_routes = new ArrayList<Route>();
    for (Route r : m_node.getRoutesFromNode())
      m_routes.add(r.clone());
    m_mapEditor = mapEditor;
    setBlockOnOpen(false);
    setShellStyle(getShellStyle() & ~(SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL)
        | SWT.RESIZE | SWT.MIN | SWT.MAX);
  }

  public List<Route> getRoutes() {
    return m_routes;
  }

  @Override
  public String getValidationError() {
    return null;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Edycja torów ruchu");

    final PaintListener paintListener = new PaintListener() {
      public void paintControl(PaintEvent e) {
        paintSelectedRoute();
      }
    };
    m_mapEditor.getCanvas().addPaintListener(paintListener);
    newShell.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent arg0) {
        m_mapEditor.getCanvas().removePaintListener(paintListener);
        m_mapEditor.getCanvas().redraw();
      }
    });
    newShell.setSize(640,300);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite contents = (Composite) super.createDialogArea(parent);
    contents.setLayout(new FillLayout());
    m_tabFolder = new TabFolder(contents, SWT.TOP);
    TabItem page1 = new TabItem(m_tabFolder, SWT.NONE);
    page1.setControl(createMainPage(m_tabFolder));
    page1.setText("Edycja danych");
    m_tabFolder.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent arg0) {
      }

      public void widgetSelected(SelectionEvent e) {
        onPageChange();
      }
    });

    contents.pack();
    TabItem page2 = new TabItem(m_tabFolder, SWT.NONE);
    page2.setControl(createHidingPage(m_tabFolder));
    page2.setText("Edycja widocznoœci");
    //Point s = contents.getSize();
    //contents.setSize(s.x, 400);
    return contents;
  }

  @Override
  protected void okPressed() {
    super.okPressed();
    m_node.setRoutes(m_routes);
    Traffix.model().setModified(true);
    Traffix.model().fireUpdated();
  }

  private Control createHidingPage(Composite parent) {
    Composite contents = new Composite(parent, SWT.NONE);
    contents.setLayout(new GridLayout(1, false));

    GridData data = new GridData();

    HidingTable tab = new HidingTable(contents);
    data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.grabExcessVerticalSpace = true;
    tab.table().setLayoutData(data);
    tab.viewer().setInput(m_routes);
    m_hidingTable = tab;
    updateHidingCheckboxes();
    m_hidingTable.table().addSelectionListener(new SelectionListener() {

      public void widgetDefaultSelected(SelectionEvent arg0) {
      }

      public void widgetSelected(SelectionEvent arg0) {
        onHidingTabSelectionChange();
      }

    });

    Label lab = new Label(contents, SWT.NONE);
    lab.setText("Zaznacz widoczne tory");

    return contents;
  }

  private Control createMainPage(Composite parent) {
    Composite contents = new Composite(parent, SWT.NONE);
    contents.setLayout(new GridLayout(1, false));

    GridData data = new GridData();

    EditingTable tab = new EditingTable(contents);
    data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.grabExcessVerticalSpace = true;
    //data.verticalIndent = GridData.BEGINNING;
    // data.widthHint = GridData.FILL_HORIZONTAL;
    // data.heightHint = GridData.FILL_VERTICAL;

    tab.table().setLayoutData(data);

    tab.viewer().setInput(getVisibleRoutes());

    RowData rowData = new RowData();
    // data.verticalAlignment = SWT.BEGINNING;
    // data.grabExcessHorizontalSpace = true;

    Composite cmdpanel = new Composite(contents, SWT.NONE);
    cmdpanel.setLayout(new RowLayout(SWT.HORIZONTAL));
    int bs = SWT.NONE;
    m_cmdButtons = new Button[5];
    Button btn = new Button(cmdpanel, bs);
    btn.setText("Grupy");
    btn.setLayoutData(rowData);
    m_cmdButtons[0] = btn;
    btn.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent arg0) {
        IStructuredSelection sel = (IStructuredSelection) m_editingTable.viewer()
            .getSelection();
        Route route = (Route) sel.getFirstElement();
        GroupSelectionDialog dlg = new GroupSelectionDialog(getShell());
        dlg.setGroups(route.getInfo().getControllingGroups());
        if (dlg.open() == dlg.OK) {
          //route.getInfo().setControllingGroups(dlg.getGroups());
          //System.out.println(dlg.getGroups().size());
          route.getInfo().getControllingGroups().assign(dlg.getGroups());
          m_editingTable.viewer().refresh(route);
        }
      }
    });

    btn = new Button(cmdpanel, bs);
    btn.setText("Parametry");
    btn.setLayoutData(rowData);
    m_cmdButtons[1] = btn;
    btn.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent arg0) {
        IStructuredSelection sel = (IStructuredSelection) m_editingTable.viewer()
            .getSelection();
        Route route = (Route) sel.getFirstElement();
        MoveParamsDialog dlg = new MoveParamsDialog(getShell(), route.getInfo()
            .getMoveParams());
        dlg.open();
      }
    });
    btn = new Button(cmdpanel, bs);
    btn.setText("Sprzê¿enie");
    btn.setLayoutData(rowData);
    m_cmdButtons[2] = btn;
    btn.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent arg0) {
        IStructuredSelection sel = (IStructuredSelection) m_editingTable.viewer()
            .getSelection();
        Route route = (Route) sel.getFirstElement();
        GroupSelectionDialog dlg = new GroupSelectionDialog(getShell());
        dlg.setGroups(route.getInfo().getLinkedGroups());
        if (dlg.open() == dlg.OK) {
          //route.getInfo().setLinkedGroups(dlg.getGroups());
          route.getInfo().getLinkedGroups().assign(dlg.getGroups());
          m_editingTable.viewer().refresh(route);
        }
      }
    });
    btn = new Button(cmdpanel, bs);
    btn.setText("Komentarz");
    btn.setLayoutData(rowData);
    m_cmdButtons[3] = btn;
    btn.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent arg0) {
        IStructuredSelection sel = (IStructuredSelection) m_editingTable.viewer()
            .getSelection();
        Route route = (Route) sel.getFirstElement();
        String msg = "Podaj komentarz dla wybranego toru";
        String init = route.getInfo().comment;
        InputDialog dlg = new InputDialog(getShell(), Traffix.NAME, msg, init, null);
        if (dlg.open() == InputDialog.OK) {
          route.getInfo().comment = dlg.getValue();
        }
      }
    });
    btn = new Button(cmdpanel, bs);
    btn.setText("Skasuj");
    btn.setLayoutData(rowData);
    m_cmdButtons[4] = btn;
    btn.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent arg0) {
        IStructuredSelection sel = (IStructuredSelection) m_editingTable.viewer()
            .getSelection();
        Route route = (Route) sel.getFirstElement();
        int numRoute = m_routes.indexOf(route);
        MessageBox box = new MessageBox(getShell(), SWT.NO | SWT.YES | SWT.ICON_QUESTION);
        box.setMessage("Czy na pewno skasowaæ tor " + (numRoute + 1) + "?");
        if (box.open() == SWT.YES) {
          deleteRoute(numRoute);
        }
      }
    });

    contents.pack();

    m_editingTable = tab;
    m_editingTable.table().addSelectionListener(new SelectionListener() {

      public void widgetDefaultSelected(SelectionEvent arg0) {
      }

      public void widgetSelected(SelectionEvent arg0) {
        onEditingTabSelectionChange();
      }

    });

    updateCommandButtons();

    return contents;
  }

  private void deleteRoute(int id) {
    Traffix.simManager().getGraph().deleteRoute(m_node, id);
    m_mapEditor.getCanvas().redraw();
    Traffix.model().setModified(true);
    cancelPressed();
    // okPressed();
  }

  private List<Route> getVisibleRoutes() {
    List<Route> vis = new ArrayList<Route>();
    for (Route r : m_routes) {
      if (!r.getInfo().hidden)
        vis.add(r);
    }
    return vis;
  }

  private void onEditingTabSelectionChange() {
    updateCommandButtons();
    m_mapEditor.getCanvas().redraw();
  }

  private void onHidingTabSelectionChange() {
    m_mapEditor.getCanvas().redraw();
  }

  private void onPageChange() {
    int sel = m_tabFolder.getSelectionIndex();
    if (sel == 0) {
      m_editingTable.viewer().setInput(getVisibleRoutes());
      m_editingTable.viewer().refresh();
      m_editingTable.table().setFocus();
      m_mapEditor.getCanvas().redraw();
      updateCommandButtons();
    } else if (sel == 1) {
      m_hidingTable.viewer().refresh();
      m_hidingTable.table().setFocus();
      m_mapEditor.getCanvas().redraw();
    }
  }

  private void paintSelectedRoute() {
    Route r = null;
    if (m_tabFolder != null) {
      int sel = -1;
      if (m_tabFolder.getSelectionIndex() == 0) {
        sel = m_editingTable.table().getSelectionIndex();
        if (sel != -1 && sel < m_editingTable.table().getItemCount() - 1)
          r = (Route) m_editingTable.viewer().getElementAt(sel);
      }

      if (m_tabFolder.getSelectionIndex() == 1) {
        sel = m_hidingTable.table().getSelectionIndex();
        if (sel != -1)
          r = (Route) m_hidingTable.viewer().getElementAt(sel);
      }
    }
    if (r == null)
      return;
    IGraphPath p = r.path();
    Gc gc = m_mapEditor.getGc();
    gc.setLineWidth(4);
    gc.setForeground(Colors.get(new RGB(255, 0, 0)));
    for (int i = 0; i < p.getNumNodes() - 1; ++i) {
      Point pos = m_mapEditor.getCoordTransformer()
          .terrainToScreen(p.getNode(i).getPos());
      Point pos2 = m_mapEditor.getCoordTransformer().terrainToScreen(
          p.getNode(i + 1).getPos());
      gc.drawLine(pos.x, pos.y, pos2.x, pos2.y);
    }
    gc.setLineWidth(1);
  }

  private void updateCommandButtons() {
    int sel = m_editingTable.table().getSelectionIndex();
    boolean e = sel != -1;
    for (Button b : m_cmdButtons)
      b.setEnabled(e);
  }

  private void updateHidingCheckboxes() {
    for (Route r : m_routes)
      m_hidingTable.viewer().setChecked(r, !r.getInfo().hidden);
  }
}

class EditingTable {
  static class SumLine {
  };

  static String[] s_tableCols   = { "Lp", "Nazwa", "N", "C", "A", "T", "Pieszy", "Rower.", "Sygnalizator",
    "Priorytet", "Pref. sek.", "" };
  static int[]    s_tableWidths = { 50, 150, 40, 40, 40, 40, 50, 50, 70, 70, 70, 70 };
  List<Route>     m_routes;
  Table           m_table;
  TableViewer     m_viewer;
  SumLine         m_sumLine;

  class MyContentProvider implements IStructuredContentProvider {
    public void dispose() {
    }

    public Object[] getElements(Object inputElement) {
      List<Object> elems = new ArrayList<Object>((List<Object>) inputElement);
      elems.add(m_sumLine = new SumLine());
      return (Object[]) elems.toArray(new Object[elems.size()]);
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      m_routes = (List<Route>) newInput;
    }
  }

  class MyLabelProvider extends LabelProvider
      implements
        ITableLabelProvider,
        IColorProvider {
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }

    public String getColumnText(Object element, int columnIndex) {
      if (element instanceof SumLine) {
        switch (columnIndex) {
          case 1 :
            return "SUMA";
          case 6 :
            return "RAZEM";
        }
        if (columnIndex >= 2 && columnIndex <= 5 || columnIndex == 8) {
          int sum = 0;
          for (Route r : m_routes) {
            RouteInfo inf = r.getInfo();
            if (columnIndex == 2)
              sum += inf.getMeasure().nPh;
            if (columnIndex == 3)
              sum += inf.getMeasure().hPh;
            if (columnIndex == 4)
              sum += inf.getMeasure().busPh;
            if (columnIndex == 5)
              sum += inf.getMeasure().trolleyPh;
            if (columnIndex == 8)
              sum += inf.getMeasure().nPh + 2*inf.getMeasure().hPh + 3*inf.getMeasure().busPh
                  + 4*inf.getMeasure().trolleyPh;
          }
          return Integer.toString(sum);
        }
        return "";
      }
      Route route = (Route) element;
      RouteInfo inf = route.getInfo();
      switch (columnIndex) {
        case 0 :
          return Integer.toString(m_routes.indexOf(route) + 1);
        case 1 :
          String name = route.getInfo().name;
          return name != null ? name : "<brak>";
        case 2 :
          return Integer.toString(inf.getMeasure().nPh);
        case 3 :
          return Integer.toString(inf.getMeasure().hPh);
        case 4 :
          return Integer.toString(inf.getMeasure().busPh);
        case 5 :
          return Integer.toString(inf.getMeasure().trolleyPh);
        case 6 :
          return Integer.toString(inf.getMeasure().pedeInterval);
        case 7:
          return Integer.toString(inf.getMeasure().cyclistInterval);
        case 8:
          VehicleGroupSet set = inf.getControllingGroups();
          if (set.size() == 0)
            return "brak";
          if (set.size() == Traffix.model().getNumGroups())
            return "wszystkie";
          boolean neg = false;
          if (set.size() > Traffix.model().getNumGroups()/2) {
            set = set.negate();
            neg = true;
          }
          String s="";
          for (traffix.core.VehicleGroup g : set) {
            s += neg?"-":"+";
            s += g.getElectricName();
            s += " ";
          }
          return s;
        case 9 :
          return Integer.toString(inf.priority);
        case 10 :
          return Integer.toString(inf.getPreferredSec());
      }
      return "";
    }

    public Color getForeground(Object element) {
      return Colors.system(SWT.COLOR_BLACK);
    }

    public Color getBackground(Object element) {
      if (!(element instanceof SumLine))
        return Colors.system(SWT.COLOR_WHITE);
      return Colors.system(SWT.COLOR_YELLOW);
    }
  }

  class MyModifier implements ICellModifier {
    public boolean canModify(Object element, String property) {
      if (element instanceof SumLine)
        return false;
      int col = Arrays.asList(s_tableCols).indexOf(property);
      if (col == 1 || col == 2 || col == 3 || col == 4 || col == 5 || col == 6 || col == 7 || col == 9
          || col == 10)
        return true;
      return false;
    }

    public Object getValue(Object element, String property) {
      int col = Arrays.asList(s_tableCols).indexOf(property);
      Route route = (Route) element;
      RouteInfo inf = route.getInfo();
      switch (col) {
        case 1 :
          return route.getInfo().name;
        case 2 :
          return Integer.toString(inf.getMeasure().nPh);
        case 3 :
          return Integer.toString(inf.getMeasure().hPh);
        case 4 :
          return Integer.toString(inf.getMeasure().busPh);
        case 5 :
          return Integer.toString(inf.getMeasure().trolleyPh);
        case 6:
          return Integer.toString(inf.getMeasure().pedeInterval);
        case 7 :
          return Integer.toString(inf.getMeasure().cyclistInterval);
        case 9 :
          return Integer.toString(inf.priority);
        case 10 :
          return Integer.toString(inf.getPreferredSec());
      }
      return null;
    }

    public void modify(Object element, String property, Object value) {
      if (element instanceof Item)
        element = ((Item) element).getData();
      int col = Arrays.asList(s_tableCols).indexOf(property);
      Route route = (Route) element;
      RouteInfo inf = route.getInfo();
      String s = (String) value;
      try {
        switch (col) {
          case 1 :
            route.getInfo().name = s;
            break;
          case 2 :
            inf.getMeasure().nPh = Integer.parseInt(s);
            break;
          case 3 :
            inf.getMeasure().hPh = Integer.parseInt(s);
            break;
          case 4 :
            inf.getMeasure().busPh = Integer.parseInt(s);
            break;
          case 5 :
            inf.getMeasure().trolleyPh = Integer.parseInt(s);
            break;
          case 6 :
            inf.getMeasure().pedeInterval = Integer.parseInt(s);
            break;
          case 7:
            inf.getMeasure().cyclistInterval = Integer.parseInt(s);
            break;
          case 9 :
            inf.priority = Integer.parseInt(s);
            break;
          case 10 :
            inf.setPreferredSec(Integer.parseInt(s));
            break;
        }
      } catch (NumberFormatException e) {
      }
      viewer().refresh(element);
      viewer().refresh(m_sumLine);
    }
  }

  public EditingTable(Composite parent) {
    m_table = new Table(parent, SWT.FULL_SELECTION | SWT.BORDER);
    m_table.setHeaderVisible(true);
    TableColumn col;
    for (int i = 0; i < s_tableCols.length; i++) {
      col = new TableColumn(m_table, SWT.NONE);
      col.setText(s_tableCols[i]);
      col.setWidth(s_tableWidths[i]);
    }
    m_table.setLinesVisible(true);
    m_table.addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        if (m_table.getItemCount() == 0)
          return;
        Rectangle rc1 = m_table.getItem(m_table.getItemCount()-1).getBounds(1);
        Rectangle rc2 = m_table.getItem(m_table.getItemCount()-1).getBounds(7);
        rc1.width = rc2.x+rc2.width-rc1.x;
        e.gc.setLineWidth(2);
        e.gc.drawLine(rc1.x, rc1.y, rc1.x+rc1.width, rc1.y);
        e.gc.drawLine(rc1.x, rc1.y+rc1.height, rc1.x+rc1.width, rc1.y+rc1.height);
        e.gc.setLineWidth(1);
      }
    
    });
    
    m_viewer = new TableViewer(m_table);
    m_viewer.setColumnProperties(s_tableCols);
    m_viewer.setContentProvider(new MyContentProvider());
    m_viewer.setLabelProvider(new MyLabelProvider());
    m_viewer.setCellModifier(new MyModifier());
    CellEditor[] editors = new CellEditor[11];
    editors[1] = new TextCellEditor(m_table);
    editors[2] = new TextCellEditor(m_table);
    editors[3] = new TextCellEditor(m_table);
    editors[4] = new TextCellEditor(m_table);
    editors[5] = new TextCellEditor(m_table);
    editors[7] = new TextCellEditor(m_table);
    editors[9] = new TextCellEditor(m_table);
    editors[10] = new TextCellEditor(m_table);
    editors[6] = new TextCellEditor(m_table);
    m_viewer.setCellEditors(editors);
  }

  public Table table() {
    return m_table;
  }

  public TableViewer viewer() {
    return m_viewer;
  }
}

class HidingTable {
  static String[]     s_tableCols   = { "Lp", "Nazwa" };
  static int[]        s_tableWidths = { 50, 150 };
  List<Route>         m_routes;
  Table               m_table;
  CheckboxTableViewer m_viewer;

  class MyContentProvider implements IStructuredContentProvider {

    public void dispose() {
    }

    public Object[] getElements(Object inputElement) {
      List<Route> routes = (List<Route>) inputElement;
      return (Route[]) routes.toArray(new Route[routes.size()]);
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      m_routes = (List<Route>) newInput;
    }
  }

  class MyLabelProvider extends LabelProvider implements ITableLabelProvider {
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }

    public String getColumnText(Object element, int columnIndex) {
      Route route = (Route) element;
      RouteInfo inf = route.getInfo();
      switch (columnIndex) {
        case 0 :
          return Integer.toString(m_routes.indexOf(route) + 1);
        case 1 :
          String name = route.getInfo().name;
          return name != null ? name : "<brak>";
      }
      return null;
    }
  }

  class MyModifier implements ICellModifier {
    public boolean canModify(Object element, String property) {
      int col = Arrays.asList(s_tableCols).indexOf(property);
      return col == 1;
    }

    public Object getValue(Object element, String property) {
      int col = Arrays.asList(s_tableCols).indexOf(property);
      Route route = (Route) element;
      switch (col) {
        case 1 :
          return route.getInfo().name;
      }
      return null;
    }

    public void modify(Object element, String property, Object value) {
      if (element instanceof Item)
        element = ((Item) element).getData();
      int col = Arrays.asList(s_tableCols).indexOf(property);
      Route route = (Route) element;
      String s = (String) value;
      switch (col) {
        case 1 :
          route.getInfo().name = s;
          break;
      }
      viewer().refresh(element);
    }
  }

  public HidingTable(Composite parent) {
    m_table = new Table(parent, SWT.CHECK | SWT.FULL_SELECTION | SWT.BORDER);
    m_table.setHeaderVisible(true);
    m_table.setLinesVisible(true);
    TableColumn col;
    for (int i = 0; i < s_tableCols.length; i++) {
      col = new TableColumn(m_table, SWT.NONE);
      col.setText(s_tableCols[i]);
      col.setWidth(s_tableWidths[i]);
    }
    m_viewer = new CheckboxTableViewer(m_table);
    m_viewer.setColumnProperties(s_tableCols);
    m_viewer.setContentProvider(new MyContentProvider());
    m_viewer.setLabelProvider(new MyLabelProvider());
    m_viewer.setCellModifier(new MyModifier());
    CellEditor[] editors = new CellEditor[2];
    editors[1] = new TextCellEditor(m_table);
    m_viewer.setCellEditors(editors);

    m_viewer.addCheckStateListener(new ICheckStateListener() {
      public void checkStateChanged(CheckStateChangedEvent event) {
        Route r = (Route) event.getElement();
        r.getInfo().hidden = !event.getChecked();
        m_viewer.setSelection(new StructuredSelection(r));
      }

    });
  }

  public Table table() {
    return m_table;
  }

  public CheckboxTableViewer viewer() {
    return m_viewer;
  }
}
