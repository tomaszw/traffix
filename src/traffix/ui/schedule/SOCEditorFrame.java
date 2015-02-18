
package traffix.ui.schedule;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.widgets.*;

import traffix.Traffix;
import traffix.Utils;
import traffix.automation.Excel;
import traffix.core.VehicleGroup;
import traffix.core.schedule.*;
import traffix.ui.Images;
import traffix.ui.Keymap;
import traffix.ui.misc.SwtCoolTable;

/**
 * Author: tomek
 * Date: 2005-02-26
 */
enum SOCColumnId {
  Grupa,
  Nazwa,
  IloscPasow,
  DlugoscSygnalu,
  NatezenieRuchu,
  Przepustowosc,
  Obciazenie,
  Rezerwa,
  Tt,
  G1,
  G2,
  Wp,
  NatezenieNasycenia,
  DlugoscCyklu,
  NazwaProgramu
}

public class SOCEditorFrame extends Window {
  static String[] columnNames = {
    "Grupa", "Nazwa", "Iloœæ pasów", "D³ugoœæ sygna³u",
    "Natê¿enie ruchu", "Przepustowoœæ", "Obci¹¿enie",
    "Rezerwa", "tt     ", "g1", "g2", "wp    ",
    "Natê¿enie nasycenia", "D³ugoœæ cyklu",
    "Nazwa programu"
  };
  static SOCColumnId[] columnOrder = {
    SOCColumnId.Grupa,
    SOCColumnId.Nazwa,
    SOCColumnId.IloscPasow,
    SOCColumnId.DlugoscSygnalu,
    SOCColumnId.NatezenieRuchu,
    SOCColumnId.Przepustowosc,
    SOCColumnId.Obciazenie,
    SOCColumnId.Rezerwa,
    SOCColumnId.Tt,
    SOCColumnId.G1,
    SOCColumnId.G2,
    SOCColumnId.Wp,
    SOCColumnId.NatezenieNasycenia,
    SOCColumnId.DlugoscCyklu,
    SOCColumnId.NazwaProgramu
  };

  private Action m_exportToExcel = new ExportToExcel();
  private class ExportToExcel extends Action {
      public ExportToExcel() {
        setText("Eksportuj!");
        setToolTipText("Eksport tabeli do Excela");
        setImageDescriptor(Images.getDescriptor("icons/export.gif"));
      }


      public void run() {
        exportToExcel();
      }
  }

  private Action m_help = new Help();
  private class Help extends Action {
    public Help() {
      setText("Pomoc");
      setToolTipText("Poka¿ pomoc");
      setImageDescriptor(Images.getDescriptor("icons/qmark.gif"));
      setMenuCreator(new IMenuCreator() {
        public void dispose() {
        }

        public Menu getMenu(Control parent) {
          MenuManager helpMenu = new MenuManager("P&omoc");
          Traffix.appendFilesToHelpMenu(helpMenu);
          return helpMenu.createContextMenu(parent);
        }

        public Menu getMenu(Menu parent) {
          MenuManager helpMenu = new MenuManager();
          Traffix.appendFilesToHelpMenu(helpMenu);
          Menu menu = new Menu(parent);
          helpMenu.fill(menu, 0);
          return menu;
        }
      });
    }
    public void run() {
    }
  }
  private SOCTable m_capacityTable;
  private ToolBar m_toolBar;
  private Schedule m_schedule;
  private NumberFormat m_twoDigitFmt = new DecimalFormat("0.00");
  private NumberFormat m_oneDigitFmt = new DecimalFormat("0.0");

  class SOCTable extends SwtCoolTable {
    public SOCTable(Composite parent, int style) {
      super(parent, style, columnNames);
      setInput(m_schedule);
    }

    protected CellEditor[] createEditors() {
      CellEditor[] editors = new CellEditor[columnNames.length];
      for (int i=0; i<editors.length; ++i)
      editors[i] = new TextCellEditor(asTable());
      return editors;
    }

