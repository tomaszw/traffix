/*
 * Created on 2004-08-13
 */

package traffix.core.sim.graph;

import java.util.*;

import org.tw.geometry.*;
import org.tw.geometry.GeometryKit.SegmentP;
import org.tw.patterns.observer.UpdateableObj;
import org.tw.web.XmlKit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import traffix.Traffix;
import traffix.core.model.IPersistent;
import traffix.core.sim.Route;
import traffix.core.sim.entities.IEntity;
import traffix.core.sim.entities.Mobile;

public class Graph extends UpdateableObj implements IPersistent {

  public static final int X_SECTORS       = 100;
  public static final int Y_SECTORS       = 100;

  private GraphEdge[]          m_edgeArray     = new GraphEdge[0];
  private Map<Pair, GraphEdge> m_edgeCache     = new HashMap<Pair, GraphEdge>();

  private List<GraphEdge>      m_edges         = new ArrayList<GraphEdge>();
  private List<IEntity>   m_intersections = new ArrayList<IEntity>();
  private List<Node>      m_nodes         = new ArrayList<Node>();
  private Sector[][]      m_sectors       = new Sector[Y_SECTORS][X_SECTORS];

  public static interface UpdateListener {
    void update();
  }

  static class Pair {
    Object a, b;

    public Pair() {
    }

    public Pair(Object a, Object b) {
      this.a = a;
      this.b = b;
    }

    public boolean equals(Object arg0) {
      if (!(arg0 instanceof Pair))
        return false;
      Pair p = (Pair) arg0;
      return a == p.a && b == p.b;
    }

    public int hashCode() {
      return 27 * a.hashCode() + b.hashCode();
    }
  }

  static class Sector {
    GraphEdge[]    edges      = new GraphEdge[0];
    Set<GraphEdge> setOfEdges = new HashSet<GraphEdge>();
  }

  public Graph() {
    for (int i = 0; i < m_sectors.length; i++) {
      for (int j = 0; j < m_sectors[i].length; j++) {
        m_sectors[i][j] = new Sector();
      }
    }
  }

  public void addEdge(Node a, Node b) {
    for (int i = 0; i < a.getNumOutgoingNodes(); ++i) {
      if (a.getOutgoingNode(i) == b)
        return; // bla
    }
    if (!m_nodes.contains(a))
      m_nodes.add(a);
    if (!m_nodes.contains(b))
      m_nodes.add(b);
    link(a, b);

    updateIntersections();
    updateSectors();

    Set<Node> begnodes = findBeginningNodesFor(b);
    for (Node n : begnodes)
      n.updateRoutes();
  }

  public void addNewNodeAt(GraphLocation loc) {
    GraphEdge e = loc.getEdge();
    Node n = new Node(this);
    m_nodes.add(n);

    
//    n.setPos(e.B.getPos());
//    e.B.setPos(loc.p);
//    link(e.B, n);
    
    n.setPos(loc.getPoint());
    unlink(e.A, e.B);
    link(e.A, n);
    link(n, e.B);

    // update();

  }

  public void addNode(Node n) {
    if (!m_nodes.contains(n)) {
      n.m_graph = this;
      m_nodes.add(n);
      // update();
    }
  }

  public IGraphPath buildPath(List nodes) {
    GraphPath path = new GraphPath(this);
    for (Iterator iter = nodes.iterator(); iter.hasNext();) {
      Node n = (Node) iter.next();
      path.add(n);
    }
    path.cache();
    return path;
  }

  public IGraphPath buildPath(Node[] nodes) {
    return buildPath(Arrays.asList(nodes));
  }

  public GraphLocation closestLocation(Vec2f to, float tolerance) {
    float minDist = Float.POSITIVE_INFINITY;
    GraphLocation loc = new GraphLocation();
    for (Iterator<GraphEdge> iter = m_edges.iterator(); iter.hasNext();) {
      GraphEdge edge = iter.next();
      Vec2f a = edge.A.getPos();
      Vec2f b = edge.B.getPos();
      float d = GeometryKit.distanceOfLineToPoint(a.x, a.y, b.x, b.y, to.x, to.y);
      if (d < minDist) {
        minDist = d;
        Vec2f d1 = edge.getDirection();
        Vec2f d2 = to.sub(a);
        loc.setEdge(edge);
        loc.setDistanceOverEdge(d1.dot(d2));
        loc.setPoint(a.add(d1.mul(loc.getDistanceOverEdge())));
      }
    }
    return minDist < tolerance ? loc : null;
  }

