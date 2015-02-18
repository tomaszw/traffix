/*
 * Created on 2004-08-21
 */

package traffix.ui.sim.entities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.tw.geometry.Polygonf;

import traffix.core.sim.entities.SlowdownDetector;
import traffix.ui.Colors;
import traffix.ui.Gc;
import traffix.ui.Images;
import traffix.ui.misc.SwtKit;
import traffix.ui.sim.CoordinateTransformer;

public class SlowdownDetectorUi extends UiEntity {
  private SlowdownDetector m_detector;

  SlowdownDetectorUi(IUiEntityContextProvider cp, SlowdownDetector d) {
    super(cp);
    m_detector = d;
  }

  public UiEntityType getType() {
    return UiEntityType.SLOWDOWNDETECTOR;
  }

  public void paint() {
    super.paint();
    CoordinateTransformer ct = getContext().getCoordTransformer();
    Gc gc = getContext().getGc();
    Polygonf bounds = m_detector.getBounds();

    if (!isInEditMode()) {
//      Color bg = Colors.get(new RGB(225, 225, 225));
//      if (m_detector.getState() == TransitDetector.S_PRESSED)
//        bg = Colors.get(new RGB(180, 180, 180));
//      gc.setBackground(bg);
//      gc.fillPolygon(screenPts);

    } else {
      int[] screenPts = SwtKit.pointsToIntegers(ct.terrainToScreen(bounds));

      gc.setLineWidth(3);
      gc.setForeground(Colors.get(new RGB(180, 180, 0)));
      gc.drawPolygon(screenPts);

      Point p = ct.terrainToScreen(m_detector.getCenter());
      gc.setLineWidth(1);
      gc.setLineStyle(SWT.LINE_DOT);
      
      Image lab = Images.get("icons/slow.gif");
      Rectangle bs = lab.getBounds();
      Point sz = new Point(bs.width, bs.height);
      
      gc.setBackground(Colors.get(new RGB(255, 255, 255)));
      gc.setForeground(Colors.get(new RGB(0, 0, 0)));
      gc.fillRoundRectangle(p.x - sz.x/2 - 4, p.y - sz.y/2 - 2, sz.x + 8, sz.y + 4,
        12, 12);
      gc.drawRoundRectangle(p.x - sz.x/2 - 4, p.y - sz.y/2 - 2, sz.x + 8, sz.y + 4,
        12, 12);
      gc.drawImage(lab, p.x - sz.x/2, p.y - sz.y/2);
      gc.setLineStyle(SWT.LINE_SOLID);
    }
  }
}