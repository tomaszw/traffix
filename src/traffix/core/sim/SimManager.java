/*
 * Created on 2004-07-22
 */

package traffix.core.sim;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import org.eclipse.swt.graphics.Rectangle;
import org.tw.geometry.Rectanglef;
import org.tw.geometry.Vec2f;
import org.tw.patterns.observer.IUpdateListener;
import org.tw.patterns.observer.IUpdateable;
import org.tw.patterns.observer.UpdateableObj;
import org.tw.web.XmlKit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;

import traffix.Traffix;
import traffix.core.TerrainMap;
import traffix.core.Time;
import traffix.core.Weekdays;
import traffix.core.model.Model;
import traffix.core.schedule.Schedule;
import traffix.core.sim.entities.*;
import traffix.core.sim.generation.*;
import traffix.core.sim.graph.Graph;
import traffix.core.sim.graph.Node;
import traffix.ui.BigImage;
import traffix.ui.sim.SimMode;
import traffix.ui.sim.entities.*;

public class SimManager implements ISimManager {
  private static final IGenerationModel s_groupGenerationModel    = new GroupGenerationModel();
  private static final IGenerationModel s_standardGenerationModel = new StandardGenerationModel();
  private static final IGenerationModel s_accidentGenerationModel = new AccidentGenerationModel();
  private static final ITrafficModel    s_standardTrafficModel    = new StandardTrafficModel();
  private static final ITrafficModel    s_accidentTrafficModel    = new AccidentTrafficModel();

  private Databanks                     m_databanks               = new Databanks();
  private EntityManager                 m_entityManager           = new EntityManager();
  private Rectanglef                    m_filmRc;
  private float                         m_fps                     = 0;
  private int                           m_generationModel         = IGenerationModel.STANDARD;
  private Graph                         m_graph                   = new Graph();
  private List<IMobile>                 m_mobiles                 = new ArrayList<IMobile>();
  private SimMode                       m_mode;
  private Model                         m_model;
  private ScheduleManager               m_scheduleManager;
  private SimParams                     m_simParams               = new SimParams();
  private float                         m_speedFactor             = 1;
  private Map                           m_startNodeCache          = new HashMap();
  private TerrainMap                    m_terrainMap;
  // private float m_time = 0;
  private IUpdateable                   m_updateable              = new UpdateableObj();

  private List<IUpdateListener>         m_updateListeners         = new ArrayList<IUpdateListener>();
  private float                         m_zoom                    = 1;
  private String                        m_name;
  private List<IUiEntity>               m_uiEntities;
  private IUiEntityContextProvider      m_uiCp                    = null;
  private Map<IMobile, MobileUi>        m_mobileUis               = new HashMap<IMobile, MobileUi>();

  public SimManager(Model model, SimMode mode) {
    m_model = model;
    m_mode = mode;

    m_graph.addUpdateListener(new IUpdateListener() {
      public void onUpdate(int hint, Object data) {
        onGraphChange();
      }
    });

    m_model.addUpdateListener(new IUpdateListener(){
    
      public void onUpdate(int hint, Object data) {
        onModelChange();
      }
    
    });
    
    if (mode == SimMode.Default)
      m_scheduleManager = new ScheduleManager();
    else
      m_scheduleManager = null;
  }

  private void onModelChange() {
//    if (m_uiCp != null) {
//      disposeUiEntities();
//      m_uiEntities = createUiEntities(m_uiCp);
//    }
  }
  
  private void internalReset() {
    m_graph = new Graph();
    m_graph.addUpdateListener(new IUpdateListener() {
      public void onUpdate(int hint, Object data) {
        onGraphChange();
      }
    });
    if (m_mode == SimMode.Default) {
      Schedule act = m_scheduleManager.getActiveSchedule();
      m_scheduleManager = new ScheduleManager();
      m_scheduleManager.setActiveSchedule(act);
    }
    else
      m_scheduleManager = null;
    m_entityManager = new EntityManager();

    disposeUiEntities();
    m_databanks.clear();
    m_simParams.duration = 3600;
    m_terrainMap = m_model.getTerrainMap();
    getGenerationModel().reset();
    scheduleManager().reset();
    m_speedFactor = 1;
    m_mobiles.clear();
    m_startNodeCache.clear();
    m_zoom = 1;
    rewindSimulation();
  }

