/*
 * Created on 2004-08-17
 */

package traffix.core.sim.graph;

import java.util.*;

import org.tw.geometry.GeometryKit;
import org.tw.geometry.Vec2f;

import traffix.core.sim.IObstacle;
import traffix.core.sim.entities.IMobile;

public class GraphPath implements IGraphPath {
  private Graph m_graph;
  private float m_length;
  private Node m_node1, m_node2;
  private Node[] m_nodes = new Node[0];
  private float[] m_nodeLocations = new float[0];
  private Map<Node, Float> m_nodeLocationsMap = new HashMap<Node,Float>();
  
  private int m_nodesLength;
  private int m_nodesOffset;
  private List<Node> m_tempNodes = new ArrayList<Node>();

  public GraphPath(Graph graph) {
    m_graph = graph;

  }

  public boolean containsEdge(Node a, Node b) {
    for (int i=0; i<getNumNodes()-1; ++i) {
      if (getNode(i) == a && getNode(i+1) == b)
        return true;
    }
    return false;
  }
  
  public float calculateLength() {
    if (getNumNodes() <= 1)
      return 0;

    float accLen = 0;
    Node A = getNode(0);
    Node B;
    for (int i = 1; i < getNumNodes(); ++i) {
      B = getNode(i);
      float segLen = B.getPos().sub(A.getPos()).length();
      accLen += segLen;
      A = B;
    }
    return accLen;
  }

  //  public IGraphPath chopUpto(Node end) {
  //    boolean add = false;
  //    IGraphPath res = new GraphPath(m_graph);
  //    for (int i = 0; i < getNumNodes(); ++i) {
  //      Node n = getNode(i);
  //      if (n == end)
  //        add = true;
  //      if (add)
  //        res.add(n);
  //    }
  //    return res;
  //  }

  //  public ObstacleEntry getFirstObstacle() {
  //    return getFirstObstacle(0);
  //
  //  }

  //  public ObstacleEntry[] getObstacles() {
  //    List entries = new LinkedList();
  //    float accLen = 0;
  //    for (int i = 1; i < m_nodes.size(); ++i) {
  //      Node n1 = getNode(i - 1);
  //      Node n2 = getNode(i);
  //      Edge edge = m_graph.findEdge(n1, n2);
  //      for (Iterator iter = edge.getObstacleIterator(); iter.hasNext();) {
  //        IObstacle o = (IObstacle) iter.next();
  //        GraphLocation[] obstacleLocs = o.getObstacleGraphLocations();
  //        for (int nloc = 0; nloc < obstacleLocs.length; ++nloc) {
  //          if (obstacleLocs[nloc].edge == edge) {
  //            GraphLocation loc = new GraphLocation();
  //            loc.edge = edge;
  //            loc.distanceOverEdge = obstacleLocs[nloc].distanceOverEdge;
  //            loc.distanceUptoEdge = accLen;
  //            loc.distance = loc.distanceOverEdge + loc.distanceUptoEdge;
  //
  //            ObstacleEntry e = new ObstacleEntry();
  //            e.obstacle = o;
  //            e.pathLocation = loc;
  //            entries.add(e);
  //          }
  //        }
  //      }
  //      accLen += edge.getLength();
  //    }
  //    return (ObstacleEntry[]) entries.toArray(new ObstacleEntry[entries.size()]);
  //  }

  public float getDistanceTo(Vec2f p) {
    float minDist = Float.MAX_VALUE;

    float accLen = 0;
    Vec2f A = getNode(0).getPos();
    Vec2f B;
    for (int i = 1; i < getNumNodes(); ++i) {
      B = getNode(i).getPos();
      float segLen = A.distanceTo(B);

      float d = GeometryKit.distanceOfLineToPoint(A.x, A.y, B.x, B.y, p.x, p.y);
      if (d < minDist) {
        minDist = d;
      }

      accLen += segLen;
      A = B;
    }
    return minDist;
  }

