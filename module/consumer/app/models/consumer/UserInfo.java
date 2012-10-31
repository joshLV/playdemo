package models.consumer;

import play.db.jpa.Model;

import javax.persistence.*;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users_info")
public class UserInfo extends Model {
    
    private static final long serialVersionUID = 812220609113062L;
    
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public User user;

    @Column(name = "full_name")
    public String fullName;

//    @Mobile
//    public String mobile;

    /**
     * 性别 
     */
    @Column(name = "user_sex")
    public Integer userSex;

    /*出生年月*/
    public String birthday;
    /*电话*/
    public String phone;

    /*婚姻状况*/
    @Column(name = "marry_state")
    public Integer marryState;
    /*职位*/
    public String position;
    /*qq*/
    public String userqq;
    /*薪水*/
    public Integer salary;
    /*职位*/
    public String interest;
    /*行业*/
    public String industry;
    /*其他行业*/
    @Column(name = "other_info")
    public String otherInfo;
    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "bind_mobile_at")
    public Date bindMobileAt;

    @Column(name = "total_points")
    public Long totalPoints;

    public UserInfo(User user) {
        this.user = user;
        this.marryState = 0;
        this.userSex = 0;
        this.createdAt = new Date();
    }

    public UserInfo() {
    }

    /**
     * 更新用户信息
     *
     * @param userInfo 用户信息
     */
    public void update(UserInfo userInfo, String interest) {
        fullName = userInfo.fullName;
        salary = userInfo.salary;
        position = userInfo.position;
        phone = userInfo.phone;
        userqq = userInfo.userqq;
        userSex = userInfo.userSex;
        industry = userInfo.industry;
        birthday = userInfo.birthday;
        this.interest = interest;
        marryState = userInfo.marryState;
        otherInfo = userInfo.otherInfo;
        createdAt = new Date();
        this.save();
    }


    /**
     * 查询用户基本信息
     *
     * @param user 用户ID信息
     * @return 用户基本信息
     */
    public static UserInfo findByUser(User user) {
        List<UserInfo> userInfos = UserInfo.find("byUser", user).fetch();
        UserInfo userInfo = null;
        if (userInfos.size() > 0) {
            userInfo = userInfos.get(0);
        }

        return userInfo;
    }

}
