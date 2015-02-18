/*
 * Created on 2005-08-24
 */

package traffix.core.model;

import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import traffix.Traffix;
import traffix.core.VehicleGroup;
import traffix.core.McmMatrix;
import traffix.core.TerrainMap;
import traffix.core.schedule.Schedule;
import traffix.core.schedule.ScheduleBank;
import traffix.core.schedule.WeeklySchedule;

public class Junction {
  private JunctionDescription m_description = new JunctionDescription();
  private McmMatrix m_mcmMatrix;
  private int m_subJunctionCount = 0;
  private ScheduleBank m_scheduleBank = new ScheduleBank();
  private TerrainMap m_terrainMap = new TerrainMap();
  private List<VehicleGroup> m_vehicleGroups = new ArrayList<VehicleGroup>();
  private WeeklySchedule m_weeklySchedule;

  public Junction() {
  }

  public void freeMap() {
    if (m_terrainMap != null)
      m_terrainMap.dispose();
    m_terrainMap = null;
  }

  public JunctionDescription getDescription() {
    return m_description;
  }

  public McmMatrix getMcmMatrix() {
    return m_mcmMatrix;
  }

  public ScheduleBank getScheduleBank() {
    return m_scheduleBank;
  }

  public TerrainMap getTerrainMap() {
    return m_terrainMap;
  }

  public List<VehicleGroup> getVehicleGroups() {
    return m_vehicleGroups;
  }

  public WeeklySchedule getWeeklySchedule() {
    return m_weeklySchedule;
  }

  public boolean loadFromXmlElement(Document document, Element root) {
    // load description
    if (root.hasAttribute("mergeCount"))
      m_subJunctionCount = Integer.parseInt(root.getAttribute("mergeCount"));
    else
      m_subJunctionCount = 0;

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
    // m_loadMonitor.worked(10);
    // load groups
    NodeList groupNodes = ((Element) root.getElementsByTagName("groups").item(0))
        .getElementsByTagName("group");
    Vector<VehicleGroup> groups = new Vector<VehicleGroup>();
    for (int i = 0; i < groupNodes.getLength(); ++i) {
      VehicleGroup g = new VehicleGroup(this);
      if (!g.xmlLoad(document, (Element) groupNodes.item(i)))
        return false;
      groups.add(g);
    }

    // m_loadMonitor.worked(10);
    // load mcm matrix
    McmMatrix mcmMatrix = new McmMatrix();
    if (!mcmMatrix.xmlLoad(document, (Element) root.getElementsByTagName("mcm-matrix")
        .item(0)))
      return false;

    // m_loadMonitor.worked(10);
    // load schedule bank
    ScheduleBank scheduleBank = new ScheduleBank();
    if (!scheduleBank.xmlLoad(document, (Element) root.getElementsByTagName(
        "schedule-bank").item(0)))
      return false;

    // m_loadMonitor.worked(10);
    // load weekly schedule
    WeeklySchedule weeklySchedule = new WeeklySchedule(scheduleBank);
    if (!weeklySchedule.xmlLoad(document, (Element) root.getElementsByTagName(
        "weekly-schedule").item(0)))
      return false;
    // m_loadMonitor.worked(10);
    // load terrain map
    TerrainMap terrainMap = null;
    Element tmapElem = (Element) root.getElementsByTagName("terrain-map").item(0);
    if (tmapElem != null) {
      terrainMap = new TerrainMap();
      if (!terrainMap.xmlLoad(document, tmapElem))
        return false;
    }
    if (terrainMap == null) {
      terrainMap = new TerrainMap();
      terrainMap.setTerrainDims(100, 100);
    }
    // m_loadMonitor.worked(40);
    m_description = desc;
    m_vehicleGroups = groups;
    this.m_mcmMatrix = mcmMatrix;
    this.m_scheduleBank = scheduleBank;
    this.m_weeklySchedule = weeklySchedule;
    this.m_terrainMap = terrainMap;
    return true;
  }

