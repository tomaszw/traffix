/*
 * Created on 2004-06-27
 */

package traffix.core.model;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.tw.patterns.observer.IUpdateListener;
import org.tw.patterns.observer.IUpdateable;
import org.tw.patterns.observer.UpdateableObj;
import org.tw.persistence.xml.IStoreSession;
import org.tw.web.XmlKit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import traffix.Traffix;
import traffix.core.*;
import traffix.core.accident.AccidentModel;
import traffix.core.accident.IAccidentModel;
import traffix.core.schedule.*;
import traffix.core.sim.ISimManager;
import traffix.core.sim.SimManager;
import traffix.ui.ProgressDialog;
import traffix.ui.sim.SimMode;

public class Model extends AbstractModel implements IPersistent, IUpdateable {
  public static final int EVT_CHANGE_ACTIVE_SIMMAN = 6;
  public static final int EVT_CHANGE_GROUPS = 2;
  public static final int EVT_CHANGE_SCHEDULE = 1;
  public static final int EVT_CHANGE_SCHEDULEBANK = 4;
  public static final int EVT_CHANGE_TEMPSCHEDULE = 5;
  public static final int EVT_CHANGE_WEEKLYSCHEDULE = 3;

  private List<IAccidentModel> m_accidentModels;
  private List<ISimManager> m_accidentSimManagers;

  private ISimManager m_actSimManager;
  private Junction m_junctionData = new Junction();
  private ISimManager m_mainSimManager;
  private boolean m_operationStatus;
  private UpdateableObj m_updImpl = new UpdateableObj();

  static class EImport extends Exception {
    public EImport() {
      super("Wyst¹pi³ b³¹d podczas importu danych.");
    }
  }

  static class ELoad extends Exception {
    public ELoad() {
      super("B³¹d podczas odczytu danych,");
    }
  }

  public Model() {
    m_mainSimManager = new SimManager(this, SimMode.Default);
    m_actSimManager = m_mainSimManager;
    m_accidentSimManagers = new ArrayList<ISimManager>();
    m_accidentModels = new ArrayList<IAccidentModel>();
  }

  public void activateMainSimManager() {
    activateSimManager("SYMULACJA");
  }

  public void activateSimManager(String accname) {
    ISimManager prev = m_actSimManager;
    if (accname.equals("SYMULACJA"))
      m_actSimManager = m_mainSimManager;
    else {
      for (ISimManager m : m_accidentSimManagers)
        if (m.getName().equals(accname)) {
          m_actSimManager = m;
          break;
        }
    }

    if (m_actSimManager == prev)
      return;
    fireUpdated(Model.EVT_CHANGE_ACTIVE_SIMMAN, new Pair<ISimManager, ISimManager>(prev,
        m_actSimManager));
  }

  public void addAccident(String name) {
    ISimManager sm = new SimManager(this, SimMode.Accidents);
    sm.setName(name);
    m_accidentSimManagers.add(sm);

    IAccidentModel am = new AccidentModel();
    m_accidentModels.add(am);
  }

  public void addSchedule(Schedule sch) {
    getJunction().getScheduleBank().add(sch);
    setModified(true);
    fireUpdated(Model.EVT_CHANGE_SCHEDULEBANK, null);
  }

  public void addUpdateListener(IUpdateListener l) {
    m_updImpl.addUpdateListener(l);
  }

  public void addWeeklyScheduleEntry(WeeklyScheduleEntry e) {
    getJunction().getWeeklySchedule().addEntry(e);
    setModified(true);
    fireUpdated(Model.EVT_CHANGE_WEEKLYSCHEDULE, null);
  }

  public boolean areGroupsColliding(int i, int j) {
    if (getJunction().getMcmMatrix() == null)
      return false;
    return getJunction().getMcmMatrix().areColliding(i, j);
  }

  public boolean close() {
    boolean r = super.close();
    if (r) {
      reset();
      fireUpdated();
    }
    return r;
  }

  public void create() {
    super.create();
    fireUpdated();
  }