  public IGraphPath.ObstacleEntry getFirstBlockingObstacle(float since, IMobile mobile) {
    float accLen = 0;
    float minDist = Float.POSITIVE_INFINITY;
    IGraphPath.ObstacleEntry entry = new IGraphPath.ObstacleEntry();

    for (int i = 1; i < getNumNodes() && (minDist == Float.POSITIVE_INFINITY); ++i) {
      Node n1 = getNode(i - 1);
      Node n2 = getNode(i);
      GraphEdge edge = m_graph.findEdge(n1, n2);
      for (Iterator<IObstacle> iter = edge.getObstacleIterator(); iter.hasNext();) {
        IObstacle o = iter.next();
        if (o == mobile || !o.shouldBlock(mobile))
          continue;
        GraphLocation[] obstacleLocs = o.getObstacleGraphLocations();
        for (int nloc = 0; nloc < obstacleLocs.length; ++nloc) {
          if (obstacleLocs[nloc].getEdge() == edge) {
            GraphLocation loc = new GraphLocation();
            loc.setEdge(edge);
            loc.setDistanceOverEdge(obstacleLocs[nloc].getDistanceOverEdge());
            loc.setDistanceUptoEdge(accLen);
            loc.setDistance(loc.getDistanceOverEdge() + loc.getDistanceUptoEdge());

            if (loc.getDistance() < since)
              continue;
            if (loc.getDistance() < minDist) {
              minDist = loc.getDistance();
              entry.obstacle = o;
              entry.pathLocation = loc;
            }
          }
        }
      }
      accLen += edge.getLength();
    }
    return minDist == Float.POSITIVE_INFINITY ? null : entry;
  }

  public IGraphPath.ObstacleEntry getFirstObstacle(float since, IObstacle except) {
    float accLen = 0;
    float minDist = Float.POSITIVE_INFINITY;
    IGraphPath.ObstacleEntry entry = new IGraphPath.ObstacleEntry();

    for (int i = 1; i < getNumNodes() && minDist == Float.POSITIVE_INFINITY; ++i) {
      Node n1 = getNode(i - 1);
      Node n2 = getNode(i);
      GraphEdge edge = m_graph.findEdge(n1, n2);
      for (Iterator<IObstacle> iter = edge.getObstacleIterator(); iter.hasNext();) {
        IObstacle o = iter.next();
        if (o == except)
          continue;
        GraphLocation[] obstacleLocs = o.getObstacleGraphLocations();
        for (int nloc = 0; nloc < obstacleLocs.length; ++nloc) {
          if (obstacleLocs[nloc].getEdge() == edge) {
            GraphLocation loc = new GraphLocation();
            loc.setEdge(edge);
            loc.setDistanceOverEdge(obstacleLocs[nloc].getDistanceOverEdge());
            loc.setDistanceUptoEdge(accLen);
            loc.setDistance(loc.getDistanceOverEdge() + loc.getDistanceUptoEdge());

            if (loc.getDistance() < since)
              continue;
            if (loc.getDistance() < minDist) {
              minDist = loc.getDistance();
              entry.obstacle = o;
              entry.pathLocation = loc;
            }
          }
        }
      }

      accLen += edge.getLength();
    }

    return minDist == Float.POSITIVE_INFINITY ? null : entry;
  }

  public float getLength() {
    return m_length;
  }

  public Node getNode(int i) {
    return m_nodes[i + m_nodesOffset];
  }

  public int getNodeIndex(Node n) {
    for (int i = 0; i < getNumNodes(); ++i) {
      if (getNode(i) == n)
        return i;
    }
    return -1;
  }

  public float getNodeLocation(Node n) {
    Float f = m_nodeLocationsMap.get(n);
    if (f != null)
      return f.floatValue();
    return Float.NaN;
    
//    for (int i = 0; i < getNumNodes(); i++) {
//      if (getNode(i) == n)
//        return m_nodeLocations[i+m_nodesOffset];
//    }
//    return Float.NaN;
  }
  
