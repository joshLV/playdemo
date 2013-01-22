package models.resale;

import models.order.OuterOrderPartner;
import org.hibernate.annotations.Index;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * @author likang
 *         Date: 13-1-11
 */
@Entity
@Table(name = "resaler_product_journal")
public class ResalerProductJournal extends Model {
    @Column(name = "created_at")
    public Date createdAt;

    @Lob
    @Column(name = "json_data")
    public String jsonData;

    @Column(name = "operator_id")
    public Long operatorId;

    @Transient
    public String operator;

    @ManyToOne
    public ResalerProduct product;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    public ResalerProductJournalType type;

    @Column(name = "remark")
    public String remark;

    public ResalerProductJournal() {
        createdAt = new Date();
    }

    public static ResalerProductJournal createJournal(ResalerProduct product, Long operatorId, String jsonData,
                                                      ResalerProductJournalType type, String remark) {
        ResalerProductJournal journal = new ResalerProductJournal();
        journal.product = product;
        journal.operatorId = operatorId;
        journal.jsonData = jsonData;
        journal.type = type;
        journal.remark = remark;
        return journal.save();
    }
}
