package com.kgisl.ocr.validator;

import com.kgisl.ocr.service.CaptchaService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
public class CaptchaValidator {

	@Autowired
	private CaptchaService captchaService;

	private Logger logger = LoggerFactory.getLogger(CaptchaValidator.class);

	public boolean validateCaptcha(String captchaResponse, Model model) {

		boolean isValidCaptcha = false;
		isValidCaptcha = captchaService.getCaptchaAPIResponse(captchaResponse);
		logger.info("isValidCaptcha: " + isValidCaptcha);
		
		// Hard coding only for Unit test - To be removed
		// isValidCaptcha to be set as false;
		
		if (!isValidCaptcha) {
			model.addAttribute("error", "Invalid captcha.");
		}
		return isValidCaptcha;
	}

}