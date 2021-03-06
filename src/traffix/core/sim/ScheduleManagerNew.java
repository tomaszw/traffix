/*
 * Created on 2004-09-03
 */

package traffix.core.sim;

import org.tw.web.XmlKit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import traffix.Traffix;
import traffix.core.VehicleGroup;
import traffix.core.sim.entities.TransitDetector;
import traffix.core.model.IPersistent;
import traffix.core.schedule.LightTypes;
import traffix.core.schedule.Schedule;

import java.util.*;

public class ScheduleManagerNew implements IScheduleProvider, IPersistent {
  private Schedule m_active;
  private float m_scheduleTime = 0;

  private FreezeInfo m_freeze = new FreezeInfo();
  private Map<TransitDetector, FreezeRequest> m_freezeRequests = new HashMap<TransitDetector, FreezeRequest>();

  private AccomodationMatrix m_priMatrix = new AccomodationMatrix();
  private Map<Object, ScheduleChangeRequest> m_changeRequests = new HashMap<Object, ScheduleChangeRequest>();
  private boolean m_useAccomodationProgram = false;

  private static class FreezeRequest {
    TransitDetector requestingDetector;
    int tmax;
  }

  private static class ScheduleChangeRequest {
    Object sender;
    Set changeTo;
    int clearedByGroup;
    boolean clearCond1, clearCond2;
  }

  private static class FreezeInfo {
    float absoluteStart, prolongDuration, maxDuration;
    boolean active, prolonging; //, maxTimeExceeded;
    float prolongStart;
  }

  public void requestFreeze(TransitDetector requestingDetector, int tmax) {
    FreezeRequest req = new FreezeRequest();
    req.requestingDetector = requestingDetector;
    req.tmax = tmax;
    m_freezeRequests.put(requestingDetector, req);
  }

  public void prolongFreeze(float dur) {
    if (!m_freeze.active)
      return;

    m_freeze.prolonging = true;
    m_freeze.prolongStart = Traffix.simManager().getCurrentTime();
    m_freeze.prolongDuration = dur;
  }

  public Schedule getActiveSchedule() {
    return m_active;
  }

  public float getFreezeDuration() {
    if (!isScheduleFreezed())
      return 0;
    return Traffix.simManager().getCurrentTime() - m_freeze.absoluteStart;
  }

  public int getScheduleTime() {
    Schedule sch = getActiveSchedule();
    if (sch == null)
      return 0;
    return ((int) m_scheduleTime)%sch.getProgramLength();
  }

  public String getXmlTagName() {
    return "scheduleManager";
  }

  public boolean isScheduleFreezed() {
    return m_freeze.active;
  }

  public AccomodationMatrix priMatrix() {
    return m_priMatrix;
  }

  public void requestChange(Object sender, Set changeTo, VehicleGroup clearedByGroup) {
    ScheduleChangeRequest req = new ScheduleChangeRequest();
    req.sender = sender;
    req.changeTo = changeTo;
    req.clearedByGroup = clearedByGroup.getIndex();
    m_changeRequests.put(sender, req);
  }

  public void reset() {
    m_changeRequests.clear();
    m_freeze.active = false;
    m_freeze.prolonging = false;
    m_scheduleTime = 0;

    if (m_useAccomodationProgram) {
      m_active = getBaseSchedule();
    }
    if (m_active != null)
      m_active.numSimCycles = 1;
  }

  public void setAccomodationProgramUsage(boolean u) {
    m_useAccomodationProgram = u;
  }

  public void setActiveSchedule(Schedule sch) {
    m_active = sch;
    Traffix.simManager().fireUpdated();
  }

  public void tick(float t0, float delta) {
    checkReqClearCondition1();

    updateScheduleTime(t0, delta);

    // end cycle if needed
    if (m_scheduleTime >= getActiveSchedule().getProgramLength()) {
      // cycle ended!
      cycleEnded();
    }

    checkReqClearCondition2();
  }

  private void checkReqClearCondition2() {
    // check for clear condition 2 and clear if necesarry
    Set<Object> toClear = new HashSet<Object>();
    for (ScheduleChangeRequest req : m_changeRequests.values()) {
      int light = getActiveSchedule().getProgram(req.clearedByGroup).get((int) m_scheduleTime);
      req.clearCond2 = (light == LightTypes.RED || light == LightTypes.NO_SIGNAL);

      if (req.clearCond1 && req.clearCond2)
        toClear.add(req.sender);
    }
    for (Object sender : toClear) {
      m_changeRequests.remove(sender);
    }
  }

