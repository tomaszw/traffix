/*
 * Created on 2004-08-13
 */

package traffix.core.sim.graph;

import org.tw.geometry.Vec2f;
import org.tw.geometry.Polygonf;
import org.tw.web.XmlKit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import traffix.Traffix;
import traffix.core.VehicleGroup;
import traffix.core.model.IPersistent;
import traffix.core.sim.IObstacle;
import traffix.core.sim.Route;
import traffix.core.sim.RouteInfo;
import traffix.core.sim.entities.IMobile;
import traffix.core.sim.entities.Mobile;

import java.util.*;

public class Node implements IPersistent, IObstacle {
  public Vec2f            labelPos          = new Vec2f(0, 0);
  public int              numWaitingMobiles = 0;
  Graph                   m_graph;
  int                     m_mark;
  private Mobile          m_approachingMobile;
  private List<Node>      m_incomingNodes   = new ArrayList<Node>();
  private String          m_name            = null;
  private List<Node>      m_outgoingNodes   = new ArrayList<Node>();
  private Vec2f           m_pos             = new Vec2f(0, 0);
  // private List<RouteInfo> m_routeInfos = new ArrayList<RouteInfo>();
  private List<Route>     m_routes          = new ArrayList<Route>();
  private List<Integer>   m_startingGroups  = new ArrayList<Integer>(); // List of
  private List<RouteInfo> m_routeTmpInfos   = null;

  public static Node fromIndex(int index) {
    return Traffix.simManager().getGraph().getNode(index);
  }

  public Node(Graph graph) {
    m_graph = graph;
  }

  public void addIncomingNode(Node node) {
    if (!m_incomingNodes.contains(node))
      m_incomingNodes.add(node);
  }

  public void addOutgoingNode(Node node) {
    if (!m_outgoingNodes.contains(node))
      m_outgoingNodes.add(node);
  }

  public void delIncomingNode(Node node) {
    m_incomingNodes.remove(node);
  }

  public void delOutgoingNode(Node node) {
    m_outgoingNodes.remove(node);
  }

  public void delRoute(int numRoute) {
    m_routes.remove(numRoute);
  }

  public Polygonf getBounds() {
    return null;
  }

  public Set<VehicleGroup> /* of Group */getIncomingGroups() {
    m_graph.markNodes(0);
    return getIncomingGroupsInternal();
  }

  public Node getIncomingNode(int i) {
    return m_incomingNodes.get(i);
  }

  public int getIndex() {
    return m_graph.getNodeIndex(this);
  }

  // Integer
  public String getName() {
    return m_name;
  }

  public int getNumIncomingNodes() {
    return m_incomingNodes.size();
  }

  public int getNumOutgoingNodes() {
    return m_outgoingNodes.size();
  }

  public GraphLocation[] getObstacleGraphLocations() {
    GraphLocation[] locs = new GraphLocation[getNumOutgoingNodes()];
    for (int i = 0; i < locs.length; ++i) {
      Node n = getOutgoingNode(i);
      GraphEdge e = m_graph.findEdge(this, n);
      locs[i] = new GraphLocation();
      locs[i].setEdge(e);
      locs[i].setDistanceOverEdge(0);
      locs[i].setPoint(e.A.m_pos.copy());
    }
    return locs;
  }

  public float getObstacleRadius() {
    return 3;
  }

  public Node getOutgoingNode(int i) {
    return m_outgoingNodes.get(i);
  }

  public IGraphPath[] getOutgoingPaths(float maxDistance) {
    List<List<Node>> pnodes = getOutgoingPathsInternal(maxDistance);
    IGraphPath[] paths = new GraphPath[pnodes.size()];
    for (int i = 0; i < paths.length; i++) {
      paths[i] = m_graph.buildPath(pnodes.get(i));
    }
    return paths;
  }

  public Vec2f getPos() {
    return m_pos;
  }

  public List<Route> getRoutesFromNode() {
    assert (isBeginningNode() == true);
    return m_routes;
  }

  public String getXmlTagName() {
    return "node";
  }

  public boolean isBeginningNode() {
    return getNumIncomingNodes() == 0;
  }

  public void notifyApproach(IMobile mobile, float remainingDist) {
  }

