/*
 * Created on 2004-08-21
 */

package traffix.ui.sim;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import traffix.Traffix;
import traffix.core.Time;
import traffix.core.schedule.Schedule;
import traffix.core.sim.Databanks;
import traffix.core.sim.ISimManager;
import traffix.core.sim.generation.IGenerationModel;
import traffix.core.sim.generation.StandardGenerationModel;
import traffix.ui.VerifiedDialog;

public class SimParamsDialog extends VerifiedDialog {
  private Button m_accomodationProg, m_nlNum, m_nlVirtual, m_nlStoppedTime, m_betterColls, m_barriers, m_nodeLabels, m_showNumWaiting;
  private Text m_autostopTime, m_speedFactor;
  private Menu m_databankMenu;
  private Combo m_databank, m_schedule, m_generationModel;

  public SimParamsDialog(Shell parentShell) {
    super(parentShell);
  }

  public void fromSimulation() {
    ISimManager simman = Traffix.simManager();

    Schedule schedule = Traffix.simScheduleManager().getActiveSchedule();
    m_accomodationProg.setSelection(Traffix.simScheduleManager()
      .usesAccomodationProgram());
    m_nlNum.setSelection(simman.simParams().showNodeNumbers);
    m_nlVirtual.setSelection(simman.simParams().showVirtualVehicles);
    m_nlStoppedTime.setSelection(simman.simParams().showAvgStopTime);
    m_betterColls.setSelection(simman.simParams().betterCollisionDetection);
    m_barriers.setSelection(simman.simParams().barriersActive);
    m_nodeLabels.setSelection(simman.simParams().showNodeLabels);
    m_showNumWaiting.setSelection(simman.simParams().showNumWaiting);

    if (schedule != null) {
      for (int i = 0; i < m_schedule.getItemCount(); ++i) {
        if (m_schedule.getItem(i).equals(schedule.getName())) {
          m_schedule.select(i);
          break;
        }
      }
    } else {
      m_schedule.select(0);
    }

    Time as = new Time();
    as = as.addSecs((int) simman.simParams().duration);

    m_autostopTime.setText(as.getHourMinString());
    m_speedFactor.setText(Float.toString(simman.getSpeedFactor()));
    m_generationModel
      .select(simman.getGenerationModel() instanceof StandardGenerationModel ? 0 : 1);
    m_databank.select(simman.databanks().getActive());
    syncControls();
  }

  public String getValidationError() {
    if (Time.parseHourMin(m_autostopTime.getText()) == null) {
      return "B³êdny czas autostopu";
    }
    try {
      float fac = Float.parseFloat(m_speedFactor.getText());
      if (fac <= 0 || fac > 100)
        return "B³êdny wspó³czynnik przyspieszenia";
    } catch (NumberFormatException e) {
      return "B³êdny wspó³czynnik przyspieszenia";
    }

    return null;
  }

  public void toSimulation() {
    ISimManager simman = Traffix.simManager();
    simman.simParams().showNodeNumbers = m_nlNum.getSelection();
    simman.simParams().showVirtualVehicles = m_nlVirtual.getSelection();
    simman.simParams().showAvgStopTime = m_nlStoppedTime.getSelection();
    simman.simParams().betterCollisionDetection = m_betterColls.getSelection();
    simman.simParams().barriersActive = m_barriers.getSelection();
    simman.simParams().showNodeLabels = m_nodeLabels.getSelection();
    simman.simParams().showNumWaiting = m_showNumWaiting.getSelection();

    if (m_accomodationProg.getSelection() == true) {
      //Traffix.simScheduleManager().setActiveSchedule(null);
    } else {
      String name = m_schedule.getItem(m_schedule.getSelectionIndex());
      Traffix.simScheduleManager().setActiveSchedule(Traffix.model().getSchedule(name));
      Traffix.simManager().fireUpdated();
    }
    Traffix.simScheduleManager().setAccomodationProgramUsage(m_accomodationProg.getSelection());
    Time as = Time.parseHourMin(m_autostopTime.getText());
    float fac = Float.parseFloat(m_speedFactor.getText());
    int model = m_generationModel.getSelectionIndex() == 0
      ? IGenerationModel.STANDARD
      : IGenerationModel.GROUP;
    simman.simParams().duration = as.toSecs();
    simman.setSpeedFactor(fac);
    simman.setGenerationModel(model);
    simman.databanks().setActive(m_databank.getSelectionIndex());
    Traffix.model().setModified(true);
    simman.fireUpdated();
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Parametry symulacji");
  }

