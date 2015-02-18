/*
 * Created on 2004-08-18
 */

package traffix.ui.sim;

import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.tw.geometry.Vec2f;
import org.tw.geometry.Rectanglef;

import traffix.ui.Gc;
import traffix.ui.sim.entities.IUiEntity;
import traffix.ui.sim.tools.State;

public interface IMapEditor {
  Canvas getCanvas();

  CoordinateTransformer getCoordTransformer();

  State getCurrentState();

  Display getDisplay();

  IUiEntity getEntity(int i);

  Gc getGc();

  Vec2f getMarkerPos();
  int getNumEntities();
  Shell getShell();

  Rectanglef getVisibleRect();

  void setCurrentState(State toState);

  void setCursor(Cursor cursor);
}