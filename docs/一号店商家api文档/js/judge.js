
KISSY
		.ready(function(C) {
			var C = KISSY, L = C.DOM, K = C.Event;
			var J = L.query(".api-list"), N = L.get(".searchInput"), G = L
					.get("#searchBtn");
			J && C.each(J, function(P) {
				K.on(P, "mouseover", function(Q) {
					L.addClass(this, "hover-list")
				});
				K.on(P, "mouseout", function(Q) {
					L.removeClass(this, "hover-list")
				})
			});
			N && K.on(N, "focus", function(P) {
				L.css(G, "background-position", "0px -73px")
			});
			N && K.on(N, "blur", function(P) {
				L.css(G, "background-position", "0px 0px")
			});
			var O = C.one(".level-one"), B = C.all(".first", O), I = C.all(
					".second", O);
			B
					&& C
							.each(
									B,
									function(S) {
										var R = L.parent(S, "li"), P = C.all(
												".level-two", R), Q = P
												&& C.DOM.hasClass(P,
														"category-hide");
										if (Q) {
											C.one(S).addClass("hasSub")
										}
										C
												.one(S)
												.on(
														"click",
														function() {
															Q = P
																	&& C.DOM
																			.hasClass(
																					P,
																					"category-hide");
															if (Q) {
																C
																		.one(
																				this)
																		.removeClass(
																				"hasSub");
																C
																		.one(
																				this)
																		.addClass(
																				"showSub");
																C.one("ul", R)
																		&& C
																				.one(
																						"ul",
																						R)
																				.removeClass(
																						"category-hide");
																C
																		.one(R)
																		.addClass(
																				"selected")
															} else {
																if (!!C
																		.one(
																				"ul",
																				R)) {
																	C
																			.one(
																					"ul",
																					R)
																			.addClass(
																					"category-hide");
																	C
																			.one(
																					R)
																			.removeClass(
																					"selected");
																	C
																			.one(
																					this)
																			.removeClass(
																					"showSub");
																	C
																			.one(
																					this)
																			.addClass(
																					"hasSub")
																}
															}
														})
									});
			I
					&& C
							.each(
									I,
									function(S) {
										var R = L.parent(S, "li"), P = C.all(
												".level-three", R), Q = P
												&& C.DOM.hasClass(P,
														"category-hide");
										if (Q) {
											C.one(S).addClass("hasSub")
										}
										C
												.one(S)
												.on(
														"click",
														function() {
															Q = P
																	&& C.DOM
																			.hasClass(
																					P,
																					"category-hide");
															if (Q) {
																C
																		.one(
																				this)
																		.removeClass(
																				"hasSub");
																C
																		.one(
																				this)
																		.addClass(
																				"showSub");
																C.one("ul", R)
																		&& C
																				.one(
																						"ul",
																						R)
																				.removeClass(
																						"category-hide");
																C
																		.one(R)
																		.addClass(
																				"selected")
															} else {
																if (!!C
																		.one(
																				"ul",
																				R)) {
																	C
																			.one(
																					"ul",
																					R)
																			.addClass(
																					"category-hide");
																	C
																			.one(
																					R)
																			.removeClass(
																					"selected");
																	C
																			.one(
																					this)
																			.removeClass(
																					"showSub");
																	C
																			.one(
																					this)
																			.addClass(
																					"hasSub")
																}
															}
														})
									});
			var M = C.one("#searchSelect"), A = C.one("#q"), F = C
					.one("#api-search-input");
			M
					&& M
							.on(
									"change",
									function(P) {
										if (C.one(this).val() == "API") {
											window.isDefault = true;
											G && L.css(G, "display", "none");
											A && L.css(A, "display", "none");
											F && L.css(F, "display", "block");
											L
													.val(F,
															"\u8bf7\u70b9\u51fb\u641c\u7d22\u4e0b\u62c9\u63d0\u793a\u5217\u8868\u9879")
										} else {
											G && L.css(G, "display", "block");
											A && L.css(A, "display", "block");
											F && L.css(F, "display", "none")
										}
									});
			F && F.on("focus", function() {
				if (window.isDefault) {
					L.val(F, "")
				}
			});
			var H = L.query("div.APIgory-list");
			if (!H) {
				return
			}
			C.each(H, function(Q) {
				if (L.get("s", Q)) {
					var P = L.attr(L.get("a", Q), "href");
					K.on(Q, "click", function() {
						window.location = P
					})
				}
			});
			var D = L.create("<div>"), E = L.get("#bd");
			L.addClass(D, "backTop");
			L.append(D, "body");
			K.on(D, "click", function() {
				var P = 10;
				setTimeout(function() {
					var Q = document.documentElement.scrollTop;
					window.scrollTo(0, Q - P);
					P *= 1.1;
					if (document.documentElement.scrollTop <= 0) {
						window.scrollTo(0, 0)
					} else {
						setTimeout(arguments.callee, 25)
					}
				}, 25)
			})
		});