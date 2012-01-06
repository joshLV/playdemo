package models;

import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name="cusers")
public class Cusers extends Model{

	public long company_id;
	/**登录帐号**/
	public String login_name;
	/**手机**/
	public String mobile;
	/**密码**/
	public String password;
	/**随机种子**/
	public String randnum;
	/**登录时间点**/
	public int last_login_at;
	/**登录状态**/
	public int status;
	/**登录人的ip**/
	public String login_ip;
	
	public long getCompany_id() {
		return company_id;
	}
	public void setCompany_id(long company_id) {
		this.company_id = company_id;
	}
	public String getLogin_name() {
		return login_name;
	}
	public void setLogin_name(String login_name) {
		this.login_name = login_name;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getRandnum() {
		return randnum;
	}
	public void setRandnum(String randnum) {
		this.randnum = randnum;
	}
	public int getLast_login_at() {
		return last_login_at;
	}
	public void setLast_login_at(int last_login_at) {
		this.last_login_at = last_login_at;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getLogin_ip() {
		return login_ip;
	}
	public void setLogin_ip(String login_ip) {
		this.login_ip = login_ip;
	}
	
}