  public void deleteNode(Node node) {
    deleteNodeWithoutUpdate(node);
    update();
  }

  public void deleteRoute(Node node, int numRoute) {
    Route route = node.getRoutesFromNode().get(numRoute);
    node.delRoute(numRoute);
    // List<Route> nodeRoutes = node.getRoutesFromNode();
    List<Route> allRoutes = computeAllRoutes();
    IGraphPath path = route.path();
    for (int i = 0; i < path.getNumNodes() - 1; ++i) {
      Node a = path.getNode(i);
      Node b = path.getNode(i+1);
      
      boolean allowRem = true;
      for (Route r : allRoutes) {
        if (r.path().containsEdge(a, b)) {
          allowRem = false;
          break;
        }
      }
      if (allowRem) {
        removeEdge(a, b);
        a.delOutgoingNode(b);
        b.delIncomingNode(a);
      }
    }
    
    // delete isolated nodes
    Iterator<Node> iter = m_nodes.iterator();
    while (iter.hasNext()) {
      Node n = iter.next();
      if (n.getNumIncomingNodes() == 0 && n.getNumOutgoingNodes() == 0)
        iter.remove();
    }
    
    // for (int i = 0; i < route.getNumNodes(); ++i) {
    // Node n = route.path().getNode(i);
    // boolean allowRemove = true;
    //
    // for (Route r : nodeRoutes) {
    // if (r.containsNode(n)) {
    // allowRemove = false;
    // break;
    // }
    // }
    // if (allowRemove)
    // deleteNodeWithoutUpdate(n);
    // }
    update();
  }

  private List<Route> computeAllRoutes() {
    List<Route> allRoutes = new ArrayList<Route>();
    for (Node n : m_nodes) {
      if (n.isBeginningNode())
        allRoutes.addAll(n.getRoutesFromNode());
    }
    return allRoutes;
  }

  public GraphEdge findEdge(Node n, Node n2) {
    return m_edgeCache.get(new Pair(n, n2));
  }

  public List<GraphEdge> findOutgoingEdges(Node n) {
    List<GraphEdge> res = new LinkedList<GraphEdge>();
    for (int i = 0; i < n.getNumOutgoingNodes(); ++i) {
      Node n2 = n.getOutgoingNode(i);
      GraphEdge edge = findEdge(n, n2);
      if (edge == null)
        throw new IllegalStateException();
      res.add(edge);
    }
    return res;
  }

  public Iterator<GraphEdge> getEdgeIterator() {
    return m_edges.iterator();
  }

  public List<IEntity> getIntersections() {
    return m_intersections;
  }

  public Node getNode(int index) {
    return m_nodes.get(index);
  }

  public int getNodeIndex(Node n) {
    return m_nodes.indexOf(n);
  }

  public Iterator<Node> getNodeIterator() {
    return m_nodes.iterator();
  }

  public int getNumNodes() {
    return m_nodes.size();
  }

  public String getXmlTagName() {
    return "graph";
  }

  public List<GraphLocation> intersectMobileBounds(Mobile m) {
    List<GraphLocation> locs = new ArrayList<GraphLocation>();

    Polygonf poly = m.getBounds();
    Rectanglef clipRc = m.getBoundingRect();
    float[] segLengths = m.getBoundingEdgeLens();

    Set<GraphEdge> edges = getIntersectingEdges(clipRc);
    SegmentP p = new SegmentP();
    p.p = new Vec2f();

    for (GraphEdge edge : edges) {
      Vec2f a = edge.A.getPos();
      Vec2f b = edge.B.getPos();
      float edgelen = edge.getLength();

      for (int s = 0; s < 4; ++s) {
        if (GeometryKit.intersectSegments(a, b, poly.getPoint(s), poly
            .getPoint((s + 1) % 4), edgelen, segLengths[s], p)) {
          float t = p.t;

          GraphLocation loc = new GraphLocation();
          loc.setDistanceOverEdge(t);
          loc.setDistance(t);
          loc.setEdge(edge);
          loc.setPoint(p.p);
          locs.add(loc);
        }
      }
    }
    return locs;
  }

