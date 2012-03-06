package models.accounts;

import models.accounts.util.SerialNumberUtil;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 凭证明细
 *
 * User: likang
 * Date: 12-3-5
 */
@Entity
@Table(name = "certificate_detail")
public class CertificateDetail extends Model {

    @Column(name = "serial_number")
    public String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    public CertificateType certificateType;     //凭证类型

    public BigDecimal amount;                   //发生的金额

    @Column(name = "trade_id")
    public Long tradeId;                        //关联的交易ID  [充值、支付、提现等]

    public String summary;                      //摘要

    @Column(name = "created_at")
    public Date createdAt;
    
    public CertificateDetail(CertificateType type, BigDecimal amount, Long tradeId, String summary){
        this.certificateType = type;
        this.amount = amount;
        this.tradeId = tradeId;
        this.summary = summary;
        this.createdAt = new Date();
        this.serialNumber = SerialNumberUtil.generateSerialNumber(this.createdAt);
    }
    
}
