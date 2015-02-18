/*
 * Created on 2004-07-12
 */

package traffix.ui;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

import traffix.Traffix;
import traffix.core.McmMatrix;
import traffix.ui.misc.SwtKit;

public class McmMatrixFrame extends Window {
  Canvas m_canvas;
  McmMatrix m_mcmMatrix;
  private Point[][] m_mcmMatrixSizes;

  private String[][] m_mcmMatrixValues;

  public McmMatrixFrame(Shell parentShell, McmMatrix mcmMatrix) {
    super(parentShell);
    setShellStyle(getShellStyle() | SWT.SYSTEM_MODAL);
    m_mcmMatrix = mcmMatrix;
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setImage(Images.get("icons/traffix.gif"));
    newShell.setText("Podgl¹d tabeli czasów miêdzyzielonych");
    newShell.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        onKeyDown(e);
      }

      public void keyReleased(KeyEvent e) {
      }
    });

  }

  protected Control createContents(Composite parent) {
    parent.setLayout(new FillLayout());
    ScrolledComposite contents = new ScrolledComposite(parent, SWT.H_SCROLL
      | SWT.V_SCROLL);
    contents.setLayout(new FillLayout());

    m_canvas = new Canvas(contents, SWT.NONE);
    cacheMcmMatrix();
    Point canvasSz = computeCanvasSize();
    m_canvas.setSize(canvasSz);
    m_canvas.addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        paintMcmMatrix(e.gc);
      }
    });

    contents.setContent(m_canvas);
    contents.setOrigin(0, 0);

    Rectangle bounds = getShell().computeTrim(0, 0, canvasSz.x, canvasSz.y);
    bounds.x = Math.min(bounds.width, 800);
    bounds.y = Math.min(bounds.height, 600);
    getShell().setBounds(bounds);
    return contents;
  }

  private int getMatrixDim() {
    // hack - use number of groups instead of matrix dimension so
    // too big matrices can be displayed anyway...
    return Traffix.model().getNumGroups();
  }

  private void cacheMcmMatrix() {
    GC gc = new GC(m_canvas);
    int dim = getMatrixDim();
    m_mcmMatrixValues = new String[dim + 1][dim + 1];
    m_mcmMatrixSizes = new Point[dim + 1][dim + 1];
    for (int i = 0; i < dim + 1; ++i) {
      for (int j = 0; j < dim + 1; ++j) {
        String val = "";
        if (i > 0 && j > 0) {
          if (m_mcmMatrix.areColliding(i - 1, j - 1)) {
            val = Integer.toString(m_mcmMatrix.getMcmTime(i - 1, j - 1));
          }
        } else if (i == 0 && j == 0) {
          val = "";
        } else if (i == 0) {
          val = Traffix.model().getGroupByIndex(j - 1).getPrefix()
            + Traffix.model().getGroupByIndex(j - 1).getNum();
        } else {
          val = Traffix.model().getGroupByIndex(i - 1).getPrefix()
            + Traffix.model().getGroupByIndex(i - 1).getNum();
        }
        Point ext = gc.textExtent(val);
        ext.x += 6;
        ext.y += 2;
        m_mcmMatrixValues[i][j] = val;
        m_mcmMatrixSizes[i][j] = ext;
      }
    }
    gc.dispose();
  }

  private Point computeCanvasSize() {
    Point cellSz = computeCellSize();
    int dim = getMatrixDim();
    cellSz.x *= dim + 1;
    cellSz.y *= dim + 1;
    return cellSz;
  }

  private Point computeCellSize() {
    int maxW = 0, maxH = 0;
    int dim = getMatrixDim();
    for (int i = 0; i < dim + 1; ++i) {
      for (int j = 0; j < dim + 1; ++j) {
        maxW = Math.max(maxW, m_mcmMatrixSizes[i][j].x);
        maxH = Math.max(maxH, m_mcmMatrixSizes[i][j].y);
      }
    }
    return new Point(maxW, maxH);
  }

  private void onKeyDown(KeyEvent e) {
    if (e.keyCode == SWT.ESC || e.keyCode == Keymap.MCM_MATRIX_KEY)
      close();
    if (e.keyCode == Keymap.SCREENSHOT_KEY)
      screenshotToClipboard();
  }

  private void screenshotToClipboard() {
    Point sz = computeCanvasSize();
    Image img = new Image(getShell().getDisplay(), sz.x, sz.y);
    GC gc = new GC(img);
    paintMcmMatrix(gc);
    gc.dispose();
    SwtKit.swtImageToClipboard(getShell(), img);
    img.dispose();
  }
  
  private void paintMcmMatrix(GC gc) {
    Point sz = m_canvas.getSize();
    Point cellSz = computeCellSize();

    Image memImage = new Image(m_canvas.getDisplay(), sz.x, sz.y);
    GC memGc = new GC(memImage);

    int dim = getMatrixDim();
    int y = 0;
    for (int i = 0; i < dim + 1; ++i) {
      int x = 0;
      for (int j = 0; j < dim + 1; ++j) {
        String txt = m_mcmMatrixValues[i][j];
        Point ext = memGc.textExtent(txt);
        memGc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        memGc.drawText(txt, x + cellSz.x/2 - ext.x/2, y + cellSz.y/2 - ext.y/2);

        if (i == j) {
          //memGc.setLineWidth(2);
          //memGc.drawLine(x+2,y+2,x+cellSz.x-4,y+cellSz.y-4);
          //memGc.drawLine(x+cellSz.x-4,y+2,x+2,y+cellSz.y-4);
          memGc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
          memGc.fillRectangle(x, y, cellSz.x, cellSz.y);
        }
        x += cellSz.x;
      }
      y += cellSz.y;
    }
    memGc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

    memGc.setLineWidth(2);
    memGc.drawLine(0, cellSz.y, sz.x, cellSz.y);
    memGc.drawLine(cellSz.x, 0, cellSz.x, sz.y);
    //memGc.drawLine(cellSz.x, cellSz.y, sz.x, sz.y);
    memGc.setLineWidth(1);
    for (int i = 1; i < dim + 1; ++i) {
      memGc.drawLine(0, i*cellSz.y, sz.x, i*cellSz.y);
      memGc.drawLine(i*cellSz.x, 0, i*cellSz.x, sz.y);
    }

    gc.drawImage(memImage, 0, 0);
    memGc.dispose();
    memImage.dispose();
  }
}