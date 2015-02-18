/*
 * Created on 2004-07-07
 */

package traffix.ui.schedule;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import traffix.ui.Images;

public class UpperRulerWidget extends Composite {
  int m_offset = 0;
  int m_numSecs = 10;
  int m_arrowPos = -1;
  int m_arrowPos2 = -1;

  Canvas m_canvas;

  public UpperRulerWidget(Composite parent, int style) {
    super(parent, style);

    setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
    m_canvas = new Canvas(this, SWT.NO_BACKGROUND);

    m_canvas.addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        UpperRulerWidget.this.paint(e.gc);
      }
    });

    addControlListener(new ControlListener() {
      public void controlMoved(ControlEvent e) {
      }

      public void controlResized(ControlEvent e) {
        layout();
      }
    });
  }

  public void setArrowPos(int pos) {
    if (pos == m_arrowPos)
      return;
    m_arrowPos = pos;
    m_canvas.redraw();
  }

  public void setArrowPos2(int pos) {
    if (pos == m_arrowPos2)
      return;
    m_arrowPos2 = pos;
    m_canvas.redraw();
  }

  public Point computeSize(int wHint, int hHint, boolean changed) {
    int w, h;
    if (wHint != SWT.DEFAULT)
      w = wHint;
    else
      w = m_numSecs*ScheduleStyle.cellW;
    if (hHint != SWT.DEFAULT)
      h = hHint;
    else
      h = 30;
    return new Point(w, h);
  }

  public void setNumSecs(int numSecs) {
    m_numSecs = numSecs;
    layout();
    m_canvas.redraw();
  }

  public void setOffset(int off) {
    m_offset = off;
    layout();
  }

  public void layout(boolean changed) {
    Point sz = getSize();
    m_canvas.setSize(Math.max(sz.x, ScheduleStyle.cellW*m_numSecs + 20), sz.y);
    m_canvas.setLocation(-m_offset, 0);
  }

  public void paint(GC gc) {
    Point sz = m_canvas.getSize();
    Image buffer = new Image(getDisplay(), sz.x, sz.y);
    GC memGc = new GC(buffer);
    try {
      memGc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
      memGc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

      int numSecs = m_numSecs;
      for (int i = 0; i < numSecs; ++i) {
        int baseLen = sz.y/2, len;
        boolean bold = true;
        if (i%10 == 0)
          len = baseLen;
        else if (i%5 == 0)
          len = baseLen/2 + baseLen/4;
        else {
          len = baseLen/2;
          bold = false;
        }

        int x = ScheduleStyle.cellW*i;
        memGc.drawLine(x, sz.y - 1, x, sz.y - len);
        if (bold) {
          memGc.drawLine(x - 1, sz.y - 1, x - 1, sz.y - len);
          memGc.drawLine(x + 1, sz.y - 1, x + 1, sz.y - len);
        }
        if (i%5 == 0 && i != 0) {
          String txt = Integer.toString(i);
          Point extent = memGc.textExtent(txt);
          memGc.drawText(txt, x - extent.x/2, sz.y - sz.y/2 - extent.y - 2);
        }

        if (i == m_arrowPos || i == m_arrowPos2) {
          Image img = Images.get("icons/arrowDown2.gif");
          Rectangle bounds = img.getBounds();
          memGc.drawImage(img, x - bounds.width/2, sz.y - 1 - bounds.height);
        }
      }
      gc.drawImage(buffer, 0, 0);
    } finally {
      memGc.dispose();
      buffer.dispose();
    }
  }
}