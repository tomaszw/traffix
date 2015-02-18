/*
 * Created on 2004-07-30
 */

package traffix.ui.sim;

import org.eclipse.swt.graphics.Point;
import org.tw.geometry.Vec2f;
import org.tw.geometry.Polygonf;

public class CoordinateTransformer {
  private Vec2f m_terrainDimension;
  private Point m_screenDimension;

  public CoordinateTransformer(Vec2f mapDims, Point wndDims) {
    m_terrainDimension = mapDims;
    m_screenDimension = wndDims;
  }

  public Point getScreenDimension() {
    return m_screenDimension;
  }

  public Vec2f getTerrainDimension() {
    return m_terrainDimension;
  }

  public float screenToTerrain(int dim) {
    return screenToTerrain(dim, 0).x;
  }

  public Vec2f screenToTerrain(int x, int y) {
    return new Vec2f((float) x / m_screenDimension.x * m_terrainDimension.x,
        m_terrainDimension.y - (float) y / m_screenDimension.y * m_terrainDimension.y);
  }

  public Vec2f screenToTerrain(Point p) {
    return screenToTerrain(p.x, p.y);
  }

  public int terrainToScreen(float dim) {
    return terrainToScreen(dim, 0).x;
  }

  public Point terrainToScreen(float x, float y) {
    return new Point((int) (x / m_terrainDimension.x * m_screenDimension.x),
        (int) ((m_terrainDimension.y - y) / m_terrainDimension.y * m_screenDimension.y));
  }

  public Point[] terrainToScreen(Polygonf poly) {
    Point[] r = new Point[poly.getNumSides()];
    for (int i = 0; i < r.length; i++) {
      r[i] = terrainToScreen(poly.getPoint(i));
    }
    return r;
  }

  public Point terrainToScreen(Vec2f p) {
    return terrainToScreen(p.x, p.y);
  }
}