  public List<GraphLocation> intersectPoly(Polygonf poly) {
    List<GraphLocation> locs = new ArrayList<GraphLocation>();
    Rectanglef clipRc = poly.getBoundingRect();
    Segmentf edgeSeg = new Segmentf();
    float[] segLengths = new float[poly.getNumSides()];
    Segmentf[] segs = new Segmentf[poly.getNumSides()];
    for (int i = 0; i < segLengths.length; i++) {
      segs[i] = new Segmentf();
      segs[i].a = poly.getPoint(i);
      segs[i].b = poly.getPoint((i + 1) % segs.length);
      segLengths[i] = segs[i].length();
    }

    Set<GraphEdge> edges = getIntersectingEdges(clipRc);
    for (Iterator<GraphEdge> iter = edges.iterator(); iter.hasNext();) {
      GraphEdge edge = iter.next();

      edgeSeg.a = edge.A.getPos();
      edgeSeg.b = edge.B.getPos();
      float edgelen = edge.getLength();

      for (int s = 0; s < segs.length; ++s) {
        GeometryKit.SegmentP p = GeometryKit.intersectSegments(edgeSeg, segs[s], edgelen,
            segLengths[s]);
        if (p != null) {
          float t = p.t;

          GraphLocation loc = new GraphLocation();
          loc.setDistanceOverEdge(t);
          loc.setDistance(t);
          loc.setEdge(edge);
          loc.setPoint(p.p);
          locs.add(loc);
        }
      }
    }
    return locs;
  }

  public List<GraphLocation> intersectSegment(Segmentf seg, Rectanglef clipRc) {
    List<GraphLocation> locs = new LinkedList<GraphLocation>();
    Segmentf edgeSeg = new Segmentf();
    float seglen = seg.length();
    int numInters = 0;

    Set<GraphEdge> edges = null;
    if (clipRc != null)
      edges = getIntersectingEdges(clipRc);
    else
      edges = new HashSet<GraphEdge>(m_edges);
    for (Iterator<GraphEdge> iter = edges.iterator(); iter.hasNext();) {
      GraphEdge edge = iter.next();

      edgeSeg.a = edge.A.getPos();
      edgeSeg.b = edge.B.getPos();
      float edgelen = edge.getLength();

      GeometryKit.SegmentP p = GeometryKit.intersectSegments(edgeSeg, seg, edgelen,
          seglen);
      if (p != null) {
        ++numInters;
        float t = p.t;

        GraphLocation loc = new GraphLocation();
        loc.setDistanceOverEdge(t);
        loc.setDistance(t);
        loc.setEdge(edge);
        loc.setPoint(p.p);
        locs.add(loc);
      }
    }

    return locs;
  }

  public void link(Node a, Node b) {
    if (!m_nodes.contains(a) || !m_nodes.contains(b))
      throw new IllegalStateException();
    GraphEdge newEdge = new GraphEdge(this, a, b);
    for (Iterator<GraphEdge> iter = m_edges.iterator(); iter.hasNext();) {
      GraphEdge e = iter.next();
      if (e.equals(newEdge))
        return; // already linked
    }
    m_edges.add(newEdge);
    a.addOutgoingNode(b);
    b.addIncomingNode(a);

    updateEdgeCache();

    // update();
  }

  public void markNodes(int mark) {
    for (Iterator<Node> iter = m_nodes.iterator(); iter.hasNext();) {
      Node n = iter.next();
      n.m_mark = mark;
    }
  }

  public void update() {
    updateEdgeCache();
    updateIntersections();
    updateSectors();

    for (Iterator<Node> iter = m_nodes.iterator(); iter.hasNext();) {
      Node n = iter.next();
      if (n.isBeginningNode())
        n.updateRoutes();
    }
    fireUpdated();
  }

  public boolean xmlLoad(Document document, Element root) {
    m_nodes.clear();
    m_edges.clear();

    Element[] nodesElem = XmlKit.childElems(root, "nodes");
    if (nodesElem.length != 1)
      return false;
    Element[] nodeElems = XmlKit.childElems(nodesElem[0], "node");
    for (int i = 0; i < nodeElems.length; ++i) {
      Node n = new Node(this);
      m_nodes.add(n);
    }
    
    for (int i = 0; i < nodeElems.length; ++i) {
      if (!getNode(i).xmlLoad(document, nodeElems[i]))
        return false;
    }

    Element edgesElem = XmlKit.firstChild(root, "edges");
    if (edgesElem == null)
      return false;

    Element[] edgeElems = XmlKit.childElems(edgesElem, "edge");
    for (int i = 0; i < edgeElems.length; i++) {
      GraphEdge e = new GraphEdge(this);
      if (!e.xmlLoad(document, edgeElems[i]))
        return false;
      boolean contains = false;
      for (GraphEdge e2 : m_edges) {
        if (e.equals(e2)) {
          contains = true;
          break;
        }
      }
      if (!contains) {
        m_edges.add(e);
      }
    }

    // Collections.shuffle(m_edges);

    update();

    return true;
  }

