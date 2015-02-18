/*
 * Created on 2004-07-10
 */

package traffix.core;

import java.io.IOException;
import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;
import org.tw.geometry.Vec2f;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import traffix.Traffix;
import traffix.Utils;
import traffix.core.model.IPersistent;
import traffix.core.schedule.LightTypes;
import traffix.core.schedule.Schedule;
import traffix.ui.BigImage;
import traffix.ui.Colors;

public class TerrainMap implements IPersistent {
  float m_arrowHeadSz;
  BigImage m_bigImage;
  String m_bmpFilename;

  Color m_clGreen = Colors.get(new RGB(0, 255, 0));
  Color m_clPathnameBkgnd = Colors.get(new RGB(240, 240, 240));
  float m_fatFactor = 1.0f;

  Image m_image, m_cachedImage;
  int m_lineWidth;

  Font m_pathFont;
  List<McmPath> m_paths;
  float m_width = 100, m_height = 100;
  float m_zoom = 0.4f;

  private Map m_pedestrianPathCache = new HashMap();

  public static TerrainMap importFromMcmXml(Element root) {
    TerrainMap map = new TerrainMap();

    NodeList mapNodes = root.getElementsByTagName("map");
    if (mapNodes.getLength() > 0) {
      Element mapElem = (Element) mapNodes.item(0);
      map.m_bmpFilename = mapElem.getAttribute("filename");
      map.m_width = Float.parseFloat(mapElem.getAttribute("sizex"));
      map.m_height = Float.parseFloat(mapElem.getAttribute("sizey"));
    }

    Vector<McmPath> paths = new Vector<McmPath>();

    NodeList groupNodes = root.getElementsByTagName("group");
    for (int i = 0; i < groupNodes.getLength(); ++i) {
      Element groupElem = (Element) groupNodes.item(i);
      int id = i;//Integer.parseInt(groupElem.getAttribute("groupId"));
      String name = groupElem.getAttribute("name");
      NodeList pathNodes = groupElem.getElementsByTagName("path");
      for (int j = 0; j < pathNodes.getLength(); ++j) {
        Element pathElem = (Element) pathNodes.item(j);
        NodeList subpathNodes = pathElem.getElementsByTagName("subpath");
        for (int k = 0; k < subpathNodes.getLength(); ++k) {
          Element subpathElem = (Element) subpathNodes.item(k);
          McmPath path = new McmPath();
          path.setGroupIndex(id);
          path.setGroupName(name);

          NodeList pointNodes = subpathElem.getElementsByTagName("point");
          for (int l = 0; l < pointNodes.getLength(); ++l) {
            Element pointElem = (Element) pointNodes.item(l);
            float x = Float.parseFloat(pointElem.getAttribute("x"));
            float y = Float.parseFloat(pointElem.getAttribute("y"));
            path.addPoint(x, y);
          }
          paths.add(path);
        }
      }
    }

    map.m_paths = paths;
    map.loadImage();

    return map;
  }

  public void clearPaths() {
    m_paths.clear();
  }

  public void defineMap(String bmap, float w, float h) {
    dispose();
    m_bmpFilename = bmap;
    m_width = w;
    m_height = h;
    loadImage();
  }

  public void dispose() {
    if (m_bigImage != null)
      m_bigImage.dispose();
    if (m_image != null)
      m_image.dispose();
    if (m_cachedImage != null)
      m_cachedImage.dispose();
  }

  public Image getBackgroundImage() {
    Rectangle fullBounds = m_image.getBounds();
    Rectangle zoomBounds = getZoomedImageBounds();
    if (m_cachedImage == null || !m_cachedImage.getBounds().equals(zoomBounds)) {
      if (m_cachedImage != null)
        m_cachedImage.dispose();
      m_cachedImage = new Image(Display.getDefault(), zoomBounds);
      GC imgGc = new GC(m_cachedImage);
      imgGc.drawImage(m_image, 0, 0, fullBounds.width, fullBounds.height, 0, 0,
          zoomBounds.width, zoomBounds.height);
      imgGc.dispose();
    }
    return m_cachedImage;
  }

  public float getFatFactor() {
    return m_fatFactor;
  }

