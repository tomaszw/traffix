/*
 * Created on 2004-08-21
 */

package traffix.ui.sim.entities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.tw.geometry.Polygonf;

import traffix.core.sim.entities.Barrier;
import traffix.ui.Colors;
import traffix.ui.Gc;
import traffix.ui.Images;
import traffix.ui.misc.SwtKit;
import traffix.ui.sim.CoordinateTransformer;

public class BarrierUi extends UiEntity {
  private Barrier m_barrier;

  BarrierUi(IUiEntityContextProvider cp, Barrier d) {
    super(cp);
    m_barrier = d;
  }

  public UiEntityType getType() {
    return UiEntityType.BARRIER;
  }

  public void paint() {
    super.paint();
    CoordinateTransformer ct = getContext().getCoordTransformer();
    Gc gc = getContext().getGc();
    Polygonf bounds = m_barrier.getBounds();

    int[] screenPts = SwtKit.pointsToIntegers(ct.terrainToScreen(bounds));

    if (!isInEditMode()) {
//      Color bg = Colors.get(new RGB(225,225,225));
//      if (Traffix.simScheduleManager().isRequestActive(m_barrier))
//        bg = Colors.get(new RGB(180,180,180));
//      gc.setBackground(bg);
//      gc.fillPolygon(screenPts);

    } else {
      gc.setLineWidth(1);
      if (m_barrier.isActive()) {
        gc.setForeground(Colors.get(new RGB(255, 0, 0)));
      } else {
        gc.setForeground(Colors.get(new RGB(0, 0, 0)));
      }
      gc.drawPolygon(screenPts);
      gc.setLineWidth(2);
      gc.drawLine(screenPts[0], screenPts[1], screenPts[2], screenPts[3]);
      gc.drawLine(screenPts[4], screenPts[5], screenPts[6], screenPts[7]);

      Point p = ct.terrainToScreen(m_barrier.getCenter());
      gc.setLineWidth(2);
      //gc.setLineStyle(SWT.LINE_DASH);
      
      if (m_barrier.getMaster() != null) {
        Point p2 = ct.terrainToScreen(m_barrier.getMaster().getCenter());
        for (int i = 0; i < 1; ++i)
          gc.drawLine(i + p.x, i + p.y, i + p2.x, i + p2.y);
      }

      gc.setLineWidth(1);
      gc.setLineStyle(SWT.LINE_DOT);
      String lab = "P";

      Image img = Images.get("icons/barrier.gif");
      Rectangle bs = img.getBounds();
      Point sz = new Point(bs.width, bs.height);
      gc.drawImage(img, p.x - sz.x/2, p.y - sz.y/2);
//      gc.setBackground(Colors.get(new RGB(255, 255, 255)));
//      gc.setForeground(Colors.get(new RGB(0, 0, 0)));
//      gc.fillRoundRectangle(p.x - sz.x / 2 - 4, p.y - sz.y / 2 - 2, sz.x + 8, sz.y + 4,
//          12, 12);
//      gc.drawRoundRectangle(p.x - sz.x / 2 - 4, p.y - sz.y / 2 - 2, sz.x + 8, sz.y + 4,
//          12, 12);
//      gc.drawText(lab, p.x - sz.x / 2, p.y - sz.y / 2, true);
      gc.setLineStyle(SWT.LINE_SOLID);
    }
  }
}