package tutoringWebsite.model;

public class user {
	private String email;
	private String password;
	private String genInfo;
	public user(){
	}
	public void setEmail(String email){
		this.email = email;
	}
	public String getEmail() {
		return this.email;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPassword() {
		return this.password;
	}
	public void setGenInfo(String genInfo) {
		this.genInfo = genInfo;
	}
	public String getGenInfo() {
		return this.genInfo;
	}
}