  public void notifyPass(IMobile mobile) {
  }

  public void setName(String name) {
    m_name = name;
  }

  public void setPos(Vec2f pos) {
    Vec2f delta = pos.sub(m_pos);
    m_pos = pos;
    labelPos = labelPos.add(delta);

  }

  public void setRoutes(List<Route> routes) {
    assert (isBeginningNode() == true);
    m_routes = routes;
    // updateRouteInfos();
  }

  public boolean shouldBlock(IMobile mobile) {
    float minTime = Float.POSITIVE_INFINITY;
    IMobile chosenOne = null;
    for (Iterator<IMobile> iter = Traffix.simManager().getMobileIterator(); iter
        .hasNext();) {
      IMobile m = iter.next();
      float t = m.approxNodeImpactTime(this);
      if (t < minTime) {
        minTime = t;
        chosenOne = m;
      }
    }
    if (chosenOne == null)
      return false;
    return mobile != chosenOne;
  }

  // public void updateRouteInfos() {
  // if (!isBeginningNode()) {
  // m_routeInfos.clear();
  // return;
  // }
  //
  // for (int i = 0; i < m_routes.size(); ++i) {
  // if (i < m_routeInfos.size()) {
  // (m_routeInfos.get(i)).assign((m_routes.get(i)).getInfo());
  // } else {
  // m_routeInfos.add((m_routes.get(i)).getInfo());
  // }
  // }
  // }

  private boolean sameNodes(IGraphPath a, IGraphPath b) {
    if (a.getNumNodes() != b.getNumNodes())
      return false;
    for (int i = 0; i < a.getNumNodes(); ++i)
      if (a.getNode(i) != b.getNode(i))
        return false;
    return true;
  }

  private int findSamePathRoute(IGraphPath p) {
    for (int i = 0; i < m_routes.size(); ++i) {
      if (sameNodes(p, m_routes.get(i).path()))
        return i;
    }
    return -1;
  }

  private Route findNamedRoute(String name) {
    for (Route r : m_routes)
      if (r.getInfo().name != null && r.getInfo().name.equals(name))
        return r;
    return null;
  }

  private boolean canCopyRouteInfo(IGraphPath pa, IGraphPath pb) {
    // can we copy route info of path pb to pa?

    // we assume we do when the routes are more or less the same - they can differ
    // on one position or so
    int ia = 0;
    int ib = 0;
    if (pa.getNumNodes() > pb.getNumNodes()) {
      int skipped = 0;
      while (ia < pa.getNumNodes() && ib < pb.getNumNodes()) {
        Node na = pa.getNode(ia);
        Node nb = pb.getNode(ib);
        if (na == nb) {
          ++ia;
          ++ib;
          skipped = 0;
        } else {
          ++skipped;
          if (skipped > 1)
            return false;
          ++ia;
        }
      }
      return true;
    } else if (pa.getNumNodes() < pb.getNumNodes()) {
      int skipped = 0;
      while (ia < pa.getNumNodes() && ib < pb.getNumNodes()) {
        Node na = pa.getNode(ia);
        Node nb = pb.getNode(ib);
        if (na == nb) {
          ++ia;
          ++ib;
          skipped = 0;
        } else {
          ++skipped;
          if (skipped > 1)
            return false;
          ++ib;
        }
      }
      return true;
    } else {
      while (ia < pa.getNumNodes()) {
        if (pa.getNode(ia) != pb.getNode(ia))
          return false;
        ++ia;
      }
      return true;
    }

  }

