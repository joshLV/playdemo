package models.sales;

import com.uhuila.common.constants.ImageSize;
import com.uhuila.common.util.PathUtil;
import models.supplier.Supplier;
import play.Play;
import play.data.validation.MaxSize;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.ModelPaginator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "brands")
public class Brand extends Model {
    @Required
    @MaxSize(20)
    public String name;
    public String logo;
    @ManyToOne
    public Supplier supplier;
    @Required
    @Min(0)
    @Column(name = "display_order")
    public int displayOrder = 100; //显示次序，默认100
    //    @Time
//    public String workAt;      //营业时间上班时间
//    @Time
//    public String closeAt;     //营业时间下班时间
    @MaxSize(500)
    public String introduce;   //特色产品介绍

    private static final String IMAGE_SERVER = Play.configuration.getProperty
            ("image.server", "img0.uhcdn.com");

    @Transient
    public String getShowLogo() {
        return PathUtil.getImageUrl(IMAGE_SERVER, logo, ImageSize.LOGO);
    }

    @Transient
    public String getTinyLogo() {
        return PathUtil.getImageUrl(IMAGE_SERVER, logo, ImageSize.TINY);
    }

    @Transient
    public String getOriginalLogo() {
        return PathUtil.getImageUrl(IMAGE_SERVER, logo, ImageSize.ORIGINAL);
    }
/*
    @Transient
    @MaxSize(2)
    public String workAtHour;
    @Transient
    @MaxSize(2)
    public String workAtMin;
    @Transient
    @MaxSize(2)
    public String closeAtHour;
    @Transient
    @MaxSize(2)
    public String closeAtMin;

    public void setWorkAt(String workAt) {
        this.workAt = workAt;
        String[] workAtParts = workAt.split(":");
        if (workAtParts != null && workAtParts.length == 2) {
            workAtHour = workAtParts[0];
            workAtMin = workAtParts[1];
        }
    }

    public void setCloseAt(String closeAt) {
        this.closeAt = closeAt;
        String[] closeAtParts = closeAt.split(":");
        if (closeAtParts != null && closeAtParts.length == 2) {
            closeAtHour = closeAtParts[0];
            closeAtMin = closeAtParts[1];
        }
    }

    @Override
    public boolean create() {
        this.workAt = workAtHour + ":" + workAtMin;
        this.closeAt = closeAtHour + ":" + closeAtMin;
        return super.create();
    }
*/

    public static List<Brand> findTop(int limit) {
        return find("order by displayOrder").fetch(limit);
    }

    public static List<Brand> findByOrder() {
        return find("order by displayOrder").fetch();
    }

    public static List<Brand> findTop(int limit, long brandId) {
        List<Brand> brands = findTop(limit);
        if (brandId != 0) {
            boolean containsBrands = false;
            for (Brand brand : brands) {
                if (brand.id == brandId) {
                    containsBrands = true;
                    break;
                }
            }
            if (!containsBrands) {
                List<Brand> showBrands = new ArrayList<>();
                showBrands.add((Brand) findById(brandId));
                if (brands.size() == limit) {
                    brands.remove(limit - 1);
                }
                showBrands.addAll(brands);
                brands = showBrands;
            }
        }
        return brands;
    }

    public static ModelPaginator getBrandPage(int pageNumber, int pageSize, Long supplierId) {
        ModelPaginator page;
        if (supplierId != null) {
            page = new ModelPaginator(Brand.class, "supplier.id=?", supplierId).orderBy("displayOrder,name");
        } else {
            page = new ModelPaginator(Brand.class).orderBy("displayOrder,name");
        }
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }
}
