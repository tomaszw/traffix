/*
 * Created on 2004-08-24
 */

package traffix.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public abstract class VerifiedDialog extends Dialog {
  private Label m_validationLabel;
  private Image m_image;
  private Image m_warningImg = Images.get("icons/validationErr.gif");

  protected VerifiedDialog(Shell parentShell) {
    super(parentShell);
  }

  protected void okPressed() {
    if (getValidationError() == null)
      super.okPressed();
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);

    newShell.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        disposeLabelImage();
      }
    });
  }

  public abstract String getValidationError();

  public void setValidationLabel(Label lab) {
    m_validationLabel = lab;
    lab.setText("foo");
  }

  public void validate() {
    updateButtons();
    updateLabel();
  }

  private void updateButtons() {
    String err = getValidationError();
    Button okBtn = getButton(OK);
    if (okBtn != null)
      okBtn.setEnabled(err == null);
  }

  protected Control createContents(Composite parent) {
    Control res = super.createContents(parent);
    validate();
    if (getButton(CANCEL) != null)
      getButton(CANCEL).setText("Anuluj");
    return res;
  }

  private void disposeLabelImage() {
    if (m_image != null) {
      if (!m_image.isDisposed())
        m_image.dispose();
      m_image = null;
    }
  }

  private void updateLabel() {
    if (m_validationLabel == null)
      return;

    Rectangle bounds = m_validationLabel.getBounds();
    bounds.width = Math.max(1, bounds.width);
    bounds.height = Math.max(1, bounds.height);

    disposeLabelImage();
    String err = getValidationError();
    if (err == null) {
      m_validationLabel.setImage(null);
      m_validationLabel.setText("foo");
      m_validationLabel.setBackground(Colors.get(new RGB(0, 0, 0)));
    } else {
      GC labgc = new GC(m_validationLabel);
      Point szText = labgc.textExtent(err);
      labgc.dispose();

      bounds.height = szText.y;
      bounds.width = szText.x + m_warningImg.getBounds().width + 4;
      m_image = new Image(Display.getDefault(), bounds);
      GC gc = new GC(m_image);
      gc.drawImage(m_warningImg, 0, 0);
      gc.drawText(err, m_warningImg.getBounds().width + 4, 0);
      gc.dispose();
      m_validationLabel.setImage(m_image);
      m_validationLabel.setBackground(Colors.get(new RGB(255, 255, 255)));

    }
  }

  public void addVerifiedControl(Slider ctrl) {
    ctrl.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        validate();
      }
    });
  }

  public void addVerifiedControl(Text ctrl) {
    ctrl.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        validate();
      }
    });
  }

  public void addVerifiedControl(Scale ctrl) {
    ctrl.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        validate();
      }
    });
  }

  public void addVerifiedControl(Combo ctrl) {
    ctrl.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        validate();
      }
    });
  }
}