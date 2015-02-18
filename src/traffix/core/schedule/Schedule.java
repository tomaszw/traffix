/*
 * Created on 2004-07-01
 */

package traffix.core.schedule;

import java.util.*;

import org.tw.web.XmlKit;
import org.w3c.dom.*;

import traffix.Traffix;
import traffix.core.VehicleGroup;
import traffix.core.Range;
import traffix.core.McmMatrix;
import traffix.core.model.IPersistent;

public class Schedule implements IPersistent, Cloneable {
  public int                    numSimCycles          = 0;
  private CapacityEntry[]       m_capaInfo;
  private GroupProgram[]        m_groupPrograms;
  private int[]                 m_groups;
  private Map<Integer, Integer> m_junctionZeroMarkers = new HashMap<Integer, Integer>();
  private String                m_name                = "PUSTY";

  /**
   * Create default 60sec schedule for current vehicle groups
   */
  public static Schedule createPulsingYellow(int numGroups) {
    if (numGroups == 0) {
      // can't, no groups
      return null;
    }
    Schedule sch = new Schedule(numGroups);
    sch.setProgramLength(60);
    for (int i = 0; i < numGroups; ++i) {
      GroupProgram p = sch.getProgram(i);
      VehicleGroup g = sch.getGroup(i);
      if (g.getPrefix().equals("b") || g.getPrefix().equals("k"))
        p.floodFill(0, LightTypes.PULSING_YELLOW);
      else
        p.floodFill(0, LightTypes.NO_SIGNAL);
    }

    sch.setName("¯Ó£TY-MIGAJ¥CY");
    return sch;
  }

  /**
   * Create empty program with just red light
   */
  public static Schedule createRed(int numGroups, String name) {
    if (numGroups == 0) {
      // can't, no groups
      return null;
    }
    Schedule sch = new Schedule(numGroups);
    sch.setProgramLength(60);
    sch.setName(name);
    return sch;
  }

  /**
   * Create default Y schedule
   */
  public static Schedule createYSchedule(int numGroups) {
    if (numGroups == 0) {
      // can't, no groups
      return null;
    }
    Schedule sch = new Schedule(numGroups);
    sch.setProgramLength(5);
    for (int i = 0; i < numGroups; ++i) {
      GroupProgram p = sch.getProgram(i);
      VehicleGroup g = sch.getGroup(i);
      if (g.getPrefix().equals("b") || g.getPrefix().equals("k"))
        p.floodFill(0, LightTypes.YELLOW);
      else
        p.floodFill(0, LightTypes.RED);
    }

    sch.setName("PROGRAM Y");
    return sch;
  }

  public Schedule() {
  }

  public Schedule(int[] groups) {
    m_groups = groups;
    m_groupPrograms = new GroupProgram[m_groups.length];
    m_capaInfo = new CapacityEntry[m_groups.length];
    for (int i = 0; i < m_groupPrograms.length; ++i) {
      m_groupPrograms[i] = new GroupProgram();
      m_groupPrograms[i].setLength(80);
      m_capaInfo[i] = new CapacityEntry();
    }
    updateCapacityData();
  }

  private Schedule(int numGroups) {
    m_groups = new int[numGroups];
    for (int i = 0; i < numGroups; ++i)
      m_groups[i] = i;
    m_groupPrograms = new GroupProgram[m_groups.length];
    m_capaInfo = new CapacityEntry[m_groups.length];
    for (int i = 0; i < m_groupPrograms.length; ++i) {
      m_groupPrograms[i] = new GroupProgram();
      m_groupPrograms[i].setLength(80);
      m_capaInfo[i] = new CapacityEntry();
    }

    updateCapacityData();
  }

  public void append(Schedule other) throws BadFormatException {
    if (other.getNumGroups() != getNumGroups())
      throw new BadFormatException();
    for (int i = 0; i < getNumGroups(); ++i) {
      GroupProgram p = getProgram(i);
      p.append(other.getProgram(i));
    }
  }

  public void assign(Schedule other) {
    m_name = other.m_name;
    m_groups = new int[other.m_groups.length];
    System.arraycopy(other.m_groups, 0, m_groups, 0, m_groups.length);
    m_groupPrograms = new GroupProgram[other.m_groupPrograms.length];
    m_capaInfo = new CapacityEntry[other.m_groupPrograms.length];
    System.out.println(other.m_groupPrograms.length + " " + other.m_capaInfo.length);
    for (int i = 0; i < other.m_groupPrograms.length; ++i) {
      m_groupPrograms[i] = (GroupProgram) other.m_groupPrograms[i].clone();
      m_capaInfo[i] = (CapacityEntry) other.m_capaInfo[i].clone();
    }
    m_junctionZeroMarkers.clear();
    m_junctionZeroMarkers.putAll(other.m_junctionZeroMarkers);
  }

