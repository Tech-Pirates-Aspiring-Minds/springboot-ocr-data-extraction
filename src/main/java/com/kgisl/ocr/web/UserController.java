package com.kgisl.ocr.web;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.kgisl.ocr.model.User;
import com.kgisl.ocr.service.SecurityService;
import com.kgisl.ocr.service.UserService;
import com.kgisl.ocr.validator.CaptchaValidator;
import com.kgisl.ocr.validator.LoginValidator;
import com.kgisl.ocr.validator.UserValidator;

@Controller
public class UserController {
	@Autowired
	private UserService userService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private UserValidator userValidator;

	@Autowired
	private LoginValidator loginValidator;
	
	@Autowired
	private CaptchaValidator captchaValidator;

	private static final String CAPTCHA_VALUE = "g-recaptcha-response";
	    
    private Logger logger = LoggerFactory.getLogger(UserController.class);
    
	@GetMapping("/registration")
	public String registration(Model model) {
		
		logger.info("inside GET registration() method ");
		model.addAttribute("userForm", new User());
		return "registration";
	}

	@PostMapping("/registration")
	public String registration(@ModelAttribute("userForm") User userForm, BindingResult bindingResult) {
		
		logger.info("inside POST registration() method");
		userValidator.validate(userForm, bindingResult);

		if (bindingResult.hasErrors()) {
			return "registration";
		}

		userService.save(userForm);

		securityService.autoLogin(userForm.getUsername(), userForm.getPasswordConfirm());

		return "redirect:/welcome";
	}

/*	@GetMapping("/login")
	public String login(Model model, String error, String logout) {
		if (error != null)
			model.addAttribute("error", "Your username and password is invalid.");

		if (logout != null)
			model.addAttribute("message", "You have been logged out successfully.");

		return "login";
	}
*/
	
	@GetMapping("/login")
	public String login(Model model) {
		
		logger.info("inside GET login() method ");
		model.addAttribute("userForm", new User());
		return "login";
	}
	
    @PostMapping("/login")
    public String login(@ModelAttribute("userForm") User userForm, Model model, BindingResult bindingResult, @RequestParam(CAPTCHA_VALUE) String captchaResponse) {

		logger.info("inside POST login() method ");
	
    	loginValidator.validate(userForm, bindingResult);
		if (bindingResult.hasErrors()) {
			return "login";
		}
		
    	if(!captchaValidator.validateCaptcha(captchaResponse, model)) {
    		return "login";
    	}
    	else {
    		securityService.autoLogin(userForm.getUsername(), userForm.getPassword());
    		return "redirect:/welcome";
    	}
    }
    
	@GetMapping({ "/", "/welcome" })
	public String welcome(Model model) {
		return "ocr";
	}

}
