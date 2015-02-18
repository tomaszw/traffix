package traffix.ui;

import java.io.File;

import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.widgets.*;
import org.tw.patterns.observer.IUpdateListener;
import org.tw.patterns.observer.IUpdateable;
import org.tw.web.XmlKit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import traffix.Traffix;
import traffix.Utils;
import traffix.automation.Excel;
import traffix.core.VehicleGroup;
import traffix.core.Weekdays;
import traffix.core.model.Model;
import traffix.core.schedule.*;
import traffix.core.sim.StatisticsExporter;
import traffix.ui.schedule.ScheduleEditorFrame;
import traffix.ui.schedule.WeeklyScheduleEntryDialog;
import traffix.ui.sim.SimFrame;
import traffix.ui.sim.SimMode;
import za.co.quirk.layout.LatticeData;
import za.co.quirk.layout.LatticeLayout;

public class TraffixFrame extends ApplicationWindow {

  Action m_accidents = new Accidents();
  Action                  m_about                 = new About();
  Action                  m_exit                  = new Quit();
  Action                  m_exportSim             = new ExportSim();
  Action                  m_importMcm             = new ImportFromMcm();
  Action                  m_merge                 = new Merge();
  Action                  m_new                   = new New();
  Action                  m_open                  = new Open();
  Action                  m_openSimulation        = new OpenSimulation();
  Action                  m_projectGroupsDown     = new ProjectGroupDown();
  Action                  m_projectGroupsUp       = new ProjectGroupUp();
  Action                  m_save                  = new Save();
  Action                  m_saveas                = new SaveAs();
  Action                  m_scheduleAdd           = new ScheduleBankAdd();
  Action                  m_scheduleEdit          = new ScheduleBankEdit();
  Action                  m_scheduleExport        = new ScheduleBankExport();
  Action                  m_scheduleImport        = new ScheduleBankImport();
  Action                  m_scheduleRemove        = new ScheduleBankRemove();
  Action                  m_scheduleRename        = new ScheduleBankRename();
  Action                  m_wsAdd                 = new WSEntryAdd();
  Action                  m_wsEdit                = new WSEntryEdit();
  Action                  m_wsExcelExport         = new WSExcelExport();
  Action                  m_wsExport              = new WSExport();
  Action                  m_wsImport              = new WSImport();
  Action                  m_wsRemove              = new WSEntryRemove();
  Action                  m_wsStartDay            = new WSStartDay();

  private Table           m_groupTable;
  private ToolBar         m_groupToolbar;
  private Table           m_scheduleBankTable;
  private Menu            m_scheduleBankTableMenu;
  private TableViewer     m_scheduleBankTableViewer;
  private ToolBar         m_scheduleBankToolbar;
  private IInputValidator m_scheduleNameValidator = new IInputValidator() {
                                                    public String isValid(String newText) {
                                                      if (newText.equals(""))
                                                        return "Nazwa nie mo¿e byæ pusta.";
                                                      if (Traffix.model().getSchedule(
                                                          newText) != null)
                                                        return "Nazwa jest zajêta.";
                                                      return null;
                                                    }
                                                  };
  private Table           m_weeklyScheduleTable;
  private Menu            m_weeklyScheduleTableMenu;
  private TableViewer     m_weeklyScheduleTableViewer;
  private ToolBar         m_weeklyScheduleToolbar;

  class Accidents extends Action {
    public Accidents() {
      setText("Wypadki");
      setToolTipText("Modu³ symulacji wypadków");
    }
    
    public void run() {
      if (!Traffix.model().isEmpty()) {
        SimFrame f = new SimFrame(getShell(), SimMode.Accidents);
        // f.setBlockOnOpen(true);
        f.open();
      }
    }
  }
  
  class About extends Action {
    public About() {
      setText("O programie...");
      setToolTipText("Poka¿ informacje o programie");
      setImageDescriptor(Images.getDescriptor("icons/accidents.gif"));
    }

    public void run() {
      AboutDlg dlg = new AboutDlg(getShell());
      dlg.open();
    }
  }

  class AboutDlg extends org.eclipse.jface.dialogs.Dialog {
    protected AboutDlg(Shell parentShell) {
      super(parentShell);
    }

    protected void configureShell(Shell newShell) {
      newShell.setText("O programie...");
      // newShell.setSize(200,150);
      super.configureShell(newShell);
    }

    protected Control createContents(Composite parent) {
      Control r = super.createContents(parent);
      getButton(CANCEL).setVisible(false);
      return r;
    }

    protected Control createDialogArea(Composite parent) {
      Composite area = new Composite(parent, SWT.BORDER);
      area.setLayout(new FillLayout());
      Text text = new Text(area, SWT.READ_ONLY | SWT.MULTI);
      text.setText(Traffix.NAME + " wersja " + Traffix.VERSION + "\n\n\n"
          + "(c) 2004 Tomasz Wróblewski");
      return area;
    }
  }

  class ExportSim extends Action {
    public ExportSim() {
      setText("Eksport eksperymentu");
      setToolTipText("Eksport danych eksperymentu do Excela");
      setImageDescriptor(Images.getDescriptor("icons/export.gif"));
    }

    public void run() {
      onSimExport();
    }
  }