  public void clear() {
    m_groups = null;
    m_groupPrograms = null;
    m_capaInfo = null;
    m_junctionZeroMarkers.clear();
  }

  @Override
  public Schedule clone() {
    Schedule s = new Schedule();
    s.assign(this);
    return s;
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof Schedule))
      return false;
    Schedule other = (Schedule) obj;
    return m_name.equals(other.m_name)
        && java.util.Arrays.equals(m_groups, other.m_groups)
        && java.util.Arrays.equals(m_groupPrograms, other.m_groupPrograms)
        && java.util.Arrays.equals(m_capaInfo, other.m_capaInfo)
        && m_junctionZeroMarkers.equals(other.m_junctionZeroMarkers);
  }

  public void extend(int p, int len) {
    for (int i = 0; i < getNumGroups(); ++i)
      getProgram(i).extend(p, len);
  }

  public int findNextMovePhase(int start) {
    boolean[] greenStates = getGreenStates(start);

    int p = normalizeTime(start + 1);
    while (p != start && equal(greenStates, getGreenStates(p))) {
      p = normalizeTime(p + 1);
    }
    if (p == start)
      return -1;
    return p;
  }

  public int findPrevMovePhase(int start) {
    boolean[] greenStates = getGreenStates(start);

    int p = normalizeTime(start - 1);
    while (p != start && equal(greenStates, getGreenStates(p))) {
      p = normalizeTime(p - 1);
    }
    if (p == start)
      return -1;
    greenStates = getGreenStates(p);
    while (equal(greenStates, getGreenStates(p)))
      p = normalizeTime(p - 1);
    p = normalizeTime(p + 1);
    return p;
  }

  public CapacityEntry getCapacityEntry(int i) {
    return m_capaInfo[i];
  }

  public int[] getColumn(int time) {
    int[] r = new int[getNumGroups()];
    for (int i = 0; i < r.length; ++i)
      r[i] = getProgram(i).get(time);
    return r;
  }

  public VehicleGroup getGroup(int i) {
    return Traffix.model().getGroupByIndex(m_groups[i]);
  }

  public int getJunctionZeroMarker(int jun) {
    if (m_junctionZeroMarkers.containsKey(jun))
      return m_junctionZeroMarkers.get(jun);
    return 0;
  }

  public String getName() {
    return m_name;
  }

  public int getNumGroups() {
    if (m_groups == null)
      return 0;
    return m_groups.length;
  }

  /**
   * Calculates number of collisions for every second of referenceGroup program with all
   * other group programs.
   */
  public int[] getNumOfCollisions(int referenceGroup, McmMatrix mcmMatrix) {
    int progLen = getProgramLength();
    int[] collisions = new int[progLen];
    for (int i = 0; i < collisions.length; ++i)
      collisions[i] = 0;
    GroupProgram refProg = getProgram(referenceGroup);
    for (int i = 0; i < m_groups.length; ++i) {
      if (i == referenceGroup || !mcmMatrix.areColliding(referenceGroup, i))
        continue;
      GroupProgram prog = getProgram(i);
      Range[] greenBs = prog.findGreenLight();
      if (greenBs == null || greenBs.length == 0)
        continue;
      for (int seg = 0; seg < greenBs.length; ++seg) {
        int zoneL = greenBs[seg].from() - mcmMatrix.getMcmTime(referenceGroup, i);
        int zoneR = greenBs[seg].to() + mcmMatrix.getMcmTime(i, referenceGroup);

        int zoneLen = zoneR - zoneL;
        if (zoneLen > getProgramLength()) {
          zoneR = zoneL + getProgramLength();
        }

        zoneL = normalizeTime(zoneL);
        zoneR = normalizeTime(zoneR);
        int t = zoneL;
        if (zoneLen < getProgramLength()) {
          while (t != zoneR) {
            ++collisions[t];
            t = normalizeTime(t + 1);
          }
        } else {
          for (t = 0; t < getProgramLength(); ++t)
            ++collisions[t];
        }
      }
    }
    return collisions;
  }

  public GroupProgram getProgram(int i) {
    return m_groupPrograms[i];
  }

  public int getProgramLength() {
    if (m_groupPrograms == null)
      return 0;
    return m_groupPrograms[0].getLength();
  }

  public int getTotalNumOfCollisions(McmMatrix mcmMatrix) {
    int num = 0;
    for (int i = 0; i < m_groups.length; ++i) {
      int[] coll = getNumOfCollisions(i, mcmMatrix);
      GroupProgram prog = getProgram(i);
      for (int j = 0; j < coll.length; ++j)
        if (LightTypes.isGreen(prog.get(j)))
          num += coll[j];
    }
    return num;
  }

  public String getXmlTagName() {
    return "schedule";
  }

  // merge two schedules using Kosendiak rules
  public Schedule merge(Schedule other) {
    Schedule s = clone();
    GroupProgram[] progs = new GroupProgram[getNumGroups() + other.getNumGroups()];
    int[] groups = new int[progs.length];
    for (int i = 0; i < groups.length; ++i)
      groups[i] = i;
    for (int i = 0; i < m_groups.length; ++i)
      progs[i] = getProgram(i).clone();
    for (int i = 0; i < other.m_groups.length; ++i) {
      progs[i + m_groups.length] = other.getProgram(i).clone();
      if (progs[i + m_groups.length].getLength() != getProgramLength()) {
        GroupProgram p = progs[i + m_groups.length].clone();
        p.setLength(getProgramLength());
        p.set(0, getProgramLength() - 1, LightTypes.NO_SIGNAL);
      }
    }
    s.setGroupsAndPrograms(groups, progs);
    for (int i = 0; i < other.m_groups.length; ++i)
      s.m_capaInfo[i + m_groups.length] = other.m_capaInfo[i].clone();
    return s;
  }

  public void moveJunctionZeroMarker(int jun, int delta) {
    if (!m_junctionZeroMarkers.containsKey(jun))
      m_junctionZeroMarkers.put(jun, 0);
    m_junctionZeroMarkers.put(jun, normalizeTime(m_junctionZeroMarkers.get(jun) + delta));
  }

  public int normalizeTime(int time) {
    return getProgram(0).normalizeTime(time);
  }

  public void setGroupsAndPrograms(int[] groups, GroupProgram[] progs) {
    if (progs.length != groups.length)
      throw new IllegalArgumentException();
    m_groups = groups;
    m_groupPrograms = progs;

    List<CapacityEntry> caps = new ArrayList<CapacityEntry>(Arrays.asList(m_capaInfo));
    while (caps.size() > m_groupPrograms.length)
      caps.remove(caps.size() - 1);
    while (caps.size() < m_groupPrograms.length)
      caps.add(new CapacityEntry());
    m_capaInfo = caps.toArray(m_capaInfo);
  }

  public void setName(String name) {
    m_name = name;
  }

  public void setProgramLength(int len) {
    for (int i = 0; i < m_groups.length; ++i)
      m_groupPrograms[i].setLength(len);
  }

  public void shrink(int p, int len) {
    for (int i = 0; i < getNumGroups(); ++i)
      getProgram(i).shrink(p, len);
  }

  public void swapPrograms(int a, int b) {
    GroupProgram tmp = m_groupPrograms[a];
    m_groupPrograms[a] = m_groupPrograms[b];
    m_groupPrograms[b] = tmp;
  }

  public void updateCapacityData() {
    for (int i = 0; i < m_groups.length; ++i)
      updateCapacityData(i);
  }

  public void updateCapacityData(int numGroup) {
    VehicleGroup group = getGroup(numGroup);
    GroupProgram prog = m_groupPrograms[numGroup];
    CapacityEntry e = m_capaInfo[numGroup];
    int G = prog.getTotalGreenDuration();
    int g = G / 20 + 1;
    float Ge = G + g - group.getBusCoeff() - group.getParkingCoeff();
    float TC = getProgramLength();
    float Cp = (group.getNumTracks() * Ge * 3600) / (group.getDegreeOfFreedom() * TC);
    Cp = (float) Math.ceil(Cp);
    float S = (group.getNumTracks() * (TC + g - group.getBusCoeff() - group.getParkingCoeff()) * 3600)
        / (group.getDegreeOfFreedom() * TC);
    S = (float) Math.ceil(S);
    float QCp = e.Q / Cp;
    QCp = (float) (Math.ceil(QCp * 100) / 100);
    float dCp = -(((100 + group.getOverflowCoeff()) * QCp) - 100);
    dCp = (float) (Math.ceil(dCp * 100) / 100);

    e.S = (int) S;
    e.Cp = Cp;
    e.QCp = QCp;
    e.dCp = dCp;
  }

  public boolean xmlLoad(Document document, Element root) {
    if (!root.getTagName().equals("schedule"))
      return false;
    String name = root.getAttribute("name");

    NodeList progNodes = root.getElementsByTagName("group-program");
    GroupProgram[] progs = new GroupProgram[progNodes.getLength()];
    int[] groups = new int[progs.length];

    CapacityEntry[] capaInfo = new CapacityEntry[progs.length];
    for (int i = 0; i < capaInfo.length; ++i)
      capaInfo[i] = new CapacityEntry();

    for (int i = 0; i < groups.length; ++i) {
      Element progElem = (Element) progNodes.item(i);
      if (!progElem.hasAttribute("groupId"))
        return false;
      // natezenie ruchu
      if (progElem.hasAttribute("Q"))
        capaInfo[i].Q = Integer.parseInt(progElem.getAttribute("Q"));

      groups[i] = Integer.parseInt(progElem.getAttribute("groupId"));
      if (!progElem.hasAttribute("length"))
        return false;
      int length = Integer.parseInt(progElem.getAttribute("length"));
      progs[i] = new GroupProgram();
      progs[i].setLength(length);

      Node textNode = progElem.getFirstChild();
      while (textNode != null && textNode.getNodeType() != Node.TEXT_NODE)
        textNode = textNode.getNextSibling();
      if (textNode == null)
        return false;
      String progString = textNode.getNodeValue();

      for (int j = 0; j < progString.length(); ++j)
        progs[i].set(j, j, (int) (progString.charAt(j) - 'a'));
    }

    Map<Integer,Integer> jzm = new HashMap<Integer,Integer>();
    Element[] elems = XmlKit.childElems(root, "junctionZeroMarker");
    for (Element e : elems) {
      int jun = Integer.parseInt(e.getAttribute("jun"));
      int m = Integer.parseInt(e.getAttribute("value"));
      jzm.put(jun, m);
    }
    
    m_groups = groups;
    m_groupPrograms = progs;
    m_name = name;
    m_junctionZeroMarkers = jzm;
    
    m_capaInfo = capaInfo;
    return true;
  }

  public Element xmlSave(Document document) {
    Element root = document.createElement("schedule");
    root.setAttribute("name", m_name);
    for (int i = 0; i < getNumGroups(); ++i) {
      Element progElem = document.createElement("group-program");
      // natezenie ruchu
      progElem.setAttribute("Q", Integer.toString(m_capaInfo[i].Q));
      progElem.setAttribute("groupId", Integer.toString(m_groups[i]));
      GroupProgram prog = m_groupPrograms[i];
      progElem.setAttribute("length", Integer.toString(prog.getLength()));
      StringBuffer buf = new StringBuffer(prog.getLength());
      for (int j = 0; j < prog.getLength(); ++j)
        buf.append((char) ('a' + prog.get(j)));

      root.appendChild(progElem);

      String progString = buf.toString();
      Node textNode = document.createTextNode(progString);
      progElem.appendChild(textNode);
    }
    
    for (int jun : m_junctionZeroMarkers.keySet()) {
      int m = m_junctionZeroMarkers.get(jun);
      Element e = document.createElement("junctionZeroMarker");
      e.setAttribute("jun", Integer.toString(jun));
      e.setAttribute("value", Integer.toString(m));
      root.appendChild(e);
    }
    return root;
  }

  boolean equal(boolean[] a, boolean[] b) {
    if (a.length != b.length)
      return false;
    for (int i = 0; i < a.length; ++i)
      if (a[i] != b[i])
        return false;
    return true;
  }

  boolean[] getGreenStates(int time) {
    time = normalizeTime(time);
    boolean[] states = new boolean[getNumGroups()];
    for (int i = 0; i < states.length; ++i) {
      int light = getProgram(i).get(time);
      states[i] = LightTypes.isGreen(light) ? true : false;
    }
    return states;
  }

  void putColumn(int[] col, int time) {
    for (int i = 0; i < getNumGroups(); ++i)
      getProgram(i).set(time, time, col[i]);
  }
}