  public Element xmlSave(Document document) {
    Element root = document.createElement("graph");
    Element nodesElem = document.createElement("nodes");
    root.appendChild(nodesElem);

    for (int i = 0; i < m_nodes.size(); ++i) {
      Element nodeElem = getNode(i).xmlSave(document);
      nodesElem.appendChild(nodeElem);
    }

    Element edgesElem = document.createElement("edges");
    root.appendChild(edgesElem);

    for (int i = 0; i < m_edges.size(); ++i) {
      GraphEdge edge = m_edges.get(i);
      Element edgeElem = edge.xmlSave(document);
      edgesElem.appendChild(edgeElem);
    }
    return root;
  }

  private void deleteNodeWithoutUpdate(Node node) {
    for (int i = 0; i < node.getNumIncomingNodes(); ++i) {
      for (int j = 0; j < node.getNumOutgoingNodes(); ++j) {
        link(node.getIncomingNode(i), node.getOutgoingNode(j));
      }
    }

    m_nodes.remove(node);
    unlink(node);
  }

  private Set<Node> findBeginningNodesFor(Node n) {
    Set<Node> res = new HashSet<Node>();
    Set<Node> vis = new HashSet<Node>();
    findBeginningNodesFor_aux(n, res, vis);
    return res;
  }

  private void findBeginningNodesFor_aux(Node n, Set<Node> found, Set<Node> visited) {
    if (visited.contains(n))
      return;
    visited.add(n);
    if (n.isBeginningNode())
      found.add(n);
    for (int i = 0; i < n.getNumIncomingNodes(); ++i)
      findBeginningNodesFor_aux(n.getIncomingNode(i), found, visited);
  }

  private void findEdgeSectors() {
    Vec2f sz = Traffix.simManager().getTerrainDims();
    float dimx = sz.x / X_SECTORS;
    float dimy = sz.y / Y_SECTORS;
    Vec2f min = new Vec2f(), max = new Vec2f();
    for (Iterator<GraphEdge> iter = m_edges.iterator(); iter.hasNext();) {
      GraphEdge e = iter.next();
      Vec2f a = e.A.getPos();
      Vec2f b = e.B.getPos();

      min.x = Math.min(a.x, b.x);
      min.y = Math.min(a.y, b.y);
      max.x = Math.max(a.x, b.x);
      max.y = Math.max(a.y, b.y);

      int minsx = (int) (min.x / dimx);
      int maxsx = (int) (max.x / dimx);
      int minsy = (int) (min.y / dimy);
      int maxsy = (int) (max.y / dimy);

      minsx = Math.min(minsx, X_SECTORS-1);
      maxsx = Math.min(maxsx, X_SECTORS-1);
      
      minsy = Math.min(minsy, Y_SECTORS-1);
      maxsy = Math.min(maxsy, Y_SECTORS-1);
      
      for (int sy = minsy; sy <= maxsy; ++sy) {
        for (int sx = minsx; sx <= maxsx; ++sx) {
          float px = sx * dimx;
          float py = sy * dimy;

          m_sectors[sy][sx].setOfEdges.add(e);
        }
      }
    }
  }

  private Set<GraphEdge> getIntersectingEdges(Rectanglef rc) {
    Set<GraphEdge> set = new HashSet<GraphEdge>();
    Vec2f sz = Traffix.simManager().getTerrainDims();
    float dimx = sz.x / X_SECTORS;
    float dimy = sz.y / Y_SECTORS;

    int minsx = (int) (rc.x / dimx);
    int maxsx = (int) (Math.ceil((rc.x + rc.width) / dimx));
    int minsy = (int) (rc.y / dimy);
    int maxsy = (int) (Math.ceil((rc.y + rc.height) / dimy));

    if (minsx < 0)
      minsx = 0;
    else if (minsx >= X_SECTORS)
      minsx = X_SECTORS - 1;
    if (maxsx < 0)
      maxsx = 0;
    else if (maxsx >= X_SECTORS)
      maxsx = X_SECTORS - 1;

    if (minsy < 0)
      minsy = 0;
    else if (minsy >= Y_SECTORS)
      minsy = Y_SECTORS - 1;
    if (maxsy < 0)
      maxsy = 0;
    else if (maxsy >= Y_SECTORS)
      maxsy = Y_SECTORS - 1;

    for (int sy = minsy; sy <= maxsy; ++sy) {
      for (int sx = minsx; sx <= maxsx; ++sx) {
        set.addAll(m_sectors[sy][sx].setOfEdges);
      }
    }
    return set;
  }

