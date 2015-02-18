/*
 * Created on 2004-09-01
 */

package traffix.ui.sim.tools;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.tw.geometry.Vec2f;
import traffix.Traffix;
import traffix.core.sim.entities.Barrier;
import traffix.core.sim.entities.BarrierDetector;
import traffix.core.sim.entities.IEntity;
import traffix.ui.Colors;
import traffix.ui.Cursors;
import traffix.ui.Gc;
import traffix.ui.sim.IMapEditor;
import traffix.ui.sim.MapSensor;


public class BarrierDetectorTool extends RectEntityTool {
  private MapSensor m_sensor;
  private Barrier m_hoverBarrier;

  public class BindBarrier extends State {

    private BarrierDetector m_detector;
    private Vec2f m_cur;

    public BindBarrier(IMapEditor editor, BarrierDetector d) {
      super(editor);
      m_detector = d;
    }

    public void enter() {
      super.enter();
      setCursor(Cursors.ARROW);
      m_cur = getMarkerPosInMeters();
      track(m_cur);
    }

    public void onMouseMove(MouseEvent e) {
      super.onMouseMove(e);
      track(m_cur);
      m_cur = getMarkerPosInMeters();
      track(m_cur);
      m_hoverBarrier = m_sensor.senseBarrier(getMarkerPosInMeters());
      if (m_hoverBarrier != null) {
        setCursor(Cursors.ARROW_HOOK);
      } else {
        setCursor(Cursors.ARROW);
      }
    }

    private void track(Vec2f to) {
      Gc gc = m_editor.getGc();
      gc.setLineWidth(2);
      gc.setForeground(Colors.system(SWT.COLOR_WHITE));
      gc.setXORMode(true);
      Point p1 = m_editor.getCoordTransformer().terrainToScreen(m_detector.getCenter());
      Point p2 = m_editor.getCoordTransformer().terrainToScreen(to);
      gc.drawLine(p1.x, p1.y, p2.x, p2.y);
      gc.setXORMode(false);
    }

    public void leave() {
      super.leave();
      m_editor.getCanvas().redraw();
    }

    public void onMouseUp(MouseEvent e) {
      super.onMouseUp(e);
      if (e.button == 1) {
        if (m_hoverBarrier != null) {
          m_detector.bind(m_hoverBarrier);
        }
        setNextState(new BarrierDetectorTool(m_editor));
      }
    }
  }

  public class HoverBdCenter extends HoverEntityCenter {
    public HoverBdCenter(IMapEditor editor, IEntity stop) {
      super(editor, stop);
    }

    public void onMouseDown(MouseEvent e) {
      super.onMouseDown(e);
      if (e.button == 1) {
        setNextState(new BindBarrier(m_editor, (BarrierDetector) m_entity));
      }
    }

  }

  protected State createState_HoverCenter(IMapEditor ed, IEntity ent) {
    return new HoverBdCenter(ed, ent);
  }

  public BarrierDetectorTool(IMapEditor editor) {
    super(editor);
    m_sensor = new MapSensor(editor);
  }

  protected IEntity detectEntity(Vec2f p) {
    return m_sensor.senseBarrierDetector(p);
  }

  protected void entityDefined(Vec2f beg, Vec2f end, float width) {
    Traffix.simManager().entityManager().addStaticEntity(new BarrierDetector(beg, end, width));
    m_editor.getCanvas().redraw();
    Traffix.model().setModified(true);

  }

  protected void entityDeleted(IEntity e) {
    Traffix.simManager().entityManager().removeStaticEntity(e);
    Traffix.model().setModified(true);

  }

  protected void entityDoubleClicked(IEntity e) {
  }

  protected RectEntityTool newInstance() {
    return new BarrierDetectorTool(m_editor);
  }

  protected void entityMoved(IEntity e, Vec2f to) {
    BarrierDetector d = (BarrierDetector) e;
    d.moveTo(to);
    Traffix.model().setModified(true);
  }

  protected void entityShapeChanged(IEntity e, Vec2f beg, Vec2f end, float width) {
    BarrierDetector d = (BarrierDetector) e;
    d.changeShape(beg, end, width);
    Traffix.model().setModified(true);
  }

}
