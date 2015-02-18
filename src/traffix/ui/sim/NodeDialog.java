/*
 * Created on 2004-08-17
 */

package traffix.ui.sim;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import traffix.Traffix;
import traffix.core.VehicleGroupSet;
import traffix.core.model.Model;
import traffix.core.sim.Route;
import traffix.core.sim.RouteInfo;
import traffix.core.sim.entities.IMobile;
import traffix.core.sim.graph.IGraphPath;
import traffix.core.sim.graph.Node;
import traffix.ui.*;

public class NodeDialog extends VerifiedDialog {
  Text[] m_hphText, m_nphText, m_busphText, m_trolleyphText,
  m_prefSecText, m_prioText;
  private java.util.List<Route> m_routes;
  private VehicleGroupSet[] m_controlGroups;
  private VehicleGroupSet[] m_linkedGroups;
  private Label[] m_groupsLabels;
  private Node m_node;
  private IMapEditor m_editor;
  private int m_selRoute = -1;
  private Label[] m_sumLabels;

  public NodeDialog(Shell parentShell, Node node, IMapEditor editor) {
    super(parentShell);
    setBlockOnOpen(false);
    setShellStyle(getShellStyle() | SWT.RESIZE|SWT.MAX|SWT.MIN);
    setShellStyle(getShellStyle() & ~(SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL));
    m_node = node;
    if (m_node.isBeginningNode())
      m_routes = m_node.getRoutesFromNode();
    else
      m_routes = new ArrayList<Route>();

    m_controlGroups = new VehicleGroupSet[m_routes.size()];
    m_linkedGroups = new VehicleGroupSet[m_routes.size()];
    for (int i = 0; i < m_controlGroups.length; ++i) {
      m_controlGroups[i] = new VehicleGroupSet();
      m_linkedGroups[i] = new VehicleGroupSet();
    }

    m_editor = editor;
  }

  private void deleteRoute(int id) {
    Traffix.simManager().getGraph().deleteRoute(m_node, id);
    m_editor.getCanvas().redraw();
    Traffix.model().setModified(true);
    cancelPressed();
    //okPressed();
  }

  public void fromNode() {
    for (int i = 0; i < m_routes.size(); ++i) {
      Route route = m_routes.get(i);
      RouteInfo info = route.getInfo();
      m_nphText[i].setText(Integer.toString(info.getMeasure().nPh));
      m_hphText[i].setText(Integer.toString(info.getMeasure().hPh));
      m_busphText[i].setText(Integer.toString(info.getMeasure().busPh));
      m_trolleyphText[i].setText(Integer.toString(info.getMeasure().trolleyPh));
      m_prefSecText[i].setText(Integer.toString(info.getPreferredSec()));
      m_controlGroups[i].assign(info.getControllingGroups());
      m_linkedGroups[i].assign(info.getLinkedGroups());
      m_prioText[i].setText(Integer.toString(info.priority));

    }
    updateGroupsLabels();
  }

  private void updateGroupsLabels() {
    Model m = Traffix.model();
    for (int i = 0; i < m_routes.size(); ++i) {
      VehicleGroupSet set = m_controlGroups[i];
      Label lab = m_groupsLabels[i];
      String text = "";
      if (set.size() == 0)
        text = "brak";
      else if (set.size() == m.getNumGroups())
        text = "wszystkie";
      else if (set.size() < m.getNumGroups() / 2) {
        // show +
        for (int j = 0; j < set.size(); ++j)
          text = text + "+" + set.get(j).getElectricName() + " ";
      } else {
        // show -
        for (int j = 0; j < m.getNumGroups(); ++j) {
          if (!set.contains(m.getGroupByIndex(j))) {
            text = text + "-" + m.getGroupByIndex(j).getElectricName() + " ";
          }
        }
      }
      lab.setText(text);
    }
  }

  public String getValidationError() {
    for (int i = 0; i < m_routes.size(); ++i) {
      try {
        int p = Integer.parseInt(m_prioText[i].getText());
        if (p < 0)
          throw new NumberFormatException();
      } catch (NumberFormatException e) {
        return "Z³y priorytet: tor " + (i + 1);
      }

      try {
        int nph = Integer.parseInt(m_nphText[i].getText());
        if (nph < 0)
          throw new NumberFormatException();
      } catch (NumberFormatException e) {
        return "Z³a iloœæ pojazdów normalnych: tor " + (i + 1);
      }
      try {
        int hph = Integer.parseInt(m_hphText[i].getText());
        if (hph < 0)
          throw new NumberFormatException();
      } catch (NumberFormatException e) {
        return "Z³a iloœæ pojazdów ciê¿kich: tor " + (i + 1);
      }

      try {
        int hph = Integer.parseInt(m_busphText[i].getText());
        if (hph < 0)
          throw new NumberFormatException();
      } catch (NumberFormatException e) {
        return "Z³a iloœæ autobusów: tor " + (i + 1);
      }

      try {
        int hph = Integer.parseInt(m_trolleyphText[i].getText());
        if (hph < 0)
          throw new NumberFormatException();
      } catch (NumberFormatException e) {
        return "Z³a iloœæ tramwajów: tor " + (i + 1);
      }

      try {
        int sec = Integer.parseInt(m_prefSecText[i].getText());
        if (sec < 0)
          throw new NumberFormatException();
      } catch (NumberFormatException e) {
        return "Z³a preferowana sekunda: tor " + (i + 1);
      }
    }

    updateSumLabels();
    return null;
  }

