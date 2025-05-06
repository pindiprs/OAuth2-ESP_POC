package net.risk.espproject.command.impl;

import net.risk.espproject.command.IClientManagement;

public class ClientManagementImpl implements IClientManagement {
    @Override
    public String registerClient(String publicKey, String realm, String userName, String password) {
        return "";
    }

    @Override
    public String rotateClientKeys(String publicKey, String realm, String userName, String password) {
        return "";
    }

    @Override
    public String retrieveClientID(String realm, String userName, String password) {
        return "";
    }

    @Override
    public boolean deleteClient(String realm, String userName, String password) {
        return false;
    }
}