  class ImportFromMcm extends Action {
    public ImportFromMcm() {
      setText("Import Mcm");
      setToolTipText("Importuj dane Mcm");
      setImageDescriptor(Images.getDescriptor("icons/importMcm.gif"));
    }

    public void run() {
      Traffix.model().importMcmData();
    }
  }

  class Merge extends Action {
    Merge() {
      setText("Do³¹cz projekt");
      setToolTipText("Scala dwa projekty");
    }

    @Override
    public void run() {
      Traffix.model().mergeProjects();
      updateTitle();
    }
  }

  class New extends Action {
    public New() {
      setText("Nowy");
      setToolTipText("Nowy projekt");
      setAccelerator(SWT.CTRL | 'N');
      setImageDescriptor(Images.getDescriptor("icons/new.gif"));
    }

    public void run() {
      Traffix.model().create();
    }
  }

  class Open extends Action {
    public Open() {
      setText("Otwórz");
      setToolTipText("Otwórz projekt");
      setAccelerator(SWT.CTRL | 'O');
      setImageDescriptor(Images.getDescriptor("icons/open.gif"));
    }

    public void run() {
      Traffix.model().open();
      if (m_weeklyScheduleTable.getSelectionCount() == 0)
        m_weeklyScheduleTable.setSelection(0);
    }
  }

  class OpenSimulation extends Action {
    public OpenSimulation() {
      setText("Symulacja");
      setToolTipText("Poka¿ okno symulacji");
      setImageDescriptor(Images.getDescriptor("icons/sim.gif"));
    }

    public void run() {
      if (!Traffix.model().isEmpty()) {
        SimFrame f = new SimFrame(getShell(), SimMode.Default);
        // f.setBlockOnOpen(true);
        f.open();
      }
    }
  }

  class ProjectGroupDown extends Action {
    public ProjectGroupDown() {
      setText("W dó³");
      setToolTipText("Przesuniêcie grup jednego projektu w dó³");
    }

    @Override
    public void run() {
      int sel = m_groupTable.getSelectionIndex();
      if (sel != -1) {
        Traffix.model().moveProjectGroupsDown(
            Traffix.model().getGroupByIndex(sel).getJunctionIndex());
        m_groupTable.setSelection((sel + 1) % Traffix.model().getNumGroups());
      }
    }
  }

  class ProjectGroupUp extends Action {
    public ProjectGroupUp() {
      setText("W górê");
      setToolTipText("Przesuniêcie grup jednego projektu w górê");
    }

    @Override
    public void run() {
      int sel = m_groupTable.getSelectionIndex();
      if (sel != -1) {
        Traffix.model().moveProjectGroupsUp(
            Traffix.model().getGroupByIndex(sel).getJunctionIndex());
        m_groupTable.setSelection((sel - 1 + Traffix.model().getNumGroups())
            % Traffix.model().getNumGroups());
      }
    }
  }

  class Quit extends Action {
    public Quit() {
      setText("WyjdŸ");
      setToolTipText("WyjdŸ");
      setAccelerator(SWT.CTRL | 'Q');
      setImageDescriptor(Images.getDescriptor("icons/exit.gif"));
    }

    public void run() {
      if (Traffix.model().close())
        TraffixFrame.this.close();
    }
  }

  class Save extends Action {
    public Save() {
      setImageDescriptor(Images.getDescriptor("icons/save.gif"));
      setText("Zapisz");
      setAccelerator(SWT.CTRL | 'S');
      setToolTipText("Zapisz");
    }

    public void run() {
      Traffix.model().save();
      updateTitle();
    }
  }

  class SaveAs extends Action {
    public SaveAs() {
      setImageDescriptor(Images.getDescriptor("icons/saveas.gif"));
      setText("Zapisz jako...");
      setToolTipText("Zapisz pod inn¹ nazw¹");
    }

    public void run() {
      Traffix.model().saveAs();
      updateTitle();
    }
  }

  class ScheduleBankAdd extends Action {
    public ScheduleBankAdd() {
      setText("Dodaj");
      setToolTipText("Dodaj nowy, pusty harmonogram");
      setImageDescriptor(Images.getDescriptor("icons/plus.gif"));
    }

    public void run() {
      onScheduleBankAdd();
    }
  }

  class ScheduleBankEdit extends Action {
    public ScheduleBankEdit() {
      setText("Uk³adaj");
      setToolTipText("Uk³adaj wybrany harmonogram");
      setAccelerator(SWT.CTRL | 'H');
      setImageDescriptor(Images.getDescriptor("icons/schedule.gif"));
    }

    public void run() {
      onScheduleBankEdit();
    }
  }

  class ScheduleBankExport extends Action {
    public ScheduleBankExport() {
      setText("Eksport");
      setToolTipText("Eksportuj wybrany harmonogram");
      setImageDescriptor(Images.getDescriptor("icons/export.gif"));
    }

    public void run() {
      onScheduleBankExport();
    }
  }

  class ScheduleBankImport extends Action {
    public ScheduleBankImport() {
      setText("Import");
      setToolTipText("Importuj harmonogram z zewnêtrznego pliku");
      setImageDescriptor(Images.getDescriptor("icons/import.gif"));
    }

    public void run() {
      onScheduleBankImport();
    }
  }

  class ScheduleBankRemove extends Action {
    public ScheduleBankRemove() {
      setText("Usuñ");
      setToolTipText("Usuñ wybrany harmonogram");
      setImageDescriptor(Images.getDescriptor("icons/deleteProg.gif"));
    }

