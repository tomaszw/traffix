/*
 * Created on 2004-09-01
 */

package traffix.ui.sim;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import traffix.Traffix;
import traffix.core.schedule.Schedule;
import traffix.core.sim.AccomodationMatrix;
import traffix.ui.Colors;
import traffix.ui.ListSelectionDialog;
import traffix.ui.VerifiedDialog;

public class AccPriorityDialog extends VerifiedDialog {
  private Schedule[] m_schedules;
  private AccomodationMatrix m_matrix;
  private Text[][] m_cells;
  private Label[] m_hLabs, m_vLabs;
  private int m_x = -1, m_y = -1;

  public AccPriorityDialog(Shell parentShell, AccomodationMatrix matrix) {
    super(parentShell);
    m_matrix = matrix;
    m_schedules = m_matrix.getSchedules();
    //m_matrix.update();
  }

  public void fromMatrix() {
    int num = m_schedules.length;
    String[] names = m_matrix.getNames();
    for (int i = 0; i < num; ++i) {
      m_hLabs[i].setText(names[i]);
      m_vLabs[i].setText(names[i]);
    }
    for (int i = 0; i < num; ++i) {
      for (int j = 0; j < num; ++j) {
        String a = names[i];
        String b = names[j];
        m_cells[i][j].setText(Integer.toString(m_matrix.getPriority(a, b)));
      }
    }
    //getShell().pack();
  }

  public void toMatrix() {
    int num = m_schedules.length;
    String[] names = m_matrix.getNames();
    for (int i = 0; i < num; ++i) {
      for (int j = 0; j < num; ++j) {
        String a = names[i];
        String b = names[j];
        int p = Integer.parseInt(m_cells[i][j].getText());
        m_matrix.setPriority(a, b, p);
      }
    }
  }

  private void changeBaseSchedule() {
    ListSelectionDialog dlg = new ListSelectionDialog(getShell(), "Program podstawowy", "Wybierz program podstawowy");
    dlg.setItems(m_matrix.getNames());
    if (dlg.open() == dlg.OK) {
      int idx = dlg.getSelectedIndex();
      String sch = m_matrix.getNames()[idx];
      m_matrix.setBaseSchedule(sch);
      fromMatrix();
    }
  }

  protected void createButtonsForButtonBar(Composite parent) {
    Button btn = createButton(parent, 50, "Program podstawowy...", false);
    btn.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        changeBaseSchedule();
      }
    });
    super.createButtonsForButtonBar(parent);
  }

  protected Control createDialogArea(Composite parent) {
    Composite contents = (Composite) super.createDialogArea(parent);
    GridLayout layout = new GridLayout(2, true);
    contents.setLayout(layout);

    GridData data;

    Label label = new Label(contents, SWT.NONE);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    label.setLayoutData(data);
    setValidationLabel(label);

    Composite matrix = new Composite(contents, SWT.NONE);
    data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
    data.horizontalSpan = 2;
    matrix.setLayoutData(data);

    matrix.setLayout(new GridLayout(m_schedules.length + 1, true));
    m_cells = new Text[m_schedules.length][m_schedules.length];
    m_hLabs = new Label[m_schedules.length];
    m_vLabs = new Label[m_schedules.length];
    for (int i = 0; i < m_schedules.length + 1; ++i) {
      for (int j = 0; j < m_schedules.length + 1; ++j) {
        if (i == 0 && j == 0) {
          new Label(matrix, SWT.NONE);
        } else if (i == 0) {
          String name = m_schedules[j - 1].getName();
          label = new Label(matrix, SWT.NONE);
          label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER));
          label.setText(name);
          m_hLabs[j - 1] = label;
        } else if (j == 0) {
          String name = m_schedules[i - 1].getName();
          label = new Label(matrix, SWT.NONE);
          label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
          label.setText(name);
          m_vLabs[i - 1] = label;
        } else {
          final Text text = new Text(matrix, SWT.NONE);
          text.setLayoutData(new GridData(GridData.FILL_BOTH));//HORIZONTAL|GridData.VERTICAL_ALIGN_CENTER));
          addVerifiedControl(text);
          m_cells[i - 1][j - 1] = text;
          final int celli = i - 1;
          final int cellj = j - 1;
          text.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
              text.selectAll();
              hilight(celli, cellj);
            }

            public void focusLost(FocusEvent e) {
            }

          });
        }
      }
    }

    fromMatrix();
    return contents;
  }

  private void hilight(int i, int j) {
    if (m_y != -1 && m_x != -1) {
      m_hLabs[m_x].setBackground(Colors.system(SWT.COLOR_WIDGET_BACKGROUND));
      m_vLabs[m_y].setBackground(Colors.system(SWT.COLOR_WIDGET_BACKGROUND));
    }
    m_y = i;
    m_x = j;
    m_hLabs[j].setBackground(Colors.get(new RGB(255, 150, 0)));
    m_vLabs[i].setBackground(Colors.get(new RGB(255, 150, 0)));
  }

  protected void okPressed() {
    toMatrix();
    Traffix.model().setModified(true);
    super.okPressed();
  }

  public String getValidationError() {
    int n = m_schedules.length;
    for (int i = 0; i < n; ++i) {
      for (int j = 0; j < n; ++j) {
        try {
          Integer.parseInt(m_cells[i][j].getText());
        } catch (NumberFormatException e) {
          return "B³êdna komórka " + i + "," + j;
        }
      }
    }
    return null;
  }

}
