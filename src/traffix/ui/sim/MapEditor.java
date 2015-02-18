/*
 * Created on 2004-07-23
 */

package traffix.ui.sim;

import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.tw.geometry.Rectanglef;
import org.tw.geometry.Vec2f;
import org.tw.patterns.observer.IUpdateListener;

import traffix.Traffix;
import traffix.core.Pair;
import traffix.core.model.Model;
import traffix.core.sim.ISimManager;
import traffix.ui.*;
import traffix.ui.sim.entities.*;
import traffix.ui.sim.tools.PathsTool;
import traffix.ui.sim.tools.State;

public class MapEditor extends ScrolledCanvas
    implements
      IMapEditor,
      IUiEntityContextProvider {
  private CoordinateTransformer m_coordTransformer;
  private State            m_editorState;
  private List             m_entities      = new ArrayList();
  private UiEntityContext  m_entityContext = new UiEntityContext();
  private Gc               m_gc;
  private float            m_markerX, m_markerY;
  private Image            m_memImage;
  private GC               m_swtGc;
  private Rectangle        m_terrainImgBounds;
  private Font             m_messageFont;
  private IUpdateListener  m_simManagerLis;

  public MapEditor(Composite parent, int style) {
    super(parent, style);// | SWT.NO_BACKGROUND);
    UiEntityFactory.setUiEntityContextProvider(this);
    setCanvasSize(1024, 768);

    m_swtGc = new GC(getCanvas());
    m_gc = new Gc(m_swtGc);
    m_messageFont = new Font(Display.getDefault(), "Courier", 10, SWT.NORMAL);
    getCanvas().addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        m_messageFont.dispose();
        m_swtGc.dispose();
        if (m_memImage != null) {
          m_memImage.dispose();
          m_memImage = null;
        }
      }
    });
    m_entityContext.mapEditor = this;
    m_entityContext.setGc(wrapGc(m_swtGc));
    m_terrainImgBounds = new Rectangle(0, 0, 1024, 768);
    // hook up stuff
    final IUpdateListener modelLis = new IUpdateListener() {
      public void onUpdate(int hint, Object data) {
        if (hint == Model.EVT_CHANGE_ACTIVE_SIMMAN) {
          Pair<ISimManager, ISimManager> p = (Pair<ISimManager, ISimManager>) data;
          p.a.removeUpdateListener(m_simManagerLis);
          hookSimulationManagerEvents();
          recreateEntities();
          getCanvas().redraw();
        }
      }
    };
    Traffix.model().addUpdateListener(modelLis);
    addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent arg0) {
        Traffix.model().removeUpdateListener(modelLis);
      }
    });

    hookSimulationManagerEvents();
    hookCanvasEvents();

    updateMapScreenBounds();

    m_editorState = new PathsTool(this);
    m_editorState.enter();

    recreateEntities();

    getCanvas().setFocus();
  }

  public CoordinateTransformer getCoordTransformer() {
    return m_coordTransformer;
  }

  public State getCurrentState() {
    return m_editorState;
  }

  public IUiEntity getEntity(int i) {
    return (IUiEntity) m_entities.get(i);
  }

  public Gc getGc() {
    return m_gc;
  }

  public Vec2f getMarkerPos() {
    return new Vec2f(m_markerX, m_markerY);
  }

  public int getNumEntities() {
    return m_entities.size();
  }

  public UiEntityContext getUiEntityContext() {
    return m_entityContext;
  }

  public void setCurrentState(State toState) {
    m_editorState.setNextState(toState);
    stateTransit();
  }

  public void setSimulationRunning(boolean running) {
    m_entityContext.simRunning = running;
    getDisplay().asyncExec(new Runnable() {
      public void run() {
        redrawCanvas();
      }
    });
  }

  public Rectanglef getVisibleRect() {
    Rectanglef rc = new Rectanglef();
    Rectangle r = getBounds();
    Vec2f start = m_coordTransformer.screenToTerrain(getOrigin());
    Vec2f sz = m_coordTransformer.screenToTerrain(new Point(r.width, r.height));
    rc.x = start.x;
    rc.y = start.y;
    rc.width = sz.x;
    rc.height = sz.y;
    return rc;
  }

  private void hookCanvasEvents() {
    getCanvas().addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        updateEntityContext();
        paintMap(e.gc);
      }
    });

    getCanvas().addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        m_editorState.onKeyDown(e);
        stateTransit();
      }

      public void keyReleased(KeyEvent e) {
        m_editorState.onKeyUp(e);
        stateTransit();
      }
    });

    getCanvas().addMouseMoveListener(new MouseMoveListener() {
      public void mouseMove(MouseEvent e) {
        onMouseMove(e);
        m_editorState.onMouseMove(e);
        stateTransit();
      }
    });

    getCanvas().addMouseListener(new MouseListener() {
      public void mouseDoubleClick(MouseEvent e) {
        m_editorState.onDoubleClick(e);
        stateTransit();
      }

      public void mouseDown(MouseEvent e) {
        m_editorState.onMouseDown(e);
        stateTransit();
      }

      public void mouseUp(MouseEvent e) {
        m_editorState.onMouseUp(e);
        stateTransit();
      }
    });

    getHorizontalBar().addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        redrawCanvas();
      }
    });

    getVerticalBar().addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        redrawCanvas();
      }
    });
  }

  private void hookSimulationManagerEvents() {
    final IUpdateListener simListener = new IUpdateListener() {
      public void onUpdate(final int hint, final Object data) {
        onSimManagerChange();
      }
    };

    Traffix.model().getActiveSimManager().addUpdateListener(simListener);

    addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        Traffix.model().getActiveSimManager().removeUpdateListener(simListener);
      }
    });

    m_simManagerLis = simListener;

  }

  private void onMouseMove(MouseEvent e) {
    Vec2f p = getCoordTransformer().screenToTerrain(e.x, e.y);
    m_markerX = p.x;
    m_markerY = p.y;
  }

  private void paintMap(GC gc) {
    updateCoordTransformer();

    float zoom = Traffix.simManager().getZoom();

    // where to draw to
    Rectangle dstBounds = getBounds();
    dstBounds.x = getOrigin().x;
    dstBounds.y = getOrigin().y;

    // background source
    BigImage fullBg = Traffix.simManager().getTerrainImage();
    Rectangle fullBounds = fullBg.getBounds();
    Rectangle srcBounds = new Rectangle((int) (getOrigin().x / zoom),
        (int) (getOrigin().y / zoom), (int) (dstBounds.width / zoom),
        (int) (dstBounds.height / zoom));
    srcBounds.width = Math.min(srcBounds.width, fullBounds.width - srcBounds.x);
    srcBounds.height = Math.min(srcBounds.height, fullBounds.height - srcBounds.y);
    dstBounds.width = (int) (srcBounds.width * zoom);
    dstBounds.height = (int) (srcBounds.height * zoom);

    Image bgImage = fullBg.getImagePortion(srcBounds, new Point(dstBounds.width,
        dstBounds.height));

    if (m_memImage == null || dstBounds.width != m_memImage.getBounds().width
        || dstBounds.height != m_memImage.getBounds().height) {
      if (m_memImage != null)
        m_memImage.dispose();
      m_memImage = new Image(getDisplay(), dstBounds.width, dstBounds.height);
    }
    MapGc memGc = new MapGc(new GC(m_memImage), this);
    memGc.drawImage(bgImage, 0, 0);
    // sort entities
    Collections.sort(m_entities, new Comparator() {
      public int compare(Object o1, Object o2) {
        IUiEntity e1 = (IUiEntity) o1;
        IUiEntity e2 = (IUiEntity) o2;
        if (e1.getPaintPriority() < e2.getPaintPriority())
          return -1;
        else if (e1.getPaintPriority() > e2.getPaintPriority())
          return 1;
        return 0;
      }
    });
    // paint entities
    Gc prevGc = m_entityContext.getGc();
    m_entityContext.setGc(memGc);
    memGc.setViewportOrigin(dstBounds.x, dstBounds.y);
    for (int i = 0; i < getNumEntities(); ++i)
      getEntity(i).paint();
    m_entityContext.setGc(prevGc);
    // filming rectangle
    Rectanglef filmRc = Traffix.simManager().getFilmRectangle();
    if (filmRc != null) {
      Point p1 = getCoordTransformer().terrainToScreen(new Vec2f(filmRc.x, filmRc.y));
      Point p2 = getCoordTransformer().terrainToScreen(
          new Vec2f(filmRc.x + filmRc.width, filmRc.y + filmRc.height));
      int x = Math.min(p1.x, p2.x);
      int y = Math.max(p1.y, p2.y);
      int w = Math.abs(p1.x - p2.x);
      int h = Math.abs(p1.y - p2.y);
      memGc.setLineWidth(2);
      memGc.setLineStyle(SWT.LINE_SOLID);
      memGc.setForeground(Colors.get(new RGB(0, 0, 255)));
      memGc.drawRectangle(x, y, w, h);
      memGc.setLineWidth(1);
    }
    memGc.setViewportOrigin(0, 0);

    // paint time
    // if (m_entityContext.m_simRunning) {
    paintInfoLine(memGc);
    // }

    // swap buffers
    gc.drawImage(m_memImage, dstBounds.x, dstBounds.y);

    memGc.dispose();

  }

  public void paintInfoLine() {
    MapGc gc = new MapGc(new GC(getCanvas()), this);
    paintInfoLine(gc);
    gc.dispose();
  }

  private void paintInfoLine(MapGc memGc) {
    String time = Traffix.simManager().getSimulationMessage();
    Point sz = memGc.textExtent(time);
    memGc.setBackground(Colors.get(new RGB(255, 255, 0)));
    memGc.setForeground(Colors.system(SWT.COLOR_BLACK));
    memGc.setFont(m_messageFont);
    memGc.drawText(time, 0, 0);
  }

  // private void paintMap(GC gc) {
  // updateCoordTransformer();
  // Rectangle paintBounds = getBounds();
  // paintBounds.x = getOrigin().x;
  // paintBounds.y = getOrigin().y;
  // Image bgImage = Traffix.getModel().getTerrainMap().getBackgroundImage();
  // paintBounds.width = Math.min(bgImage.getBounds().width - paintBounds.x,
  // paintBounds.width);
  // paintBounds.height = Math.min(bgImage.getBounds().height - paintBounds.y,
  // paintBounds.height);
  //
  // Image memImage = new Image(getDisplay(), paintBounds.width, paintBounds.height);
  // MapGc memGc = new MapGc(new GC(memImage), this);
  // try {
  // //BigImage bgImage = Traffix.getModel
  // // paint bg
  // memGc.drawImage(bgImage, paintBounds.x, paintBounds.y, paintBounds.width,
  // paintBounds.height, 0, 0, paintBounds.width, paintBounds.height);
  //
  // // sort entities
  // Collections.sort(m_entities, new Comparator() {
  // public int compare(Object o1, Object o2) {
  // IUiEntity e1 = (IUiEntity) o1;
  // IUiEntity e2 = (IUiEntity) o2;
  // if (e1.getPaintPriority() < e2.getPaintPriority())
  // return -1;
  // else if (e1.getPaintPriority() > e2.getPaintPriority())
  // return 1;
  // return 0;
  // }
  // });
  //
  // // paint entities
  // MapGc prevGc = m_entityContext.getMapGc();
  // m_entityContext.setMapGc(memGc);
  // memGc.setViewportOrigin(paintBounds.x, paintBounds.y);
  // for (int i = 0; i < getNumEntities(); ++i)
  // getEntity(i).paint();
  // memGc.setViewportOrigin(0, 0);
  // m_entityContext.setMapGc(prevGc);
  //
  // // paint time
  // if (m_entityContext.m_simRunning) {
  // String time = SimulationManager.get().getTimeString();
  // Point sz = memGc.textExtent(time);
  // memGc.setBackground(Colors.get(new RGB(255, 255, 0)));
  // memGc.drawText(time, 0, 0);
  // }
  //
  // // swap buffers
  // gc.drawImage(memImage, paintBounds.x, paintBounds.y);
  //
  // } finally {
  // memGc.dispose();
  // memImage.dispose();
  // }
  // }

  private void recreateEntities() {
    ISimManager simman = Traffix.simManager();
    simman.setEntityContextProvider(this);
    m_entities = simman.getUiEntities();

    updateEntityContext();
  }

  private void stateTransit() {
    if (m_editorState.getNextState() != m_editorState) {
      m_editorState.leave();
      m_editorState = m_editorState.getNextState();
      m_editorState.enter();
      stateTransit();
    }
  }

  private void updateCoordTransformer() {
    Vec2f mapDims = Traffix.simManager().getTerrainDims();
    m_coordTransformer = new CoordinateTransformer(mapDims, getCanvas().getSize());
    m_entityContext.setCoordTransformer(m_coordTransformer);
  }

  private void updateEntityContext() {
    m_entityContext.setGc(wrapGc(m_swtGc));
    m_entityContext.setCoordTransformer(m_coordTransformer);
  }

  private void updateMapScreenBounds() {
    // TerrainMap map = Traffix.getModel().getTerrainMap();
    // map.setZoom(Traffix.getModel().getSimulationManager().getZoom());
    Rectangle bs = Traffix.simManager().getTerrainDimsZoomedAndOnScreen();
    setCanvasSize(bs.width, bs.height);
    layout();
    updateCoordTransformer();
  }

  private MapGc wrapGc(GC gc) {
    MapGc mapGc = new MapGc(gc, this);
    return mapGc;
  }

  private void onSimManagerChange() {
    getCanvas().setFocus();
    updateMapScreenBounds();
    recreateEntities();
    redrawCanvas();
  }
}