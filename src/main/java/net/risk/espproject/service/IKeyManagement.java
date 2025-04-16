package net.risk.espproject.service;

public interface IKeyManagement {
    String registerClient(String publicKey, String realm, String userName, String password);
}
