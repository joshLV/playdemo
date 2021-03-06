package controllers;

import com.uhuila.common.util.FileUploadUtil;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.WithdrawAccount;
import models.accounts.util.AccountUtil;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.admin.SupplierUserType;
import models.operator.OperateUser;
import models.operator.Operator;
import models.order.Prepayment;
import models.resale.Resaler;
import models.resale.ResalerStatus;
import models.sales.Shop;
import models.supplier.Supplier;
import models.supplier.SupplierCategory;
import models.supplier.SupplierStatus;
import operate.rbac.ContextedPermission;
import operate.rbac.annotations.ActiveNavigation;
import operate.rbac.annotations.Right;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import play.Play;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 商户管理的控制器.
 * <p/>
 * User: sujie
 * Date: 3/20/12
 * Time: 3:13 PM
 */
@With(OperateRbac.class)
@ActiveNavigation("suppliers_index")
public class Suppliers extends Controller {
    private static final String ADMIN_ROLE = "admin";
    private static final String SALES_ROLE = "sales";
    public static final String SUPPLIER_BASE_DOMAIN = Play.configuration.getProperty("application.supplierDomain");
    public static final String BASE_URL = Play.configuration.getProperty("application.baseUrl", "");

    public static void index(Long supplierId, String code, String domainName, String keyword) {
        int page = getPage();

//        String otherName = request.params.get("otherName");
//        String code = request.params.get("code");
        Long operatorId = OperateRbac.currentUser().id;
        List<Supplier> suppliers = Supplier.findByCondition(supplierId, code, domainName, keyword);
        render(suppliers, page, supplierId, code, domainName, keyword, operatorId);
    }

    @ActiveNavigation("suppliers_add")
    public static void add() {
        List<OperateUser> operateUserList = OperateUser.getSales(SALES_ROLE);
        renderArgs.put("baseDomain", SUPPLIER_BASE_DOMAIN);
        List<SupplierCategory> supplierCategoryList = SupplierCategory.findAll();
        renderArgs.put("sellECoupon", "1");
        List<Resaler> resalerList = Resaler.find("operator !=? and status=?", Operator.defaultOperator(), ResalerStatus.APPROVED).fetch();
        render(operateUserList, supplierCategoryList, resalerList);
    }

    private static int getPage() {
        String page = request.params.get("page");
        if (StringUtils.isNotEmpty(page) && (page.contains("?x-http-method-override=PUT") || page.contains("x-http-method-override=PUT"))) {
            page = page.replace("x-http-method-override=PUT", "").replace("?", "");
        }
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        return pageNumber;
    }

    /**
     * 创建商户，同时创建商户的一个管理员.
     *
     * @param supplier
     * @param image
     * @param admin
     */
    @ActiveNavigation("suppliers_add")
    public static void create(@Valid Supplier supplier, File image, @Valid SupplierUser admin) {
        checkImage(image);
        initAdmin(admin);

        checkItems(supplier);
        //随机产生6位数字密码
        String password = "123456";
        admin.encryptedPassword = password;
        admin.confirmPassword = password;

        Validation.match("validation.jobNumber", admin.jobNumber, "^[0-9]*");

        if (Validation.hasErrors()) {
            List<OperateUser> operateUserList = OperateUser.getSales(SALES_ROLE);
            renderArgs.put("baseDomain", SUPPLIER_BASE_DOMAIN);
            List<SupplierCategory> supplierCategoryList = SupplierCategory.findAll();
            renderProperties();
            render("Suppliers/add.html", supplier, operateUserList, supplierCategoryList);
        }
        supplier.loginName = admin.loginName;
        supplier.create();
        try {
            supplier.logo = uploadImagePath(image, supplier.id);
            supplier.save();
        } catch (IOException e) {
            error("supplier.image_upload_failed");
        }
        admin.create(supplier.id);

        //添加商户属性
        setSupplierProperty(supplier.id);

        // 确保创建商户Account，以避免并发时产生2个accounts
        AccountUtil.getSupplierAccount(supplier.id, supplier.defaultOperator());

        //发送密码给商户管理员手机
//        String comment = Play.configuration.getProperty("message.comment", "【券市场】 恭喜您已开通券市场账号，用户名：username，密码：password。（请及时修改密码）客服热线：4006865151");
//        comment = comment.replace("username", admin.loginName);
//        comment = comment.replace("password", password);
//        SMSUtil.send(comment, admin.mobile, "0000");
        index(null, null, null, null);
    }

