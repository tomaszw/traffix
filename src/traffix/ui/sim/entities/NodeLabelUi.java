/*
 * Created on 2004-08-21
 */

package traffix.ui.sim.entities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import traffix.Traffix;
import traffix.core.sim.ISimManager;
import traffix.core.sim.Route;
import traffix.core.sim.SimParams;
import traffix.core.sim.graph.Node;
import traffix.ui.Colors;
import traffix.ui.Gc;
import traffix.ui.sim.CoordinateTransformer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NodeLabelUi extends UiEntity {
  private Node m_node;
  private int m_nodeId;

  NodeLabelUi(IUiEntityContextProvider cp, Node d, int nodeId) {
    super(cp);
    m_node = d;
    m_nodeId = nodeId;
  }

  public UiEntityType getType() {
    return UiEntityType.NODELABEL;
  }

  public int getPaintPriority() {
    return 200;
  }

  public void paint() {
    super.paint();
    CoordinateTransformer ct = getContext().getCoordTransformer();
    Gc gc = getContext().getGc();

    Point p = getContext().getCoordTransformer().terrainToScreen(m_node.labelPos);

    gc.setLineStyle(SWT.LINE_DOT);
    gc.setLineWidth(1);
    Point np = getContext().getCoordTransformer().terrainToScreen(m_node.getPos());
    gc.drawLine(np.x, np.y, p.x, p.y);
    gc.setLineStyle(SWT.LINE_SOLID);

    List<Route> routes = m_node.getRoutesFromNode();
    int sumVirtual = 0;
    int sumReal = 0;
    float sumStopTime = 0;
    for (Iterator<Route> iter = routes.iterator(); iter.hasNext();) {
      Route r = iter.next();
      sumVirtual += r.getInfo().numArrivedVirtualVehicles;
      for (int j = 0; j < r.getInfo().numArrivedVehicles.length; ++j)
        sumReal += r.getInfo().numArrivedVehicles[j];
      sumStopTime += r.getInfo().summedStopTime;
    }

    ISimManager sm = Traffix.simManager();
    float simTime = sm.getCurrentTime();
    float virtualPerHour = 0;
    if (simTime != 0)
      virtualPerHour = sumVirtual/simTime*3600;
    float avgStopTime = sumStopTime;
    if (sumReal != 0)
      avgStopTime /= sumReal;

    List<String> lines = new ArrayList<String>(5);
    SimParams ps = Traffix.simManager().simParams();

    if (ps.showNodeNumbers) {
      lines.add(Integer.toString(m_nodeId));
    }
    if (ps.showVirtualVehicles) {
      lines.add("pu: " + (int) virtualPerHour + "/h");
    }
    if (ps.showAvgStopTime) {
      lines.add("postój: " + (int) avgStopTime + "s");
    }
    if (ps.showNumWaiting) {
      lines.add("doje¿d¿a: " + m_node.numWaitingMobiles);
    }

    String[] linesArr = lines.toArray(new String[lines.size()]);
    Point sz = new Point(0, 0);
    for (int i = 0; i < linesArr.length; i++) {
      Point lsz = gc.textExtent(linesArr[i]);
      sz.y += lsz.y;
      sz.x = Math.max(sz.x, lsz.x);
    }

    int roundsz = 4;
    gc.setBackground(Colors.get(new RGB(255, 255, 255)));
    gc.setForeground(Colors.get(new RGB(0, 0, 0)));
    gc.fillRoundRectangle(p.x - sz.x/2 - 4, p.y - sz.y/2 - 2, sz.x + 8, sz.y + 4, 16,
      16);
    gc.drawRoundRectangle(p.x - sz.x/2 - 4, p.y - sz.y/2 - 2, sz.x + 8, sz.y + 4, 16,
      16);
    int y = p.y - sz.y/2;
    for (int i = 0; i < linesArr.length; i++) {
      Point lsz = gc.textExtent(linesArr[i]);
      gc.drawText(linesArr[i], p.x - lsz.x/2, y, true);
      y += lsz.y;
    }
  }
}