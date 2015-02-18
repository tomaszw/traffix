/*
 * Created on 2004-09-03
 */

package traffix.core.sim.entities;

import java.util.*;

import org.tw.web.XmlKit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import traffix.Traffix;
import traffix.core.model.IPersistent;
import traffix.ui.sim.entities.*;

public class EntityManager implements IPersistent, Iterable<IEntity> {
  private EntityList             m_busStops            = new EntityList("busStops",
                                                           BusStop.class);
  private List<IEntity>          m_entityCache         = new ArrayList<IEntity>();
  private EntityList             m_lights              = new EntityList("lights",
                                                           Light.class);
  private EntityList             m_pedestrianPaths     = new EntityList(
                                                           "pedestrianPaths",
                                                           PedestrianPath.class);
  private EntityList             m_presenceDetectors   = new EntityList(
                                                           "presenceDetectors",
                                                           PresenceDetector.class);
  private EntityList             m_transitDetectors    = new EntityList(
                                                           "transitDetectors",
                                                           TransitDetector.class);
  private EntityList             m_pedestrianDetectors = new EntityList(
                                                           "pedestrianDetectors",
                                                           PedestrianDetector.class);
  private EntityList             m_barrierDetectors    = new EntityList(
                                                           "barrierDetectors",
                                                           BarrierDetector.class);
  private EntityList             m_condClearDetectors    = new EntityList(
      "condClearDetectors",
      CondClearDetector.class);
  private EntityList             m_slowdownDetectors   = new EntityList(
                                                           "slowdownDetectors",
                                                           SlowdownDetector.class);

  private EntityList             m_barriers            = new EntityList("barriers",
                                                           Barrier.class);

  private Map<Class, EntityList> m_containerMap        = new HashMap<Class, EntityList>();
  private Map<String, IDetector> m_detectorMap         = new HashMap<String, IDetector>();

  public EntityManager() {
    m_containerMap.put(Light.class, m_lights);
    m_containerMap.put(BusStop.class, m_busStops);
    m_containerMap.put(PedestrianPath.class, m_pedestrianPaths);
    m_containerMap.put(TransitDetector.class, m_transitDetectors);
    m_containerMap.put(PresenceDetector.class, m_presenceDetectors);
    m_containerMap.put(PedestrianDetector.class, m_pedestrianDetectors);
    m_containerMap.put(BarrierDetector.class, m_barrierDetectors);
    m_containerMap.put(Barrier.class, m_barriers);
    m_containerMap.put(SlowdownDetector.class, m_slowdownDetectors);
    m_containerMap.put(CondClearDetector.class, m_condClearDetectors);
  }

  public BarrierDetector getBarrierDetector(int idx) {
    return (BarrierDetector) m_barrierDetectors.get(idx);
  }

  public int indexOf(IEntity e) {
    EntityList container = m_containerMap.get(e.getClass());
    if (container != null)
      return container.indexOf(e);
    return -1;
  }

  public void clear() {
    for (EntityList l : m_containerMap.values()) {
      l.clear();
    }
    
    update();
    updateSimManager();
    Traffix.model().fireUpdated();
  }

  public List<IUiEntity> createUiEntities(IUiEntityContextProvider provider) {
    List<IUiEntity> entities = new ArrayList<IUiEntity>();
    UiEntityFactory.setUiEntityContextProvider(provider);

    for (Iterator<IEntity> iter = m_lights.iterator(); iter.hasNext();) {
      Light light = (Light) iter.next();
      LightUi ui = UiEntityFactory.createLightUi(light);
      entities.add(ui);
    }

    for (Iterator<IEntity> iter = m_pedestrianPaths.iterator(); iter.hasNext();) {
      PedestrianPath path = (PedestrianPath) iter.next();
      PedestrianPathUi ui = UiEntityFactory.createPedestrianPathUi(path);
      entities.add(ui);
    }

    for (Iterator<IEntity> iter = m_busStops.iterator(); iter.hasNext();) {
      BusStop stop = (BusStop) iter.next();
      BusStopUi ui = UiEntityFactory.createBusStopUi(stop);
      entities.add(ui);
    }

    for (Iterator<IEntity> iter = m_transitDetectors.iterator(); iter.hasNext();) {
      TransitDetector d = (TransitDetector) iter.next();
      TransitDetectorUi ui = UiEntityFactory.createTransitDetectorUi(d);
      entities.add(ui);

    }

    for (Iterator<IEntity> iter = m_presenceDetectors.iterator(); iter.hasNext();) {
      PresenceDetector d = (PresenceDetector) iter.next();
      PresenceDetectorUi ui = UiEntityFactory.createPresenceDetectorUi(d);
      entities.add(ui);
    }

    for (Iterator<IEntity> iter = m_pedestrianDetectors.iterator(); iter.hasNext();) {
      PedestrianDetector d = (PedestrianDetector) iter.next();
      PedestrianDetectorUi ui = UiEntityFactory.createPedestrianDetectorUi(d);
      entities.add(ui);
    }

    for (Iterator<IEntity> iter = m_condClearDetectors.iterator(); iter.hasNext();) {
      CondClearDetector d = (CondClearDetector) iter.next();
      CondClearDetectorUi ui = UiEntityFactory.createCondClearDetectorUi(d);
      entities.add(ui);
    }

    for (Iterator<IEntity> iter = m_barriers.iterator(); iter.hasNext();) {
      Barrier e = (Barrier) iter.next();
      BarrierUi ui = UiEntityFactory.createBarrierUi(e);
      entities.add(ui);
    }

    for (Iterator<IEntity> iter = m_barrierDetectors.iterator(); iter.hasNext();) {
      BarrierDetector e = (BarrierDetector) iter.next();
      BarrierDetectorUi ui = UiEntityFactory.createBarrierDetectorUi(e);
      entities.add(ui);
    }

    for (IEntity e : m_slowdownDetectors) {
      SlowdownDetectorUi ui = UiEntityFactory.createSlowdownDetectorUi((SlowdownDetector)e);
      entities.add(ui);
    }
    
    return entities;
  }

