/*
 * Created on 2004-08-30
 */

package traffix.core;

import java.util.*;

import org.tw.web.XmlKit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import traffix.Traffix;
import traffix.core.model.IPersistent;
import traffix.core.model.Model;

public class VehicleGroupSet implements IPersistent, Cloneable, Iterable<VehicleGroup> {
  private List<Integer> m_groups = new ArrayList<Integer>();

  public void add(VehicleGroup g) {
    int id = g.getUniqueId();
    if (!m_groups.contains(id))
      m_groups.add(id);
  }

  public void addAll(VehicleGroupSet set) {
    for (Iterator<Integer> iter = set.m_groups.iterator(); iter.hasNext();) {
      Integer id = iter.next();
      if (!m_groups.contains(id))
        m_groups.add(id);
    }
  }

  public List<VehicleGroup> asList() {
    List<VehicleGroup> tempList = new LinkedList<VehicleGroup>();
    for (Iterator<Integer> iter = m_groups.iterator(); iter.hasNext();) {
      Integer id = iter.next();
      tempList.add(VehicleGroup.fromUniqueIdent(id.intValue()));
    }
    return Collections.unmodifiableList(tempList);
  }

  public void assign(VehicleGroupSet set) {
    clear();
    m_groups.addAll(set.m_groups);
  }

  public void clear() {
    m_groups.clear();
  }

  @Override
  public VehicleGroupSet clone() {
    try {
      VehicleGroupSet res = (VehicleGroupSet) super.clone();
      res.m_groups = new ArrayList<Integer>();
      res.m_groups.addAll(m_groups);
      return res;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      return null;
    }
  }

  public boolean contains(VehicleGroup g) {
    Integer id = new Integer(g.getUniqueId());
    return m_groups.contains(id);
  }

  public VehicleGroup get(int i) {
    return VehicleGroup.fromUniqueIdent((m_groups.get(i)).intValue());
  }

  public String getXmlTagName() {
    return "groupSet";
  }

  public Iterator<VehicleGroup> iterator() {
    List<VehicleGroup> tempList = asList();
    return tempList.iterator();
  }

  public VehicleGroupSet negate() {
    VehicleGroupSet set = new VehicleGroupSet();
    Model m = Traffix.model();
    for (int i = 0; i < m.getNumGroups(); ++i) {
      VehicleGroup g = m.getGroupByIndex(i);
      if (!contains(g))
        set.add(g);
    }
    return set;
  }

  public void remove(VehicleGroup g) {
    m_groups.remove(new Integer(g.getUniqueId()));
  }

  public int size() {
    return m_groups.size();
  }

  public VehicleGroup[] toArray() {
    List<VehicleGroup> list = asList();
    return list.toArray(new VehicleGroup[list.size()]);
  }

  public boolean xmlLoad(Document document, Element root) {
    m_groups.clear();
    try {
      Element[] elems = XmlKit.childElems(root, "g");
      for (int i = 0; i < elems.length; i++) {
        int id = Integer.parseInt(elems[i].getAttribute("id"));
        m_groups.add(id);
      }
    } catch (NumberFormatException e) {
      return false;
    }

    if (root.hasAttribute("negated") && root.getAttribute("negated").equals("1"))
      assign(negate());
    return true;
  }

  public Element xmlSave(Document document) {
    Element root = document.createElement("groupSet");
    if (m_groups.size() > Traffix.model().getNumGroups() / 2) {
      root.setAttribute("negated", "1");
      negate().doXmlSave(document, root);
    } else {
      root.setAttribute("negated", "0");
      doXmlSave(document, root);
    }
    return root;
  }

  private void doXmlSave(Document document, Element root) {
    for (Iterator<Integer> iter = m_groups.iterator(); iter.hasNext();) {
      Integer id = iter.next();
      Element elem = document.createElement("g");
      elem.setAttribute("id", id.toString());
      root.appendChild(elem);
    }
  }
}