    public void run() {
      onScheduleBankRemove();
    }
  }

  class ScheduleBankRename extends Action {
    public ScheduleBankRename() {
      setText("Zmieñ nazwê");
      setToolTipText("Zmieñ nazwê harmonogramu");
      setImageDescriptor(Images.getDescriptor("icons/rename.gif"));
    }

    public void run() {
      onScheduleBankRename();
    }
  }

  class WSEntryAdd extends Action {
    public WSEntryAdd() {
      setText("Dodaj");
      setToolTipText("Dodaj nowy wpis do cyklu tygodniowego");
      setImageDescriptor(Images.getDescriptor("icons/plus.gif"));
    }

    public void run() {
      onWeeklyScheduleAdd();
    }
  }

  class WSEntryEdit extends Action {
    public WSEntryEdit() {
      setText("Zmieñ");
      setToolTipText("Zmieñ wybrany wpis cyklu tygodniowego");
      setImageDescriptor(Images.getDescriptor("icons/edit.gif"));
    }

    public void run() {
      onWeeklyScheduleEdit();
    }
  }

  class WSEntryRemove extends Action {
    public WSEntryRemove() {
      setText("Usuñ");
      setToolTipText("Usuñ wybrany wpis cyklu tygodniowego");
      setImageDescriptor(Images.getDescriptor("icons/deleteProg.gif"));
    }

    public void run() {
      onWeeklyScheduleRemove();
    }

  }

  class WSExcelExport extends Action {
    public WSExcelExport() {
      setText("Eksport do Excela");
      setToolTipText("Eksport cyklu tygodniowego do Excela");
      setImageDescriptor(Images.getDescriptor("icons/export.gif"));
    }

    public void run() {
      onWeeklyScheduleExcelExport();
    }
  }

  class WSExport extends Action {
    public WSExport() {
      setText("Eksport");
      setToolTipText("Eksport cyklu tygodniowego do zewnêtrznego pliku");
      setImageDescriptor(Images.getDescriptor("icons/export.gif"));
    }

    public void run() {
      onWeeklyScheduleExport();
    }

  }

  class WSImport extends Action {
    public WSImport() {
      setText("Import");
      setToolTipText("Import cyklu tygodniowego z zewnêtrznego pliku");
      setImageDescriptor(Images.getDescriptor("icons/import.gif"));
    }

    public void run() {
      onWeeklyScheduleImport();
    }

  }

  class WSStartDay extends Action {
    public WSStartDay() {
      setText("Dzieñ pocz¹tkowy");
      setToolTipText("Zmieñ dzieñ pocz¹tkowy dla cyklu tygodniowego");
      setImageDescriptor(Images.getDescriptor("icons/startday.gif"));
    }

    public void run() {
      onWeeklyScheduleStartDay();
    }

  }

  public TraffixFrame(Shell parent) {
    super(parent);
    // TraffixApp.s_app = this;
    addMenuBar();
    addCoolBar(SWT.NONE);
    Traffix.model().addUpdateListener(new IUpdateListener() {
      public void onUpdate(int hint, Object data) {
        onModelUpdated(hint, data);
      }
    });
  }

  public void updateTitle() {
    getShell().setText(
        Traffix.model().getTitle() + " - " + Traffix.NAME + " " + Traffix.VERSION);
  }

