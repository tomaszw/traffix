/*
 * Created on 2004-07-22
 */

package traffix.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class ScrolledCanvas extends ScrolledComposite {
  private Canvas m_canvas;

  public ScrolledCanvas(Composite parent, int style) {
    super(parent, style);

    setLayout(new FillLayout());
    m_canvas = new Canvas(this, SWT.NO_BACKGROUND);
    setContent(m_canvas);
  }

  public Canvas getCanvas() {
    return m_canvas;
  }

  public void setCanvasSize(int x, int y) {
    m_canvas.setSize(x, y);
  }

  public void redrawCanvas() {
    checkWidget();
    if (!OS.IsWindowVisible(m_canvas.handle))
      return;
    if (OS.IsWinCE) {
      OS.InvalidateRect(m_canvas.handle, null, true);
    } else {
      int flags = OS.RDW_FRAME | OS.RDW_INVALIDATE;
      OS.RedrawWindow(m_canvas.handle, null, 0, flags);
    }
  }

}