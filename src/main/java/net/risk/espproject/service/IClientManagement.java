package net.risk.espproject.service;

public interface IClientManagement {
    String registerClient(String publicKey, String realm, String userName, String password);
    String rotateClient(String publicKey, String realm, String userName, String password);
    String retrieveClientID(String realm, String userName, String password);
    boolean deleteClient(String realm, String userName, String password);

}
