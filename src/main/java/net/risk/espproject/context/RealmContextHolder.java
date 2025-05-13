package net.risk.espproject.context;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class RealmContextHolder {
    private static final ThreadLocal<String> realmContext = new ThreadLocal<>();

    public static void setRealm(String realm) {
        realmContext.set(realm);
    }

    public static String getRealm() {
        return realmContext.get() != null ? realmContext.get() : "AccAuth"; // Default fallback
    }

    public static void clear() {
        realmContext.remove();
    }
    /**
     * realms/AccAuth/oauth2/token
     */
}
