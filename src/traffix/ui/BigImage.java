/*
 * Created on 2004-08-24
 */

package traffix.ui;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;

import java.awt.image.BufferedImage;

public class BigImage {
  private Image m_cachedPortion;
  private Rectangle m_cachedPortionBounds = new Rectangle(0, 0, 0, 0);
  private Point m_cachedSize = new Point(0, 0);
  private Image m_image;

  public BigImage(Image image) {
    m_image = image;
  }

  public Rectangle getBounds() {
    return m_image.getBounds();
  }

  public void dispose() {
    if (m_cachedPortion != null) {
      m_cachedPortion.dispose();
      m_cachedPortion = null;
    }
  }

  public Image getImage() {
    return m_image;
  }

  public Image getImagePortion(Rectangle bounds) {
    return getImagePortion(bounds, new Point(bounds.width, bounds.height));
  }

  public Image getImagePortion(Rectangle bounds, Point destSize) {
    if (m_cachedPortion != null && bounds.equals(m_cachedPortionBounds) && destSize.equals(m_cachedSize))
      return m_cachedPortion;

    m_cachedPortionBounds = new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height);
    m_cachedSize = new Point(destSize.x, destSize.y);
    if (m_cachedPortion != null)
      m_cachedPortion.dispose();
    m_cachedPortion = new Image(Display.getDefault(), destSize.x, destSize.y);
    scaleImage(m_image, bounds, m_cachedPortion, m_cachedPortion.getBounds());
    return m_cachedPortion;
  }

  private void scaleImage(Image src, Rectangle srcBounds, Image dst, Rectangle dstBounds) {
    GC gc = new GC(dst);

    gc.drawImage(src, srcBounds.x, srcBounds.y, srcBounds.width, srcBounds.height, dstBounds.x, dstBounds.y,
      dstBounds.width, dstBounds.height);
    gc.dispose();
  }

  private void scaleImage2(Image src, Rectangle srcBounds, Image dst, Rectangle dstBounds) {
    ImageData srcData = src.getImageData();
    ImageData dstData = dst.getImageData();

    float scalex = 1;//(float)srcBounds.width / dstBounds.width;
    float scaley = 1;//(float)srcBounds.height / dstBounds.height;
    for (int y = 0; y < dstBounds.height; ++y) {
      for (int x = 0; x < dstBounds.width; ++x) {
        float sx1 = srcBounds.x + x*scalex;
        float sy1 = srcBounds.y + y*scaley;
        int bgr = srcData.getPixel(x, y);//(int)sx1, (int)sy1);
        dstData.setPixel(dstBounds.x + x, dstBounds.y + y, bgr);
      }
    }

    Image out = new Image(Display.getDefault(), dstData);
    GC tmpGc = new GC(dst);
    tmpGc.drawImage(out, 0, 0);
    tmpGc.dispose();
    out.dispose();

  }

  private BufferedImage swtImageToAwtImage(Image src) {
    ImageData data = src.getImageData();
    Rectangle bounds = src.getBounds();
    int w = bounds.width;
    int h = bounds.height;
    BufferedImage out = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < h; ++y) {
      for (int x = 0; x < w; ++x) {
        int bgr = data.getPixel(x, y);
        int rgb = 0;
        rgb |= (bgr & 0xff0000) >> 16;
        rgb |= (bgr & 0x00ff00);
        rgb |= (bgr & 0x0000ff) << 16;
        out.setRGB(x, y, rgb);
      }
    }
    return out;
  }

  private Image awtImageToSwtImage(BufferedImage src) {
    int w = src.getWidth(null);
    int h = src.getHeight(null);
    Image out = new Image(Display.getDefault(), w, h);
    ImageData data = out.getImageData();
    for (int y = 0; y < h; ++y) {
      for (int x = 0; x < w; ++x) {
        int rgb = src.getRGB(x, y);
        int bgr = 0;
        bgr |= (rgb & 0xff0000) >> 16;
        bgr |= (rgb & 0x00ff00);
        bgr |= (rgb & 0x0000ff) << 16;
        data.setPixel(x, y, bgr);
      }
    }
    return out;
  }
}