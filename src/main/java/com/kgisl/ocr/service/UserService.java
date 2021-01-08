package com.kgisl.ocr.service;

import com.kgisl.ocr.model.User;

public interface UserService {
	void save(User user);

	User findByUsername(String username);

}
