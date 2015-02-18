/*
 * Created on 2004-08-17
 */

package traffix.ui.sim.entities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.tw.geometry.Vec2f;
import org.tw.geometry.Polygonf;
import org.tw.geometry.Rectanglef;

import traffix.core.sim.entities.IMobileChain;
import traffix.core.sim.entities.IMobile;
import traffix.core.sim.entities.Mobile;
import traffix.ui.Colors;
import traffix.ui.Gc;
import traffix.ui.sim.CoordinateTransformer;


public class MobileUi extends UiEntity {
  private IMobile m_mobile;

  public static final Vec2f X_AXIS = new Vec2f(1, 0);
  public static final Vec2f Y_AXIS = new Vec2f(0, 1);

  private Vec2f m_center;

  public UiEntityType getType() {
    return UiEntityType.VEHICLE;
  }

  MobileUi(IUiEntityContextProvider cp, IMobile vehicle) {
    super(cp);
    m_mobile = vehicle;
  }

  public int getPaintPriority() {
    return 999;
  }

  public void paint() {
    super.paint();
    if (getContext().informationFrame)
      return;

    doPaint(m_mobile);
    IMobileChain cm=null;
    if (m_mobile instanceof IMobileChain) {
      cm = (IMobileChain) m_mobile;
      cm = cm.getNextInChain();
      while (cm != null) {
        doPaint(cm);
        cm = cm.getNextInChain();
      }
    }
  }

  private void doPaint(IMobile mobile) {
    Polygonf bounds = mobile.getBounds();
    Rectanglef boundsRc = bounds.getBoundingRect();
    if (!getContext().filming && getContext().simRunning && boundsRc.intersects(getContext().mapEditor.getVisibleRect()))
      return;
    
    IMobileChain prev = null;
    if (mobile instanceof IMobileChain) {
      prev = ((IMobileChain) mobile).getPrevInChain();
    }

    if (prev != null) {
      if (prev.getBounds().isInside(mobile.getPosition()))
        return;
    }

    Gc gc = getContext().getGc();
    Vec2f center = new Vec2f();
    int[] poly = new int[bounds.getNumSides()*2];
    CoordinateTransformer ct = getContext().getCoordTransformer();
    for (int i = 0; i < bounds.getNumSides(); ++i) {
      center = center.add(bounds.getPoint(i));
      Point p = ct.terrainToScreen(bounds.getPoint(i));
      poly[2*i] = p.x;
      poly[2*i + 1] = p.y;
    }
    center = center.div(4);

    Color bkgnd = Colors.get(new RGB(0, 200, 255));
    if (mobile.getType() == IMobile.HeavyVehicle) {
      if (Math.abs(mobile.getLength() - 10) < 0.000001f)
        bkgnd = Colors.get(new RGB(255, 150, 0));
      else
        bkgnd = Colors.get(new RGB(0, 150, 255));
    } else if (mobile.getType() == IMobile.Bus)
      bkgnd = Colors.get(new RGB(255, 0, 0));
    else if (mobile.getType() == IMobile.Trolley)
      bkgnd = Colors.get(new RGB(0, 0, 255));
    else if (mobile.getType() == IMobile.Pedestrian)
      bkgnd = Colors.get(new RGB(200,200, 50));
    else if (mobile.getType() == IMobile.Cyclist)
      bkgnd = Colors.get(new RGB(200,200, 50));
      
    boolean bigbad = mobile.getType() != IMobile.NormalVehicle;
    gc.setBackground(bkgnd);
    gc.fillPolygon(poly);
    gc.setForeground(Colors.system(SWT.COLOR_BLACK));
    gc.setLineWidth(bigbad ? 2 : 1);
    //gc.setLineWidth(1);
    gc.setLineStyle(SWT.LINE_SOLID);
    gc.drawPolygon(poly);

    gc.setLineWidth(1);
    gc.setBackground(Colors.system(SWT.COLOR_WHITE));

    if (prev != null) {
      gc.setBackground(Colors.system(SWT.COLOR_BLACK));
      Point p = ct.terrainToScreen(mobile.getPosition().add(mobile.getDirection().mul(0.5f)));
      int w = ct.terrainToScreen(1.0f)/2 + 1;
      gc.fillOval(p.x - w, p.y - w, w*2 + 1, w*2 + 1);
    }
  }
}
