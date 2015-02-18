/*
 * Created on 2004-07-31
 */

package traffix;


public class NativeUtils {
  static {
    System.loadLibrary("NativeUtils");
  }

  native public static int Win_LoadCursorFromFile(String file);

  public static double clock() {
    return System.nanoTime()/1000000000.0;
  }
//  native public static double clock();
}
