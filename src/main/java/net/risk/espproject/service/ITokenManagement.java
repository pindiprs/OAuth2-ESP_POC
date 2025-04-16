package net.risk.espproject.service;

public interface ITokenManagement {
    String getToken(String userName, String clientId, String authorize_elements);
}
