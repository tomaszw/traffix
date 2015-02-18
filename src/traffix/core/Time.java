/*
 * Created on 2004-08-20
 */

package traffix.core;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Time {
  public int h, m, s;
  public int weekday;

  public float toSecs() {
    return h*3600 + m*60 + s;
  }

  public static Time fromString(String s) {
    Time t = new Time();
    try {
      int p = s.indexOf('.');
      if (p == -1)
        return null;
      t.weekday = Integer.parseInt(s.substring(0, p));

      s = s.substring(p + 1);
      p = s.indexOf('.');
      if (p == -1)
        return null;
      t.h = Integer.parseInt(s.substring(0, p));

      s = s.substring(p + 1);
      p = s.indexOf('.');
      if (p == -1)
        return null;
      t.m = Integer.parseInt(s.substring(0, p));

      s = s.substring(p + 1);
      t.s = Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return null;
    }

    return t;
  }

  public static Time parseHourMin(String txt) {
    int col = txt.indexOf(':');
    if (col == -1)
      return null;

    try {
      String a = txt.substring(0, col);
      String b = txt.substring(col + 1);

      Time t = new Time();
      t.h = Integer.parseInt(a);
      t.m = Integer.parseInt(b);

      if (t.h < 0 || t.h > 23)
        return null;
      if (t.m < 0 || t.h > 59)
        return null;
      return t;
    } catch (IndexOutOfBoundsException e) {
      return null;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public Time addSecs(int secs) {
    Time t = new Time();
    t.weekday = weekday;
    t.h = h;
    t.m = m;
    t.s = s;
    t.s += secs;
    if (t.s >= 60) {
      t.m += t.s/60;
      t.s %= 60;
      if (t.m >= 60) {
        t.h += t.m/60;
        t.m %= 60;
        if (t.h >= 24) {
          t.weekday += t.h/24;
          t.h %= 24;
          t.weekday %= 7;
        }
      }
    }
    return t;
  }

  public String getHourMinString() {
    NumberFormat fmt = new DecimalFormat("00");
    return fmt.format(h) + ":" + fmt.format(m);
  }

  public String toString() {
    NumberFormat fmt = new DecimalFormat("00");
    return fmt.format(h) + ":" + fmt.format(m) + ":" + fmt.format(s);
    //return weekday + "." + h + "." + m + "." + s;
  }
}