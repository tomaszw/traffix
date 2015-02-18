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

public class CondClearDetector extends RectangularEntity implements IDetector {
  public static final int S_IDLE                = 0;
  public static final int S_PRESSED             = 1;
  private String          m_name                = "";
  private Set<IMobile>    m_overlapping         = new HashSet<IMobile>();
  private Set<Integer>    m_reactingMobileTypes = new HashSet<Integer>();
  private int             m_state               = S_IDLE;

  public CondClearDetector() {
    initReactingTypes();
  }

  @Override
  public String toString() {
    return m_name;
  }
  
  public CondClearDetector(Vec2f beg, Vec2f end, float width) {
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
  
  public String getName() {
    return m_name;
  }

  public int getState() {
    return m_state;
  }

  public String getXmlTagName() {
    return "condClearDetector";
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
  }

  public void reset() {
    m_state = S_IDLE;
    m_overlapping.clear();
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
      if (!testOverlapAndRemove(m_overlapping)) {
        m_state = S_IDLE;
      }
    }
  }

  public boolean xmlLoad(Document document, Element root) {
    if (root.hasAttribute("name"))
      m_name = root.getAttribute("name");
    
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
    for (int t : m_reactingMobileTypes) {
      Element e = document.createElement("reactsTo");
      e.setAttribute("type", Integer.toString(t));
      root.appendChild(e);
    }

    return root;
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