    public String getColumnText(Object element, int columnIndex) {
      int groupNum = (Integer) element;
      VehicleGroup g = m_schedule.getGroup(groupNum);
      GroupProgram p = m_schedule.getProgram(groupNum);
      CapacityEntry cap = m_schedule.getCapacityEntry(groupNum);

      // colour
      if (columnIndex == 0) {
        if (cap.dCp >= 0) {
          asTable().getItem(groupNum).setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        } else {
          asTable().getItem(groupNum).setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
        }
      }

      switch (columnOrder[columnIndex]) {
      case Grupa:
        return g.getElectricName();
      case Nazwa:
        return g.getName();
      case IloscPasow:
        return Integer.toString(g.getNumTracks());
      case DlugoscSygnalu:
        return Integer.toString(p.getTotalGreenDuration());
      case NatezenieRuchu:
        return Integer.toString(cap.Q);
      case Przepustowosc:
        return m_twoDigitFmt.format(cap.Cp);
      case Obciazenie:
        return m_twoDigitFmt.format(cap.QCp);
      case Rezerwa:
        return m_twoDigitFmt.format(cap.dCp);
      case Tt:
        return m_twoDigitFmt.format(g.getDegreeOfFreedom());
      case G1:
        return Integer.toString(g.getBusCoeff());
      case G2:
        return Integer.toString(g.getParkingCoeff());
      case Wp:
        return m_twoDigitFmt.format(g.getOverflowCoeff());
      case NatezenieNasycenia:
        // wyliczone natezenie nasycenia
        return Integer.toString(cap.S);
      case DlugoscCyklu:
        return Integer.toString(p.getLength());
      case NazwaProgramu:
        return m_schedule.getName();
      }
      return null;
    }

    public Object[] getElements(Object inputElement) {
      Integer[] elems = new Integer[m_schedule.getNumGroups()];
      for (int i = 0; i < elems.length; ++i)
        elems[i] = i;
      return elems;
    }

    public boolean canModify(Object element, String property) {
      int col = Arrays.asList(columnNames).indexOf(property);
      switch (columnOrder[col]) {
        case IloscPasow:
        case Tt:
        case G1:
        case G2:
        case Wp:
        case NatezenieRuchu:
          return true;
      }
      return false;
    }

    public Object getValue(Object element, String property) {
      int col = Arrays.asList(columnNames).indexOf(property);
      int groupNum = (Integer) element;
      VehicleGroup g = m_schedule.getGroup(groupNum);
      CapacityEntry cap = m_schedule.getCapacityEntry(groupNum);
      return getColumnText(element, col);
    }

    public void modify(Object element, String property, Object value) {
      int col = Arrays.asList(columnNames).indexOf(property);
      if (element instanceof Item)
        element = ((Item) element).getData();

      int groupNum = (Integer) element;
      CapacityEntry e = m_schedule.getCapacityEntry(groupNum);
      VehicleGroup g = VehicleGroup.fromIndex(groupNum);
      try {
        switch (columnOrder[col]) {
        case IloscPasow:
          g.setNumTracks(Integer.parseInt((String) value));
          break;
        case Tt:
          g.setDegreeOfFreedom(m_twoDigitFmt.parse((String) value).floatValue());
          break;
        case G1:
          g.setBusCoeff(Integer.parseInt((String) value));
          break;
        case G2:
          g.setParkingCoeff(Integer.parseInt((String) value));
          break;
        case Wp:
          g.setOverflowCoeff(m_twoDigitFmt.parse((String) value).floatValue());
          break;
        case NatezenieRuchu:
          e.Q = Integer.parseInt((String) value);
          System.out.println(e.Q);
          break;
        }
      } catch (ParseException ex) {
      } catch (NumberFormatException ex) {
      }

      //for (int i=0; i<m_schedule.getNumGroups(); ++i)
      //  m_schedule.updateCapacityData(i);
      // uaktualniamy dane we wszystkich harmonogramach
      ScheduleBank bank = Traffix.model().getScheduleBank();
      for (Schedule s : bank.getSchedules()) {
        s.updateCapacityData(groupNum);
      }
      m_schedule.updateCapacityData(groupNum);
      asTableViewer().refresh();
    }
  }

