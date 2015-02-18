/*
 * Created on 2004-09-12
 */

package traffix.core.sim.entities;

import org.tw.geometry.Vec2f;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import traffix.Traffix;

public class Barrier extends RectangularEntity {
  private boolean m_active = true;
  private BarrierDetector m_master;

  public Barrier() {
    super();
  }

  public Barrier(Vec2f beg, Vec2f end, float width) {
    super(beg, end, width);
  }

  public Barrier(Vec2f center, Vec2f axis1, Vec2f axis2) {
    super(center, axis1, axis2);
  }

  public BarrierDetector getMaster() {
    return m_master;
  }

  public String getXmlTagName() {
    return "barrier";
  }

  public boolean isActive() {
    return m_active;
  }

  public Element xmlSave(Document document) {
    Element root = super.xmlSave(document);
    root.setAttribute("active", m_active ? "1" : "0");
    if (m_master != null)
      root.setAttribute("master", Integer.toString(Traffix.simEntityManager().indexOf(m_master)));
    return root;
  }

  public boolean xmlLoad(Document document, Element root) {
    if (root.hasAttribute("active")) {
      m_active = root.getAttribute("active").equals("1");
    }
    if (root.hasAttribute("master")) {
      int idx = Integer.parseInt(root.getAttribute("master"));
      m_master = Traffix.simEntityManager().getBarrierDetector(idx);
    }

    return super.xmlLoad(document, root);
  }

  public void reset() {
  }

  public void setActive(boolean active) {
    m_active = active;
  }

  public void setMaster(BarrierDetector master) {
    m_master = master;
  }

  public boolean shouldBlock(IMobile mobile) {
    if (!m_active || !Traffix.simManager().simParams().barriersActive)
      return false;
    return m_master != null ? m_master.barriersDownFor(mobile) : false;
  }

  public void tick(float t0, float delta) {
  }

}