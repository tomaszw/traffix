/*
 * Created on 2004-07-20
 */
package traffix.ui.sim;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.tw.geometry.Rectanglef;

import traffix.Config;
import traffix.NativeUtils;
import traffix.Traffix;
import traffix.core.Time;
import traffix.core.accident.IAccidentModel;
import traffix.core.sim.ISimManager;
import traffix.core.sim.StatisticsExporter;
import traffix.ui.Colors;
import traffix.ui.Images;
import traffix.ui.sim.tools.*;
import za.co.quirk.layout.LatticeData;
import za.co.quirk.layout.LatticeLayout;

public class SimFrame extends Window {
  private MapEditor m_editor;
  private Menu      m_filterMenu, m_menu;

  private boolean   m_playing, m_playingOneStep;
  private int       m_playStartTime;

  private IAction   m_simulate, m_simulateStep, m_simulateUpto, m_simulateFast;
  private IAction[] m_toolActions;
  private ToolBar   m_toolBar;
  private Table     m_vehiclePathsTable;
  private ComboItem m_zoomItem;
  private SimMode   m_mode;
  private Combo     m_simsCombo;
  private ToolBar   m_toolsToolBar;
  private Composite m_rightPane;
  private Thread    m_playThread;
  private KeyListener m_stopSimKeyListener;

  public SimFrame(Shell parentShell, SimMode mode) {
    super(parentShell);
    m_mode = mode;
  }

  public boolean close() {
    if (m_playing)
      return false;

    return super.close();
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Okno symulacji");
    newShell.setImage(Images.get("icons/sim.gif"));

    // m_menu = new Menu(newShell, SWT.BAR);
    // MenuItem viewIt = new MenuItem(m_menu, SWT.CASCADE);
    // viewIt.setText("&Widok");
    // Menu view = new Menu(viewIt);
    // viewIt.setMenu(view);
    //    
    // MenuItem it;
    // it = new MenuItem(view, SWT.CHECK);
    // it.setText("Tory");
    // it = new MenuItem(view, SWT.CHECK);
    // it.setText("Œwiat³a");
    // it = new MenuItem(view, SWT.CHECK);
    // it.setText("Przejœcia pieszych");
    // it = new MenuItem(view, SWT.CHECK);
    // it.setText("Przystanki");
    // it = new MenuItem(view, SWT.CHECK);
    // it.setText("Detektory");
    // it = new MenuItem(view, SWT.CHECK);
    // it.setText("Bariery");
    //    
    // newShell.setMenuBar(m_menu);
  }

  private void switchTools(SimMode mode) {
    // if (m_mode == mode)
    // return;

    ToolBarManager tm = new ToolBarManager(SWT.WRAP | SWT.FLAT | SWT.VERTICAL);
    if (mode == SimMode.Default)
      initDefaultModeToolActions();
    else
      initAccidentsModeToolActions();

    for (int i = 0; i < m_toolActions.length; ++i)
      tm.add(m_toolActions[i]);
    m_toolsToolBar.dispose();
    m_toolsToolBar = tm.createControl(m_rightPane);
    m_toolsToolBar.setLayoutData(new LatticeData("1,0"));
    m_rightPane.layout();
    selectTool(0);
  }

