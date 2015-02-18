/*
 * Created on 2004-08-25
 */

package traffix.core.sim.entities;

import org.tw.geometry.Vec2f;
import org.tw.geometry.Polygonf;
import org.tw.geometry.Segmentf;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import traffix.Traffix;
import traffix.core.VehicleGroup;
import traffix.core.model.IPersistent;
import traffix.core.schedule.LightTypes;
import traffix.core.schedule.Schedule;
import traffix.core.sim.AbstractObstacle;
import traffix.core.sim.IScheduleProvider;
import traffix.core.sim.graph.GraphLocation;

import java.util.ArrayList;
import java.util.List;

public class PedestrianPath extends RectangularEntity implements IPersistent {
  private int m_groupId = Traffix.model().getGroupByIndex(0).getUniqueId();
  private IScheduleProvider m_schedulePv;

  public PedestrianPath() {
    m_schedulePv = Traffix.simManager();
  }

  public PedestrianPath(IScheduleProvider pv) {
    m_schedulePv = pv;
  }

  public void reset() {
  }

  public PedestrianPath(IScheduleProvider pv, Vec2f beg, Vec2f end, float width) {
    super(beg,end,width);
    m_schedulePv = pv;
  }

  public Segmentf getBottomLine() {
    Segmentf seg = new Segmentf();
    Vec2f half_d1 = getAxis1().mul(0.5f);
    Vec2f half_d2 = getAxis2().mul(0.5f);
    seg.a = getCenter().sub(half_d1).sub(half_d2);
    seg.b = getCenter().add(half_d1).sub(half_d2);
    return seg;
  }

  public VehicleGroup getGroup() {
    return VehicleGroup.fromUniqueIdent(m_groupId);
  }

  public Segmentf getMiddleLine() {
    Segmentf seg = new Segmentf();
    Vec2f half_d1 = getAxis1().mul(0.5f);
    seg.a = getCenter().sub(half_d1);
    seg.b = getCenter().add(half_d1);
    return seg;
  }

  //  public float constrainDistanceToMobile(float distance, GraphLocation obstacleLoc,
  //      IMobile mobile) {
  //    return distance - 1;
  //  }

  public float getObstacleRadius() {
    return 1;
  }

  public Segmentf getTopLine() {
    Segmentf seg = new Segmentf();
    Vec2f half_d1 = getAxis1().mul(0.5f);
    Vec2f half_d2 = getAxis2().mul(0.5f);
    seg.a = getCenter().sub(half_d1).add(half_d2);
    seg.b = getCenter().add(half_d1).add(half_d2);
    return seg;
  }

  public boolean isOpen() {
    Schedule sch = m_schedulePv.getActiveSchedule();
    int state = 0;
    if (sch != null) {
      int sec = Traffix.simScheduleManager().getScheduleTime();
      VehicleGroup g = VehicleGroup.fromUniqueIdent(m_groupId);
      state = sch.getProgram(g.getIndex()).get(sec);
    }
    return !LightTypes.isBlocking(state);
  }

  public void setGroup(VehicleGroup g) {
    m_groupId = g.getUniqueId();
  }

  public boolean shouldBlock(IMobile mobile) {
    return isOpen();
  }

  public void tick(float t0, float delta) {
  }

  public void update() {
    updateGraphLocations();
  }

  public boolean xmlLoad(Document document, Element root) {
    super.xmlLoad(document, root);
    try {
      m_groupId = Integer.parseInt(root.getAttribute("group"));
    } catch (NumberFormatException e) {
      return false;
    }
    updateGraphLocations();
    return true;
  }

  public String getXmlTagName() {
    return "pedestrianPath";
  }

  public Element xmlSave(Document document) {
    Element root = super.xmlSave(document);
    root.setAttribute("group", Integer.toString(m_groupId));
    return root;
  }

  protected List<GraphLocation> computeObstacleLocations() {
    List<GraphLocation> locs = new ArrayList<GraphLocation>();
    locs.addAll(Traffix.simManager().getGraph().intersectSegment(getTopLine(), null));
    locs.addAll(Traffix.simManager().getGraph().intersectSegment(getBottomLine(), null));
    return locs;
  }
}