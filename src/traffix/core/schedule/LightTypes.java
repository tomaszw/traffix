/*
 * Created on 2004-06-27
 */

package traffix.core.schedule;

public final class LightTypes {
  public static final int RED = 0;
  public static final int GREEN = 1;
  public static final int YELLOW = 2;
  public static final int PULSING_YELLOW = 3;
  public static final int PULSING_GREEN = 4;
  public static final int PULSING_RED = 5;
  public static final int RED_YELLOW = 6;
  public static final int NO_SIGNAL = 7;

  public static final int NUM_LIGHTS = 8;
  public static int s_lightTypesMenuOrder[] = {
    GREEN,
    RED,
    NO_SIGNAL,
    PULSING_YELLOW,
    PULSING_RED,
    PULSING_GREEN,
    RED_YELLOW,
    YELLOW
  };

  public static final boolean isGreen(int light) {
    return light == PULSING_GREEN || light == GREEN;
  }

  public static final boolean isBlocking(int light) {
    return light == RED || light == YELLOW || light == PULSING_RED
      || light == PULSING_YELLOW || light == RED_YELLOW || light == NO_SIGNAL;
  }

  public static final String[] names = {"Czerwone", "Zielone", "¯ó³te",
                                        "¯ó³te Migaj¹ce", "Zielone Migaj¹ce", "Czerwone Migaj¹ce", "¯ó³to-czerwone",
                                        "Brak Sygna³u"};
}