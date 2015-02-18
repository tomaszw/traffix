/*
 * Created on 2004-09-01
 */

package traffix.ui.sim.tools;

import org.tw.geometry.Vec2f;
import traffix.Traffix;
import traffix.core.sim.entities.Barrier;
import traffix.core.sim.entities.IEntity;
import traffix.ui.sim.IMapEditor;
import traffix.ui.sim.MapSensor;


public class BarrierTool extends RectEntityTool {
  private MapSensor m_sensor;

  public BarrierTool(IMapEditor editor) {
    super(editor);
    m_sensor = new MapSensor(editor);
  }

//  protected float clampWidth(float w) {
//    return Math.min(Math.abs(w),0.1f);
//  }
  
  protected IEntity detectEntity(Vec2f p) {
    return m_sensor.senseBarrier(p);
  }

  protected void entityDefined(Vec2f beg, Vec2f end, float width) {
    Traffix.simManager().entityManager().addStaticEntity(new Barrier(beg, end, width));
    m_editor.getCanvas().redraw();
    Traffix.model().setModified(true);

  }

  protected void entityDeleted(IEntity e) {
    Traffix.simManager().entityManager().removeStaticEntity(e);
    Traffix.model().setModified(true);

  }

  protected void entityDoubleClicked(IEntity e) {
    Barrier b = (Barrier) e;
    b.setActive(!b.isActive());
  }

  protected RectEntityTool newInstance() {
    return new BarrierTool(m_editor);
  }

  protected void entityMoved(IEntity e, Vec2f to) {
    Barrier d = (Barrier) e;
    d.moveTo(to);
    Traffix.model().setModified(true);
  }

  protected void entityShapeChanged(IEntity e, Vec2f beg, Vec2f end, float width) {
    Barrier d = (Barrier) e;
    d.changeShape(beg, end, width);
    Traffix.model().setModified(true);
  }
}
