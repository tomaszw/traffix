/*
 * Created on 2004-07-01
 */

package traffix.ui.schedule;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.tw.patterns.observer.IUpdateListener;

import traffix.Config;
import traffix.Traffix;
import traffix.core.VehicleGroup;
import traffix.core.Range;
import traffix.core.actionframework.IAction;
import traffix.core.model.JunctionDescription;
import traffix.core.model.Model;
import traffix.core.schedule.*;
import traffix.core.schedule.GroupProgram.McmMarker;
import traffix.ui.*;
import za.co.quirk.layout.LatticeData;
import za.co.quirk.layout.LatticeLayout;

public class ScheduleEditorWidget extends Composite {
  static final int              LIMAGE_H        = 24;
  static final int              LIMAGE_W        = 24;

  private static NumberFormat   s_2digitFmt     = new DecimalFormat("0.00");
  private static final int      S_DEFAULT       = 0;
  private static final int      S_INSERT        = 2;
  private static final int      S_MODIFY        = 4;
  private static final int      S_PROLONG_GREEN = 5;
  private static final int      S_RESIZE_KB     = 3;

  private static final int      S_RESIZE_MOUSE  = 1;
  private static final String[] s_stateNames    = { "", "Poszerzanie", "Wstawianie",
    "Poszerzanie", "Modyfikacja", "Wyd³u¿anie zielonego" };

  private ScheduleEditorConfig  m_config        = new ScheduleEditorConfig();
  private Menu                  m_contextKbInsertMenu;
  private Menu                  m_contextMenu;

  private Canvas                m_greenPaneCanvas;
  private ScrolledComposite     m_greenPaneScrolls;
  private Canvas                m_groupListCanvas;
  private Font                  m_groupListFont;
  private ScrolledComposite     m_groupListScrolls;
  private Canvas                m_header;
  private Font                  m_headerFont;

  private HintInfo              m_hintInfo      = new HintInfo();
  private HoverInfo             m_hoverInfo     = new HoverInfo();
  private InsertInfo            m_insertInfo    = new InsertInfo();

  private Image[]               m_lightImages;
  private LightPainter          m_lightPainter  = new LightPainter();

  private int                   m_markerX       = 0;
  private int                   m_markerY       = 0;

  private Point                 m_offset        = new Point(0, 0);
  private ProlongInfo           m_prolongInfo   = new ProlongInfo();
  private Schedule              m_schedule;
  private Canvas                m_scheduleCanvas;
  private ScrolledComposite     m_scheduleScrolls;

  private ResizeInfo            m_sizingInfo    = new ResizeInfo();

  private int                   m_state         = S_DEFAULT;
  private Label                 m_stateLabel;
  private MarkerWidget                m_topMarker;
  private Composite             m_topMarkersCanvas;
  private UpperRulerWidget            m_topRuler;

  private static class HintInfo {
    Range[]                 greenBounds;
    GroupProgram.McmMarker[][] groupHints;
  }

  private static class HoverInfo {
    boolean atBorder  = false;
    int     group     = -1;
    int     light     = 0;
    int     mouseX    = 0;
    int     mouseY    = 0;
    int     prevGroup = -1;
    int     sec       = 0;
  }

  private static class InsertInfo {
    int beg;
    int group;
    int left  = -1;
    int light;
    int numClicks;
    int right = -1;
  }

  private static class ProlongInfo {
    int          beg, group;
    GroupProgram programCopy;
  }

  private static class ResizeInfo {
    int          beg;
    int          cur;
    int          group;
    GroupProgram programCopy;
  }

