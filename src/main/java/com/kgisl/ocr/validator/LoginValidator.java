package com.kgisl.ocr.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.kgisl.ocr.model.User;
import com.kgisl.ocr.service.UserService;

@Component
public class LoginValidator implements Validator {

	@Autowired
	private UserService userService;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	private Logger logger = LoggerFactory.getLogger(LoginValidator.class);

	@Override
	public boolean supports(Class<?> aClass) {
		return User.class.equals(aClass);
	}

	@Override
	public void validate(Object obj, Errors errors) {

		logger.info("Validating request from login Form");
		User user = (User) obj;

		User userFromDB = userService.findByUsername(user.getUsername());
		if (userFromDB == null) {
			logger.info("userFromDB is null: " + userFromDB);
			errors.rejectValue("username", "Name.loginForm.nosuchuser");
		} 
		else {
			logger.info("user is available in DB");
			if (!bCryptPasswordEncoder.matches(user.getPassword(), userFromDB.getPassword())) {
				logger.debug("passwords dont match ");
				errors.rejectValue("password", "Pass.loginForm.invalidPassword");
			}
		}
	}
}