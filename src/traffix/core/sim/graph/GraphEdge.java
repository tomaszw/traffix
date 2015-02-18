/*
 * Created on 2004-08-13
 */

package traffix.core.sim.graph;

import org.tw.geometry.Vec2f;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import traffix.Traffix;
import traffix.core.model.IPersistent;
import traffix.core.sim.IObstacle;

import java.util.*;

public class GraphEdge implements IPersistent {
  public Node A, B;
  private Vec2f m_dir;
  private float m_length;
  private List<IObstacle> m_obstacles = new ArrayList<IObstacle>();
  private Graph m_graph;
  
  public GraphEdge(Graph graph) {
    m_graph = graph;
  }

  public GraphEdge(Graph graph, Node a, Node b) {
    this(graph);
    A = a;
    B = b;
  }

  public void addObstacle(IObstacle o) {
    if (!m_obstacles.contains(o))
      m_obstacles.add(o);
  }

  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    if (obj == null)
      return false;
    if (obj.getClass() != getClass())
      return false;
    GraphEdge other = (GraphEdge) obj;
    return other.A == A && other.B == B;
  }

  public final Vec2f getDirection() {
    return m_dir;// B.pos.sub(A.pos).div(getLength());
  }

  public final float getLength() {
    return m_length;
  }

  public Iterator<IObstacle> getObstacleIterator() {
    if (A.getNumIncomingNodes() < 2)
      return m_obstacles.iterator();
    else {
      Collection<IObstacle> coll = new LinkedList<IObstacle>(m_obstacles);
      coll.add(A);
      return coll.iterator();
    }
  }

  public String getXmlTagName() {
    return "edge";
  }

  // public float getTravelProbability(Group group) {
  // if (hasSimpleProbability())
  // return m_probability;
  // Float p = (Float) m_probabilityMap.get(group);
  // if (p == null)
  // return 0;
  // return p.floatValue();
  // }

  // public boolean hasSimpleProbability() {
  // return A.m_flowDefType == Node.FLOW_SIMPLIFIED;
  // }

  @Override
  public int hashCode() {
    if (A == null && B == null)
      return super.hashCode();
    int hash = 0;
    if (A != null)
      hash = A.hashCode();
    if (B != null)
      hash = hash ^ B.hashCode();
    return hash;
  }

  public void removeObstacle(IObstacle o) {
    m_obstacles.remove(o);
  }

  public void update() {
    m_length = A.getPos().distanceTo(B.getPos());
    m_dir = B.getPos().sub(A.getPos()).div(getLength());
  }

  public boolean xmlLoad(Document document, Element root) {
    Graph graph = Traffix.simManager().getGraph();
    A = null;
    B = null;
    try {
      A = graph.getNode(Integer.parseInt(root.getAttribute("a")));
      B = graph.getNode(Integer.parseInt(root.getAttribute("b")));
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  public Element xmlSave(Document document) {
    Graph graph = A.m_graph;
    Element root = document.createElement("edge");
    root.setAttribute("a", Integer.toString(graph.getNodeIndex(A)));
    root.setAttribute("b", Integer.toString(graph.getNodeIndex(B)));
    return root;
  }
}