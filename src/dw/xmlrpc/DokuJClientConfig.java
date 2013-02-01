package dw.xmlrpc;

import java.net.MalformedURLException;
import java.net.URL;

public class DokuJClientConfig {
	public static final String defaultUserAgent = "DokuJClient - XmlRpc client for Dokuwiki";

	private URL _url = null;
	URL url(){
		return _url;
	}

	private String _user = null;
	String user(){
		return _user;
	}

	private String _password = null;
	String password(){
		return _password;
	}

	private String _userAgent = null;
	String userAgent(){
		return _userAgent;
	}

	public DokuJClientConfig(String url) throws MalformedURLException{
		this(new URL(url));
	}

	public DokuJClientConfig(URL url){
		_url = url;
	}

	public void setUserAgent(String userAgent){
		_userAgent = userAgent;
	}

	public void setUser(String user, String password){
		_user = user;
		_password = password;
	}
}
