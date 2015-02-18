/*
 * Created on 2004-09-04
 */

package traffix.core.sim.graph;

import org.tw.geometry.Vec2f;
import org.tw.geometry.Polygonf;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import traffix.Traffix;
import traffix.core.sim.AbstractObstacle;
import traffix.core.sim.Route;
import traffix.core.sim.entities.IMobile;

import java.util.*;

public class Intersection extends AbstractObstacle {
  public GraphLocation l1, l2;

  private static class RouteCache {
    Route route;
    float firstDist = Float.POSITIVE_INFINITY;
    IMobile first;
    float firstSpeed;
    int pri;
  }

  private Map<Route, RouteCache> m_cache = new HashMap<Route, RouteCache>();
  private Route m_open;

  protected List<GraphLocation> computeObstacleLocations() {
    List<GraphLocation> obstacles = new ArrayList<GraphLocation>(2);
    obstacles.add(l1);
    obstacles.add(l2);
    return obstacles;
  }

  public Polygonf getBounds() {
    return null;
  }

  public float getObstacleRadius() {
    return 3;
  }

  private int priority(IMobile m) {
    return m.getRoute().getInfo().priority;
  }

  public void notifyApproach(IMobile mobile, float remainingDist) {
    if (!Traffix.simManager().simParams().betterCollisionDetection)
      return;

    if (remainingDist < -getObstacleRadius())
      return;
    RouteCache c = m_cache.get(mobile.getRoute());
    if (c == null) {
      c = new RouteCache();
    }
    if (remainingDist < c.firstDist) {
      c.firstDist = remainingDist;
      c.first = mobile;
      c.firstSpeed = mobile.getSpeed();
      c.route = mobile.getRoute();
      c.pri = c.route.getInfo().priority;
    }
    if (c.route != null)
      m_cache.put(c.route, c);
  }

  public void notifyPass(IMobile mobile) {
  }

  public boolean shouldBlock(IMobile mobile) {
    if (!Traffix.simManager().simParams().betterCollisionDetection)
      return false;
    if (m_open == null)
      return false;
    return mobile.getRoute() != m_open;
  }

  public void update() {
    updateGraphLocations();
  }

  public void tick(float t0, float delta) {
    if (!Traffix.simManager().simParams().betterCollisionDetection)
      return;

    RouteCache pref = null;
    for (Iterator<RouteCache> iter = m_cache.values().iterator(); iter.hasNext();) {
      RouteCache c = iter.next();
      pref = preferred(pref, c);
    }
    if (pref != null)
      m_open = pref.route;
    else {
      //System.out.println("zonk");
      m_open = null;
    }
    //System.out.println(m_cache.size());
    m_cache.clear();
  }

  private RouteCache preferred(RouteCache a, RouteCache b) {
    if (a == null)
      return b;
    if (b == null)
      return a;
    boolean b1 = a.first.isBlocked(this);
    boolean b2 = b.first.isBlocked(this);
    if (b1 && !b2)
      return b;
    if (b2 && !b1)
      return a;
    if (a.pri >= b.pri)
      return a;
    return b;
  }

  public void reset() {
    m_cache.clear();
  }

  public Vec2f getCenter() {
    return l1.getPoint();
  }

  public String getXmlTagName() {
    return null;
  }

  public Element xmlSave(Document document) {
    return null;
  }

  public boolean xmlLoad(Document document, Element element) {
    return false;
  }
}