package ph.com.team.gobiker.settings;

public final class SettingsService {
    private static boolean kphFlag = true;

    public static boolean isKphFlag() {
        return kphFlag;
    }

    public static void toggleIsKphFlag() {
        SettingsService.kphFlag = !SettingsService.kphFlag;
    }
}