  private float calcNodeLocation(int idx) {
    if (idx == 0)
      return 0;

    float accLen = 0;
    for (int i = 1; i < m_nodes.length; ++i) {
      Node a = m_nodes[i-1];
      Node b = m_nodes[i];
      accLen += m_graph.findEdge(a, b).getLength();
      if (idx == i) {
        return accLen;
      }
    }
    return Float.NaN;
  }

  private void updateNodeLocations() {
    m_nodeLocations = new float[m_nodes.length];
    m_nodeLocationsMap.clear();
    for (int i=0; i<m_nodes.length; ++i) {
      m_nodeLocations[i] = calcNodeLocation(i);
      m_nodeLocationsMap.put(m_nodes[i], m_nodeLocations[i]);
    }
  }
  
  public void update() {
    updateNodeLocations();
  }
  
  public int getNumNodes() {
    return m_nodesLength;
  }

  public GraphEdge getTraversedEdge() {
    return m_graph.findEdge(m_node1, m_node2);
  }

  public static final float OBSTACLE_NOTIFY_DIST = 100;

  public void notifyObstaclesOnApproach(float pos, IMobile mobile) {
    float accLen = 0;
    for (int i = 1; i < getNumNodes() && accLen - pos < OBSTACLE_NOTIFY_DIST; ++i) {
      Node n1 = getNode(i - 1);
      Node n2 = getNode(i);
      GraphEdge edge = m_graph.findEdge(n1, n2);
      for (Iterator<IObstacle> iter = edge.getObstacleIterator(); iter.hasNext();) {
        IObstacle o = iter.next();
        if (o == mobile)
          continue;
        GraphLocation[] obstacleLocs = o.getObstacleGraphLocations();
        for (int nloc = 0; nloc < obstacleLocs.length; ++nloc) {
          if (obstacleLocs[nloc].getEdge() == edge) {
            float d = accLen + obstacleLocs[nloc].getDistanceOverEdge() - pos - o.getObstacleRadius();
            if (d > OBSTACLE_NOTIFY_DIST)
              continue;
            o.notifyApproach(mobile, d);
          }
        }
      }

      accLen += edge.getLength();
    }
  }

  public Vec2f traverseDistance(float loc) {
    if (getNumNodes() < 2)
      return getNode(0).getPos();

    Vec2f A = getNode(0).getPos();
    Vec2f B = A;
    m_node1 = getNode(0);
    m_node2 = getNode(1);

    if (loc < 0)
      return A;

    float accLen = 0;
    for (int i = 1; i < getNumNodes(); ++i) {
      B = getNode(i).getPos();
      m_node1 = getNode(i - 1);
      m_node2 = getNode(i);

      float segLen = A.distanceTo(B);
      accLen += segLen;
      if (accLen >= loc) {
        // degenerate case
        if (segLen == 0)
          return new Vec2f(A.x, A.y);

        float t = (loc - accLen + segLen)/segLen;
        Vec2f delta = new Vec2f(B.x - A.x, B.y - A.y);
        return new Vec2f(A.x + t*delta.x, A.y + t*delta.y);
      }
      A = B;
    }
    return B;
  }

