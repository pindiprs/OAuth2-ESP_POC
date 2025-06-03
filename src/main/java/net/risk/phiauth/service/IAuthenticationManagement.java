package net.risk.phiauth.service;

import net.risk.phiauth.constant.AcurrientAuthenticationStatus;

public interface IAuthenticationManagement {

    AcurrientAuthenticationStatus authenticate(String userId, String credential);
}