  public SOCEditorFrame(Shell parent, Schedule schedule) {
    super(parent);
    //setShellStyle(getShellStyle() | SWT.SYSTEM_MODAL);
    m_schedule = schedule;
  }

  void exportToExcel() {
    Excel excel = new Excel(Traffix.shell());
    excel.setVisible(true);
    excel.addWorkbook();
    OleAutomation sheet = excel.getWorksheet(1);
    for (int i=0; i<columnNames.length; ++i) {
      String c1 = excel.getCellName(i,0);
      excel.setCellValue(sheet, c1, columnNames[i]);
    }
    for (int i=0; i<m_capacityTable.asTable().getItemCount(); ++i) {
      TableItem item = m_capacityTable.asTable().getItem(i);
      for (int j=0; j<columnNames.length; ++j) {
        String txt = item.getText(j);
        String cell = excel.getCellName(j, i+1);
        excel.setCellValue(sheet, cell, txt);
      }
    }
    excel.dispose();
  }

  protected ToolBarManager createToolBarManager(int style) {
    ToolBarManager tm = new ToolBarManager(style);
    tm.add(m_exportToExcel);
    tm.add(m_help);
    Utils.forceToolbarText(tm);

    return tm;
  }

  protected MenuManager createMenuManager() {
    MenuManager mm = new MenuManager();
    MenuManager helpMenu = new MenuManager("P&omoc");
    Traffix.appendFilesToHelpMenu(helpMenu);
    mm.add(helpMenu);
    return mm;
  }

  protected void configureShell(Shell shell) {
    super.configureShell(shell);
//shell.setSize(640, 480);
    shell.setText("Metoda SOC");
    shell.setImage(Images.get("icons/schedule.gif"));
  }

  protected Control createContents(Composite parent) {

    Composite contents = (Composite) super.createContents(parent);
    //double[][] size = {
    //  {LatticeLayout.FILL}, {LatticeLayout.PREFERRED, LatticeLayout.FILL}};
    //LatticeLayout layout = new LatticeLayout(size);
    //contents.setLayout(layout);
    contents.setLayout(new GridLayout(1,false));
    
    ToolBarManager cm = createToolBarManager(SWT.FLAT | SWT.WRAP);
    Control c = cm.createControl(contents);
    //c.setLayoutData(new LatticeData("0,0"));
    GridData d = new GridData();
    d.horizontalAlignment = GridData.FILL_HORIZONTAL;
    c.setLayoutData(d);
    
    m_capacityTable = new SOCTable(contents, SWT.FULL_SELECTION|SWT.V_SCROLL);
    m_capacityTable.asTable().setHeaderVisible(true);
    for (int i = 3; i < 14; ++i)
      m_capacityTable.asTable().getColumn(i).setAlignment(SWT.RIGHT);

    m_capacityTable.asTable().addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        onKeyDown(e);
      }

      public void keyReleased(KeyEvent e) {
      }
    });
    d = new GridData();
    c = m_capacityTable.asTable();
    d.horizontalAlignment = GridData.FILL_HORIZONTAL;
    d.verticalAlignment = GridData.FILL_VERTICAL;
    d.grabExcessHorizontalSpace = true;
    d.grabExcessVerticalSpace = true;
    c.setLayoutData(d);
//    m_capacityTable.asTable().setLayoutData(new LatticeData("0,1"));
    m_capacityTable.asTable().setFocus();
//m_capacityTable.asTable().setLinesVisible(true);
    contents.pack();
    return contents;
  }

  private void onKeyDown(KeyEvent e) {
    if (e.keyCode == Keymap.SOC_EDITOR_KEY) {
      close();
    } else if (e.keyCode == ' ' || e.keyCode == '\r') {
      int idx = m_capacityTable.asTable().getSelectionIndex();
      if (idx != -1) {
        TableItem item = m_capacityTable.asTable().getItem(idx);
        m_capacityTable.asTableViewer().editElement(item.getData(), Arrays.asList(columnOrder).indexOf(SOCColumnId.NatezenieRuchu));
      } else {
        m_capacityTable.asTable().setSelection(0);
      }
    }
  }
}
