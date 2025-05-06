package net.risk.espproject.command;

public interface IClientManagement {
    String registerClient(String publicKey, String realm, String userName, String password);
    String rotateClientKeys(String publicKey, String realm, String userName, String password);
    String retrieveClientID(String realm, String userName, String password);
    boolean deleteClient(String realm, String userName, String password);

}