  public void fireUpdated() {
    m_updImpl.fireUpdated(NO_HINT, null);
  }

  public void fireUpdated(int hint, Object data) {
    m_updImpl.fireUpdated(hint, data);
  }

  public IAccidentModel getAccident(String name) {
    int idx = -1;
    int i = 0;
    for (ISimManager sm : m_accidentSimManagers) {
      if (sm.getName() != null && sm.getName().equals(name)) {
        idx = i;
        break;
      }
      ++i;
    }
    return idx != -1 ? m_accidentModels.get(idx) : null;
  }

  public IAccidentModel getActiveAccident() {
    int idx = m_accidentSimManagers.indexOf(getActiveSimManager());
    if (idx != -1)
      return m_accidentModels.get(idx);
    return null;
  }

  public ISimManager getActiveSimManager() {
    return m_actSimManager;
  }

  public JunctionDescription getDescription() {
    return getJunction().getDescription();
  }

  public VehicleGroup getGroupById(int id) {
    for (int i = 0; i < getJunction().getVehicleGroups().size(); ++i) {
      VehicleGroup g = getJunction().getVehicleGroups().get(i);
      if (g.getUniqueId() == id)
        return g;
    }
    return null;
  }

  public VehicleGroup getGroupByIndex(int i) {
    return getJunction().getVehicleGroups().get(i);
  }

  public int getGroupIndex(VehicleGroup g) {
    return getJunction().getVehicleGroups().indexOf(g);
  }

  public Iterator<VehicleGroup> getGroupIterator() {
    return getJunction().getVehicleGroups().iterator();
  }

  public VehicleGroupSet getGroups() {
    VehicleGroupSet set = new VehicleGroupSet();
    for (int i = 0; i < getNumGroups(); ++i)
      set.add(getGroupByIndex(i));
    return set;
  }

  public ISimManager getMainSimManager() {
    return m_mainSimManager;
  }

  public McmMatrix getMcmMatrix() {
    return getJunction().getMcmMatrix();
  }

  public int getMcmTime(int i, int j) {
    return getJunction().getMcmMatrix().getMcmTime(i, j);
  }

  public int getNumGroups() {
    return getJunction().getVehicleGroups().size();
  }

  public Schedule getSchedule(String name) {
    return getJunction().getScheduleBank().get(name);
  }

  public ScheduleBank getScheduleBank() {
    return getJunction().getScheduleBank();
  }

  public int getScheduleIndex(Schedule schedule) {
    return getJunction().getScheduleBank().getScheduleIndex(schedule);
  }

  public ISimManager getSimManagerFor(IAccidentModel m) {
    int idx = m_accidentModels.indexOf(m);
    if (idx == -1)
      return null;
    return m_accidentSimManagers.get(idx);
  }

  public List<String> getSimManagerNames() {
    List<String> names = new ArrayList<String>();
    names.add("SYMULACJA");
    for (ISimManager m : m_accidentSimManagers)
      if (m.getName() != null)
        names.add(m.getName());
    return names;
  }

  public TerrainMap getTerrainMap() {
    return getJunction().getTerrainMap();
  }

  public WeeklySchedule getWeeklySchedule() {
    return getJunction().getWeeklySchedule();
  }

  public int getWeeklyScheduleStartDay() {
    return getJunction().getWeeklySchedule().getStartDay();
  }

  public String getXmlTagName() {
    return "traffix";
  }

