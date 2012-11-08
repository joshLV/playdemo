package functional;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.sales.AreaFactory;
import models.job.qingtuan.QTSpider;
import models.sales.*;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Test;
import play.i18n.Messages;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

/**
 * @author likang
 *         Date: 12-11-8
 */
public class QTSpiderTest extends FunctionalTest{
    @Before
    public void setUP(){
        FactoryBoy.deleteAll();
        FactoryBoy.create(Area.class);
        new AreaFactory().createOrFindArea(AreaType.CITY, "000", "全国", null);
        FactoryBoy.create(Supplier.class, "qingtuan");
        FactoryBoy.create(Brand.class);

        final Category category = FactoryBoy.create(Category.class);
        Category subCategory = FactoryBoy.create(Category.class, new BuildCallback<Category>() {
            @Override
            public void build(Category target) {
                target.parentCategory = category;
            }
        });

        Messages.defaults.put("qingtuan.旅游住宿", category.id.toString());
        Messages.defaults.put("qingtuan." + category.id + ".旅游", subCategory.id.toString());

    }
    @Test
    public void testJob(){
        assertEquals(0, Goods.count());

        VirtualFile vf =  VirtualFile.fromRelativePath("test/data/qingtuan.xml");
        String xmlString = vf.contentAsString();
        QTSpider.parseQtXml(xmlString);

        assertEquals(1, Goods.count());//data/qingtuan.xml 中有两个商品 一个创建成功 一个创建不成功(category没有合适的)
    }
}
