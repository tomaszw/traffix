/*
 * Created on 2004-07-07
 */

package traffix.ui.schedule;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import traffix.Traffix;
import traffix.core.Weekdays;
import traffix.core.model.Model;
import traffix.core.schedule.Schedule;
import traffix.core.schedule.WeeklyScheduleEntry;
import za.co.quirk.layout.LatticeData;
import za.co.quirk.layout.LatticeLayout;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class WeeklyScheduleEntryDialog extends Dialog {
  WeeklyScheduleEntry m_entry;
  NumberFormat m_fmtTimeElem = new DecimalFormat("00");
  Text m_offset, m_time;
  Combo m_schedule;
  Combo m_weekday;

  public WeeklyScheduleEntryDialog(Shell parentShell, WeeklyScheduleEntry entry) {
    super(parentShell);
    m_entry = entry;
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Parametry programu");
  }

  protected void createButtonsForButtonBar(Composite parent) {
    super.createButtonsForButtonBar(parent);
    getButton(CANCEL).setText("Anuluj");
  }

  protected Control createDialogArea(Composite parent) {
    Composite contents = new Composite(parent, SWT.NONE);
    double[][] sizes = {
      {4, LatticeLayout.PREFERRED, 4, LatticeLayout.PREFERRED, 4},
      {4, LatticeLayout.PREFERRED, 4, LatticeLayout.PREFERRED, 4,
       LatticeLayout.PREFERRED, 4, LatticeLayout.PREFERRED, 4}};

    contents.setLayout(new LatticeLayout(sizes));

    Label label;
    label = new Label(contents, SWT.NONE);
    label.setText("Nazwa");
    label.setLayoutData(new LatticeData("1,1"));

    m_schedule = new Combo(contents, SWT.BORDER | SWT.READ_ONLY);
    m_schedule.setLayoutData(new LatticeData("3,1"));

    label = new Label(contents, SWT.NONE);
    label.setText("Dzieñ tygodnia");
    label.setLayoutData(new LatticeData("1,3"));

    m_weekday = new Combo(contents, SWT.READ_ONLY);
    m_weekday.setVisibleItemCount(7);
    m_weekday.setLayoutData(new LatticeData("3,3"));
    for (int i = 0; i < 7; ++i)
      m_weekday.add(Weekdays.names[i]);
    m_weekday.select(0);
    label = new Label(contents, SWT.NONE);
    label.setText("Godzina");
    label.setLayoutData(new LatticeData("1,5"));

    m_time = new Text(contents, SWT.BORDER);
    m_time.setLayoutData(new LatticeData("3,5"));

    label = new Label(contents, SWT.NONE);
    label.setText("Offset");
    label.setLayoutData(new LatticeData("1,7"));

    m_offset = new Text(contents, SWT.BORDER);
    m_offset.setLayoutData(new LatticeData("3,7"));

    fromEntry();
    m_schedule.setFocus();

    return contents;
  }

  protected void okPressed() {
    String err = validateContents();
    if (err == null) {
      toEntry();
      super.okPressed();
    } else {
      MessageBox dlg = new MessageBox(getShell());
      dlg.setText("B³êdne parametry");
      dlg.setMessage(err);
      dlg.open();
    }
  }

  private int findIndex(String name) {
    for (int i = 0; i < m_schedule.getItemCount(); ++i)
      if (name.equals(m_schedule.getItem(i)))
        return i;
    return -1;
  }

  private void focusControl(Text ctrl) {
    ctrl.setFocus();
    ctrl.setSelection(0, ctrl.getText().length());
  }

  private void fromEntry() {
    populateScheduleCombo();
    m_schedule.select(findIndex(m_entry.scheduleName));
    m_weekday.select(m_entry.day);
    String t = m_fmtTimeElem.format(m_entry.hour) + ":"
      + m_fmtTimeElem.format(m_entry.minute);
    m_time.setText(t);
    m_offset.setText(Integer.toString(m_entry.offset));

    m_offset.setSelection(0, m_offset.getText().length());
    m_time.setSelection(0, m_time.getText().length());
  }

  private void populateScheduleCombo() {
    Model m = Traffix.model();
    Schedule[] schedules = m.getScheduleBank().getSchedules();
    m_schedule.removeAll();
    for (int i = 0; i < schedules.length; ++i)
      m_schedule.add(schedules[i].getName());
    m_schedule.setVisibleItemCount(7);
  }

  private void toEntry() {
    m_entry.scheduleName = m_schedule.getText();
    m_entry.day = m_weekday.getSelectionIndex();

    String t = m_time.getText();
    int colon = t.indexOf(':');
    int h, m;
    h = Integer.parseInt(t.substring(0, colon));
    m = Integer.parseInt(t.substring(colon + 1));

    m_entry.hour = h;
    m_entry.minute = m;

    m_entry.offset = Integer.parseInt(m_offset.getText());
  }

  private String validateContents() {
    final String errTime = "B³êdny format czasu";
    final String errOffset = "B³êdny format offsetu";
    String t = m_time.getText();
    int colon = t.indexOf(':');
    if (colon == -1) {
      focusControl(m_time);
      return errTime;
    }
    try {
      int h, m;
      h = Integer.parseInt(t.substring(0, colon));
      m = Integer.parseInt(t.substring(colon + 1));
      if (h < 0 || h > 23 || m < 0 || m > 59) {
        focusControl(m_time);
        return errTime;
      }
    } catch (NumberFormatException e) {
      focusControl(m_time);
      return errTime;
    }

    try {
      int offset = Integer.parseInt(m_offset.getText());
    } catch (NumberFormatException e) {
      focusControl(m_offset);
      return errOffset;
    }

    return null;
  }
}