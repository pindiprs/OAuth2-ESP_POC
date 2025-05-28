package net.risk.phiauth.util;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.util.Base64URL;
import lombok.experimental.UtilityClass;
import net.risk.phiauth.constant.KEY_STATUS;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static net.risk.phiauth.constant.AuthConfigConstants.SIGNING_ALG;

@UtilityClass
public class KeyUtils {
    public static ECKey generateECKey(String kid, String x, String y, String d) {
        return new ECKey.Builder(
                Curve.P_256,
                new Base64URL(x),
                new Base64URL(y)
        )
                .d(new Base64URL(d))
                .keyID(kid)
                .algorithm(new Algorithm(SIGNING_ALG))
                .build();
    }
    public static KEY_STATUS checkStatus(int statusInt) {
        return switch (statusInt) {
            case 0 -> KEY_STATUS.EXPIRED;
            case 1 -> KEY_STATUS.ACTIVE;
            case 2 -> KEY_STATUS.FUTURE;
            case 3 -> KEY_STATUS.OBSOLETE;
            default -> throw new IllegalArgumentException("Invalid status: " + statusInt);
        };
    }

    public static boolean rotateKeys(String realm, Map<String, String> realmMap) {

        KEY_STATUS currentStatus = KeyUtils.checkStatus(Integer.parseInt(realmMap.get("status")));
        if(currentStatus == KEY_STATUS.OBSOLETE) {
            // delete the keys
        }
        if(currentStatus == KEY_STATUS.ACTIVE) {
            if(isKeyExpired(realmMap)){
                // create new active key
                // create new future key
            }
        }
        return false;
    }

    private static boolean isKeyExpired(Map<String, String> realmMap) {
        String expiredAt = realmMap.get("date_expire");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // Custom format
        LocalDateTime expiredAtTime = LocalDateTime.parse(expiredAt, formatter);
        long hoursUntilExpired = LocalDateTime.now().until(expiredAtTime, ChronoUnit.HOURS);
        return hoursUntilExpired <= 48;
    }
}