  private void internalReset_old() {
    disposeUiEntities();
    scheduleManager().accMatrix().init();
    rewindSimulation();
    m_databanks.clear();
    m_simParams.duration = 0;
    m_terrainMap = m_model.getTerrainMap();
    getGenerationModel().reset();
    scheduleManager().reset();
    m_speedFactor = 1;
    m_graph = new Graph();
    m_graph.addUpdateListener(new IUpdateListener() {
      public void onUpdate(int hint, Object data) {
        onGraphChange();
      }
    });
    m_entityManager.clear();
    m_mobiles.clear();
    m_startNodeCache.clear();
    m_zoom = 1;
  }

  public void setName(String name) {
    m_name = name;
  }

  public void addMobile(IMobile m) {
    if (m_mobiles.size() >= MAX_MOBILES)
      return;
    if (m.getRoute() == null)
      return;
    m.init();
    m_mobiles.add(m);
    addMobileUi(m);
    // update(OTHER, null);
  }

  private void addMobileUi(IMobile m) {
    if (m_mobileUis.containsKey(m))
      return;

    MobileUi ui = UiEntityFactory.createVehicleUi(m);
    m_uiEntities.add(ui);
    m_mobileUis.put(m, ui);
  }

  private void removeMobileUi(IMobile m) {
    MobileUi ui = m_mobileUis.get(m);
    m_uiEntities.remove(ui);
  }

  public void addUpdateListener(IUpdateListener l) {
    m_updateable.addUpdateListener(l);
  }

  public void createPathsUsingMcmData() {

  }

  public List<IUiEntity> getUiEntities() {
    if (m_uiEntities == null)
      m_uiEntities = createUiEntities(m_uiCp);
    return m_uiEntities;
  }

  public void setEntityContextProvider(IUiEntityContextProvider provider) {
    //if (provider != m_uiCp) {
      m_uiCp = provider;
      disposeUiEntities();
      m_uiEntities = createUiEntities(provider);
    //}
  }

  private void disposeUiEntities() {
    if (m_uiEntities != null) {
      for (IUiEntity e : m_uiEntities)
        e.dispose();
      m_uiEntities.clear();
      m_uiEntities = null;
    }
    m_mobileUis.clear();
  }

  // Don't forget to dispose created entities!
  private List<IUiEntity> createUiEntities(IUiEntityContextProvider provider) {
    if (provider == null)
      throw new Error("No uientitycontextprovider");
    List<IUiEntity> entities = new ArrayList<IUiEntity>();
    UiEntityFactory.setUiEntityContextProvider(provider);
    GraphUi gui = UiEntityFactory.createGraphUi(this.getGraph());
    entities.add(gui);

    if (m_simParams.showNodeLabels)
      entities.addAll(UiEntityFactory.createNodeLabelUis(this.getGraph()));

    entities.addAll(entityManager().createUiEntities(provider));

    for (Iterator<IMobile> iter = getMobileIterator(); iter.hasNext();) {
      IMobile veh = iter.next();
      MobileUi ui = UiEntityFactory.createVehicleUi(veh);
      entities.add(ui);
    }

    return entities;
  }

  public Databanks databanks() {
    return m_databanks;
  }

  public void dispose() {
    if (m_terrainMap != null)
      m_terrainMap.dispose();
    disposeUiEntities();
  }

  public EntityManager entityManager() {
    return m_entityManager;
  }

  public Schedule getActiveSchedule() {
    return scheduleManager().getActiveSchedule();
  }

  public ITrafficModel getActiveTrafficModel() {
    return m_mode == SimMode.Accidents ? s_accidentTrafficModel : s_standardTrafficModel;
  }

  public float getCurrentTime() {
    return scheduleManager().getAbsoluteTime();
  }

  public List<IDetector> getDetectors() {
    LinkedList<IDetector> det = new LinkedList<IDetector>();
    for (IEntity e : m_entityManager) {
      if (e instanceof IDetector) {
        det.add((IDetector) e);
      }
    }
    return det;
  }

  public Rectanglef getFilmRectangle() {
    return m_filmRc;
  }