  protected Control createContents(Composite parent) {
    parent.setLayout(new FillLayout());
    Composite contents = new Composite(parent, SWT.NONE);
    double[][] sizes = {
      { 2.0, 0, 0, LatticeLayout.FILL, 2 },
      { 0.0, LatticeLayout.PREFERRED, 5, LatticeLayout.PREFERRED, 0, LatticeLayout.FILL,
        2 } };
    LatticeLayout lay = new LatticeLayout(sizes);
    contents.setLayout(lay);

    m_rightPane = new Composite(contents, SWT.NONE);
    LatticeLayout rpLayout = new LatticeLayout(
        new double[][] { { 2.0, LatticeLayout.PREFERRED, 2, LatticeLayout.FILL, 0 },
          { LatticeLayout.FILL } });

    m_rightPane.setLayout(rpLayout);
    m_rightPane.setLayoutData(new LatticeData("3,5"));

    ToolBarManager tm = new ToolBarManager(SWT.WRAP | SWT.FLAT | SWT.VERTICAL);
    if (m_mode == SimMode.Default)
      initDefaultModeToolActions();
    else
      initAccidentsModeToolActions();

    for (int i = 0; i < m_toolActions.length; ++i)
      tm.add(m_toolActions[i]);

    m_toolsToolBar = tm.createControl(m_rightPane);
    m_toolsToolBar.setLayoutData(new LatticeData("1,0"));
    m_toolActions[0].setChecked(true);

    m_editor = new MapEditor(m_rightPane, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
    m_editor.setLayoutData(new LatticeData("3,0"));
    ToolBar tb = createToolBar(contents);
    tb.setLayoutData(new LatticeData("1,1,3,1"));
//    ToolBar tb2 = createToolBar2(contents);
//    tb2.setLayoutData(new LatticeData("1,3,3,3"));

    m_editor.getCanvas().setFocus();
    m_editor.getCanvas().forceFocus();
    m_editor.setCurrentState(new GroupsTool(m_editor));

    m_editor.getCanvas().addKeyListener(new KeyListener() {

      public void keyPressed(KeyEvent arg0) {
        if (arg0.character == '\r')
          onSimPlayStep();
      }

      public void keyReleased(KeyEvent arg0) {
      }

    });
    m_editor.setBackground(Colors.get(new RGB(0xff,0xff,0xff)));
    return contents;
  }

  protected void initializeBounds() {
    getShell().setSize(640, 480);
  }

  private ToolBar createToolBar2(Composite parent) {
    ToolBarManager tm = new ToolBarManager(SWT.FLAT | SWT.WRAP);
    ComboItem simitem = new ComboItem("");
    tm.add(simitem);

    Action a = null;
    tm.add(a = new Action() {
      @Override
      public void run() {
        onNewAccident();
      }
    });
    a.setImageDescriptor(Images.getDescriptor("icons/plus.gif"));
    a.setToolTipText("Stworzenie nowego wypadku");

    tm.add(a = new Action() {});
    a.setImageDescriptor(Images.getDescriptor("icons/xmark.gif"));
    a.setToolTipText("Usuniêcie aktualnego wypadku");
    tm.add(a = new Action() {
      @Override
      public void run() {
        IAccidentModel am = Traffix.model().getActiveAccident();
        if (am == null)
          return;
        AccidentPartsDialog dlg = new AccidentPartsDialog(getShell(), am.participants(),
            Traffix.model().getActiveAccident());
        if (dlg.open() == AccidentPartsDialog.OK) {
          am.setParticipants(dlg.getParticipants());
        }
      }
    });
    a.setImageDescriptor(Images.getDescriptor("icons/accidentParts.gif"));
    a.setToolTipText("Definicja uczestników wypadku");
    ToolBar tb = tm.createControl(parent);
    m_simsCombo = simitem.m_combo;
    m_simsCombo.addSelectionListener(new SelectionListener() {

      public void widgetDefaultSelected(SelectionEvent arg0) {
      }

      public void widgetSelected(SelectionEvent e) {
        String[] items = m_simsCombo.getItems();
        int idx = m_simsCombo.getSelectionIndex();

        if (idx == 0 && m_mode != SimMode.Default) {
          m_mode = SimMode.Default;
          switchTools(SimMode.Default);
        }
        if (idx != 0 && m_mode != SimMode.Accidents) {
          m_mode = SimMode.Accidents;
          switchTools(SimMode.Accidents);
        }
        if (idx != -1) {
          Traffix.model().activateSimManager(items[idx]);
          Traffix.simManager().getGraph().update();
          Traffix.simManager().fireUpdated();
          syncZoomCombo();
        }
      }

    });

    populateSimsCombo();
    return tb;
  }

  private void populateSimsCombo() {
    m_simsCombo.removeAll();
    for (String s : Traffix.model().getSimManagerNames()) {
      m_simsCombo.add(s);
    }
    syncSimsCombo();
  }

  private void syncSimsCombo() {
    String name = Traffix.model().getActiveSimManager().getName();
    if (name == null)
      m_simsCombo.select(0);
    else {
      String[] items = m_simsCombo.getItems();
      for (int i = 0; i < items.length; i++) {
        if (items[i].equals(name)) {
          m_simsCombo.select(i);
          return;
        }
      }
    }
  }

  private void syncZoomCombo() {
    float zoom = Traffix.simManager().getZoom();
    zoom *= 100;
    zoom /= 5;
    zoom = 40 - zoom;
    int idx = Math.round(zoom);
    m_zoomItem.m_combo.select(idx);
  }

  protected void onNewAccident() {
    NewAccidentDialog dlg = new NewAccidentDialog(getShell());
    if (dlg.open() == dlg.OK) {
      Traffix.model().addAccident(dlg.name);
      Traffix.model().activateSimManager(dlg.name);
      Traffix.simManager().getGraph().update();
      Traffix.simManager().fireUpdated();
      populateSimsCombo();
      syncZoomCombo();
      m_editor.getCanvas().redraw();
      if (m_mode != SimMode.Accidents) {
        m_mode = SimMode.Accidents;
        switchTools(m_mode);
      }
    }
  }

  private ToolBar createToolBar(Composite parent) {
    ToolBarManager tm = new ToolBarManager(SWT.FLAT | SWT.WRAP);
    Action a = null;
    tm.add(a=new Action("Import obrazu", Images.getDescriptor("icons/importImage.gif")) {
      public void run() {
        onImportImage();
      }
    });
    a.setToolTipText("Import obrazu");
    
    tm.add(a=new Action("Eksport danych", Images.getDescriptor("icons/export.gif")) {
      public void run() {
        onExportData();
      }
    });
    a.setToolTipText("Eksport danych");

    tm.add(a=new Action("Przeskalowanie", Images.getDescriptor("icons/rescale.gif")) {
      public void run() {
        onRescaleImage();
      }
    });
    a.setToolTipText("Przeskalowanie");

    tm
        .add(a=new Action("Parametry symulacji", Images
            .getDescriptor("icons/simparams.gif")) {
          public void run() {
            onSimParams();
          }
        });
    a.setToolTipText("Parametry symulacji");

    tm.add(a=new Action("Detektory...", Images.getDescriptor("icons/macierzdet.gif")) {
      public void run() {
        onSimDetectorsTable();
      }
    });
    a.setToolTipText("Detektory");

    tm.add(new Separator());

    m_zoomItem = new ComboItem("Powiêkszenie");
    tm.add(m_zoomItem);

    tm.add(new Separator());
    tm.add(a=new Action("Przewiñ i generuj", Images.getDescriptor("icons/rewind.gif")) {
      public void run() {
        onSimRewindAndGenerate();
      }
    });
    a.setToolTipText("Przewiñ i generuj");

    tm.add(a=new Action("Przewiñ", Images.getDescriptor("icons/przewin.gif")) {
      public void run() {
        onSimRewind();
      }
    });
    a.setToolTipText("Przewiñ");

    tm.add(m_simulate = new Action("Symuluj", Action.AS_CHECK_BOX) {
      public void run() {
        onSimPlay();
      }
    });
    m_simulate.setImageDescriptor(Images.getDescriptor("icons/play.gif"));
    m_simulate.setToolTipText("Symuluj");
    
    tm.add(m_simulateFast = new Action("Symuluj szybko", Action.AS_CHECK_BOX) {
      public void run() {
        onSimPlayFast();
      }
    });
    m_simulateFast.setImageDescriptor(Images.getDescriptor("icons/playFast.gif"));
    m_simulateFast.setToolTipText("Symuluj szybko");
    
    tm.add(m_simulateUpto = new Action("Symuluj do", Images
        .getDescriptor("icons/playdo.gif")) {
      public void run() {
        onSimPlayUpto();
      }
    });
    m_simulateUpto.setToolTipText("Symuluj do czasu");
    
    tm.add(m_simulateStep = new Action("Symuluj 1s", Images
        .getDescriptor("icons/play1s.gif")) {
      @Override
      public void run() {
        onSimPlayStep();
      }
    });
    m_simulateStep.setToolTipText("Symuluj 1s");
    
    tm.add(a=new Action("Film", Images.getDescriptor("icons/movie.gif")) {
      public void run() {
        onMakeMovie();
      }
    });
    a.setToolTipText("Film");

    tm.add(a=new Action("Reset totalny", Images.getDescriptor("icons/resetsim.gif")) {
      public void run() {
        onSimReset();
      }
    });
    a.setToolTipText("Reset symulacji");

    //Utils.forceToolbarText(tm);
    ToolBar tb = tm.createControl(parent);

    for (int i = 200; i != 0; i -= 5) {
      String txt = Integer.toString(i) + "%";
      m_zoomItem.m_combo.add(txt);
    }
    float zoom = Traffix.simManager().getZoom();
    int selIdx = (int) ((zoom * 100 - 200) / -5);

    m_zoomItem.m_combo.select(selIdx);
    m_zoomItem.m_combo.setVisibleItemCount(20);
    m_zoomItem.m_combo.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        int id = m_zoomItem.m_combo.getSelectionIndex();
        float zoom = (200 - 5 * id) / 100.0f;
        Traffix.simManager().setZoom(zoom);
      }
    });

    return tb;
  }

  private void initDefaultModeToolActions() {
    java.util.List<Action> acts = new LinkedList<Action>();
    Action marqueeTool = new Action("Obszar filmu", IAction.AS_RADIO_BUTTON) {
      public void run() {
        onMarqueeTool();
      }
    };
    marqueeTool.setImageDescriptor(Images.getDescriptor("icons/marqueeTool.gif"));
    marqueeTool.setToolTipText("Narzêdzie obszaru filmowania");

    Action pathsTool = new Action("Tory", IAction.AS_RADIO_BUTTON) {
      public void run() {
        onPathsTool();
      }
    };
    pathsTool.setImageDescriptor(Images.getDescriptor("icons/pathsTool.gif"));
    pathsTool.setToolTipText("Narzêdzie torów");

    Action lightsTool = new Action("Sygnalizatory", IAction.AS_RADIO_BUTTON) {
      public void run() {
        onLightsTool();
      }
    };
    lightsTool.setImageDescriptor(Images.getDescriptor("icons/lightsTool.gif"));
    lightsTool.setToolTipText("Narzêdzie sygnalizatorów");

    Action groupsTool = new Action("Grupy", IAction.AS_RADIO_BUTTON) {
      public void run() {
        onGroupsTool();
      }
    };
    groupsTool.setImageDescriptor(Images.getDescriptor("icons/groupsTool.gif"));
    groupsTool.setToolTipText("Narzêdzie grup");

    Action pedestrianTool = new Action("Piesi", IAction.AS_RADIO_BUTTON) {
      public void run() {
        super.run();
        onPedestrianTool();
      }
    };
    pedestrianTool.setImageDescriptor(Images.getDescriptor("icons/pedestrianTool.gif"));
    pedestrianTool.setToolTipText("Narzêdzie pieszych i rowerzystów");

    Action stopTool = new Action("Przystanki", IAction.AS_RADIO_BUTTON) {
      public void run() {
        super.run();
        onBusStopTool();
      }
    };
    stopTool.setImageDescriptor(Images.getDescriptor("icons/busStopTool.gif"));
    stopTool.setToolTipText("Narzêdzie przystanków");

    Action transDetectorTool = new Action("Detektory tranzytu", IAction.AS_RADIO_BUTTON) {
      public void run() {
        super.run();
        onTransitDetectorTool();
      }
    };
    transDetectorTool.setImageDescriptor(Images
        .getDescriptor("icons/transitDetectorTool.gif"));
    transDetectorTool.setToolTipText("Narzêdzie detektorów tranzytu");

    Action presDetectorTool = new Action("Detektory obecnoœci", IAction.AS_RADIO_BUTTON) {
      public void run() {
        super.run();
        onPresenceDetectorTool();
      }
    };
    presDetectorTool.setImageDescriptor(Images
        .getDescriptor("icons/presenceDetectorTool.gif"));
    presDetectorTool.setToolTipText("Narzêdzie detektorów obecnoœci");

    Action pedeDetectorTool = new Action("Detektory pieszych", IAction.AS_RADIO_BUTTON) {
      public void run() {
        super.run();
        onPedestrianDetectorTool();
      }
    };
    pedeDetectorTool.setImageDescriptor(Images
        .getDescriptor("icons/pedestrianDetectorTool.gif"));
    pedeDetectorTool.setToolTipText("Narzêdzie detektorów pieszych");

    Action condClearDetectorTool = new Action("Detektory kasuj¹ce", IAction.AS_RADIO_BUTTON) {
      public void run() {
        super.run();
        onCondClearDetectorTool();
      }
    };
    condClearDetectorTool.setImageDescriptor(Images.getDescriptor("icons/condclearDetectorTool.gif"));
    condClearDetectorTool.setToolTipText("Narzêdzie detektorów kasuj¹cych");
    
    Action barDetectorTool = new Action("Detektory barrier", IAction.AS_RADIO_BUTTON) {
      public void run() {
        super.run();
        onBarrierDetectorTool();
      }
    };
    barDetectorTool.setImageDescriptor(Images
        .getDescriptor("icons/barrierDetectorTool.gif"));
    barDetectorTool.setToolTipText("Bariera - detektor");

    Action barTool = new Action("bariera", IAction.AS_RADIO_BUTTON) {
      public void run() {
        super.run();
        onBarrierTool();
      }
    };
    barTool.setImageDescriptor(Images.getDescriptor("icons/barrierTool.gif"));
    barTool.setToolTipText("Bariera - przeszkoda");

    Action slowdownTool = new Action("Detektory spowalniajace", IAction.AS_RADIO_BUTTON) {
      public void run() {
        super.run();
        onSlowdownDetectorTool();
      }
    };
    slowdownTool.setImageDescriptor(Images.getDescriptor("icons/slow.gif"));
    slowdownTool.setToolTipText("Narzêdzie detektorów spowalniaj¹cych");

    acts.add(groupsTool);
    acts.add(pathsTool);
    acts.add(lightsTool);
    acts.add(pedestrianTool);
    acts.add(stopTool);
    acts.add(transDetectorTool);
    acts.add(presDetectorTool);
    acts.add(pedeDetectorTool);
    acts.add(condClearDetectorTool);
    acts.add(barDetectorTool);
    acts.add(barTool);
    acts.add(slowdownTool);
    acts.add(marqueeTool);

    m_toolActions = acts.toArray(new IAction[acts.size()]);
  }

  private void initAccidentsModeToolActions() {
    java.util.List<Action> acts = new LinkedList<Action>();
    Action nodesTool = new Action("Wêz³y torów wypadków", IAction.AS_RADIO_BUTTON) {
      public void run() {
        onAccidentNodesTool();
      }
    };
    nodesTool.setToolTipText("Edycja parametrów wêz³ów dla torów wypadków");
    nodesTool.setImageDescriptor(Images.getDescriptor("icons/accidentNodes.gif"));

    Action marqueeTool = new Action("Obszar filmu", IAction.AS_RADIO_BUTTON) {
      public void run() {
        onMarqueeTool();
      }
    };
    marqueeTool.setImageDescriptor(Images.getDescriptor("icons/marqueeTool.gif"));
    marqueeTool.setToolTipText("Narzêdzie obszaru filmowania");

    Action pathsTool = new Action("Tory", IAction.AS_RADIO_BUTTON) {
      public void run() {
        onAccidentsPathsTool();
      }
    };
    pathsTool.setImageDescriptor(Images.getDescriptor("icons/accidentPaths.gif"));
    pathsTool.setToolTipText("Narzêdzie torów uczestników wypadku");

    Action lightsTool = new Action("Sygnalizatory", IAction.AS_RADIO_BUTTON) {
      public void run() {
        onLightsTool();
      }
    };
    lightsTool.setImageDescriptor(Images.getDescriptor("icons/lightsTool.gif"));
    lightsTool.setToolTipText("Narzêdzie sygnalizatorów");

    Action groupsTool = new Action("Grupy", IAction.AS_RADIO_BUTTON) {
      public void run() {
        onGroupsTool();
      }
    };
    groupsTool.setImageDescriptor(Images.getDescriptor("icons/groupsTool.gif"));
    groupsTool.setToolTipText("Narzêdzie grup");

    Action pedestrianTool = new Action("Piesi", IAction.AS_RADIO_BUTTON) {
      public void run() {
        super.run();
        onPedestrianTool();
      }
    };
    pedestrianTool.setImageDescriptor(Images.getDescriptor("icons/pedestrianTool.gif"));
    pedestrianTool.setToolTipText("Narzêdzie pieszych i rowerzystów");

    Action stopTool = new Action("Przystanki", IAction.AS_RADIO_BUTTON) {
      public void run() {
        super.run();
        onBusStopTool();
      }
    };
    stopTool.setImageDescriptor(Images.getDescriptor("icons/busStopTool.gif"));
    stopTool.setToolTipText("Narzêdzie przystanków");

    Action transDetectorTool = new Action("Detektory tranzytu", IAction.AS_RADIO_BUTTON) {
      public void run() {
        super.run();
        onTransitDetectorTool();
      }
    };
    transDetectorTool.setImageDescriptor(Images
        .getDescriptor("icons/transitDetectorTool.gif"));
    transDetectorTool.setToolTipText("Narzêdzie detektorów tranzytu");

    Action presDetectorTool = new Action("Detektory obecnoœci", IAction.AS_RADIO_BUTTON) {
      public void run() {
        super.run();
        onPresenceDetectorTool();
      }
    };
    presDetectorTool.setImageDescriptor(Images
        .getDescriptor("icons/presenceDetectorTool.gif"));
    presDetectorTool.setToolTipText("Narzêdzie detektorów obecnoœci");

    Action pedeDetectorTool = new Action("Detektory pieszych", IAction.AS_RADIO_BUTTON) {
      public void run() {
        super.run();
        onPedestrianDetectorTool();
      }
    };
    pedeDetectorTool.setImageDescriptor(Images
        .getDescriptor("icons/pedestrianDetectorTool.gif"));
    pedeDetectorTool.setToolTipText("Narzêdzie detektorów pieszych");

    Action barDetectorTool = new Action("Detektory barrier", IAction.AS_RADIO_BUTTON) {
      public void run() {
        super.run();
        onBarrierDetectorTool();
      }
    };
    barDetectorTool.setImageDescriptor(Images
        .getDescriptor("icons/barrierDetectorTool.gif"));
    barDetectorTool.setToolTipText("Bariera - detektor");

    Action barTool = new Action("bariera", IAction.AS_RADIO_BUTTON) {
      public void run() {
        super.run();
        onBarrierTool();
      }
    };
    barTool.setImageDescriptor(Images.getDescriptor("icons/barrierTool.gif"));
    barTool.setToolTipText("Bariera - przeszkoda");

    Action slowdownTool = new Action("Detektory spowalniajace", IAction.AS_RADIO_BUTTON) {
      public void run() {
        super.run();
        onSlowdownDetectorTool();
      }
    };
    slowdownTool.setImageDescriptor(Images.getDescriptor("icons/slow.gif"));
    slowdownTool.setToolTipText("Narzêdzie detektorów spowalniaj¹cych");

    acts.add(nodesTool);
    acts.add(pathsTool);
    acts.add(lightsTool);
    acts.add(pedestrianTool);
    acts.add(stopTool);
    acts.add(transDetectorTool);
    acts.add(presDetectorTool);
    acts.add(pedeDetectorTool);
    acts.add(barDetectorTool);
    acts.add(barTool);
    acts.add(slowdownTool);
    acts.add(marqueeTool);

    m_toolActions = acts.toArray(new IAction[acts.size()]);
  }

  private void onAccidentNodesTool() {
    m_editor.setCurrentState(new AccidentNodesTool(m_editor));
  }

  private void onBarrierDetectorTool() {
    m_editor.setCurrentState(new BarrierDetectorTool(m_editor));
  }

  private void onBarrierTool() {
    m_editor.setCurrentState(new BarrierTool(m_editor));
  }

  private void onBusStopTool() {
    m_editor.setCurrentState(new BusStopTool(m_editor));
  }

  private void onExportData() {
    StatisticsExporter e = new StatisticsExporter();
    e.exportDatabanks();
    e.changeSheet(2);
    e.exportPresenceDetectors();
    e.exportPedestrianDetectors();
    e.exportTransitDetectors();
    e.dispose();
  }

  private void onGroupsTool() {
    m_editor.setCurrentState(new GroupsTool(m_editor));
  }

  private void onImportImage() {
    FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
    dlg.setFilterExtensions(new String[] { "*.gif;*.png;*.jpg;*.bmp" });
    String path = dlg.open();
    if (path != null) {
      Traffix.simManager().importImage(path);
      m_editor.setCurrentState(new ScaleTool(m_editor, m_editor.getCurrentState()));
    }
  }

  private void onLightsTool() {
    m_editor.setCurrentState(new LightTool(m_editor));
  }

  private void onMakeMovie() {
    Rectanglef area = Traffix.simManager().getFilmRectangle();
    if (area == null) {
      Traffix.error("Niezdefiniowany obszar filmowania");
      return;
    }

    MovieDialog dlg = new MovieDialog(getShell());
    dlg.open();
  }

  private void onMarqueeTool() {
    m_editor.setCurrentState(new MarqueeTool(m_editor));
  }

  private void onAccidentsPathsTool() {
    m_editor.setCurrentState(new AccidentPathsTool(m_editor));
  }

  private void onPathsTool() {
    m_editor.setCurrentState(new PathsTool(m_editor));
  }

  private void onPedestrianDetectorTool() {
    m_editor.setCurrentState(new PedestrianDetectorTool(m_editor));
  }

  private void onCondClearDetectorTool() {
    m_editor.setCurrentState(new CondClearDetectorTool(m_editor));
  }
  
  private void onPedestrianTool() {
    m_editor.setCurrentState(new PedestrianTool(m_editor));
  }

  private void onPresenceDetectorTool() {
    m_editor.setCurrentState(new PresenceDetectorTool(m_editor));
  }

  private void onRescaleImage() {
    m_editor.setCurrentState(new ScaleTool(m_editor, m_editor.getCurrentState()));
  }

  private void onSimDetectorsTable() {
    MobileToDetectorAssignDialog dlg = new MobileToDetectorAssignDialog(getShell());
    dlg.open();
  }
  
  private void onSimParams() {
    SimParamsDialog dlg = new SimParamsDialog(getShell());
    if (dlg.open() == SimParamsDialog.OK) {}
  }

  private void stopSimulation() {
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        m_playing = false;
        m_simulate.setChecked(false);
        m_simulateFast.setChecked(false);
        m_editor.setSimulationRunning(false);
//        try {
//          m_playThread.join();
//        } catch (InterruptedException e) {}
        m_editor.getCanvas().removeKeyListener(m_stopSimKeyListener);
      }
    });
  }
  
  private void beginSimulation(final float speedFactor) {
    if (m_playing)
      return;
    m_playing = true;
    m_stopSimKeyListener = new KeyAdapter() {
          public void keyPressed(KeyEvent e) {
            if (e.character == ' ' || e.character == '\r')
              stopSimulation();
          }
        };
    m_editor.getCanvas().setFocus();
    m_editor.getCanvas().addKeyListener(m_stopSimKeyListener);

    m_playThread = new Thread() {
      @Override
      public void run() {
        final ISimManager simman = Traffix.simManager();
        double t0 = NativeUtils.clock();
        while (m_playing) {
          final double t1 = NativeUtils.clock();
          final double dt = (t1 - t0) * speedFactor;
          Display.getDefault().syncExec(new Runnable() {
            public void run() {
              simman.tick((float) dt);
              m_editor.redrawCanvas();
              m_editor.getCanvas().setFocus();
            }
          });
          t0 = t1;
          
          if (simman.simParams().duration != 0
              && simman.getCurrentTime() >= simman.simParams().duration) {
            stopSimulation();
          }
        }
      }
    };
    m_playThread.start();
  }

  private void onSimPlay() {
    if (!m_playing) {
      beginSimulation(Traffix.simManager().getSpeedFactor());
    } else {
      stopSimulation();
    }
  }
  
  private void onSimPlayFast() {
    if (!m_playing) {
      beginSimulation(Config.SIM_FAST_SIMULATION_SPEEDFACTOR);
    } else {
      stopSimulation();
    }
  }
  
  private void onSimPlayStep() {
    ISimManager simman = Traffix.simManager();
    for (int i = 0; i < 20; ++i) {
      simman.tick(0.05f);
      simman.fireUpdated();
      while (getShell().getDisplay().readAndDispatch()) {}
    }
  }

  private void onSimPlayUpto() {
    NumberFormat fmt = new DecimalFormat("00");
    int curTime = (int) Traffix.simManager().getCurrentTime();
    Time t = new Time().addSecs(curTime);
    String timestr = fmt.format(t.h) + ":" + fmt.format(t.m) + ":" + fmt.format(t.s);
    String msg = "Podaj czas do którego symulowaæ. Aktualny czas to " + timestr;
    IInputValidator timeValidator = new IInputValidator() {
      public String isValid(String s) {
        if (strToSecs(s) != -1)
          return null;
        return "B³êdny format czasu";
      }
    };

    InputDialog dlg = new InputDialog(getShell(), Traffix.NAME, msg, timestr,
        timeValidator);
    if (dlg.open() == InputDialog.OK) {
      ISimManager simman = Traffix.simManager();
      int t2 = strToSecs(dlg.getValue());
      int diff = t2 - curTime;
      if (diff < 0) {
        simman.rewindSimulation();
        diff = t2;
      }
      int count = (int) (diff / ISimManager.MAX_SIMULATION_STEP);
      for (int i = 0; i < count; ++i) {
        simman.tick(ISimManager.MAX_SIMULATION_STEP);
        m_editor.paintInfoLine();
      }
      simman.fireUpdated();
    }
  }

  private void onSimReset() {
    MessageBox dlg = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
    dlg.setText(Traffix.NAME);
    dlg.setMessage("Zostan¹ skasowane inforamcje o symulacji! Kontynuowaæ?");
    if (dlg.open() == SWT.YES) {
      Traffix.model().resetSimManager();
    }
  }

  private void onSimRewind() {
    if (m_playing)
      return;
    Traffix.simManager().rewindSimulation();
    Traffix.simManager().fireUpdated();
  }

  private void onSimRewindAndGenerate() {
    if (m_playing)
      return;
    Traffix.simManager().rewindAndGenerateSimulation();
    Traffix.simManager().fireUpdated();
  }

  private void onSlowdownDetectorTool() {
    m_editor.setCurrentState(new SlowdownDetectorTool(m_editor));
  }

  private void onTransitDetectorTool() {
    m_editor.setCurrentState(new TransitDetectorTool(m_editor));
  }

  private void selectTool(int tool) {
    m_toolActions[tool].setChecked(true);
    m_toolActions[tool].run();
  }

  private int strToSecs(String timestr) {
    int r = -1;
    StringTokenizer tok = new StringTokenizer(timestr, ":");
    if (tok.countTokens() > 3 || tok.countTokens() < 1)
      return -1;
    int[] ar = new int[tok.countTokens()];
    try {
      for (int i = 0; i < ar.length; ++i)
        ar[i] = Integer.parseInt(tok.nextToken());
      if (ar.length == 1)
        r = ar[0];
      if (ar.length == 2)
        r = ar[0] * 60 + ar[1];
      if (ar.length == 3)
        r = ar[0] * 3600 + ar[1] * 60 + ar[2];
    } catch (NumberFormatException e) {
      return -1;
    }
    return r;
  }
}

class ComboItem extends ControlContribution {
  public Combo m_combo;

  protected ComboItem(String id) {
    super(id);
  }

  protected Control createControl(Composite parent) {
    m_combo = new Combo(parent, SWT.READ_ONLY | SWT.NO_FOCUS);
    return m_combo;
  }
}