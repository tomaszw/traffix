/*
 * Created on 2004-07-22
 */

package traffix.ui.sim.entities;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.tw.geometry.Vec2f;
import org.tw.geometry.Rectanglef;


public interface IUiEntity {
  void dispose();
  Rectanglef getBounds();
  UiEntityContext getContext();
  Vec2f getOrigin();
  int getPaintPriority();
  Rectangle getScreenBounds();
  Point getScreenPos();
  UiEntityType getType();
  void moveBy(float dx, float dy);
  void moveTo(float x, float y);
  void paint();
}