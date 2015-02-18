/*
 * Created on 2004-07-31
 */

package traffix.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;

import traffix.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Cursors {
  public static final Cursor ARROW = get("cursors/arrow.cur");
  public static final Cursor ARROW_ADD = get("cursors/add.cur");
  public static final Cursor ARROW_HOOK = get("cursors/hook.cur");
  public static final Cursor CROSS = Display.getDefault().getSystemCursor(SWT.CURSOR_CROSS);
  public static final Cursor HAND = Display.getDefault().getSystemCursor(SWT.CURSOR_HAND);
  public static final Cursor SIZEALL = Display.getDefault().getSystemCursor(SWT.CURSOR_SIZEALL);
  private static Map<String, Cursor> m_cursors;
  
  public static void dispose() {
    Iterator<Cursor> it = m_cursors.values().iterator();
    while (it.hasNext()) {
      Cursor im = it.next();
      im.dispose();
    }
    m_cursors.clear();
  }

  public static Cursor get(String path) {
    if (m_cursors == null)
      m_cursors = new HashMap<String, Cursor>();
    Cursor im = m_cursors.get(path);
    if (im == null) {
      im = Utils.loadCursorFromFile(path);
      m_cursors.put(path, im);
    }
    return im;
  }
}