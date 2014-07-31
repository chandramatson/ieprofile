/**
 * 
 */
package com.ieprofile.controller.gmail;

import java.io.PrintWriter;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.ieprofile.common.EmailRepo;
import com.ieprofile.common.messageconverter.SSEvent;
import com.ieprofile.helper.gmail.EmailListener;
import com.ieprofile.helper.gmail.OAuth2Authenticator;
import com.ieprofile.vo.MessageBean;

/**
 * @author chandrasekharpappala
 *
 */
@Controller
public class GmailAuthController {
	
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
	
	@Autowired
	OAuth2Authenticator authenticator;
	
	@Autowired
	EmailListener emailListener;
	
	@Autowired
	EmailRepo repo;

	private GoogleAuthorizationCodeFlow flow;
	
	private String stateToken;
	
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	
	@PostConstruct
	public void afterPropertiesSet() throws Exception{
	
		flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT,
				new JacksonFactory(), gmailClientId, gmailClientSecret, Arrays.asList(gmailScope.split(";"))).build();
	}
	
	@RequestMapping(value ="/glogin",method = RequestMethod.GET)
	public void autherizeGmailUser(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws Exception {
		generateStateToken();
		//model.addObject("gmailurl", buildGmailLoginUrl());
		session.setAttribute("state", stateToken);
		String url = buildGmailLoginUrl();
		new URL(url).openStream();
		
		response.sendRedirect(url);
		//
		//request.getRequestDispatcher(buildGmailLoginUrl()).forward(request, response);
		
		
	}

	@RequestMapping(value ="/auth",method = RequestMethod.GET)
	public String responseFromGmail(HttpServletRequest request, HttpSession session, ModelAndView mv) throws Exception {
		if (request.getParameter("code") != null && request.getParameter("state") != null
				&& request.getParameter("state").equals(session.getAttribute("state"))) {

			session.removeAttribute("state");
			String authCode = request.getParameter("code");
			Map<String, Object> userIdentity = getAccessToken(authCode, session);
			String accessToken = (String)userIdentity.get("ACCESS_TOKEN");
			if("error".equalsIgnoreCase(accessToken)) {
				return "error";
			} else {
				request.setAttribute("messages", getGmailMessages(accessToken, (String)session.getAttribute("USER")));
				request.setAttribute("user", userIdentity);
			}
		} else {
			return "error";
		}
		return "dashboard";
	}
	
	@RequestMapping(value ="/gmailListener", method = RequestMethod.GET, produces="text/event-stream")
	@ResponseBody
	public SSEvent listener(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws Exception {
		MessageBean bean = new MessageBean();
		
		SSEvent se = new SSEvent();
		
		try{
			//content type must be set to text/event-stream
	        //encoding must be set to UTF-8
	        //response.setCharacterEncoding("UTF-8");
			//response . setContentType ( "text/event-stream" ) ; 
			  // response . setCharacterEncoding("UTF-8"); 
			   //response . setStatus ( 200 ) ; 
			   //PrintWriter  out   =   response.getWriter() ; 
			  
	        if (!repo.getMessages().isEmpty()) {
	        	LinkedList<MessageBean> beans = (LinkedList<MessageBean>)repo.getMessages();
	        	se.setData(beans.get(beans.size()-1));
	        }
		} catch (Exception e){
			e.printStackTrace();
		}
		return se;
	}
	
	 @RequestMapping ( value   =   "/test" ,   method   =   RequestMethod . GET )     
	 public   void   sseResponse ( HttpServletResponse  response )   throws Exception{ 
	   response . setContentType ( "text/event-stream" ) ; 
	   response . setCharacterEncoding("UTF-8"); 
	   response . setStatus ( 200 ) ; 
	   PrintWriter  out   =   response.getWriter() ; 
	   out . write ( "data:test1nn" ) ; 
	   out . flush ( ) ; 
	 } 
	
	private List<MessageBean> getGmailMessages(String accessToken, String email) throws Exception{
		authenticator.initialize();
		return authenticator.connectToImap("imap.gmail.com",993, email, accessToken,false);
	}

	/**
	 * Expects an Authentication Code, and makes an authenticated request for the user's profile information
	 * @return JSON formatted user profile information
	 * @param authCode authentication code provided by google
	 */
	public Map<String, Object> getAccessToken(String authCode, HttpSession session) throws Exception {
		System.out.println("auth token: "+authCode);

		final GoogleTokenResponse response = flow.newTokenRequest(authCode).setRedirectUri(callBackUrl).execute();
		String accessToken = response.getAccessToken();
		System.out.println("access token : "+accessToken);
		
		final Credential credential = flow.createAndStoreCredential(response, null);
		final HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(credential);
		// Make an authenticated request
		final GenericUrl url = new GenericUrl(gmailUserInfoUrl);
		final HttpRequest request = requestFactory.buildGetRequest(url);
		request.getHeaders().setContentType("application/json");
		final String jsonIdentity = request.execute().parseAsString();
		System.out.println("json identity : " + jsonIdentity);
		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> map = mapper.readValue(jsonIdentity, Map.class);
		map.put("ACCESS_TOKEN", accessToken);
		session.setAttribute("USER", map.get("email"));
		return map;

	}
	
	/**
	 * Builds a login URL based on client ID, secret, callback URI, and scope 
	 */
	public String buildGmailLoginUrl() {
		
		final GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
		
		return url.setRedirectUri(callBackUrl).setState(stateToken).build();
	}
	
	/**
	 * Generates a secure state token 
	 */
	private void generateStateToken(){
		
		SecureRandom sr = new SecureRandom();
		
		stateToken = "google;"+sr.nextInt();
		
	}
	
}