    /**
     * 设置商户属性
     */
    private static void setSupplierProperty(Long id) {
        Supplier supplier = Supplier.findById(id);
        supplier.setProperty(Supplier.CAN_SALE_REAL, request.params.get(Supplier.CAN_SALE_REAL));
        supplier.setProperty(Supplier.SELL_ECOUPON, request.params.get(Supplier.SELL_ECOUPON));
        supplier.setProperty(Supplier.KTV_SUPPLIER, request.params.get(Supplier.KTV_SUPPLIER));
        supplier.setProperty(Supplier.KTV_SKU_PUSH_END_HOUR, request.params.get(Supplier.KTV_SKU_PUSH_END_HOUR));
        supplier.setProperty(Supplier.IS_WITHDRAW_DELAY, request.params.get(Supplier.IS_WITHDRAW_DELAY));
        supplier.setProperty(Supplier.PROPERTY_GIFT_CARD, request.params.get(Supplier.PROPERTY_GIFT_CARD));
        supplier.setProperty(Supplier.FREEZE_AMOUNT, request.params.get(Supplier.FREEZE_AMOUNT));
        supplier.setProperty(Supplier.SET_WITHDRAW_AMOUNT, request.params.get(Supplier.SET_WITHDRAW_AMOUNT));
        supplier.setProperty(Supplier.MEI_TUAN, request.params.get(Supplier.MEI_TUAN));
        supplier.setProperty(Supplier.DIAN_PING, request.params.get(Supplier.DIAN_PING));
        supplier.setProperty(Supplier.NUOMI, request.params.get(Supplier.NUOMI));
        supplier.setProperty(Supplier.LASHOU, request.params.get(Supplier.LASHOU));
        supplier.setProperty(Supplier.SET_RESERVE_AMOUNT, request.params.get(Supplier.SET_RESERVE_AMOUNT));
        supplier.setProperty(Supplier.SET_LESS_WITHDRAW_AMOUNT, request.params.get(Supplier.SET_LESS_WITHDRAW_AMOUNT));
    }

    private static void redirectUrl(int page) {
        if (Play.mode.isDev()) {
            redirect("http://localhost:9303/" + "suppliers?page=" + page);
        } else {
            redirect(BASE_URL + "/suppliers?page=" + page);
        }
    }

    /**
     * 验证手机和电话
     *
     * @param supplier
     */
    private static void checkItems(Supplier supplier) {
        if (StringUtils.isEmpty(supplier.mobile) && StringUtils.isEmpty(supplier.phone)) {
            Validation.addError("supplier.mobile", "validation.lessOne");
        }
        if (supplier.domainName != null) {
            if (Supplier.existDomainName(supplier.domainName)) {
                Validation.addError("supplier.domainName", "validation.existed");
            }
        }
    }

    private static void initAdmin(SupplierUser admin) {
        admin.roles = new ArrayList<>();
        admin.roles.add(SupplierRole.findByKey(ADMIN_ROLE));
        admin.roles.add(SupplierRole.findByKey(SALES_ROLE));
    }

    private static void checkImage(File image) {
        if (image == null) {
            return;
        }
        //检查目录
        File uploadDir = new File(OperateUploadFiles.ROOT_PATH);
        if (!uploadDir.isDirectory()) {
            Validation.addError("supplier.image", "validation.write");
        }

        //检查目录写权限
        if (!uploadDir.canWrite()) {
            Validation.addError("supplier.image", "validation.write");
        }

        if (image.length() > OperateUploadFiles.MAX_SIZE) {
            Validation.addError("supplier.image", "validation.maxFileSize");
        }

        //检查扩展名
        //定义允许上传的文件扩展名
        String[] fileTypes = OperateUploadFiles.FILE_TYPES.trim().split(",");
        String fileExt = image.getName().substring(image.getName().lastIndexOf(".") + 1).toLowerCase();
        if (!Arrays.<String>asList(fileTypes).contains(fileExt)) {
            Validation.addError("supplier.image", "validation.invalidType", StringUtils.join(fileTypes, ','));
        }
    }

    /**
     * 上传图片
     *
     * @param uploadImageFile
     * @param supplierId
     */
    private static String uploadImagePath(File uploadImageFile, Long supplierId) throws IOException {
        if (uploadImageFile == null || uploadImageFile.getName() == null) {
            return "";
        }
        //取得文件存储路径

        String absolutePath = FileUploadUtil.storeImage(uploadImageFile, supplierId, OperateUploadFiles.ROOT_PATH);
        return absolutePath.substring(OperateUploadFiles.ROOT_PATH.length(), absolutePath.length());
    }

