/*
 * Created on 2004-09-01
 */

package traffix.core.sim.entities;

import org.tw.geometry.Vec2f;
import org.tw.web.XmlKit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import traffix.Traffix;
import traffix.core.schedule.Schedule;
import traffix.core.sim.ISimManager;
import traffix.core.VehicleGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class PedestrianDetector extends RectangularEntity implements IDetector {
  public static final int S_IDLE = 0;
  public static final int S_PRESSED = 1;

  private int m_interval = 240;
  private float m_lastPressTime;

  private List m_schedules = new ArrayList(); // list of string
  private int m_state = S_IDLE;
  private VehicleGroup m_clearedBy = VehicleGroup.fromIndex(0);
  private String m_name="";

  public PedestrianDetector() {
  }

  public PedestrianDetector(Vec2f beg, Vec2f end, float width) {
    super(beg, end, width);
  }

  public int getInterval() {
    return m_interval;
  }

  public VehicleGroup getClearedBy() {
    return m_clearedBy;
  }

  public void setClearedBy(VehicleGroup clearedBy) {
    m_clearedBy = clearedBy;
  }

  public void postLoadFix() {
  }
  
  public List getSchedules() {
    return m_schedules;
  }

  public int getState() {
    return m_state;
  }

  public String getXmlTagName() {
    return "presenceDetector";
  }

  public String getName() {
    return m_name;
  }

  public boolean isActive() {
    return Traffix.simScheduleManager().isRequestActive(this);
  }

  public void setName(String name) {
    m_name = name;
  }

  public void reset() {
    m_state = S_IDLE;
    m_lastPressTime = 0;
  }

  public void setInterval(int interval) {
    m_interval = interval;
  }

  public void setSchedules(List schedules) {
    m_schedules = schedules;
  }

  public boolean shouldBlock(IMobile mobile) {
    ISimManager simman = Traffix.simManager();

    return false;
  }


  public void tick(float t0, float delta) {
    if (m_state == S_IDLE) {
      if (t0 - m_lastPressTime > m_interval) {
        m_lastPressTime = t0;
        m_state = S_PRESSED;
        if (canRequestChange()) {
          Traffix.simScheduleManager().requestChange(this, new HashSet(m_schedules), m_clearedBy);
        }
      }
    } else if (m_state == S_PRESSED) {
      if (t0 - m_lastPressTime > 1)
        m_state = S_IDLE;
    }
  }

  public boolean xmlLoad(Document document, Element root) {
    if (!super.xmlLoad(document, root))
      return false;
    if (root.hasAttribute("clearedByGroup"))
      m_clearedBy = VehicleGroup.fromUniqueIdent(Integer.parseInt(root.getAttribute("clearedByGroup")));
    if (root.hasAttribute("name"))
      m_name = root.getAttribute("name");
    m_interval = Integer.parseInt(root.getAttribute("interval"));
    m_schedules.clear();
    Element[] elems = XmlKit.childElems(root, "schedule");
    for (int i = 0; i < elems.length; i++) {
      String name = elems[i].getAttribute("name");
      m_schedules.add(Traffix.scheduleBank().get(name));
    }
    return true;
  }

  public Element xmlSave(Document document) {
    Element root = super.xmlSave(document);
    if (m_clearedBy != null)
      root.setAttribute("clearedByGroup", Integer.toString(m_clearedBy.getUniqueID()));
    root.setAttribute("interval", Integer.toString(m_interval));
    root.setAttribute("name", m_name);
    for (Iterator iter = m_schedules.iterator(); iter.hasNext();) {
      Schedule s = (Schedule) iter.next();
      Element e = document.createElement("schedule");
      e.setAttribute("name", s.getName());
      root.appendChild(e);
    }
    return root;
  }

  private boolean canRequestChange() {
    return true;
  }

  public boolean isReactingToMobileType(int type) {
    return true;
  }

  public void setReactingToMobileType(int type, boolean r) {
  }
}