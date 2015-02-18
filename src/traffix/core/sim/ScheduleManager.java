/*
 * Created on 2004-09-03
 */

package traffix.core.sim;

import java.util.*;

import org.tw.web.XmlKit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import traffix.Traffix;
import traffix.core.VehicleGroup;
import traffix.core.model.IPersistent;
import traffix.core.schedule.LightTypes;
import traffix.core.schedule.Schedule;
import traffix.core.sim.entities.IDetector;
import traffix.core.sim.entities.TransitDetector;

public class ScheduleManager implements IScheduleProvider, IPersistent {
  private float                               m_absoluteTime           = 0;
  private Schedule                            m_active;

  private Map<Object, ScheduleChangeRequest>  m_changeRequests         = new HashMap<Object, ScheduleChangeRequest>();
  private FreezeInfo                          m_freeze                 = new FreezeInfo();

  private Map<TransitDetector, FreezeRequest> m_freezeRequests         = new HashMap<TransitDetector, FreezeRequest>();
  private AccomodationMatrix                  m_priMatrix              = new AccomodationMatrix();
  private float                               m_scheduleTime           = 0;
  private boolean                             m_useAccomodationProgram = false;

  private static class FreezeInfo {
    float absoluteStart, prolongDuration;
    boolean active, prolonging; // , maxTimeExceeded;
    float   prolongStart;
  }

  private static class FreezeRequest {
    boolean         linkedDetectorActive;
    TransitDetector requestingDetector;
    int             tmax;
  }

  private static class ScheduleChangeRequest {
    Set             changeTo;
    boolean         clearCond1, clearCond2;
    int             clearingGroup = -1;
    float           maxTime       = Float.POSITIVE_INFINITY;
    List<IDetector> clearingDets  = new ArrayList<IDetector>();

    IDetector          sender;
  }

  public AccomodationMatrix accMatrix() {
    return m_priMatrix;
  }

  public float getAbsoluteTime() {
    return m_absoluteTime;
  }

  public Schedule getActiveSchedule() {
    return m_active;
  }

  public float getFreezeDuration() {
    if (!isScheduleFreezed())
      return 0;
    return m_absoluteTime - m_freeze.absoluteStart;
  }

  public int getScheduleTime() {
    Schedule sch = getActiveSchedule();
    if (sch == null)
      return 0;
    return ((int) m_scheduleTime) % sch.getProgramLength();
  }

  public String getXmlTagName() {
    return "scheduleManager";
  }

  public boolean isRequestActive(Object sender) {
    for (Iterator<ScheduleChangeRequest> iter = m_changeRequests.values().iterator(); iter
        .hasNext();) {
      ScheduleChangeRequest req = iter.next();
      if (req.sender == sender)
        return true;
    }
    return false;
  }

  public boolean isScheduleFreezed() {
    return m_freeze.active;
  }

  public void prolongFreeze(float dur) {
    if (!m_freeze.active)
      return;
    // dont prolong freeze if any detectors which requested
    // freeze are active
    for (FreezeRequest req : m_freezeRequests.values()) {
      if (req.requestingDetector.getState() != TransitDetector.S_IDLE)
        return;
    }
    m_freeze.prolonging = true;
    m_freeze.prolongStart = m_absoluteTime;
    m_freeze.prolongDuration = dur;
  }

  public void requestChange(IDetector sender, Set changeTo, VehicleGroup clearedByGroup) {
    ScheduleChangeRequest req = m_changeRequests.get(sender);
    if (req == null) {
      req = new ScheduleChangeRequest();
      req.sender = sender;
      req.changeTo = changeTo;
      req.clearingGroup = clearedByGroup != null ? clearedByGroup.getIndex() : -1;
      m_changeRequests.put(sender, req);
    }
  }

  public void requestChange2(IDetector sender, Set changeTo, VehicleGroup clearedByGroup,
      float maxDur, Set<IDetector> clearingDets) {
    ScheduleChangeRequest req = m_changeRequests.get(sender);
    if (req == null) {
      req = new ScheduleChangeRequest();
      req.sender = sender;
      req.changeTo = changeTo;
      req.clearingGroup = clearedByGroup != null ? clearedByGroup.getIndex() : -1;
      req.maxTime = m_absoluteTime + maxDur;
      req.clearingDets = new ArrayList<IDetector>(clearingDets);
      m_changeRequests.put(sender, req);
    }
  }

