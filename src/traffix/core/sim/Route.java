/*
 * Created on 2004-08-02
 */

package traffix.core.sim;

import org.tw.geometry.Vec2f;
import traffix.Traffix;
import traffix.core.sim.entities.IMobile;
import traffix.core.sim.graph.GraphLocation;
import traffix.core.sim.graph.IGraphPath;
import traffix.core.sim.graph.Node;

import java.util.List;

public class Route implements Cloneable {
  private boolean    m_cyclic = false;
  private RouteInfo  m_info   = new RouteInfo();
  private IGraphPath m_path;

  public static Route create(List<Node> nodes) {
    Route r = new Route();
    r.m_path = Traffix.simManager().getGraph().buildPath(nodes);
    return r;
  }

  public static Route createFromPath(IGraphPath path) {
    Route r = new Route();
    r.m_path = path;
    return r;
  }


  private Route() {
  }

  @Override
  public Route clone() {
    try {
      Route res = (Route) super.clone();
      res.m_info = m_info.clone();
      return res;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      return null;
    }
  }

  public boolean containsNode(Node n) {
    return m_path.getNodeIndex(n) != -1;
  }

  public Node getBeginningNode() {
    return m_path.getNode(0);
  }

  public IGraphPath.ObstacleEntry getFirstBlockingObstacle(float since, IMobile mobile) {
    return m_path.getFirstBlockingObstacle(since, mobile);
  }

  public RouteInfo getInfo() {
    return m_info;
  }

  public float getLength() {
    return m_cyclic ? Float.POSITIVE_INFINITY : m_path.getLength();
  }

  public int getNumNodes() {
    return m_path.getNumNodes();
  }

  public Vec2f getStartPos() {
    return m_path.getNode(0).getPos();
  }

  public IGraphPath path() {
    return m_path;
  }

  public void setInfo(RouteInfo info) {
    m_info.assign(info);
  }

  public Vec2f traverseDistance(float loc) {
    return m_path.traverseDistance(loc);
  }

  public GraphLocation traverseDistanceEx(float loc) {
    return m_path.traverseDistanceEx(loc);
  }

  public IGraphPath trim(float min, float max) {
    return m_path.trim(min, max);
  }
  
  public void update() {
    m_path.update();
  }
}