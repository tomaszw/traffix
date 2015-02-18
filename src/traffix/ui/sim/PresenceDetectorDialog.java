/*
 * Created on 2004-09-02
 */

package traffix.ui.sim;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import traffix.Traffix;
import traffix.core.sim.entities.CondClearDetector;
import traffix.core.sim.entities.IDetector;
import traffix.core.sim.entities.PresenceDetector;
import traffix.ui.*;

public class PresenceDetectorDialog extends VerifiedDialog {
  private Text                  m_actTime;
  private Text                  m_name;
  private GroupCombo            m_condClearGroup;
  private PresenceDetector      m_detector;
  private ScheduleCheckboxTable m_scheduleTable;
  private Text                  m_condClearTime;
  private Text                  m_condClearDets;
  private Set<IDetector>        m_clearDets;

  public PresenceDetectorDialog(Shell parentShell, PresenceDetector d) {
    super(parentShell);
    m_detector = d;
  }

  public void fromDetector() {
    NumberFormat fmt = new DecimalFormat("0.00");

    m_name.setText(m_detector.getName());
    m_actTime.setText(Float.toString(m_detector.getActivationTime()));
    m_scheduleTable.setSchedules(m_detector.getSchedules());
    m_condClearGroup.select(m_detector.getClearedBy());
    float t = m_detector.getCondClearTime();
    if (t != Float.POSITIVE_INFINITY) {
      m_condClearTime.setText(Integer.toString((int)t));
    } else {
      m_condClearTime.setText("");
    }
    m_clearDets = new HashSet<IDetector>(m_detector.condClearDetectors());
    fillCondClearDetsTxt();
  }

  private void fillCondClearDetsTxt() {
    String detTxt = "";
    for (IDetector d : m_clearDets) {
      detTxt += "," + d.getName();
    }
    if (!detTxt.equals(""))
      detTxt = detTxt.substring(1);
    m_condClearDets.setText(detTxt);
  }

  public String getValidationError() {
    try {
      float t = Float.parseFloat(m_actTime.getText());
      if (t < 0)
        throw new NumberFormatException();
    } catch (NumberFormatException e) {
      return "B³êdny czas aktywacji";
    }

    try {
      if (!m_condClearTime.getText().equals("")) {
        float t = Float.parseFloat(m_condClearTime.getText());
        if (t < 0)
          throw new NumberFormatException();
      }

    } catch (Exception e) {
      return "B³êdny czas kasuj¹cy warunek";
    }

    if (m_name.getText().equals(""))
      return "Pusta nazwa";
    java.util.List<IDetector> dets = Traffix.simManager().getDetectors();
    for (IDetector d : dets)
      if (d != m_detector && d.getName().equals(m_name.getText()))
        return "Nazwa ju¿ zajêta";

    return null;
  }
  public void toDetector() {
    m_detector.setName(m_name.getText());
    m_detector.setSchedules(m_scheduleTable.getSchedules());
    m_detector.setActivationTime(Float.parseFloat(m_actTime.getText()));
    m_detector.setClearedBy(m_condClearGroup.getSelectedGroup());
    if (!m_condClearTime.getText().equals(""))
      m_detector.setCondClearTime(Float.parseFloat(m_condClearTime.getText()));
    else {
      m_detector.setCondClearTime(Float.POSITIVE_INFINITY);
    }
    m_detector.condClearDetectors().clear();
    m_detector.condClearDetectors().addAll(m_clearDets);
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Detektor obecnoœci");
  }

  protected Control createDialogArea(Composite parent) {
    Composite contents = (Composite) super.createDialogArea(parent);
    GridLayout layout = new GridLayout(2, false);
    contents.setLayout(layout);

    GridData data;

    Label label = new Label(contents, SWT.NONE);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    label.setLayoutData(data);
    setValidationLabel(label);

    label = new Label(contents, SWT.NONE);
    label.setText("Nazwa");
    m_name = new Text(contents, SWT.NONE);
    m_name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    addVerifiedControl(m_name);

    label = new Label(contents, SWT.NONE);
    label.setText("Czas aktywacji (s)");

    m_actTime = new Text(contents, SWT.BORDER);
    m_actTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    label = new Label(contents, SWT.NONE);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    label.setLayoutData(data);
    label.setText("Przywo³ywane programy");

    m_scheduleTable = new ScheduleCheckboxTable(contents, SWT.NONE);
    data = new GridData(GridData.FILL_BOTH);
    data.horizontalSpan = 2;
    m_scheduleTable.setLayoutData(data);

    Group g = new Group(contents, SWT.NONE);
    g.setText("Warunek kasuj¹cy");
    g.setLayout(new GridLayout(2, true));
    g.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, GridData.BEGINNING, false,
        false, 2, 1));

    label = new Label(g, SWT.NONE);
    label.setText("Grupa");

    m_condClearGroup = new GroupCombo(g, SWT.READ_ONLY, true);
    m_condClearGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    label = new Label(g, SWT.NONE);
    label.setText("Czas aktywnoœci (s)");

    m_condClearTime = new Text(g, SWT.BORDER);
    m_condClearTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    label = new Label(g, SWT.NONE);
    label.setText("Detektor");

    Composite panel = new Composite(g, SWT.NONE);
    panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    GridLayout l = new GridLayout(2, false);
    l.horizontalSpacing = 0;
    l.verticalSpacing = 0;
    l.marginLeft = 0;
    l.marginTop = 0;
    l.marginWidth = 0;
    l.marginHeight = 0;
    panel.setLayout(l);
    m_condClearDets = new Text(panel, SWT.READ_ONLY | SWT.BORDER);
    m_condClearDets.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    Button cdBtn = new Button(panel, SWT.PUSH);
    cdBtn.setText("..");
    cdBtn.setLayoutData(new GridData());
    cdBtn.addSelectionListener(new SelectionListener() {

      public void widgetDefaultSelected(SelectionEvent e) {
      }

      public void widgetSelected(SelectionEvent e) {
        onSelectClearingDetectors();
      }

    });
    fromDetector();

    return contents;
  }

  private void onSelectClearingDetectors() {
    java.util.List<IDetector> alld = new ArrayList<IDetector>(Traffix.simManager().getDetectors());
    Iterator<IDetector> iter = alld.iterator();
    while (iter.hasNext()) {
      if (!(iter.next() instanceof CondClearDetector))
        iter.remove();
    }
    ObjSelectionDialog<IDetector> dlg = new ObjSelectionDialog<IDetector>(getShell(),
        alld);
    dlg.setSelection(m_clearDets);
    if (dlg.open() == ObjSelectionDialog.OK) {
      m_clearDets = dlg.getSelection();
      fillCondClearDetsTxt();
    }

  }

  protected void okPressed() {
    toDetector();
    Traffix.model().setModified(true);
    super.okPressed();
  }
}