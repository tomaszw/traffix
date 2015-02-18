/*
 * Created on 2004-08-31
 */

package traffix.ui.sim.tools;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.tw.geometry.Vec2f;
import traffix.Traffix;
import traffix.ui.Colors;
import traffix.ui.Cursors;
import traffix.ui.Gc;
import traffix.ui.sim.CoordinateTransformer;
import traffix.ui.sim.IMapEditor;


public class ScaleTool extends State {
  private State m_prevState;
  private PaintListener m_paintListener;
  private Vec2f m_p1, m_p2;

  public ScaleTool(IMapEditor editor, State prev) {
    super(editor);
    m_prevState = prev;

  }

  public void onMouseDown(MouseEvent e) {
    super.onMouseDown(e);
    if (e.button == 1) {
      if (m_p1 == null) {
        m_p1 = getMarkerPosInMeters();
        m_editor.getCanvas().redraw();
      } else if (m_p2 == null) {
        m_p2 = getMarkerPosInMeters();
        m_editor.getCanvas().redraw();

        IInputValidator validator = new IInputValidator() {
          public String isValid(String newText) {
            try {
              float f = Float.parseFloat(newText);
              if (f <= 0)
                return "D³ugoœæ musi byæ wiêksza ni¿ 0";
            } catch (NumberFormatException e) {
              return "B³êdna d³ugoœæ";
            }
            return null;
          }
        };

        InputDialog dlg = new InputDialog(m_editor.getShell(),
          "D³ugoœæ odcinka", "Podaj d³ugoœæ odcinka w metrach", "10", validator);

        if (dlg.open() == dlg.OK) {
          Point p1 = m_editor.getCoordTransformer().terrainToScreen(m_p1);
          Point p2 = m_editor.getCoordTransformer().terrainToScreen(m_p2);
          int dx = p2.x - p1.x;
          int dy = p2.y - p1.y;
          float curLen = (float) Math.sqrt(dx*dx + dy*dy)/Traffix.simManager().getZoom();
          float askedLen = Float.parseFloat(dlg.getValue());

          if (curLen != 0) {
            float scale = askedLen/curLen;
            Rectangle bs = Traffix.simManager().getTerrainImage().getBounds();
            float w = bs.width*scale;
            float h = bs.height*scale;
            Traffix.simManager().rescaleMap(w, h);
            Traffix.model().setModified(true);
          }
        }
        setNextState(m_prevState);
      }
    }
  }

  public void enter() {
    super.enter();
    setCursor(Cursors.CROSS);
    m_paintListener = new PaintListener() {
      public void paintControl(PaintEvent e) {
        onPaint();
      }
    };
    m_editor.getCanvas().addPaintListener(m_paintListener);
    Traffix.inform("Kliknij w dwóch punktach i wprowadŸ d³ugoœæ odcinka");
  }

  public void leave() {
    super.leave();
    m_editor.getCanvas().removePaintListener(m_paintListener);
    m_editor.getCanvas().redraw();
  }

  private void onPaint() {
    Gc gc = m_editor.getGc();
    CoordinateTransformer ct = m_editor.getCoordTransformer();
    if (m_p1 != null) {
      Point p = ct.terrainToScreen(m_p1);
      gc.setBackground(Colors.get(new RGB(0, 255, 0)));
      gc.fillOval(p.x - 4, p.y - 4, 9, 9);
    }
    if (m_p2 != null) {
      Point p1 = ct.terrainToScreen(m_p1);
      Point p2 = ct.terrainToScreen(m_p2);
      gc.setBackground(Colors.get(new RGB(0, 255, 0)));
      gc.fillOval(p2.x - 4, p2.y - 4, 9, 9);
      gc.setLineStyle(SWT.LINE_SOLID);
      gc.setLineWidth(1);
      gc.setForeground(Colors.get(new RGB(0, 255, 0)));
      gc.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

  }

}
