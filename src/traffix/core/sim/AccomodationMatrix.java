/*
 * Created on 2004-09-02
 */

package traffix.core.sim;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;

import org.tw.web.XmlKit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import traffix.Traffix;
import traffix.core.model.IPersistent;
import traffix.core.schedule.Schedule;

public class AccomodationMatrix implements IPersistent {
  private int[][] m_data = new int[0][0];
  private Schedule[] m_schedules = new Schedule[0];

  public AccomodationMatrix() {
  }

  public int dim() {
    return m_schedules.length;
  }

  public void updateSchedules() {
    for (int i=0; i<m_schedules.length; ++i)
      m_schedules[i] = Traffix.scheduleBank().get(m_schedules[i].getName());
  }
  
  public Schedule[] getSchedules() {
    return m_schedules;
  }

  public String getBaseSchedule() {
    return dim() > 0 ? m_schedules[0].getName() : null;
  }

  public String[] getNames() {
    String[] names = new String[dim()];
    for (int i = 0; i < names.length; i++) {
      names[i] = m_schedules[i].getName();
    }
    Arrays.sort(names, new Comparator<String>() {
      public int compare(String o1, String o2) {
        if (o1.equals(getBaseSchedule()))
            return -1;
        if (o2.equals(getBaseSchedule()))
            return 1;
        
        return o1.compareToIgnoreCase(o2);
      }
    });
    return names;
  }

  public int getPriority(String a, String b) {
    int i = getIndex(a);
    int j = getIndex(b);
    if (i == -1 || j == -1)
      return 0;
    return m_data[i][j];
  }

  public int getPriority(Schedule a, Schedule b) {
    int i = getIndex(a);
    int j = getIndex(b);
    if (i == -1 || j == -1)
      return 0;
    return m_data[i][j];
  }

  public String getXmlTagName() {
    return "scheduleMatrix";
  }

  public void setBaseSchedule(String name) {
    int idx = getIndex(name);
    if (idx == -1)
      return;//throw new IllegalArgumentException();
    if (idx == 0)
      return;
    swapSchedules(0, idx);
  }

  public void setPriority(String a, String b, int p) {
    int i = getIndex(a);
    int j = getIndex(b);
    m_data[i][j] = p;
  }

  public void setSchedules(Schedule[] schs) {
    String[] names = new String[schs.length];
    for (int i = 0; i < names.length; i++) {
      names[i] = schs[i].getName();
    }
    setSchedules(names);
  }

  public void setSchedules(String[] names) {
    Schedule[] schedules = new Schedule[names.length];
    for (int i = 0; i < schedules.length; i++) {
      schedules[i] = Traffix.scheduleBank().get(names[i]);
    }
    String base = getBaseSchedule();
    int[][] newData = new int[names.length][names.length];
    for (int i = 0; i < names.length; ++i) {
      for (int j = 0; j < names.length; ++j) {
        newData[i][j] = getPriority(schedules[i], schedules[j]);
      }
    }
    m_data = newData;

    m_schedules = new Schedule[names.length];
    for (int i = 0; i < names.length; ++i)
      m_schedules[i] = Traffix.scheduleBank().get(names[i]);

    if (base != null)
      setBaseSchedule(base);
  }

  public void update() {
//    Schedule[] schs = Traffix.model().getScheduleBank().getSchedules();
//    String[] names = new String[schs.length];
//    for (int i = 0; i < names.length; i++) {
//      names[i] = schs[i].getName();
//    }
//    setSchedules(names);
  }

  public boolean xmlLoad(Document document, Element root) {
    int dim = Integer.parseInt(root.getAttribute("dim"));
    m_schedules = new Schedule[dim];
    Element[] nameElems = XmlKit.childElems(root, "schedule");
    for (int i = 0; i < m_schedules.length; i++) {
      m_schedules[i] = Traffix.scheduleBank().get(nameElems[i].getAttribute("name"));
    }
    m_data = new int[dim][dim];
    Element[] cells = XmlKit.childElems(root, "cell");
    int idx = 0;
    for (int i = 0; i < m_schedules.length; ++i) {
      for (int j = 0; j < m_schedules.length; ++j) {
        m_data[i][j] = Integer.parseInt(cells[idx++].getAttribute("p"));
      }
    }
    return true;
  }

  public Element xmlSave(Document document) {
    Element root = document.createElement(getXmlTagName());
    root.setAttribute("dim", Integer.toString(dim()));
    for (int i = 0; i < m_schedules.length; ++i) {
      Element e = document.createElement("schedule");
      e.setAttribute("name", m_schedules[i].getName());
      root.appendChild(e);
    }
    for (int i = 0; i < m_schedules.length; ++i) {
      for (int j = 0; j < m_schedules.length; ++j) {
        Element e = document.createElement("cell");
        e.setAttribute("p", Integer.toString(m_data[i][j]));
        root.appendChild(e);
      }
    }
    return root;
  }

  private int getIndex(Schedule sch) {
    for (int i = 0; i < m_schedules.length; i++) {
      if (m_schedules[i] == sch)
        return i;
    }
    return -1;
  }

  private int getIndex(String name) {
    for (int i = 0; i < m_schedules.length; i++) {
      if (m_schedules[i].getName().equals(name))
        return i;
    }
    return -1;
  }

  public void init() {
    setSchedules(Traffix.scheduleBank().getSchedules());
  }

  private void swapCols(int a, int b) {
    if (a == b)
      return;
    for (int i = 0; i < dim(); ++i) {
      int tmp = m_data[i][a];
      m_data[i][a] = m_data[i][b];
      m_data[i][b] = tmp;
    }
  }

  private void swapRows(int a, int b) {
    int[] tmp = m_data[a];
    m_data[a] = m_data[b];
    m_data[b] = tmp;
  }

  private void swapSchedules(int a, int b) {
    Schedule tmp = m_schedules[a];
    m_schedules[a] = m_schedules[b];
    m_schedules[b] = tmp;
    swapRows(a, b);
    swapCols(a, b);
  }

  public int getPriority(int i, int j) {
    return m_data[i][j];
  }
}