  // 
  public Junction merge(Junction other) {
    Junction merged = new Junction();
    merged.m_description = m_description.clone();
    merged.m_mcmMatrix = m_mcmMatrix.merge(other.m_mcmMatrix);
    merged.m_terrainMap = new TerrainMap();
    merged.m_terrainMap.setTerrainDims(100, 100);
    merged.m_weeklySchedule = m_weeklySchedule.merge(other.m_weeklySchedule);

    List<VehicleGroup> groups1 = new ArrayList<VehicleGroup>();
    List<VehicleGroup> groups2 = new ArrayList<VehicleGroup>();
    for (VehicleGroup g : m_vehicleGroups) {
      groups1.add(g.clone());
    }
    for (VehicleGroup g : other.m_vehicleGroups) {
      groups2.add(g.clone());
    }

    for (VehicleGroup g : groups1) {
      g.setJunction(this);
      merged.m_vehicleGroups.add(g);
    }
    for (VehicleGroup g : groups2) {
      g.setJunction(this);
      merged.m_vehicleGroups.add(g);
    }

    // new ids
    int id = getMaxGroupId() + 1;
    for (VehicleGroup g : groups2)
      g.setUniqueID(id++);

    merged.m_subJunctionCount = m_subJunctionCount + other.m_subJunctionCount + 1;

    for (VehicleGroup g : groups1)
      if (g.getJunctionIndex() == 0)
        g.setJunctionIndex(g.getJunctionIndex() + 1);
    for (VehicleGroup g : groups2)
      g.setJunctionIndex(g.getJunctionIndex() + (merged.m_subJunctionCount + 1));

    List<String> incompatibleSchedules = new ArrayList<String>();
    List<String> deletedSchedules = new ArrayList<String>();
    // merge schedules
    merged.m_scheduleBank = new ScheduleBank();
    for (Schedule sch1 : m_scheduleBank.getSchedules()) {
      Schedule sch2 = other.m_scheduleBank.get(sch1.getName());
      if (sch2 != null) {
        if (sch1.getProgramLength() == sch2.getProgramLength())
          merged.m_scheduleBank.add(sch1.merge(sch2));
        else
          incompatibleSchedules.add(sch1.getName());
      } else {
        deletedSchedules.add(sch1.getName());
      }
    }
    for (Schedule s : other.m_scheduleBank.getSchedules())
      if (m_scheduleBank.get(s.getName()) == null)
        if (!deletedSchedules.contains(s.getName()))
          deletedSchedules.add(s.getName());

    Collections.sort(incompatibleSchedules);
    Collections.sort(deletedSchedules);
    String msg = "Harmonogramy o ró¿nej d³ugoœci programów zosta³y skasowane:\n";
    for (String s : incompatibleSchedules)
      msg += s + "\n";

    msg += "\nHarmonogramy nie wystêpuj¹ce podwójnie zosta³y skasowane:\n";
    for (String s : deletedSchedules)
      msg += s + "\n";
    Traffix.inform(msg);
    return merged;
  }

  public Element saveToXmlElement(Document document, Element eRoot) {
    eRoot.setAttribute("mergeCount", Integer.toString(m_subJunctionCount));
    // description
    Element descElem = document.createElement("description");
    eRoot.appendChild(descElem);
    descElem.setAttribute("crossingNum", m_description.crossingNum);
    descElem.setAttribute("projectNum", m_description.projectNum);
    descElem.setAttribute("city", m_description.city);
    descElem.setAttribute("streets", m_description.streets);
    descElem.setAttribute("date", m_description.date);
    descElem.setAttribute("controllerType", m_description.controllerType);
    descElem.setAttribute("note", m_description.note);

    // groups
    Element eGroups = document.createElement("groups");
    eRoot.appendChild(eGroups);
    for (int i = 0; i < m_vehicleGroups.size(); ++i) {
      Element eGroup = m_vehicleGroups.get(i).xmlSave(document);
      if (eGroup == null)
        return null;
      eGroups.appendChild(eGroup);
    }

    // mcm matrix
    if (m_mcmMatrix != null) {
      Element eMatrix = m_mcmMatrix.xmlSave(document);
      if (eMatrix == null)
        return null;
      eRoot.appendChild(eMatrix);
    }

    // schedule bank
    if (m_scheduleBank != null) {
      Element bankElem = m_scheduleBank.xmlSave(document);
      if (bankElem == null)
        return null;
      eRoot.appendChild(bankElem);
    }

    // weekly schedule
    if (m_weeklySchedule != null) {
      Element weeklyScheduleElem = m_weeklySchedule.xmlSave(document);
      if (weeklyScheduleElem == null)
        return null;
      eRoot.appendChild(weeklyScheduleElem);
    }
    // terrain map
    if (m_terrainMap != null) {
      Element tmapElem = m_terrainMap.xmlSave(document);
      if (tmapElem == null)
        return null;
      eRoot.appendChild(tmapElem);
    }
    return eRoot;
  }

  public void setDescription(JunctionDescription description) {
    this.m_description = description;
  }

  public void setMcmMatrix(McmMatrix mcmMatrix) {
    this.m_mcmMatrix = mcmMatrix;
  }

  public void setScheduleBank(ScheduleBank scheduleBank) {
    this.m_scheduleBank = scheduleBank;
  }

  public void setTerrainMap(TerrainMap terrainMap) {
    m_terrainMap = terrainMap;
  }

  public void setVehicleGroups(List<VehicleGroup> vehicleGroups) {
    this.m_vehicleGroups = vehicleGroups;
  }

  public void setWeeklySchedule(WeeklySchedule weeklySchedule) {
    this.m_weeklySchedule = weeklySchedule;
  }

  private void assignNewGroupIds(int startId) {
    for (VehicleGroup g : m_vehicleGroups)
      g.setUniqueID(startId++);
  }

  private void assignProjectNum(int pnum) {
    for (VehicleGroup g : m_vehicleGroups)
      g.setJunctionIndex(pnum);
  }

  private int getMaxGroupId() {
    int maxId = 0;
    for (VehicleGroup g : m_vehicleGroups)
      maxId = Math.max(maxId, g.getUniqueId());
    return maxId;
  }

  private int getMaxProjectNum() {
    int maxPnum = 0;
    for (VehicleGroup g : m_vehicleGroups)
      maxPnum = Math.max(maxPnum, g.getJunctionIndex());
    return maxPnum;
  }
}