  private void removeEdge(Node a, Node b) {
    for (Iterator<GraphEdge> iter = m_edges.iterator(); iter.hasNext();) {
      GraphEdge e = iter.next();
      if (e.A == a && e.B == b)
        iter.remove();
    }
  }

  private void unlink(Node node) {
    for (int i = 0; i < node.getNumIncomingNodes(); ++i) {
      removeEdge(node.getIncomingNode(i), node);
    }
    for (int i = 0; i < node.getNumOutgoingNodes(); ++i) {
      removeEdge(node, node.getOutgoingNode(i));
    }

    for (int i = 0; i < node.getNumIncomingNodes(); ++i) {
      node.getIncomingNode(i).delOutgoingNode(node);
    }
    for (int i = 0; i < node.getNumOutgoingNodes(); ++i) {
      node.getOutgoingNode(i).delIncomingNode(node);
    }
  }

  private void unlink(Node a, Node b) {
    removeEdge(a, b);
    a.delOutgoingNode(b);
    b.delIncomingNode(a);
  }

  private void updateEdgeCache() {
    m_edgeCache.clear();
    for (Iterator<GraphEdge> iter = m_edges.iterator(); iter.hasNext();) {
      GraphEdge e = iter.next();
      Pair pair = new Pair();
      pair.a = e.A;
      pair.b = e.B;
      m_edgeCache.put(pair, e);
      e.update();
    }
    m_edgeArray = m_edges.toArray(new GraphEdge[m_edges.size()]);
  }

  private void updateIntersections() {
    Set<GraphEdge> done = new HashSet<GraphEdge>();
    for (Iterator<IEntity> iter = m_intersections.iterator(); iter.hasNext();) {
      Intersection inter = (Intersection) iter.next();
      inter.dispose();
    }
    m_intersections.clear();
    Rectanglef clip = new Rectanglef();
    Segmentf s1 = new Segmentf();
    Segmentf s2 = new Segmentf();
    for (Iterator<GraphEdge> iter = m_edges.iterator(); iter.hasNext();) {
      GraphEdge e1 = iter.next();
      for (Iterator<GraphEdge> iterator = m_edges.iterator(); iterator.hasNext();) {
        GraphEdge e2 = iterator.next();

        if (done.contains(e2) || e1.B == e2.A || e2.B == e1.A || e1.A == e2.A
            || e2.B == e1.B)
          continue;

        done.add(e1);
        s1.a = e1.A.getPos();
        s1.b = e1.B.getPos();
        s2.a = e2.A.getPos();
        s2.b = e2.B.getPos();
        if (e1 != e2) {
          GeometryKit.SegmentP p1 = GeometryKit.intersectSegments(s1, s2);
          if (p1 != null) {
            GeometryKit.SegmentP p2 = GeometryKit.intersectSegments(s2, s1);

            if (p2 == null) {
              continue;
            }

            Intersection is = new Intersection();
            is.l1 = new GraphLocation();
            is.l2 = new GraphLocation();
            is.l1.setDistanceOverEdge(p1.t);
            is.l2.setDistanceOverEdge(p2.t);
            is.l1.setPoint(p1.p);
            is.l2.setPoint(p2.p);
            is.l1.setEdge(e1);
            is.l2.setEdge(e2);
            is.update();
            m_intersections.add(is);
          }
        }
      }
    }
  }

  private void updateSectors() {
    Vec2f sz = Traffix.simManager().getTerrainDims();
    float dimx = sz.x / X_SECTORS;
    float dimy = sz.y / Y_SECTORS;

    m_sectors = new Sector[Y_SECTORS][X_SECTORS];

    for (int y = 0; y < Y_SECTORS; ++y) {
      for (int x = 0; x < X_SECTORS; ++x) {
        Sector s = new Sector();
        m_sectors[y][x] = s;
        s.setOfEdges = new HashSet<GraphEdge>();
      }
    }
    findEdgeSectors();
    for (int y = 0; y < Y_SECTORS; ++y) {
      for (int x = 0; x < X_SECTORS; ++x) {
        m_sectors[y][x].edges = m_sectors[y][x].setOfEdges
            .toArray(new GraphEdge[m_sectors[y][x].setOfEdges.size()]);
      }
    }
  }
}