/*
 * Created on 2004-08-20
 */

package traffix.core.sim.generation;

import java.util.*;

import org.tw.geometry.Obb;

import traffix.Traffix;
import traffix.core.sim.ISimManager;
import traffix.core.sim.Route;
import traffix.core.sim.RouteInfo;
import traffix.core.sim.entities.*;
import traffix.core.sim.graph.Graph;
import traffix.core.sim.graph.Node;

public abstract class AbstractGenerationModel implements IGenerationModel {
  private static Random s_random        = new Random();
  private List<Entry>   m_arrivals      = null;
  private float         m_arrivalsT0;
  private HashSet<Node> m_blockedNodes  = new HashSet<Node>();
  private List<Entry>   m_orgArrivals   = new LinkedList<Entry>();
  private List<Entry>   m_queuedEntries = new LinkedList<Entry>();

  public abstract float chooseTime(Route route);

  public Mobile createMobile(Route route, int type) {
    ISimManager simman = Traffix.simManager();

    Mobile mobile = null;
    if (type == IMobile.Trolley || (type == IMobile.HeavyVehicle && Math.random() < 0.5f)) {
      mobile = new MobileChain(simman);
    } else {
      mobile = new Mobile(simman);
    }
    setupSingleMobile(route, type, mobile);

    if (mobile.route.getNumNodes() < 2) {
      System.out.println("Zonk!");
      return null;
    }

    if (mobile instanceof MobileChain) {
      MobileChain m1 = (MobileChain) mobile;
      MobileChain m2 = new MobileChain(simman);
      m1.nextInChain = m2;
      m2.prevInChain = m1;
      m2.location = mobile.location;
      m2.length = mobile.length;
      m2.type = mobile.type;
      m2.route = mobile.route;
      m2.speed = mobile.maxSpeed;
      m2.maxSpeed = mobile.maxSpeed;
      m2.maxAccel = mobile.maxAccel;
      if (mobile.type == IMobile.HeavyVehicle) {
        m1.length = 6;
        m2.length = 4;
      }
      return m1;// new Mobile[]{m1, m2};
    }

    return mobile;// new Mobile[]{mobile};
  }

  private float m_setArriveTime      = 0;
  private int   m_busOrTrolleyGenSeg = 0;

  public List<Entry> generateHourlySchedule(List<Route> routes) {
    List<Entry> entrances = new LinkedList<Entry>();

    for (Iterator<Route> iter = routes.iterator(); iter.hasNext();) {
      Route route = iter.next();
      RouteInfo info = route.getInfo();
      int sum = info.getMeasure().nPh + info.getMeasure().hPh;
      // generate normal vehicles
      for (int i = 0; i < info.getMeasure().nPh; ++i) {
        generateEntry(entrances, route, IMobile.NormalVehicle);
      }
      // generate heavy vehicles
      for (int i = 0; i < info.getMeasure().hPh; ++i) {
        generateEntry(entrances, route, IMobile.HeavyVehicle);
      }
      // buses
      m_setArriveTime = 0;
      m_busOrTrolleyGenSeg = 0;
      for (int i = 0; i < info.getMeasure().busPh; ++i) {
        double seglen = 3600.0 / info.getMeasure().busPh;
        m_setArriveTime = (float) (m_busOrTrolleyGenSeg * seglen + seglen / 2
            + Math.random() * seglen / 4 - seglen / 8);
        generateEntry(entrances, route, IMobile.Bus);
        ++m_busOrTrolleyGenSeg;
      }
      // trolleys
      m_setArriveTime = 0;
      m_busOrTrolleyGenSeg = 0;
      for (int i = 0; i < info.getMeasure().trolleyPh; ++i) {
        double seglen = 3600.0 / info.getMeasure().trolleyPh;
        m_setArriveTime = (float) (m_busOrTrolleyGenSeg * seglen + seglen / 2
            + Math.random() * seglen / 4 - seglen / 8);
        generateEntry(entrances, route, IMobile.Trolley);
        ++m_busOrTrolleyGenSeg;
      }

      // pedestrians
      if (info.getMeasure().pedeInterval > 0) {
        int numPede = 3600 / info.getMeasure().pedeInterval;
        float t = 0;
        for (int i = 0; i < numPede; ++i) {
          t += info.getMeasure().pedeInterval;
          m_setArriveTime = t;
          generateEntry(entrances, route, IMobile.Pedestrian);
        }
      }

      // cyclists
      if (info.getMeasure().cyclistInterval > 0) {
        int numPede = 3600 / info.getMeasure().cyclistInterval;
        float t = 0;
        for (int i = 0; i < numPede; ++i) {
          t += info.getMeasure().cyclistInterval;
          m_setArriveTime = t;
          generateEntry(entrances, route, IMobile.Cyclist);
        }
      }

    }
    Collections.sort(entrances, new Comparator() {
      public int compare(Object arg0, Object arg1) {
        Entry a = (Entry) arg0;
        Entry b = (Entry) arg1;
        return Float.compare(a.relativeArrivalTime, b.relativeArrivalTime);
      }
    });
    return entrances;
  }

  public int getNumQueued() {
    return m_queuedEntries.size();
  }

