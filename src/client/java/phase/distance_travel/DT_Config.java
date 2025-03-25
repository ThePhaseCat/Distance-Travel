package phase.distance_travel;

import eu.midnightdust.lib.config.MidnightConfig;

public class DT_Config extends MidnightConfig
{
    @Comment(centered = true) public static Comment text1;

    @Entry public static boolean odoMode = false;

    @Entry public static boolean printTrackingMessages = false;

    @Entry public static boolean goToStatsAfterDone = false;

    @Comment(centered = true) public static Comment text2;

    @Comment public static Comment text3;

    @Comment public static Comment text4;

    @Entry public static int timerInterval = 2500;
}