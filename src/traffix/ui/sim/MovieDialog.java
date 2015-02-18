/*
 * Created on 2004-08-27
 */

package traffix.ui.sim;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.tw.geometry.Rectanglef;
import traffix.Traffix;
import traffix.core.sim.MovieMaker;
import traffix.core.sim.MovieMakerException;
import traffix.ui.Colors;
import traffix.ui.ProgressDialog;
import traffix.ui.VerifiedDialog;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class MovieDialog extends VerifiedDialog {
  Text m_outFile, m_fps, m_len;
  Label m_outDimsLab;
  Slider m_proportion, m_scale;
  Label m_scaleLab, m_proportionLab;

  public MovieDialog(Shell parentShell) {
    super(parentShell);
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Film");
  }

  public String getValidationError() {
    String outfile = m_outFile.getText();
    if (outfile.equals(""))
      return "Zdefiniuj plik wyjœciowy";

    int fps, len;

    try {
      fps = Integer.parseInt(m_fps.getText());
      if (fps < 1)
        return "Zbyt ma³a liczba klatek na sekundê";
    } catch (NumberFormatException e) {
      return "B³êdna iloœæ klatek na sekundê";
    }

    try {
      len = Integer.parseInt(m_len.getText());
      if (len < 1)
        return "Zbyt ma³a d³ugoœæ filmu";
    } catch (NumberFormatException e) {
      return "B³êdna d³ugoœæ filmu";
    }

    return null;
  }

  private GridData createGriddataFb() {
    return new GridData(GridData.FILL_BOTH);
  }

  protected Control createDialogArea(Composite parent) {
    Composite contents = (Composite) super.createDialogArea(parent);
    GridLayout layout = new GridLayout(2, false);
    GridData data;
    contents.setLayout(layout);

    Label label;

    label = new Label(contents, SWT.NONE);
    data = createGriddataFb();
    data.horizontalSpan = 2;
    label.setLayoutData(data);
    setValidationLabel(label);

    label = new Label(contents, SWT.NONE);
    label.setText("Plik wyjœciowy");

    m_outFile = new Text(contents, SWT.BORDER);
    m_outFile.setLayoutData(createGriddataFb());
    addVerifiedControl(m_outFile);

    label = new Label(contents, SWT.NONE);
    label.setText("Klatek na sekundê");

    m_fps = new Text(contents, SWT.BORDER);
    m_fps.setLayoutData(createGriddataFb());
    m_fps.setText("15");
    addVerifiedControl(m_fps);

    label = new Label(contents, SWT.NONE);
    label.setText("D³ugoœc w sekundach");

    m_len = new Text(contents, SWT.BORDER);
    m_len.setLayoutData(createGriddataFb());
    //m_len.setText("60");
    addVerifiedControl(m_len);

    label = new Label(contents, SWT.NONE);
    m_scaleLab = label;
    data = createGriddataFb();
    data.horizontalSpan = 2;
    label.setLayoutData(data);
    m_scale = new Slider(contents, SWT.NONE);
    m_scale.setMinimum(1);
    m_scale.setThumb(20);
    m_scale.setMaximum(200 + m_scale.getThumb());
    data = createGriddataFb();
    data.horizontalSpan = 2;
    m_scale.setLayoutData(data);
    m_scale.setSelection(100);

    m_scale.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        syncControls();
      }
    });
    addVerifiedControl(m_scale);

    label = new Label(contents, SWT.NONE);
    m_proportionLab = label;
    data = createGriddataFb();
    data.horizontalSpan = 2;
    label.setLayoutData(data);
    m_proportion = new Slider(contents, SWT.NONE);
    data = createGriddataFb();
    data.horizontalSpan = 2;
    m_proportion.setLayoutData(data);
    m_proportion.setMinimum(1);
    m_proportion.setThumb(20);
    m_proportion.setMaximum(200 + m_proportion.getThumb());
    m_proportion.setSelection(80);

    m_proportion.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        syncControls();
      }
    });
    addVerifiedControl(m_proportion);

    label = new Label(contents, SWT.NONE);
    label.setText("Rozmiar wyjœciowy");

    m_outDimsLab = new Label(contents, SWT.NONE);
    m_outDimsLab.setLayoutData(createGriddataFb());
    m_outDimsLab.setBackground(Colors.system(SWT.COLOR_WHITE));

    syncControls();

    return contents;
  }

  private Point calcOutDims() {
    Rectanglef rc = Traffix.simManager().getFilmRectangle();
    float orgpropo = rc.height/rc.width;

    Point dims = new Point(640, 480);
    dims.y = (int) (dims.x*orgpropo);

    float propo = m_proportion.getSelection()/100.0f;
    float scale = m_scale.getSelection()/100.0f;

    dims.y = (int) (dims.y*scale*propo);
    dims.x = (int) (dims.x*scale);

    dims.x = Math.max(8, (dims.x/8)*8);
    dims.y = Math.max(8, (dims.y/8)*8);

    return dims;
  }

  protected void okPressed() {
    getShell().setVisible(false);
    try {
      Rectanglef rc = Traffix.simManager().getFilmRectangle();
      if (rc == null)
        return;
      final Point sz = calcOutDims();
      final MovieMaker movie = new MovieMaker(rc, sz);
      final int fps = Integer.parseInt(m_fps.getText());
      int len = Integer.parseInt(m_len.getText());
      final int numframes = len*fps;

      final ProgressDialog dlg = new ProgressDialog(getShell());
      IRunnableWithProgress runnable = new IRunnableWithProgress() {
        public void run(IProgressMonitor monitor) throws InvocationTargetException,
          InterruptedException {
          movie.setProgressMonitor(monitor);
          try {
            movie.createMovie(new File(m_outFile.getText()), fps, numframes);
          } catch (MovieMakerException e) {
            Traffix.error(e.getMessage());
          }
          movie.dispose();
        }
      };
      try {
        dlg.run(false, true, runnable);
      } catch (InvocationTargetException e) {
        Traffix.reportException(e.getCause());
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      Traffix.model().setModified(true);
      Traffix.simManager().fireUpdated();
    } finally {
      super.okPressed();
    }

  }

  private void syncControls() {
    NumberFormat fmt = new DecimalFormat("000");
    int scale = m_scale.getSelection();
    int propo = m_proportion.getSelection();

    m_scaleLab.setText("Skala " + fmt.format(scale) + "%");
    m_proportionLab.setText("Proporcja " + fmt.format(propo) + "%");

    Point dims = calcOutDims();
    m_outDimsLab.setText(Integer.toString(dims.x) + "x" + Integer.toString(dims.y));
  }
}