  public ScheduleEditorWidget(Composite parent, int style) {
    super(parent, style);

    m_markerX = 0;

    changeColors(0);

    allocResources();

    buildUi();

    final IUpdateListener modelListener = new IUpdateListener() {
      public void onUpdate(int hint, Object data) {
        if (hint == Model.EVT_CHANGE_SCHEDULE
            || hint == Model.EVT_CHANGE_TEMPSCHEDULE) {
          onModelChange();
        }
      }
    };

    Traffix.model().addUpdateListener(modelListener);

    addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        Traffix.model().removeUpdateListener(modelListener);
      }
    });
  }

  public void changeColors() {

    changeColors((m_config.colorScheme + 1) % 3);
  }

  public void changeColors(int id) {
    m_config.colorScheme = id;
    switch (m_config.colorScheme) {
      case 0 :
        ScheduleStyle.greenLightColor = new RGB(0, 255, 0);
        ScheduleStyle.redLightColor = new RGB(215, 0, 00);
        ScheduleStyle.yellowLightColor = new RGB(255, 255, 0);
        break;
      case 1 :
        ScheduleStyle.redLightColor = new RGB(255, 77, 90);
        ScheduleStyle.yellowLightColor = new RGB(251, 202, 33);
        ScheduleStyle.greenLightColor = new RGB(204, 255, 203);
        break;

      case 2 :
        ScheduleStyle.redLightColor = new RGB(245, 245, 245);
        ScheduleStyle.yellowLightColor = new RGB(255, 255, 255);
        ScheduleStyle.greenLightColor = new RGB(255, 255, 255);
        break;

    }
    if (m_schedule != null)
      onModelChange();
  }

  public Image createScreenshot() {
    Point sz;
    int w = 0, h = 0;
    Point szSchedule = m_scheduleCanvas.getSize();
    Point szGL = m_groupListCanvas.getSize();
    Point szHeader = m_header.getSize();
    Point szRuler = m_topRuler.getSize();
    Point szGreen = m_greenPaneCanvas.getSize();

    szRuler.x = getNumSecs() * ScheduleStyle.cellW;
    szHeader.x = szGL.x + szSchedule.x;
    if (m_config.showGreenDurations)
      szHeader.x += szGreen.x;
    GC tmpGc;

    Image imgHeader = new Image(getDisplay(), szHeader.x, szHeader.y);
    tmpGc = new GC(imgHeader);
    paintHeaderPane(tmpGc, false);
    tmpGc.dispose();

    Image imgRuler = new Image(getDisplay(), szRuler.x + 20, szRuler.y);
    tmpGc = new GC(imgRuler);
    m_topRuler.paint(tmpGc);
    tmpGc.dispose();

    Image imgGL = new Image(getDisplay(), szGL.x, szGL.y);
    tmpGc = new GC(imgGL);
    int my = m_markerY;
    m_markerY = -1;
    paintGroupPane(tmpGc);
    m_markerY = my;
    tmpGc.dispose();

    Image imgSch = new Image(getDisplay(), szSchedule.x, szSchedule.y);
    tmpGc = new GC(imgSch);
    paintMainEditorPane(tmpGc);
    tmpGc.dispose();

    Image imgLegend = new Image(getDisplay(), szGL.x + szSchedule.x, 40);
    tmpGc = new GC(imgLegend);
    paintLegend(tmpGc);
    tmpGc.dispose();

    Image imgGreen = new Image(getDisplay(), Math.max(10, szGreen.x), Math.max(10,
        szGreen.y));
    tmpGc = new GC(imgGreen);
    if (m_config.showGreenDurations)
      paintRightBubblePane(tmpGc);
    tmpGc.dispose();

    int hHeader = imgHeader.getBounds().height;
    int hRuler = imgRuler.getBounds().height;
    int hGL = imgGL.getBounds().height;
    int hSch = imgSch.getBounds().height;

    int wGreen = imgGreen.getBounds().width;
    int wHeader = imgHeader.getBounds().width;
    int wGL = imgGL.getBounds().width;
    int wSch = imgSch.getBounds().width;

    h = imgHeader.getBounds().height + imgRuler.getBounds().height
        + imgGL.getBounds().height + 40;
    w = imgGL.getBounds().width + imgSch.getBounds().width;
    if (m_config.showGreenDurations)
      w += wGreen;
    Image img = new Image(getDisplay(), w, h);
    GC gc = new GC(img);
    gc.drawImage(imgHeader, 0, 0);
    gc.drawImage(imgRuler, wGL, hHeader);
    gc.drawImage(imgGL, 0, hHeader + hRuler);
    gc.drawImage(imgSch, wGL, hHeader + hRuler);
    if (m_config.showGreenDurations)
      gc.drawImage(imgGreen, wGL + wSch, hHeader + hRuler);
    gc.drawImage(imgLegend, wGL, hHeader + hRuler + hSch);
    gc.dispose();

    imgHeader.dispose();
    imgRuler.dispose();
    imgGL.dispose();
    imgSch.dispose();
    imgGreen.dispose();
    imgLegend.dispose();

    return img;
  }

  public void insertBegin(int light) {
    m_insertInfo.light = light;
    m_insertInfo.numClicks = 0;
    m_insertInfo.group = -1;
    m_state = S_INSERT;
    updateCursorShape();
  }

  public void setDefaultCellSize(int w) {
    ScheduleStyle.cellW = w;
    onModelChange();
  }

  public void setNumCycles(int num) {
    m_config.numCycles = num;
    onModelChange();
  }

  public void setSchedule(Schedule schedule) {
    m_schedule = schedule;
    updateSizes();
    redraw();
  }

  private void allocResources() {
    Display d = getDisplay();

    m_groupListFont = new Font(d, "Verdana", 8, SWT.BOLD);// ITALIC);
    m_headerFont = new Font(d, "Times New Roman", 10, SWT.NONE);

    m_lightImages = m_lightPainter.buildLightImages(getDisplay(), LIMAGE_W, LIMAGE_H);

    addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        m_headerFont.dispose();
        m_groupListFont.dispose();
        for (int i = 0; i < m_lightImages.length; ++i)
          m_lightImages[i].dispose();
      }
    });
  }

  private void buildContextMenus() {
    m_contextMenu = new Menu(this);
    int[] lightTypesOrder = LightTypes.s_lightTypesMenuOrder;
    m_contextKbInsertMenu = new Menu(this);

    MenuItem item = new MenuItem(m_contextMenu, SWT.CASCADE);
    item.setText("Wstaw");
    Menu insertMenu = new Menu(item);
    item.setMenu(insertMenu);

    item = new MenuItem(m_contextKbInsertMenu, SWT.NONE);
    item.setText("Modyfikacja");
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        modifyBegin();
      }
    });

    new MenuItem(m_contextKbInsertMenu, SWT.SEPARATOR);
    for (int i = 0; i < LightTypes.NUM_LIGHTS; ++i) {
      MenuItem it = new MenuItem(insertMenu, SWT.NONE);
      final int light = lightTypesOrder[i];
      it.setText(LightTypes.names[light]);
      it.setImage(m_lightImages[light]);
      it.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          insertBegin(light);
          restoreCursorPos();
        }
      });

      it = new MenuItem(m_contextKbInsertMenu, SWT.NONE);
      it.setText(LightTypes.names[light]);
      it.setImage(m_lightImages[lightTypesOrder[i]]);
      it.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          insertBegin(light);
          restoreCursorPos();
          insertAdvanceState();
        }
      });
    }

    // item = new MenuItem(m_contextKbInsertMenu, SWT.CASCADE);
    // item.setText("Inne");
    // Menu otherMenu = new Menu(item);
    // item.setMenu(otherMenu);

    item = new MenuItem(m_contextMenu, SWT.CASCADE);
    item.setText("Podmieñ");
    Menu replMenu = new Menu(item);
    item.setMenu(replMenu);

    for (int i = 0; i < LightTypes.NUM_LIGHTS; ++i) {
      MenuItem it = new MenuItem(replMenu, SWT.NONE);
      it.setText("Na " + LightTypes.names[i]);
      it.setImage(m_lightImages[i]);
      final int light = i;
      it.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          Traffix.actionManager().run(
              GroupProgramActions.floodFill(m_schedule.getProgram(m_markerY), m_markerX,
                  light));
          restoreCursorPos();
        }
      });
    }

    item = new MenuItem(m_contextMenu, SWT.NONE);
    item.setText("Reset œwiat³a");
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        Traffix.actionManager().run(
            GroupProgramActions.reset(m_schedule.getProgram(m_markerY)));
        restoreCursorPos();
      }
    });

    item = new MenuItem(m_contextMenu, SWT.SEPARATOR);

    item = new MenuItem(m_contextMenu, SWT.NONE);
    item.setText("Znaczniki MCM");
    // item.setImage(Images.get(ScheduleStyle.imgQmark));
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        toggleMcmTimeHint();
        restoreCursorPos();
      }
    });

    item = new MenuItem(m_contextMenu, SWT.NONE);
    item.setText("Momenty nieskolidowane");
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        toggleUncollidedSecsHint();
        restoreCursorPos();
      }
    });

    item = new MenuItem(m_contextMenu, SWT.NONE);
    item.setText("Momenty skolidowane");
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        toggleCollidedSecsHint();
        restoreCursorPos();
      }
    });

    item = new MenuItem(m_contextMenu, SWT.NONE);
    item.setText("Fazy ruchu");
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        openMovePhasesFrame();
        restoreCursorPos();
      }
    });

    item = new MenuItem(m_contextMenu, SWT.NONE);
    item.setText("Podgl¹d tabeli MCM");
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        openMcmMatrixFrame();
        restoreCursorPos();
      }
    });

    item = new MenuItem(m_contextMenu, SWT.SEPARATOR);

    item = new MenuItem(m_contextMenu, SWT.NONE);
    item.setText("B¹ble zielonego œwiat³a");
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        toggleGreenDurations();
        restoreCursorPos();
      }
    });

  }

  private void buildUi() {
    setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

    double[][] size = {
      { (double) ScheduleStyle.groupListW, LatticeLayout.FILL, LatticeLayout.MINIMUM },
      { 40.0, 22.0, 30.0, LatticeLayout.FILL } };
    LatticeLayout layout = new LatticeLayout(size);
    setLayout(layout);

    addControlListener(new ControlListener() {
      public void controlMoved(ControlEvent e) {
      }

      public void controlResized(ControlEvent e) {
        setOffset(0, 0);
      }
    });

    m_header = new Canvas(this, SWT.NONE);
    m_header.setLayoutData(new LatticeData("0,0,2,0"));
    m_header.addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        paintHeaderPane(e.gc, true);
      }
    });
    m_stateLabel = new Label(m_header, SWT.NONE);
    m_stateLabel.setFont(m_groupListFont);
    m_stateLabel.setBackground(getDisplay().getSystemColor(SWT.COLOR_YELLOW));

    m_groupListScrolls = new ScrolledComposite(this, SWT.NONE);// BORDER);
    m_groupListScrolls.setLayoutData(new LatticeData("0,3"));
    m_groupListScrolls.setLayout(new FillLayout());
    m_groupListCanvas = new Canvas(m_groupListScrolls, SWT.NO_BACKGROUND);
    m_groupListCanvas.setLayoutData(new LatticeData("0,3"));
    m_groupListCanvas.addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        paintGroupPane(e.gc);
      }
    });
    m_groupListScrolls.setContent(m_groupListCanvas);

    m_topMarkersCanvas = new Composite(this, SWT.NONE);
    m_topMarkersCanvas.setLayoutData(new LatticeData("1,1"));
    m_topMarkersCanvas.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
    m_topRuler = new UpperRulerWidget(this, SWT.NONE);
    m_topRuler.setLayoutData(new LatticeData("1,2,2,2"));

    m_scheduleScrolls = new ScrolledComposite(this, SWT.H_SCROLL | SWT.V_SCROLL);
    m_scheduleScrolls.setAlwaysShowScrollBars(true);
    m_scheduleScrolls.setLayoutData(new LatticeData("1,3,2,3"));
    m_scheduleScrolls.setLayout(new FillLayout());
    m_scheduleScrolls.getHorizontalBar().setPageIncrement(300);
    m_scheduleScrolls.getVerticalBar().setPageIncrement(100);

    m_scheduleScrolls.getHorizontalBar().addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        onHScroll();
      }
    });
    m_scheduleScrolls.getVerticalBar().addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        onVScroll();
      }
    });
    m_scheduleCanvas = new Canvas(m_scheduleScrolls, SWT.NO_BACKGROUND);
    m_scheduleCanvas.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
    // m_scheduleCanvas.setLayoutData(new LatticeData("1,3"));
    m_scheduleCanvas.addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        paintMainEditorPane(e.gc);
      }
    });
    m_scheduleScrolls.setContent(m_scheduleCanvas);

    m_greenPaneScrolls = new ScrolledComposite(this, SWT.NONE);// BORDER);
    m_greenPaneScrolls.setLayoutData(new LatticeData("2,3"));
    m_greenPaneScrolls.setLayout(new FillLayout());
    m_greenPaneScrolls.setSize(0, 0);
    m_greenPaneCanvas = new Canvas(m_greenPaneScrolls, SWT.NO_BACKGROUND);
    // m_greenPaneCanvas.setSize(0,0);
    m_greenPaneCanvas.addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        paintRightBubblePane(e.gc);
      }
    });
    m_greenPaneScrolls.setVisible(false);

    m_topMarker = new MarkerWidget(this, SWT.NONE, MarkerWidget.TOP);
    m_topMarker.moveAbove(m_topMarkersCanvas);

    buildContextMenus();

    updateSizes();

    addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        onKeyDown(e);
      }
    });

    m_scheduleCanvas.addMouseMoveListener(new MouseMoveListener() {
      public void mouseMove(MouseEvent e) {
        onMouseMove(e);
      }
    });

    m_scheduleCanvas.addMouseListener(new MouseAdapter() {
      public void mouseDown(MouseEvent e) {
        onMouseDown(e);
      }

      public void mouseUp(MouseEvent e) {
        onMouseUp(e);
      }
    });
  }

  private void cancelAction() {
    Traffix.actionManager().seqEnd();

    if (m_state == S_RESIZE_KB || m_state == S_RESIZE_MOUSE) {
      m_schedule.getProgram(m_sizingInfo.group).assign(m_sizingInfo.programCopy);
    }

    if (m_state == S_PROLONG_GREEN) {
      m_schedule.getProgram(m_prolongInfo.group).assign(m_prolongInfo.programCopy);
    }

    m_state = S_DEFAULT;
    updateHoverInfo();
    updateCursorShape();
    m_topRuler.setArrowPos(-1);
    m_topRuler.setArrowPos2(-1);
    m_topRuler.redraw();
    m_scheduleCanvas.redraw();
  }

  private boolean canSee(int group, int sec) {
    return canSeeTime(sec) && canSeeGroup(group);
  }

  private boolean canSeeGroup(int group) {
    Rectangle b = getShownBounds();
    return group >= b.y && group < b.y + b.height;
  }

  private boolean canSeeTime(int sec) {
    while (sec < 0)
      sec += m_schedule.getProgramLength();
    Rectangle b = getShownBounds();

    return sec >= b.x && sec < b.x + b.width;
  }

  private void forceSeeGroup(int group) {
    if (!canSeeGroup(group)) {
      Rectangle b = getShownBounds();
      int top = b.y;
      int bottom = b.y + b.height - 1;
      int delta = 0;
      if (group < top) {
        delta = group - top;
      } else {
        delta = group - bottom;
      }
      delta *= ScheduleStyle.groupBarH;
      setOffset(m_offset.x, m_offset.y + delta);
    }
  }

  private void forceSeeTime(int time) {
    if (!canSeeTime(time)) {
      Rectangle b = getShownBounds();
      int left = b.x;
      int right = b.x + b.width - 1;
      int delta = 0;
      if (time < left) {
        delta = time - left;
      } else {
        delta = time - right;
      }
      delta *= ScheduleStyle.cellW;
      setOffset(m_offset.x + delta, m_offset.y);

    }
  }

  private int getMarkerXAtPixel(int x) {
    double markerPos = Math.floor(x / (double) ScheduleStyle.cellW + 0.5);
    if (markerPos < getMinMarkerPos())
      markerPos = getMinMarkerPos();
    int maxSec = getMaxMarkerPos();
    if (markerPos > maxSec)
      markerPos = maxSec;
    return (int) markerPos;
  }

  private int getMaxMarkerPos() {
    return getNumSecs();
  }

  private int getMinMarkerPos() {
    return 0;
  }

  private int getNumSecs() {
    return m_config.numCycles * m_schedule.getProgramLength();
  }

  private GroupProgram getProgram(int i) {
    return m_schedule.getProgram(i);
  }

  private Rectangle getShownBounds() {
    Rectangle bounds = m_scheduleScrolls.getBounds();
    bounds.width -= m_scheduleScrolls.getVerticalBar().getSize().x;
    bounds.height -= m_scheduleScrolls.getHorizontalBar().getSize().y;

    int topGroup = (m_offset.y + (ScheduleStyle.groupBarH - 1)) / ScheduleStyle.groupBarH;
    int bottomGroup = (bounds.height - (ScheduleStyle.groupBarH - 1))
        / ScheduleStyle.groupBarH + topGroup;
    int leftSec = (m_offset.x + (ScheduleStyle.cellW - 1)) / ScheduleStyle.cellW;
    int rightSec = (bounds.width - (ScheduleStyle.cellW - 1)) / ScheduleStyle.cellW
        + leftSec;
    return new Rectangle(leftSec, topGroup, rightSec - leftSec + 1, bottomGroup
        - topGroup + 1);
  }

  private void insertAdvanceState() {
    if (m_state != S_INSERT)
      return;

    if (m_insertInfo.numClicks == 0) {
      ++m_insertInfo.numClicks;
      m_insertInfo.left = m_markerX;
      m_insertInfo.group = m_markerY;
      m_topRuler.setArrowPos(m_markerX);
    } else if (m_insertInfo.numClicks == 1) {
      m_topRuler.setArrowPos(-1);
      m_topRuler.setArrowPos2(-1);
      m_state = S_DEFAULT;
      int beg = m_insertInfo.left;
      int end = m_markerX - 1;
      if (end < beg) {
        int tmp = beg;
        beg = end + 1;
        end = tmp - 1;
      }
      GroupProgram prog = m_schedule.getProgram(m_insertInfo.group);
      if (beg <= end) {
        Traffix.actionManager().run(
            GroupProgramActions.setInterval(prog, beg, end, m_insertInfo.light));
      }
    }
  }

  private void insertBeginAtMarker(int light) {
    m_insertInfo.light = light;
    m_insertInfo.numClicks = 0;
    m_insertInfo.group = -1;
    m_state = S_INSERT;
    ++m_insertInfo.numClicks;
    m_insertInfo.left = m_markerX;
    m_insertInfo.group = m_markerY;
    m_topRuler.setArrowPos(m_markerX);
    updateCursorShape();
  }

  private void modifyBackspace() {
    IAction action = ScheduleActions.shrink(m_schedule, m_markerX - 1);
    snapCursorToMarker();
    Traffix.actionManager().run(action);
    setMarkerX(m_markerX - 1);
    snapCursorToMarker();
  }

  private void modifyBegin() {
    m_state = S_MODIFY;
    updateCursorShape();
    Traffix.actionManager().seqBegin();
  }

  private void modifyDelete() {
    IAction action = ScheduleActions.shrink(m_schedule, m_markerX);
    snapCursorToMarker();
    Traffix.actionManager().run(action);
    // setMarkerX(m_markerX - 1);
    snapCursorToMarker();
  }

  private void modifyExtend() {
    IAction action = ScheduleActions.extend(m_schedule, m_markerX - 1);
    snapCursorToMarker();
    Traffix.actionManager().run(action);
    setMarkerX(m_markerX + 1);
    snapCursorToMarker();
  }

  private void modifyUpdate() {
    updateSizes();
    m_scheduleCanvas.redraw();
    Traffix.model().setModified(true);
    Traffix.model().fireUpdated(Model.EVT_CHANGE_SCHEDULE, null);
  }

  private void moveMarker(KeyEvent e) {
    int keycode = e.keyCode;
    if (!(keycode == SWT.ARROW_LEFT || keycode == SWT.ARROW_RIGHT
        || keycode == SWT.ARROW_UP || keycode == SWT.ARROW_DOWN))
      return;

    int y = m_markerY;
    if (keycode == SWT.ARROW_DOWN) {
      ++y;
    } else if (keycode == SWT.ARROW_UP) {
      --y;
    }
    if (y < 0)
      y = 0;
    if (y > m_schedule.getNumGroups() - 1)
      y = m_schedule.getNumGroups() - 1;
    if (m_markerY != y) {
      setMarkerY(y);
      forceSeeGroup(y);
      snapCursorToMarker();
    }

    if (keycode == SWT.ARROW_RIGHT) {
      int pos = m_markerX + 1;
      if ((e.stateMask & SWT.CTRL) != 0)
        pos = m_markerX + 10;

      if (pos > getMaxMarkerPos()) {
        pos = getMaxMarkerPos();
      }
      forceSeeTime(pos);
      setMarkerX(pos);
      snapCursorToMarker();
    } else if (keycode == SWT.ARROW_LEFT) {
      int pos = m_markerX - 1;
      if ((e.stateMask & SWT.CTRL) != 0)
        pos = m_markerX - 10;

      if (pos < getMinMarkerPos()) {
        pos = getMinMarkerPos();
      }
      forceSeeTime(pos);
      setMarkerX(pos);
      snapCursorToMarker();
    }
  }

  private void onHScroll() {
    m_offset = m_scheduleScrolls.getOrigin();
    m_topRuler.setOffset(m_offset.x);
    // m_topRuler.redraw();
  }

  private void onKeyDown(KeyEvent e) {
    if (m_schedule == null)
      return;
    if (e.keyCode == SWT.ESC) {
      cancelAction();
      return;
    }

    switch (m_state) {
      case S_DEFAULT :
        moveMarker(e);
        if (e.keyCode == Keymap.MCM_HINT_KEY) {
          toggleMcmTimeHint();
        } else if (e.keyCode == Keymap.UNCOLLIDED_HINT_KEY) {
          toggleUncollidedSecsHint();
        } else if (e.keyCode == Keymap.COLLIDED_HINT_KEY) {
          toggleCollidedSecsHint();
        } else if (e.keyCode == Keymap.MOVE_PHASES_KEY) {
          openMovePhasesFrame();
        } else if (e.keyCode == Keymap.GREEN_BUBBLES_KEY) {
          toggleGreenDurations();
        } else if (e.keyCode == Keymap.MCM_MATRIX_KEY) {
          openMcmMatrixFrame();
        } else if (e.keyCode == Keymap.SOC_EDITOR_KEY) {
          openCapacityEditor();
        } else if (e.keyCode == SWT.INSERT) {
          popupContextMenu(m_contextKbInsertMenu);
        } else if (e.keyCode == SWT.HOME) {
          scrollProgramLeft();
        } else if (e.keyCode == SWT.END) {
          scrollProgramRight();
        } else if (e.keyCode == SWT.PAGE_UP) {
          scrollAllProgramsLeft();
        } else if (e.keyCode == SWT.PAGE_DOWN) {
          scrollAllProgramsRight();
        } else if (e.character == 'l') {
          scrollJunctionProgramsLeft();
        } else if (e.character == 'p') {
          scrollJunctionProgramsRight();
        } else if (e.character == '\r') {
          if (m_hoverInfo.atBorder)
            resizeBeginKb();
          else
            popupContextMenu(m_contextKbInsertMenu);
        }
        break;
      case S_INSERT :
        moveMarker(e);
        if (e.character == '\r')
          insertAdvanceState();
        break;
      case S_MODIFY :
        if (e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.BS)
          modifyBackspace();
        else if (e.keyCode == SWT.DEL)
          modifyDelete();
        else if (e.keyCode == SWT.ARROW_RIGHT)
          modifyExtend();
        else if (e.character == '\r')
          cancelAction();
        break;
      case S_RESIZE_KB :
        moveMarker(e);
        if (e.character == '\r')
          resizeEnd();
        break;

    }
    m_groupListCanvas.redraw();
  }

  private void onMarkerMove(int dx, int dy) {
    if (m_state == S_RESIZE_KB || m_state == S_RESIZE_MOUSE) {
      if (dx != 0) {
        resizeContinue();
      }
    } else if (m_state == S_PROLONG_GREEN) {
      if (dx != 0) {
        prolongContinue();
      }
    } else if (m_state == S_INSERT) {
      m_topRuler.setArrowPos2(m_markerX);
    }
  }

  private void onModelChange() {
    if (m_schedule != null) {
      updateSizes();
      updateHoverInfo();
      updateCursorShape();
      updateTopMarkerWidget();
      m_schedule.updateCapacityData();
    }
    // if (m_hintGroup != -1)
    updateHintInfo(m_config.mcmHintGp);
    if (m_scheduleCanvas != null)
      m_scheduleCanvas.redraw();
    if (m_groupListCanvas != null)
      m_groupListCanvas.redraw();
    if (m_header != null)
      m_header.redraw();

    if (m_config.showGreenDurations)
      m_greenPaneCanvas.redraw();
  }

  private void onMouseDown(MouseEvent e) {
    int prevX = m_hoverInfo.mouseX;
    int prevY = m_hoverInfo.mouseY;
    updateHoverInfo();
    setMarkerY(m_hoverInfo.group);
    updateMarkerXFromMouseX();
    updateCursorShape();

    if (m_state == S_DEFAULT) {
      if (m_hoverInfo.atBorder && e.button == 1) {
        resizeBeginMouse();
      } else if (e.button == 1 && !m_hoverInfo.atBorder
          && LightTypes.isGreen(m_hoverInfo.light)) {
        prolongBegin();
      }
    }
  }

  private void onMouseMove(MouseEvent e) {
    int prevMarkerY = m_markerY;
    int prevX = m_hoverInfo.mouseX;
    int prevY = m_hoverInfo.mouseY;
    updateHoverInfo();
    setMarkerY(m_hoverInfo.group);
    updateMarkerXFromMouseX();
    updateCursorShape();

    if (m_markerY != prevMarkerY) {
      GC gc = new GC(m_groupListCanvas);
      paintGroupPane(gc);
      gc.dispose();
    }
    if (m_state == S_RESIZE_MOUSE || m_state == S_PROLONG_GREEN) {
      int prev = m_sizingInfo.cur;
      m_sizingInfo.cur = m_markerX;
      if (prev != m_markerX) {
        if (!canSeeTime(m_markerX)) {
          forceSeeTime(m_markerX);
          snapCursorXToMarkerX();
        }
      }
    }
  }

  private void onMouseUp(MouseEvent e) {
    int prevX = m_hoverInfo.mouseX;
    int prevY = m_hoverInfo.mouseY;
    updateHoverInfo();
    setMarkerY(m_hoverInfo.group);
    updateMarkerXFromMouseX();
    updateCursorShape();

    if (m_state == S_RESIZE_MOUSE) {
      if (e.button == 1)
        resizeEnd();
    } else if (m_state == S_PROLONG_GREEN) {
      if (e.button == 1)
        prolongEnd();
    } else if (m_state == S_INSERT) {
      if (e.button == 1) {
        insertAdvanceState();
      }

      if (e.button == 3) {
        m_state = S_DEFAULT;
        updateCursorShape();
      }
    } else if (m_state == S_DEFAULT) {
      if (e.button == 3) {
        popupContextMenu(m_contextMenu);
      }
    }

  }

  private void onVScroll() {
    m_offset = m_scheduleScrolls.getOrigin();
    Point origin = new Point(0, m_scheduleScrolls.getOrigin().y);
    m_groupListScrolls.setOrigin(origin);
    m_groupListCanvas.setLocation(-origin.x, -origin.y);

    m_greenPaneScrolls.setOrigin(0, m_offset.y);
    m_greenPaneCanvas.setLocation(-origin.x, -origin.y);
  }

  private void openCapacityEditor() {
    SOCEditorFrame f = new SOCEditorFrame(getShell(), m_schedule);
    // f.setBlockOnOpen(true);
    f.open();
  }

  private void openMcmMatrixFrame() {
    McmMatrixFrame frame = new McmMatrixFrame(getShell(), Traffix.model().getMcmMatrix());
    frame.setBlockOnOpen(true);
    frame.open();
  }

  private void openMovePhasesFrame() {
    MovePhasesFrame frame = new MovePhasesFrame(getShell(), m_schedule, m_markerX);
    frame.setBlockOnOpen(true);
    frame.open();
  }

  private void paintGrid(GC gc) {
    int numSecs = getNumSecs();
    Point sz = m_scheduleCanvas.getSize();

    gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
    for (int i = 1; i < m_config.numCycles; ++i) {
      int x = ScheduleStyle.cellW * m_schedule.getProgramLength();
      gc.drawLine(x, 0, x, sz.y);
      gc.drawLine(x - 1, 0, x - 1, sz.y);
      gc.drawLine(x + 1, 0, x + 1, sz.y);
    }
  }

  private void paintGroupPane(GC gc) {

    int numGroups = m_schedule.getNumGroups();
    int barH = ScheduleStyle.groupBarH;
    Point sz = m_groupListCanvas.getSize();
    Point scheduleSz = m_scheduleCanvas.getSize();

    Image buffer = new Image(getDisplay(), sz.x, sz.y);
    GC memGc = new GC(buffer);

    int y = 0, y2 = 0;
    memGc.setFont(m_groupListFont);
    for (int i = 0; i < numGroups; ++i) {
      VehicleGroup group = m_schedule.getGroup(i);
      y = barH * i;
      y2 = y + barH - 1;

      memGc.setForeground(Colors.get(ScheduleStyle.groupGradientColor));
      memGc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
      memGc.fillGradientRectangle(0, y, 50, barH, false);

      memGc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
      memGc.setBackground(Colors.get(ScheduleStyle.groupGradientColor));
      memGc.fillGradientRectangle(50, y, sz.x - 50, barH, false);

      // black is default group name color
      if (group.getJunctionIndex() % 2 == 0)
        memGc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
      else
        memGc.setForeground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

      // colorize group name diffrently depending on QCp coefficient
      if (m_schedule.getCapacityEntry(i).dCp < 0) {
        memGc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLUE));
      }
      // if hint enabled, colorize colliding groups diffrently
      if (m_config.mcmHintGp != -1) {
        if (Traffix.model().areGroupsColliding(m_config.mcmHintGp, i))
          memGc.setForeground(Colors.get(ScheduleStyle.collidingGroupColor));
      }

      String txt = group.getElectricName();
      // txt = txt.toUpperCase();
      Point extent = memGc.textExtent(txt);
      memGc.drawText(txt, 4, y + barH / 2 - extent.y / 2, true);

      txt = group.getName();// .toUpperCase();
      extent = memGc.textExtent(txt);
      memGc.drawText(txt, 45, y + barH / 2 - extent.y / 2, true);

      // separating line
      memGc.drawLine(0, y, sz.x, y);
      if (i != 0 && group.getJunctionIndex() != m_schedule.getGroup(i - 1).getJunctionIndex()) {
        memGc.drawLine(0, y - 1, sz.x + scheduleSz.x, y - 1);
        memGc.drawLine(0, y, sz.x + scheduleSz.x, y);
        memGc.drawLine(0, y + 1, sz.x + scheduleSz.x, y + 1);
      }

      Image imgQmark = Images.get(ScheduleStyle.imgQmark);
      if (i == m_config.mcmHintGp) {
        Rectangle b = imgQmark.getBounds();
        memGc.drawImage(imgQmark, sz.x - 2 - b.width, y + ScheduleStyle.groupBarH / 2
            - b.height / 2);
      }

      Image imgArrowRight = Images.get(ScheduleStyle.imgArrowRight);
      if (m_state != S_RESIZE_MOUSE && i == m_markerY) {
        Rectangle b = imgArrowRight.getBounds();
        memGc.drawImage(imgArrowRight, sz.x - 2 - b.width, y + ScheduleStyle.groupBarH
            / 2 - b.height / 2);
      } else if (m_state == S_RESIZE_MOUSE && i == m_sizingInfo.group) {
        Rectangle b = imgArrowRight.getBounds();
        memGc.drawImage(imgArrowRight, sz.x - 2 - b.width, y + ScheduleStyle.groupBarH
            / 2 - b.height / 2);
      }
    }
    // separating line
    memGc.drawLine(0, y2, sz.x, y2);

    memGc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));

    memGc.setForeground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
    gc.drawImage(buffer, 0, 0);
    memGc.dispose();
    buffer.dispose();
  }

  private void paintHeaderPane(GC gc, boolean collisionWarning) {
    Rectangle bounds = m_header.getBounds();
    gc.setBackground(m_header.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    gc.fillRectangle(bounds);
    gc.setFont(m_headerFont);
    String line;

    JunctionDescription d = Traffix.model().getDescription();

    line = m_schedule.getName();

    line = line + " " + d.projectNum + " " + d.crossingNum + " " + d.city + " "
        + d.streets + " " + d.date + " " + d.note;

    Point ex = gc.textExtent(line);

    // gc.drawText(line, bounds.width / 2 - ex.x / 2, bounds.height - ex.y - 5);
    gc.drawText(line, m_groupListCanvas.getBounds().width, bounds.height - ex.y - 5);

    //
    if (collisionWarning) {
      int numColls = m_schedule.getTotalNumOfCollisions(Traffix.model().getMcmMatrix());
      if (numColls != 0) {
        Image img = Images.get("icons/warning.gif");
        gc.drawImage(img, 0, 0);
        gc.setBackground(Colors.get(new RGB(175, 0, 0)));
        gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        Rectangle imgBounds = img.getBounds();
        String msg = "UWAGA! S¥ KOLIZJE!";
        // Point msgEx = gc.textExtent(msg);
        gc.drawText(msg, imgBounds.width + 4, 0);
      }
    }

  }

  private void paintLegend(GC gc) {
    int px = 0;
    String[] legend = new String[LightTypes.NUM_LIGHTS];
    legend[LightTypes.GREEN] = "G";
    legend[LightTypes.PULSING_GREEN] = "G mig.";
    legend[LightTypes.RED] = "R";
    legend[LightTypes.PULSING_RED] = "R mig.";
    legend[LightTypes.YELLOW] = "Y";
    legend[LightTypes.PULSING_YELLOW] = "Y mig.";
    legend[LightTypes.RED_YELLOW] = "R/Y";
    legend[LightTypes.NO_SIGNAL] = "brak";
    for (int i = 0; i < LightTypes.NUM_LIGHTS; ++i) {
      String name = legend[i];
      Point extent = gc.textExtent(name);
      m_lightPainter.paintBar(gc, px, 4, 24, 20, i, true, true);
      px += 26;
      gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
      gc.drawText(name, px, 14 - extent.y / 2);
      px += gc.textExtent(name).x;
      px += 8;
    }
  }

  private void paintMainEditorPane(GC gc) {
    Rectangle bounds = m_scheduleCanvas.getBounds();
    Image buffer = new Image(m_scheduleCanvas.getDisplay(), bounds);
    GC memGc = new GC(buffer);
    // grid
    int numSecs = getNumSecs();
    Point sz = m_scheduleCanvas.getSize();

    for (int i = 0; i < numSecs; i += 1) {
      int x = ScheduleStyle.cellW * i;
      memGc.drawLine(x, 0, x, sz.y);
      if (i % 5 == 0) {
        memGc.drawLine(x - 1, 0, x - 1, sz.y);
        memGc.drawLine(x + 1, 0, x + 1, sz.y);
      }
    }

    // programs
    int numGroups = m_schedule.getNumGroups();
    //memGc.setAntialias(1);
    for (int i = 0; i < numGroups; ++i) {
      paintProgram(memGc, i);
    }
    //memGc.setAntialias(0);
    // bubbles
    if (m_config.showGreenDurations)
      paintProgramGreenDurationBubbles(memGc);
    // grid 2
    paintGrid(memGc);
    // lines and marker between junction groups
    for (int i = 0; i < numGroups - 1; ++i) {
      boolean paintUpMarker = false, paintDownMarker = false;

      if (m_schedule.getGroup(i).getJunctionIndex() != m_schedule.getGroup(i + 1).getJunctionIndex()) {
        int y = ScheduleStyle.groupBarH * (i + 1);
        memGc.drawLine(0, y - 1, sz.x, y - 1);
        memGc.drawLine(0, y, sz.x, y);
        memGc.drawLine(0, y + 1, sz.x, y + 1);
      }

      int upy = 0, upx = 0;
      int dwy = 0, dwx = 0;

      if (m_schedule.getGroup(i).getJunctionIndex() != m_schedule.getGroup(i + 1).getJunctionIndex()) {
        upy = dwy = ScheduleStyle.groupBarH * (i + 1);
        upx = m_schedule.getJunctionZeroMarker(m_schedule.getGroup(i).getJunctionIndex())
            * ScheduleStyle.cellW;
        dwx = m_schedule.getJunctionZeroMarker(m_schedule.getGroup(i + 1).getJunctionIndex())
            * ScheduleStyle.cellW;
        paintUpMarker = paintDownMarker = true;
      }

      if (i == 0 && m_schedule.getGroup(i).getJunctionIndex() != 0) {
        dwy = 0;
        dwx = m_schedule.getJunctionZeroMarker(m_schedule.getGroup(i).getJunctionIndex())
            * ScheduleStyle.cellW;
        paintDownMarker = true;
      }

      if (i + 1 == numGroups - 1 && m_schedule.getGroup(i + 1).getJunctionIndex() != 0) {
        upy = ScheduleStyle.groupBarH * (i + 2);
        upx = m_schedule.getJunctionZeroMarker(m_schedule.getGroup(i + 1).getJunctionIndex())
            * ScheduleStyle.cellW;
        paintUpMarker = true;
      }

      if (!Config.SCHEDULE_PAINT_JUNCTION_ZEROMARKERS) {
        paintUpMarker = paintDownMarker = false;
      }

      if (paintUpMarker) {
        int h = 8;
        int[] marker = new int[] { upx, upy - 8, upx + 3, upy - 4, upx + 3, upy, upx - 3,
          upy, upx - 3, upy - 4, upx, upy - 8 };
        memGc.setForeground(Colors.system(SWT.COLOR_BLACK));
        memGc.setBackground(Colors.system(SWT.COLOR_WHITE));
        memGc.fillPolygon(marker);
        memGc.drawPolygon(marker);
      }
      if (paintDownMarker) {
        int[] marker = new int[] { dwx, dwy + 8, dwx + 3, dwy + 4, dwx + 3, dwy, dwx - 3,
          dwy, dwx - 3, dwy + 4, dwx, dwy + 8 };
        memGc.setForeground(Colors.system(SWT.COLOR_BLACK));
        memGc.setBackground(Colors.system(SWT.COLOR_WHITE));
        memGc.fillPolygon(marker);
        memGc.drawPolygon(marker);
      }
    }

    gc.drawImage(buffer, 0, 0);
    buffer.dispose();
    memGc.dispose();
  }

  private void paintProgram(GC gc, int id) {
    GroupProgram p = m_schedule.getProgram(id);
    p.paintLightBar(gc, m_lightPainter, 0, id * ScheduleStyle.groupBarH, getNumSecs());
    if (m_config.mcmHintGp != -1) {
      McmMarker[] hints = m_hintInfo.groupHints[id];
      p.paintMcmMarkers(gc, 0, id * ScheduleStyle.groupBarH, hints, m_config.numCycles);
    }
    if (m_config.mcmUncollidedHintGp == id || m_config.showCollidedSecs) {
      int[] collisions = m_schedule
          .getNumOfCollisions(id, Traffix.model().getMcmMatrix());
      if (m_config.mcmUncollidedHintGp == id)
        p.paintUncollidedSecs(gc, 0, id * ScheduleStyle.groupBarH, m_config.numCycles,
            collisions);
      if (m_config.showCollidedSecs)
        p.paintCollidedSecs(gc, 0, id * ScheduleStyle.groupBarH, m_config.numCycles,
            collisions);
    }
    // paint zero marker
    if (Config.SCHEDULE_PAINT_GROUP_ZEROMARKERS) {
      int m = p.getZeroMarkerPos();
      gc.setForeground(Colors.system(SWT.COLOR_BLUE));
      int x = m * ScheduleStyle.cellW;
      int y0 = id * ScheduleStyle.groupBarH;
      int y1 = y0 + ScheduleStyle.groupBarH - 1;
      gc.drawLine(x, y0, x, y1);
      gc.drawLine(ScheduleStyle.cellW * (getNumSecs()) - 1, y0, ScheduleStyle.cellW
          * (getNumSecs()) - 1, y1);
    }
    gc.setForeground(Colors.system(SWT.COLOR_BLACK));
    // gc.drawLine(x+1, y0, x+1, y1);
  }

  private void paintProgramGreenDurationBubbles(GC gc) {
    for (int i = 0; i < m_schedule.getNumGroups(); ++i) {
      getProgram(i).paintGreenLightBubbles(gc, 0, i * ScheduleStyle.groupBarH,
          m_config.numCycles);
    }
  }

  private void paintRightBubblePane(GC gc) {
    Point sz = m_greenPaneCanvas.getSize();
    Image buffer = new Image(m_greenPaneCanvas.getDisplay(), Math.max(10, sz.x), Math
        .max(10, sz.y));
    GC memGc = new GC(buffer);

    for (int i = 0; i < m_schedule.getNumGroups(); ++i) {
      int amount = m_schedule.getProgram(i).getTotalGreenDuration();
      float QCp = m_schedule.getCapacityEntry(i).QCp;

      if (amount == 0)
        continue;
      String txt = Integer.toString(amount);
      // txt = txt + " [" + s_2digitFmt.format(QCp) + "]";

      Point extent = memGc.textExtent(txt);
      int x = 6;
      int y = i * ScheduleStyle.groupBarH + ScheduleStyle.groupBarH / 2 - extent.y / 2;

      memGc.drawRoundRectangle(4, y - 2, 60, extent.y + 4, 12, 12);
      memGc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
      memGc.drawText(txt, 32 - extent.x / 2, y);
    }

    gc.drawImage(buffer, 0, 0);
    memGc.dispose();
    buffer.dispose();
  }

  private void popupContextMenu(Menu m) {
    Point p = m.getDisplay().getCursorLocation();
    m.setLocation(p.x, p.y);
    m.setVisible(true);
  }

  private void prolongBegin() {
    updateHoverInfo();
    m_state = S_PROLONG_GREEN;
    m_prolongInfo.group = m_markerY;
    m_prolongInfo.beg = m_markerX;
    m_prolongInfo.programCopy = (GroupProgram) m_schedule.getProgram(m_markerY).clone();
    m_topRuler.setArrowPos(m_markerX);
    m_topRuler.redraw();
    updateCursorShape();
  }

  private void prolongContinue() {
    if (m_state != S_PROLONG_GREEN)
      return;
    GroupProgram prog = m_schedule.getProgram(m_prolongInfo.group);
    // int delta = prog.normalizeTime(m_markerX) - prog.normalizeTime(m_prolongInfo.beg);
    int delta = m_markerX - m_prolongInfo.beg;
    prog.assign(m_prolongInfo.programCopy);
    prog.prolongGreen(m_prolongInfo.beg, delta);
    updateHintInfo(m_config.mcmHintGp);
    m_schedule.updateCapacityData();
    m_groupListCanvas.redraw();
    m_scheduleCanvas.redraw();
  }

  private void prolongEnd() {
    if (m_state != S_PROLONG_GREEN)
      return;
    GroupProgram prog = m_schedule.getProgram(m_prolongInfo.group);
    GroupProgram changed = (GroupProgram) prog.clone();
    prog.assign(m_prolongInfo.programCopy);

    IAction action = Traffix.actionManager().renameAction(
        GroupProgramActions.copy(prog, changed), "Wyd³u¿enie zielonego");

    m_state = S_DEFAULT;
    m_topRuler.setArrowPos(-1);
    Traffix.actionManager().run(action);
  }

  private void resizeBeginKb() {
    m_state = S_RESIZE_KB;
    m_sizingInfo.group = m_markerY;
    m_sizingInfo.beg = m_markerX;
    m_sizingInfo.cur = m_markerX;
    m_sizingInfo.programCopy = (GroupProgram) m_schedule.getProgram(m_markerY).clone();
    updateCursorShape();

    // Traffix.getActionManager().seqBegin();
  }

  private void resizeBeginMouse() {
    m_state = S_RESIZE_MOUSE;
    m_sizingInfo.group = m_markerY;
    m_sizingInfo.beg = m_markerX;
    m_sizingInfo.cur = m_markerX;
    m_sizingInfo.programCopy = (GroupProgram) m_schedule.getProgram(m_markerY).clone();
    updateCursorShape();

    // Traffix.getActionManager().seqBegin();
  }

  private void resizeContinue() {
    if (m_state != S_RESIZE_MOUSE && m_state != S_RESIZE_KB)
      return;

    GroupProgram prog = m_schedule.getProgram(m_sizingInfo.group);
    int delta = m_markerX - m_sizingInfo.beg;
    prog.assign(m_sizingInfo.programCopy);
    prog.resizeLight(m_sizingInfo.beg, delta);
    updateHintInfo(m_config.mcmHintGp);
    m_schedule.updateCapacityData();
    m_groupListCanvas.redraw();
    m_scheduleCanvas.redraw();
    // //m_state = S_DEFAULT;
    // int beg = m_sizingInfo.beg;
    // m_sizingInfo.beg = m_markerX;
    // int end = m_markerX;
    // GroupProgram prog = m_schedule.getProgram(m_sizingInfo.group);
    //
    // IAction action = null;
    // if (end < beg)
    // action = ActionFactory.createInsertLight(prog, end, beg - 1, prog.get(beg));
    // else if (end > beg)
    // action = ActionFactory.createInsertLight(prog, beg, end - 1, prog.get(beg - 1));
    // if (action != null) {
    // Traffix.getActionManager().run(action);
    // }
    //
    // // cancel if needed
    // if (prog.get(m_markerX) == prog.get(m_markerX - 1))
    // cancelAction();
  }

  private void resizeEnd() {
    if (m_state != S_RESIZE_KB && m_state != S_RESIZE_MOUSE)
      return;
    GroupProgram prog = m_schedule.getProgram(m_sizingInfo.group);
    GroupProgram changed = (GroupProgram) prog.clone();
    prog.assign(m_sizingInfo.programCopy);

    IAction action = Traffix.actionManager().renameAction(
        GroupProgramActions.copy(prog, changed), "Poszerzenie œwiat³a");

    m_state = S_DEFAULT;
    Traffix.actionManager().run(action);
  }

  private void restoreCursorPos() {
    Point p = m_scheduleCanvas.toDisplay(m_hoverInfo.mouseX, m_hoverInfo.mouseY);
    m_scheduleCanvas.getDisplay().setCursorLocation(p.x, p.y);
  }

  private void scrollAllProgramsLeft() {
    Traffix.actionManager().run(ScheduleActions.scroll(m_schedule, true));
  }

  private void scrollAllProgramsRight() {
    Traffix.actionManager().run(ScheduleActions.scroll(m_schedule, false));
  }

  private void scrollJunctionProgramsLeft() {
    Traffix.actionManager().run(
        ScheduleActions.scrollJunctionGroups(m_schedule, true, m_schedule
            .getGroup(m_markerY).getJunctionIndex()));
  }

  private void scrollJunctionProgramsRight() {
    Traffix.actionManager().run(
        ScheduleActions.scrollJunctionGroups(m_schedule, false, m_schedule
            .getGroup(m_markerY).getJunctionIndex()));
  }

  private void scrollProgramLeft() {
    Traffix.actionManager().run(
        GroupProgramActions.scroll(m_schedule.getProgram(m_markerY), true));
  }

  private void scrollProgramRight() {
    Traffix.actionManager().run(
        GroupProgramActions.scroll(m_schedule.getProgram(m_markerY), false));
  }

  private void setMarkerX(int pos) {
    pos = Math.max(getMinMarkerPos(), pos);
    pos = Math.min(getMaxMarkerPos(), pos);
    if (pos == m_markerX)
      return;
    int dx = pos - m_markerX;
    m_markerX = pos;
    updateTopMarkerWidget();

    onMarkerMove(dx, 0);
  }

  private void setMarkerY(int pos) {
    pos = Math.max(0, pos);
    pos = Math.min(m_schedule.getNumGroups() - 1, pos);
    if (pos == m_markerY)
      return;
    int dy = pos - m_markerY;
    m_markerY = pos;
    // redraw marker
    m_groupListCanvas.redraw();

    onMarkerMove(0, dy);
  }

  private void setOffset(int x, int y) {
    m_offset.x = x;
    m_offset.y = y;
    m_scheduleScrolls.setOrigin(x, y);
    Point p = m_scheduleScrolls.getOrigin();
    m_groupListCanvas.setLocation(0, -p.y);
    m_greenPaneCanvas.setLocation(0, -p.y);
    m_offset.x = p.x;
    m_offset.y = p.y;
    m_topRuler.setOffset(m_offset.x);
  }

  private void snapCursorToMarker() {
    int x = m_hoverInfo.mouseX, y = m_hoverInfo.mouseY;
    if (m_markerX != -1)
      x = m_markerX * ScheduleStyle.cellW;// + ScheduleStyle.cellW / 2;
    if (m_markerY != -1)
      y = m_markerY * ScheduleStyle.groupBarH + ScheduleStyle.groupBarH / 2;
    Point p = m_scheduleCanvas.toDisplay(x, y);
    m_scheduleCanvas.getDisplay().setCursorLocation(p.x, p.y);

    updateHoverInfo();
    updateCursorShape();
  }

  private void snapCursorXToMarkerX() {
    int x = m_hoverInfo.mouseX, y = m_hoverInfo.mouseY;
    if (m_markerX != -1)
      x = m_markerX * ScheduleStyle.cellW;// ScheduleStyle.cellW / 2;
    Point p = m_scheduleCanvas.toDisplay(x, y);
    m_scheduleCanvas.getDisplay().setCursorLocation(p.x, p.y);

    updateHoverInfo();
    updateCursorShape();
  }

  private void snapCursorYToMarkerY() {
    int x = m_hoverInfo.mouseX, y = m_hoverInfo.mouseY;
    if (m_markerY != -1)
      y = m_markerY * ScheduleStyle.groupBarH + ScheduleStyle.groupBarH / 2;
    Point p = m_scheduleCanvas.toDisplay(x, y);
    m_scheduleCanvas.getDisplay().setCursorLocation(p.x, p.y);

    updateHoverInfo();
    updateCursorShape();
  }

  private void toggleCollidedSecsHint() {
    m_config.showCollidedSecs = !m_config.showCollidedSecs;
    m_scheduleCanvas.redraw();
    updateStateLabel();
  }

  private void toggleGreenDurations() {
    m_config.showGreenDurations = !m_config.showGreenDurations;
    if (m_config.showGreenDurations) {
      m_scheduleScrolls.setLayoutData(new LatticeData("1,3"));
      Point sz = m_scheduleCanvas.getSize();
      m_greenPaneCanvas.setSize(80, sz.y);
      m_greenPaneScrolls.setVisible(true);
      m_greenPaneCanvas.setVisible(true);
      m_scheduleCanvas.redraw();
      layout();
    } else {
      m_scheduleScrolls.setLayoutData(new LatticeData("1,3,2,3"));
      m_greenPaneCanvas.setVisible(false);
      m_greenPaneScrolls.setVisible(false);
      m_scheduleCanvas.redraw();
      // m_greenPaneCanvas.setSize(0,0);
      layout();
    }
  }

  private void toggleLegend() {
    m_config.showLegend = !m_config.showLegend;
  }

  private void toggleMcmTimeHint() {
    if (m_config.mcmHintGp == m_markerY)
      m_config.mcmHintGp = -1;
    else
      m_config.mcmHintGp = m_markerY;
    updateHintInfo(m_config.mcmHintGp);

    m_groupListCanvas.redraw();
    m_scheduleCanvas.redraw();
  }

  private void toggleUncollidedSecsHint() {
    if (m_config.mcmUncollidedHintGp == m_markerY)
      m_config.mcmUncollidedHintGp = -1;
    else
      m_config.mcmUncollidedHintGp = m_markerY;

    m_scheduleCanvas.redraw();
    updateStateLabel();
  }

  private void updateCursorShape() {
    Display d = m_scheduleCanvas.getDisplay();
    Cursor cursor = d.getSystemCursor(SWT.CURSOR_ARROW);
    if (m_state == S_INSERT) {
      cursor = d.getSystemCursor(SWT.CURSOR_HAND);
    } else if ((m_state == S_DEFAULT && m_hoverInfo.atBorder)
        || m_state == S_RESIZE_MOUSE || m_state == S_RESIZE_KB) {
      cursor = d.getSystemCursor(SWT.CURSOR_SIZEWE);
    } else if (m_state == S_MODIFY) {
      cursor = d.getSystemCursor(SWT.CURSOR_UPARROW);
    } else if (m_state == S_DEFAULT) {
      if (LightTypes.isGreen(m_hoverInfo.light) && !m_hoverInfo.atBorder)
        cursor = d.getSystemCursor(SWT.CURSOR_HAND);
    } else if (m_state == S_PROLONG_GREEN) {
      cursor = d.getSystemCursor(SWT.CURSOR_HAND);
    }

    m_scheduleCanvas.setCursor(cursor);

    updateStateLabel();
  }

  private void updateHintInfo(int referenceGroup) {
    if (referenceGroup == -1)
      return;
    GroupProgram prog = m_schedule.getProgram(referenceGroup);
    Range[] greenBounds = prog.findGreenLight();
    m_hintInfo.groupHints = new GroupProgram.McmMarker[m_schedule.getNumGroups()][];
    m_hintInfo.greenBounds = greenBounds;

    if (greenBounds == null)
      return;

    for (int i = 0; i < m_schedule.getNumGroups(); ++i) {
      if (!Traffix.model().areGroupsColliding(referenceGroup, i))
        continue;
      m_hintInfo.groupHints[i] = new GroupProgram.McmMarker[greenBounds.length * 2];
      for (int x = 0; x < greenBounds.length; ++x) {
        int boundL = greenBounds[x].from();
        int boundR = greenBounds[x].to();

        int zoneL = boundL - Traffix.model().getMcmTime(i, referenceGroup);
        int zoneR = boundR + Traffix.model().getMcmTime(referenceGroup, i);

        int zoneLen = zoneR - zoneL;
        if (zoneLen > m_schedule.getProgramLength()) {
          zoneR = zoneL + m_schedule.getProgramLength();
        }

        GroupProgram.McmMarker hint = new GroupProgram.McmMarker();
        hint.dir = 0;
        hint.time = m_schedule.normalizeTime(zoneL);
        m_hintInfo.groupHints[i][2 * x] = hint;

        hint = new GroupProgram.McmMarker();
        hint.dir = 1;
        hint.time = m_schedule.normalizeTime(zoneR);
        hint.time = prog.normalizeTime(hint.time);
        m_hintInfo.groupHints[i][2 * x + 1] = hint;
      }
    }
  }

  private void updateHoverInfo() {
    Point mPos = m_scheduleCanvas.getDisplay().getCursorLocation();
    mPos = m_scheduleCanvas.toControl(mPos.x, mPos.y);
    m_hoverInfo.mouseX = mPos.x;
    m_hoverInfo.mouseY = mPos.y;
    m_hoverInfo.prevGroup = m_hoverInfo.group;

    int group = mPos.y / ScheduleStyle.groupBarH;
    if (group >= 0 && group < m_schedule.getNumGroups())
      m_hoverInfo.group = group;

    m_hoverInfo.atBorder = false;
    int pos = getMarkerXAtPixel(mPos.x);
    m_hoverInfo.sec = pos;
    if (m_hoverInfo.group != -1 && pos != -1) {
      GroupProgram prog = m_schedule.getProgram(m_hoverInfo.group);
      if (prog.get(pos - 1) != prog.get(pos)) {
        m_hoverInfo.atBorder = true;
      }

      m_hoverInfo.light = prog.get(pos);
    }
  }

  private void updateMarkerXFromMouseX() {
    double markerPos = Math
        .floor(m_hoverInfo.mouseX / (double) ScheduleStyle.cellW + 0.5);
    if (markerPos < 0)
      markerPos = 0;

    setMarkerX(getMarkerXAtPixel(m_hoverInfo.mouseX));
  }

  private void updateSizes() {
    if (m_schedule == null) {
      return;
    }

    int x = getNumSecs() * ScheduleStyle.cellW;
    int y = ScheduleStyle.groupBarH * m_schedule.getNumGroups();
    if (m_scheduleCanvas != null)
      m_scheduleCanvas.setSize(x, y);
    if (m_groupListCanvas != null)
      m_groupListCanvas.setSize(ScheduleStyle.groupListW, ScheduleStyle.groupBarH
          * m_schedule.getNumGroups());
    if (m_topRuler != null)
      m_topRuler.setNumSecs(getNumSecs() + 1);
  }

  private void updateStateLabel() {
    String text = s_stateNames[m_state];
    int uncolGp = m_config.mcmUncollidedHintGp;
    if (m_config.showCollidedSecs)
      text = text + " -Skolidowane-";// z ("+m_schedule.getGroup(colGp).name+")-";
    if (uncolGp != -1)
      text = text + " -Nieskolidowane z (" + m_schedule.getGroup(uncolGp).getName() + ")-";
    m_stateLabel.setText(text);
    m_stateLabel.setSize(m_stateLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));

    Point sz = m_stateLabel.getSize();
    Point headerSz = m_header.getSize();
    m_stateLabel.setLocation(headerSz.x / 2 - sz.x / 2, 0);
  }

  private void updateTopMarkerWidget() {
    if (m_schedule == null)
      return;
    // top marker
    int progLen = m_schedule.getProgramLength();
    int time = m_markerX;
    m_topMarker.setText(time + " (" + (time % progLen) + ")");

    Rectangle canvasBounds = m_topMarkersCanvas.getBounds();
    Rectangle markerBounds = m_topMarker.getBounds();

    m_topMarker.setLocation(canvasBounds.x + ScheduleStyle.cellW * m_markerX - m_offset.x
        - markerBounds.width / 2, canvasBounds.y + canvasBounds.height
        - markerBounds.height);
  }
}

class ScheduleEditorConfig {
  int     colorScheme         = 0;
  int     mcmHintGp           = -1;
  int     mcmUncollidedHintGp = -1;
  int     numCycles           = 1;
  boolean showCollidedSecs    = false;
  boolean showGreenDurations  = false;
  boolean showLegend          = false;
}