  protected void createButtonsForButtonBar(Composite parent) {
    Button btn = createButton(parent, 49, "Wybór faz..", false);
    btn.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        AccSelectionDialog dlg = new AccSelectionDialog(getShell());
        dlg.open();
      }
    });

    btn = createButton(parent, 50, "Cykl..", false);
    btn.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        AccPriorityDialog dlg = new AccPriorityDialog(getShell(), Traffix
          .simScheduleManager().accMatrix());
        dlg.open();
      }
    });

    super.createButtonsForButtonBar(parent);
  }

  protected Control createDialogArea(Composite parent) {
    Composite contents = (Composite) super.createDialogArea(parent);

    GridLayout layout = new GridLayout(2, false);
    contents.setLayout(layout);
    GridData data;

    Label label = new Label(contents, SWT.NONE);
    data = createGriddata();
    data.horizontalSpan = 2;
    label.setLayoutData(data);
    setValidationLabel(label);

    label = new Label(contents, SWT.NONE);
    label.setText("Autostop (hh:mm)");

    m_autostopTime = new Text(contents, SWT.BORDER);
    m_autostopTime.setLayoutData(createGriddata());
    addVerifiedControl(m_autostopTime);

    label = new Label(contents, SWT.NONE);
    label.setText("Wspó³czynnik przyspieszenia");

    m_speedFactor = new Text(contents, SWT.BORDER);
    m_speedFactor.setLayoutData(createGriddata());
    addVerifiedControl(m_speedFactor);

    m_barriers = new Button(contents, SWT.CHECK);
    m_barriers.setText("Bariery");
    data = new GridData(GridData.FILL_BOTH);
    data.horizontalSpan = 2;
    m_barriers.setLayoutData(data);

    m_betterColls = new Button(contents, SWT.CHECK);
    m_betterColls.setText("Detekcja kolizji w punktach przeciêcia");
    data = new GridData(GridData.FILL_BOTH);
    data.horizontalSpan = 2;
    m_betterColls.setLayoutData(data);

    m_nodeLabels = new Button(contents, SWT.CHECK);
    m_nodeLabels.setText("Etykiety wêz³ów");
    data = new GridData(GridData.FILL_BOTH);
    data.horizontalSpan = 2;
    m_nodeLabels.setLayoutData(data);

    m_nlNum = new Button(contents, SWT.CHECK);
    m_nlNum.setText("Etykiety wêz³ów: numeruj wêz³y");
    data = new GridData(GridData.FILL_BOTH);
    data.horizontalSpan = 2;
    m_nlNum.setLayoutData(data);

    m_nlVirtual = new Button(contents, SWT.CHECK);
    m_nlVirtual.setText("Etykiety wêz³ów: pojazdy umowne na godzinê");
    data = new GridData(GridData.FILL_BOTH);
    data.horizontalSpan = 2;
    m_nlVirtual.setLayoutData(data);

    m_nlStoppedTime = new Button(contents, SWT.CHECK);
    m_nlStoppedTime.setText("Etykiety wêz³ów: œredni czas postoju");
    data = new GridData(GridData.FILL_BOTH);
    data.horizontalSpan = 2;
    m_nlStoppedTime.setLayoutData(data);

    m_showNumWaiting = new Button(contents, SWT.CHECK);
    m_showNumWaiting.setText("Etykiety wêz³ów: iloœæ doje¿d¿aj¹cych");
    data = new GridData(GridData.FILL_BOTH);
    data.horizontalSpan = 2;
    m_showNumWaiting.setLayoutData(data);

    m_accomodationProg = new Button(contents, SWT.CHECK);
    m_accomodationProg.setText("Program akomodacyjny");
    data = new GridData(GridData.FILL_BOTH);
    data.horizontalSpan = 2;
    m_accomodationProg.setLayoutData(data);
    m_accomodationProg.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        syncControls();
      }
    });

    data = new GridData(GridData.FILL_BOTH);
    data.horizontalSpan = 2;
    m_schedule = new Combo(contents, SWT.READ_ONLY);
    m_schedule.setLayoutData(data);
    addVerifiedControl(m_schedule);

    label = new Label(contents, SWT.NONE);
    label.setText("Bank danych");

    Composite databankPane = new Composite(contents, SWT.NONE);
    layout = new GridLayout(2, false);
    layout.horizontalSpacing = 0;
    layout.verticalSpacing = 0;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    databankPane.setLayout(layout);
    databankPane.setLayoutData(createGriddata());
    m_databank = new Combo(databankPane, SWT.READ_ONLY);
    m_databank.setVisibleItemCount(Databanks.MAX_DATABANKS);
    m_databank.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    addVerifiedControl(m_databank);
    final Button dbbtn = new Button(databankPane, SWT.ARROW | SWT.RIGHT);
    buildDatabankMenu(contents);

    dbbtn.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        Point p = dbbtn.toDisplay(e.x, e.y);
        m_databankMenu.setLocation(p.x, p.y);
        m_databankMenu.setVisible(true);
      }
    });

    label = new Label(contents, SWT.NONE);
    label.setText("Model generacji");

    m_generationModel = new Combo(contents, SWT.READ_ONLY);
    m_generationModel.setLayoutData(createGriddata());
    addVerifiedControl(m_generationModel);

    Schedule[] schedules = Traffix.model().getScheduleBank().getSchedules();
    Arrays.sort(schedules, new Comparator<Schedule>() {
      public int compare(Schedule o1, Schedule o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    for (int i = 0; i < schedules.length; ++i)
      m_schedule.add(schedules[i].getName());
    m_schedule.select(0);

    m_generationModel.add("Standardowy");
    m_generationModel.add("Grupy");
    m_generationModel.select(0);
    m_generationModel.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        syncControls();
      }
    });

    Databanks banks = Traffix.simManager().databanks();
    for (int i = 0; i < Databanks.MAX_DATABANKS; ++i) {
      m_databank.add("-- " + (i + 1) + " -- " + banks.getComment(i));
    }
    m_databank.select(0);

    fromSimulation();

    syncControls();

    return contents;
  }

  protected void okPressed() {
    toSimulation();
    Traffix.model().setModified(true);
    super.okPressed();
  }

  private void buildDatabankMenu(Composite parent) {
    m_databankMenu = new Menu(parent);
    MenuItem mi = new MenuItem(m_databankMenu, SWT.NONE);
    mi.setText("Komentarz");
    mi.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        Databanks banks = Traffix.databanks();
        int sel = m_databank.getSelectionIndex();
        InputDialog dlg = new InputDialog(getShell(), "Komentarz",
          "Podaj komentarz dla banku", banks.getComment(sel), null);
        if (dlg.open() == dlg.OK) {
          banks.setComment(sel, dlg.getValue());
        }
        updateBanksCombo();
      }
    });

    final IInputValidator bankNumValidator = new IInputValidator() {
      public String isValid(String newText) {
        try {
          int b = Integer.parseInt(newText);
          if (b < 1 || b > Databanks.MAX_DATABANKS)
            return "B³êdny numer banku";
        } catch (NumberFormatException e) {
          return "B³êdny numer banku";
        }
        return null;
      }
    };

    mi = new MenuItem(m_databankMenu, SWT.NONE);
    mi.setText("Kopia");
    mi.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        Databanks banks = Traffix.databanks();
        int sel = m_databank.getSelectionIndex();
        String msg = "Skopiuj bank " + (sel + 1) + " do banku: ";
        InputDialog dlg = new InputDialog(getShell(), "Komentarz", msg, Integer
          .toString(sel + 1), bankNumValidator);
        if (dlg.open() == dlg.OK) {
          int to = Integer.parseInt(dlg.getValue()) - 1;
          banks.copy(sel, to);
        }
        updateBanksCombo();
      }
    });
  }

  private GridData createGriddata() {
    GridData d = new GridData(GridData.FILL_BOTH);
    return d;
  }

  private void syncControls() {
    boolean check = m_accomodationProg.getSelection();
    m_schedule.setEnabled(!check);
  }

  private void updateBanksCombo() {
    int sel = m_databank.getSelectionIndex();
    Databanks banks = Traffix.simManager().databanks();
    for (int i = 0; i < Databanks.MAX_DATABANKS; ++i) {
      m_databank.setItem(i, "-- " + (i + 1) + " -- " + banks.getComment(i));
    }
    m_databank.select(sel);
  }

}