  private void checkReqClearCondition1() {
    // check for request clear condition 1
    for (ScheduleChangeRequest req : m_changeRequests.values()) {
      int light = getActiveSchedule().getProgram(req.clearedByGroup).get((int) m_scheduleTime);
      req.clearCond1 = (light != LightTypes.RED && light != LightTypes.NO_SIGNAL);
    }
  }
  private void updateScheduleTime(float t0, float delta) {
    // start freeze if its not active and there are any
    // freeze requests
    if (!m_freeze.active) {
      if (m_freezeRequests.size() > 0) {
        m_freeze.active = true;
        m_freeze.absoluteStart = Traffix.simManager().getCurrentTime();
        m_freeze.prolonging = false;
        m_freeze.maxDuration = 0;
      }
    }

    // update max duration of freeze
    for (FreezeRequest req : m_freezeRequests.values())
      m_freeze.maxDuration = Math.max(m_freeze.maxDuration, req.tmax);
    m_freezeRequests.clear();

    // do stuff in freeze is active (prolonging and ending it)
    if (m_freeze.active) {
      if (t0 - m_freeze.absoluteStart > m_freeze.maxDuration) {
        m_freeze.active = false;
        //m_freeze.maxTimeExceeded = true;
        m_freeze.prolonging = false;
        m_scheduleTime = (float) Math.floor(m_scheduleTime + 1);
      } else if (m_freeze.prolonging
        && (t0 - m_freeze.prolongStart > m_freeze.prolongDuration)) {
        m_freeze.active = false;
        m_freeze.prolonging = false;
        m_scheduleTime = (float) Math.floor(m_scheduleTime + 1);
      }
    } else {
      // normal stuff if freeze not active
      m_scheduleTime += delta;
    }
  }

  public void unfreeze() {
    m_freeze.active = false;
    m_freeze.prolonging = false;
  }

  public boolean usesAccomodationProgram() {
    return m_useAccomodationProgram;
  }

  public boolean xmlLoad(Document document, Element root) {
    m_useAccomodationProgram = root.getAttribute("accProg").equals("1");
    if (root.hasAttribute("active"))
      m_active = Traffix.model().getScheduleBank().get(root.getAttribute("active"));
    Element priMatrixElem = XmlKit.firstChild(root, m_priMatrix.getXmlTagName());
    if (priMatrixElem != null && !m_priMatrix.xmlLoad(document, priMatrixElem))
      return false;
    return true;
  }

  public Element xmlSave(Document document) {
    Element root = document.createElement(getXmlTagName());
    root.setAttribute("accProg", m_useAccomodationProgram ? "1" : "0");
    if (m_active != null)
      root.setAttribute("active", m_active.getName());

    root.appendChild(m_priMatrix.xmlSave(document));
    return root;
  }

  private void switchProgram() {
    Set set = new HashSet();
    for (Iterator<ScheduleChangeRequest> iter = m_changeRequests.values().iterator(); iter.hasNext();) {
      ScheduleChangeRequest req = iter.next();
      set.addAll(req.changeTo);
    }

    Schedule next = null;
    int pri = 0;
    // priority of >= 2 must have conditions
    for (Iterator iter = set.iterator(); iter.hasNext();) {
      Schedule to = (Schedule) iter.next();
      int p = m_priMatrix.getPriority(m_active, to);
      if (p != 0 && p > pri) {
        pri = p;
        next = to;
      }
    }
    
    // priority of 1
    if (next == null) {
      Schedule[] all = Traffix.scheduleBank().getSchedules();
      for (int i = 0; i < all.length; i++) {
        if (m_priMatrix.getPriority(m_active, all[i]) == 1) {
          next = all[i];
          break;
        }
      }
    }

    if (next == null) {
      next = Traffix.scheduleBank().get(m_priMatrix.getBaseSchedule());
    }

    m_active = next;
  }

  public boolean isRequestActive(Object sender) {
    for (Iterator<ScheduleChangeRequest> iter = m_changeRequests.values().iterator(); iter.hasNext();) {
      ScheduleChangeRequest req = iter.next();
      if (req.sender == sender)
        return true;
    }
    return false;
  }

  private void cycleEnded() {
    Schedule act = m_active;
    m_scheduleTime = 0;
    if (m_useAccomodationProgram) {
      switchProgram();
    }
    // count cycles
    ++m_active.numSimCycles;
  }

  private Schedule getBaseSchedule() {
    if (!m_useAccomodationProgram)
      return m_active;
    String name = priMatrix().getBaseSchedule();
    return Traffix.model().getScheduleBank().get(name);
  }

  private int getPriority(Schedule from, Schedule to) {
    return m_priMatrix.getPriority(from.getName(), to.getName());
  }
}