/*
 * Created on 2004-08-21
 */

package traffix.ui.sim.entities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.tw.geometry.Vec2f;
import org.tw.geometry.Polygonf;
import traffix.core.sim.entities.BusStop;
import traffix.ui.Colors;
import traffix.ui.Gc;
import traffix.ui.Images;
import traffix.ui.sim.CoordinateTransformer;

public class BusStopUi extends UiEntity {
  private BusStop m_stop;

  public static final int CENTER_RADIUS = 8;

  BusStopUi(IUiEntityContextProvider cp, BusStop stop) {
    super(cp);
    m_stop = stop;
  }

  public UiEntityType getType() {
    return UiEntityType.BUSSTOP;
  }

  public void paint() {
    super.paint();
    CoordinateTransformer ct = getContext().getCoordTransformer();
    Gc gc = getContext().getGc();
    Polygonf bounds = m_stop.getBounds();
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

      if (m_stop.isUnloading())
        gc.setBackground(Colors.get(new RGB(255, 0, 255)));
      else
        gc.setBackground(Colors.get(new RGB(255, 0, 0)));
      gc.fillPolygon(orgpoints);
    } else {
      gc.setLineWidth(2);
      gc.setLineStyle(SWT.LINE_SOLID);
      gc.setForeground(Colors.get(new RGB(255, 0, 0)));
      gc.drawPolygon(orgpoints);

      Point p = ct.terrainToScreen(m_stop.getCenter());
      gc.setLineWidth(1);
      gc.setLineStyle(SWT.LINE_DOT);
      gc.setForeground(Colors.get(new RGB(0, 0, 0)));
      gc.setBackground(Colors.get(new RGB(255, 255, 255)));


      p = ct.terrainToScreen(m_stop.getCenter());
      Image img = Images.get("icons/stop.gif");
      Rectangle bs = img.getBounds();
      Point sz = new Point(bs.width, bs.height);
      gc.drawImage(img, p.x - sz.x/2, p.y - sz.y/2);
//      String label = "S";
//      Point sz = gc.textExtent(label);
//      
//      gc.fillRoundRectangle(p.x-sz.x/2-4,p.y-sz.y/2-2,sz.x+8,sz.y+4,12,12);
//      gc.drawRoundRectangle(p.x-sz.x/2-4,p.y-sz.y/2-2,sz.x+8,sz.y+4,12,12);
//      gc.drawText(label, p.x-sz.x/2,p.y-sz.y/2, true);
//      gc.setLineStyle(SWT.LINE_SOLID);
    }
  }
}