    /**
     * 编辑门店页面展示
     *
     * @param id 门店标识
     */
    @Right("SUPPLIERS_MANAGE")
    @ActiveNavigation("suppliers_index")
    public static void edit(long id) {
        int page = getPage();
        Supplier supplier = Supplier.findById(id);
        SupplierUser admin = SupplierUser.findAdmin(id, supplier.loginName);
        List<WithdrawAccount> withdrawAccounts =
                WithdrawAccount.findAllBySupplier(supplier.getId());
        List<OperateUser> operateUserList = OperateUser.getSales(SALES_ROLE);
        List<SupplierCategory> supplierCategoryList = SupplierCategory.findAll();
        renderArgs.put("baseDomain", SUPPLIER_BASE_DOMAIN);

        List<Shop> independentShopList = Shop.findIndependentList(supplier.id);
        Boolean hasSupplierCodeEditPermission = ContextedPermission.hasPermission("SUPPLIER_CODE_EDIT");

        List<Resaler> resalerList = Resaler.find("operator !=? and status=?", Operator.defaultOperator(), ResalerStatus.APPROVED).fetch();
        renderArgs.put("resalerList", resalerList);
//        List<Long> resalerIds = getResalerIds(supplier.resalers);
//        renderArgs.put("resalerIds", resalerIds);

        render(supplier, supplierCategoryList, independentShopList, hasSupplierCodeEditPermission, admin, id, withdrawAccounts, operateUserList, page);
    }

    @Right("SALES_SET_SUPPLIER_WITHDRAW_AMOUNT")
    @ActiveNavigation("suppliers_index")
    public static void setWithdrawAmount(long id) {
        Supplier supplier = Supplier.findById(id);
        Account account = Account.find("uid = ? and accountType = ?", id, AccountType.SUPPLIER).first();
        Date date = DateUtils.truncate(new Date(), Calendar.DATE);
        //余额
        BigDecimal withdrawAmount = account.getWithdrawAmount(date);
        //可提现余额
        BigDecimal prepaymentBalance = Prepayment.findAmountBySupplier(supplier);
        BigDecimal supplierWithdrawAmount = account.getSupplierWithdrawAmount(prepaymentBalance, date);
        BigDecimal supplierAmount = account.amount;
        //预留金
        String reserveAmount = supplier.getProperty(Supplier.SET_RESERVE_AMOUNT);
        BigDecimal reserveAmountToBigDecimal = StringUtils.isBlank(reserveAmount) ? BigDecimal.ZERO : new BigDecimal(reserveAmount);
        supplierWithdrawAmount = supplierWithdrawAmount.subtract(reserveAmountToBigDecimal);

        String lessWithdrawAmount = supplier.getProperty(Supplier.SET_LESS_WITHDRAW_AMOUNT);
        BigDecimal lessWithdrawAmountToBigDecimal = StringUtils.isBlank(lessWithdrawAmount) ? BigDecimal.ZERO : new BigDecimal(lessWithdrawAmount);

        render(supplier, withdrawAmount, supplierWithdrawAmount, supplierAmount, reserveAmountToBigDecimal,lessWithdrawAmountToBigDecimal);
    }


    public static void confirmSetWithdrawAmount(Supplier supplier) {
        supplier.save();
        index(null, null, null, null);
    }

    private static List<Long> getResalerIds(List<Resaler> resalers) {
        List<Long> resalerIds = new ArrayList<>();
        if (resalers != null && !resalers.isEmpty()) {
            for (Resaler resaler : resalers) {
                if (resalerIds != null) {
                    resalerIds.add(resaler.id);
                }
            }
        }
        return resalerIds;
    }

    public static void withdrawAccountCreateAndUpdate(@Valid WithdrawAccount withdrawAccount, Long supplierId) {
        if (Validation.hasErrors()) {
            renderArgs.put("withdrawAccount", withdrawAccount);
            Validation.keep();
            edit(supplierId);
        }
        Supplier supplier = Supplier.findById(supplierId);
        if (withdrawAccount.shopId == null) {
            withdrawAccount.userId = supplier.getId();
            withdrawAccount.accountType = AccountType.SUPPLIER;
        } else {
            withdrawAccount.userId = withdrawAccount.shopId;
            withdrawAccount.accountType = AccountType.SHOP;
            withdrawAccount.supplierId = supplierId;
        }
        withdrawAccount.save();

        edit(supplierId);
    }

