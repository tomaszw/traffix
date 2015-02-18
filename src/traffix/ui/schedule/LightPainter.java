/*
 * Created on 2004-07-05
 */

package traffix.ui.schedule;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;
import org.tw.geometry.Vec2f;
import traffix.core.schedule.LightTypes;
import traffix.ui.Colors;
import traffix.ui.Gc;

public class LightPainter {
  public LightPainter() {
  }

  public Image[] buildLightImages(Device d, int w, int h) {
    Image[] imgs = new Image[LightTypes.NUM_LIGHTS];
    for (int i = 0; i < LightTypes.NUM_LIGHTS; ++i) {
      imgs[i] = new Image(d, w, h);
      GC gc = new GC(imgs[i]);
      paintInterior(new Gc(gc), 0, 0, w, h, i);//, false, false);
      gc.dispose();
    }
    return imgs;
  }

  public void paintBar(Gc gc, int x, int y, int w, int h, int lightType,
    boolean boundLeft, boolean boundRight) {
    gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
    gc.setLineStyle(SWT.LINE_SOLID);
    // find size
    if (boundLeft) {
      //x+=1;
      //w-=1;
      //x += 3;
      //w -= 3;
    }
    if (boundRight) {
      //w -= 2;
    }

    y += 1;
    h -= 2;

    paintInterior(gc, x, y, w, h, lightType);

    // draw borders
    gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));

    gc.drawLine(x, y, x + w - 1, y);
    gc.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
    if (boundLeft) {
      gc.drawLine(x, y, x, y + h - 1);
    }
    if (boundRight) {
      //if (lightType != LightTypes.PULSING_GREEN && lightType !=
      // LightTypes.PULSING_YELLOW)
      //gc.drawLine(x + w - 1, y, x + w - 1, y + h - 1);
    }
  }

  public void paintBar(GC gc, int x, int y, int w, int h, int lightType,
    boolean boundLeft, boolean boundRight) {
    paintBar(new Gc(gc), x, y, w, h, lightType, boundLeft, boundRight);
  }

  public void paintInterior(Gc gc, int x, int y, int w, int h, int lightType) {
    //    Pointf quad[] = new Pointf[4];
    //    quad[0] = new Pointf(x, y);
    //    quad[1] = new Pointf(x + w, y);
    //    quad[2] = new Pointf(x + w, y + h);
    //    quad[3] = new Pointf(x, y + h);
    //    paintInterior(gc, quad, lightType);
    Rectangle clip = gc.getClipping();
    gc.setClipping(x, y, w, h);
    gc.setLineStyle(SWT.LINE_SOLID);
    gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
    Color red, green, yellow;
    red = Colors.get(ScheduleStyle.redLightColor);
    green = Colors.get(ScheduleStyle.greenLightColor);
    yellow = Colors.get(ScheduleStyle.yellowLightColor);

    // background draw
    switch (lightType) {
      case LightTypes.NO_SIGNAL:
        gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        gc.fillRectangle(x, y, w, h);
        int stepx = ScheduleStyle.groupBarH - 3;
        int mx = ((x - stepx)/stepx)*stepx;
        //gc.setLineWidth(2);
        int vmarg = 3;
        int hmarg = 3;
        while (mx < x + w) {
          gc.drawLine(mx + hmarg, y + h - 1 - vmarg, mx + stepx - hmarg, y + vmarg);
          gc.drawLine(mx + hmarg, y + vmarg, mx + stepx - hmarg, y + h - 1 - vmarg);
          mx += stepx;
        }
        //gc.setLineWidth(1);
        break;

      case LightTypes.RED:
        gc.setBackground(red);
        gc.fillRectangle(x, y, w, h);
        //gc.setLineWidth(2);
        gc.drawLine(x, y + h/2, x + w, y + h/2);
        gc.setLineWidth(1);
        break;
      case LightTypes.GREEN:
        gc.setBackground(green);
        gc.fillRectangle(x, y, w, h);
        break;
      case LightTypes.YELLOW:
        gc.setBackground(yellow);
        gc.fillRectangle(x, y, w, h);
        //gc.setLineWidth(2);
        gc.drawLine(x, y + h - 1, x + w, y);
        gc.setLineWidth(1);
        break;
      case LightTypes.RED_YELLOW:
        gc.setBackground(yellow);
        gc.fillRectangle(x, y, w, h);
        gc.setBackground(red);
        gc.fillRectangle(x, y, w, h/2);
        //gc.setLineWidth(2);
        gc.drawLine(x, y + h/2, x + w, y + h/2);
        gc.drawLine(x, y + h - 1, x + w, y);
        gc.setLineWidth(1);
        break;
      case LightTypes.PULSING_RED:
        gc.setBackground(red);
        gc.fillRectangle(x, y, w, h);
        stepx = ScheduleStyle.cellW;
        mx = (x/ScheduleStyle.cellW)*ScheduleStyle.cellW;
        while (mx < x + w) {
          gc.drawLine(mx, y + h/2, mx + stepx/2, y + h/2);
          mx += stepx;
        }
        break;
      case LightTypes.PULSING_GREEN:
        for (int i = 0; i < w; ++i) {
          if (i%5 < 3) {
            gc.setForeground(green);
          } else {
            gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
          }
          gc.drawLine(x + i, y, x + i, y + h - 1);
        }
        break;
      case LightTypes.PULSING_YELLOW:
        gc.setBackground(yellow);//Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        gc.fillRectangle(x, y, w, h);
        gc.setLineWidth(2);
        stepx = ScheduleStyle.groupBarH/3;
        mx = ((x - stepx)/stepx)*stepx;
        while (mx < x + w) {
          gc.drawLine(mx, y + h - 1, mx + stepx, y);
          mx += stepx;
        }
        gc.setLineWidth(1);
        break;
    }
    gc.setClipping(clip);
  }

  public void paintInteriorDuringSimulation(Gc gc, Vec2f[] quad, int lightType, int sec) {
    boolean odd = sec%2 == 1;
    Vec2f h_dir = quad[1].sub(quad[0]).normalize();
    Vec2f v_dir = quad[2].sub(quad[1]).normalize();
    float h_len = quad[1].sub(quad[0]).length();
    float v_len = quad[2].sub(quad[1]).length();

    int[] points = new int[8];
    for (int i = 0; i < 4; ++i) {
      points[2*i] = (int) quad[i].x;
      points[2*i + 1] = (int) quad[i].y;
    }

    Color red, green, yellow, white;
    red = Colors.get(ScheduleStyle.redLightColor);
    green = Colors.get(ScheduleStyle.greenLightColor);
    yellow = Colors.get(ScheduleStyle.yellowLightColor);
    white = Colors.system(SWT.COLOR_WHITE);

    gc.setLineStyle(SWT.LINE_SOLID);
    gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));

    switch (lightType) {
      case LightTypes.RED:
        gc.setBackground(red);
        gc.fillPolygon(points);
        break;
      case LightTypes.PULSING_RED:
        gc.setBackground(odd ? red : white);
        gc.fillPolygon(points);
        break;
      case LightTypes.GREEN:
        gc.setBackground(green);
        gc.fillPolygon(points);
        break;
      case LightTypes.PULSING_GREEN:
        gc.setBackground(odd ? green : white);
        gc.fillPolygon(points);
        break;
      case LightTypes.YELLOW:
        gc.setBackground(yellow);
        gc.fillPolygon(points);
        break;
      case LightTypes.PULSING_YELLOW:
        gc.setBackground(odd ? yellow : white);
        gc.fillPolygon(points);
        break;
      case LightTypes.RED_YELLOW:
        gc.setBackground(yellow);
        gc.fillPolygon(upperHalf(quad));
        gc.setBackground(red);
        gc.fillPolygon(lowerHalf(quad));
        break;
      case LightTypes.NO_SIGNAL:
        gc.setBackground(white);
        gc.fillPolygon(points);
        gc.drawPolygon(points);
        break;
    }
  }

  private int[] lowerHalf(Vec2f[] quad) {
    Vec2f[] q = new Vec2f[4];
    System.arraycopy(quad, 0, q, 0, 4);
    q[0] = q[3].sub(q[3].sub(q[0]).mul(0.5f));
    q[1] = q[2].sub(q[2].sub(q[1]).mul(0.5f));
    return quadToPoints(q);
  }

  private int[] quadToPoints(Vec2f[] quad) {
    int[] points = new int[8];
    for (int i = 0; i < 4; ++i) {
      points[2*i] = (int) quad[i].x;
      points[2*i + 1] = (int) quad[i].y;
    }
    return points;
  }

  private int[] upperHalf(Vec2f[] quad) {
    Vec2f[] q = new Vec2f[4];
    System.arraycopy(quad, 0, q, 0, 4);
    q[2] = q[1].add(q[2].sub(q[1]).mul(0.5f));
    q[3] = q[0].add(q[3].sub(q[0]).mul(0.5f));
    return quadToPoints(q);
  }

}