  public IGenerationModel getGenerationModel() {
    if (m_mode == SimMode.Accidents) {
      return s_accidentGenerationModel;
    }

    if (m_generationModel == IGenerationModel.STANDARD)
      return s_standardGenerationModel;
    return s_groupGenerationModel;
  }

  public Graph getGraph() {
    return m_graph;
  }

  public Iterator<IMobile> getMobileIterator() {
    return m_mobiles.iterator();
  }

  public String getName() {
    return m_name;
  }

  public IDetector getNamedDetector(String name) {
    return m_entityManager.getNamedDetector(name);
  }

  public Set<String> getRouteNames() {
    Set<String> names = new HashSet<String>();
    for (Route r : getRoutes())
      names.add(r.getInfo().name);
    return names;
  }

  public List<Route> getRoutes() {
    List<Route> routes = new ArrayList<Route>();
    for (int i = 0; i < m_graph.getNumNodes(); ++i) {
      Node n = m_graph.getNode(i);
      if (n.isBeginningNode()) {
        routes.addAll(n.getRoutesFromNode());
      }
    }
    return routes;
  }

  public List<Route> getRoutesFromNode(Node n) {
    if (!n.isBeginningNode())
      throw new IllegalArgumentException();
    return n.getRoutesFromNode();
  }

  public String getSimulationMessage() {
    int maxSchedNameLen = 0;
    for (Schedule s : m_model.getScheduleBank())
      maxSchedNameLen = Math.max(maxSchedNameLen, s.getName().length());

    NumberFormat fmt = new DecimalFormat("00");
    NumberFormat fmt3 = new DecimalFormat("000");
    NumberFormat fpsfmt = new DecimalFormat("0.0");
    Time t = new Time().addSecs((int) getCurrentTime());
    String day = Weekdays.names[t.weekday];
    String schname = "";
    if (getActiveSchedule() != null)
      schname = getActiveSchedule().getName();
    while (schname.length() < maxSchedNameLen)
      schname = " " + schname;
    String msg = fmt.format(t.h) + ":" + fmt.format(t.m) + ":" + fmt.format(t.s);

    msg += " " + schname + "-" + fmt3.format(scheduleManager().getScheduleTime() + 1);
    msg += " Sdt=";
    if (scheduleManager().isScheduleFreezed()) {
      fmt = new DecimalFormat("00.0");
      msg += fmt.format(scheduleManager().getFreezeDuration()) + "s";
    } else {
      msg += "     ";
    }
    msg += " [x" + fpsfmt.format(m_speedFactor) + "]";

    msg += " [" + m_mobiles.size() + " + "
        + fmt3.format(getGenerationModel().getNumQueued()) + " pojazdów]";
    return msg;
  }

  public float getSpeedFactor() {
    return m_speedFactor;
  }

  public Vec2f getTerrainDims() {
    if (m_terrainMap == null)
      return m_model.getTerrainMap().getTerrainDims();
    return m_terrainMap.getTerrainDims();
  }

  public Rectangle getTerrainDimsZoomedAndOnScreen() {
    BigImage img = getTerrainImage();
    if (img != null) {
      Rectangle bounds = getTerrainImage().getBounds();
      bounds.width = (int) (bounds.width * m_zoom);
      bounds.height = (int) (bounds.height * m_zoom);
      return bounds;
    } else {
      Rectangle bounds = new Rectangle(0, 0, 100, 100);
      return bounds;
    }
  }

  public BigImage getTerrainImage() {
    if (m_terrainMap == null)
      return m_model.getTerrainMap().getFullImage();
    return m_terrainMap.getFullImage();
  }

  public String getXmlTagName() {
    return "simulation";
  }

  public float getZoom() {
    return m_zoom;
  }

  public void importImage(String path) {
    Vec2f dims = getTerrainDims();
    m_terrainMap = new TerrainMap();
    m_terrainMap.defineMap(path, dims.x, dims.y);
    m_model.setTerrainMap(m_terrainMap);
    fireUpdated();
  }

  public Iterable<IMobile> mobiles() {
    return m_mobiles;
  }

  public void removeUpdateListener(IUpdateListener l) {
    m_updateable.removeUpdateListener(l);
  }

