/*
 * Created on 2004-07-12
 */

package traffix.ui.schedule;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

import traffix.Traffix;
import traffix.core.TerrainMap;
import traffix.core.schedule.Schedule;
import traffix.ui.Images;
import traffix.ui.Keymap;
import traffix.ui.misc.SwtKit;

public class MovePhasesFrame extends Window {
  Canvas m_canvas;
  Schedule m_schedule;
  ScrolledComposite m_scrolledComp;
  int m_time;

  public MovePhasesFrame(Shell parentShell, Schedule schedule, int time) {
    super(parentShell);
    setShellStyle(getShellStyle() | SWT.SYSTEM_MODAL);
    m_time = time;
    m_schedule = schedule;
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setSize(800, 600);
    newShell.setImage(Images.get("icons/traffix.gif"));

    newShell.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        //Traffix.getModel().getTerrainMap().disposeImage();
      }
    });
  }

  protected Control createContents(Composite parent) {
    parent.setLayout(new FillLayout());
    Composite contents = new Composite(parent, SWT.NONE);
    contents.setLayout(new FillLayout());
    m_canvas = new Canvas(contents, SWT.NO_BACKGROUND);
    m_canvas.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
    Rectangle b = Traffix.model().getTerrainMap().getZoomedImageBounds();
    m_canvas.setSize(b.width, b.height);
    m_canvas.addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        paintMap(e.gc);
      }
    });
    m_canvas.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        onKeyDown(e);
      }

      public void keyReleased(KeyEvent e) {
        onKeyUp(e);
      }
    });

    return contents;
  }

  private void onKeyDown(KeyEvent e) {
    if (e.keyCode == SWT.ESC || e.keyCode == Keymap.MOVE_PHASES_KEY)
      close();
    else if (e.keyCode == Keymap.SCREENSHOT_KEY) {
      onScreenshot();
    } else if (e.keyCode == SWT.ARROW_LEFT) {
      if ((e.stateMask & SWT.CTRL) != 0) {
        int t = m_schedule.findPrevMovePhase(m_time);
        m_time = t != -1 ? t : m_time;
      } else {
        --m_time;
      }
      m_time = m_schedule.normalizeTime(m_time);
      m_canvas.redraw();
    } else if (e.keyCode == SWT.ARROW_RIGHT) {
      if ((e.stateMask & SWT.CTRL) != 0) {
        int t = m_schedule.findNextMovePhase(m_time);
        m_time = t != -1 ? t : m_time;
      } else {
        ++m_time;
      }
      m_time = m_schedule.normalizeTime(m_time);
      m_canvas.redraw();
    } else if (e.keyCode == SWT.ARROW_UP) {
      TerrainMap map = Traffix.model().getTerrainMap();
      float ff = Math.max(0.1f, map.getFatFactor() - 0.1f);
      map.setFatFactor(ff);
      m_canvas.redraw();
    } else if (e.keyCode == SWT.ARROW_DOWN) {
      TerrainMap map = Traffix.model().getTerrainMap();
      float ff = Math.min(4.0f, map.getFatFactor() + 0.1f);
      map.setFatFactor(ff);
      m_canvas.redraw();
    } else if (e.character == 'k') {
      ColorDialog dlg = new ColorDialog(getShell(), SWT.NONE);
      RGB cl = dlg.open();
      if (cl != null) {
        Traffix.model().getTerrainMap().setPathColor(cl);
        m_canvas.redraw();
      }
    }
  }

  private void onKeyUp(KeyEvent e) {
  }

  private void onScreenshot() {
    TerrainMap map = Traffix.model().getTerrainMap();
    map.setZoom(1);
    Rectangle bs = map.getZoomedImageBounds();
    while (bs.width > 2047 || bs.height > 2047) {
      int bigDim = Math.max(bs.width, bs.height);
      float scale = 2047.0f/bigDim;
      map.setZoom(scale);
      bs = map.getZoomedImageBounds();
    }

    Image shot = new Image(Display.getDefault(), bs.width, bs.height);
    try {
      GC gc = new GC(shot);
      map.paintBackground(gc);
      map.paintGreenPaths(gc, m_time, m_schedule);
      gc.dispose();

      SwtKit.swtImageToClipboard(getShell(), shot);
    } finally {
      shot.dispose();
    }
  }

  private void paintMap(GC gc) {
    Rectangle bounds = m_canvas.getBounds();
    Image memImage = new Image(m_canvas.getDisplay(), bounds.width, bounds.height);
    GC memGc = new GC(memImage);
    TerrainMap map = Traffix.model().getTerrainMap();
    map.setZoomToFit(bounds);
    map.paintBackground(memGc);
    map.paintGreenPaths(memGc, m_time, m_schedule);
    getShell().setText(map.getImageFilename() + " : t=" + m_time);
    gc.drawImage(memImage, 0, 0);
    memGc.dispose();
    memImage.dispose();
  }
}