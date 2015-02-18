/*
 * Created on 2004-07-23
 */

package traffix.ui.sim.tools;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.tw.geometry.Vec2f;
import org.tw.geometry.Polygonf;
import traffix.Traffix;
import traffix.core.sim.entities.IEntity;
import traffix.core.sim.entities.Light;
import traffix.ui.Colors;
import traffix.ui.Cursors;
import traffix.ui.Gc;
import traffix.ui.sim.IMapEditor;

public abstract class RectEntityTool extends State {
  private Vec2f m_beg, m_end;
  private float m_width;

  public class ChangeEntityAxis extends State {

    private IEntity m_entity;

    public ChangeEntityAxis(IMapEditor editor, IEntity e) {
      super(editor);
      m_entity = e;
    }

    public void enter() {
      super.enter();
      Point p = getMarkerPosInPixels();
      m_beg = m_editor.getCoordTransformer().screenToTerrain(p.x, p.y);
      m_end = m_beg.copy();
      m_width = clampWidth(1);
      trackEntity();
    }

    public void onMouseDown(MouseEvent e) {
      super.onMouseDown(e);
      //if (e.button == 3) {
      setNextState(new ChangeEntityWidth(m_editor, m_entity));
      //}
    }

    public void onMouseMove(MouseEvent e) {
      super.onMouseMove(e);
      trackEntity();
      m_end = m_editor.getCoordTransformer().screenToTerrain(e.x, e.y);
      trackEntity();
    }
  }

  public class DefineEntityAxis extends State {
    public DefineEntityAxis(IMapEditor editor) {
      super(editor);
    }

    public void enter() {
      super.enter();
      Point p = getMarkerPosInPixels();
      m_beg = m_editor.getCoordTransformer().screenToTerrain(p.x, p.y);
      m_end = m_beg.copy();
      m_width = clampWidth(1);
      trackEntity();
    }

    public void onMouseMove(MouseEvent e) {
      super.onMouseMove(e);
      trackEntity();
      m_end = m_editor.getCoordTransformer().screenToTerrain(e.x, e.y);
      trackEntity();
    }

    private boolean equals(Point a, Point b) {
      return a.x == b.x && a.y == b.y;
    }
    
    public void onMouseDown(MouseEvent e) {
      super.onMouseDown(e);
      if (e.button == 1 && !m_beg.equals(m_end)) {
        setNextState(new DefineEntityWidth(m_editor));
      }
    }
    
    public void onMouseUp(MouseEvent e) {
      super.onMouseUp(e);
      if (e.button == 1 && !m_beg.equals(m_end)) {
        setNextState(new DefineEntityWidth(m_editor));
      }
    }
  }

  public class DefineEntityWidth extends State {
    public DefineEntityWidth(IMapEditor editor) {
      super(editor);
    }

    public void onMouseDown(MouseEvent e) {
      super.onMouseDown(e);
      if (e.button == 1) {
        trackEntity();

        RectEntityTool.this.entityDefined(m_beg, m_end, m_width);

        setNextState(newInstance());
      }
    }

    public void onMouseMove(MouseEvent e) {
      super.onMouseMove(e);
      trackEntity();
      Vec2f end = m_editor.getCoordTransformer().screenToTerrain(e.x, e.y);
      Vec2f d1 = m_end.sub(m_beg);
      Vec2f d2 = new Vec2f(-d1.y, d1.x).normalize();
      float len = d2.dot(end.sub(m_end));
      m_width = clampWidth(len*2);
      trackEntity();
    }
  }

  public class HoverEntityCenter extends State {
    protected IEntity m_entity;

    public HoverEntityCenter(IMapEditor editor, IEntity stop) {
      super(editor);
      m_entity = stop;
    }

    public void enter() {
      super.enter();
      setCursor(Cursors.HAND);
    }

    public void onDoubleClick(MouseEvent e) {
      super.onDoubleClick(e);

      RectEntityTool.this.entityDoubleClicked(m_entity);

      m_editor.getCanvas().redraw();
    }

    public void onKeyDown(KeyEvent e) {
      super.onKeyDown(e);

      if (e.character == SWT.DEL) {
        RectEntityTool.this.entityDeleted(m_entity);
        setNextState(newInstance());
      }
    }

    public void onMouseDown(MouseEvent e) {
      super.onMouseDown(e);
      if (e.button == 3 && (e.stateMask & SWT.CTRL) != 0) {
        setNextState(new MoveEntity(m_editor, m_entity, true));
      } else if (e.button == 3) {
        setNextState(new MoveEntity(m_editor, m_entity, false));
      }
    }

    public void onMouseMove(MouseEvent e) {
      super.onMouseMove(e);

      IEntity ent = detectEntity(getMarkerPosInMeters());
      if (ent != m_entity)
        setNextState(newInstance());
    }
  }

  public class MoveEntity extends State {
    private IEntity m_entity;
    private Point m_beg, m_cur;
    private boolean m_changeShape;

    public MoveEntity(IMapEditor editor, IEntity e, boolean changeShape) {
      super(editor);
      m_entity = e;
      m_changeShape = changeShape;
    }

    public void enter() {
      super.enter();
      setCursor(Cursors.HAND);
      m_beg = m_editor.getCoordTransformer().terrainToScreen(m_entity.getCenter());
      m_cur = m_beg;
      track(m_cur);
    }

