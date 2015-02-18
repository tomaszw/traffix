/*
 * Created on 2004-07-23
 */

package traffix.ui.sim.tools;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import traffix.Traffix;
import traffix.core.sim.graph.Node;
import traffix.ui.Cursors;
import traffix.ui.Gc;
import traffix.ui.sim.AccidentNodeDialog;
import traffix.ui.sim.IMapEditor;
import traffix.ui.sim.MapSensor;

public class AccidentNodesTool extends State {
  private Font      m_font;
  private MapSensor m_sensor;

  class HoverNode extends State {
    private Node          m_node;
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

      AccidentNodeDialog dlg = new AccidentNodeDialog(m_editor.getShell(), m_node,
          Traffix.model().getActiveAccident());
      dlg.open();
      if (m_sensor.senseNode(getMarkerPosInMeters()) != m_node)
        setNextState(new AccidentNodesTool(m_editor));
    }

    @Override
    public void onMouseDown(MouseEvent e) {
      super.onMouseDown(e);
      // if (e.button == 3) {
      // RouteEditDialog dlg = new RouteEditDialog(m_editor.getShell(), m_node,
      // m_editor);
      // dlg.setBlockOnOpen(false);
      // dlg.open();
      // }
    }

    public void onMouseMove(MouseEvent e) {
      super.onMouseMove(e);
      if (m_sensor.senseNode(getMarkerPosInMeters()) != m_node)
        setNextState(new AccidentNodesTool(m_editor));
    }

    private void centerText(Gc gc, String txt, int x, int y, boolean transparent) {
      Point sz = gc.textExtent(txt);
      gc.drawText(txt, x - sz.x / 2, y - sz.y / 2, transparent);
    }

    private void paintNodeInfo() {
    }

  }

  public AccidentNodesTool(IMapEditor editor) {
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

    Node node = m_sensor.senseNode(getMarkerPosInMeters());
    if (node != null)
      setNextState(new HoverNode(m_editor, node));
  }

}