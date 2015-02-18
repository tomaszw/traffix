/*
 * Created on 2004-09-01
 */

package traffix.core.sim.entities;

import java.util.*;

import org.tw.geometry.Vec2f;
import org.tw.web.XmlKit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import traffix.Traffix;
import traffix.core.VehicleGroup;
import traffix.core.schedule.Schedule;
import traffix.core.sim.ISimManager;

public class PresenceDetector extends RectangularEntity implements IDetector {
  public static final int S_IDLE                = 0;
  public static final int S_PRESSED             = 1;

  private float           m_activationTime      = 1;
  private Set<IDetector>  m_condClearDetectors  = new HashSet<IDetector>();
  private Set<String>     m_condClearDetNames   = new HashSet<String>();
  private VehicleGroup           m_condClearGroup      = null;
  private float           m_condClearTime       = Float.POSITIVE_INFINITY;
  private String          m_name                = "";
  private Set<IMobile>    m_overlapping         = new HashSet<IMobile>();
  private float           m_pressTime;
  private Set<Integer>    m_reactingMobileTypes = new HashSet<Integer>();
  private List            m_schedules           = new ArrayList();         // list of
  // string
  private int             m_state               = S_IDLE;

  public PresenceDetector() {
    initReactingTypes();
  }

  public PresenceDetector(Vec2f beg, Vec2f end, float width) {
    super(beg, end, width);
    initReactingTypes();
  }

  public Set<IDetector> condClearDetectors() {
    return m_condClearDetectors;
  }

  public float getActivationTime() {
    return m_activationTime;
  }

  public VehicleGroup getClearedBy() {
    return m_condClearGroup;
  }

  public float getCondClearTime() {
    return m_condClearTime;
  }