  public void rescaleMap(float w, float h) {
    if (m_terrainMap == m_model.getTerrainMap()) {
      m_terrainMap = new TerrainMap();
      m_terrainMap.defineMap(m_model.getTerrainMap().getImageFilename(), w, h);
    } else {
      m_terrainMap.setTerrainDims(w, h);
    }
    fireUpdated();// CHANGED_ZOOM, null);
    m_graph.update();
  }

  public void reset() {
    internalReset();
    fireUpdated();
  }

  public void rewindAndGenerateSimulation() {
    rewindSimulation();
    getGenerationModel().reset();
  }

  public void rewindSimulation() {
    // Setup mobile arrivals
    getGenerationModel().rewind();
    // Schedule back to normal
    scheduleManager().reset();
    // Mobiles from crossing weg
    for (IMobile m : m_mobiles) {
      m.dispose();
    }
    m_mobiles.clear();

    disposeUiEntities();

    // Ui entities back to standard state
    m_entityManager.resetEntities();
    // Reset route statistics
    for (Route r : getRoutes()) {
      r.getInfo().resetStatistics();
    }
    // Reset schedule statistics
    for (Schedule s : Traffix.scheduleBank().getSchedules()) {
      s.numSimCycles = 0;
    }
  }

  public ScheduleManager scheduleManager() {
    return m_mode == SimMode.Default ? m_scheduleManager : m_model.getMainSimManager()
        .scheduleManager();
  }

  public void setFilmRectangle(Rectanglef rc) {
    Vec2f mapDims = getTerrainDims();
    m_filmRc = rc;
    m_filmRc.width = Math.min(mapDims.x - rc.x, m_filmRc.width);
    m_filmRc.height = Math.min(rc.y, m_filmRc.height);
  }

  public void setFps(float fps) {
    this.m_fps = fps;
  }

  public void setGenerationModel(int generationModel) {
    m_generationModel = generationModel;
  }

  public void setSpeedFactor(float speedFactor) {
    m_speedFactor = speedFactor;
  }

  public void setZoom(float zoom) {
    m_zoom = zoom;
    fireUpdated(MSG_CHANGED_ZOOM, null);
  }

  public SimParams simParams() {
    return m_simParams;
  }

  public void tick(float delta) {
    // while (delta > MAX_SIMULATION_STEP) {
    // tick(MAX_SIMULATION_STEP);
    // delta -= MAX_SIMULATION_STEP;
    // }
    if (delta > MAX_SIMULATION_STEP)
      delta = MAX_SIMULATION_STEP;
    // int currentSec = (int) (m_time + delta / 2);

    // tick entities
    m_entityManager.tick(getCurrentTime(), delta);

    // generate vehicles
    getGenerationModel().update(getCurrentTime(), delta);

    // move && disintegrate vehicles
    for (Iterator<IMobile> iter = m_mobiles.iterator(); iter.hasNext();) {
      IMobile mobile = iter.next();
      mobile.tick(getCurrentTime(), delta);
      if (mobile.shouldDisintegrate()) {
        // disintegrate!
        // if (mobile.countsAsSingle()) {
        RouteInfo info = mobile.getRoute().getInfo();
        info.numPassedVirtualVehicles += mobile.getVirtualVehiclesWeight();
        ++info.numPassedVehicles[mobile.getType()];
        info.summedTravelTime += getCurrentTime() - mobile.getArrivalTime();
        // }
        iter.remove();
        removeMobileUi(mobile);
        mobile.dispose();
      }
    }

    // update schedule manager
    scheduleManager().tick(delta);
  }

  public void fireUpdated() {
    if (m_terrainMap == null)
      m_terrainMap = m_model.getTerrainMap();

    fireUpdated(MSG_OTHER, null);
  }

