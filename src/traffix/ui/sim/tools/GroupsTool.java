/*
 * Created on 2004-07-23
 */

package traffix.ui.sim.tools;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import traffix.Traffix;
import traffix.core.sim.Route;
import traffix.core.sim.graph.Node;
import traffix.ui.Cursors;
import traffix.ui.Gc;
import traffix.ui.sim.*;

public class GroupsTool extends State {
  private Font m_font;
  private MapSensor m_sensor;

  class HoverLabel extends State {
    private Node m_node;

    public HoverLabel(IMapEditor editor, Node node) {
      super(editor);
      m_node = node;
    }

    public void enter() {
      super.enter();
      setCursor(Cursors.HAND);
    }

    public void onDoubleClick(MouseEvent e) {
      super.onDoubleClick(e);

      RouteEditDialog dlg = new RouteEditDialog(m_editor.getShell(), m_node, m_editor);
      dlg.open();
      if (m_sensor.senseNode(getMarkerPosInMeters()) != m_node)
        setNextState(new GroupsTool(m_editor));
    }

    public void leave() {
      super.leave();
      m_editor.getCanvas().redraw();
    }

    public void onMouseDown(MouseEvent e) {
      super.onMouseDown(e);
      if (e.button == 1) {
        setNextState(new MoveLabel(m_editor, m_node));
      }
    }

    public void onMouseMove(MouseEvent e) {
      super.onMouseMove(e);
      if (m_sensor.senseNodeLabel(getMarkerPosInMeters()) != m_node)
        setNextState(new GroupsTool(m_editor));
    }
  }

  class HoverNode extends State {
    private Node m_node;
    private PaintListener m_paintListener;

    public HoverNode(IMapEditor editor, Node node) {
      super(editor);
      m_node = node;
    }

    public void enter() {
      super.enter();
      setCursor(Cursors.HAND);
      m_font = new Font(Display.getDefault(), "Lucida Console", 8, SWT.NONE);
      paintNodeInfo();

      m_paintListener = new PaintListener() {
        public void paintControl(PaintEvent e) {
          paintNodeInfo();
        }
      };
      m_editor.getCanvas().addPaintListener(m_paintListener);
    }

    public void leave() {
      super.leave();
      m_editor.getCanvas().removePaintListener(m_paintListener);
      m_font.dispose();
      m_editor.getCanvas().redraw();
    }

    public void onDoubleClick(MouseEvent e) {
      super.onDoubleClick(e);

      RouteEditDialog dlg = new RouteEditDialog(m_editor.getShell(), m_node, m_editor);
      dlg.open();
      if (m_sensor.senseNode(getMarkerPosInMeters()) != m_node)
        setNextState(new GroupsTool(m_editor));
    }

    @Override
    public void onMouseDown(MouseEvent e) {
      super.onMouseDown(e);
//      if (e.button == 3) {
//        RouteEditDialog dlg = new RouteEditDialog(m_editor.getShell(), m_node, 
//            m_editor);
//        dlg.setBlockOnOpen(false);
//        dlg.open();
//      }
    }
    
    public void onMouseMove(MouseEvent e) {
      super.onMouseMove(e);
      if (m_sensor.senseNode(getMarkerPosInMeters()) != m_node)
        setNextState(new GroupsTool(m_editor));
    }

    private void centerText(Gc gc, String txt, int x, int y, boolean transparent) {
      Point sz = gc.textExtent(txt);
      gc.drawText(txt, x - sz.x/2, y - sz.y/2, transparent);
    }

    private void paintNodeInfo() {
    }

  }

  class MoveLabel extends State {
    private Node m_node;

    public MoveLabel(IMapEditor editor, Node node) {
      super(editor);
      m_node = node;
    }

    public void onDoubleClick(MouseEvent e) {
      super.onDoubleClick(e);

      RouteEditDialog dlg = new RouteEditDialog(m_editor.getShell(), m_node, m_editor);
      dlg.open();
      if (m_sensor.senseNode(getMarkerPosInMeters()) != m_node)
        setNextState(new GroupsTool(m_editor));
    }

    public void onMouseUp(MouseEvent e) {
      super.onMouseUp(e);
      if (e.button == 1) {
        m_node.labelPos = getMarkerPosInMeters();
        Traffix.model().setModified(true);
        m_editor.getCanvas().redraw();
        setNextState(new GroupsTool(m_editor));
      }
    }

  }

  public GroupsTool(IMapEditor editor) {
    super(editor);

    m_sensor = new MapSensor(editor);
    m_sensor.setThresholdInPixels(8);
  }

  public void enter() {
    super.enter();
    setCursor(Cursors.ARROW);
    checkStateTransit();
  }

  public void onMouseMove(MouseEvent e) {
    super.onMouseMove(e);
    checkStateTransit();
  }

  private void checkStateTransit() {

    Node nodeLab = m_sensor.senseNodeLabel(getMarkerPosInMeters());
    if (Traffix.simManager().simParams().showNodeLabels && nodeLab != null)
      setNextState(new HoverLabel(m_editor, nodeLab));
    else {
      Node node = m_sensor.senseNode(getMarkerPosInMeters());
      if (node != null)
        setNextState(new HoverNode(m_editor, node));
    }
  }

}