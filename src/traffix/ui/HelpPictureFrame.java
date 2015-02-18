/*
 * Created on 2004-07-12
 */

package traffix.ui;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.ole.win32.COM;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import traffix.Traffix;
import traffix.core.TerrainMap;
import traffix.core.schedule.Schedule;
import traffix.ui.Images;
import traffix.ui.Keymap;

import java.io.File;
import java.io.FileInputStream;

public class HelpPictureFrame extends Window {
  private Canvas m_canvas;
  private ScrolledComposite m_scrolledComp;
  private Image m_image;
  private File m_helpFile;

  public HelpPictureFrame(Shell parentShell, File helpFile) {
    super(parentShell);
    setShellStyle(getShellStyle() & ~(SWT.SYSTEM_MODAL|SWT.APPLICATION_MODAL));
    //setShellStyle(getShellStyle() | SWT.SYSTEM_MODAL);
    m_helpFile = helpFile;
    m_image = new Image(Display.getDefault(), helpFile.getAbsolutePath());
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setSize(800, 600);
    newShell.setImage(Images.get("icons/qmark20x20.gif"));
    newShell.setText(m_helpFile.getName());
    newShell.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        m_image.dispose();
      }
    });
  }

  protected Control createContents(Composite parent) {
    parent.setLayout(new FillLayout());
    m_scrolledComp = new ScrolledComposite(parent, SWT.H_SCROLL|SWT.V_SCROLL);
    m_scrolledComp.setAlwaysShowScrollBars(true);
    m_scrolledComp.setLayout(new FillLayout());
    m_canvas = new Canvas(m_scrolledComp, SWT.NO_BACKGROUND);
    m_canvas.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
    Rectangle b = m_image.getBounds();
    m_canvas.setSize(b.width, b.height);
    m_canvas.addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        paintImage(e.gc);
      }
    });

    m_scrolledComp.setContent(m_canvas);

    m_scrolledComp.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
      }

      public void keyReleased(KeyEvent e) {
        if (e.keyCode == Keymap.HELP_KEY)
          close();
      }
    });

    return m_scrolledComp;//contents;
  }

  private void paintImage(GC gc) {
    gc.drawImage(m_image, 0, 0);
  }
}