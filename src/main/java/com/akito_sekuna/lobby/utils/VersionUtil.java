package com.akito_sekuna.lobby.utils;

public final class VersionUtil {

    private VersionUtil() {}

    public static boolean isCompatible(String addonVer, String coreVer) {
        try {
            String[] addonParts = addonVer.split("\\.");
            String[] coreParts = coreVer.split("\\.");

            if (addonParts.length < 2 || coreParts.length < 2) return false;

            int addonMajor = Integer.parseInt(addonParts[0]);
            int addonMinor = Integer.parseInt(addonParts[1]);

            int coreMajor = Integer.parseInt(coreParts[0]);
            int coreMinor = Integer.parseInt(coreParts[1]);

            return (addonMajor == coreMajor) && (addonMinor == coreMinor);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
