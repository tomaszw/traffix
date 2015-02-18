/*
 * Created on 2005-10-01
 */

package traffix.ui.sim;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import traffix.core.accident.IAccidentModel;
import traffix.core.sim.graph.Node;
import traffix.ui.VerifiedDialog;

import static java.lang.Math.*;

public class AccidentNodeDialog extends VerifiedDialog {

  private IAccidentModel m_accModel;
  private Text           m_name;
  private Node           m_node;
  private Text           m_speed;
  private Text           m_stopTime;

  public AccidentNodeDialog(Shell parentShell, Node n, IAccidentModel am) {
    super(parentShell);
    m_node = n;
    m_accModel = am;
  }
  
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Wêze³ wypadku");
  }
  
  
  public String getValidationError() {
    if (!m_name.getText().equals("")) {
      Node node = m_accModel.getNode(m_name.getText());
      if (node != m_node && node != null) {
        return "Nazwa nie jest unikalna";
      }
    }
    try {
      Integer.parseInt(m_speed.getText());
    } catch (NumberFormatException e) {
      return "B³êdna prêdkoœæ";
    }
    try {
      Integer.parseInt(m_stopTime.getText());
    } catch (NumberFormatException e) {
      return "B³êdny czas postoju";
    }
    return null;
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite contents = (Composite) super.createDialogArea(parent);
    contents.setLayout(new GridLayout(2, true));

    Label lab = new Label(contents, SWT.NONE);
    GridData d = new GridData(GridData.FILL_HORIZONTAL);
    d.horizontalSpan = 2;
    lab.setLayoutData(d);
    setValidationLabel(lab);

    d = new GridData(GridData.FILL_HORIZONTAL);
    lab = new Label(contents, SWT.NONE);
    lab.setText("Nazwa");
    m_name = new Text(contents, SWT.FLAT);
    m_name.setLayoutData(d);
    addVerifiedControl(m_name);
    lab = new Label(contents, SWT.NONE);
    lab.setText("Postój (s)");
    m_stopTime = new Text(contents, SWT.FLAT);
    m_stopTime.setLayoutData(d);
    addVerifiedControl(m_stopTime);
    lab = new Label(contents, SWT.NONE);
    lab.setText("Prêdkoœæ (km/h)");
    m_speed = new Text(contents, SWT.FLAT);
    m_speed.setLayoutData(d);
    addVerifiedControl(m_speed);
    fromNode();
    
    return contents;
  }

  @Override
  protected void okPressed() {
    toNode();
    super.okPressed();
  }

  private void fromNode() {
    if (m_node.getName() != null)
      m_name.setText(m_node.getName());
    else
      m_name.setText("");
    float kmh = m_accModel.getNodeSpeed(m_node) * 3.6f;
    m_speed.setText(Integer.toString(round(kmh)));
    m_stopTime.setText(Integer.toString(round(m_accModel.getNodeStopTime(m_node))));
  }

  private void toNode() {
    if (!m_name.getText().equals(""))
      m_node.setName(m_name.getText());
    else
      m_node.setName(null);
    float mps = Integer.parseInt(m_speed.getText()) / 3.6f;
    m_accModel.setNodeSpeed(m_node, mps);
    m_accModel.setNodeStopTime(m_node, Integer.parseInt(m_stopTime.getText()));
  }
}
