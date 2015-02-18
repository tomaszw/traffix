/*
 * Created on 2004-08-31
 */

package traffix.ui.sim;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import traffix.Traffix;
import traffix.core.sim.entities.IMobile;
import traffix.core.sim.entities.IMobile.MoveParams;
import traffix.ui.Colors;
import traffix.ui.VerifiedDialog;

public class MoveParamsDialog extends VerifiedDialog {
  private MoveParams[] m_params;
  private Text[] m_accelTxt, m_speedTxt, m_vdeltaTxt;

  private static String[] m_names = new String[4];

  static {
    m_names[IMobile.NormalVehicle] = "Normalne";
    m_names[IMobile.HeavyVehicle] = "Ciê¿kie";
    m_names[IMobile.Bus] = "Autobusy";
    m_names[IMobile.Trolley] = "Tramwaje";
  }

  public MoveParamsDialog(Shell parentShell, MoveParams[] params) {
    super(parentShell);
    m_params = params;
  }

  public void fromParams() {
    for (int i = 0; i < 4; ++i) {
      m_speedTxt[i].setText(Float.toString(m_params[i].speed*3.6f));
      m_accelTxt[i].setText(Float.toString(m_params[i].acceleration));
      m_vdeltaTxt[i].setText(Float.toString(m_params[i].vdelta*3.6f));
    }
  }

  public void toParams() {
    for (int i = 0; i < 4; ++i) {
      m_params[i].speed = Float.parseFloat(m_speedTxt[i].getText())/3.6f;
      m_params[i].acceleration = Float.parseFloat(m_accelTxt[i].getText());
      m_params[i].vdelta = Float.parseFloat(m_vdeltaTxt[i].getText())/3.6f;
    }
  }

  public String getValidationError() {
    for (int i = 0; i < 4; ++i) {
      try {
        float speed = Float.parseFloat(m_speedTxt[i].getText());
        if (speed < 0)
          throw new NumberFormatException();
      } catch (NumberFormatException e) {
        return "B³êdna Vmax dla panelu " + m_names[i];
      }
      try {
        float acc = Float.parseFloat(m_accelTxt[i].getText());
        if (acc < 0)
          throw new NumberFormatException();
      } catch (NumberFormatException e) {
        return "B³êdne przyspieszenie dla panelu " + m_names[i];
      }
      try {
        float vd = Float.parseFloat(m_vdeltaTxt[i].getText());
        if (vd < 0)
          throw new NumberFormatException();
      } catch (NumberFormatException e) {
        return "B³êdne V +/- dla panelu " + m_names[i];
      }
    }
    return null;
  }

  protected Control createDialogArea(Composite parent) {
    Composite contents = (Composite) super.createDialogArea(parent);

    GridLayout layout = new GridLayout(2, true);
    contents.setLayout(layout);
    GridData data;

    Label vallab = new Label(contents, SWT.NONE);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    vallab.setLayoutData(data);
    setValidationLabel(vallab);

    m_accelTxt = new Text[4];
    m_speedTxt = new Text[4];
    m_vdeltaTxt = new Text[4];
    for (int i = 0; i < 4; ++i) {
      Composite pane = new Composite(contents, SWT.NONE);
      pane.setLayout(new GridLayout(2, true));

      Label label = new Label(pane, SWT.NONE);
      data = new GridData(GridData.FILL_HORIZONTAL);
      data.horizontalSpan = 2;
      label.setLayoutData(data);
      label.setBackground(Colors.system(SWT.COLOR_WHITE));
      label.setText(m_names[i]);

      final int fi = i;
      label = new Label(pane, SWT.NONE);
      label.setText("Vmax (km/h)");
      Text text = new Text(pane, SWT.NONE);
      m_speedTxt[i] = text;
      addVerifiedControl(m_speedTxt[i]);
      text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      m_speedTxt[i].addFocusListener(new FocusListener() {
        public void focusGained(org.eclipse.swt.events.FocusEvent e) {
          m_speedTxt[fi].selectAll();
        }

        public void focusLost(org.eclipse.swt.events.FocusEvent e) {
        }
      });
      label = new Label(pane, SWT.NONE);
      label.setText("Przysp. (m/s)");
      text = new Text(pane, SWT.NONE);
      m_accelTxt[i] = text;
      addVerifiedControl(m_accelTxt[i]);
      text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      m_accelTxt[i].addFocusListener(new FocusListener() {
        public void focusGained(org.eclipse.swt.events.FocusEvent e) {
          m_accelTxt[fi].selectAll();
        }

        public void focusLost(org.eclipse.swt.events.FocusEvent e) {
        }
      });

      label = new Label(pane, SWT.NONE);
      label.setText("V +/- (km/h)");
      text = new Text(pane, SWT.NONE);
      m_vdeltaTxt[i] = text;
      addVerifiedControl(m_vdeltaTxt[i]);
      text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      m_vdeltaTxt[i].addFocusListener(new FocusListener() {
        public void focusGained(org.eclipse.swt.events.FocusEvent e) {
          m_vdeltaTxt[fi].selectAll();
        }

        public void focusLost(org.eclipse.swt.events.FocusEvent e) {
        }
      });
    }

    fromParams();
    return contents;
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Parametry ruchu");
  }

  protected void okPressed() {
    toParams();
    Traffix.model().setModified(true);
    super.okPressed();
  }
}