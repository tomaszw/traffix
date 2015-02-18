/*
 * Created on 2004-08-25
 */

package traffix.ui.sim.tools;

import org.tw.geometry.Vec2f;
import traffix.Traffix;
import traffix.core.sim.entities.BusStop;
import traffix.core.sim.entities.IEntity;
import traffix.core.sim.entities.PedestrianPath;
import traffix.ui.sim.IMapEditor;
import traffix.ui.sim.MapSensor;
import traffix.ui.sim.PedestrianPathDialog;

public class PedestrianTool extends RectEntityTool {
  private MapSensor m_sensor;

  public PedestrianTool(IMapEditor editor) {
    super(editor);
    m_sensor = new MapSensor(editor);
  }

  protected IEntity detectEntity(Vec2f p) {
    return m_sensor.sensePedestrianPath(p);
  }

  protected void entityDefined(Vec2f beg, Vec2f end, float width) {
    Traffix.simManager().entityManager().addStaticEntity(new PedestrianPath(Traffix.simManager(), beg, end, width));
    m_editor.getCanvas().redraw();
    Traffix.model().setModified(true);

  }

  protected void entityDeleted(IEntity e) {
    Traffix.simManager().entityManager().removeStaticEntity(e);
    Traffix.model().setModified(true);
  }

  protected void entityDoubleClicked(IEntity e) {
    PedestrianPath p = (PedestrianPath) e;
    PedestrianPathDialog dlg = new PedestrianPathDialog(m_editor.getShell(), p);
    dlg.open();
  }

  protected RectEntityTool newInstance() {
    return new PedestrianTool(m_editor);
  }

  protected void entityMoved(IEntity e, Vec2f to) {
    PedestrianPath d = (PedestrianPath) e;
    d.moveTo(to);
    Traffix.model().setModified(true);
  }

  protected void entityShapeChanged(IEntity e, Vec2f beg, Vec2f end, float width) {
    PedestrianPath d = (PedestrianPath) e;
    d.changeShape(beg, end, width);
    Traffix.model().setModified(true);
  }
}
