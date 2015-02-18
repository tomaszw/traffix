/*
 * Created on 2004-07-03
 */

package traffix.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Images {
  private static Map<String, Image> m_images = new HashMap<String, Image>();

  public static void dispose() {
    Iterator<Image> it = m_images.values().iterator();
    while (it.hasNext()) {
      Image im = it.next();
      im.dispose();
    }
    m_images.clear();
  }

  public static Image get(String path) {
    Image im = m_images.get(path);
    if (im == null) {
      im = new Image(Display.getDefault(), path);
      //im = new Image(Display.getDefault(), Images.class.getResourceAsStream(path));
      m_images.put(path, im);
    }
    return im;
  }

  public static ImageDescriptor getDescriptor(String path) {
    ImageDescriptor desc = null;
    try {
      desc = ImageDescriptor.createFromURL(new URL("file:" + path));
      //desc = ImageDescriptor.createFromURL(Images.class.getResource(path));
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return desc;
  }
}