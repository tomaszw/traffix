/*
 * Created on 2004-08-27
 */

package traffix.ui.sim.tools;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.tw.geometry.Vec2f;
import org.tw.geometry.Rectanglef;
import traffix.Traffix;
import traffix.ui.Colors;
import traffix.ui.Cursors;
import traffix.ui.Gc;
import traffix.ui.sim.IMapEditor;


public class MarqueeTool extends State {
  Vec2f m_p1, m_p2;

  class Define2ndPoint extends State {
    public Define2ndPoint(IMapEditor editor) {
      super(editor);
    }

    public void enter() {
      super.enter();
      m_p2 = getMarkerPosInMeters();
      track();
    }

    public void onMouseMove(MouseEvent e) {
      super.onMouseMove(e);
      track();
      m_p2 = getMarkerPosInMeters();
      track();
    }

    public void onMouseUp(MouseEvent e) {
      super.onMouseUp(e);
      if (e.button == 1) {
        Vec2f p1 = m_p1;
        Vec2f p2 = m_p2;
        Rectanglef rc = new Rectanglef();
        rc.x = Math.min(p1.x, p2.x);
        rc.y = Math.max(p1.y, p2.y);
        rc.width = Math.abs(p2.x - p1.x);
        rc.height = Math.abs(p2.y - p1.y);
        Traffix.simManager().setFilmRectangle(rc);
        Traffix.model().setModified(true);
        setNextState(new MarqueeTool(m_editor));
      }
    }

    public void leave() {
      super.leave();
      Traffix.simManager().fireUpdated();
    }

    private void track() {
      Point p1 = m_editor.getCoordTransformer().terrainToScreen(m_p1);
      Point p2 = m_editor.getCoordTransformer().terrainToScreen(m_p2);

      int x = Math.min(p1.x, p2.x);
      int y = Math.min(p1.y, p2.y);
      int w = Math.abs(p2.x - p1.x);
      int h = Math.abs(p2.y - p1.y);

      Gc gc = m_editor.getGc();
      gc.setLineStyle(SWT.LINE_DASH);
      gc.setLineWidth(1);
      gc.setXORMode(true);
      gc.setForeground(Colors.system(SWT.COLOR_WHITE));
      gc.drawRectangle(x, y, w, h);
      gc.setXORMode(false);
    }
  }

  public MarqueeTool(IMapEditor editor) {
    super(editor);
  }

  public void enter() {
    super.enter();
    setCursor(Cursors.CROSS);
  }

  public void onMouseDown(MouseEvent e) {
    super.onMouseDown(e);
    if (e.button == 1) {
      m_p1 = getMarkerPosInMeters();
      setNextState(new Define2ndPoint(m_editor));
    }
  }
}
