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
import traffix.core.model.IPersistent;
import traffix.core.sim.AbstractObstacle;
import traffix.core.sim.graph.GraphLocation;

import java.util.List;

public class BusStop extends RectangularEntity implements IPersistent {
  public boolean m_busStop, m_trolleyStop;
  public float m_maxUnloadingTime = 15;

  private IMobile m_justUnloadedMobile;
  private IMobile m_unloadingMobile;
  private float m_unloadingTime;

  public BusStop() {
  }

  public String getXmlTagName() {
    return "busStop";
  }

  public BusStop(Vec2f beg, Vec2f end, float width) {
    super(beg, end, width);
  }

  public float getObstacleRadius() {
    return getAxis2().length()/2;
  }

  public boolean isUnloading() {
    return m_unloadingMobile != null;
  }

  public void reset() {
    m_unloadingTime = 0;
    m_unloadingMobile = null;
    m_justUnloadedMobile = null;
  }

  public boolean shouldBlock(IMobile mobile) {
    if ((m_busStop && mobile.getType() == IMobile.Bus)
      || (m_trolleyStop && mobile.getType() == IMobile.Trolley)
      || (!m_busStop && !m_trolleyStop)) {
      if (mobile != m_justUnloadedMobile) {
        if (mobile != m_unloadingMobile && canBeginUnloading(mobile)) {
          m_unloadingMobile = mobile;
          m_unloadingTime = 0;
        }
        return true;
      }
    }

    return false;
  }

  public void tick(float t0, float delta) {
    if (m_unloadingMobile != null) {
      m_unloadingTime += delta;

      if (m_unloadingTime >= m_maxUnloadingTime) {
        m_unloadingTime = 0;
        m_justUnloadedMobile = m_unloadingMobile;
        m_unloadingMobile = null;
      }
    }
  }

  public void update() {
    updateGraphLocations();
  }

  public boolean xmlLoad(Document document, Element root) {
    super.xmlLoad(document, root);
    try {
      m_maxUnloadingTime = Float.parseFloat(root.getAttribute("maxUnloadTime"));
      m_trolleyStop = root.getAttribute("trolleyStop").equals("1");
      m_busStop = root.getAttribute("busStop").equals("1");

    } catch (NumberFormatException e) {
      return false;
    }
    updateGraphLocations();
    return true;
  }

  
  public Element xmlSave(Document document) {
    Element root = super.xmlSave(document);
    root.setAttribute("trolleyStop", m_trolleyStop ? "1" : "0");
    root.setAttribute("busStop", m_busStop ? "1" : "0");
    root.setAttribute("maxUnloadTime", Float.toString(m_maxUnloadingTime));

    return root;
  }

  private boolean canBeginUnloading(IMobile mobile) {
    if (mobile.getSpeed() > 0.02f)
      return false;
    float rad = getObstacleRadius();
    GraphLocation[] locs = getObstacleGraphLocations();
    for (int i = 0; i < locs.length; ++i) {
      if (mobile.getPosition().distanceTo(locs[i].getPoint()) < rad + 1.0f)
        return true;
    }
    return false;
  }
}