  public void toNode() {
    selectRoute(-1);
    for (int i = 0; i < m_routes.size(); ++i) {
      Route route = m_routes.get(i);
      RouteInfo info = route.getInfo();
      info.priority = Integer.parseInt(m_prioText[i].getText());

      info.getMeasure().nPh = Integer.parseInt(m_nphText[i].getText());
      info.getMeasure().hPh = Integer.parseInt(m_hphText[i].getText());
      info.getMeasure().busPh = Integer.parseInt(m_busphText[i].getText());
      info.getMeasure().trolleyPh = Integer.parseInt(m_trolleyphText[i].getText());
      info.setPreferredSec(Integer.parseInt(m_prefSecText[i].getText()));
      info.getControllingGroups().assign(m_controlGroups[i]);
      info.getLinkedGroups().assign(m_linkedGroups[i]);
      //route.getBeginningNode().updateRouteInfos();
    }
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(m_node.isBeginningNode() ? "Wêze³ pocz¹tkowy" : "Wêze³");
    final PaintListener listener = new PaintListener() {
      public void paintControl(PaintEvent e) {
        paintEditor();
      }
    };
    m_editor.getCanvas().addPaintListener(listener);
    newShell.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        m_editor.getCanvas().removePaintListener(listener);
      }
    });
  }

  private void selectRoute(int n) {
    if (m_selRoute == n)
      return;
    m_selRoute = n;
    m_editor.getCanvas().redraw();
  }

  private void paintEditor() {
    if (m_selRoute == -1 || m_selRoute > m_routes.size())
      return;
    Route r = m_routes.get(m_selRoute);
    IGraphPath p = r.path();
    Gc gc = m_editor.getGc();
    gc.setLineWidth(4);
    gc.setForeground(Colors.get(new RGB(255, 0, 0)));
    for (int i = 0; i < p.getNumNodes() - 1; ++i) {
      Point pos = m_editor.getCoordTransformer().terrainToScreen(p.getNode(i).getPos());
      Point pos2 = m_editor.getCoordTransformer().terrainToScreen(p.getNode(i + 1).getPos());
      gc.drawLine(pos.x, pos.y, pos2.x, pos2.y);
    }
    gc.setLineWidth(1);
  }

  private void updateSumLabels() {
    for (int lab = 0; lab < 4; ++lab) {
      int sum = 0;
      for (int i = 0; i < m_routes.size(); ++i) {
        switch (lab) {
        case 0:
          sum += Integer.parseInt(m_nphText[i].getText());
          break;
        case 1:
          sum += Integer.parseInt(m_hphText[i].getText());
          break;
        case 2:
          sum += Integer.parseInt(m_busphText[i].getText());
          break;
        case 3:
          sum += Integer.parseInt(m_trolleyphText[i].getText());
          break;
        }
      }
      if (m_sumLabels != null && m_sumLabels[lab] != null)
        m_sumLabels[lab].setText(Integer.toString(sum));
    }

  }

  protected Control createDialogArea(Composite parent) {
    final Composite contents = (Composite) super.createDialogArea(parent);

    GridLayout lay = new GridLayout(1, false);
    contents.setLayout(lay);
    GridData data;

    Label vlabel = new Label(contents, SWT.NONE);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    vlabel.setLayoutData(data);
    setValidationLabel(vlabel);

    if (m_node.isBeginningNode()) {
      ScrolledComposite scrolled = new ScrolledComposite(contents, SWT.H_SCROLL|SWT.V_SCROLL);
      final Composite pane = new Composite(scrolled, SWT.NONE);
      //scrolled.setAlwaysShowScrollBars(true);
      //scrolled.setMinSize(320, 200);
      scrolled.setExpandHorizontal(true);
      scrolled.setExpandVertical(true);
      scrolled.setLayout(new FillLayout());
      //pane.setSize(400,400);
      scrolled.setContent(pane);
      GridLayout paneLay = new GridLayout(13, false);
      paneLay.horizontalSpacing = 2;
      paneLay.verticalSpacing = 4;
      pane.setLayout(paneLay);
      Label trLabel = new Label(pane, SWT.NONE);
      trLabel.setBackground(Colors.system(SWT.COLOR_WHITE));
      trLabel.setText("Przep³yw pojazdów");
      data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER);
      data.horizontalSpan = 13;
      trLabel.setLayoutData(data);

      Label label = new Label(pane, SWT.NONE);
      label.setText("Tor");
      label.setBackground(Colors.system(SWT.COLOR_WHITE));
      label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
        | GridData.VERTICAL_ALIGN_CENTER));

      label = new Label(pane, SWT.NONE);
      label.setText("Normalne");
      label.setBackground(Colors.system(SWT.COLOR_WHITE));
      label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
        | GridData.VERTICAL_ALIGN_CENTER));
      label = new Label(pane, SWT.NONE);
      label.setText("Ciê¿kie");
      label.setBackground(Colors.system(SWT.COLOR_WHITE));
      label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
        | GridData.VERTICAL_ALIGN_CENTER));

      label = new Label(pane, SWT.NONE);
      label.setText("Autobusy");
      label.setBackground(Colors.system(SWT.COLOR_WHITE));
      label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
        | GridData.VERTICAL_ALIGN_CENTER));

      label = new Label(pane, SWT.NONE);
      label.setText("Tramwaje");
      label.setBackground(Colors.system(SWT.COLOR_WHITE));
      label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
        | GridData.VERTICAL_ALIGN_CENTER));

      label = new Label(pane, SWT.NONE);
      label.setText("Priorytet");
      label.setBackground(Colors.system(SWT.COLOR_WHITE));
      label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
        | GridData.VERTICAL_ALIGN_CENTER));

      label = new Label(pane, SWT.NONE);
      label.setText("Sygnalizator");
      label.setBackground(Colors.system(SWT.COLOR_WHITE));
      data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER);
      label.setLayoutData(data);

      label = new Label(pane, SWT.NONE);
      label.setText("Preferowana sekunda");
      label.setBackground(Colors.system(SWT.COLOR_WHITE));
      data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER);
      label.setLayoutData(data);

      label = new Label(pane, SWT.NONE);
      label = new Label(pane, SWT.NONE);
      label = new Label(pane, SWT.NONE);
      label = new Label(pane, SWT.NONE);
      label = new Label(pane, SWT.NONE);

      int numRoutes = m_routes.size();
      m_nphText = new Text[numRoutes];
      m_hphText = new Text[numRoutes];
      m_busphText = new Text[numRoutes];
      m_prioText = new Text[numRoutes];
      m_trolleyphText = new Text[numRoutes];
      m_prefSecText = new Text[numRoutes];
      m_groupsLabels = new Label[numRoutes];

      for (int i = 0; i < numRoutes; ++i) {
        final int numRoute = i;
        label = new Label(pane, SWT.NONE);
        label.setText(Integer.toString(i + 1));
        label.setAlignment(SWT.RIGHT);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
          | GridData.VERTICAL_ALIGN_CENTER));

        // normal
        Text text = new Text(pane, SWT.NONE);
        m_nphText[i] = text;
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
          | GridData.VERTICAL_ALIGN_CENTER));
        text.addFocusListener(new FocusListener() {
          public void focusGained(FocusEvent e) {
            selectRoute(numRoute);
            m_nphText[numRoute].selectAll();
          }

          public void focusLost(FocusEvent e) {
          }
        });
        addVerifiedControl(text);

        // heavy
        text = new Text(pane, SWT.NONE);
        m_hphText[i] = text;
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
          | GridData.VERTICAL_ALIGN_CENTER));
        text.addFocusListener(new FocusListener() {
          public void focusGained(FocusEvent e) {
            selectRoute(numRoute);
            m_hphText[numRoute].selectAll();
          }

          public void focusLost(FocusEvent e) {
          }
        });
        addVerifiedControl(text);

        // bus
        text = new Text(pane, SWT.NONE);
        m_busphText[i] = text;
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
          | GridData.VERTICAL_ALIGN_CENTER));
        text.addFocusListener(new FocusListener() {
          public void focusGained(FocusEvent e) {
            selectRoute(numRoute);
            m_busphText[numRoute].selectAll();
          }

          public void focusLost(FocusEvent e) {
          }
        });
        addVerifiedControl(text);

        // trolley
        text = new Text(pane, SWT.NONE);
        m_trolleyphText[i] = text;
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
          | GridData.VERTICAL_ALIGN_CENTER));
        text.addFocusListener(new FocusListener() {
          public void focusGained(FocusEvent e) {
            selectRoute(numRoute);
            m_trolleyphText[numRoute].selectAll();
          }

          public void focusLost(FocusEvent e) {
          }
        });
        addVerifiedControl(text);

        // priority
        text = new Text(pane, SWT.NONE);
        m_prioText[i] = text;
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
          | GridData.VERTICAL_ALIGN_CENTER));
        text.addFocusListener(new FocusListener() {
          public void focusGained(FocusEvent e) {
            selectRoute(numRoute);
            m_prioText[numRoute].selectAll();
          }

          public void focusLost(FocusEvent e) {
          }
        });
        addVerifiedControl(text);

        label = new Label(pane, SWT.NONE);
        m_groupsLabels[i] = label;

        // preferred sec
        text = new Text(pane, SWT.NONE);
        m_prefSecText[i] = text;
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
          | GridData.VERTICAL_ALIGN_CENTER));
        text.addFocusListener(new FocusListener() {
          public void focusGained(FocusEvent e) {
            selectRoute(numRoute);
            m_prefSecText[numRoute].selectAll();
          }

          public void focusLost(FocusEvent e) {
          }
        });

        addVerifiedControl(text);

        Button btn = new Button(pane, SWT.PUSH);
        btn.setText("Grupy..");
        btn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
          | GridData.VERTICAL_ALIGN_CENTER));
        btn.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent e) {
            selectRoute(numRoute);
            GroupSelectionDialog dlg = new GroupSelectionDialog(getShell());
            dlg.setGroups(m_controlGroups[numRoute]);
            if (dlg.open() == dlg.OK) {
              m_controlGroups[numRoute].assign(dlg.getGroups());
            }
            updateGroupsLabels();
            getShell().pack();
          }
        });

        // move parameters
        btn = new Button(pane, SWT.PUSH);
        btn.setText("Parametry...");
        btn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
          | GridData.VERTICAL_ALIGN_CENTER));
        btn.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent e) {
            selectRoute(numRoute);
            IMobile.MoveParams[] mp = (m_routes.get(numRoute)).getInfo()
              .getMoveParams();
            MoveParamsDialog dlg = new MoveParamsDialog(getShell(), mp);
            dlg.open();
          }
        });

        // light links
        btn = new Button(pane, SWT.PUSH);
        btn.setText("Sprzê¿enie...");
        btn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
          | GridData.VERTICAL_ALIGN_CENTER));
        btn.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent e) {
            selectRoute(numRoute);
            GroupSelectionDialog dlg = new GroupSelectionDialog(getShell());
            dlg.setGroups(m_linkedGroups[numRoute]);
            if (dlg.open() == dlg.OK) {
              m_linkedGroups[numRoute].assign(dlg.getGroups());
            }
          }
        });

        // remove track
        btn = new Button(pane, SWT.PUSH);
        btn.setText("Skasuj tor");
        btn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
          | GridData.VERTICAL_ALIGN_CENTER));
        btn.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent e) {
            MessageBox box = new MessageBox(getShell(), SWT.NO|SWT.YES|SWT.ICON_QUESTION);
            box.setMessage("Czy na pewno skasowaæ tor " + (numRoute+1) + "?");
            if (box.open() == SWT.YES) {
              deleteRoute(numRoute);
            }
          }
        });

        // set track comment
        btn = new Button(pane, SWT.PUSH);
        btn.setText("Komentarz...");
        btn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
          | GridData.VERTICAL_ALIGN_CENTER));
        btn.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent e) {
            String msg = "Podaj komentarz dla wybranego toru";
            String init = m_routes.get(numRoute).getInfo().comment;
            InputDialog dlg = new InputDialog(getShell(), Traffix.NAME, msg, init,
              null);
            if (dlg.open() == InputDialog.OK) {
              m_routes.get(numRoute).getInfo().comment = dlg.getValue();
              Traffix.model().setModified(true);
            }
          }
        });
      }

      new Label(pane, SWT.NONE);
      m_sumLabels = new Label[4];
      for (int i = 0; i < 4; ++i) {
        m_sumLabels[i] = new Label(pane, SWT.NONE);
        m_sumLabels[i].setBackground(Colors.get(new RGB(225, 225, 225)));
        m_sumLabels[i].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      }

      data = new GridData(GridData.FILL_BOTH);
      data.horizontalSpan = 1;
      scrolled.setLayoutData(data);
      //pane.pack();
      scrolled.setVisible(true);
      contents.pack();

      scrolled.setMinSize(pane.getSize());//400,400);

    }

    fromNode();

    contents.pack();

    return contents;
  }

  protected void okPressed() {
    toNode();
    Traffix.model().setModified(true);
    Traffix.simManager().fireUpdated();
    super.okPressed();
  }
}