/*
 * Created on 2004-08-09
 */

package traffix.core.sim.entities;

import org.tw.geometry.Vec2f;
import org.tw.geometry.Polygonf;
import org.tw.geometry.Segmentf;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import traffix.Traffix;
import traffix.core.VehicleGroup;
import traffix.core.VehicleGroupSet;
import traffix.core.model.IPersistent;
import traffix.core.schedule.LightTypes;
import traffix.core.schedule.Schedule;
import traffix.core.sim.AbstractObstacle;
import traffix.core.sim.IScheduleProvider;
import traffix.core.sim.Route;
import traffix.core.sim.graph.GraphLocation;

import java.util.Iterator;
import java.util.List;

public class Light extends RectangularEntity {
  private int m_groupId = Traffix.model().getGroupByIndex(0).getUniqueId();
  private IScheduleProvider m_schedulePv;
  private int m_state;
  private float m_yellowDur;

  public Light() {
    m_schedulePv = Traffix.simManager();
  }

  public Light(IScheduleProvider provider) {
    m_schedulePv = provider;
  }

  public String getXmlTagName() {
    return "light";
  }

  public void reset() {
    m_state = LightTypes.NO_SIGNAL;
    m_yellowDur = 0;
  }

  public Light(IScheduleProvider scheduleSel, Vec2f beg, Vec2f end, float width) {
    super(beg,end,width);
    m_schedulePv = scheduleSel;
  }

  public VehicleGroup getGroup() {
    return VehicleGroup.fromUniqueIdent(m_groupId);
  }

  //  public float constrainDistanceToMobile(float distance, GraphLocation obstacleLoc,
  //      IMobile mobile) {
  //    return distance - m_axis2.length()/2;
  //  }

  public float getObstacleRadius() {
    return getAxis2().length()/2;
  }

  public int getState() {
    return m_state;
  }

  public boolean isInside(Vec2f p) {
    return getBounds().isInside(p);
  }

  public void setGroup(VehicleGroup g) {
    m_groupId = g.getUniqueId();
  }

  public boolean shouldBlock(IMobile mobile) {
    if (!shouldBlock(getGroup(), mobile)) {
      return false;
    }
    VehicleGroupSet linked = getLinkedLights(mobile);
    if (!linked.contains(getGroup()))
      return true;
    for (Iterator<VehicleGroup> iter = getLinkedLights(mobile).iterator(); iter.hasNext();) {
      VehicleGroup lg = iter.next();
      if (lg == getGroup())
        continue;
      if (lg == null)
        continue;
      if (!shouldBlock(lg, mobile))
        return false;
    }
    return true;
  }

  private int getState(VehicleGroup g) {
    int t0 = Traffix.simScheduleManager().getScheduleTime();
    Schedule sch = m_schedulePv.getActiveSchedule();
    if (sch != null && g != null) {
      return sch.getProgram(g.getIndex()).get(t0);
    }
    return LightTypes.NO_SIGNAL;
  }

  public void tick(float t0, float delta) {
    m_state = getState(getGroup());
    if (m_state != LightTypes.YELLOW) {
      m_yellowDur = 0;
    } else {
      m_yellowDur += delta;
    }
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

  public Element xmlSave(Document document) {
    Element root = super.xmlSave(document);
    root.setAttribute("group", Integer.toString(m_groupId));
    return root;
  }

  protected List<GraphLocation> computeObstacleLocations() {
    return Traffix.simManager().getGraph().intersectSegment(getBaseline(), null);
  }

  private VehicleGroupSet getLinkedLights(IMobile mobile) {
    return mobile.getRoute().getInfo().getLinkedGroups();
  }

  private boolean shouldBlock(VehicleGroup g, IMobile mobile) {
    int state = m_state;
    if (g != getGroup()) {
      state = getState(g);
    }

    if (state == LightTypes.YELLOW && m_yellowDur <= 1)
      return false;
    if (state == LightTypes.GREEN)
      return false;
    if (state == LightTypes.PULSING_GREEN && !getGroup().getPrefix().equals("t"))
      return false;
    if (state == LightTypes.PULSING_YELLOW && !getGroup().getPrefix().equals("t"))
      return false;
    Route route = mobile.getRoute();
    return route.getInfo().getControllingGroups().contains(getGroup());
  }
}