package models.consumer;

import java.util.*;
import javax.persistence.*;

import play.db.jpa.*;

@Entity
@Table(name = "users")
public class User extends Model {

    public String loginName;

    public String email;

    public String mobile;

    public String openIdSource;

    public String password;

    public String passwordSalt;

    public Date lastLoginAt;

    public int status;

    public String loginIp;
}
