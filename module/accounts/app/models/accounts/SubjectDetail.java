package models.accounts;

import models.accounts.util.SerialUtil;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 科目明细
 *
 * User: likang
 * Date: 12-3-5
 */
@Entity
@Table(name = "subject")
public class SubjectDetail extends Model {

    public String serial;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    public SubjectType subjectType;         //科目类型

    @ManyToOne
    @JoinColumn(name = "certificate_detail")
    public CertificateDetail certificateDetail;     //关联的明细

    @Column(name = "debit_amount")
    public BigDecimal debitAmount;          //借记发生额

    @Column(name = "credit_amount")
    public BigDecimal creditAmount;         //贷记发生额

    public String summary;                  //摘要

    @Column(name = "created_at")
    public Date createdAt;

    public SubjectDetail(SubjectType subjectType, CertificateDetail certificateDetail, BigDecimal debitAmount,
                         BigDecimal creditAmount, String summary){

        this.subjectType = subjectType;
        this.certificateDetail = certificateDetail;
        this.debitAmount = debitAmount;
        this.creditAmount = creditAmount;
        this.summary = summary;
        this.createdAt = new Date();
        this.serial = SerialUtil.generateSerialNumber(this.createdAt);
    }

}
