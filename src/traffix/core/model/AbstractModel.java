/*
 * Created on 2004-07-03
 */

package traffix.core.model;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import traffix.Traffix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class AbstractModel {
  private String m_defaultExtension = "tfx";
  private String m_fileName = "Projekt.tfx";
  private String m_filePath = "";
  private boolean m_hasFilename = false;
  private boolean m_modified = false;
  private int m_modifyCount = 0;
  private Shell m_shell;

  public AbstractModel() {
  }

  // returns true if closing is allowed
  public boolean close() {
    if (m_modified) {
      MessageBox dlg = new MessageBox(m_shell, SWT.YES | SWT.NO | SWT.CANCEL
        | SWT.ICON_QUESTION);
      dlg.setText(Traffix.NAME);
      dlg.setMessage("\"" + m_fileName + "\" zosta³ zmodyfikowany. Zapisaæ zmiany?");
      int ret = dlg.open();
      if (ret == SWT.YES) {
        save();
        return true;
      } else if (ret == SWT.CANCEL)
        return false;
    }
    m_fileName = "Projekt.tfx";
    m_hasFilename = false;
    m_filePath = "";
    m_modified = false;
    return true;
  }

  public void create() {
    if (close()) {
      m_fileName = "Projekt.tfx";
      m_hasFilename = false;
      m_filePath = "";
      m_modified = false;
    }
  }

  public String getBaseFileName() {
    int dot = m_fileName.lastIndexOf('.');
    if (dot == -1)
      return m_fileName;
    return m_fileName.substring(0, dot);
  }

  public String getFileName() {
    return m_fileName;
  }

  public int getModifyCount() {
    return m_modifyCount;
  }

  public String getPath() {
    return m_filePath;
  }

  public String getTitle() {
    if (!m_modified)
      return m_fileName;
    else
      return m_fileName + "*";
  }

  public boolean hasFilename() {
    return m_hasFilename;
  }

  public boolean isModified() {
    return m_modified;
  }

  public void open() {
    FileDialog f = new FileDialog(m_shell, SWT.OPEN);
    f.setFilterExtensions(new String[]{"*." + m_defaultExtension, "*.*"});
    String filename = f.open();
    if (filename != null && close()) {
      if (readContents(filename)) {
        setPath(filename);
        setModified(false);
      }
    }
  }

  public abstract boolean readContents(String filename);

  public void save() {
    if (!m_hasFilename)
      saveAs();
    else {
      if (saveContents(getPath()))
        setModified(false);
    }
  }

  public void saveAs() {
    FileDialog f = new FileDialog(m_shell, SWT.SAVE);
    f.setFilterExtensions(new String[]{"*." + m_defaultExtension, "*.*"});
    f.setFileName(m_fileName);
    String filename = f.open();
    if (filename != null) {
      boolean exists = true;

      try {
        (new FileInputStream(filename)).close();
      } catch (FileNotFoundException e) {
        exists = false;
      } catch (IOException e) {
      }

      if (exists) {
        MessageBox dlg = new MessageBox(m_shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
        dlg.setText(Traffix.NAME);
        dlg.setMessage("Plik \"" + filename + "\" ju¿ istnieje. Nadpisaæ?");
        if (dlg.open() != SWT.YES)
          return;
      }

      setPath(filename);
      save();
    }
  }

  public abstract boolean saveContents(String filename);

  public void setModified(boolean modified) {
    m_modified = modified;
    if (modified)
      ++m_modifyCount;
  }

  public void setFileName(String fname) {
    int idx = m_filePath.lastIndexOf(m_fileName);
    if (idx != -1) {
      String dirPath = m_filePath.substring(0, idx);
      m_fileName = fname;
      m_filePath = dirPath + m_fileName;
    } else {
      m_fileName = fname;
    }
  }
    
  public void setPath(String path) {
    File file = new File(path);
    m_filePath = file.getAbsolutePath();
    m_fileName = file.getName();
    m_hasFilename = true;
  }

  public void setShell(Shell shell) {
    m_shell = shell;
  }
}