  public void importMcmData() {
    FileDialog dlg = new FileDialog(Traffix.shell(), SWT.OPEN);
    String[] exts = { "*.mxp", "*.*" };
    dlg.setFilterExtensions(exts);
    String filename = dlg.open();
    if (filename == null)
      return;

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(new File(filename));
      Element root = doc.getDocumentElement();

      // import description
      JunctionDescription desc = new JunctionDescription();
      Element descElem = ((Element) root.getElementsByTagName("description").item(0));
      if (descElem != null) {
        desc.city = descElem.getAttribute("city");
        desc.controllerType = descElem.getAttribute("controllerType");
        desc.crossingNum = descElem.getAttribute("crossingNum");
        desc.date = descElem.getAttribute("date");
        desc.note = descElem.getAttribute("note");
        desc.projectNum = descElem.getAttribute("projectNum");
        desc.streets = descElem.getAttribute("streets");
      }

      // import groups
      Vector<VehicleGroup> groups = new Vector<VehicleGroup>();
      Element groupRootNode = (Element) root.getElementsByTagName("groups").item(0);
      if (groupRootNode == null)
        throw new EImport();
      NodeList groupNodes = groupRootNode.getElementsByTagName("group");
      for (int i = 0; i < groupNodes.getLength(); ++i)
        groups.add(importMcmGroup((Element) groupNodes.item(i)));
      // import mcm matrix
      Element mcmMatrixNode = (Element) root.getElementsByTagName("mcm-matrix").item(0);
      McmMatrix mcmMatrix = McmMatrix.importFromMcmXml(mcmMatrixNode);
      if (mcmMatrix == null)
        throw new EImport();

      TerrainMap terrainMap = TerrainMap.importFromMcmXml(root);

      // if current doc is empty, just import
      if (getJunction().getVehicleGroups().size() == 0) {
        reset();

        getJunction().setTerrainMap(terrainMap);
        getJunction().setVehicleGroups(groups);
        getJunction().setDescription(desc);
        getJunction().setMcmMatrix(mcmMatrix);
        // add yellow pulsing program
        getJunction().getScheduleBank().add(
            Schedule.createPulsingYellow(getJunction().getVehicleGroups().size()));
        // add y-schedule
        getJunction().getScheduleBank().add(
            Schedule.createYSchedule(getJunction().getVehicleGroups().size()));

        setModified(true);
      } else {
        // try to import modified data
        importModifiedMcmData(groups, mcmMatrix, desc, terrainMap);
      }
    } catch (ParserConfigurationException e) {
      Traffix.reportException(e);
    } catch (SAXException e) {
      Traffix.reportException(e);
    } catch (IOException e) {
      Traffix.reportException(e);
    } catch (EImport e) {
      Traffix.reportException(e);
    }

