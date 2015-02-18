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
import traffix.core.model.IPersistent;
import traffix.ui.Images;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ScheduleBank implements Iterable<Schedule>, IPersistent {
  private Map<String, Schedule> m_schedules = new HashMap<String, Schedule>();

  public void add(Schedule s) {
    m_schedules.put(s.getName(), s);
  }

  public Iterator<Schedule> iterator() {
    return m_schedules.values().iterator();
  }

  public boolean contains(String name) {
    return m_schedules.containsKey(name);
  }

  public IStructuredContentProvider createContentProvider() {
    return new SbContentP();
  }

  public ITableLabelProvider createLabelProvider() {
    return new SbLabelP();
  }

  public ViewerSorter createSorter() {
    return new SbSorter();
  }

  public Schedule get(String name) {
    return m_schedules.get(name);
  }

  public int getNumSchedules() {
    return m_schedules.size();
  }

  public Schedule[] getSchedules() {
    Schedule[] r = new Schedule[m_schedules.values().size()];
    m_schedules.values().toArray(r);
    Arrays.sort(r, new Comparator<Schedule>() {
      public int compare(Schedule o1, Schedule o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
      }
    });
    return r;
  }

  public void remove(String name) {
    m_schedules.remove(name);
  }

  public boolean xmlLoad(Document document, Element rootElem) {
    Map<String, Schedule> schedules = new HashMap<String, Schedule>();

    NodeList scheduleNodes = rootElem.getElementsByTagName("schedule");
    for (int i = 0; i < scheduleNodes.getLength(); ++i) {
      Schedule schedule = new Schedule();
      if (!schedule.xmlLoad(document, (Element) (scheduleNodes.item(i)))) {
        return false;
      }

      schedules.put(schedule.getName(), schedule);
    }

    m_schedules = schedules;

    return true;
  }

  public String getXmlTagName() {
    return "schedule-bank";
  }

  public Element xmlSave(Document document) {
    Element rootElem = document.createElement("schedule-bank");
    Iterator<Schedule> it = m_schedules.values().iterator();
    while (it.hasNext()) {
      Schedule s = it.next();
      Element scheduleElem = s.xmlSave(document);
      if (scheduleElem == null)
        return null;
      rootElem.appendChild(scheduleElem);
    }
    return rootElem;
  }

  public int getScheduleIndex(Schedule schedule) {
    Schedule[] sch = getSchedules();
    return Arrays.asList(sch).indexOf(schedule);
  }
}

class SbContentP implements IStructuredContentProvider {
  public void dispose() {
  }

  public Object[] getElements(Object inputElement) {
    ScheduleBank bank = (ScheduleBank) inputElement;
    return bank.getSchedules();
  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }
}

class SbLabelP extends LabelProvider implements ITableLabelProvider {
  private NumberFormat offsetFmt = new DecimalFormat("000");
  private NumberFormat timeFmt = new DecimalFormat("00");

  public Image getColumnImage(Object element, int columnIndex) {
    if (columnIndex != 0)
      return null;
    Schedule sch = (Schedule) element;
    if (sch.getTotalNumOfCollisions(Traffix.model().getMcmMatrix()) != 0) {
      return Images.get("icons/warning20x20.gif");
    } else {
      return Images.get("icons/newProg.gif");
    }
  }

  public String getColumnText(Object element, int columnIndex) {
    Schedule e = (Schedule) element;
    switch (columnIndex) {
      case 0:
        return e.getName();
      case 1:
        return Integer.toString(e.getProgramLength());
    }

    return "";
  }
}

class SbSorter extends ViewerSorter {
  public int compare(Viewer viewer, Object el1, Object el2) {
    Schedule s1 = (Schedule) el1;
    Schedule s2 = (Schedule) el2;
    return s1.getName().compareTo(s2.getName());
  }
}