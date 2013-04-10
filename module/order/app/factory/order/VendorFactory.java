package factory.order;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.order.Vendor;

/**
 * 供货商
 * <p/>
 * User: wangjia
 * Date: 13-3-29
 * Time: 上午9:53
 */
public class VendorFactory extends ModelFactory<Vendor> {
    @Override
    public Vendor define() {
        Vendor vendor = new Vendor();
        vendor.name = "Vendor" + FactoryBoy.sequence(Vendor.class);
        vendor.address = "Address" + FactoryBoy.sequence(Vendor.class);
        vendor.phone = "021-647898" + FactoryBoy.sequence(Vendor.class);
        vendor.authorizedRepresentative = "authorizedRepresentative";
        vendor.fax = "021-647898" + FactoryBoy.sequence(Vendor.class);
        vendor.bankName = "bankName";
        vendor.cardNumber = "01" + FactoryBoy.sequence(Vendor.class);
        return vendor;
    }
}
