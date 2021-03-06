jQuery(function (f) {
    var a = f("#goodsId").val();
    f("#link_add_cart").click(function (A) {
        A.preventDefault();
        var z = f("#number").val();
        var y = Number(f("#boughtNumber").val());
        var w = Number(f("#addCartNumber").val()) + z * 1;
        var x = Number(f("#limit_" + a).val());
        if (x > 0 && x - y > 0 && w > (x - y)) {
            f(".error").html("<strong style='display: block;'>已经超过限购数量，不能继续加入购物车！</strong>").css("color", "#F55");
            return false
        }
        if (x > 0 && z > (x - y)) {
            z.val(x - y);
            return false
        }
        f.post("/carts", {goodsId:a, increment:f("#number").val()}, function (B) {
            f("#addCartNumber").val(w);
            f("#add_cart_result").show();
            setTimeout("$('#add_cart_result').css('display','none')", 5000);
            f("#result-count").text(B.count);
            f("#result-amount").text(B.amount);
            f("#cart-count").html(B.count);
            f("#reload").val("true");
            f("#cart-js").html(B.count);
            f("#order_confirm").hide()
        })
    });
    f("#link_buy_now").click(function () {
        var w = "${goods.limitNumber}";
        var x = Number(f("#number").val());
        if (w > 0 && x > w) {
            f(".error").html("<strong>只能购买" + w + "个</strong>").css("color", "#F55");
            return false
        }
        f("#order_create_form").submit();
        return false
    });
    f("#J_closeTips").click(function (w) {
        w.preventDefault();
        f("#add_cart_result").hide()
    });
    f("#increase-btn").click(function () {
        g(f(this).attr("name"), 1);
        return false
    });
    f("#decrease-btn").click(function () {
        g(f(this).attr("name"), -1);
        return false
    });
    function g(A, C) {
        var y = f("#number");
        var E = f("#last_num_" + A);
        var B = Number(f("#stock_" + A).val());
        var z = Number(f("#limit_" + A).val());
        var w = Number(E.val());
        var D = w + C;
        var x = Number(f("#boughtNumber").val());
        if (D <= 0) {
            y.val(w);
            return
        }
        if (D > 999) {
            D = 999;
            C = 999 - w
        }
        if (D > B) {
            D = B;
            C = B - w;
            f("#stock_hit").css("display", "inline-block");
            return
        } else {
            f("#stock_hit").css("display", "none")
        }
        if (z > 0 && D > (z - x)) {
            y.val(z - x);
            C = z - w;
            return false
        }
        if (C == 0) {
            y.val(w);
            return
        }
        y.val(D);
        E.val(D)
    }

    f("#gallery").slides({play:5000, pause:2500, slideSpeed:500, hoverPause:true}).hover(function () {
        f("#gallery .btn").show()
    }, function () {
        f("#gallery .btn").hide()
    });
    f("#switch").slides({play:4000, pause:2000, slideSpeed:300, hoverPause:true});
    f("#qq").click(function (w) {
        w.preventDefault();
        f("#share-im").slideToggle(100)
    });
    f("#share-url").click(function () {
        f(this).select()
    });
    var s, p, b = {latlngStr:"", latlng:{}, title:""}, q = 1, u = 5, c, e, t, v = f("#outlet-page"), n = new google.maps.Geocoder();
    if (location.host == "127.0.0.1" || location.host == "192.168.18.242") {
        e = "/zome/home/template/outletList.php";
        t = "/zome/home/template/consult.php"
    } else {
        e = "/goods/" + a + "/shops";
        t = "/goods/" + a + "/questions"
    }
    function o() {
        f.getJSON(e, "currPage=" + q + "&pageSize=" + u, function (w) {
            if (w.status == 0) {
                j(w.outletList);
                c = Math.ceil(w.totalOutlet / 5);
                f("#outlet-total-num").text(w.totalOutlet)
            } else {
                f("#outlet").hide()
            }
            if (c > 1) {
                v.html('<a class="next-page" href="#">下一页</a>')
            }
        })
    }

    o();
    function r(C, y, B, A) {
        var x = {zoom:y, center:B, zoomControl:true, panControl:false, scaleControl:false, overviewMapControl:false, streetViewControl:false, mapTypeControl:false, mapTypeId:google.maps.MapTypeId.ROADMAP}, z = new google.maps.Map(document.getElementById(C), x), w = new google.maps.Marker({position:B, map:z, animation:google.maps.Animation.DROP, title:A});
        return{map:z, marker:w}
    }

    function d(y, w, x) {
        b.latlngStr = y;
        b.latlng = new google.maps.LatLng(w[0], w[1]);
        b.title = x;
        if (s === undefined) {
            s = r("min_gmap", 13, b.latlng, b.title)
        }
        s.map.setCenter(b.latlng);
        s.marker.setMap(null);
        s.marker = new google.maps.Marker({position:b.latlng, map:s.map, animation:google.maps.Animation.DROP, title:b.title})
    }

    f(".outlet-name").live("click", function () {
        var w = f(this);
        f(".outlet-name").removeClass("outlet-show");
        w.addClass("outlet-show");
        f(".outlet-attr:visible").slideUp(100);
        w.siblings().slideToggle(100);
        var x = w.attr("data-latlng");
        if (x != "") {
            arr = x.split(",");
            d(x, arr, w.text())
        } else {
            n.geocode({address:w.attr("data-addr")}, function (A, z) {
                if (z == google.maps.GeocoderStatus.OK) {
                    var y = A[0].geometry.location;
                    x = y.lat() + "," + y.lng();
                    arr = x.split(",")
                } else {
                    x = "31.001197278248362,122.25685396265624";
                    arr = [31.001197278248362, 122.25685396265624]
                }
                w.attr("data-latlng", x);
                d(x, arr, w.text())
            })
        }
    });
    function m(x) {
        var w = "";
        for (i in x) {
            w += '<li><h5 class="outlet-name" data-addr="' + x[i].addr + '" data-latlng="' + x[i].latlng + '">' + x[i].name + '</h5><div class="outlet-attr">    <p>' + x[i].addr + "</p>    <p><span>" + x[i].tel + '</span> <a class="view-map" href="#">查看地图»</a> <a class="search-path" href="#">公交/驾车»</a></p></div></li>'
        }
        f(".outlet-list ul").html(w);
        f(".outlet-attr:first").show();
        f(".outlet-name:first").addClass("outlet-show")
    }

    function j(x) {
        var y, w;
        if (x[0]["latlng"] != "") {
            y = x[0]["latlng"];
            w = y.split(",");
            m(x);
            d(y, w, x[0]["name"])
        } else {
            n.geocode({address:x[0]["addr"]}, function (B, A) {
                if (A == google.maps.GeocoderStatus.OK) {
                    var z = B[0].geometry.location;
                    y = z.lat() + "," + z.lng();
                    w = y.split(",")
                } else {
                    y = "31.001197278248362,122.25685396265624";
                    w = [31.001197278248362, 122.25685396265624]
                }
                x[0]["latlng"] = y;
                m(x);
                d(y, w, x[0]["name"])
            })
        }
    }

    f(".view-map").live("click", function (w) {
        w.preventDefault();
        if (f("#map_mask").length == 0) {
            f("body").append('<div id="map_mask"></div>');
            f("#map_mask").css({width:f(window).width(), height:f("body").height()})
        } else {
            f("#map_mask").show()
        }
        if (f("#map_box").length == 0) {
            f("body").append('<div id="map_box"><a class="close" href="javascript:void(0)" hidefocus="true"></a><h3>' + b.title + '</h3><div id="big_gmap" style="width:800px;height:500px;"></div><p>提醒：地图标注位置仅供参考，具体情况以实际道路标识信息为准</p></div>');
            f("#map_box .close").click(function () {
                f("#map_box").hide();
                f("#map_mask").hide()
            })
        } else {
            f("#map_box").show();
            f("#map_box h3").text(b.title)
        }
        f("#map_box").css({top:f(window).height() / 2 - 284 + f(document).scrollTop() + "px", left:f(window).width() / 2 - 413 + "px"});
        if (p == undefined) {
            p = r("big_gmap", 15, b.latlng, b.title)
        }
        p.map.setCenter(b.latlng);
        p.marker.setMap(null);
        p.marker = new google.maps.Marker({position:b.latlng, map:p.map, animation:google.maps.Animation.DROP, title:b.title})
    });
    f(".search-path").live("click", function (w) {
        w.preventDefault();
        if (f("#map_mask").length == 0) {
            f("body").append('<div id="map_mask"></div>');
            f("#map_mask").css({width:f(window).width(), height:f("body").height()})
        } else {
            f("#map_mask").show()
        }
        if (f("#map_search").length == 0) {
            f("body").append('<div id="map_search"><a class="close" href="javascript:void(0)" hidefocus="true"></a><h3>查询路线</h3><form action="http://ditu.google.cn/maps" method="get" target="_blank"><ul><li><span class="text">目的地</span> <span id="daddr-txt">' + b.title + '</span><input type="hidden" id="daddr-val" name="daddr" value="' + b.latlngStr + '"></li><li><span class="text">出行方式</span> <input type="radio" name="dirflg" checked value="r">公交 <input type="radio" name="dirflg" value="d">驾车</li><li><span class="text">出发地</span> <input type="text" name="saddr" class="saddr"></li><li><button type="submit" class="btn">查询</button></li></ul></form></div>');
            f("#map_search .close").click(function () {
                f("#map_mask").hide();
                f("#map_search").hide()
            })
        } else {
            f("#map_search").show();
            f("#daddr-txt").text(b.title);
            f("#daddr-val").val(b.latlngStr)
        }
        f("#map_search").css({top:f(window).height() / 2 - 100 + f(document).scrollTop() + "px", left:f(window).width() / 2 - 183 + "px"})
    });
    f("#outlet-page").delegate("a", "click", function (w) {
        w.preventDefault();
        if (f(this).hasClass("next-page")) {
            q++;
            f.getJSON(e, "currPage=" + q + "&pageSize=" + u, function (x) {
                j(x.outletList);
                if (c <= 2) {
                    v.html('<a class="prev-page" href="#">上一页</a>')
                } else {
                    if (q == c) {
                        v.html('<a class="prev-page" href="#">上一页</a>')
                    } else {
                        v.html('<a class="prev-page" href="#">上一页</a> | <a class="next-page" href="#">下一页</a>')
                    }
                }
            })
        }
        if (f(this).hasClass("prev-page")) {
            q--;
            f.getJSON(e, "currPage=" + q + "&pageSize=" + u, function (x) {
                j(x.outletList);
                if (q == 1) {
                    v.html('<a class="next-page" href="#">下一页</a>')
                } else {
                    v.html('<a class="prev-page" href="#">上一页</a> | <a class="next-page" href="#">下一页</a>')
                }
            })
        }
    });
    var l = {currPage:1, pageSize:5};

    function k(z, y) {
        var x = "";
        if (z > 1) {
            x += '<a class="prev" href="javascript:void(0)" data-page="' + (z - 1) + '"><i></i>上一页</a>'
        } else {
            x += '<span class="prev"><i></i>上一页</span>'
        }
        if (y <= 10) {
            for (var w = 1; w <= y; w++) {
                if (z == w) {
                    x += '<span class="curr">' + w + "</span>"
                } else {
                    x += '<a href="javascript:void(0)" data-page="' + w + '">' + w + "</a>"
                }
            }
        } else {
            if (z < 4) {
                for (var w = 1; w < z; w++) {
                    x += '<a href="javascript:void(0)" data-page="' + w + '">' + w + "</a>"
                }
                x += '<span class="curr">' + z + "</span>";
                x += '<a href="javascript:void(0)" data-page="' + (Number(z) + 1) + '">' + (Number(z) + 1) + "</a>";
                x += '<a href="javascript:void(0)" data-page="' + (Number(z) + 2) + '">' + (Number(z) + 2) + "</a>";
                x += '<span class="omit">...</span>';
                x += '<a href="javascript:void(0)" data-page="' + y + '">' + y + "</a>"
            }
            if (z >= 4 && (z <= y - 3)) {
                x += '<a href="javascript:void(0)" data-page="1">1</a>';
                x += '<span class="omit">...</span>';
                x += '<a href="javascript:void(0)" data-page="' + (Number(z) - 2) + '">' + (Number(z) - 2) + "</a>";
                x += '<a href="javascript:void(0)" data-page="' + (Number(z) - 1) + '">' + (Number(z) - 1) + "</a>";
                x += '<span class="curr">' + z + "</span>";
                x += '<a href="javascript:void(0)" data-page="' + (Number(z) + 1) + '">' + (Number(z) + 1) + "</a>";
                x += '<a href="javascript:void(0)" data-page="' + (Number(z) + 2) + '">' + (Number(z) + 2) + "</a>";
                x += '<span class="omit">...</span>';
                x += '<a href="javascript:void(0)" data-page="' + y + '">' + y + "</a>"
            }
            if (z > y - 3) {
                x += '<a href="javascript:void(0)" data-page="1">1</a>';
                x += '<span class="omit">...</span>';
                x += '<a href="javascript:void(0)" data-page="' + (Number(z) - 2) + '">' + (Number(z) - 2) + "</a>";
                x += '<a href="javascript:void(0)" data-page="' + (Number(z) - 1) + '">' + (Number(z) - 1) + "</a>";
                x += '<span class="curr">' + z + "</span>";
                for (var w = z + 1; w <= y; w++) {
                    x += '<a href="javascript:void(0)" data-page="' + w + '">' + w + "</a>"
                }
            }
        }
        if (z < y) {
            x += '<a class="next" href="javascript:void(0)" data-page="' + (Number(z) + 1) + '"><i></i>下一页</a>'
        } else {
            x += '<span class="next"><i></i>下一页</span>'
        }
        return x
    }

    function h() {
        f.getJSON(t, "currPage=" + l.currPage + "&pageSize=" + l.pageSize, function (y) {
            var x = y.list, w = "";
            l.pageCount = Math.ceil(y.total / l.pageSize);
            if (x.length > 0) {
                for (i in x) {
                    w += '<li><dl class="question"><dt>咨询内容：</dt><dd>' + x[i].question + '<span class="date">' + x[i].qdate + '</span></dd></dl><dl class="answer"><dt>客服回复：</dt><dd>' + x[i].answer + '<span class="date">' + x[i].adate + "</span></dd></dl></li>"
                }
                f(".consult-list").html(w);
                f("#consult .pagination").html(k(l.currPage, l.pageCount))
            }
        })
    }

    f("#consult .pagination").delegate("a", "click", function (w) {
        w.preventDefault();
        l.currPage = f(this).attr("data-page");
        h()
    });
    h();
    f("#submit").click(function (B) {
        B.preventDefault();
        var y = f("#question").val(), w = f("#mobile"), A = w.length == 1 ? w.val() : "", x = f("#consult-form .error"), C, z = function (D) {
            x.html(D).show();
            clearTimeout(C);
            C = setTimeout(function () {
                x.hide()
            }, 3000)
        };
        if (y == "") {
            z("请输入咨询的问题");
            return
        }
        if (A != "" && !(/^(1\d{10})$/i).test(A)) {
            z("请输入正确的手机号");
            return
        }
        f.get("/user-question", {content:y, mobile:A, goodsId:a}, function (D) {
            if (D.error == "") {
                z("您的咨询发表成功，请耐心等待回复，谢谢")
            } else {
                z(D.error)
            }
        })
    });
    (new GoTop()).init({pageWidth:960, nodeId:"go-top", nodeWidth:24, distanceToBottom:100, distanceToPage:10, hideRegionHeight:130, text:""})
});