  public void updateRoutes() {
    if (!isBeginningNode())
      return;
    List<Route> newRoutes = new ArrayList<Route>();
    IGraphPath[] newPaths = getOutgoingPaths(Float.POSITIVE_INFINITY);
    for (int i = 0; i < m_routes.size(); ++i) {
      for (int j = i; j < newPaths.length; ++j) {
        if (newPaths[j] != null) {
          if (canCopyRouteInfo(newPaths[j], m_routes.get(i).path())) {
            Route r = Route.createFromPath(newPaths[j]);
            r.setInfo(m_routes.get(i).getInfo());
            newRoutes.add(r);
            newPaths[j] = null;
          }
        }
      }
    }
    for (int i = 0; i < newPaths.length; ++i) {
      if (newPaths[i] != null) {
        Route r = Route.createFromPath(newPaths[i]);
        newRoutes.add(r);
      }
    }

    m_routes = newRoutes;
    // first time through after load, set route infos from those that were read
    if (m_routeTmpInfos != null) {
      for (int i = 0; i < m_routes.size() && i < m_routeTmpInfos.size(); ++i)
        m_routes.get(i).setInfo(m_routeTmpInfos.get(i));
      m_routeTmpInfos = null;
    }

    for (Route r : m_routes)
      r.update();
  }
  public void updateRoutes_old() {
    boolean first = m_routeTmpInfos != null;
    // for (int i=0; i<m_routes.size(); ++i)
    // m_routes.get(i).setInfo(m_routeInfos.get(i));
    List<Route> newRoutes = new ArrayList<Route>();
    if (isBeginningNode()) {
      IGraphPath[] newPaths = getOutgoingPaths(Float.POSITIVE_INFINITY);
      boolean sameNum = newPaths.length == m_routes.size();
      for (int j = 0; j < newPaths.length; j++) {
        Route r = Route.createFromPath(newPaths[j]);

        if (first) {
          r.setInfo(m_routeTmpInfos.get(j));
          newRoutes.add(r);
          continue;
        }

        if (!sameNum) {
          int samePidx = findSamePathRoute(r.path());
          if (samePidx != -1) {
            RouteInfo ri = new RouteInfo();
            ri.assign(m_routes.get(samePidx).getInfo());
            r.setInfo(ri);
          } else {
            RouteInfo ri = new RouteInfo();
            r.setInfo(ri);
          }
        } else {
          if (j < m_routes.size()) {
            RouteInfo ri = new RouteInfo();
            ri.assign(m_routes.get(j).getInfo());
            r.setInfo(ri);
          } else {
            RouteInfo ri = new RouteInfo();
            r.setInfo(ri);
          }
        }
        newRoutes.add(r);
      }
    }
    m_routes = newRoutes;
    // for (int i=0; i<m_routes.size(); ++i)
    // m_routeInfos.set(i, m_routes.get(i).getInfo());
    // updateRouteInfos();
    for (Route r : m_routes)
      r.update();
  }

