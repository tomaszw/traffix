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
import traffix.Traffix;
import traffix.core.sim.graph.Graph;
import traffix.core.sim.graph.GraphLocation;
import traffix.core.sim.graph.Node;
import traffix.ui.Colors;
import traffix.ui.Cursors;
import traffix.ui.Gc;
import traffix.ui.sim.IMapEditor;
import traffix.ui.sim.MapSensor;

public class AccidentPathsTool extends State {
  private static final int BTN_ADD = 3;
  private static final int BTN_MOVE = 1;
  private static final int NODE_SENSE_THR = 8;
  private MapSensor m_sensor;

  private boolean canLink(Node n1, Node n2) {
    return !n2.isBeginningNode();
  }

  class AddNode extends State {
    private Point m_cur;
    private Node m_prevNode;

    public AddNode(IMapEditor editor, Node prev) {
      super(editor);
      m_prevNode = prev;
      if (m_prevNode == null) {
        m_prevNode = new Node(Traffix.simManager().getGraph());
        m_prevNode.setPos(getMarkerPosInMeters());
      }
    }

    public void enter() {
      super.enter();
      m_cur = getMarkerPosInPixels();
      trackLine(m_cur);
      setCursor(Cursors.ARROW_ADD);
    }

    public void onMouseMove(MouseEvent e) {
      super.onMouseMove(e);
      trackLine(m_cur);
      m_cur = new Point(e.x, e.y);
      trackLine(m_cur);
    }

    public void onMouseUp(MouseEvent e) {
      super.onMouseUp(e);
      if (e.button == BTN_ADD) {
        Vec2f p = getMarkerPosInMeters();
        Node nextNode = m_sensor.senseNode(p);
        if (nextNode == m_prevNode) {
          m_editor.getCanvas().redraw();
          setNextState(new AccidentPathsTool(m_editor));
          return;
        } else if (nextNode != null && !canLink(m_prevNode, nextNode)) {
          nextNode = null;
        }

        //Traffix.simManager().getGraph().addNode(m_prevNode);

        if (nextNode == null) {
          nextNode = new Node(Traffix.simManager().getGraph());
          nextNode.setPos(p);
          //Traffix.simManager().getGraph().addNode(nextNode);
        }

        //Traffix.simManager().getGraph().link(m_prevNode, nextNode);
        //Traffix.simManager().getGraph().update();
        Traffix.simManager().getGraph().addEdge(m_prevNode, nextNode);
        Traffix.simManager().fireUpdated();
        Traffix.model().setModified(true);
        setNextState(new AccidentPathsTool(m_editor));
      }
    }

    private void trackLine(Point p) {
      Gc gc = m_editor.getGc();
      gc.setXORMode(true);
      gc.setLineWidth(2);
      gc.setForeground(m_editor.getDisplay().getSystemColor(SWT.COLOR_WHITE));
      gc.fillOval(p.x - 4, p.y - 4, 8, 8);
      gc.setLineWidth(1);
      Point beg = m_editor.getCoordTransformer().terrainToScreen(m_prevNode.getPos());
      gc.drawLine(beg.x, beg.y, p.x, p.y);
      gc.setXORMode(false);
    }
  }

  class HoverNode extends State {
    private Node m_node;

    public HoverNode(IMapEditor editor, Node node) {
      super(editor);
      m_node = node;
    }

    public void enter() {
      super.enter();
      setCursor(Cursors.HAND);
    }


    public void onMouseDown(MouseEvent e) {
      super.onMouseDown(e);

      if (e.button == BTN_MOVE) {
        setNextState(new MoveNode(m_editor, m_node));
      } else if (e.button == BTN_ADD) {
        if (m_node.getNumOutgoingNodes() == 0) {
          setNextState(new AddNode(m_editor, m_node));
        }
      }
    }

    public void onKeyDown(KeyEvent e) {
      super.onKeyDown(e);
      if (e.character == SWT.DEL) {
        Traffix.simManager().getGraph().deleteNode(m_node);
        m_editor.getCanvas().redraw();
        setNextState(new AccidentPathsTool(m_editor));
      }
    }

