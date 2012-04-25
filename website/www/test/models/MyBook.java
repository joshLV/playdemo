package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "test_my_book")
public class MyBook extends OptimisticLockingModel {
    
    public Long count;
    
}
