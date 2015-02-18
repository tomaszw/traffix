/*
 * Created on 2004-09-01
 */

package traffix.core.sim.entities;

import org.tw.geometry.Vec2f;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import traffix.Traffix;

import java.util.*;

public class BarrierDetector extends RectangularEntity {
  public static final int S_IDLE = 0;
  public static final int S_PRESSED = 1;
  private static final float REACTION_TIME = 1;

  private Set<IMobile> m_overlapping = new HashSet<IMobile>();
  private int m_state = S_IDLE;
  private float m_depressTime = 0;

  public BarrierDetector() {
  }

  public BarrierDetector(Vec2f beg, Vec2f end, float width) {
    super(beg, end, width);
  }

  public int getState() {
    return m_state;
  }

  public String getXmlTagName() {
    return "barrierDetector";
  }

  public void reset() {
    m_state = S_IDLE;
    m_overlapping.clear();
    m_depressTime = 0;
  }

  public void notifyApproach(IMobile mobile, float remainingDist) {
    super.notifyApproach(mobile, remainingDist);

    if (m_state == S_IDLE) {
      if (overlaps(mobile)) {
        m_overlapping.add(mobile);
        m_state = S_PRESSED;
        m_depressTime = Float.POSITIVE_INFINITY;
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
        m_depressTime = t0;
      }
    }
  }

  public boolean xmlLoad(Document document, Element root) {
    return super.xmlLoad(document, root);
  }

  public Element xmlSave(Document document) {
    Element root = super.xmlSave(document);

    return root;
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

  public void bind(Barrier barrier) {
    barrier.setMaster(this);
  }

  public boolean barriersDownFor(IMobile mobile) {
    float t0 = Traffix.simManager().getCurrentTime();
    if (t0 - m_depressTime > REACTION_TIME)
      return false;
    boolean block = false;
    for (Iterator<IMobile> iter = m_overlapping.iterator(); iter.hasNext();) {
      IMobile m = iter.next();
      if (m == mobile)
        return false;
      if (m.getRoute() != mobile.getRoute())
        block = true;
    }
    return block;
  }
}