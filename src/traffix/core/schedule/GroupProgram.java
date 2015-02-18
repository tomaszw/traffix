/*
 * Created on 2004-07-01
 */

package traffix.core.schedule;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;
import traffix.core.Range;
import traffix.ui.Colors;
import traffix.ui.Images;
import traffix.ui.schedule.LightPainter;
import traffix.ui.schedule.ScheduleStyle;

import java.util.LinkedList;

public class GroupProgram implements Cloneable {
  int[] m_data;
  // marks second no. 0
  private int   m_zeroMarkerPos = 0;

  public static class McmMarker {
    public int dir;
    public int time;
  }

  public GroupProgram() {
  }

  public GroupProgram(GroupProgram src) {
    assign(src);
  }

  public void append(GroupProgram other) {
    int p = m_data.length;
    setLength(m_data.length + other.m_data.length);
    for (int i = p; i < m_data.length; ++i)
      m_data[i] = other.m_data[i - p];
  }

  public void assign(GroupProgram other) {
    m_data = new int[other.m_data.length];
    System.arraycopy(other.m_data, 0, m_data, 0, other.m_data.length);
  }

  public void clear() {
    for (int i = 0; i < m_data.length; ++i)
      m_data[i] = LightTypes.RED;
  }

  @Override
  public GroupProgram clone() {
    return new GroupProgram(this);
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof GroupProgram))
      return false;
    GroupProgram other = (GroupProgram) obj;
    return java.util.Arrays.equals(m_data, other.m_data);
  }

  public void extend(int pos, int len) {
    pos = normalizeTime(pos);
    int[] oldProgram = m_data;
    setLength(m_data.length + len);
    for (int i = pos; i < pos + len; ++i)
      m_data[i] = oldProgram[pos];
    for (int i = pos + len; i < m_data.length; ++i)
      m_data[i] = oldProgram[i - len];
  }

  public Range[] findGreenLight() {
    if (m_data.length == 0)
      return null;
    int p = 0;
    if (LightTypes.isGreen(m_data[p]))
      p = skipGreen(p);
    else
      p = skip(p);
    if (p == -1) {
      if (!LightTypes.isGreen(m_data[0]))
        return null;
      Range[] b = new Range[1];
      b[0] = new Range(0, getLength());
      return b;
    }
    LinkedList<Range> found = new LinkedList<Range>();

    int start = p;

    for (;;) {
      if (LightTypes.isGreen(m_data[p])) {
        int next = skipGreen(p);

        Range bounds = new Range(p, next);
        if (bounds.from() > bounds.to())
          bounds = new Range(bounds.from(), bounds.to() + m_data.length);
        found.add(bounds);
        p = next;
        if (p == start)
          break;
      } else {
        p = skip(p);
        if (p == start)
          break;
      }
    }

    Range[] b = new Range[found.size()];
    found.toArray(b);
    return b;
  }

  public Range[] findLight(int light) {
    if (m_data.length == 0)
      return null;
    int p = 0;
    p = skip(p);
    if (p == -1) {
      if (m_data[0] != light)
        return null;
      Range[] b = new Range[1];
      b[0] = new Range(0, getLength());
      return b;
    }
    LinkedList<Range> found = new LinkedList<Range>();

    int start = p;

    for (;;) {
      if (m_data[p] == light) {
        int next = skip(p);
        Range bounds = new Range(p, next);
        if (bounds.from() > bounds.to())
          bounds = new Range(bounds.from(), bounds.to() + m_data.length);
        found.add(bounds);
        p = next;
        if (p == start)
          break;
      } else {
        p = skip(p);
        if (p == start)
          break;
      }
    }

    Range[] b = new Range[found.size()];
    found.toArray(b);
    return b;
  }

  public void floodFill(int pos, int light) {
    pos = normalizeTime(pos);
    int refClr = m_data[pos];
    int l = pos;
    int r = pos;
    boolean donel = false, doner = false;
    while (!donel) {
      m_data[l] = light;
      --l;
      if (l < 0 || m_data[l] != refClr)
        donel = true;
    }
    while (!doner) {
      m_data[r] = light;
      ++r;
      if (r >= m_data.length || m_data[r] != refClr)
        doner = true;
    }

    // int r = (pos + 1)%m_data.length;
    // boolean stop = false;
    // while (l != r && !stop) {
    // stop = true;
    // if (m_data[l] == refClr) {
    // m_data[l--] = light;
    // if (l < 0)
    // l += m_data.length;
    // stop = false;
    // }
    // if (l != r) {
    // if (m_data[r] == refClr) {
    // m_data[r++] = light;
    // r %= m_data.length;
    // stop = false;
    // }
    // } else {
    // if (m_data[r] == refClr) {
    // m_data[r] = light;
    // stop = true;
    // }
    // }
    // }
  }

  public int get(int i) {
    return m_data[normalizeTime(i)];
  }

  public int getLength() {
    return m_data.length;
  }

  public int getTotalGreenDuration() {
    int amount = 0;
    for (int i = 0; i < m_data.length; ++i)
      if (LightTypes.isGreen(m_data[i]))
        ++amount;
    return amount;
  }

  public int getZeroMarkerPos() {
    return m_zeroMarkerPos;
  }

  public int normalizeTime(int time) {
    if (m_data.length == 0)
      return 0;
    while (time < 0)
      time += m_data.length;
    time %= m_data.length;
    return time;
  }

  public void paintCollidedSecs(GC gc, int x, int y, int numCycles, int[] collisions) {
    gc.setBackground(Colors.get(new RGB(0, 0, 255)));
    int cyclePixLen = getLength() * ScheduleStyle.cellW;
    for (int i = 0; i < getLength(); ++i) {
      int px = x + ScheduleStyle.cellW * i;
      int py = y;
      if (collisions[i] > 0 && LightTypes.isGreen(get(i))) {
        for (int cyc = 0; cyc < numCycles; ++cyc)
          gc.fillRectangle(px + cyc * cyclePixLen, py + 6, ScheduleStyle.cellW,
              ScheduleStyle.groupBarH - 11);
      }
    }
  }

  public void paintGreenLightBubbles(GC gc, int x, int y, int numCycles) {
    int cycleLen = getLength();
    Range[] b = findGreenLight();
    if (b == null || b.length == 0)
      return; // no bubbles!
    // bubbles, bubbles!
    for (int seg = 0; seg < b.length; ++seg) {
      int l = b[seg].from() % getLength();
      int r = b[seg].to() % getLength();

      int p = l;
      int space = r - l;
      if (l > r)
        space = r + cycleLen - l;
      if (space < 5)
        continue;
      if (l > r && numCycles == 1) {
        int spaceLeft = r;
        int spaceRight = cycleLen - l;
        if (spaceRight > 5)
          p = l;
        else
          p = spaceRight >= spaceLeft ? l : 0;
      }
      ++p;
      String txt = Integer.toString(l) + "-" + Integer.toString(r);
      Point ext = gc.textExtent(txt);

      int px = x + p * ScheduleStyle.cellW;
      int py = y + ScheduleStyle.groupBarH / 2 - ext.y / 2;

      Rectangle rect = new Rectangle(px - 4, py - 2, ext.x + 8, ext.y + 3);
      gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
      gc.fillRoundRectangle(rect.x, rect.y, rect.width, rect.height, 12, 12);
      gc.drawRoundRectangle(rect.x, rect.y, rect.width, rect.height, 12, 12);
      gc.drawText(txt, px, py);
    }
  }

  public void paintLightBar(GC gc, LightPainter lp, int x, int y, int numSecs) {
    if (m_data.length == 0)
      return;
    int beg = 0, end = 0;

    int progLen = getLength();
    while (beg < numSecs && end < numSecs) {
      if (get(beg) == get(end)) {
        ++end;
      } else {
        int numCells = end - beg;
        boolean boundLeft = get(beg - 1) != get(beg);
        boolean boundRight = get(end) != get(end - 1);
        int px = x + ScheduleStyle.cellW * beg;
        lp.paintBar(gc, px, y + 1, ScheduleStyle.cellW * numCells,
            ScheduleStyle.groupBarH - 1, get(beg), boundLeft, boundRight);
        beg = end;
      }
    }

    if (end == numSecs) {
      int numCells = end - beg;
      boolean boundLeft = get(beg - 1) != get(beg);
      boolean boundRight = get(end) != get(end - 1);
      int px = x + ScheduleStyle.cellW * beg;
      lp.paintBar(gc, px, y + 1, ScheduleStyle.cellW * numCells,
          ScheduleStyle.groupBarH - 1, get(beg), boundLeft, boundRight);
    }
  }

  public void paintMcmMarkers(GC gc, int x, int y, McmMarker[] markers, int numCycles) {
    if (m_data.length == 0)
      return;

    if (markers == null)
      return;
    Display display = Display.getDefault();

    gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
    for (int i = 0; i < markers.length; ++i) {
      int xpos = x + markers[i].time * ScheduleStyle.cellW;
      int y1 = y + 3;
      int y2 = y1 + ScheduleStyle.groupBarH - 1 - 6;

      int h = y2 - y1;
      Image img = (markers[i].dir == 0) ? Images.get("icons/arrowLeft.gif") : Images
          .get("icons/arrowRight.gif");
      Rectangle bounds = img.getBounds();

      if (markers[i].dir == 0) {
        for (int cyc = 0; cyc < numCycles; ++cyc) {
          gc.drawImage(img, xpos + 1, (y1 + y2) / 2 - bounds.height / 2 + 1);
          xpos += getLength() * ScheduleStyle.cellW;
        }
      } else {
        xpos = x + (normalizeTime(markers[i].time - 1)) * ScheduleStyle.cellW
            + ScheduleStyle.cellW - bounds.width;
        for (int cyc = 0; cyc < numCycles; ++cyc) {
          gc.drawImage(img, xpos, (y1 + y2) / 2 - bounds.height / 2 + 1);
          xpos += getLength() * ScheduleStyle.cellW;
        }
      }
    }
  }

  public void paintUncollidedSecs(GC gc, int x, int y, int numCycles, int[] collisions) {
    gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
    int cyclePixLen = getLength() * ScheduleStyle.cellW;
    for (int i = 0; i < getLength(); ++i) {
      int px = x + ScheduleStyle.cellW * i;
      int py = y;
      if (collisions[i] == 0 && !LightTypes.isGreen(get(i))) {
        for (int cyc = 0; cyc < numCycles; ++cyc)
          gc.fillRectangle(px + cyc * cyclePixLen, py + 6, ScheduleStyle.cellW,
              ScheduleStyle.groupBarH - 11);
      }
    }
  }

  public void prolongGreen(int beg, int delta) {
    beg = normalizeTime(beg);
    // find red light bound
    int start = skip(beg);
    if (start == -1)
      return;
    int redStart = start;
    while (m_data[redStart] != LightTypes.RED && m_data[redStart] != LightTypes.NO_SIGNAL) {
      redStart = skip(redStart);
      if (redStart == start)
        return;
    }

    // find red light length;
    int p = redStart;
    int redLen = 0;
    while (m_data[p] == LightTypes.RED || m_data[p] == LightTypes.NO_SIGNAL) {
      p = normalizeTime(p + 1);
      ++redLen;
    }

    if (delta > 0) {
      // delta can't be higher than red light length
      delta = Math.min(delta, redLen);

      // move stuff
      p = beg;
      while (delta > 0) {
        for (int i = redStart; i != beg; i = normalizeTime(i - 1)) {
          m_data[i] = m_data[normalizeTime(i - 1)];
        }
        --delta;
        redStart = normalizeTime(redStart + 1);
        beg = normalizeTime(beg + 1);
      }
    } else if (delta < 0) {
      delta = -delta;
      // find green light start
      beg = skipLeft(beg);
      if (beg == -1)
        return;
      beg = normalizeTime(beg + 1);
      int greenLen = 0;
      p = beg;
      while (LightTypes.isGreen(m_data[p])) {
        p = normalizeTime(p + 1);
        ++greenLen;
      }
      if (!LightTypes.isGreen(m_data[beg]))
        return;
      delta = Math.min(greenLen, delta);
      while (delta > 0) {
        for (int i = beg; i != redStart; i = normalizeTime(i + 1)) {
          m_data[i] = m_data[normalizeTime(i + 1)];
        }
        --delta;
      }
    }
  }

  public void resizeLight(int beg, int delta) {
    beg = normalizeTime(beg);
    if (delta > 0) {
      int light = m_data[normalizeTime(beg - 1)];
      int p = beg;
      while (delta > 0) {
        m_data[p] = light;
        --delta;
        p = normalizeTime(p + 1);
      }
    } else {
      int light = m_data[normalizeTime(beg)];
      int p = normalizeTime(beg - 1);
      delta = -delta;
      while (delta > 0) {
        m_data[p] = light;
        --delta;
        p = normalizeTime(p - 1);
      }
    }
  }

  public void scrollLeft() {
    if (m_data.length == 0)
      return;

    int f = m_data[0];
    for (int i = 0; i < m_data.length - 1; ++i)
      m_data[i] = m_data[i + 1];
    m_data[m_data.length - 1] = f;

    m_zeroMarkerPos = normalizeTime(m_zeroMarkerPos - 1);
  }

  public void scrollRight() {
    if (m_data.length == 0)
      return;

    int f = m_data[m_data.length - 1];
    for (int i = m_data.length - 1; i > 0; --i)
      m_data[i] = m_data[i - 1];
    m_data[0] = f;

    m_zeroMarkerPos = normalizeTime(m_zeroMarkerPos + 1);
  }

  public void set(int min, int max, int color) {
    min = normalizeTime(min);
    max = normalizeTime(max);
    int i = min;
    while (i != max) {
      m_data[i] = color;
      i = (i + 1) % getLength();
    }
    m_data[max] = color;
  }

  public void setLength(int len) {
    int[] oldProgram = m_data;
    m_data = new int[len];
    for (int i = 0; i < len; ++i)
      m_data[i] = LightTypes.RED;
    if (oldProgram == null)
      return;
    if (len <= oldProgram.length) {
      for (int i = 0; i < len; ++i)
        m_data[i] = oldProgram[i];
    } else {
      for (int i = 0; i < oldProgram.length; ++i)
        m_data[i] = oldProgram[i];
    }
  }

  public void shrink(int pos, int len) {
    pos = normalizeTime(pos);
    if (len > m_data.length - pos)
      len = m_data.length - pos;

    int[] oldProgram = m_data;
    if (m_data.length - len < 1)
      return;
    setLength(m_data.length - len);
    for (int i = pos; i < m_data.length; ++i)
      m_data[i] = oldProgram[i + len];
  }

  private int skip(int time) {
    int start = time;
    time = normalizeTime(time + 1);
    while (time != start && m_data[time] == m_data[start])
      time = normalizeTime(time + 1);
    if (time == start)
      return -1;
    return time;
  }

  private int skipGreen(int time) {
    int start = time;
    time = normalizeTime(time + 1);
    while (time != start && LightTypes.isGreen(m_data[time]))
      time = normalizeTime(time + 1);
    if (time == start)
      return -1;
    return time;
  }

  private int skipLeft(int time) {
    int start = time;
    time = normalizeTime(time - 1);
    while (time != start && m_data[time] == m_data[start])
      time = normalizeTime(time - 1);
    if (time == start)
      return -1;
    return time;
  }
}
