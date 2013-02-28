package models.sales;

import cache.CacheCallBack;
import cache.CacheHelper;
import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.PathUtil;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.data.validation.MaxSize;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.ModelPaginator;
import play.modules.solr.SolrField;
import play.modules.solr.SolrSearchable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "brands")
@SolrSearchable
public class Brand extends Model {

    private static final long serialVersionUID = 7063232060911301L;
    public static final String IMAGE_TINY = "60x46_nw";
    public static final String IMAGE_SMALL = "172x132";
    public static final String IMAGE_MIDDLE = "234x178";
    public static final String IMAGE_LARGE = "340x260";
    public static final String IMAGE_LOGO = "300x180_nw";
    public static final String IMAGE_SLIDE = "nw";
    public static final String IMAGE_ORIGINAL = "nw";
    public static final String IMAGE_DEFAULT = "";

    @Required
    @MaxSize(20)
    @SolrField
    public String name;

    public String logo;

    @Column(name = "site_display_image")
    public String siteDisplayImage;

    @Required
    @MaxSize(20)
    @SolrField
    public String description;     //品牌描述

    @ManyToOne
    public Supplier supplier;
    @Required
    @Min(0)
    @Column(name = "display_order")
    public Integer displayOrder = 100; //显示次序，默认100
    //    @Time
//    public String workAt;      //营业时间上班时间
//    @Time
//    public String closeAt;     //营业时间下班时间
    @MaxSize(4000)
    @SolrField
    public String introduce;     //特色产品介绍

    @Enumerated(EnumType.ORDINAL)
    @SolrField
    public DeletedStatus deleted;

    @Column(name = "is_hot")
    @SolrField
    public Boolean isHot;       //是否热点品牌

    public Boolean display;     //是否显示

    public static final String CACHEKEY = "BRAND";

    @Override
    public void _save() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        super._save();
    }

    @Override
    public void _delete() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        super._delete();
    }

    @Override
    @SolrField
    public Long getId() {
        return id;
    }

    private static final String IMAGE_SERVER = Play.configuration.getProperty
            ("image.server", "img0.dev.uhcdn.com");

    @Transient
    public String getShowLogo() {
        return PathUtil.getImageUrl(IMAGE_SERVER, logo, IMAGE_LOGO);
    }

    @Transient
    public String getSiteDisplayMiddleImage() {
        return PathUtil.getImageUrl(IMAGE_SERVER, siteDisplayImage, IMAGE_MIDDLE);
    }

    @Transient
    public String getTinyLogo() {
        return PathUtil.getImageUrl(IMAGE_SERVER, logo, IMAGE_TINY);
    }

    @Transient
    public String getOriginalLogo() {
        return PathUtil.getImageUrl(IMAGE_SERVER, logo, IMAGE_ORIGINAL);
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
    public static void update(Long id, Brand brand) {
        Brand oldBrand = Brand.findById(id);
        if (oldBrand == null) {
            return;
        }
        oldBrand.name = brand.name;
        oldBrand.displayOrder = brand.displayOrder;
        oldBrand.introduce = brand.introduce;
        oldBrand.isHot = (brand.isHot == null) ? Boolean.FALSE : brand.isHot;
        oldBrand.display = (brand.display == null) ? Boolean.TRUE : brand.display;

        oldBrand.description = brand.description;
        if (!StringUtils.isEmpty(brand.logo)) {
            oldBrand.logo = brand.logo;
        }
        if (!StringUtils.isEmpty(brand.siteDisplayImage)) {
            oldBrand.siteDisplayImage = brand.siteDisplayImage;
        }
        oldBrand.save();
    }

    public static List<Brand> findTop(int limit) {
        return find("deleted= ? order by displayOrder", DeletedStatus.UN_DELETED).fetch(limit);
    }

    public static List<Brand> findByOrder(Supplier supplier) {
        return findByOrder(supplier, null, true);
    }

    public static List<Brand> findByOrder(Supplier supplier, Long operatorId) {
        return findByOrder(supplier, operatorId, null);
    }

    public static List<Brand> findByOrder(Supplier supplier, Long operatorId, Boolean hasSeeAllSupplierPermission) {
        StringBuilder sq = new StringBuilder("deleted = ?");
        List params = new ArrayList();
        params.add(DeletedStatus.UN_DELETED);
        if (supplier != null) {
            sq.append(" and supplier = ?");
            params.add(supplier);
        }

        if (hasSeeAllSupplierPermission != null && !hasSeeAllSupplierPermission) {
            if (operatorId != null) {
                sq.append(" and supplier.salesId = ?");
                params.add(operatorId);
            }
        }
        return find(sq.toString(), params.toArray()).fetch();
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

    public static ModelPaginator getBrandPage(int pageNumber, int pageSize, Long supplierId, Long brandId) {
        ModelPaginator page;
        StringBuilder sq = new StringBuilder();
        sq.append("deleted = ?");
        List list = new ArrayList();
        list.add(DeletedStatus.UN_DELETED);
        if (supplierId != null && supplierId.longValue() != 0) {
            sq.append("and supplier.id=?");
            list.add(supplierId);
        }
        if (brandId != null && brandId.longValue() != 0) {
            sq.append(" and id=?");
            list.add(brandId);
        }
        page = new ModelPaginator(Brand.class, sq.toString(), list.toArray()).orderBy("displayOrder desc,name");
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }

    public static Brand findBrandById(final Long id) {
        return CacheHelper.getCache(CacheHelper.getCacheKey(Brand.CACHEKEY + id, "BRAND_BY_ID"), new CacheCallBack<Brand>() {
            @Override
            public Brand loadData() {
                return Brand.findById(id);
            }
        });
    }
}
