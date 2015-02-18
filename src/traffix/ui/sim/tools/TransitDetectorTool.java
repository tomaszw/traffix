/*
 * Created on 2004-09-01
 */

package traffix.ui.sim.tools;

import org.tw.geometry.Vec2f;
import traffix.Traffix;
import traffix.core.sim.entities.IEntity;
import traffix.core.sim.entities.TransitDetector;
import traffix.ui.sim.IMapEditor;
import traffix.ui.sim.MapSensor;
import traffix.ui.sim.TransitDetectorDialog;


public class TransitDetectorTool extends RectEntityTool {
  private MapSensor m_sensor;

  public TransitDetectorTool(IMapEditor editor) {
    super(editor);
    m_sensor = new MapSensor(editor);
  }

  protected IEntity detectEntity(Vec2f p) {
    return m_sensor.senseTransitDetector(p);
  }

  protected void entityDefined(Vec2f beg, Vec2f end, float width) {
    Traffix.simManager().entityManager().addStaticEntity(new TransitDetector(beg, end, width));
    m_editor.getCanvas().redraw();
    Traffix.model().setModified(true);

  }

  protected void entityDeleted(IEntity e) {
    Traffix.simManager().entityManager().removeStaticEntity((TransitDetector) e);
    Traffix.model().setModified(true);

  }

  protected void entityDoubleClicked(IEntity e) {
    TransitDetector d = (TransitDetector) e;
    TransitDetectorDialog dlg = new TransitDetectorDialog(m_editor.getShell(), d);
    dlg.open();
  }

  protected RectEntityTool newInstance() {
    return new TransitDetectorTool(m_editor);
  }

  protected void entityMoved(IEntity e, Vec2f to) {
    TransitDetector d = (TransitDetector) e;
    d.moveTo(to);
    Traffix.model().setModified(true);
  }

  protected void entityShapeChanged(IEntity e, Vec2f beg, Vec2f end, float width) {
    TransitDetector d = (TransitDetector) e;
    d.changeShape(beg, end, width);
    Traffix.model().setModified(true);
  }

}