    public static void withdrawAccountDelete(Long id, Long supplierId) {
        WithdrawAccount withdrawAccount = WithdrawAccount.findById(id);
        if (withdrawAccount != null) {
            withdrawAccount.delete();
        }
        edit(supplierId);
    }

    @Right("SUPPLIERS_MANAGE")
    public static void update(Long id, @Valid Supplier supplier, File image) {
        int page = getPage();
        Supplier oldSupplier = Supplier.findById(id);
        if (StringUtils.isNotBlank(supplier.domainName) && !oldSupplier.domainName.equals(supplier.domainName)) {
            checkItems(supplier);
        }
        //Validation.match("validation.jobNumber", admin.jobNumber, "^[0-9]*");

        if (Validation.hasErrors()) {
            List<OperateUser> operateUserList = OperateUser.getSales(SALES_ROLE);
            renderArgs.put("baseDomain", SUPPLIER_BASE_DOMAIN);
            renderProperties();
            render("/Suppliers/edit.html", supplier, id, operateUserList, page);
        }

        Supplier.update(id, supplier);
        Supplier sp = Supplier.findById(id);
        //添加或更新商户属性
        setSupplierProperty(id);

        redirectUrl(page);
    }

    private static void renderProperties() {
        String sellECoupon = request.params.get(Supplier.SELL_ECOUPON);
        String canSaleReal = request.params.get(Supplier.CAN_SALE_REAL);
        String ktvSupplier = request.params.get(Supplier.KTV_SUPPLIER);
        String ktvSkuPushEndHour = request.params.get(Supplier.KTV_SKU_PUSH_END_HOUR);
        String giftCard = request.params.get(Supplier.PROPERTY_GIFT_CARD);
        renderArgs.put("sellECoupon", sellECoupon);
        renderArgs.put("canSaleReal", canSaleReal);
        renderArgs.put("ktvSupplier", ktvSupplier);
        renderArgs.put("ktvSkuPushEndHour", ktvSkuPushEndHour);
        renderArgs.put("giftCard", giftCard);
    }

    public static void updateCode(Long id, Long supplierCategoryId) {
        Supplier supplier = Supplier.findById(id);
        SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
        if (supplier != null && supplierCategory != null && supplier.supplierCategory != null && supplier.supplierCategory.id != supplierCategoryId) {
            supplier.resetCode(supplierCategory);
        }

        render(supplier);
    }

    @Right("SUPPLIERS_MANAGE")
    public static void freeze(long id) {
        Supplier.freeze(id);
        redirectUrl(getPage());
    }

    public static void unfreeze(long id) {
        Supplier.unfreeze(id);
        redirectUrl(getPage());
    }

    @Right("SUPPLIERS_MANAGE")
    public static void delete(long id) {
        Supplier.delete(id);
        index(null, null, null, null);
    }


    @ActiveNavigation("suppliers_index")
    public static void suppliersExcelOut(Long supplierId, String code, String domainName, String keyword) {
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "商户列表_" + System.currentTimeMillis() + ".xls");
        List<Supplier> supplierList = Supplier.findByCondition(supplierId, code, domainName, keyword);
        for (Supplier supplier : supplierList) {
            if (supplier.showSellingState == null || supplier.showSellingState == false) {
                supplier.whetherToShowSellingState = "不允许";
            } else {
                supplier.whetherToShowSellingState = "允许";
            }
            if (supplier.status == SupplierStatus.NORMAL) {
                supplier.statusName = "正常";
            } else if (supplier.status == SupplierStatus.FREEZE) {
                supplier.statusName = "冻结";
            }
            supplier.shopsCount = supplier.getShops().size();
            supplier.brandsCount = supplier.getBrands().size();
            supplier.goodsCount = supplier.getGoods().size();
        }
        render(supplierList);
    }

    public static void exportMaterial(long supplierId, String supplierDomainName) {
        JPAExtPaginator<SupplierUser> supplierUsersPage = SupplierUser
                .getSupplierUserList(SupplierUserType.ANDROID, null, null, null,
                        supplierId, null, 1,
                        1);
        JPAExtPaginator<SupplierUser> supplierUsers = SupplierUser
                .getSupplierUserList(null, null, null,
                        supplierId, null, 1,
                        1);
        String qrCodePath = Play.configuration.getProperty("weixin.qrcode.path");
        for (SupplierUser s : supplierUsers) {
            if (StringUtils.isBlank(s.idCode)) {
                s.idCode = SupplierUser.generateAvailableIdCode();
                s.save();
            }
        }

        render(supplierUsersPage, supplierDomainName, supplierUsers, qrCodePath);
    }


}
