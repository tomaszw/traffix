/*
 * Created on 2004-08-21
 */

package traffix.ui.sim.entities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.tw.geometry.Vec2f;
import org.tw.geometry.Polygonf;
import traffix.Traffix;
import traffix.core.sim.entities.Light;
import traffix.ui.Colors;
import traffix.ui.Gc;
import traffix.ui.schedule.LightPainter;
import traffix.ui.sim.CoordinateTransformer;

public class LightUi extends UiEntity {
  private Light m_light;
  private LightPainter m_painter = new LightPainter();

  public static final int CENTER_RADIUS = 8;

  LightUi(IUiEntityContextProvider cp, Light light) {
    super(cp);
    m_light = light;
  }

  public UiEntityType getType() {
    return UiEntityType.LIGHT;
  }

  public int getPaintPriority() {
    return 2;
  }

  public void paint() {
    super.paint();
    CoordinateTransformer ct = getContext().getCoordTransformer();
    Gc gc = getContext().getGc();
    Polygonf bounds = m_light.getBounds();
    //Region clip = new Region();
    int[] points = new int[2*bounds.getNumSides()];
    int[] orgpoints = new int[2*bounds.getNumSides()];
    for (int i = 0; i < bounds.getNumSides(); ++i) {
      Point p = ct.terrainToScreen(bounds.getPoint(i));
      points[i*2] = gc.tx(p.x);
      points[i*2 + 1] = gc.ty(p.y);
      orgpoints[i*2] = (p.x);
      orgpoints[i*2 + 1] = (p.y);
    }
    //clip.add(points);

    if (!isInEditMode()) {
      //m_painter.setClipping(clip);
      Vec2f quad[] = new Vec2f[4];
      for (int i = 0; i < 4; ++i) {
        Point screen = ct.terrainToScreen(bounds.getPoint(i));
        quad[i] = new Vec2f(screen.x, screen.y);
      }
      m_painter.paintInteriorDuringSimulation(gc, quad, m_light.getState(), Traffix
        .simScheduleManager().getScheduleTime());

      //      Rectanglef rect = bounds.getBoundingRect();
      //      Point p1 = ct.terrainToScreen(rect.x, rect.y);
      //      Point p2 = ct.terrainToScreen(rect.x + rect.width, rect.y + rect.height);
      //
      //      m_painter.paintInterior(gc, p1.x, p1.y, p2.x - p1.x, p2.y - p1.y, m_light
      //          .getState());
    } else {
      gc.setLineWidth(3);
      gc.setLineStyle(SWT.LINE_SOLID);
      gc.setForeground(Colors.get(new RGB(255, 0, 0)));
      gc.drawPolygon(orgpoints);

      //      Segmentf line = m_light.getBaseline();
      //      Point p1 = ct.terrainToScreen(line.a);
      //      Point p2 = ct.terrainToScreen(line.b);
      //      gc.setLineWidth(1);
      //      gc.drawLine(p1.x,p1.y,p2.x,p2.y);

      gc.setLineWidth(1);
      Point p = ct.terrainToScreen(m_light.getCenter());
      gc.setLineStyle(SWT.LINE_DOT);
      gc.setForeground(Colors.get(new RGB(0, 0, 0)));
      gc.setBackground(Colors.get(new RGB(255, 255, 255)));

      //      GraphLocation[] locs = m_light.getObstacleGraphLocations();
      //      for (int i=0; i<locs.length; ++i) {
      //        p = ct.terrainToScreen(locs[i].p);
      //        
      //        gc.setBackground(Colors.get(new RGB(255, 100, 0)));
      //        gc.fillOval(p.x-3,p.y-3,7,7);
      //      }

      p = ct.terrainToScreen(m_light.getCenter());
      String label = m_light.getGroup().getElectricName();
      Point sz = gc.textExtent(label);

      gc.setBackground(Colors.get(new RGB(255, 255, 255)));
      gc.setForeground(Colors.get(new RGB(0, 0, 0)));
      gc.fillRoundRectangle(p.x - sz.x/2 - 4, p.y - sz.y/2 - 2, sz.x + 8, sz.y + 4,
        12, 12);
      gc.drawRoundRectangle(p.x - sz.x/2 - 4, p.y - sz.y/2 - 2, sz.x + 8, sz.y + 4,
        12, 12);
      gc.drawText(label, p.x - sz.x/2, p.y - sz.y/2, true);
      gc.setLineStyle(SWT.LINE_SOLID);

      //gc.fillOval(p.x-CENTER_RADIUS, p.y-CENTER_RADIUS, CENTER_RADIUS*2+1,
      // CENTER_RADIUS*2+1);
      //      for (int i = 0; i < 2; ++i) {
      //        gc.drawOval(p.x - CENTER_RADIUS + i, p.y - CENTER_RADIUS + i, CENTER_RADIUS * 2 +
      // 1
      //            - i * 2, CENTER_RADIUS * 2 + 1 - i * 2);
      //        gc.setLineStyle(SWT.LINE_SOLID);
      //      }

    }
    //clip.dispose();
  }
}