package net.risk.phiauth.constant;

public enum AcurrientAuthenticationStatus {
    SUCCESS(0),
    USER_NOT_FOUND(1),
    PASSWORD_NOT_RIGHT(2),
    SUSPENDED(3),
    COMPANY_STATUS_INVALID(4),
    IP_ADDRESS_INVALID(5),
    ADMIN_AUTHENTICATION_ERROR(6),
    PATH_NOT_FOUND(7),
    INVALID_PERMISSION_VALUE(9),
    INVALID_OFFSET_IN_MAPPING_TABLE(10),
    USER_ALREADY_EXISTS(11),
    COMPANY_ID_NOT_EXIST(12),
    INCORRECT_INPUT(13),
    COMPANY_NOT_FOUND(14),
    PASSWORD_NOT_GOOD_ENOUGH(15),
    FREE_TRIAL_USED_UP(17),
    COMPANY_ID_MISMATCH_ADMIN_USER(18),
    PASSWORD_EXPIRED(19),
    REQUIRED_TO_CHANGE_PASSWORD(20),
    ACP_DEFINED_AT_COMPANY_LEVEL(21),
    PARENT_COMPANY_CHAIN_TOO_LONG(22),
    ACCESS_FROM_COUNTRY_NOT_PERMITTED(23),
    INVALID_HIERARCHY_CHAIN(24);

    private final int code;

    AcurrientAuthenticationStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static AcurrientAuthenticationStatus fromCode(int code) {
        for (AcurrientAuthenticationStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid authentication status: " + code);
    }
}