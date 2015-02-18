/*
 * Created on 2004-07-22
 */

package traffix.ui.sim;

import org.eclipse.swt.graphics.GC;
import traffix.ui.Gc;

public class MapGc extends Gc {
  private IMapEditor m_editor;

  public MapGc(GC gc, IMapEditor editor) {
    super(gc);
    m_editor = editor;
  }

  public CoordinateTransformer getCoordTransformer() {
    return m_editor.getCoordTransformer();
  }
}