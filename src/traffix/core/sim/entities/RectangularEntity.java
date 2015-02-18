/*
 * Created on 2004-08-09
 */

package traffix.core.sim.entities;

import org.tw.geometry.Obb;
import org.tw.geometry.Vec2f;
import org.tw.geometry.Polygonf;
import org.tw.geometry.Segmentf;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import traffix.Traffix;
import traffix.core.model.IPersistent;
import traffix.core.sim.AbstractObstacle;
import traffix.core.sim.graph.GraphLocation;

import java.util.LinkedList;
import java.util.List;

public abstract class RectangularEntity extends AbstractObstacle implements IPersistent {

  private Vec2f m_axis1, m_axis2;
  private Vec2f m_center;
  private Obb m_obb;

  public RectangularEntity() {
  }

  public RectangularEntity(Vec2f beg, Vec2f end, float width) {
    m_axis1 = end.sub(beg);
    m_axis2 = new Vec2f(-m_axis1.y, m_axis1.x).normalize().mul(width);
    m_center = beg.add(m_axis1.mul(0.5f));
  }

  public RectangularEntity(Vec2f center, Vec2f axis1, Vec2f axis2) {
    m_center = new Vec2f(center);
    m_axis1 = new Vec2f(axis1);
    m_axis2 = new Vec2f(axis2);
  }

  public void changeShape(Vec2f beg, Vec2f end, float width) {
    m_axis1 = end.sub(beg);
    m_axis2 = new Vec2f(-m_axis1.y, m_axis1.x).normalize().mul(width);
    m_center = beg.add(m_axis1.mul(0.5f));
    update();
  }

  public void changeShape(Vec2f center, Vec2f axis1, Vec2f axis2) {
    m_center = new Vec2f(center);
    m_axis1 = new Vec2f(axis1);
    m_axis2 = new Vec2f(axis2);
    update();
  }

  public Segmentf getBaseline() {
    Segmentf seg = new Segmentf();
    Vec2f half_d1 = m_axis1.mul(0.5f);
    seg.a = m_center.sub(half_d1);
    seg.b = m_center.add(half_d1);
    return seg;
  }

  public Polygonf getBounds() {
    Vec2f half_d1 = m_axis1.mul(0.5f);
    Vec2f half_d2 = m_axis2.mul(0.5f);

    Polygonf poly = new Polygonf();
    poly.addPoint(m_center.sub(half_d1).sub(half_d2));
    poly.addPoint(m_center.add(half_d1).sub(half_d2));
    poly.addPoint(m_center.add(half_d1).add(half_d2));
    poly.addPoint(m_center.sub(half_d1).add(half_d2));

    return poly;
  }

  public Vec2f getCenter() {
    return m_center;
  }

  public Obb getObb() {
    return m_obb;
  }

  public float getObstacleRadius() {
    return 0;//m_axis2.length() / 2;
  }

  public Vec2f getAxis1() {
    return m_axis1;
  }
  
  public Vec2f getAxis2() {
    return m_axis2;
  }
  
  public boolean isInside(Vec2f p) {
    return getBounds().isInside(p);
  }

  public void moveTo(Vec2f p) {
    m_center = p;
    update();
  }

  public void setCenter(Vec2f center) {
    m_center.assign(center);
    updateGraphLocations();
  }

  public void update() {
    m_obb = new Obb(m_center, m_axis1, m_axis2);
    updateGraphLocations();
  }

  public boolean xmlLoad(Document document, Element root) {
    try {
      m_center = new Vec2f();
      m_axis1 = new Vec2f();
      m_axis2 = new Vec2f();
      m_center.x = Float.parseFloat(root.getAttribute("cx"));
      m_center.y = Float.parseFloat(root.getAttribute("cy"));
      m_axis1.x = Float.parseFloat(root.getAttribute("a1x"));
      m_axis1.y = Float.parseFloat(root.getAttribute("a1y"));
      m_axis2.x = Float.parseFloat(root.getAttribute("a2x"));
      m_axis2.y = Float.parseFloat(root.getAttribute("a2y"));
    } catch (NumberFormatException e) {
      return false;
    }
    //updateGraphLocations();
    return true;
  }

  public Element xmlSave(Document document) {
    Element root = document.createElement(getXmlTagName());
    root.setAttribute("cx", Float.toString(m_center.x));
    root.setAttribute("cy", Float.toString(m_center.y));
    root.setAttribute("a1x", Float.toString(m_axis1.x));
    root.setAttribute("a1y", Float.toString(m_axis1.y));
    root.setAttribute("a2x", Float.toString(m_axis2.x));
    root.setAttribute("a2y", Float.toString(m_axis2.y));
    return root;
  }

  protected List<GraphLocation> computeObstacleLocations() {
    //Rectanglef clip = new Rectanglef();

    //List locs = new LinkedList();
    Polygonf bounds = getBounds();
    if (bounds == null)
      return new LinkedList<GraphLocation>();
    return Traffix.simManager().getGraph().intersectPoly(bounds);
    //    for (int i = 0; i < 4; ++i) {
    //      Pointf a = bounds.getPoint(i);
    //      Pointf b = bounds.getPoint((i + 1) % 4);
    //      Segmentf seg = new Segmentf();
    //      seg.a = a;
    //      seg.b = b;
    //      float cx1, cy1, cx2, cy2;
    //      cx1 = Math.min(seg.a.x, seg.b.x);
    //      cx2 = Math.max(seg.a.x, seg.b.x);
    //      cy1 = Math.min(seg.a.y, seg.b.y);
    //      cy2 = Math.max(seg.a.y, seg.b.y);
    //      clip.x = cx1;
    //      clip.y = cy1;
    //      clip.width = cx2 - cx1;
    //      clip.height = cy2 - cy1;
    //      locs.addAll(Traffix.simManager().getGraph().intersectSegment(seg,
    //          clip));
    //    }
    //
    //    return locs;
  }
}