/*
 * Created on 2004-08-13
 */

package traffix.ui.sim.entities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.tw.geometry.Vec2f;
import org.tw.patterns.observer.IUpdateListener;
import traffix.Traffix;
import traffix.Utils;
import traffix.core.VehicleGroup;
import traffix.core.VehicleGroupSet;
import traffix.core.model.Model;
import traffix.core.sim.ISimManager;
import traffix.core.sim.Route;
import traffix.core.sim.entities.IEntity;
import traffix.core.sim.graph.GraphEdge;
import traffix.core.sim.graph.Graph;
import traffix.core.sim.graph.Intersection;
import traffix.core.sim.graph.Node;
import traffix.ui.Colors;
import traffix.ui.Gc;
import traffix.ui.sim.CoordinateTransformer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GraphUi extends UiEntity {
  private Color m_clEdgeBg = Colors.get(new RGB(255, 255, 255));
  private Color m_clEdgeFg = Colors.get(new RGB(0, 225, 0));
  private Color m_clNode   = Colors.get(new RGB(0, 0, 0));

  private Graph m_graph;

  GraphUi(IUiEntityContextProvider cp, Graph graph) {
    super(cp);
    m_graph = graph;
  }

  public void dispose() {
  }

  public int getPaintPriority() {
    return getContext().simRunning ? 100 : 0;
  }

  public UiEntityType getType() {
    return UiEntityType.GRAPH;
  }

  public void paint() {
    if (isInEditMode()) {
      paintEdges();
      paintNodes();
      paintIntersections();
    } else {
      if (getContext().informationFrame) {
        paintEdges();
      }
    }

    // if (Traffix.simManager().simParams().showVirtualVehicles
    // || getContext().m_informationFrame)
    // paintVirtualVehiclesCount();
  }

  private void paintEdge(GraphEdge edge) {

    Gc gc = getContext().getGc();

    Vec2f a = edge.A.getPos();

    Vec2f b = edge.B.getPos();

    Point p1 = getContext().getCoordTransformer().terrainToScreen(a);
    Point p2 = getContext().getCoordTransformer().terrainToScreen(b);

    gc.setBackground(m_clEdgeBg);
    gc.setForeground(m_clEdgeFg);
    gc.setLineWidth(3);
    Utils.paintArrow(gc, p1.x, p1.y, p2.x, p2.y, 10.0f, 0.5f);
  }

  private void paintEdges() {
    Iterator<GraphEdge> iter = m_graph.getEdgeIterator();
    while (iter.hasNext())
      paintEdge(iter.next());
  }

  private void paintGroupLabels() {
    ISimManager sm = Traffix.simManager();
    Model model = Traffix.model();
    Gc gc = getContext().getGc();
    gc.setLineWidth(1);
    gc.setLineStyle(SWT.LINE_DOT);

    for (int i = 0; i < m_graph.getNumNodes(); ++i) {
      Node n = m_graph.getNode(i);
      if (!n.isBeginningNode())
        continue;
      VehicleGroupSet groups = new VehicleGroupSet();

      List<Route> routes = sm.getRoutesFromNode(n);
      for (Iterator<Route> iter = routes.iterator(); iter.hasNext();) {
        Route r = iter.next();
        groups.addAll(r.getInfo().getControllingGroups());
      }

      String label = "";
      for (Iterator<VehicleGroup> iter = groups.iterator(); iter.hasNext();) {
        VehicleGroup group = iter.next();
        label += group.getElectricName();
        if (iter.hasNext())
          label += "-";
      }
      if (label.equals(""))
        continue;

      Point p = getContext().getCoordTransformer().terrainToScreen(n.getPos());
      Point sz = gc.textExtent(label);

      gc.setBackground(Colors.get(new RGB(255, 255, 255)));
      gc.setForeground(Colors.get(new RGB(0, 0, 0)));
      gc.fillRoundRectangle(p.x - sz.x / 2 - 4, p.y - sz.y / 2 - 2, sz.x + 8, sz.y + 4,
          12, 12);
      gc.drawRoundRectangle(p.x - sz.x / 2 - 4, p.y - sz.y / 2 - 2, sz.x + 8, sz.y + 4,
          12, 12);
      gc.drawText(label, p.x - sz.x / 2, p.y - sz.y / 2, true);
    }
    gc.setLineStyle(SWT.LINE_SOLID);
  }

  private void paintIntersections() {
    Gc gc = getContext().getGc();
    gc.setBackground(Colors.get(new RGB(255, 0, 0)));
    CoordinateTransformer ct = getContext().getCoordTransformer();

    List<IEntity> inters = m_graph.getIntersections();
    for (Iterator<IEntity> iter = inters.iterator(); iter.hasNext();) {
      Intersection inter = (Intersection) iter.next();
      Point p = ct.terrainToScreen(inter.l1.getPoint());
      gc.fillOval(p.x - 2, p.y - 2, 5, 5);
    }
  }

  private void paintNode(Node node) {
    if (!node.isBeginningNode())
      return;
    if (!isInEditMode())
      return;
    Gc gc = getContext().getGc();
    gc.setBackground(m_clNode);
    gc.setForeground(m_clNode);
    gc.setLineWidth(1);
    gc.setLineStyle(SWT.LINE_SOLID);
    int sz = 3;
    if (node.getNumOutgoingNodes() > 1)
      sz = 4;
    if (node.getNumIncomingNodes() > 1)
      sz = 4;

    Point p = getContext().getCoordTransformer().terrainToScreen(node.getPos());
    gc.fillOval(p.x - sz, p.y - sz, sz * 2 + 1, sz * 2 + 1);

    if (node.isBeginningNode()) {
      gc.drawOval(p.x - sz - 3, p.y - sz - 3, sz * 2 + 6, sz * 2 + 6);
    }
  }

  // private void paintVirtualVehiclesCount() {
  // ISimManager sm = Traffix.simManager();
  // Model model = Traffix.model();
  // Gc gc = getContext().getGc();
  // gc.setLineWidth(1);
  // gc.setLineStyle(SWT.LINE_DOT);
  //
  // int nodenum = 1;
  // for (int i = 0; i < m_graph.getNumNodes(); ++i) {
  // Node n = m_graph.getNode(i);
  // if (!n.isBeginningNode())
  // continue;
  // List routes = sm.getRoutesFromNode(n);
  // int count = 0;
  // for (Iterator iter = routes.iterator(); iter.hasNext();) {
  // Route r = (Route) iter.next();
  // count += r.getInfo().numVirtualVehicles;
  // }
  //
  // float simTime = sm.getCurrentTime();
  // float perHour = 0;
  // if (simTime != 0)
  // perHour = count / simTime * 3600;
  //      
  // String label = nodenum + ": " + Integer.toString(Math.round(perHour)) + "/h";
  // ++nodenum;
  //      
  // Point p = getContext().getCoordTransformer().terrainToScreen(n.pos);
  // Point sz = gc.textExtent(label);
  //
  // gc.setBackground(Colors.get(new RGB(255, 255, 255)));
  // gc.setForeground(Colors.get(new RGB(0, 0, 0)));
  // gc.fillRoundRectangle(p.x - sz.x / 2 - 4, p.y - sz.y / 2 - 2, sz.x + 8, sz.y + 4,
  // 12, 12);
  // gc.drawRoundRectangle(p.x - sz.x / 2 - 4, p.y - sz.y / 2 - 2, sz.x + 8, sz.y + 4,
  // 12, 12);
  // gc.drawText(label, p.x - sz.x / 2, p.y - sz.y / 2, true);
  // }
  // gc.setLineStyle(SWT.LINE_SOLID);
  // }

  private void paintNodes() {
    Iterator<Node> iter = m_graph.getNodeIterator();
    while (iter.hasNext())
      paintNode(iter.next());
  }
}