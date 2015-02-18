/*
 * Created on 2004-07-05
 */

package traffix.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.tw.geometry.Vec2f;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import traffix.core.model.IPersistent;

public class McmPath implements IPersistent {
  private int m_groupIndex;
  private String m_groupName;

  private List<Vec2f> m_points = new ArrayList<Vec2f>();

  public McmPath() {
  }

  public void addPoint(float x, float y) {
    m_points.add(new Vec2f(x, y));
  }

  public int getGroupIndex() {
    return m_groupIndex;
  }

  public String getGroupName() {
    return m_groupName;
  }

  public String getXmlTagName() {
    return "path";
  }

  public int getNumPoints() {
    return m_points.size();
  }

  public Vec2f getPoint(int num) {
    return (Vec2f) m_points.get(num);
  }

  public void setGroupIndex(int groupIndex) {
    this.m_groupIndex = groupIndex;
  }

  public void setGroupName(String groupName) {
    this.m_groupName = groupName;
  }

  public boolean xmlLoad(Document document, Element root) {
    int id;
    String name;
    id = Integer.parseInt(root.getAttribute("groupIndex"));
    name = root.getAttribute("groupName");

    List<Vec2f> points = new ArrayList<Vec2f>();
    NodeList pointNodes = root.getElementsByTagName("point");
    for (int i = 0; i < pointNodes.getLength(); ++i) {
      Element pointElem = (Element) pointNodes.item(i);
      Vec2f p = new Vec2f();
      p.x = Float.parseFloat(pointElem.getAttribute("x"));
      p.y = Float.parseFloat(pointElem.getAttribute("y"));
      points.add(p);
    }

    setGroupIndex(id);
    setGroupName(name);
    m_points = points;
    return true;
  }

  public Element xmlSave(Document document) {
    Element root = document.createElement("path");
    root.setAttribute("groupIndex", Integer.toString(getGroupIndex()));
    root.setAttribute("groupName", getGroupName());

    for (int i = 0; i < m_points.size(); ++i) {
      Vec2f p = (Vec2f) m_points.get(i);
      Element pointElem = document.createElement("point");
      root.appendChild(pointElem);
      pointElem.setAttribute("x", Float.toString(p.x));
      pointElem.setAttribute("y", Float.toString(p.y));
    }

    return root;
  }
}