  public void requestFreeze(TransitDetector requestingDetector, int tmax,
      boolean activeLink) {
    FreezeRequest req = new FreezeRequest();
    req.requestingDetector = requestingDetector;
    req.tmax = tmax;
    req.linkedDetectorActive = activeLink;

    m_freezeRequests.put(requestingDetector, req);

    // mark freeze as active if needed
    if (!m_freeze.active) {
      m_freeze.active = true;
      m_freeze.absoluteStart = m_absoluteTime;
      m_freeze.prolonging = false;
    }

    // cancel prolonging
    m_freeze.prolonging = false;

  }

  public void reset() {
    m_changeRequests.clear();
    unfreeze();
    m_scheduleTime = 0;
    m_absoluteTime = 0;

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
  }

  public void tick(float delta) {
    if (m_active == null)
      return;

    float t0 = m_absoluteTime;
    m_absoluteTime += delta;

    checkReqClearCondition1();

    updateScheduleTime(t0, delta);

    // end cycle if needed
    if (m_scheduleTime >= getActiveSchedule().getProgramLength()) {
      // cycle ended!
      cycleEnded();
    }

    checkReqClearCondition2();
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

  private float calcMaxFreezeDuration() {
    float tmax = 0;
    boolean activeLink = false;
    for (FreezeRequest req : m_freezeRequests.values())
      if (req.linkedDetectorActive) {
        activeLink = true;
        tmax = Float.POSITIVE_INFINITY;
        break;
      }
    // System.out.println(m_freezeRequests.size());
    for (FreezeRequest req : m_freezeRequests.values()) {
      if (activeLink)
        tmax = Math.min(tmax, req.tmax);
      else
        tmax = Math.max(tmax, req.tmax);
    }
    return tmax;
  }

  private void checkReqClearCondition1() {
    // check for request clear condition 1
    for (ScheduleChangeRequest req : m_changeRequests.values()) {
      req.clearCond1 = false;
      if (req.clearingGroup != -1) {
        int light = getActiveSchedule().getProgram(req.clearingGroup).get(
            (int) m_scheduleTime);
        req.clearCond1 = (light != LightTypes.RED && light != LightTypes.NO_SIGNAL);
      }
    }
  }

  private void checkReqClearCondition2() {
    // check for clear condition 2 and clear if necesarry
    Set<Object> toClear = new HashSet<Object>();
    for (ScheduleChangeRequest req : m_changeRequests.values()) {
      req.clearCond2 = false;
      if (req.clearingGroup != -1) {
        int light = getActiveSchedule().getProgram(req.clearingGroup).get(
            (int) m_scheduleTime);
        req.clearCond2 = (light == LightTypes.RED || light == LightTypes.NO_SIGNAL);
      }
      if (req.clearCond1 && req.clearCond2)
        toClear.add(req.sender);
      else if (m_absoluteTime > req.maxTime && req.sender.getState()==0) {
        toClear.add(req.sender);
      } else if (req.sender.getState()==0) {
        for (IDetector d : req.clearingDets) {
          if (d.isActive()) {
            toClear.add(req.sender);
            break;
          }
        }
      }
    }
    for (Object sender : toClear) {
      m_changeRequests.remove(sender);
    }
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
    String name = accMatrix().getBaseSchedule();
    return Traffix.model().getScheduleBank().get(name);
  }

  private int getPriority(Schedule from, Schedule to) {
    return m_priMatrix.getPriority(from.getName(), to.getName());
  }

  private void switchProgram() {
    Set set = new HashSet();
    for (Iterator<ScheduleChangeRequest> iter = m_changeRequests.values().iterator(); iter
        .hasNext();) {
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

  private void unfreeze() {
    m_freezeRequests.clear();
    m_freeze.active = false;
    m_freeze.prolonging = false;
  }

  private void updateScheduleTime(float t0, float delta) {
    // get max duration of freeze
    float freezeMax = calcMaxFreezeDuration();

    // if freeze is active:
    // a. check if max freeze time exceeded. end freeze completly if so
    // b. check if prolongation time exceeded. end freeze completly if so
    if (m_freeze.active) {
      if (t0 - m_freeze.absoluteStart > freezeMax) {
        // System.out.println("unfreezing 1");
        unfreeze();
        m_scheduleTime = (float) Math.floor(m_scheduleTime + 1);
      } else if (m_freeze.prolonging
          && (t0 - m_freeze.prolongStart > m_freeze.prolongDuration)) {
        // System.out.println("unfreezing 2");
        unfreeze();
        m_scheduleTime = (float) Math.floor(m_scheduleTime + 1);
      }
    } else {
      // normal stuff if freeze not active
      m_scheduleTime += delta;
    }
  }
}