    public void onMouseMove(MouseEvent e) {

      super.onMouseMove(e);
      track(m_cur);
      m_cur = getMarkerPosInPixels();
      track(m_cur);
    }

    public void onMouseUp(MouseEvent e) {
      super.onMouseUp(e);
      if (e.button == 3) {
        //entityMoved(m_entity, getMarkerPosInMeters());
        //m_editor.getCanvas().redraw();
        if (m_changeShape) {
          setNextState(new ChangeEntityAxis(m_editor, m_entity));
        } else {
          entityMoved(m_entity, getMarkerPosInMeters());
          m_editor.getCanvas().redraw();
          setNextState(newInstance());
        }
      }
    }

    private void track(Point p) {
      Gc gc = m_editor.getGc();
      gc.setXORMode(true);
      gc.setLineWidth(2);
      gc.setForeground(m_editor.getDisplay().getSystemColor(SWT.COLOR_WHITE));
      gc.setBackground(m_editor.getDisplay().getSystemColor(SWT.COLOR_WHITE));
      gc.fillOval(p.x - 4, p.y - 4, 8, 8);
      gc.setLineWidth(1);
      gc.setLineStyle(SWT.LINE_DOT);
      //Utils.paintArrow(gc, m_beg.x, m_beg.y, p.x, p.y, 10.0f, 0.75f);
      gc.drawLine(m_beg.x, m_beg.y, p.x, p.y);
      gc.setLineStyle(SWT.LINE_SOLID);
      gc.setXORMode(false);
    }

  }

  class ChangeEntityWidth extends State {
    private IEntity m_entity;

    public ChangeEntityWidth(IMapEditor editor, IEntity e) {
      super(editor);
      m_entity = e;
    }

    public void onMouseDown(MouseEvent e) {
      super.onMouseDown(e);
      //if (e.button == 3) {
      trackEntity();

      RectEntityTool.this.entityShapeChanged(m_entity, m_beg, m_end, m_width);
      m_editor.getCanvas().redraw();
      setNextState(newInstance());
      //}
    }

    public void onMouseMove(MouseEvent e) {
      super.onMouseMove(e);
      trackEntity();
      Vec2f end = m_editor.getCoordTransformer().screenToTerrain(e.x, e.y);
      Vec2f d1 = m_end.sub(m_beg);
      Vec2f d2 = new Vec2f(-d1.y, d1.x).normalize();
      float len = d2.dot(end.sub(m_end));
      m_width = clampWidth(len*2);
      trackEntity();
    }
  }

  public RectEntityTool(IMapEditor editor) {
    super(editor);
  }

  public void enter() {
    super.enter();
    setCursor(Cursors.ARROW);

    changeStateIfNeeded();
  }

  public void onMouseDown(MouseEvent e) {
    super.onMouseDown(e);

    if (e.button == 1) {
      setNextState(new DefineEntityAxis(m_editor));
    }
  }

  public void onMouseMove(MouseEvent e) {
    super.onMouseMove(e);

    changeStateIfNeeded();
  }

  protected float clampWidth(float w) {
    return w;
  }

  protected State createState_HoverCenter(IMapEditor ed, IEntity ent) {
    return new HoverEntityCenter(ed, ent);
  }

  protected abstract IEntity detectEntity(Vec2f p);

  protected abstract void entityDefined(Vec2f beg, Vec2f end, float width);

  protected abstract void entityDeleted(IEntity e);

  protected abstract void entityDoubleClicked(IEntity e);

  protected abstract void entityMoved(IEntity e, Vec2f to);

  protected abstract void entityShapeChanged(IEntity e, Vec2f beg, Vec2f end,
    float width);

  protected abstract RectEntityTool newInstance();

  private void changeStateIfNeeded() {
    IEntity e = detectEntity(getMarkerPosInMeters());
    if (e != null) {
      setNextState(createState_HoverCenter(m_editor, e));
    }
  }

  private Point[] makeQuadInScreenSpace() {
    Light light = new Light(Traffix.simManager(), m_beg, m_end, m_width);
    Polygonf bounds = light.getBounds();

    Point[] q = new Point[4];
    for (int i = 0; i < 4; ++i)
      q[i] = m_editor.getCoordTransformer().terrainToScreen(bounds.getPoint(i));
    return q;
  }

  private void trackEntity() {
    Point[] quad = makeQuadInScreenSpace();
    Point p1 = m_editor.getCoordTransformer().terrainToScreen(m_beg);
    Point p2 = m_editor.getCoordTransformer().terrainToScreen(m_end);
    Gc gc = m_editor.getGc();
    gc.setXORMode(true);
    gc.setForeground(m_editor.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    gc.setBackground(Colors.get(new RGB(0, 255, 255)));
    gc.setLineStyle(SWT.LINE_DOT);
    gc.setLineWidth(1);
    gc.drawLine(p1.x, p1.y, p2.x, p2.y);
    gc.setLineStyle(SWT.LINE_SOLID);

    int points[] = new int[8];
    for (int i = 0; i < 8; ++i)
      points[i] = (i%2) == 0 ? quad[i/2].x : quad[i/2].y;
    gc.setLineWidth(1);
    gc.setForeground(Colors.get(new RGB(0, 255, 255)));
    for (int i = 0; i < 4; ++i) {
      int a = i;
      int b = (i + 1)%4;
      gc.drawLine(points[a*2], points[a*2 + 1], points[b*2], points[b*2 + 1]);
    }
    gc.setXORMode(false);
  }
}