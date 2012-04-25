package models;

import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name="test_book")
public class Book extends Model{
    public Long count;
}
