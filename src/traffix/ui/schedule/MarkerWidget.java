/*
 * Created on 2004-07-06
 */

package traffix.ui.schedule;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import traffix.ui.Images;

public class MarkerWidget extends Canvas {
  public static final int TOP = 0;
  public static final int LEFT = 1;

  Label m_arrow;
  Label m_label;
  Image m_imgArrowD = Images.get(ScheduleStyle.imgArrowDown);
  Image m_imgArrowR = Images.get(ScheduleStyle.imgArrowRight);
  int m_type = TOP;

  public MarkerWidget(Composite parent, int style, int type) {
    super(parent, style);
    m_type = type;
    init();
  }

  private void init() {
    setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

    m_label = new Label(this, SWT.NONE);
    m_label.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

    m_arrow = new Label(this, SWT.NONE);
    m_arrow.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
    m_arrow.setImage(m_type == TOP ? m_imgArrowD : m_imgArrowR);

    addControlListener(new ControlListener() {
      public void controlMoved(ControlEvent e) {
      }

      public void controlResized(ControlEvent e) {
        layout();
      }
    });
  }

  public void layout(boolean changed) {
    if (m_type == TOP) {
      Rectangle imgBounds = m_imgArrowD.getBounds();
      m_label.setSize(m_label.computeSize(SWT.DEFAULT, SWT.DEFAULT));
      m_label.setLocation(0, 0);
      Rectangle labBounds = m_label.getBounds();
      m_arrow.setSize(imgBounds.width, imgBounds.height);
      int width = Math.max(imgBounds.width, labBounds.width);
      int height = imgBounds.height + labBounds.height;
      m_arrow.setLocation(width/2 - imgBounds.width/2, labBounds.height);
    } else {
      Rectangle imgBounds = m_imgArrowR.getBounds();
      m_label.setSize(m_label.computeSize(SWT.DEFAULT, SWT.DEFAULT));
      m_label.setLocation(0, 0);
      Point labelSz = m_label.getSize();
      m_arrow.setSize(imgBounds.width, imgBounds.height);
      int h;
      if (imgBounds.height > labelSz.y)
        h = imgBounds.height;
      else
        h = labelSz.y + 1;
      m_arrow.setLocation(labelSz.x, h/2 - imgBounds.height/2);
    }

    setSize(computeSize(SWT.DEFAULT, SWT.DEFAULT));
  }

  public void setText(String text) {
    m_label.setText(text);
    layout();
  }

}