  public BigImage getFullImage() {
    if (m_bigImage == null) {
      Image img = new Image(Display.getDefault(), 640, 480);
      m_bigImage = new BigImage(img);
    }
    return m_bigImage;
  }

  public float getHeight() {
    return m_height;
  }

  public String getImageFilename() {
    return m_bmpFilename;
  }

  public McmPath[] getPaths() {
    return m_paths.toArray(new McmPath[m_paths.size()]);
  }

  public Vec2f getTerrainDims() {
    return new Vec2f(m_width, m_height);
  }

  public float getWidth() {
    return m_width;
  }

  public String getXmlTagName() {
    return "terrain-map";
  }

  public float getZoom() {
    return m_zoom;
  }

  public Rectangle getZoomedImageBounds() {
    Rectangle bounds = m_image.getBounds();
    bounds.width = (int) (bounds.width * m_zoom);
    bounds.height = (int) (bounds.height * m_zoom);
    return bounds;
  }

  public void paintBackground(GC gc) {
    gc.drawImage(getBackgroundImage(), 0, 0);
  }

  public void paintGreenPaths(GC gc, int time, Schedule schedule) {
    m_pedestrianPathCache.clear();
    Rectangle ib = m_image.getBounds();
    int size = (ib.width + ib.height) / 2;
    int fh = (int) ((float) size / 100 * m_zoom);
    m_lineWidth = (int) ((float) size * m_fatFactor / 160 * m_zoom);
    m_arrowHeadSz = (int) ((float) size * m_fatFactor / 50 * m_zoom);

    fh = Math.max(1, fh);
    m_pathFont = new Font(Display.getDefault(), "MS Sans Serif", fh, SWT.BOLD);
    Font oldFont = gc.getFont();
    gc.setFont(m_pathFont);
    int[] column = schedule.getColumn(schedule.normalizeTime(time));
    for (int i = 0; i < m_paths.size(); ++i) {
      McmPath path = m_paths.get(i);
      if (LightTypes.isGreen(column[path.getGroupIndex()])) {
        paintPath(gc, path);
      }
    }
    gc.setFont(oldFont);
    m_pathFont.dispose();
  }

  public void setFatFactor(float fatFactor) {
    m_fatFactor = fatFactor;
  }

  public void setPathColor(RGB cl) {
    m_clGreen = Colors.get(cl);
  }

  public void setTerrainDims(float w, float h) {
    m_width = w;
    m_height = h;
  }

  public void setZoom(float zoom) {
    m_zoom = zoom;
  }

  public void setZoomToFit(Rectangle bounds) {
    Rectangle ib = m_image.getBounds();
    float zx = (float) bounds.width / ib.width;
    float zy = (float) bounds.height / ib.height;
    m_zoom = Math.min(zx, zy);
  }

  public boolean xmlLoad(Document document, Element root) {
    float width = Float.parseFloat(root.getAttribute("width"));
    float height = Float.parseFloat(root.getAttribute("height"));
    String bmpFilename = root.getAttribute("bmpFilename");

    NodeList pathNodes = root.getElementsByTagName("path");
    Vector<McmPath> paths = new Vector<McmPath>();
    for (int i = 0; i < pathNodes.getLength(); ++i) {
      Element pathElem = (Element) pathNodes.item(i);
      McmPath path = new McmPath();
      if (!path.xmlLoad(document, pathElem))
        return false;
      paths.add(path);
    }

    m_width = width;
    m_height = height;
    m_bmpFilename = bmpFilename;
    m_paths = paths;

    loadImage();
    return true;
  }

  public Element xmlSave(Document document) {
    Element root = document.createElement("terrain-map");
    root.setAttribute("width", Float.toString(m_width));
    root.setAttribute("height", Float.toString(m_height));
    root.setAttribute("bmpFilename", m_bmpFilename);

    if (m_paths != null) {
      for (int i = 0; i < m_paths.size(); ++i) {
        McmPath path = m_paths.get(i);
        Element pathElem = path.xmlSave(document);
        if (pathElem == null)
          return null;
        root.appendChild(pathElem);
      }
    }
    return root;
  }

