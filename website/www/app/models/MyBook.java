package models;

import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name = "test_my_book")
public class MyBook extends OptimisticLockingModel {
    public Long count;
}
