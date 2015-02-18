/*
 * Created on 2004-07-23
 */

package traffix.ui.sim.tools;

import org.tw.geometry.Vec2f;

import traffix.Traffix;
import traffix.core.sim.entities.*;
import traffix.ui.sim.IMapEditor;
import traffix.ui.sim.LightDialog;
import traffix.ui.sim.MapSensor;

public class LightTool extends RectEntityTool {
  private MapSensor m_sensor;

  public LightTool(IMapEditor editor) {
    super(editor);
    m_sensor = new MapSensor(editor);
  }

  protected IEntity detectEntity(Vec2f p) {
    return m_sensor.senseLight(p);
  }

  protected void entityDefined(Vec2f beg, Vec2f end, float width) {
    Traffix.simManager().entityManager().addStaticEntity(new Light(Traffix.simManager(), beg, end, width));
    Traffix.model().setModified(true);
    m_editor.getCanvas().redraw();
  }

  protected void entityDeleted(IEntity e) {
    Traffix.simManager().entityManager().removeStaticEntity(e);
    Traffix.model().setModified(true);
  }

  protected void entityDoubleClicked(IEntity e) {
    Light l = (Light) e;
    LightDialog dlg = new LightDialog(m_editor.getShell(), l);
    dlg.open();
  }

  protected RectEntityTool newInstance() {
    return new LightTool(m_editor);
  }

  protected void entityMoved(IEntity e, Vec2f to) {
    RectangularEntity d = (RectangularEntity) e;
    d.moveTo(to);
    Traffix.model().setModified(true);
  }

  protected void entityShapeChanged(IEntity e, Vec2f beg, Vec2f end, float width) {
    RectangularEntity d = (RectangularEntity) e;
    d.changeShape(beg, end, width);
    Traffix.model().setModified(true);
  }
}
