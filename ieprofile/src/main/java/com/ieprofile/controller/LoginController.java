package com.ieprofile.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LoginController {
	
	@Value("${GMAIL_CLIENT_ID}")
	private String gmailClientId;
	
	@Value("${GMAIL_CLIENT_SECRET}")
	private String gmailClientSecret;
	
	@Value("${GMAIL_CALLBACK_URI}")
	private String callBackUrl;
	
	@Value("${GMAIL_SCOPE}")
	private String gmailScope;
	
	@Value("${GMAIL_USER_INFO_URL}")
	private String gmailUserInfoUrl;

	@RequestMapping(value="/", method = RequestMethod.GET)
	protected String landing(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws Exception {

		ModelAndView model = new ModelAndView();
		//generateStateToken();
		//model.addObject("gmailurl", buildGmailLoginUrl());
		//session.setAttribute("state", stateToken);
		//session.setAttribute("GOOGLE_FLOW", flow);

		return "home";
	}
	

}