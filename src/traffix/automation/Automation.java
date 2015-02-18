/*
 * Created on 2004-07-11
 */

package traffix.automation;

import org.eclipse.swt.internal.ole.win32.TYPEATTR;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleFunctionDescription;
import org.eclipse.swt.ole.win32.OlePropertyDescription;
import org.eclipse.swt.ole.win32.Variant;

public class Automation {
  private OleAutomation m_auto;

  public static Automation from(OleAutomation auto) {
    return new Automation(auto);
  }

  public static Automation from(Variant auto) {
    return from(auto.getAutomation());
  }

  public Automation(OleAutomation auto) {
    m_auto = auto;
  }

  public OleAutomation getOleAutomation() {
    return m_auto;
  }

  public void dispose() {
    m_auto.dispose();
  }

  public boolean equals(Object obj) {
    return m_auto.equals(obj);
  }

  public String getDocumentation(int dispId) {
    return m_auto.getDocumentation(dispId);
  }

  public OleFunctionDescription getFunctionDescription(int index) {
    return m_auto.getFunctionDescription(index);
  }

  public String getHelpFile(int dispId) {
    return m_auto.getHelpFile(dispId);
  }

  public int[] getIDsOfNames(String[] names) {
    return m_auto.getIDsOfNames(names);
  }

  public String getLastError() {
    return m_auto.getLastError();
  }

  public String getName(int dispId) {
    return m_auto.getName(dispId);
  }

  public String[] getNames(int dispId, int maxSize) {
    return m_auto.getNames(dispId, maxSize);
  }

  public Variant getProperty(int dispIdMember) {
    return m_auto.getProperty(dispIdMember);
  }

  public Variant getProperty(int dispIdMember, Variant[] rgvarg) {
    return m_auto.getProperty(dispIdMember, rgvarg);
  }

  public Variant getProperty(int dispIdMember, Variant[] rgvarg, int[] rgdispidNamedArgs) {
    return m_auto.getProperty(dispIdMember, rgvarg, rgdispidNamedArgs);
  }

  public int getID(String name) {
    int[] ids = getIDsOfNames(new String[]{name});
    return ids[0];
  }

  public Variant getProperty(String name) {
    return getProperty(getID(name));
  }

  public Variant getProperty(String name, Variant arg1) {
    return getProperty(getID(name), new Variant[]{arg1});
  }

  public Variant getProperty(String name, Variant arg1, Variant arg2) {
    return getProperty(getID(name), new Variant[]{arg1, arg2});
  }

  public Variant getProperty(String name, Variant arg1, Variant arg2, Variant arg3) {
    return getProperty(getID(name), new Variant[]{arg1, arg2, arg3});
  }

  public OlePropertyDescription getPropertyDescription(int index) {
    return m_auto.getPropertyDescription(index);
  }

  public TYPEATTR getTypeInfoAttributes() {
    return m_auto.getTypeInfoAttributes();
  }

  public int hashCode() {
    return m_auto.hashCode();
  }

  public Variant invoke(int dispIdMember) {
    return m_auto.invoke(dispIdMember);
  }

  public Variant invoke(String name) {
    return invoke(getID(name));
  }

  public Variant invoke(String name, Variant arg1) {
    return invoke(getID(name), new Variant[]{arg1});
  }

  public Variant invoke(String name, Variant arg1, Variant arg2) {
    return invoke(getID(name), new Variant[]{arg1, arg2});
  }

  public Variant invoke(String name, Variant arg1, Variant arg2, Variant arg3) {
    return invoke(getID(name), new Variant[]{arg1, arg2, arg3});
  }

  public Variant invoke(int dispIdMember, Variant[] rgvarg) {
    return m_auto.invoke(dispIdMember, rgvarg);
  }

  public Variant invoke(int dispIdMember, Variant[] rgvarg, int[] rgdispidNamedArgs) {
    return m_auto.invoke(dispIdMember, rgvarg, rgdispidNamedArgs);
  }

  public void invokeNoReply(int dispIdMember) {
    m_auto.invokeNoReply(dispIdMember);
  }

  public void invokeNoReply(int dispIdMember, Variant[] rgvarg) {
    m_auto.invokeNoReply(dispIdMember, rgvarg);
  }

  public void invokeNoReply(int dispIdMember, Variant[] rgvarg, int[] rgdispidNamedArgs) {
    m_auto.invokeNoReply(dispIdMember, rgvarg, rgdispidNamedArgs);
  }

  public boolean setProperty(int dispIdMember, Variant rgvarg) {
    return m_auto.setProperty(dispIdMember, rgvarg);
  }

  public boolean setProperty(int dispIdMember, Variant[] rgvarg) {
    return m_auto.setProperty(dispIdMember, rgvarg);
  }

  public boolean setProperty(String name, Variant arg) {
    return setProperty(getID(name), arg);
  }

  public boolean setProperty(String name, Variant arg1, Variant arg2) {
    return setProperty(getID(name), new Variant[]{arg1, arg2});
  }

  public boolean setProperty(String name, Variant arg1, Variant arg2, Variant arg3) {
    return setProperty(getID(name), new Variant[]{arg1, arg2, arg3});
  }

  public String toString() {
    return m_auto.toString();
  }
}