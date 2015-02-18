/*
 * Created on 2004-07-23
 */

package traffix.ui.sim.tools;

import org.tw.geometry.Vec2f;
import traffix.Traffix;
import traffix.core.sim.entities.BusStop;
import traffix.core.sim.entities.IEntity;
import traffix.core.sim.entities.SlowdownDetector;
import traffix.ui.sim.BusStopDialog;
import traffix.ui.sim.IMapEditor;
import traffix.ui.sim.MapSensor;

public class BusStopTool extends RectEntityTool {
  private MapSensor m_sensor;

  public BusStopTool(IMapEditor editor) {
    super(editor);
    m_sensor = new MapSensor(editor);
  }

  protected IEntity detectEntity(Vec2f p) {
    return m_sensor.senseBusStop(p);
  }

  protected void entityDefined(Vec2f beg, Vec2f end, float width) {
    Traffix.simManager().entityManager().addStaticEntity(new BusStop(beg, end, width));
    Traffix.model().setModified(true);
    m_editor.getCanvas().redraw();
  }

  protected void entityDeleted(IEntity e) {
    Traffix.simManager().entityManager().removeStaticEntity(e);
    Traffix.model().setModified(true);
  }

  protected void entityDoubleClicked(IEntity e) {
    BusStop bs = (BusStop) e;
    BusStopDialog dlg = new BusStopDialog(m_editor.getShell(), bs);
    dlg.open();
  }

  protected RectEntityTool newInstance() {
    return new BusStopTool(m_editor);
  }

  protected void entityMoved(IEntity e, Vec2f to) {
    BusStop d = (BusStop) e;
    d.moveTo(to);
    Traffix.model().setModified(true);
  }

  protected void entityShapeChanged(IEntity e, Vec2f beg, Vec2f end, float width) {
    BusStop d = (BusStop) e;
    d.changeShape(beg, end, width);
    Traffix.model().setModified(true);
  }
}