  public GraphLocation traverseDistanceEx(float loc) {
    GraphLocation res = new GraphLocation();
    res.setPoint(getNode(0).getPos());

    if (getNumNodes() < 2) {
      return res;
    }

    Vec2f A = getNode(0).getPos();
    Vec2f B = A;
    m_node1 = getNode(0);
    m_node2 = getNode(1);

    loc = Math.max(0, loc);

    float accLen = 0;
    float segLen = 0;
    for (int i = 1; i < getNumNodes(); ++i) {
      B = getNode(i).getPos();
      m_node1 = getNode(i - 1);
      m_node2 = getNode(i);
      GraphEdge edge = m_graph.findEdge(m_node1, m_node2);
      segLen = edge.getLength();
      accLen += segLen;
      if (accLen + 0.0001f >= loc) {
        // degenerate case
        if (segLen == 0) {
          res.setPoint(new Vec2f(A.x, A.y));
          res.setDistanceUptoEdge(accLen);
          res.setDistanceOverEdge(0);
          res.setDistance(accLen);
          res.setEdge(edge);
          return res;
        }

        float t = (loc - accLen + segLen)/segLen;
        Vec2f delta = new Vec2f(B.x - A.x, B.y - A.y);

        res.setPoint(new Vec2f(A.x + t*delta.x, A.y + t*delta.y));
        res.setDistanceUptoEdge(accLen - segLen);
        res.setDistanceOverEdge(loc - res.getDistanceUptoEdge());
        res.setDistance(res.getDistanceOverEdge() + res.getDistanceUptoEdge());
        res.setEdge(edge);
        return res;
      }
      A = B;
    }

    res.setPoint(B);
    res.setDistanceUptoEdge(accLen - segLen);
    res.setDistanceOverEdge(segLen);
    res.setDistance(accLen);
    res.setEdge(m_graph.findEdge(m_node1, m_node2));
    return res;
  }

  public Vec2f traversePercentage(float per) {
    float len = getLength();
    if (len == 0)
      return getNode(0).getPos();
    return traverseDistance(per*len);
  }

  //  public IGraphPath trim(float maxDistance) {
  //    Pointf A = getNode(0).pos;
  //    Pointf B = A;
  //
  //    IGraphPath res = new GraphPath(m_graph);
  //    res.add(getNode(0));
  //
  //    float accLen = 0;
  //    for (int i = 1; i < getNumNodes(); ++i) {
  //      res.add(getNode(i));
  //      B = getNode(i).pos;
  //
  //      float segLen = A.distanceTo(B);
  //      accLen += segLen;
  //
  //      if (accLen >= maxDistance)
  //        break;
  //      A = B;
  //    }
  //    return res;
  //  }

  //  public IGraphPath trim(float min, float max) {
  //    GraphLocation graphLoc = traverseDistanceEx(min);
  //    return chopUpto(graphLoc.edge.A).trim(max - min + graphLoc.distanceOverEdge);
  //  }

  public IGraphPath trim(float min, float max) {
    if (min >= m_length)
      return new GraphPath(m_graph);

    int a, b;
    int beg = -1, end = -1;

    float accLen = 0;
    a = 0;
    int numEdges = getNumNodes() - 1;
    while (a < numEdges) {
      b = a + 1;
      GraphEdge edge = m_graph.findEdge(getNode(a), getNode(b));
      float edgeLen = edge.getLength();
      if (beg == -1 && accLen + edgeLen >= min) {
        beg = a;
      } else if (end == -1 && accLen + edgeLen > max + 0.0001f) {
        end = b;
        break;
      }
      accLen += edgeLen;
      a = b;
    }

    if (beg == -1)
      beg = 0;
    if (end == -1)
      end = getNumNodes() - 1;

    GraphPath res = new GraphPath(m_graph);
    res.m_nodes = m_nodes;
    res.m_nodesOffset = m_nodesOffset + beg;
    res.m_nodesLength = end - beg + 1;
    res.m_length = res.calculateLength();

    return res;
  }

  void add(int index, Node node) {
    m_tempNodes.add(index, node);
  }

  void add(Node node) {
    m_tempNodes.add(node);
  }

  void cache() {
    m_nodes = new Node[m_tempNodes.size()];
    for (int i = 0; i < m_tempNodes.size(); ++i) {
      Node n = m_tempNodes.get(i);
      m_nodes[i] = n;
    }

    m_nodesOffset = 0;
    m_nodesLength = m_nodes.length;

    m_length = calculateLength();
    updateNodeLocations();
  }
}