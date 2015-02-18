/*
 * Created on 2004-07-05
 */

package traffix.ui;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class Colors {
  static Map<RGB, Color> m_colors = new HashMap<RGB, Color>();

  public static Color get(RGB rgb) {
    Color cl = m_colors.get(rgb);
    if (cl == null) {
      cl = new Color(Display.getDefault(), rgb);
      m_colors.put(rgb, cl);
    }
    return cl;
  }

  public static void dispose() {
    Iterator<Color> it = m_colors.values().iterator();
    while (it.hasNext()) {
      Color cl = it.next();
      cl.dispose();
    }
    m_colors.clear();
  }

  public static Color system(int col) {
    return Display.getDefault().getSystemColor(col);
  }
}
