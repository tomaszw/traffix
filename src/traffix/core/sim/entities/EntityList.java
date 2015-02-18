/*
 * Created on 2004-09-01
 */

package traffix.core.sim.entities;

import org.tw.web.XmlKit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import traffix.core.model.IPersistent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class EntityList implements IPersistent, Iterable<IEntity> {
  private List<IEntity> m_entities = new ArrayList<IEntity>();
  private Class m_entityType;
  private String m_xmlElemName;

  public EntityList(String xmlElemName, Class entityType) {
    m_xmlElemName = xmlElemName;
    m_entityType = entityType;
  }

  public void add(IEntity e) {
    m_entities.add(e);
  }

  public Collection<IEntity> asCollection() {
    return m_entities;
  }

  public void clear() {
    m_entities.clear();
  }

  public boolean contains(IEntity e) {
    return m_entities.contains(e);
  }

  public IEntity get(int i) {
    return m_entities.get(i);
  }

  public String getXmlTagName() {
    return m_xmlElemName;
  }

  public int indexOf(IEntity e) {
    return m_entities.indexOf(e);
  }

  public Iterator<IEntity> iterator() {
    return m_entities.iterator();
  }

  public void remove(IEntity e) {
    m_entities.remove(e);
    e.dispose();
  }

  public int size() {
    return m_entities.size();
  }

  public void tick(float t0, float delta) {
    for (Iterator<IEntity> iter = m_entities.iterator(); iter.hasNext();) {
      IEntity e = iter.next();
      e.tick(t0, delta);
    }
  }

  public void update() {
    for (Iterator<IEntity> iter = m_entities.iterator(); iter.hasNext();) {
      IEntity e = iter.next();
      e.update();
    }
  }

  public boolean xmlLoad(Document document, Element root) {
    m_entities.clear();

    String tagName = "";
    try {
      IEntity sample = (IEntity) m_entityType.newInstance();
      tagName = sample.getXmlTagName();
    } catch (InstantiationException e) {
      e.printStackTrace();
      return false;
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      return false;
    }

    Element[] elems = XmlKit.childElems(root, tagName);
    for (int i = 0; i < elems.length; i++) {
      try {
        IEntity e = (IEntity) m_entityType.newInstance();
        if (!e.xmlLoad(document, elems[i]))
          return false;
        m_entities.add(e);
      } catch (InstantiationException e) {
        e.printStackTrace();
        return false;
      } catch (IllegalAccessException e) {
        e.printStackTrace();
        return false;
      }

    }

    return true;
  }

  public Element xmlSave(Document document) {
    Element root = document.createElement(m_xmlElemName);
    for (Iterator<IEntity> iter = m_entities.iterator(); iter.hasNext();) {
      IEntity entity = iter.next();
      IPersistent pers = (IPersistent) entity;
      Element elem = pers.xmlSave(document);
      root.appendChild(elem);

    }
    return root;
  }

}