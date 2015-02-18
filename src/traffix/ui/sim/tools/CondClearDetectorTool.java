/*
 * Created on 2004-09-01
 */

package traffix.ui.sim.tools;

import org.tw.geometry.Vec2f;

import traffix.Traffix;
import traffix.core.sim.entities.CondClearDetector;
import traffix.core.sim.entities.IEntity;
import traffix.ui.sim.*;


public class CondClearDetectorTool extends RectEntityTool {
  private MapSensor m_sensor;

  public CondClearDetectorTool(IMapEditor editor) {
    super(editor);
    m_sensor = new MapSensor(editor);
  }

  protected IEntity detectEntity(Vec2f p) {
    return m_sensor.senseCondClearDetector(p);
  }

  protected void entityDefined(Vec2f beg, Vec2f end, float width) {
    Traffix.simManager().entityManager().addStaticEntity(new CondClearDetector(beg, end, width));
    Traffix.model().setModified(true);
    m_editor.getCanvas().redraw();
  }

  protected void entityDeleted(IEntity e) {
    Traffix.simManager().entityManager().removeStaticEntity(e);
    Traffix.model().setModified(true);
  }

  protected void entityDoubleClicked(IEntity e) {
    CondClearDetector d = (CondClearDetector) e;
    CondClearDetectorDialog dlg = new CondClearDetectorDialog(m_editor.getShell(), d);
    dlg.open();
  }

  protected RectEntityTool newInstance() {
    return new CondClearDetectorTool(m_editor);
  }

  protected void entityMoved(IEntity e, Vec2f to) {
    CondClearDetector d = (CondClearDetector) e;
    d.moveTo(to);
    Traffix.model().setModified(true);
  }

  protected void entityShapeChanged(IEntity e, Vec2f beg, Vec2f end, float width) {
    CondClearDetector d = (CondClearDetector) e;
    d.changeShape(beg, end, width);
    Traffix.model().setModified(true);
  }

}
