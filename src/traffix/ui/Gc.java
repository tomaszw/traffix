/*
 * Created on 2004-08-01
 */

package traffix.ui;

import org.eclipse.swt.graphics.*;


public class Gc {
  protected GC m_gc;
  protected Point m_origin = new Point(0, 0);

  public Gc(GC gc) {
    this.m_gc = gc;
  }

  public void copyArea(Image image, int x, int y) {
    m_gc.copyArea(image, x, y);
  }

  public void copyArea(int srcX, int srcY, int width, int height, int destX, int destY) {
    m_gc.copyArea(tx(srcX), ty(srcY), width, height, destX, destY);
  }

  public void dispose() {
    m_gc.dispose();
  }

  public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
    m_gc.drawArc(tx(x), ty(y), width, height, startAngle, arcAngle);
  }

  public void drawFocus(int x, int y, int width, int height) {
    m_gc.drawFocus(tx(x), ty(y), width, height);
  }

  public void drawImage(Image image, int x, int y) {
    m_gc.drawImage(image, tx(x), ty(y));
  }

  public void drawImage(Image image, int srcX, int srcY, int srcWidth, int srcHeight,
    int destX, int destY, int destWidth, int destHeight) {
    m_gc.drawImage(image, srcX, srcY, srcWidth, srcHeight, tx(destX), ty(destY),
      destWidth, destHeight);
  }

  public void drawLine(int x1, int y1, int x2, int y2) {
    m_gc.drawLine(tx(x1), ty(y1), tx(x2), ty(y2));
  }

  public void drawOval(int x, int y, int width, int height) {
    m_gc.drawOval(tx(x), ty(y), width, height);
  }

  public void drawPoint(int x, int y) {
    m_gc.drawPoint(tx(x), ty(y));
  }

  public void drawPolygon(int[] pointArray) {
    int[] copy = (int[]) pointArray.clone();
    for (int i = 0; i < copy.length; i += 2) {
      copy[i] = tx(copy[i]);
      copy[i + 1] = ty(copy[i + 1]);
    }
    m_gc.drawPolygon(copy);
  }

  public void drawPolyline(int[] pointArray) {
    int[] copy = (int[]) pointArray.clone();
    for (int i = 0; i < copy.length; i += 2) {
      copy[i] = tx(copy[i]);
      copy[i + 1] = ty(copy[i + 1]);
    }
    m_gc.drawPolyline(copy);
  }

  public void drawRectangle(int x, int y, int width, int height) {
    m_gc.drawRectangle(tx(x), ty(y), width, height);
  }

  public void drawRectangle(Rectangle rect) {
    drawRectangle(rect.x, rect.y, rect.width, rect.height);
  }

  public void drawRoundRectangle(int x, int y, int width, int height, int arcWidth,
    int arcHeight) {
    m_gc.drawRoundRectangle(tx(x), ty(y), width, height, arcWidth, arcHeight);
  }

  public void drawString(String string, int x, int y) {
    m_gc.drawString(string, tx(x), ty(y));
  }

  public void drawString(String string, int x, int y, boolean isTransparent) {
    m_gc.drawString(string, tx(x), ty(y), isTransparent);
  }

  public void drawText(String string, int x, int y) {
    m_gc.drawText(string, tx(x), ty(y));
  }

  public void drawText(String string, int x, int y, boolean isTransparent) {
    m_gc.drawText(string, tx(x), ty(y), isTransparent);
  }

  public void drawText(String string, int x, int y, int flags) {
    m_gc.drawText(string, tx(x), ty(y), flags);
  }

  public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
    m_gc.fillArc(tx(x), ty(y), width, height, startAngle, arcAngle);
  }

  public void fillGradientRectangle(int x, int y, int width, int height, boolean vertical) {
    m_gc.fillGradientRectangle(tx(x), ty(y), width, height, vertical);
  }

  public void fillOval(int x, int y, int width, int height) {
    m_gc.fillOval(tx(x), ty(y), width, height);
  }

  public void fillPolygon(int[] pointArray) {
    int[] copy = (int[]) pointArray.clone();
    for (int i = 0; i < copy.length; i += 2) {
      copy[i] = tx(copy[i]);
      copy[i + 1] = ty(copy[i + 1]);
    }
    m_gc.fillPolygon(copy);
  }

  public void fillRectangle(int x, int y, int width, int height) {
    m_gc.fillRectangle(tx(x), ty(y), width, height);
  }

  public void fillRectangle(Rectangle rect) {
    fillRectangle(rect.x, rect.y, rect.width, rect.height);
  }

  public void fillRoundRectangle(int x, int y, int width, int height, int arcWidth,
    int arcHeight) {
    m_gc.fillRoundRectangle(tx(x), ty(y), width, height, arcWidth, arcHeight);
  }

  public int getAdvanceWidth(char ch) {
    return m_gc.getAdvanceWidth(ch);
  }

  public Color getBackground() {
    return m_gc.getBackground();
  }

  public int getCharWidth(char ch) {
    return m_gc.getCharWidth(ch);
  }

  public Rectangle getClipping() {
    return m_gc.getClipping();
  }

  public void getClipping(Region region) {
    m_gc.getClipping(region);
  }

  public Font getFont() {
    return m_gc.getFont();
  }

  public FontMetrics getFontMetrics() {
    return m_gc.getFontMetrics();
  }

  public Color getForeground() {
    return m_gc.getForeground();
  }

  public int getLineStyle() {
    return m_gc.getLineStyle();
  }

  public int getLineWidth() {
    return m_gc.getLineWidth();
  }

  public int getStyle() {
    return m_gc.getStyle();
  }

  public GC getSwtGc() {
    return m_gc;
  }

  public Point getViewportOrigin() {
    return m_origin;
  }

  public boolean getXORMode() {
    return m_gc.getXORMode();
  }

  public boolean isClipped() {
    return m_gc.isClipped();
  }

  public boolean isDisposed() {
    return m_gc.isDisposed();
  }

  public void setBackground(Color color) {
    m_gc.setBackground(color);
  }

  public void setClipping(int x, int y, int width, int height) {
    m_gc.setClipping(tx(x), ty(y), width, height);
  }

  public void setClipping(Rectangle rect) {
    m_gc.setClipping(tx(rect.x), ty(rect.y), rect.width, rect.height);
  }

  public void setClipping(Region region) {
    m_gc.setClipping(region);
  }

  public void setFont(Font font) {
    m_gc.setFont(font);
  }

  public void setForeground(Color color) {
    m_gc.setForeground(color);
  }

  public void setLineStyle(int lineStyle) {
    m_gc.setLineStyle(lineStyle);
  }

  public void setLineWidth(int lineWidth) {
    m_gc.setLineWidth(lineWidth);
  }

  public void setViewportOrigin(int x, int y) {
    m_origin = new Point(x, y);
  }

  public void setViewportOrigin(Point p) {
    setViewportOrigin(p.x, p.y);
  }

  public void setXORMode(boolean xor) {
    m_gc.setXORMode(xor);
  }

  public Point stringExtent(String string) {
    return m_gc.stringExtent(string);
  }

  public Point textExtent(String string) {
    return m_gc.textExtent(string);
  }

  public Point textExtent(String string, int flags) {
    return m_gc.textExtent(string, flags);
  }

  public int tx(int x) {
    return x - m_origin.x;
  }

  public int ty(int y) {
    return y - m_origin.y;
  }
}

