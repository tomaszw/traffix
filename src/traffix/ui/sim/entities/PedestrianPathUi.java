/*
 * Created on 2004-08-21
 */

package traffix.ui.sim.entities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.tw.geometry.Vec2f;
import org.tw.geometry.Polygonf;
import org.tw.geometry.Segmentf;
import traffix.Traffix;
import traffix.core.sim.entities.PedestrianPath;
import traffix.ui.Colors;
import traffix.ui.Gc;
import traffix.ui.sim.CoordinateTransformer;

public class PedestrianPathUi extends UiEntity {
  private PedestrianPath m_path;

  public static final int CENTER_RADIUS = 8;

  PedestrianPathUi(IUiEntityContextProvider cp, PedestrianPath path) {
    super(cp);
    m_path = path;
  }

  public UiEntityType getType() {
    return UiEntityType.PEDESTRIAN_PATH;
  }

  public int getPaintPriority() {
    return 2;
  }

  public void paint() {
    super.paint();
    CoordinateTransformer ct = getContext().getCoordTransformer();
    Gc gc = getContext().getGc();
    Polygonf bounds = m_path.getBounds();
    int[] points = new int[2*bounds.getNumSides()];
    int[] orgpoints = new int[2*bounds.getNumSides()];
    for (int i = 0; i < bounds.getNumSides(); ++i) {
      Point p = ct.terrainToScreen(bounds.getPoint(i));
      points[i*2] = gc.tx(p.x);
      points[i*2 + 1] = gc.ty(p.y);
      orgpoints[i*2] = (p.x);
      orgpoints[i*2 + 1] = (p.y);
    }

    if (!isInEditMode()) {
      if (!m_path.isOpen()) return;
      gc.setForeground(Colors.get(new RGB(0, 200, 255)));

//      
//      gc.setLineWidth(4);
//      Segmentf line = m_path.getTopLine();
//      Point p1 = ct.terrainToScreen(line.a);
//      Point p2 = ct.terrainToScreen(line.b);
//      gc.drawLine(p1.x, p1.y, p2.x, p2.y);
//
//      line = m_path.getBottomLine();
//      p1 = ct.terrainToScreen(line.a);
//      p2 = ct.terrainToScreen(line.b);
//      gc.drawLine(p1.x, p1.y, p2.x, p2.y);

      Segmentf line;
      gc.setBackground(Colors.get(new RGB(0, 0, 255)));

      float offset = Traffix.simManager().getCurrentTime()/4;
      offset *= 2000;
      offset = (offset%2000)/1000.0f;

      Segmentf lines[] = new Segmentf[2];
      lines[0] = m_path.getTopLine();
      lines[1] = m_path.getBottomLine();
      for (int i = 0; i < 2; ++i) {
        line = lines[i];
        Vec2f p0 = line.a;
        Vec2f dir = line.b.sub(line.a).normalize();
        if (i == 1) {
          p0 = line.b;
          dir = dir.mul(-1);
        }

        float len = line.length();
        float t = offset;
        while (t <= len) {
          Vec2f p = p0.add(dir.mul(t));
          Point scrP = ct.terrainToScreen(p);
          int rad = ct.terrainToScreen(0.5f);
          gc.fillOval(scrP.x - rad, scrP.y - rad, rad*2 + 1, rad*2 + 1);
          t += 2;
        }
      }


      return;
    }

    gc.setLineWidth(1);
    gc.setLineStyle(SWT.LINE_SOLID);
    gc.setForeground(Colors.get(new RGB(0, 100, 255)));
    gc.drawPolygon(orgpoints);

    gc.setLineWidth(4);
    Segmentf line = m_path.getTopLine();
    Point p1 = ct.terrainToScreen(line.a);
    Point p2 = ct.terrainToScreen(line.b);
    gc.drawLine(p1.x, p1.y, p2.x, p2.y);

    line = m_path.getBottomLine();
    p1 = ct.terrainToScreen(line.a);
    p2 = ct.terrainToScreen(line.b);
    gc.drawLine(p1.x, p1.y, p2.x, p2.y);

    gc.setLineWidth(1);
    Point p = ct.terrainToScreen(m_path.getCenter());
    gc.setLineStyle(SWT.LINE_DOT);

    String label = m_path.getGroup().getElectricName();
    Point sz = gc.textExtent(label);

    gc.setBackground(Colors.get(new RGB(255, 255, 255)));
    gc.setForeground(Colors.get(new RGB(0, 0, 0)));
    gc.fillRoundRectangle(p.x - sz.x/2 - 4, p.y - sz.y/2 - 2, sz.x + 8, sz.y + 4, 12, 12);
    gc.drawRoundRectangle(p.x - sz.x/2 - 4, p.y - sz.y/2 - 2, sz.x + 8, sz.y + 4, 12, 12);
    gc.drawText(label, p.x - sz.x/2, p.y - sz.y/2, true);
    
//    gc.setForeground(Colors.get(new RGB(0, 0, 0)));
//    gc.setBackground(Colors.get(new RGB(255, 255, 255)));
//    for (int i = 0; i < 2; ++i) {
//      gc.drawOval(p.x - CENTER_RADIUS + i, p.y - CENTER_RADIUS + i, CENTER_RADIUS * 2 + 1
//          - i * 2, CENTER_RADIUS * 2 + 1 - i * 2);
//      gc.setLineStyle(SWT.LINE_SOLID);
//    }
  }
}