  public boolean xmlLoad(Document document, Element root) {
    internalReset();

    if (root.hasAttribute("speedFactor"))
      m_speedFactor = Float.parseFloat(root.getAttribute("speedFactor"));
    if (root.hasAttribute("autostop"))
      m_simParams.duration = Integer.parseInt(root.getAttribute("autostop"));
    if (root.hasAttribute("betterColls"))
      m_simParams.betterCollisionDetection = root.getAttribute("betterColls").equals("1");
    if (root.hasAttribute("barriersActive"))
      m_simParams.barriersActive = root.getAttribute("barriersActive").equals("1");
    if (root.hasAttribute("nodeLabels"))
      m_simParams.showNodeLabels = root.getAttribute("nodeLabels").equals("1");
    if (root.hasAttribute("nlNum"))
      m_simParams.showNodeNumbers = root.getAttribute("nlNum").equals("1");
    if (root.hasAttribute("nlVirtual"))
      m_simParams.showVirtualVehicles = root.getAttribute("nlVirtual").equals("1");
    if (root.hasAttribute("nlStoppedTime"))
      m_simParams.showAvgStopTime = root.getAttribute("nlStoppedTime").equals("1");
    if (root.hasAttribute("showNumWaiting"))
      m_simParams.showNumWaiting = root.getAttribute("showNumWaiting").equals("1");
    if (root.hasAttribute("zoom"))
      m_zoom = Float.parseFloat(root.getAttribute("zoom"));

    if (root.hasAttribute("generationModel"))
      m_generationModel = Integer.parseInt(root.getAttribute("generationModel"));
    Element mapElem = XmlKit.firstChild(root, "simulationMap");
    if (mapElem != null) {
      String bmap = mapElem.getAttribute("bitmap");
      float w = Float.parseFloat(mapElem.getAttribute("w"));
      float h = Float.parseFloat(mapElem.getAttribute("h"));
      m_terrainMap = new TerrainMap();
      m_terrainMap.defineMap(bmap, w, h);
    }

    Element graphElem = XmlKit.firstChild(root, "graph");
    if (graphElem == null)
      return false;
    if (!m_graph.xmlLoad(document, graphElem))
      return false;

    Element entityManagerElem = XmlKit.firstChild(root, m_entityManager.getXmlTagName());
    if (entityManagerElem != null) {
      if (!m_entityManager.xmlLoad(document, entityManagerElem))
        return false;
    }

    Element scheduleMgrElem = XmlKit.firstChild(root, scheduleManager().getXmlTagName());
    if (scheduleMgrElem != null && !scheduleManager().xmlLoad(document, scheduleMgrElem))
      return false;

    Element databanksElem = XmlKit.firstChild(root, m_databanks.getXmlTagName());
    if (databanksElem != null) {
      if (!m_databanks.xmlLoad(document, databanksElem))
        return false;
    }

    getGenerationModel().reset();
    m_graph.update();

    return true;
  }

  public Element xmlSave(Document document) {
    Element root = document.createElement("simulation");

    root.setAttribute("speedFactor", Float.toString(m_speedFactor));
    root.setAttribute("autostop", Integer.toString((int) m_simParams.duration));
    root.setAttribute("barriersActive", m_simParams.barriersActive ? "1" : "0");
    root.setAttribute("nodeLabels", m_simParams.showNodeLabels ? "1" : "0");
    root.setAttribute("nlNum", m_simParams.showNodeNumbers ? "1" : "0");
    root.setAttribute("nlVirtual", m_simParams.showVirtualVehicles ? "1" : "0");
    root.setAttribute("nlStopped", m_simParams.showAvgStopTime ? "1" : "0");
    root.setAttribute("showNumWaiting", m_simParams.showNumWaiting ? "1" : "0");
    root.setAttribute("generationModel", Integer.toString(m_generationModel));
    root.setAttribute("zoom", Float.toString(m_zoom));

    if (m_terrainMap != null) {
      Element map = document.createElement("simulationMap");
      map.setAttribute("bitmap", m_terrainMap.getImageFilename());
      map.setAttribute("w", Float.toString(m_terrainMap.getWidth()));
      map.setAttribute("h", Float.toString(m_terrainMap.getHeight()));
      root.appendChild(map);
    }

    root.appendChild(m_graph.xmlSave(document));
    root.appendChild(m_entityManager.xmlSave(document));
    root.appendChild(scheduleManager().xmlSave(document));
    root.appendChild(m_databanks.xmlSave(document));

    return root;
  }

  private void onGraphChange() {
    m_entityManager.update();
  }

  private void removeVehicle(Mobile v) {
    m_mobiles.remove(v);
    v.dispose();
    removeMobileUi(v);
  }

  public void fireUpdated(int hint, Object data) {
    m_updateable.fireUpdated(hint, data);
  }

  // public void fireUpdated(int hint, Object data) {
  // m_updateable.fireUpdated(hint, data);
  // }
}