  public boolean xmlLoad(Document document, Element root) {
    // m_startingGroup = -1;
    m_outgoingNodes.clear();
    m_incomingNodes.clear();
    m_startingGroups.clear();

    try {
      m_pos.x = Float.parseFloat(root.getAttribute("px"));
      m_pos.y = Float.parseFloat(root.getAttribute("py"));

      if (root.hasAttribute("labpx")) {
        labelPos.x = Float.parseFloat(root.getAttribute("labpx"));
        labelPos.y = Float.parseFloat(root.getAttribute("labpy"));
      } else {
        labelPos = m_pos.copy();
      }

      Element[] startGroups = XmlKit.childElems(root, "startGroup");
      for (int i = 0; i < startGroups.length; i++) {
        int id = Integer.parseInt(startGroups[i].getAttribute("i"));
        m_startingGroups.add(new Integer(id));
      }

      Element[] outs = XmlKit.childElems(root, "out");
      for (int i = 0; i < outs.length; ++i)
        addOutgoingNode(m_graph.getNode(Integer.parseInt(outs[i].getAttribute("i"))));

      Element[] ins = XmlKit.childElems(root, "in");
      for (int i = 0; i < ins.length; ++i)
        addIncomingNode(m_graph.getNode(Integer.parseInt(ins[i].getAttribute("i"))));

      // load route infos
      if (isBeginningNode()) {

        Element[] riNodes = XmlKit.childElems(root, "routeInfo");
        if (riNodes.length > 0) {
          m_routeTmpInfos = new ArrayList<RouteInfo>();
        }
        for (int i = 0; i < riNodes.length; ++i) {
          RouteInfo ri = new RouteInfo();
          if (!ri.xmlLoad(document, riNodes[i]))
            return false;
          m_routeTmpInfos.add(ri);
        }
      }

    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  public Element xmlSave(Document document) {
    Element root = document.createElement("node");
    root.setAttribute("px", Float.toString(m_pos.x));
    root.setAttribute("py", Float.toString(m_pos.y));
    root.setAttribute("labpx", Float.toString(labelPos.x));
    root.setAttribute("labpy", Float.toString(labelPos.y));

    for (int i = 0; i < m_startingGroups.size(); ++i) {
      int id = (m_startingGroups.get(i)).intValue();
      Element e = document.createElement("startGroup");
      e.setAttribute("i", Integer.toString(id));
      root.appendChild(e);
    }

    for (int i = 0; i < getNumOutgoingNodes(); ++i) {
      int nodeIndex = m_graph.getNodeIndex(getOutgoingNode(i));
      Element out = document.createElement("out");
      out.setAttribute("i", Integer.toString(nodeIndex));
      root.appendChild(out);
    }

    for (int i = 0; i < getNumIncomingNodes(); ++i) {
      int nodeIndex = m_graph.getNodeIndex(getIncomingNode(i));
      Element in = document.createElement("in");
      in.setAttribute("i", Integer.toString(nodeIndex));
      root.appendChild(in);
    }

    // save route infos
    if (isBeginningNode()) {
      for (Iterator<Route> iter = m_routes.iterator(); iter.hasNext();) {
        RouteInfo r = iter.next().getInfo();
        root.appendChild(r.xmlSave(document));
      }
    }

    return root;
  }

  private Set<VehicleGroup> /* of Group */getIncomingGroupsInternal() {
    Set<VehicleGroup> groups = new HashSet<VehicleGroup>();
    if (m_mark != 0)
      return groups;
    m_mark = 1;
    for (int i = 0; i < getNumIncomingNodes(); ++i) {
      groups.addAll(getIncomingNode(i).getIncomingGroups());
    }
    for (Iterator<Integer> iter = m_startingGroups.iterator(); iter.hasNext();) {
      Integer index = iter.next();
      VehicleGroup g = VehicleGroup.fromUniqueIdent(index.intValue());
      if (g != null)
        groups.add(g);
    }
    return groups;
  }

  private List<List<Node>> getOutgoingPathsInternal(float maxDistance) {
    if (getNumOutgoingNodes() == 0) {
      ArrayList<Node> p = new ArrayList<Node>();
      p.add(this);
      ArrayList<List<Node>> ps = new ArrayList<List<Node>>();
      ps.add(p);
      return ps;
    }
    List<List<Node>> paths = new ArrayList<List<Node>>();
    for (int i = 0; i < getNumOutgoingNodes(); ++i) {
      Node outN = getOutgoingNode(i);
      float edgeLen = m_pos.distanceTo(outN.m_pos);
      if (edgeLen <= maxDistance) {
        List<List<Node>> continuedPaths = outN.getOutgoingPathsInternal(maxDistance
            - edgeLen);
        for (List<Node> p : continuedPaths)
          p.add(0, this);
        paths.addAll(continuedPaths);
      }
    }
    return paths;
  }

  // private List[] getOutgoingPathsInternal(float maxDistance) {
  // if (getNumOutgoingNodes() == 0) {
  // List<Node> p = new LinkedList<Node>();
  // p.add(this);
  // return new List[] { p };
  // }
  //
  // List<List<Node>> paths = new LinkedList<List<Node>>();
  // for (int i = 0; i < getNumOutgoingNodes(); ++i) {
  // Node outgoingNode = getOutgoingNode(i);
  // float edgeLen = m_pos.distanceTo(outgoingNode.m_pos);
  // if (edgeLen <= maxDistance) {
  // List[] nextPaths = outgoingNode.getOutgoingPathsInternal(maxDistance - edgeLen);
  // for (int j = 0; j < nextPaths.length; ++j) {
  // nextPaths[j].add(0, this);
  // }
  // List<List> col = Arrays.asList(nextPaths);
  // paths.addAll((Collection) col);
  // } else {
  // List<Node> newPath = new LinkedList<Node>();
  // newPath.add(this);
  // newPath.add(outgoingNode);
  // paths.add(newPath);
  // }
  // }
  //
  // return paths.toArray(new List[paths.size()]);
  // }
}