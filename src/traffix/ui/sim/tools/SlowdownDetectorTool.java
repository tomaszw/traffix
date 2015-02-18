/*
 * Created on 2004-09-01
 */

package traffix.ui.sim.tools;

import org.tw.geometry.Vec2f;

import traffix.Traffix;
import traffix.core.sim.entities.IEntity;
import traffix.core.sim.entities.SlowdownDetector;
import traffix.core.sim.entities.TransitDetector;
import traffix.ui.sim.IMapEditor;
import traffix.ui.sim.MapSensor;
import traffix.ui.sim.SlowdownDetectorDialog;


public class SlowdownDetectorTool extends RectEntityTool {
  private MapSensor m_sensor;

  public SlowdownDetectorTool(IMapEditor editor) {
    super(editor);
    m_sensor = new MapSensor(editor);
  }

  protected IEntity detectEntity(Vec2f p) {
    return m_sensor.senseSlowdownDetector(p);
  }

  protected void entityDefined(Vec2f beg, Vec2f end, float width) {
    Traffix.simManager().entityManager().addStaticEntity(new SlowdownDetector(beg, end, width));
    m_editor.getCanvas().redraw();
    Traffix.model().setModified(true);

  }

  protected void entityDeleted(IEntity e) {
    Traffix.simManager().entityManager().removeStaticEntity((SlowdownDetector) e);
    Traffix.model().setModified(true);

  }

  protected void entityDoubleClicked(IEntity e) {
    SlowdownDetector d = (SlowdownDetector) e;
    SlowdownDetectorDialog dlg = new SlowdownDetectorDialog(m_editor.getShell(), d);
    dlg.open();
  }

  protected RectEntityTool newInstance() {
    return new SlowdownDetectorTool(m_editor);
  }

  protected void entityMoved(IEntity e, Vec2f to) {
    SlowdownDetector d = (SlowdownDetector) e;
    d.moveTo(to);
    Traffix.model().setModified(true);
  }

  protected void entityShapeChanged(IEntity e, Vec2f beg, Vec2f end, float width) {
    SlowdownDetector d = (SlowdownDetector) e;
    d.changeShape(beg, end, width);
    Traffix.model().setModified(true);
  }

}