  public Iterator<IEntity> iterator() {
    return m_entityCache.iterator();
  }

  public void addStaticEntity(IEntity e) {
    EntityList container = m_containerMap.get(e.getClass());
    if (container == null)
      throw new IllegalArgumentException();
    container.add(e);
    e.update();
    update();
    updateSimManager();
    //Traffix.model().fireUpdated();
  }

  public String getXmlTagName() {
    return "entityManager";
  }

  public void removeStaticEntity(IEntity e) {
    EntityList container = m_containerMap.get(e.getClass());
    if (container == null)
      throw new IllegalArgumentException();
    container.remove(e);
    e.dispose();
    update();
    updateSimManager();
    //Traffix.model().fireUpdated();

  }

  public void resetEntities() {
    for (Iterator<IEntity> iter = m_entityCache.iterator(); iter.hasNext();) {
      IEntity e = iter.next();
      e.reset();
    }
    updateSimManager();
  }

  public void tick(float t0, float delta) {
    for (Iterator<IEntity> iter = m_entityCache.iterator(); iter.hasNext();) {
      IEntity e = iter.next();
      e.tick(t0, delta);
    }
  }

  public void update() {
    updateCache();
    updateEntities();
  }

  private void updateSimManager() {
    Traffix.simManager().fireUpdated();
  }

  public void updateEntities() {
    for (Iterator<IEntity> iter = m_entityCache.iterator(); iter.hasNext();) {
      IEntity e = iter.next();
      e.update();
    }
  }

  public IDetector getNamedDetector(String name) {
    return m_detectorMap.get(name);
  }

  public boolean xmlLoad(Document document, Element root) {
    if (!loadEntityList(document, root, m_lights))
      return false;
    if (!loadEntityList(document, root, m_pedestrianPaths))
      return false;
    if (!loadEntityList(document, root, m_busStops))
      return false;
    if (!loadEntityList(document, root, m_transitDetectors))
      return false;
    if (!loadEntityList(document, root, m_presenceDetectors))
      return false;
    if (!loadEntityList(document, root, m_pedestrianDetectors))
      return false;
    if (!loadEntityList(document, root, m_barrierDetectors))
      return false;
    if (!loadEntityList(document, root, m_barriers))
      return false;
    if (!loadEntityList(document, root, m_slowdownDetectors))
      return false;
    if (!loadEntityList(document, root, m_condClearDetectors))
      return false;
        
    updateCache();
    // fix transit detectors
    Iterator<IEntity> iter = m_transitDetectors.iterator();
    while (iter.hasNext()) {
      TransitDetector d = (TransitDetector) iter.next();
      d.postLoadFix();
    }
    // fix presence detectors
    for (IEntity e : m_presenceDetectors) {
      PresenceDetector d = (PresenceDetector) e;
      d.postLoadFix();
    }
    return true;
  }

  public Element xmlSave(Document document) {
    Element root = document.createElement(getXmlTagName());
    root.appendChild(m_lights.xmlSave(document));
    root.appendChild(m_pedestrianPaths.xmlSave(document));
    root.appendChild(m_busStops.xmlSave(document));
    root.appendChild(m_transitDetectors.xmlSave(document));
    root.appendChild(m_presenceDetectors.xmlSave(document));
    root.appendChild(m_pedestrianDetectors.xmlSave(document));
    root.appendChild(m_barrierDetectors.xmlSave(document));
    root.appendChild(m_barriers.xmlSave(document));
    root.appendChild(m_slowdownDetectors.xmlSave(document));
    root.appendChild(m_condClearDetectors.xmlSave(document));
    return root;
  }

  private boolean loadEntityList(Document doc, Element root, EntityList list) {
    Element elem = XmlKit.firstChild(root, list.getXmlTagName());
    if (elem != null) {
      if (!list.xmlLoad(doc, elem))
        return false;
    }
    return true;
  }

  private void updateCache() {
    m_entityCache.clear();
    for (EntityList l : m_containerMap.values()) {
      m_entityCache.addAll(l.asCollection());
    }
    
//    m_entityCache.addAll(m_lights.asCollection());
//    m_entityCache.addAll(m_pedestrianPaths.asCollection());
//    m_entityCache.addAll(m_presenceDetectors.asCollection());
//    m_entityCache.addAll(m_transitDetectors.asCollection());
//    m_entityCache.addAll(m_busStops.asCollection());
//    m_entityCache.addAll(m_pedestrianDetectors.asCollection());
//    m_entityCache.addAll(m_barriers.asCollection());
//    m_entityCache.addAll(m_barrierDetectors.asCollection());
    
    m_entityCache.addAll(Traffix.simManager().getGraph().getIntersections());

    // update detector map
    m_detectorMap.clear();
    for (IEntity e : m_presenceDetectors.asCollection()) {
      IDetector d = (IDetector) e;
      m_detectorMap.put(d.getName(), d);
    }

    for (IEntity e : m_transitDetectors.asCollection()) {
      IDetector d = (IDetector) e;
      m_detectorMap.put(d.getName(), d);
    }

    for (IEntity e : m_pedestrianDetectors.asCollection()) {
      IDetector d = (IDetector) e;
      m_detectorMap.put(d.getName(), d);
    }
    for (IEntity e : m_condClearDetectors.asCollection()) {
      IDetector d = (IDetector) e;
      m_detectorMap.put(d.getName(), d);
    }
  }
}