  private void loadImage() {
    if (m_image != null)
      m_image.dispose();
    if (m_cachedImage != null)
      m_cachedImage.dispose();
    if (m_bigImage != null)
      m_bigImage.dispose();

    m_image = null;
    try {
      if (m_bmpFilename != null && m_bmpFilename.length() > 0)
        m_image = new Image(Display.getDefault(), m_bmpFilename);
    } catch (Throwable ex) {
      Display.getDefault().syncExec(new Runnable() {

        public void run() {
          Traffix.inform("B³¹d podczas odczytu obrazka " + m_bmpFilename);
        }
      });
    }
    m_bigImage = new BigImage(m_image);
  }

  private void paintGroupLabel(GC gc, VehicleGroup group, float x1, float y1) {
    String name = group.getPrefix() + group.getNum();
    Point extent = gc.textExtent(name);
    int x = (int) x1 - extent.x / 2;
    int y = (int) y1 - extent.y / 2;
    gc.setLineWidth(1);
    gc.setBackground(m_clPathnameBkgnd);
    gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
    gc.fillRectangle(x - 4, y - 1, extent.x + 6, extent.y + 2);
    gc.drawRectangle(x - 4, y - 1, extent.x + 6, extent.y + 2);
    gc.drawText(name, x, y);
  }

  private void paintPath(GC gc, McmPath path) {
    VehicleGroup group = Traffix.model().getGroupByIndex(path.getGroupIndex());

    Rectangle imgBounds = getZoomedImageBounds();
    float sx = imgBounds.width / m_width;
    float sy = imgBounds.height / m_height;

    if (!group.getPrefix().equals("p") && !group.getPrefix().equals("r"))
      paintRegularPath(gc, path, group, sx, sy);
    else {
      if (path.getNumPoints() <= 2)
        paintRegularPath(gc, path, group, sx, sy);
      else
        paintPedestrianPath(gc, path, group, sx, sy);
    }
  }

  private void paintPedestrianPath(GC gc, McmPath path, VehicleGroup group, float sx,
      float sy) {
    int x1, y1, x2, y2;
    Vec2f a = path.getPoint(0);
    Vec2f b = path.getPoint(1);
    Vec2f c = path.getPoint(2);

    boolean reverse = true;
    if (!m_pedestrianPathCache.containsKey(new Integer(group.getUniqueID()))) {
      m_pedestrianPathCache.put(new Integer(group.getUniqueID()), null);
      reverse = false;
    }

    gc.setForeground(m_clGreen);
    gc.setBackground(m_clGreen);
    gc.setLineWidth(m_lineWidth);

    x1 = (int) (a.x * sx);
    y1 = (int) ((m_height - a.y) * sy);
    x2 = (int) (b.x * sx);
    y2 = (int) ((m_height - b.y) * sy);

    int labx = x1;
    int laby = y1;

    gc.drawLine(x1, y1, x2, y2);
    if (reverse) {
      Vec2f tmp = b;
      b = c;
      c = tmp;
    }
    x1 = (int) (b.x * sx);
    y1 = (int) ((m_height - b.y) * sy);
    x2 = (int) (c.x * sx);
    y2 = (int) ((m_height - c.y) * sy);
    Utils.paintArrow(gc, x1, y1, x2, y2, m_arrowHeadSz, 0.4f);

    if (reverse)
      paintGroupLabel(gc, group, labx, laby);
  }

  private void paintRegularPath(GC gc, McmPath path, VehicleGroup group, float sx,
      float sy) {
    for (int i = 0; i < path.getNumPoints() - 1; ++i) {
      gc.setForeground(m_clGreen);
      gc.setBackground(m_clGreen);
      gc.setLineWidth(m_lineWidth);

      Vec2f a = path.getPoint(i);
      Vec2f b = path.getPoint(i + 1);

      float x1 = a.x * sx;
      float y1 = (m_height - a.y) * sx;
      float x2 = b.x * sy;
      float y2 = (m_height - b.y) * sy;
      if (i < path.getNumPoints() - 2)
        gc.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
      else
        Utils.paintArrow(gc, (int) x1, (int) y1, (int) x2, (int) y2, m_arrowHeadSz, 0.4f);

      if (i == 0) {
        paintGroupLabel(gc, group, x1, y1);
      }
    }
  }
}