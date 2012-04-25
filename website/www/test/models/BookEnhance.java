package models;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Version;

import play.db.jpa.Model;

@Entity
@Table(name = "test_book_enhance")
public class BookEnhance extends Model{
    @Version
    public long version;
    
    public Long count;
}