    fireUpdated();
  }

  public boolean isEmpty() {
    return getJunction().getVehicleGroups().size() == 0;
  }

  public void mergeProjects() {
    FileDialog dlg = new FileDialog(Traffix.shell(), SWT.OPEN);
    String[] exts = { "*.tfx", "*.*" };
    dlg.setFilterExtensions(exts);
    dlg.setText("Wybierz projekt do do³¹czenia...");
    String filename = dlg.open();
    if (filename == null)
      return;
    try {
      Document doc = XmlKit.loadDoc(filename);
      Junction data = new Junction();
      data.loadFromXmlElement(doc, doc.getDocumentElement());
      Junction merged = getJunction().merge(data);
      setCurrentProject(merged);
      setModified(true);
      setFileName("S-" + getFileName());
      for (Schedule s : getJunction().getScheduleBank())
        s.updateCapacityData();
      Traffix.simScheduleManager().accMatrix().updateSchedules();
      fireUpdated();

    } finally {}
  }

  public void moveProjectGroupsDown(int pnum) {
    int beg = 0;
    for (int i = getNumGroups() - 1; i >= 0; --i) {
      int next = (i + 1) % getNumGroups();
      if (getGroupByIndex(i).getJunctionIndex() == pnum
          && getGroupByIndex(next).getJunctionIndex() != pnum) {
        beg = next;
        break;
      }
    }

    int i = beg;
    do {

      int next = (i - 1 + getNumGroups()) % getNumGroups();
      if (next == beg)
        break;
      if (getGroupByIndex(next).getJunctionIndex() == pnum)
        swapGroups(next, i);
      else
        break;
      i = next;
    } while (i != beg);
    fireUpdated();
  }

  public void moveProjectGroupsUp(int pnum) {
    int beg = 0;
    for (int i = 0; i < getNumGroups(); ++i) {
      int next = (i + 1) % getNumGroups();
      if (getGroupByIndex(i).getJunctionIndex() != pnum
          && getGroupByIndex(next).getJunctionIndex() == pnum) {
        beg = i;
        break;
      }
    }

    int i = beg;
    do {
      int next = (i + 1) % getNumGroups();
      if (next == beg)
        break;
      if (getGroupByIndex(next).getJunctionIndex() == pnum)
        swapGroups(next, i);
      else
        break;
      i = next;
    } while (i != beg);
    fireUpdated();
  }

  public void open() {
    super.open();
    fireUpdated();
  }

  public boolean readContents(final String filename) {
    m_operationStatus = false;
    final ProgressDialog dlg = new ProgressDialog(Traffix.shell());
    IRunnableWithProgress runnable = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException,
          InterruptedException {
        // m_loadMonitor = monitor;
        monitor.beginTask("Otwieranie dokumentu", IProgressMonitor.UNKNOWN);
        try {
          Document document = XmlKit.loadDoc(filename);
          // monitor.worked(50);
          if (document != null) {
            Element root = document.getDocumentElement();
            if (!xmlLoad(document, root))
              throw new InvocationTargetException(new ELoad());
            // monitor.worked(50);
            setModified(false);
            m_operationStatus = true;
            return;
          }
        } finally {
          monitor.done();
        }
        m_operationStatus = false;
      }
    };
    try {
      dlg.run(true, false, runnable);
    } catch (InvocationTargetException e) {
      Traffix.reportException(e.getCause());
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    fireUpdated();
    return m_operationStatus;
  }

  public void removeSchedule(String name) {
    if (getJunction().getScheduleBank().contains(name)) {
      getJunction().getScheduleBank().remove(name);
      setModified(true);
      fireUpdated(Model.EVT_CHANGE_SCHEDULEBANK, null);
    }
  }

  public void removeUpdateListener(IUpdateListener l) {
    m_updImpl.removeUpdateListener(l);
  }

  public void removeWeeklyScheduleEntry(WeeklyScheduleEntry e) {
    getJunction().getWeeklySchedule().removeEntry(e);
    setModified(true);
    fireUpdated(Model.EVT_CHANGE_WEEKLYSCHEDULE, null);
  }

  public void renameSchedule(String before, String after) throws BadNameException {
    if (getJunction().getScheduleBank().contains(after))
      throw new BadNameException();
    Schedule schedule = getJunction().getScheduleBank().get(before);
    if (schedule != null) {
      getJunction().getScheduleBank().remove(before);
      schedule.setName(after);
      // re-add with new name
      getJunction().getScheduleBank().add(schedule);
      // update entries in weekly schedule
      WeeklyScheduleEntry[] entries = getJunction().getWeeklySchedule().getEntries();
      for (int i = 0; i < entries.length; ++i) {
        if (entries[i].scheduleName.equals(before))
          entries[i].scheduleName = after;
      }
      fireUpdated();
    }
  }

  public void reset() {
    getJunction().getVehicleGroups().clear();
    getJunction().setScheduleBank(new ScheduleBank());
    getJunction().setWeeklySchedule(new WeeklySchedule(getJunction().getScheduleBank()));
    getJunction().setDescription(new JunctionDescription());
    getJunction().setMcmMatrix(new McmMatrix());
    getJunction().freeMap();

    m_mainSimManager = new SimManager(this, SimMode.Default);
    m_actSimManager = m_mainSimManager;

    // m_simManager.reset();
    fireUpdated();
  }

  public void resetSimManager() {
    m_actSimManager.reset();
    setModified(true);
    fireUpdated();
  }

  public boolean saveContents(String filename) {
    Document document = XmlKit.createDoc();
    Element root = xmlSave(document);
    if (root != null) {
      document.appendChild(root);
      XmlKit.saveDoc(filename, document);
      setModified(false);
      return true;
    }
    return false;
  }

  public void setTerrainMap(TerrainMap map) {
    getJunction().setTerrainMap(map);
  }

  public void setWeeklySchedule(WeeklySchedule ws) {
    getJunction().setWeeklySchedule(ws);
    fireUpdated(Model.EVT_CHANGE_WEEKLYSCHEDULE, null);
  }

  public void setWeeklyScheduleStartDay(int day) {
    getJunction().getWeeklySchedule().setStartDay(day);
    fireUpdated(Model.EVT_CHANGE_WEEKLYSCHEDULE, null);
  }

  public void swapGroups(int a, int b) {
    Junction p = getJunction();
    Collections.swap(p.getVehicleGroups(), a, b);
    p.getMcmMatrix().swapRows(a, b);
    p.getMcmMatrix().swapCols(a, b);

    for (Schedule s : getJunction().getScheduleBank().getSchedules())
      s.swapPrograms(a, b);
    setModified(true);
    // update();
  }

  public boolean xmlLoad(final Document document, final Element root) {

    if (!getJunction().loadFromXmlElement(document, root))
      return false;

    // load simulation
    Element simElem = XmlKit.firstChild(root, "simulation");
    if (simElem != null) {
      if (!m_mainSimManager.xmlLoad(document, simElem))
        return false;
    }

    m_mainSimManager.fireUpdated();

    // update capacity calculations
    for (Schedule s : getJunction().getScheduleBank())
      s.updateCapacityData();

    return true;
  }

  public Element xmlSave(Document document) {
    Element eRoot = document.createElement("traffix");
    eRoot.setAttribute("version", "1");

    getJunction().saveToXmlElement(document, eRoot);
    // simulation
    Element simElem = m_mainSimManager.xmlSave(document);
    if (simElem == null)
      return null;
    eRoot.appendChild(simElem);

    Element accs = document.createElement("accidents");
    IStoreSession ses = Traffix.persistenceSessionFactory().newSaveSession(document);
    accs.appendChild(ses.store(m_accidentModels));
    eRoot.appendChild(accs);
    ses.close();

    return eRoot;
  }

  private int findGroupByUniqueIdent(List<VehicleGroup> groups, int ident) {
    for (int i = 0; i < groups.size(); ++i) {
      VehicleGroup g = groups.get(i);
      if (g.getUniqueID() == ident)
        return i;
    }
    return -1;
  }

  private Junction getJunction() {
    return m_junctionData;
  }

  private VehicleGroup importMcmGroup(Element groupNode) {
    VehicleGroup g = new VehicleGroup(getJunction());
    g.setPrefix(groupNode.getAttribute("type"));
    g.setNum(Integer.parseInt(groupNode.getAttribute("num")));
    g.setName(groupNode.getAttribute("name"));
    g.setEvacuationSpeed(Integer.parseInt(groupNode.getAttribute("evacuationSpeed")));
    g.setApproachSpeed(Integer.parseInt(groupNode.getAttribute("approachSpeed")));
    g.setUniqueID(Integer.parseInt(groupNode.getAttribute("uniqueIdent")));
    g.setAcceleration(Float.parseFloat(groupNode.getAttribute("accel")));
    g.setLengthInMeters(Float.parseFloat(groupNode.getAttribute("length")));
    return g;
    // m_groups.add(g);
  }

  private void importModifiedMcmData(List<VehicleGroup> newGroups,
      McmMatrix newMcmMatrix, JunctionDescription newDesc, TerrainMap terrainMap) {
    int[] mapNewToOld = new int[newGroups.size()];
    int[] mapOldToNew = new int[getJunction().getVehicleGroups().size()];

    // Map groups being imported to groups currently available
    for (int i = 0; i < newGroups.size(); ++i) {
      mapNewToOld[i] = findGroupByUniqueIdent(getJunction().getVehicleGroups(),
          (newGroups.get(i)).getUniqueID());
    }

    // Map groups currently available to groups being imported
    for (int i = 0; i < getJunction().getVehicleGroups().size(); ++i) {
      mapOldToNew[i] = findGroupByUniqueIdent(newGroups, (getJunction()
          .getVehicleGroups().get(i)).getUniqueID());
    }
    // find added groups
    Vector<VehicleGroup> addedGroups = new Vector<VehicleGroup>();
    for (int i = 0; i < newGroups.size(); ++i) {
      if (mapNewToOld[i] == -1)
        addedGroups.add(newGroups.get(i));
    }
    // find removed groups
    Vector<VehicleGroup> removedGroups = new Vector<VehicleGroup>();
    for (int i = 0; i < getJunction().getVehicleGroups().size(); ++i) {
      if (mapOldToNew[i] == -1)
        removedGroups.add(getJunction().getVehicleGroups().get(i));
    }
    // Fix track geometry params (copy from old to new)
    for (int i = 0; i < newGroups.size(); ++i) {
      int oldId = mapNewToOld[i];
      if (oldId != -1) {
        VehicleGroup oldGp = getJunction().getVehicleGroups().get(oldId);
        VehicleGroup newGp = newGroups.get(i);
        newGp.setNumTracks(oldGp.getNumTracks());
        newGp.setDegreeOfFreedom(oldGp.getDegreeOfFreedom());
        newGp.setBusCoeff(oldGp.getBusCoeff());
        newGp.setParkingCoeff(oldGp.getParkingCoeff());
        newGp.setOverflowCoeff(oldGp.getOverflowCoeff());
      }
    }

    MessageBox box = new MessageBox(Traffix.shell(), SWT.ICON_INFORMATION | SWT.YES
        | SWT.NO);
    box.setText(Traffix.NAME + ": Import Mcm");
    String msg = "Kontynuowanie operacji importowania spowoduje nastêpuj¹ce zmiany:\n";
    if (removedGroups.size() > 0) {
      msg = msg + "\nZostan¹ USUNIÊTE grupy i ich programy:\n";
      for (int i = 0; i < removedGroups.size(); ++i) {
        VehicleGroup g = removedGroups.get(i);
        msg = msg + " " + g.getPrefix() + g.getNum() + " (" + g.getName() + ")\n";
      }
    }
    if (addedGroups.size() > 0) {
      msg = msg + "\nZostan¹ DODANE grupy wraz ze standardowym programem:\n";
      for (int i = 0; i < addedGroups.size(); ++i) {
        VehicleGroup g = addedGroups.get(i);
        msg = msg + " " + g.getPrefix() + g.getNum() + " (" + g.getName() + ")\n";
      }
    }
    msg = msg + "\nDane grup, tabela MCM oraz œcie¿ki zostan¹ uaktualnione.\n";
    msg = msg + "\nKontynuowaæ?";
    box.setMessage(msg);
    if (box.open() != SWT.YES)
      return;

    getJunction().setVehicleGroups(newGroups);
    getJunction().setMcmMatrix(newMcmMatrix);
    getJunction().setDescription(newDesc);
    getJunction().setTerrainMap(terrainMap);

    // copy group programs from old data (update schedule bank)
    Schedule[] schedules = getJunction().getScheduleBank().getSchedules();
    for (int i = 0; i < schedules.length; ++i) {
      Schedule schedule = schedules[i];
      GroupProgram[] newProgs = new GroupProgram[getJunction().getVehicleGroups().size()];
      for (int numGroup = 0; numGroup < getJunction().getVehicleGroups().size(); ++numGroup) {
        if (mapNewToOld[numGroup] == -1) {
          // insert yellow pulsing program
          newProgs[numGroup] = new GroupProgram();
          newProgs[numGroup].setLength(schedule.getProgramLength());
          newProgs[numGroup].set(0, schedule.getProgramLength() - 1,
              LightTypes.PULSING_YELLOW);
        } else {
          // program is already present in old bank, use that
          newProgs[numGroup] = schedule.getProgram(mapNewToOld[numGroup]);
        }
      }
      // set schedule to newly found programs
      int[] groupIds = new int[getJunction().getVehicleGroups().size()];
      for (int j = 0; j < getJunction().getVehicleGroups().size(); ++j)
        groupIds[j] = j;
      schedule.setGroupsAndPrograms(groupIds, newProgs);
    }
    setModified(true);
  }

  private void setCurrentProject(Junction proj) {
    m_junctionData = proj;
    fireUpdated();
  }
}