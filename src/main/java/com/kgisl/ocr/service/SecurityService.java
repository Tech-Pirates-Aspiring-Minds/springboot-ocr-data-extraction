package com.kgisl.ocr.service;

public interface SecurityService {
    String findLoggedInUsername();

    void autoLogin(String username, String password);
}
