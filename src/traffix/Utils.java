/*
 * Created on 2004-07-08
 */

package traffix;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.widgets.Display;

import traffix.ui.Gc;

public class Utils {

  public static void forceToolbarText(ToolBarManager tbManager) {
    IContributionItem[] toolCbItems = tbManager.getItems();
    for (int i = 0; i < toolCbItems.length; ++i) {
      if (toolCbItems[i] instanceof ActionContributionItem) {
        ActionContributionItem cb = (ActionContributionItem) toolCbItems[i];
        cb.setMode(ActionContributionItem.MODE_FORCE_TEXT);
      }
    }
  }

  public static void paintArrow(GC gc, int x1, int y1, int x2, int y2, float headsize,
    float s) {
    float[] vec = new float[2];
    vec[0] = x2 - x1;
    vec[1] = y2 - y1;

    float len = (float) Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1]);
    if (len != 0) {
      vec[0] /= len;
      vec[1] /= len;
    }

    int hbx, hby;
    hbx = (int) (x2 - headsize*vec[0]);
    hby = (int) (y2 - headsize*vec[1]);

    float[] vecortho = new float[2];
    vecortho[1] = vec[0];
    vecortho[0] = -vec[1];

    int lx, ly, rx, ry;

    lx = (int) (hbx - headsize*vecortho[0]*s);
    ly = (int) (hby - headsize*vecortho[1]*s);

    rx = (int) (hbx + headsize*vecortho[0]*s);
    ry = (int) (hby + headsize*vecortho[1]*s);

    gc.drawLine(x1, y1, hbx, hby);
    int linew = gc.getLineWidth();
    int[] poly = {lx, ly, rx, ry, x2, y2};
    gc.fillPolygon(poly);
    gc.setLineWidth(1);
    gc.drawPolygon(poly);
    gc.setLineWidth(linew);
  }

  public static void paintArrow(Gc gc, int x1, int y1, int x2, int y2, float headsize,
    float s) {
    float[] vec = new float[2];
    vec[0] = x2 - x1;
    vec[1] = y2 - y1;

    float len = (float) Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1]);
    if (len != 0) {
      vec[0] /= len;
      vec[1] /= len;
    }

    int hbx, hby;
    hbx = (int) (x2 - headsize*vec[0]);
    hby = (int) (y2 - headsize*vec[1]);

    float[] vecortho = new float[2];
    vecortho[1] = vec[0];
    vecortho[0] = -vec[1];

    int lx, ly, rx, ry;

    lx = (int) (hbx - headsize*vecortho[0]*s);
    ly = (int) (hby - headsize*vecortho[1]*s);

    rx = (int) (hbx + headsize*vecortho[0]*s);
    ry = (int) (hby + headsize*vecortho[1]*s);

    gc.drawLine(x1, y1, hbx, hby);
    int linew = gc.getLineWidth();
    int[] poly = {lx, ly, rx, ry, x2, y2};
    gc.fillPolygon(poly);
    gc.setLineWidth(1);
    gc.drawPolygon(poly);
    gc.setLineWidth(linew);
  }

  public static Cursor loadCursorFromFile(String file) {
    Cursor cursor = null;
    byte[] blaah = {0, 0, 0, 0, 0, 0, 0, 0};

    PaletteData pdata = new PaletteData(new RGB[]{new RGB(0, 0, 0),
                                                  new RGB(255, 255, 255)});
    ImageData imgdata = new ImageData(1, 1, 1, pdata);
    cursor = new Cursor(Display.getDefault(), imgdata, imgdata, 0, 0);
    OS.DestroyCursor(cursor.handle);
    int newhandle = NativeUtils.Win_LoadCursorFromFile(file);
    if (newhandle == 0) {
      cursor.dispose();
      return null;
    }
    cursor.handle = newhandle;
    return cursor;
  }
}