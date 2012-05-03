package models.mail;

import java.io.Serializable;

public class MailMessage {

	private String email;
	private String fullName;

	public MailMessage() {
		super();
	}

	public String getEmail() {
	    return email;
	}

	public void setEmail(String email) {
	    this.email = email;
	}

	public String getFullName() {
	    return fullName;
	}

	public void setFullName(String fullName) {
	    this.fullName = fullName;
	}

}