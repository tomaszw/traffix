/*
 * Created on 2005-08-01
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

public abstract class AbstractMobileDetector extends RectangularEntity {
  public static final int S_IDLE = 0;
  public static final int S_PRESSED = 1;
  private Set<IMobile> m_overlapping = new HashSet<IMobile>();
  private int m_state = S_IDLE;
  private String m_name="";

  
  public AbstractMobileDetector() {
  }

  public AbstractMobileDetector(Vec2f beg, Vec2f end, float width) {
    super(beg, end, width);
  }

  protected Set<IMobile> getOverlappingMobiles() {
    return m_overlapping;
  }
  
  public String getName() {
    return m_name;
  }

  public void setName(String name) {
    m_name = name;
  }

  public int getState() {
    return m_state;
  }

  public boolean isActive() {
    return m_state == S_PRESSED;
  }

  public void reset() {
    m_state = S_IDLE;
    m_overlapping.clear();
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

  private boolean overlaps(IMobile m) {
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
