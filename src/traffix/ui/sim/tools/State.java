/*
 * Created on 2004-07-20
 */

package traffix.ui.sim.tools;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.tw.geometry.Vec2f;
import traffix.ui.sim.IMapEditor;

public abstract class State {

  protected IMapEditor m_editor;
  protected HoverInfo m_hoverInfo = new HoverInfo();

  private State m_nextState = this;

  static public class HoverInfo {
    float mapX, mapY;
    int pixelX, pixelY;
  }

  public State(IMapEditor editor) {
    m_editor = editor;
  }

  public void enter() {
    m_nextState = this;
    updateHoverInfo();
  }

  public State getNextState() {
    return m_nextState;
  }

  public void leave() {
  }

  public void onDoubleClick(MouseEvent e) {
  }

  public void onKeyDown(KeyEvent e) {
  }

  public void onKeyUp(KeyEvent e) {
  }

  public void onMouseDown(MouseEvent e) {
  }

  public void onMouseMove(MouseEvent e) {
    updateHoverInfo();
  }

  public void onMouseUp(MouseEvent e) {
  }

  public void setNextState(State nextState) {
    m_nextState = nextState;
  }

  protected Vec2f getMarkerPosInMeters() {
    return new Vec2f(m_editor.getMarkerPos());
  }

  protected Point getMarkerPosInPixels() {
    return m_editor.getCoordTransformer().terrainToScreen(getMarkerPosInMeters());
  }

  protected void setCursor(Cursor cur) {
    m_editor.setCursor(cur);
  }

  protected void setCursor(int id) {
    m_editor.setCursor(Display.getDefault().getSystemCursor(id));
  }

  protected void updateHoverInfo() {
    Vec2f marker = m_editor.getMarkerPos();
    m_hoverInfo.mapX = marker.x;
    m_hoverInfo.mapY = marker.y;

    Point mouse = m_editor.getCanvas().getDisplay().getCursorLocation();
    mouse = m_editor.getCanvas().toControl(mouse);
    m_hoverInfo.pixelX = mouse.x;
    m_hoverInfo.pixelY = mouse.y;
  }
}