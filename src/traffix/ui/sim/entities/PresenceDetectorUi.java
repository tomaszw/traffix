/*
 * Created on 2004-08-21
 */

package traffix.ui.sim.entities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.tw.geometry.Polygonf;

import traffix.Config;
import traffix.Traffix;
import traffix.core.sim.entities.PresenceDetector;
import traffix.ui.Colors;
import traffix.ui.Gc;
import traffix.ui.misc.SwtKit;
import traffix.ui.sim.CoordinateTransformer;

public class PresenceDetectorUi extends UiEntity {
  private PresenceDetector m_detector;

  PresenceDetectorUi(IUiEntityContextProvider cp, PresenceDetector d) {
    super(cp);
    m_detector = d;
  }

  public UiEntityType getType() {
    return UiEntityType.PRESENCEDETECTOR;
  }

  public void paint() {
    super.paint();
    CoordinateTransformer ct = getContext().getCoordTransformer();
    Gc gc = getContext().getGc();
    Polygonf bounds = m_detector.getBounds();

    int[] screenPts = SwtKit.pointsToIntegers(ct.terrainToScreen(bounds));

    if (!isInEditMode()) {
      Color bg = Colors.get(Config.RGB_PRESENCE_DETECTOR_INACTIVE);
      if (Traffix.simScheduleManager().isRequestActive(m_detector))
        bg = Colors.get(Config.RGB_PRESENCE_DETECTOR_ACTIVE);
      gc.setBackground(bg);
      gc.fillPolygon(screenPts);

    } else {
      gc.setLineWidth(3);
      gc.setForeground(Colors.get(new RGB(255, 255, 0)));
      gc.drawPolygon(screenPts);

      Point p = ct.terrainToScreen(m_detector.getCenter());
      gc.setLineWidth(1);
      gc.setLineStyle(SWT.LINE_DOT);
      String lab = "Do";

      Point sz = gc.textExtent(lab);
      gc.setBackground(Colors.get(new RGB(255, 255, 255)));
      gc.setForeground(Colors.get(new RGB(0, 0, 0)));
      gc.fillRoundRectangle(p.x - sz.x/2 - 4, p.y - sz.y/2 - 2, sz.x + 8, sz.y + 4,
        12, 12);
      gc.drawRoundRectangle(p.x - sz.x/2 - 4, p.y - sz.y/2 - 2, sz.x + 8, sz.y + 4,
        12, 12);
      gc.drawText(lab, p.x - sz.x/2, p.y - sz.y/2, true);
      gc.setLineStyle(SWT.LINE_SOLID);
    }
  }
}