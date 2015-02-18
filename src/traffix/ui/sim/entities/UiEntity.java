/*
 * Created on 2004-07-22
 */

package traffix.ui.sim.entities;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.tw.geometry.Vec2f;
import org.tw.geometry.Rectanglef;


public abstract class UiEntity implements IUiEntity {
  protected IUiEntityContextProvider m_contextProvider;
  private Vec2f m_origin = new Vec2f(0, 0);

  public UiEntity(IUiEntityContextProvider contextProvider) {
    m_contextProvider = contextProvider;
  }

  public Rectanglef getBounds() {
    return null;
  }

  public UiEntityContext getContext() {
    return m_contextProvider.getUiEntityContext();
  }

  public Vec2f getOrigin() {
    return m_origin;
  }

  public boolean isInEditMode() {
    return !(getContext().simRunning || getContext().getSimManager().getCurrentTime()>0);
  }
  
  public void dispose() {
  }

  public int getPaintPriority() {
    return 0;
  }

  public Rectangle getScreenBounds() {
    return null;
  }

  public Point getScreenPos() {
    return getContext().getCoordTransformer().terrainToScreen(m_origin);
  }

  public void moveBy(float dx, float dy) {
    moveTo(m_origin.x + dx, m_origin.y + dy);
  }

  public void moveTo(float x, float y) {
    m_origin.x = x;
    m_origin.y = y;
  }

  public void paint() {
  }
}