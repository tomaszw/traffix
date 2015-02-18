/*
 * Created on 2004-09-01
 */

package traffix.ui.sim.tools;

import org.tw.geometry.Vec2f;
import traffix.Traffix;
import traffix.core.sim.entities.IEntity;
import traffix.core.sim.entities.PresenceDetector;
import traffix.ui.sim.IMapEditor;
import traffix.ui.sim.MapSensor;
import traffix.ui.sim.PresenceDetectorDialog;


public class PresenceDetectorTool extends RectEntityTool {
  private MapSensor m_sensor;

  public PresenceDetectorTool(IMapEditor editor) {
    super(editor);
    m_sensor = new MapSensor(editor);
  }

  protected IEntity detectEntity(Vec2f p) {
    return m_sensor.sensePresenceDetector(p);
  }

  protected void entityDefined(Vec2f beg, Vec2f end, float width) {
    Traffix.simManager().entityManager().addStaticEntity(new PresenceDetector(beg, end, width));
    Traffix.model().setModified(true);
    m_editor.getCanvas().redraw();
  }

  protected void entityDeleted(IEntity e) {
    Traffix.simManager().entityManager().removeStaticEntity(e);
    Traffix.model().setModified(true);
  }

  protected void entityDoubleClicked(IEntity e) {
    PresenceDetector d = (PresenceDetector) e;
    PresenceDetectorDialog dlg = new PresenceDetectorDialog(m_editor.getShell(), d);
    dlg.open();
  }

  protected RectEntityTool newInstance() {
    return new PresenceDetectorTool(m_editor);
  }

  protected void entityMoved(IEntity e, Vec2f to) {
    PresenceDetector d = (PresenceDetector) e;
    d.moveTo(to);
    Traffix.model().setModified(true);
  }

  protected void entityShapeChanged(IEntity e, Vec2f beg, Vec2f end, float width) {
    PresenceDetector d = (PresenceDetector) e;
    d.changeShape(beg, end, width);
    Traffix.model().setModified(true);
  }

}
