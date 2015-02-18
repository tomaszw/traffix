/*
 * Created on 2004-07-08
 */

package traffix.core.schedule;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import traffix.Traffix;
import traffix.core.Weekdays;
import traffix.core.model.IPersistent;
import traffix.ui.Images;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Vector;

public class WeeklySchedule implements IPersistent,Cloneable {
  private Vector<WeeklyScheduleEntry> m_entries  = new Vector<WeeklyScheduleEntry>();
  private ScheduleBank                m_scheduleBank;
  private int                         m_startDay = Weekdays.SUNDAY;

  public WeeklySchedule(ScheduleBank bank) {
    m_scheduleBank = bank;
  }

  @Override
  public WeeklySchedule clone() {
    try {
      WeeklySchedule cloned = (WeeklySchedule) super.clone();
      cloned.m_entries = new Vector<WeeklyScheduleEntry>();
      for (WeeklyScheduleEntry e : m_entries)
        cloned.m_entries.add(e.clone());
      return cloned;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      return null;
    }
  }

  public WeeklySchedule merge(WeeklySchedule other) {
    WeeklySchedule r = clone();
    for (WeeklyScheduleEntry e : r.m_entries) {
      if (e.junctionNum == 0)
        e.junctionNum = 1;
    }
    int maxnum=0;
    for (WeeklyScheduleEntry e : r.m_entries) {
      maxnum = Math.max(e.junctionNum, maxnum);
    }
    WeeklySchedule tmp = other.clone();
    for (WeeklyScheduleEntry e : tmp.m_entries)
      e.junctionNum = maxnum + 1;
    r.m_entries.addAll(tmp.m_entries);
    return r;
  }
  
  public void addEntry(WeeklyScheduleEntry e) {
    m_entries.add(e);
  }

  public IStructuredContentProvider createContentProvider() {
    return new WsContentP();

  }

  public ITableLabelProvider createLabelProvider() {
    return new WsLabelP();
  }

  public ViewerSorter createSorter() {
    return new WsSorter(m_startDay);
  }

  public WeeklyScheduleEntry[] getEntries() {
    return m_entries.toArray(new WeeklyScheduleEntry[m_entries.size()]);
  }

  public WeeklyScheduleEntry getEntry(int i) {
    return m_entries.get(i);
  }

  public int getNumEntries() {
    return m_entries.size();
  }

  public int getStartDay() {
    return m_startDay;
  }

  public String getXmlTagName() {
    return "weekly-schedule";
  }

  public void removeEntry(WeeklyScheduleEntry e) {
    m_entries.remove(e);
  }

  public void setStartDay(int day) {
    m_startDay = day;
  }

  public boolean xmlLoad(Document document, Element root) {
    Vector<WeeklyScheduleEntry> entries = new Vector<WeeklyScheduleEntry>();
    int startDay = Weekdays.SUNDAY;
    try {
      startDay = Integer.parseInt(root.getAttribute("startDay")) % 7;
    } catch (NumberFormatException ex) {
    }

    NodeList entryElems = root.getElementsByTagName("entry");
    for (int i = 0; i < entryElems.getLength(); ++i) {
      Element entryElem = (Element) entryElems.item(i);
      WeeklyScheduleEntry e = new WeeklyScheduleEntry();
      e.scheduleName = entryElem.getAttribute("scheduleName");
      e.offset = 0;
      e.day = 0;
      e.hour = 0;
      e.minute = 0;
      e.junctionNum = 0;
      try {
        e.offset = Integer.parseInt(entryElem.getAttribute("offset"));
        e.day = Integer.parseInt(entryElem.getAttribute("day"));
        e.hour = Integer.parseInt(entryElem.getAttribute("hour"));
        e.minute = Integer.parseInt(entryElem.getAttribute("minute"));
        if (entryElem.hasAttribute("junction"))
          e.junctionNum = Integer.parseInt(entryElem.getAttribute("junction"));
      } catch (NumberFormatException ex) {
      }
      entries.add(e);
    }
    m_entries = entries;
    m_startDay = startDay;
    return true;
  }

  public Element xmlSave(Document document) {
    Element rootElem = document.createElement("weekly-schedule");
    rootElem.setAttribute("startDay", Integer.toString(m_startDay));
    for (int i = 0; i < m_entries.size(); ++i) {
      Element entryElem = document.createElement("entry");
      rootElem.appendChild(entryElem);
      WeeklyScheduleEntry e = getEntry(i);
      entryElem.setAttribute("scheduleName", e.scheduleName);
      entryElem.setAttribute("offset", Integer.toString(e.offset));
      entryElem.setAttribute("day", Integer.toString(e.day));
      entryElem.setAttribute("hour", Integer.toString(e.hour));
      entryElem.setAttribute("minute", Integer.toString(e.minute));
      entryElem.setAttribute("junction", Integer.toString(e.junctionNum));
    }
    return rootElem;
  }
}

class WsContentP implements IStructuredContentProvider {
  public void dispose() {
  }

  public Object[] getElements(Object inputElement) {
    WeeklySchedule weekly = (WeeklySchedule) inputElement;
    return weekly.getEntries();
  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }
}

class WsLabelP extends LabelProvider implements ITableLabelProvider {
  private NumberFormat offsetFmt = new DecimalFormat("000");
  private NumberFormat timeFmt   = new DecimalFormat("00");

  public Image getColumnImage(Object element, int columnIndex) {
    if (columnIndex != 0)
      return null;
    WeeklyScheduleEntry e = (WeeklyScheduleEntry) element;
    Schedule sch = Traffix.model().getSchedule(e.scheduleName);
    if (sch == null)
      return Images.get("icons/qmark20x20.gif");
    if (sch.getTotalNumOfCollisions(Traffix.model().getMcmMatrix()) == 0)
      return Images.get("icons/newProg.gif");
    else
      return Images.get("icons/warning20x20.gif");
  }

  public String getColumnText(Object element, int columnIndex) {
    WeeklyScheduleEntry e = (WeeklyScheduleEntry) element;
    switch (columnIndex) {
      case 1 :
        if (e.junctionNum == 0)
          return e.scheduleName;
        else
          return e.junctionNum + "." + e.scheduleName;
      case 2 :
        return Weekdays.names[e.day];
      case 3 :
        String hstr = timeFmt.format(e.hour);
        String mstr = timeFmt.format(e.minute);
        return hstr + ":" + mstr;
      case 4 :
        return offsetFmt.format(e.offset);
    }

    return "";
  }
}

class WsSorter extends ViewerSorter {
  int m_startDay = Weekdays.SUNDAY;

  public WsSorter(int startDay) {
    m_startDay = startDay % 7;
  }

  public int compare(Viewer viewer, Object el1, Object el2) {
    WeeklyScheduleEntry e1 = (WeeklyScheduleEntry) el1;
    WeeklyScheduleEntry e2 = (WeeklyScheduleEntry) el2;
    int day1 = (e1.day + 7 - m_startDay) % 7;
    int day2 = (e2.day + 7 - m_startDay) % 7;
    if (e1.junctionNum < e2.junctionNum)
      return -1;
    if (e1.junctionNum > e2.junctionNum)
      return 1;
    if (day1 < day2)
      return -1;
    if (day1 > day2)
      return 1;
    if (e1.hour < e2.hour)
      return -1;
    if (e1.hour > e2.hour)
      return 1;
    if (e1.minute < e2.minute)
      return -1;
    if (e1.minute > e2.minute)
      return 1;
    return 0;
  }
}