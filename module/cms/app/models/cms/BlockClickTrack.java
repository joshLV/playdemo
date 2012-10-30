package models.cms;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name="block_click_tracks")
public class BlockClickTrack extends Model {

    private static final long serialVersionUID = 18232060911893921L;
    
    public static final String MQ_KEY = "WUI2NEW-";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "block_id")
    public Block block;

    @Column(name="cookie_id")
    public String cookieId;

    @Column(name="user_id")
    public Long user_id;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "ip")
    public String ip;

}
