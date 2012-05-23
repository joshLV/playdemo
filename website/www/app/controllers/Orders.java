package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.accounts.AccountType;
import models.consumer.Address;
import models.consumer.User;
import models.order.Cart;
import models.order.NotEnoughInventoryException;
import models.order.Order;
import models.sales.Goods;
import models.sales.MaterialType;
import play.Logger;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static play.Logger.warn;

/**
 * 用户订单确认控制器.
 * <p/>
 * User: sujie
 * Date: 2/14/12
 * Time: 11:31 AM
 */
@With({SecureCAS.class, WebsiteInjector.class})
public class Orders extends Controller {
	/**
	 * 预览订单.
	 *
	 * @param items 选择的商品及数量，格式为 goods1-num1,goods2-num2-.....
	 */
	public static void index(String items) {
		if (items == null) {
			error("no goods specified");
			return;
		}
		User user = SecureCAS.getUser();
		showOrder(items);
		render(user);
	}

	private static void showOrder(String items) {
		//解析提交的商品及数量
		List<Long> goodsIds = new ArrayList<>();
		Map<Long, Integer> itemsMap = new HashMap<>();
		parseItems(items, goodsIds, itemsMap);

		//计算电子商品列表和非电子商品列表
		List<Cart> eCartList = new ArrayList<>();
		BigDecimal eCartAmount = BigDecimal.ZERO;
		List<Cart> rCartList = new ArrayList<>();
		BigDecimal rCartAmount = BigDecimal.ZERO;

		List<models.sales.Goods> goods = models.sales.Goods.findInIdList(goodsIds);
		for (models.sales.Goods g : goods) {
			Integer number = itemsMap.get(g.getId());
			if (g.materialType == models.sales.MaterialType.REAL) {
				rCartList.add(new Cart(g, number));
				rCartAmount = rCartAmount.add(g.salePrice.multiply(new BigDecimal(number.toString())));
			} else if (g.materialType == models.sales.MaterialType.ELECTRONIC) {
				eCartList.add(new Cart(g, number));
				eCartAmount = eCartAmount.add(g.salePrice.multiply(new BigDecimal(number.toString())));
			}
		}

		if (rCartList.size() == 0 && eCartList.size() == 0) {
			error("no goods specified");
			return;
		}

		List<Address> addressList = Address.findByOrder(SecureCAS.getUser());
		Address defaultAddress = Address.findDefault(SecureCAS.getUser());

		//如果有实物商品，加上运费
		if (rCartList.size() > 0) {
			rCartAmount = rCartAmount.add(Order.FREIGHT);
		}
		BigDecimal totalAmount = eCartAmount.add(rCartAmount);
		BigDecimal goodsAmount = rCartList.size() == 0 ? eCartAmount : totalAmount.subtract(Order.FREIGHT);

		renderArgs.put("goodsAmount", goodsAmount);
		renderArgs.put("totalAmount", totalAmount);
		renderArgs.put("addressList", addressList);
		renderArgs.put("address", defaultAddress);
		renderArgs.put("eCartList", eCartList);
		renderArgs.put("eCartAmount", eCartAmount);
		renderArgs.put("rCartList", rCartList);
		renderArgs.put("rCartAmount", rCartAmount);
		renderArgs.put("items", items);
	}

	/**
	 * 提交订单.
	 */
	public static void create(String items, String mobile) {
		//如果订单中有电子券，则必须填写手机号
		Http.Cookie cookie = request.cookies.get("identity");
		String cookieValue = cookie == null ? null : cookie.value;
		User user = SecureCAS.getUser();

		//解析提交的商品及数量
		List<Long> goodsIds = new ArrayList<>();
		Map<Long, Integer> itemsMap = new HashMap<>();
		parseItems(items, goodsIds, itemsMap);
		List<models.sales.Goods> goods = models.sales.Goods.findInIdList(goodsIds);
		boolean containsElectronic = containsMaterialType(goods, MaterialType.ELECTRONIC);
		boolean containsReal = containsMaterialType(goods, MaterialType.REAL);

		//电子券必须校验手机号
		if (containsElectronic) {
			Validation.required("mobile", mobile);
			Validation.match("mobile", mobile, "^1[3|4|5|8][0-9]\\d{4,8}$");
		}

		//实物券必须校验收货地址信息
		Address defaultAddress = null;
		String receiverMobile = "";
		if (containsReal) {
			defaultAddress = Address.findDefault(SecureCAS.getUser());
			if (defaultAddress == null) {
				Validation.addError("address", "validation.required");
			}else {
                receiverMobile = defaultAddress.mobile;
            }
		}


		if (Validation.hasErrors()) {
			for (String key : validation.errorsMap().keySet()) {
				warn("validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
			}

			showOrder(items);
			render("Orders/index.html", user);
		}

		//创建订单
		Order order = Order.createConsumeOrder(user.getId(), AccountType.CONSUMER);
		if (defaultAddress != null) {
			order.setAddress(defaultAddress);
		}
		//添加订单条目
		try {
			for (models.sales.Goods goodsItem : goods) {
				if (goodsItem.materialType == MaterialType.REAL) {
					order.addOrderItem(goodsItem, itemsMap.get(goodsItem.getId()), receiverMobile,
							goodsItem.salePrice, //最终成交价
							goodsItem.getResalerPriceOfUhuila()//一百券作为分销商的成本价
							);
				} else {
					order.addOrderItem(goodsItem, itemsMap.get(goodsItem.getId()), mobile,
							goodsItem.salePrice, //最终成交价
							goodsItem.getResalerPriceOfUhuila()//一百券作为分销商的成本价
							);
				}

			}


		} catch (NotEnoughInventoryException e) {
			//todo 缺少库存
			Logger.error(e, "inventory not enough");
			error("inventory not enough");
		}

		//确认订单
		order.createAndUpdateInventory();
		//删除购物车中相应物品
		Cart.delete(user, cookieValue, goodsIds);

		redirect("/payment_info/" + order.orderNumber);
	}

	private static boolean containsMaterialType(List<Goods> goods, MaterialType type) {
		boolean containsElectronic = false;
		for (Goods good : goods) {
			if (type.equals(good.materialType)) {
				containsElectronic = true;
				break;
			}
		}
		return containsElectronic;
	}

	private static void parseItems(String items, List<Long> goodsIds, Map<Long, Integer> itemsMap) {
		String[] itemSplits = items.split(",");
		for (String split : itemSplits) {
			String[] goodsItem = split.split("-");
			if (goodsItem.length == 2) {
				Integer number = Integer.parseInt(goodsItem[1]);
				if (number > 0) {
					Long goodsId = Long.parseLong(goodsItem[0]);
					goodsIds.add(goodsId);
					itemsMap.put(goodsId, number);
				}
			}
		}
	}
}