    public void onMouseMove(MouseEvent e) {
      super.onMouseMove(e);
      Node node = m_sensor.senseNode(getMarkerPosInMeters());
      if (node == null)
        setNextState(new AccidentPathsTool(m_editor));
      else if (node != m_node)
        setNextState(new HoverNode(m_editor, node));
    }
  }

  class MoveNode extends State {
    private Point m_beg, m_cur;
    private Node m_node;

    public MoveNode(IMapEditor editor, Node node) {
      super(editor);
      m_node = node;
    }

    public void enter() {
      super.enter();
      setCursor(Cursors.HAND);
      m_beg = new Point(m_hoverInfo.pixelX, m_hoverInfo.pixelY);
      m_cur = new Point(m_beg.x, m_beg.y);
      trackPoint(m_cur);

      Point p = m_editor.getCoordTransformer().terrainToScreen(m_node.getPos());
      Gc gc = m_editor.getGc();
      gc.setLineWidth(1);
      gc.setForeground(Colors.get(new RGB(0, 0, 255)));
      gc.drawOval(p.x - 6, p.y - 6, 12, 12);
    }

    public void onMouseMove(MouseEvent e) {
      super.onMouseMove(e);
      trackPoint(m_cur);
      m_cur.x = e.x;
      m_cur.y = e.y;
      trackPoint(m_cur);
    }

    public void onMouseUp(MouseEvent e) {
      super.onMouseUp(e);
      if (e.button == BTN_MOVE) {
        m_node.setPos(new Vec2f(getMarkerPosInMeters()));
        Traffix.simManager().getGraph().update();
        m_editor.getCanvas().redraw();
        Traffix.model().setModified(true);
        setNextState(new AccidentPathsTool(m_editor));
      }
    }

    private void trackPoint(Point p) {
      Gc gc = m_editor.getGc();
      gc.setXORMode(true);
      gc.setLineWidth(2);
      gc.setForeground(m_editor.getDisplay().getSystemColor(SWT.COLOR_WHITE));
      gc.fillOval(p.x - 4, p.y - 4, 8, 8);
      gc.setLineWidth(1);
      gc.setLineStyle(SWT.LINE_DOT);
      //Utils.paintArrow(gc, m_beg.x, m_beg.y, p.x, p.y, 10.0f, 0.75f);
      gc.drawLine(m_beg.x, m_beg.y, p.x, p.y);
      gc.setLineStyle(SWT.LINE_SOLID);
      gc.setXORMode(false);
    }
  }

  public AccidentPathsTool(IMapEditor editor) {
    super(editor);
    m_sensor = new MapSensor(editor);
    m_sensor.setThresholdInPixels(NODE_SENSE_THR);
  }

  public void enter() {
    super.enter();
    Node node = m_sensor.senseNode(getMarkerPosInMeters());
    if (node != null)
      setNextState(new HoverNode(m_editor, node));
    else
      setCursor(Cursors.ARROW);
  }

  public void onKeyDown(KeyEvent e) {
    super.onKeyDown(e);
    if (e.keyCode == SWT.INSERT) {
      Graph g = Traffix.simManager().getGraph();
      GraphLocation loc = g.closestLocation(getMarkerPosInMeters(), 1);
      if (loc != null) {
        g.addNewNodeAt(loc);
        Traffix.model().setModified(true);
        Traffix.simManager().getGraph().update();
        Traffix.simManager().fireUpdated();
        Node node = m_sensor.senseNode(getMarkerPosInMeters());
        if (node != null)
          setNextState(new HoverNode(m_editor, node));
      }
    }
  }

  public void onMouseDown(MouseEvent e) {
    super.onMouseDown(e);
    if (e.button == BTN_ADD) {
      setNextState(new AddNode(m_editor, null));
    }
  }

  public void onMouseMove(MouseEvent e) {
    super.onMouseMove(e);

    Node node = m_sensor.senseNode(getMarkerPosInMeters());
    if (node != null)
      setNextState(new HoverNode(m_editor, node));
    else {
      Graph g = Traffix.simManager().getGraph();
      GraphLocation loc = g.closestLocation(getMarkerPosInMeters(), 1);
      if (loc != null) {
        setCursor(Cursors.ARROW_ADD);
      } else {
        setCursor(Cursors.ARROW);
      }
    }
  }
}

