package net.risk.phiauth.constant;

public class SQLQueriesConstants {
    public static final String ALL_DATA_FROM_REALM = "SELECT * FROM esp_oauth_test.oauth2_keys";
    public static final String GET_TOKEN_KEYS_USING_REALM = "SELECT * FROM esp_oauth_test.oauth2_keys WHERE use_case = ?";
}
