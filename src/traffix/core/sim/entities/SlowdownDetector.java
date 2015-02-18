/*
 * Created on 2005-08-01
 */

package traffix.core.sim.entities;

import java.util.Iterator;

import org.tw.geometry.Vec2f;
import org.tw.web.XmlKit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import traffix.Traffix;
import traffix.core.VehicleGroup;
import traffix.core.schedule.Schedule;

public class SlowdownDetector extends AbstractMobileDetector {
  private float m_speed = 30/3.6f;
  
  public SlowdownDetector() {
  }

  public SlowdownDetector(Vec2f beg, Vec2f end, float width) {
    super(beg, end, width);
  }

  public String getXmlTagName() {
    return "slowdown-detector";
  }

  public float getSpeed() {
    return m_speed;
  }
  
  public void setSpeed(float sp) {
    m_speed = sp;
  }
  
  @Override
  public void tick(float t0, float delta) {
    super.tick(t0, delta);
    
    for (IMobile m : getOverlappingMobiles()) {
      m.limitSpeed(m_speed);
    }
  }
  
  public boolean xmlLoad(Document document, Element root) {
    if (!super.xmlLoad(document, root))
      return false;
    if (root.hasAttribute("speed"))
      m_speed = Float.parseFloat(root.getAttribute("speed"));
    return true;
  }

  public Element xmlSave(Document document) {
    Element root = super.xmlSave(document);
    root.setAttribute("speed", Float.toString(m_speed));
    return root;
  }
}
