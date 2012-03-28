package models.admin;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "operate_roles")
public class OperateRole extends Model {

    public String text;

    public String key;

    public String description;

    /**
     * 加载版本，使用应用程序加载时间，在处理完成后，删除不是当前loadVersion的记录，以完成同步.
     */
    @Column(name = "load_version")
    public long loadVersion;

    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "operate_users_roles", 
        inverseJoinColumns = @JoinColumn(name= "user_id"), 
        joinColumns = @JoinColumn(name = "role_id"))
    public Set<OperateUser> users;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "operate_permissions_roles", 
        inverseJoinColumns = @JoinColumn(name= "permission_id"), 
        joinColumns = @JoinColumn(name = "role_id"))
    public Set<OperatePermission> permissions;   
    
    public static OperateRole findByKey(String key){
        return OperateRole.find("byKey",key).first();
    }
    
}

