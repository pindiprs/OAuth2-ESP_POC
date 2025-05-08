package net.risk.espproject.util;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.util.Base64URL;
import lombok.experimental.UtilityClass;
import net.risk.espproject.constant.KEY_STATUS;

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
                .algorithm(new Algorithm("ES256"))
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
}