  protected void configureShell(Shell shell) {
    Traffix.model().setShell(shell);
    // TraffixApp.s_shell = shell;
    super.configureShell(shell);
    shell.setText("Traffix");
    shell.setImage(Images.get("icons/traffix.gif"));
    getSeperator1().setVisible(false);
    shell.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        Traffix.simManager().dispose();
        Images.dispose();
        Colors.dispose();
        Cursors.dispose();
      }
    });
    shell.setText(Traffix.model().getTitle() + " - " + Traffix.NAME + " "
        + Traffix.VERSION);
  }

  protected Control createContents(Composite parent) {
    Composite contents = new Composite(parent, SWT.BORDER);
    double[][] size = {
      { 4.0, LatticeLayout.MINIMUM, 4, LatticeLayout.MINIMUM, 4, LatticeLayout.FILL, 4 },
      { 4.0, LatticeLayout.MINIMUM, 4, LatticeLayout.FILL, 4 } };

    LatticeLayout layout = new LatticeLayout(size);
    contents.setLayout(layout);

    Label label = new Label(contents, SWT.NONE);
    label.setText("Grupy Pojazdów");
    label.setLayoutData(new LatticeData("1,1"));
    label.setBackground(label.getDisplay().getSystemColor(SWT.COLOR_WHITE));

    Composite groupPane = buildGroupPane(contents);
    groupPane.setLayoutData(new LatticeData("1,3"));

    label = new Label(contents, SWT.NONE);
    label.setText("Bank Harmonogramów");
    label.setLayoutData(new LatticeData("3,1"));
    label.setBackground(label.getDisplay().getSystemColor(SWT.COLOR_WHITE));

    Composite programBankPane = buildProgramBankPane(contents);
    programBankPane.setLayoutData(new LatticeData("3,3"));

    label = new Label(contents, SWT.NONE);
    label.setText("Cykl Tygodniowy");
    label.setLayoutData(new LatticeData("5,1"));
    label.setBackground(label.getDisplay().getSystemColor(SWT.COLOR_WHITE));

    Composite pane = buildWeeklySchedulePane(contents);
    pane.setLayoutData(new LatticeData("5,3"));

    updateButtons();

    return contents;
  }

  protected CoolBarManager createCoolBarManager(int style) {
    CoolBarManager cm = new CoolBarManager(style);
    cm.add(createToolBarManager(SWT.FLAT | SWT.WRAP));
    return cm;
  }

  protected MenuManager createMenuManager() {
    MenuManager m = new MenuManager();
    MenuManager fileMenu = new MenuManager("&Plik");
    fileMenu.add(m_new);
    fileMenu.add(m_open);
    fileMenu.add(m_save);
    fileMenu.add(m_saveas);
    fileMenu.add(new Separator());
    fileMenu.add(m_merge);
    fileMenu.add(m_importMcm);
    fileMenu.add(new Separator());
    fileMenu.add(m_exit);
    m.add(fileMenu);
    // MenuManager viewMenu = new MenuManager("&Widok");
    // viewMenu.add(m_scheduleEdit);
    // m.add(viewMenu);

    MenuManager helpMenu = new MenuManager("P&omoc");
    helpMenu.add(m_about);
    helpMenu.add(new Separator());

    //
    File helpDir = new File("help");
    File[] files = helpDir.listFiles();
    for (final File f : files) {
      String name = f.getName();
      int p = name.lastIndexOf('.');
      if (p != -1) {
        String ext = name.substring(p + 1).toLowerCase();
        if (ext.equals("png")) {
          Action a = new Action() {
            public void run() {
              Traffix.openHelpPicture(f);
            }
          };
          a.setText(name.substring(0, p));
          helpMenu.add(a);
        }
      }
    }
    //
    m.add(helpMenu);

    return m;
  }

  protected void createStatusLine(Shell parent) {
    super.createStatusLine(parent);
  }

  protected ToolBarManager createToolBarManager(int style) {
    ToolBarManager tm = new ToolBarManager(style);
    tm.add(m_new);
    tm.add(m_open);
    tm.add(m_save);
    tm.add(new Separator());
    tm.add(m_importMcm);
    tm.add(new Separator());
    tm.add(m_openSimulation);
    tm.add(m_exportSim);
    
    Utils.forceToolbarText(tm);
    return tm;
  }

  protected void handleShellCloseEvent() {
    if (Traffix.model().close()) {
      super.handleShellCloseEvent();
    }
  }

  protected void initializeBounds() {
    getShell().setMaximized(true);
  }

  private Composite buildGroupPane(Composite parent) {
    Composite contents = new Composite(parent, SWT.NONE);

    GridLayout layout = new GridLayout(1, false);
    layout.verticalSpacing = 4;
    layout.horizontalSpacing = 4;
    layout.marginHeight = 0;
    layout.marginWidth = 0;

    contents.setLayout(layout);
    GridData data;

    // group toolbar
    Composite tbComp = new Composite(contents, SWT.NONE);
    tbComp.setLayout(new FillLayout());
    ToolBarManager tbManager = new ToolBarManager(SWT.WRAP | SWT.FLAT);
    tbManager.add(m_projectGroupsDown);
    tbManager.add(m_projectGroupsUp);
    m_groupToolbar = tbManager.createControl(tbComp);
    Utils.forceToolbarText(tbManager);

    data = new GridData();
    data.verticalAlignment = SWT.BEGINNING;
    tbComp.setLayoutData(data);

    // group table
    m_groupTable = new Table(contents, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
    data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.grabExcessVerticalSpace = true;
    data.verticalAlignment = SWT.FILL;
    data.horizontalAlignment = SWT.FILL;

    m_groupTable.setLayoutData(data);
    m_groupTable.setHeaderVisible(true);
    m_groupTable.setFocus();

    TableColumn col = new TableColumn(m_groupTable, SWT.LEFT);
    col.setText("Id");
    col.setWidth(40);
    col = new TableColumn(m_groupTable, SWT.LEFT);
    col.setText("Nazwa");
    col.setWidth(80);
    col = new TableColumn(m_groupTable, SWT.LEFT);
    col.setText("Dojazd");
    col.setWidth(30);
    col = new TableColumn(m_groupTable, SWT.LEFT);
    col.setText("Ewakuacja");
    col.setWidth(30);
    col = new TableColumn(m_groupTable, SWT.LEFT);
    col.setText("Klucz");
    col.setWidth(30);

    populateGroupTable();

    return contents;
  }

  private Composite buildProgramBankPane(Composite parent) {
    Composite contents = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(1, false);
    layout.verticalSpacing = 4;
    layout.horizontalSpacing = 4;
    layout.marginHeight = 0;
    layout.marginWidth = 0;

    contents.setLayout(layout);
    GridData data;

    // program bank toolbar
    Composite tbComp = new Composite(contents, SWT.NONE);
    tbComp.setLayout(new FillLayout());
    ToolBarManager tbManager = new ToolBarManager(SWT.WRAP | SWT.FLAT);
    tbManager.add(m_scheduleAdd);
    tbManager.add(m_scheduleRename);
    tbManager.add(m_scheduleEdit);
    tbManager.add(m_scheduleRemove);
    // tbManager.add(new Separator());
    tbManager.add(m_scheduleExport);
    tbManager.add(m_scheduleImport);
    m_scheduleBankToolbar = tbManager.createControl(tbComp);
    Utils.forceToolbarText(tbManager);
    data = new GridData();
    data.verticalAlignment = SWT.BEGINNING;
    tbComp.setLayoutData(data);

    // program bank table
    TableColumn col;
    m_scheduleBankTable = new Table(contents, SWT.BORDER | SWT.SINGLE
        | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
    m_scheduleBankTable.setHeaderVisible(true);
    data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.grabExcessVerticalSpace = true;
    data.verticalAlignment = SWT.FILL;
    data.horizontalAlignment = SWT.FILL;

    m_scheduleBankTable.setLayoutData(data);
    m_scheduleBankTable.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        updateButtons();
      }
    });

    // table context menu
    MenuManager mManager = new MenuManager();
    // mManager.add(m_scheduleAdd);
    mManager.add(m_scheduleRename);
    mManager.add(m_scheduleEdit);
    mManager.add(m_scheduleRemove);
    mManager.add(new Separator());
    mManager.add(m_scheduleExport);
    // mManager.add(m_scheduleImport);
    m_scheduleBankTableMenu = mManager.createContextMenu(m_scheduleBankTable);

    m_scheduleBankTable.addMouseListener(new MouseAdapter() {
      public void mouseUp(MouseEvent e) {
        if (e.button == 3) {
          Point p = m_scheduleBankTable.toDisplay(e.x, e.y);
          m_scheduleBankTableMenu.setLocation(p.x, p.y);
          m_scheduleBankTableMenu.setVisible(true);
        }
      }
    });

    col = new TableColumn(m_scheduleBankTable, SWT.LEFT);
    col.setText("Program");
    col.setWidth(140);
    col = new TableColumn(m_scheduleBankTable, SWT.LEFT);
    col.setText("Cykl");
    col.setWidth(60);
    m_scheduleBankTableViewer = new TableViewer(m_scheduleBankTable);
    ScheduleBank b = Traffix.model().getScheduleBank();
    m_scheduleBankTableViewer.setContentProvider(b.createContentProvider());
    m_scheduleBankTableViewer.setLabelProvider(b.createLabelProvider());
    m_scheduleBankTableViewer.setSorter(b.createSorter());
    m_scheduleBankTableViewer.setInput(b);
    m_scheduleBankTableViewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        if (m_scheduleBankTable.getSelectionCount() > 0)
          m_scheduleEdit.run();
      }
    });
    return contents;
  }

  private Composite buildWeeklySchedulePane(Composite parent) {
    Composite contents = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(1, false);
    layout.verticalSpacing = 4;
    layout.horizontalSpacing = 4;
    layout.marginHeight = 0;
    layout.marginWidth = 0;

    contents.setLayout(layout);
    GridData data;

    // scheudule toolbar
    Composite tbComp = new Composite(contents, SWT.NONE);
    tbComp.setLayout(new FillLayout());
    ToolBarManager tbManager = new ToolBarManager(SWT.WRAP | SWT.FLAT);
    tbManager.add(m_wsAdd);
    tbManager.add(m_wsEdit);
    tbManager.add(m_wsRemove);
    tbManager.add(m_wsStartDay);
    tbManager.add(m_wsExport);
    tbManager.add(m_wsExcelExport);
    tbManager.add(m_wsImport);
    m_weeklyScheduleToolbar = tbManager.createControl(tbComp);
    Utils.forceToolbarText(tbManager);

    data = new GridData();
    data.verticalAlignment = SWT.BEGINNING;
    tbComp.setLayoutData(data);

    // schedule table
    TableColumn col;
    m_weeklyScheduleTable = new Table(contents, SWT.BORDER | SWT.SINGLE
        | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
    m_weeklyScheduleTable.setHeaderVisible(true);
    data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.grabExcessVerticalSpace = true;
    data.verticalAlignment = SWT.FILL;
    data.horizontalAlignment = SWT.FILL;

    m_weeklyScheduleTable.setLayoutData(data);

    col = new TableColumn(m_weeklyScheduleTable, SWT.LEFT);
    col.setWidth(30);

    col = new TableColumn(m_weeklyScheduleTable, SWT.LEFT);
    col.setText("Program");
    col.setWidth(100);
    col = new TableColumn(m_weeklyScheduleTable, SWT.RIGHT);
    col.setText("Dzieñ");
    col.setWidth(80);
    col = new TableColumn(m_weeklyScheduleTable, SWT.RIGHT);
    col.setText("Godzina");
    col.setWidth(60);
    col = new TableColumn(m_weeklyScheduleTable, SWT.RIGHT);
    col.setText("Offset");
    col.setWidth(60);
    // m_weeklyScheduleTable.setFocus();
    m_weeklyScheduleTableViewer = new TableViewer(m_weeklyScheduleTable);
    WeeklySchedule s = Traffix.model().getWeeklySchedule();
    m_weeklyScheduleTableViewer.setContentProvider(s.createContentProvider());
    m_weeklyScheduleTableViewer.setLabelProvider(s.createLabelProvider());
    m_weeklyScheduleTableViewer.setSorter(s.createSorter());
    m_weeklyScheduleTableViewer.setInput(s);

    m_weeklyScheduleTableViewer
        .addPostSelectionChangedListener(new ISelectionChangedListener() {
          public void selectionChanged(SelectionChangedEvent event) {
            updateButtons();
          }
        });
    m_weeklyScheduleTableViewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        if (m_weeklyScheduleTable.getSelectionCount() > 0)
          m_wsEdit.run();
      }
    });

    // table context menu
    MenuManager mManager = new MenuManager();
    // mManager.add(m_wsAdd);
    mManager.add(m_wsEdit);
    mManager.add(m_wsRemove);
    mManager.add(new Separator());
    mManager.add(m_wsExport);
    m_weeklyScheduleTableMenu = mManager.createContextMenu(m_weeklyScheduleTable);

    m_weeklyScheduleTable.addMouseListener(new MouseAdapter() {
      public void mouseUp(MouseEvent e) {
        if (e.button == 3) {
          Point p = m_weeklyScheduleTable.toDisplay(e.x, e.y);
          m_weeklyScheduleTableMenu.setLocation(p.x, p.y);
          m_weeklyScheduleTableMenu.setVisible(true);
        }
      }
    });

    return contents;
  }

  private void errNoGroups() {
    Traffix.error("Brak za³adowanych grup.");
  }

  private void errNoSchedules() {
    Traffix.error("Brak jakichkolwiek programów.");
  }

  private String getExcelCellName(int x, int y) {
    int numletters = 'Z' - 'A' + 1;
    String colstr = "" + (char) (x % numletters + 'A');
    if (x >= numletters) {
      x -= numletters;
      colstr = "A" + colstr;
    }

    return colstr + Integer.toString(y + 1);
  }

  private void onModelUpdated(int hint, Object data) {
    // if (e.flags.test(ModelEvent.F_ALL_CHANGED)) {
    // m_scheduleBankTableViewer.setInput(Traffix.getModel().getScheduleBank());
    //
    // }

    if (hint == Model.EVT_CHANGE_GROUPS || hint == IUpdateable.NO_HINT)
      populateGroupTable();

    // update weekly schedule table
    if (hint == IUpdateable.NO_HINT ||
        hint == Model.EVT_CHANGE_SCHEDULE
        || hint == Model.EVT_CHANGE_SCHEDULEBANK
        || hint == Model.EVT_CHANGE_WEEKLYSCHEDULE) {
      m_weeklyScheduleTableViewer.setInput(Traffix.model().getWeeklySchedule());
      m_weeklyScheduleTableViewer.setSorter(Traffix.model().getWeeklySchedule()
          .createSorter());
      m_weeklyScheduleTableViewer.refresh();
    }

    // update schedule bank table
    if (hint == IUpdateable.NO_HINT ||
        hint == Model.EVT_CHANGE_SCHEDULE ||
        hint == Model.EVT_CHANGE_SCHEDULEBANK) {
      m_scheduleBankTableViewer.setInput(Traffix.model().getScheduleBank());
      m_scheduleBankTableViewer.refresh();
    }

    updateTitle();
    updateButtons();
  }

  private void onScheduleBankAdd() {
    if (Traffix.model().getNumGroups() == 0) {
      errNoGroups();
      return;
    }

    String name = "PUSTY_0";
    Schedule[] currentSchedules = Traffix.model().getScheduleBank().getSchedules();

    boolean nameOk = false;
    int id = 1;
    while (!nameOk) {
      nameOk = true;
      for (int i = 0; i < currentSchedules.length; ++i) {
        if (currentSchedules[i].getName().equals(name)) {
          nameOk = false;
          name = "PUSTY_" + Integer.toString(id++);
          break;
        }
      }
    }

    Schedule schedule = Schedule.createRed(Traffix.model().getNumGroups(), name);

    Traffix.model().addSchedule(schedule);
  }

  private void onScheduleBankEdit() {
    int index = m_scheduleBankTable.getSelectionIndex();
    if (index != -1) {
      int numGroups = Traffix.model().getNumGroups();
      if (numGroups == 0) {
        errNoGroups();
        return;
      }

      Schedule schedule = (Schedule) m_scheduleBankTableViewer.getElementAt(index);
      Schedule workingCopy = (Schedule) schedule.clone();

      ScheduleEditorFrame frame = new ScheduleEditorFrame(getShell());
      // frame.setBlockOnOpen(true);
      frame.setEditedSchedule(workingCopy);
      frame.setReferenceSchedule(schedule);
      frame.open();
    } else {
      MessageBox dlg = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
      dlg.setText(Traffix.NAME);
      dlg.setMessage("Brak wybranego harmonogramu.");
      dlg.open();
    }
  }

  private void onScheduleBankExport() {
    int index = m_scheduleBankTable.getSelectionIndex();
    if (index == -1) {
      Traffix.error("Najpierw wybierz harmonogram.");
      return;
    }

    Schedule schedule = (Schedule) m_scheduleBankTableViewer.getElementAt(index);
    FileDialog dlg = new FileDialog(getShell(), SWT.SAVE);
    dlg.setText("Eksport harmonogramu");
    dlg.setFilterExtensions(new String[] { "*.har", "*.*" });
    dlg.setFileName(schedule.getName() + ".har");
    String filename = dlg.open();
    if (filename != null) {
      Document doc = XmlKit.createDoc();
      Element e = schedule.xmlSave(doc);
      doc.appendChild(e);
      XmlKit.saveDoc(filename, doc);
    }
  }

  private void onScheduleBankImport() {
    FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
    dlg.setText("Import harmonogramu");
    dlg.setFilterExtensions(new String[] { "*.har", "*.*" });
    String filename = dlg.open();
    if (filename != null) {
      Document doc = XmlKit.loadDoc(filename);
      if (doc != null) {
        Schedule other = new Schedule();
        if (!other.xmlLoad(doc, doc.getDocumentElement())) {
          Traffix.error("B³êdny format do³¹czanego harmonogramu.");
          return;
        }

        int numGroups = Traffix.model().getNumGroups();
        if (numGroups != other.getNumGroups()) {
          Traffix.error("Nie zgadza siê iloœæ grup w do³¹czanym harmongramie.");
          return;
        }

        InputDialog nameDlg = new InputDialog(getShell(), Traffix.NAME,
            "Ustal nazwê do³¹czanego harmonogramu", other.getName(),
            m_scheduleNameValidator);
        if (nameDlg.open() == InputDialog.OK) {
          other.setName(nameDlg.getValue());
          //
          Traffix.model().addSchedule(other);
        }
      }
    }
  }

  private void onScheduleBankRemove() {
    int index = m_scheduleBankTable.getSelectionIndex();
    if (index == -1) {
      Traffix.error("Brak wybranego harmonogramu.");
      return;
    }

    Schedule schedule = (Schedule) m_scheduleBankTableViewer.getElementAt(index);

    MessageBox dlg = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
    dlg.setText(Traffix.NAME);
    dlg.setMessage("Uwaga! Czy na pewno usun¹æ program '" + schedule.getName() + "'?");
    if (dlg.open() == SWT.YES) {
      Traffix.model().removeSchedule(schedule.getName());
      m_scheduleBankTable.setSelection(Math.min(m_scheduleBankTable.getItemCount() - 1,
          index));
      updateButtons();
    }
  }

  private void onScheduleBankRename() {
    int index = m_scheduleBankTable.getSelectionIndex();
    if (index == -1) {
      Traffix.error("Najpierw wybierz program");
      return;
    }

    Schedule schedule = (Schedule) m_scheduleBankTableViewer.getElementAt(index);
    String msg = "Zmiana nazwy programu '" + schedule.getName() + "'";
    String init = schedule.getName();
    InputDialog dlg = new InputDialog(getShell(), Traffix.NAME, msg, init,
        m_scheduleNameValidator);
    if (dlg.open() == InputDialog.OK) {
      try {
        Traffix.model().renameSchedule(schedule.getName(), dlg.getValue());
      } catch (BadNameException e) {
        Traffix.error("Nazwa '" + dlg.getValue() + "' jest ju¿ zajêta.");
      }
    }
  }

  private void onSimExport() {
    new StatisticsExporter().exportExperiments();
  }

  private void onWeeklyScheduleAdd() {
    Model m = Traffix.model();
    if (m.getScheduleBank().getNumSchedules() == 0) {
      errNoSchedules();
      return;
    }

    int index = m_scheduleBankTable.getSelectionIndex();
    if (index == -1) {
      Traffix.error("Najpierw wybierz program z banku programów.");
      return;
    }

    Schedule schedule = (Schedule) m_scheduleBankTableViewer.getElementAt(index);
    WeeklyScheduleEntry e = new WeeklyScheduleEntry();
    e.scheduleName = schedule.getName();
    e.day = m.getWeeklyScheduleStartDay();
    e.hour = 0;
    e.minute = 0;
    e.offset = 0;
    Traffix.model().addWeeklyScheduleEntry(e);

  }

  private void onWeeklyScheduleEdit() {
    int index = m_weeklyScheduleTable.getSelectionIndex();
    if (index == -1) {
      Traffix.error("Najpierw wybierz krotkê.");
      return;
    }
    WeeklyScheduleEntry e = (WeeklyScheduleEntry) m_weeklyScheduleTableViewer
        .getElementAt(index);
    WeeklyScheduleEntryDialog dlg = new WeeklyScheduleEntryDialog(getShell(), e);
    if (dlg.open() == WeeklyScheduleEntryDialog.OK) {
      Traffix.model().setModified(true);
      Traffix.model().fireUpdated(Model.EVT_CHANGE_WEEKLYSCHEDULE, null);
    }
  }

  private void onWeeklyScheduleExcelExport() {
    Excel excel = new Excel(getShell());
    excel.setVisible(true);
    excel.addWorkbook();
    OleAutomation sheet = excel.getWorksheet(1);

    int bx = 0;
    int by = 0;
    excel.setCellValue(sheet, getExcelCellName(bx, by), "Program");
    excel.setCellValue(sheet, getExcelCellName(bx + 1, by), "Dzieñ");
    excel.setCellValue(sheet, getExcelCellName(bx + 2, by), "Godzina");
    excel.setCellValue(sheet, getExcelCellName(bx + 3, by), "Offset");
    excel.setCellBold(sheet, getExcelCellName(bx, by) + ":"
        + getExcelCellName(bx + 3, by), true);
    ++by;
    TableItem[] items = m_weeklyScheduleTable.getItems();
    for (int i = 0; i < items.length; ++i) {
      for (int j = 0; j < 4; ++j) {
        String txt = items[i].getText(j + 1);
        if (j != 3)
          excel.setCellValue(sheet, getExcelCellName(bx + j, by + i), txt);
        else
          excel.setCellValue(sheet, getExcelCellName(bx + j, by + i), txt);
      }
    }

    excel.alignRight(sheet, getExcelCellName(bx + 1, by - 1) + ":"
        + getExcelCellName(bx + 3, by + items.length - 1));

    excel.dispose();
  }

  private void onWeeklyScheduleExport() {
    int numEntries = Traffix.model().getWeeklySchedule().getNumEntries();
    if (numEntries == 0) {
      Traffix.error("Nie ma co eksportowaæ - jest 0 wpisów w cyklu tygodniowym.");
      return;
    }

    FileDialog dlg = new FileDialog(getShell(), SWT.SAVE);
    dlg.setText("Eksport cyklu tygodniowego");
    dlg.setFilterExtensions(new String[] { "*.tyg", "*.*" });
    dlg.setFileName(Traffix.model().getBaseFileName() + ".tyg");
    String filename = dlg.open();
    if (filename != null) {
      Document doc = XmlKit.createDoc();
      Element e = Traffix.model().getWeeklySchedule().xmlSave(doc);
      doc.appendChild(e);
      XmlKit.saveDoc(filename, doc);
    }
  }

  private void onWeeklyScheduleImport() {
    if (Traffix.model().isModified()) {
      MessageBox box = new MessageBox(getShell(), SWT.YES | SWT.NO);
      box.setText(Traffix.NAME);
      box.setMessage("Import cyklu spowoduje wymazanie aktualnego! Kontynuowaæ?");
      if (box.open() != SWT.YES)
        return;
    }

    FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
    dlg.setText("Import cyklu tygodniowego");
    dlg.setFilterExtensions(new String[] { "*.tyg", "*.*" });
    String filename = dlg.open();
    if (filename != null) {
      Document doc = XmlKit.loadDoc(filename);
      if (doc != null) {
        WeeklySchedule ws = new WeeklySchedule(Traffix.model().getScheduleBank());
        if (!ws.xmlLoad(doc, doc.getDocumentElement())) {
          Traffix.error("B³êdny format pliku.");
          return;
        }

        Traffix.model().setWeeklySchedule(ws);
      }
    }
  }

  private void onWeeklyScheduleRemove() {
    int index = m_weeklyScheduleTable.getSelectionIndex();
    if (index == -1) {
      Traffix.error("Najpierw wybierz krotkê.");
      return;
    }

    WeeklyScheduleEntry e = (WeeklyScheduleEntry) m_weeklyScheduleTableViewer
        .getElementAt(index);
    Traffix.model().removeWeeklyScheduleEntry(e);
    m_weeklyScheduleTable.setSelection(Math.min(m_weeklyScheduleTable.getItemCount() - 1,
        index));
    updateButtons();
  }

  private void onWeeklyScheduleStartDay() {
    ListSelectionDialog dlg = new ListSelectionDialog(getShell(), Traffix.NAME,
        "Wybierz dzieñ pocz¹tkowy");
    dlg.setItems(Weekdays.names);
    dlg.setSelectedIndex(Traffix.model().getWeeklyScheduleStartDay());
    if (dlg.open() == ListSelectionDialog.OK) {
      Traffix.model().setWeeklyScheduleStartDay(dlg.getSelectedIndex());
    }
  }

  private void openHelpPicture(File f) {
    HelpPictureFrame fr = new HelpPictureFrame(getShell(), f);
    fr.setBlockOnOpen(false);
    fr.open();
  }

  private void populateGroupTable() {
    if (m_groupTable == null || m_groupTable.isDisposed())
      return;

    m_groupTable.removeAll();
    Model m = Traffix.model();
    for (int i = 0; i < m.getNumGroups(); ++i) {
      VehicleGroup g = m.getGroupByIndex(i);
      TableItem item = new TableItem(m_groupTable, SWT.NONE);
      item.setText(0, g.getElectricName());
      item.setText(1, g.getName());
      item.setText(2, Integer.toString(g.getApproachSpeed()));
      item.setText(3, Integer.toString(g.getEvacuationSpeed()));
      item.setText(4, Integer.toString(g.getUniqueID()));
    }
    m_groupTable.redraw();
  }

  private void updateButtons() {
    int selSched = m_scheduleBankTable.getSelectionIndex();
    int selEntry = m_weeklyScheduleTable.getSelectionIndex();
    Model m = Traffix.model();
    m_scheduleAdd.setEnabled(m.getNumGroups() > 0);
    m_scheduleEdit.setEnabled(selSched != -1);
    m_scheduleExport.setEnabled(selSched != -1);
    m_scheduleRemove.setEnabled(selSched != -1);
    m_scheduleRename.setEnabled(selSched != -1);
    m_scheduleImport.setEnabled(m.getNumGroups() > 0);
    m_wsAdd.setEnabled(selSched != -1);
    m_wsEdit.setEnabled(selEntry != -1);
    m_wsRemove.setEnabled(selEntry != -1);
    m_wsExport.setEnabled(m.getWeeklySchedule().getNumEntries() > 0);
    m_wsExcelExport.setEnabled(m.getWeeklySchedule().getNumEntries() > 0);
    m_wsImport.setEnabled(true);
    m_openSimulation.setEnabled(!m.isEmpty());
    m_exportSim.setEnabled(!m.isEmpty());
  }
}
