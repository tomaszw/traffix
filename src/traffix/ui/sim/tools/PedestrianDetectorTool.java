/*
 * Created on 2004-09-01
 */

package traffix.ui.sim.tools;

import org.tw.geometry.Vec2f;
import traffix.Traffix;
import traffix.core.sim.entities.IEntity;
import traffix.core.sim.entities.PedestrianDetector;
import traffix.ui.sim.IMapEditor;
import traffix.ui.sim.MapSensor;
import traffix.ui.sim.PedestrianDetectorDialog;


public class PedestrianDetectorTool extends RectEntityTool {
  private MapSensor m_sensor;

  public PedestrianDetectorTool(IMapEditor editor) {
    super(editor);
    m_sensor = new MapSensor(editor);
  }

  protected IEntity detectEntity(Vec2f p) {
    return m_sensor.sensePedestrianDetector(p);
  }

  protected void entityDefined(Vec2f beg, Vec2f end, float width) {
    Traffix.simManager().entityManager().addStaticEntity(new PedestrianDetector(beg, end, width));
    Traffix.model().setModified(true);
    m_editor.getCanvas().redraw();
  }

  protected void entityDeleted(IEntity e) {
    Traffix.simManager().entityManager().removeStaticEntity(e);
    Traffix.model().setModified(true);
  }

  protected void entityDoubleClicked(IEntity e) {
    PedestrianDetector d = (PedestrianDetector) e;
    PedestrianDetectorDialog dlg = new PedestrianDetectorDialog(m_editor.getShell(), d);
    dlg.open();
  }

  protected RectEntityTool newInstance() {
    return new PedestrianDetectorTool(m_editor);
  }

  protected void entityMoved(IEntity e, Vec2f to) {
    PedestrianDetector d = (PedestrianDetector) e;
    d.moveTo(to);
    Traffix.model().setModified(true);
  }

  protected void entityShapeChanged(IEntity e, Vec2f beg, Vec2f end, float width) {
    PedestrianDetector d = (PedestrianDetector) e;
    d.changeShape(beg, end, width);
    Traffix.model().setModified(true);
  }

}
