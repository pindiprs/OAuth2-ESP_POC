package net.risk.phiauth.service.impl;

import net.risk.phiauth.config.DbConfig;
import net.risk.phiauth.config.ServiceConfig;
import net.risk.phiauth.constant.ConfigKeys;
import net.risk.phiauth.constant.AcurrientAuthenticationStatus;
import net.risk.phiauth.service.IAuthenticationManagement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthenticationServiceImpl implements IAuthenticationManagement {

    private final JdbcTemplate jdbcTemplate;
    private final ServiceConfig serviceConfig;
    public AuthenticationServiceImpl(DbConfig dbConfig, ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
        String url = serviceConfig.getAccurintUrl();
        String username = serviceConfig.getAccurintUsername();
        String password = serviceConfig.getAccurintPassword();
        this.jdbcTemplate = new JdbcTemplate(dbConfig.createDataSource(url, username, password));
    }

    @Override
    public AcurrientAuthenticationStatus authenticate(String userId, String credential) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName(ConfigKeys.ACCURINT_DB_SPROC);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_loginid", userId)
                .addValue("p_password", credential)
                .addValue("p_application", "")
                .addValue("p_clientip", "10.145.44.113")
                .addValue("p_checkpasswd", 1)
                .addValue("p_checkip", 1)
                .addValue("p_authenticateonly", 0)
                .addValue("p_checksuspended", 1)
                .addValue("p_checkpasswordexpiration", 1)
                .addValue("p_get_company_tags_only", 0)
                .addValue("p_active_company_id", null)
                .addValue("p_get_all_tags", 1);

        Map<String, Object> result = jdbcCall.execute(params);

        int statusCode = (Integer) result.get("p_successcode");
        return AcurrientAuthenticationStatus.fromCode(statusCode);
    }
}