  public String getName() {
    return m_name;
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

  public boolean isActive() {
    return Traffix.simScheduleManager().isRequestActive(this);
  }

  public boolean isReactingToMobileType(int type) {
    return m_reactingMobileTypes.contains(type);
  }

  public void postLoadFix() {
    m_condClearDetectors.clear();
    for (String n : m_condClearDetNames) {

      IDetector det = Traffix.simManager().getNamedDetector(n);
      if (det != null)
        m_condClearDetectors.add(det);
    }

  }

  public void reset() {
    m_state = S_IDLE;
    m_overlapping.clear();
  }

  public void setActivationTime(float activationTime) {
    m_activationTime = activationTime;
  }

  public void setClearedBy(VehicleGroup clearedBy) {
    m_condClearGroup = clearedBy;
  }

  public void setCondClearTime(float condClearTime) {
    m_condClearTime = condClearTime;
  }

  public void setName(String name) {
    m_name = name;
  }

  public void setReactingToMobileType(int type, boolean r) {
    if (r)
      m_reactingMobileTypes.add(type);
    else
      m_reactingMobileTypes.remove(type);
  }

  public void setSchedules(List schedules) {
    m_schedules = schedules;
  }

  public boolean shouldBlock(IMobile mobile) {
    ISimManager simman = Traffix.simManager();

    if (m_state == S_IDLE) {
      if (overlaps(mobile)) {
        m_overlapping.add(mobile);
        m_state = S_PRESSED;
        m_pressTime = simman.getCurrentTime();
      }
    } else if (m_state == S_PRESSED) {
      if (!m_overlapping.contains(mobile) && overlaps(mobile)) {
        m_overlapping.add(mobile);
      }
    }

    return false;
  }

  public void tick(float t0, float delta) {
    if (m_state == S_PRESSED) {
      if (!testOverlapAndRemove(m_overlapping)) {
        m_state = S_IDLE;
      }
    }

    if (m_state == S_PRESSED && canRequestChange()) {
      Traffix.simScheduleManager().requestChange2(this, new HashSet(m_schedules),
          m_condClearGroup, m_condClearTime, m_condClearDetectors);
    }

  }

  public boolean xmlLoad(Document document, Element root) {
    if (!super.xmlLoad(document, root))
      return false;
    m_condClearGroup = null;
    if (root.hasAttribute("clearedByGroup"))
      m_condClearGroup = VehicleGroup.fromUniqueIdent(Integer.parseInt(root
          .getAttribute("clearedByGroup")));
    if (root.hasAttribute("acttime"))
      m_activationTime = Float.parseFloat(root.getAttribute("acttime"));
    if (root.hasAttribute("name"))
      m_name = root.getAttribute("name");

    m_schedules.clear();
    Element[] elems = XmlKit.childElems(root, "schedule");
    for (int i = 0; i < elems.length; i++) {
      String name = elems[i].getAttribute("name");
      m_schedules.add(Traffix.scheduleBank().get(name));
    }

    m_reactingMobileTypes.clear();
    elems = XmlKit.childElems(root, "reactsTo");
    for (Element e : elems) {
      m_reactingMobileTypes.add(Integer.parseInt(e.getAttribute("type")));
    }

    m_condClearTime = Float.POSITIVE_INFINITY;
    if (root.hasAttribute("condClearTime"))
      m_condClearTime = Float.parseFloat(root.getAttribute("condClearTime"));

    m_condClearDetectors.clear();
    m_condClearDetNames.clear();
    elems = XmlKit.childElems(root, "condClearDet");
    for (Element e : elems) {
      String n = e.getAttribute("name");
      m_condClearDetNames.add(n);
    }
    return true;
  }

  public Element xmlSave(Document document) {
    Element root = super.xmlSave(document);
    root.setAttribute("acttime", Float.toString(m_activationTime));
    root.setAttribute("name", m_name);

    if (m_condClearGroup != null)
      root.setAttribute("clearedByGroup", Integer.toString(m_condClearGroup.getUniqueID()));

    for (Iterator iter = m_schedules.iterator(); iter.hasNext();) {
      Schedule s = (Schedule) iter.next();
      Element e = document.createElement("schedule");
      e.setAttribute("name", s.getName());
      root.appendChild(e);
    }

    for (int t : m_reactingMobileTypes) {
      Element e = document.createElement("reactsTo");
      e.setAttribute("type", Integer.toString(t));
      root.appendChild(e);
    }

    if (m_condClearTime != Float.POSITIVE_INFINITY)
      root.setAttribute("condClearTime", Float.toString(m_condClearTime));

    for (IDetector d : m_condClearDetectors) {
      Element e = document.createElement("condClearDet");
      e.setAttribute("name", d.getName());
      root.appendChild(e);
    }
    return root;
  }

  private boolean canRequestChange() {
    return (Traffix.simManager().getCurrentTime() - m_pressTime >= m_activationTime);
  }

  private void initReactingTypes() {
    m_reactingMobileTypes.clear();
    m_reactingMobileTypes.add(IMobile.NormalVehicle);
    m_reactingMobileTypes.add(IMobile.HeavyVehicle);
    m_reactingMobileTypes.add(IMobile.Bus);
    m_reactingMobileTypes.add(IMobile.Trolley);
    m_reactingMobileTypes.add(IMobile.Pedestrian);
  }

  private boolean overlaps(IMobile m) {
    if (!m_reactingMobileTypes.contains(m.getType()))
      return false;
    if (getObb().overlaps(m.getObb()))
      return true;
    if (!(m instanceof IMobileChain))
      return false;
    IMobileChain cm = (IMobileChain) m;
    while (cm.getNextInChain() != null) {
      cm = cm.getNextInChain();
      if (overlaps(cm))
        return true;
    }
    return false;
  }

  private boolean testOverlapAndRemove(Collection<IMobile> mobiles) {
    boolean flag = false;
    for (Iterator<IMobile> iter = mobiles.iterator(); iter.hasNext();) {
      IMobile m = iter.next();
      if (!m.shouldDisintegrate() && overlaps(m)) {
        flag = true;
      } else {
        iter.remove();
      }
    }
    return flag;
  }
}