package models.order;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.resale.Resaler;
import org.w3c.dom.Document;
import play.db.jpa.Model;
import play.libs.XML;

import javax.persistence.*;
import java.util.Date;

/**
 * @author likang
 *         Date: 12-9-18
 */
@Entity
@Table(name = "outer_order", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"partner", "order_id"})})        //定义联合唯一约束
public class OuterOrder extends Model {
    @Enumerated(EnumType.STRING)
    @Column(name = "partner")
    public OuterOrderPartner partner;      //合作伙伴

    @Column(name = "order_id", nullable = true)
    public String orderId;       //合作伙伴的订单ID

    @Basic(fetch = FetchType.LAZY)
    @ManyToOne
    @JoinColumn(name = "ybq_order_id")
    public Order ybqOrder;          //一百券的订单

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    public OuterOrderStatus status; //此订单的执行状态

    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(name = "message")
    public String message;          //此订单的完整信息

    @Column(name = "created_at")
    public Date createdAt;          //创建日期

    @Version
    @Column(name = "lock_version")
    public int lockVersion;         //乐观锁
    /**
     * 对应的分销商.
     */
    @ManyToOne
    public Resaler resaler;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type")
    public OuterOrderType orderType; //此订单的产生类型

    @Column(name = "refund_number")
    public Integer refundNumber;

    public OuterOrder() {
        this.createdAt = new Date();
        this.lockVersion = 0;
    }

    @Transient
    public static OuterOrder getOuterOrder(Order ybqOrder) {
        return OuterOrder.find("ybqOrder=?", ybqOrder).first();
    }

    @Transient
    public static OuterOrder getOuterOrder(String orderId, OuterOrderPartner partner) {
        return OuterOrder.find("byOrderIdAndPartner", orderId, partner).first();
    }

    @Transient
    public JsonObject getMessageAsJsonObject() {
        return new JsonParser().parse(message).getAsJsonObject();
    }

    @Transient
    public JsonArray getMessageAsJsonArray() {
        return new JsonParser().parse(message).getAsJsonArray();
    }

    @Transient
    public Document getMessageAsXmlDocument() {
        return XML.getDocument(message);
    }
}