  public boolean isSafeToGenerate(Entry entry) {
    Mobile mobile = entry.mobiles[0];
    Obb obb2 = new Obb(mobile.getRoute().getStartPos(), 2, 2, 0);
    Iterator<IMobile> it = Traffix.simManager().getMobileIterator();
    while (it.hasNext()) {
      IMobile m = it.next();
      if (m instanceof IMobileChain) {
        IMobileChain ch = (IMobileChain) m;
        while (ch != null) {
          if (ch.getObb().overlaps(obb2))
            return false;
          ch = ch.getNextInChain();
        }
      } else {
        Obb obb = m.getObb();
        if (obb.overlaps(obb2))
          return false;
      }
    }
    return true;
  }

  public void reset() {
    m_arrivals = null;
    m_queuedEntries.clear();
  }

  public void rewind() {
    m_queuedEntries.clear();
    if (m_orgArrivals == null) {
      m_arrivals = null;
      return;
    }
    m_arrivals = new ArrayList<Entry>(m_orgArrivals);

    // Reset entries back to normal state
    for (Entry e : m_arrivals) {
      e.prevTryTime = 0;
      Mobile[] m = e.mobiles;
      for (int j = 0; j < m.length; j++) {
        m[j].location = 0.0001f;
        m[j].speed = m[j].maxSpeed;
      }
    }
  }

  public void update(float t0, float delta) {
    float t1 = t0 + delta;

    if (m_arrivals == null || t1 - m_arrivalsT0 >= 3600.0f) {
      m_arrivalsT0 = t0;
      m_orgArrivals = generateHourlySchedule(Traffix.simManager().getRoutes());
      m_arrivals = new LinkedList<Entry>(m_orgArrivals);
    }

    resetNumWaitingMobiles();
    handleArrivals(t0);
    handleQueue(t0, delta);
  }

  private void generateEntry(List<Entry> entrances, Route route, int type) {
    Mobile m = createMobile(route, type);
    if (m == null) {
      System.out.println("Uwaga - Pojazd niewygenerowany");
      return;
    }

    float t;
    if (type != IMobile.Trolley && type != IMobile.Bus
        && type != IMobile.Pedestrian && type != IMobile.Cyclist) {
      t = chooseTime(route);
    } else {
      // autobusy i tramwaje i piesi jezdza inaczej (co pewien okres czasu)
      t = m_setArriveTime;
    }
    Entry e = new Entry();
    e.relativeArrivalTime = t;
    e.absoluteArrivalTime = m_arrivalsT0 + t;
    // e.mobiles = m;
    e.mobiles = new Mobile[] { m };
    m.arrivalTime = e.absoluteArrivalTime;

    //    
    // for (int i = 0; i < m.length; ++i) {
    // if (m == null) {
    // System.out.println("Uwaga - Pojazd niewygenerowany");
    // return;
    // } else {
    // m[i].arrivalTime = e.absoluteArrivalTime;
    // }
    // }
    entrances.add(e);
  }

  private void handleArrivals(float t0) {
    for (Iterator<Entry> iter = m_arrivals.iterator(); iter.hasNext();) {
      Entry e = iter.next();
      if (e.absoluteArrivalTime <= t0) {
        // arrive at crossing
        Route r = e.mobiles[0].getRoute();
        ++r.getInfo().numArrivedVehicles[e.mobiles[0].getType()];
        r.getInfo().numArrivedVirtualVehicles += e.mobiles[0].getVirtualVehiclesWeight();

        m_queuedEntries.add(e);
        iter.remove();
      } else {
        break;
      }
    }
  }

  private void handleQueue(float t0, float delta) {
    m_blockedNodes.clear();

    for (Iterator<Entry> iter = m_queuedEntries.iterator(); iter.hasNext();) {
      Entry e = iter.next();

      // add stopped time to stats
      Route r = e.mobiles[0].getRoute();
      r.getInfo().summedStopTime += delta;
      ++r.getBeginningNode().numWaitingMobiles;

      // node blocked?
      if (m_blockedNodes.contains(e.mobiles[0].getRoute().getBeginningNode())) {
        continue;
      }

      // try every other sec
      if (t0 - e.prevTryTime > 1) {
        e.prevTryTime = t0;
        if (isSafeToGenerate(e)) {
          for (int j = 0; j < e.mobiles.length; ++j)
            Traffix.simManager().addMobile(e.mobiles[j]);
          iter.remove();
        } else {
          m_blockedNodes.add(e.mobiles[0].getRoute().getBeginningNode());
        }
      }
    }
  }

  private void resetNumWaitingMobiles() {
    Graph g = Traffix.simManager().getGraph();
    for (Iterator<Node> iter = g.getNodeIterator(); iter.hasNext();) {
      Node n = iter.next();
      if (n.isBeginningNode()) {
        n.numWaitingMobiles = 0;
      }
    }
  }

  private void setupSingleMobile(Route route, int type, Mobile mobile) {
    mobile.type = type;
    mobile.location = 0.0001f;

    // if (type != IMobile.T_PEDESTRIAN) {
    IMobile.MoveParams mp = route.getInfo().getMoveParams()[type];
    mobile.maxSpeed = (float) (mp.speed + Math.random() * mp.vdelta * 2 - mp.vdelta);
    mobile.maxSpeed = Math.max(1, mobile.maxSpeed);
    mobile.speed = mobile.maxSpeed;
    mobile.maxAccel = mp.acceleration;
    mobile.length = mp.length;
    // }
    mobile.route = route;

    // if (type == IMobile.T_PEDESTRIAN) {
    // mobile.maxSpeed = mobile.speed = 1.4f;
    // mobile.maxAccel = 6.0f;
    // mobile.length = 1;
    // }

  }
}