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
import traffix.core.schedule.Schedule;
import traffix.core.sim.ISimManager;
import traffix.core.sim.ScheduleManager;

public class TransitDetector extends RectangularEntity implements IDetector {
  public static final int S_IDLE                = 0;
  public static final int S_PRESSED             = 1;
  private List<Entry>     m_entries             = new ArrayList<Entry>();
  private float           m_freezeTime          = 3;
  private String          m_name                = "";
  private Set<IMobile>    m_overlapping         = new HashSet<IMobile>();
  private Set<Integer>    m_reactingMobileTypes = new HashSet<Integer>();
  private int             m_state               = S_IDLE;

  public static class Entry {
    public List<FreezeLink> freezeLinks = new ArrayList<FreezeLink>();
    public Schedule         schedule;
    public int              sec;
    public int              tmax;
  }

  public TransitDetector() {
    initReactingTypes();
  }

  public TransitDetector(Vec2f beg, Vec2f end, float width) {
    super(beg, end, width);
    initReactingTypes();
  }

  private void initReactingTypes() {
    m_reactingMobileTypes.clear();
    m_reactingMobileTypes.add(IMobile.NormalVehicle);
    m_reactingMobileTypes.add(IMobile.HeavyVehicle);
    m_reactingMobileTypes.add(IMobile.Bus);
    m_reactingMobileTypes.add(IMobile.Trolley);
    m_reactingMobileTypes.add(IMobile.Pedestrian);
  }
  
  public List<Entry> getEntries() {
    return m_entries;
  }

  public float getFreezeTime() {
    return m_freezeTime;
  }

  public String getName() {
    return m_name;
  }

  public int getState() {
    return m_state;
  }

  public String getXmlTagName() {
    return "transitDetector";
  }

  public boolean isActive() {
    return m_state == S_PRESSED;
  }

  public boolean isReactingToMobileType(int type) {
    return m_reactingMobileTypes.contains(type);
  }

  public void notifyApproach(IMobile mobile, float remainingDist) {
    super.notifyApproach(mobile, remainingDist);

    if (m_state == S_IDLE) {
      if (overlaps(mobile)) {
        m_overlapping.add(mobile);
        m_state = S_PRESSED;
      }
    } else if (m_state == S_PRESSED) {
      if (!m_overlapping.contains(mobile) && overlaps(mobile)) {
        m_overlapping.add(mobile);
      }
    }
  }

  public void postLoadFix() {
    // fix detectors
    for (Entry e : m_entries) {
      for (FreezeLink l : e.freezeLinks) {
        l.linkedDetector = Traffix.simManager().getNamedDetector(l.linkedDetectorName);
      }
    }
  }

  public void reset() {
    m_state = S_IDLE;
    m_overlapping.clear();
  }

  public void setEntries(List<Entry> entries) {
    m_entries = entries;
  }

  public void setFreezeTime(float freezeTime) {
    m_freezeTime = freezeTime;
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

  public boolean shouldBlock(IMobile mobile) {
    return false;
  }
  public void tick(float t0, float delta) {
    if (m_state == S_PRESSED) {
      Entry e = canFreeze();
      if (e != null) {
        ISimManager simman = Traffix.simManager();
        int tmax = e.tmax;
        int linktmax = Integer.MAX_VALUE;
        boolean activeLink = false;
        // check if need to alter tmax thanks to linked detectors
        for (FreezeLink link : e.freezeLinks) {
          if (link.linkedDetector.isActive()) {
            linktmax = Math.min(linktmax, link.tmax);
            activeLink = true;
          }
        }
        if (activeLink)
          tmax = linktmax;
        // System.out.println(tmax);
        Traffix.simScheduleManager().requestFreeze(this, tmax, activeLink);
      }
      if (!testOverlapAndRemove(m_overlapping)) {
        m_state = S_IDLE;
        if (e != null)
          Traffix.simScheduleManager().prolongFreeze(m_freezeTime);
      }
    }
  }

  public boolean xmlLoad(Document document, Element root) {
    if (root.hasAttribute("name"))
      m_name = root.getAttribute("name");
    Element[] entryElems = XmlKit.childElems(root, "entry");
    m_entries = new ArrayList<Entry>();
    for (int i = 0; i < entryElems.length; i++) {
      Entry e = new Entry();
      e.schedule = Traffix.scheduleBank().get(entryElems[i].getAttribute("schedule"));
      e.sec = Integer.parseInt(entryElems[i].getAttribute("sec"));
      if (entryElems[i].hasAttribute("tmax"))
        e.tmax = Integer.parseInt(entryElems[i].getAttribute("tmax"));

      Element[] linkElems = XmlKit.childElems(entryElems[i], "freezeLink");
      for (Element linkElem : linkElems) {
        FreezeLink link = new FreezeLink();
        if (linkElem.hasAttribute("det"))
          link.linkedDetectorName = linkElem.getAttribute("det");
        if (linkElem.hasAttribute("tmax"))
          link.tmax = Integer.parseInt(linkElem.getAttribute("tmax"));
        e.freezeLinks.add(link);
      }
      m_entries.add(e);
    }

    if (root.hasAttribute("freezeTime"))
      m_freezeTime = Integer.parseInt(root.getAttribute("freezeTime"));
    m_reactingMobileTypes.clear();
    Element[] elems = XmlKit.childElems(root, "reactsTo");
    for (Element e : elems) {
      m_reactingMobileTypes.add(Integer.parseInt(e.getAttribute("type")));
    }

    return super.xmlLoad(document, root);
  }

  public Element xmlSave(Document document) {
    Element root = super.xmlSave(document);
    root.setAttribute("name", m_name);
    for (Iterator<Entry> iter = m_entries.iterator(); iter.hasNext();) {
      Entry e = iter.next();
      Element elem = document.createElement("entry");
      elem.setAttribute("schedule", e.schedule.getName());
      elem.setAttribute("sec", Integer.toString(e.sec));
      elem.setAttribute("tmax", Integer.toString(e.tmax));

      for (FreezeLink link : e.freezeLinks) {
        Element linkElem = document.createElement("freezeLink");
        linkElem.setAttribute("det", link.linkedDetector.getName());
        linkElem.setAttribute("tmax", Integer.toString(link.tmax));
        elem.appendChild(linkElem);
      }
      root.appendChild(elem);
    }
    root.setAttribute("freezeTime", Integer.toString((int) m_freezeTime));
    for (int t : m_reactingMobileTypes) {
      Element e = document.createElement("reactsTo");
      e.setAttribute("type", Integer.toString(t));
      root.appendChild(e);
    }

    return root;
  }

  private Entry canFreeze() {
    ScheduleManager schman = Traffix.simScheduleManager();
    int t = schman.getScheduleTime();
    Schedule sch = schman.getActiveSchedule();
    if (sch == null)
      return null;
    for (Iterator<Entry> iter = m_entries.iterator(); iter.hasNext();) {
      Entry e = iter.next();
      if (t == e.sec && e.schedule == sch)
        return e;

    }
    return null;
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