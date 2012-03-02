package models.accounts;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * User: likang
 */
@Entity
@Table(name = "account_operation")
public class AccountOperation extends Model {
    @ManyToOne
    public Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type")
    public AccountOperationType operationType;

    @Column(name = "created_at")
    public Date createdAt;

    public String remark;
    
    @Column(name = "operator_id")
    public Long operatorId;       //todo 改成运营人员 entity
    
    public AccountOperation(Account account, AccountOperationType operationType, String remark, Long operatorId){
        this.account = account;
        this.operationType = operationType;
        this.remark = remark;
        this.operatorId = operatorId;
        this.createdAt = new Date();
    }
}
