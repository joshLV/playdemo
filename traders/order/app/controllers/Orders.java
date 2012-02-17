package controllers;

import java.util.List;

import models.order.OrderItems;
import play.mvc.Controller;

public class Orders extends Controller {

	/**
	 * 商户订单信息一览
	 */
	public static void index() {
		
		render();
	}

	/**
	 * 券号列表
	 */
	public static void quan() {
		
		render();
	}
}