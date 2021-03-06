jQuery(function (f) {
    var a = f("#goodsId").val();
    f("#link_add_cart").click(function (z) {
        z.preventDefault();
        var y = f("#number").val();
        var x = Number(f("#boughtNumber").val());
        var v = Number(f("#addCartNumber").val()) + y * 1;
        var w = Number(f("#limit_" + a).val());
        if (w > 0 && w - x > 0 && v > (w - x)) {
            f(".error").html("<strong style='display: block;'>已经超过限购数量，不能继续加入购物车！</strong>").css("color", "#F55");
            return false
        }
        if (w > 0 && y > (w - x)) {
            y.val(w - x);
            return false
        }
        f.post("/carts", {goodsId:a, increment:f("#number").val()}, function (A) {
            f("#addCartNumber").val(v);
            f("#add_cart_result").show();
            setTimeout("$('#add_cart_result').css('display','none')", 5000);
            f("#result-count").text(A.count);
            f("#result-amount").text(A.amount);
            f("#cart-count").html(A.count);
            f("#reload").val("true");
            f("#cart-js").html(A.count);
            f("#order_confirm").hide()
        })
    });
    f("#link_buy_now").click(function () {
        var v = "${goods.limitNumber}";
        var w = Number(f("#number").val());
        if (v > 0 && w > v) {
            f(".error").html("<strong>只能购买" + v + "个</strong>").css("color", "#F55");
            return false
        }
        f("#order_create_form").submit();
        return false
    });
    f("#J_closeTips").click(function (v) {
        v.preventDefault();
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
    function g(z, B) {
        var x = f("#number");
        var D = f("#last_num_" + z);
        var A = Number(f("#stock_" + z).val());
        var y = Number(f("#limit_" + z).val());
        var v = Number(D.val());
        var C = v + B;
        var w = Number(f("#boughtNumber").val());
        if (C <= 0) {
            x.val(v);
            return
        }
        if (C > 999) {
            C = 999;
            B = 999 - v
        }
        if (C > A) {
            C = A;
            B = A - v;
            f("#stock_hit").css("display", "inline-block");
            return
        } else {
            f("#stock_hit").css("display", "none")
        }
        if (y > 0 && C > (y - w)) {
            x.val(y - w);
            B = y - v;
            return false
        }
        if (B == 0) {
            x.val(v);
            return
        }
        x.val(C);
        D.val(C)
    }

    f("#gallery").slides({play:5000, pause:2500, slideSpeed:500, hoverPause:true}).hover(function () {
        f("#gallery .btn").show()
    }, function () {
        f("#gallery .btn").hide()
    });
    f("#switch").slides({play:4000, pause:2000, slideSpeed:300, hoverPause:true});
    f("#qq").click(function (v) {
        v.preventDefault();
        f("#share-im").slideToggle(100)
    });
    f("#share-url").click(function () {
        f(this).select()
    });
    var r, o, b = {latlngStr:"", latlng:{}, title:""}, p = 1, t = 5, c, e, s, u = f("#outlet-page"), m = new google.maps.Geocoder();
    if (location.host == "127.0.0.1" || location.host == "192.168.18.242") {
        e = "/zome/home/template/outletList.php";
        s = "/zome/home/template/consult.php"
    } else {
        e = "/goods/" + a + "/shops";
        s = "/goods/" + a + "/questions"
    }
    function n() {
        f.getJSON(e, "currPage=" + p + "&pageSize=" + t, function (v) {
            if (v.status == 0) {
                j(v.outletList);
                c = Math.ceil(v.totalOutlet / 5);
                f("#outlet-total-num").text(v.totalOutlet)
            } else {
                f("#outlet").hide()
            }
            if (c > 1) {
                u.html('<a class="next-page" href="#">下一页</a>')
            }
        })
    }

    n();
    function q(B, x, A, z) {
        var w = {zoom:x, center:A, zoomControl:true, panControl:false, scaleControl:false, overviewMapControl:false, streetViewControl:false, mapTypeControl:false, mapTypeId:google.maps.MapTypeId.ROADMAP}, y = new google.maps.Map(document.getElementById(B), w), v = new google.maps.Marker({position:A, map:y, animation:google.maps.Animation.DROP, title:z});
        return{map:y, marker:v}
    }

    function d(x, v, w) {
        b.latlngStr = x;
        b.latlng = new google.maps.LatLng(v[0], v[1]);
        b.title = w;
        if (r === undefined) {
            r = q("min_gmap", 13, b.latlng, b.title)
        }
        r.map.setCenter(b.latlng);
        r.marker.setMap(null);
        r.marker = new google.maps.Marker({position:b.latlng, map:r.map, animation:google.maps.Animation.DROP, title:b.title})
    }

    f(".outlet-name").live("click", function () {
        var v = f(this);
        f(".outlet-name").removeClass("outlet-show");
        v.addClass("outlet-show");
        f(".outlet-attr:visible").slideUp(100);
        v.siblings().slideToggle(100);
        var w = v.attr("data-latlng");
        if (w != "") {
            arr = w.split(",");
            d(w, arr, v.text())
        } else {
            m.geocode({address:v.attr("data-addr")}, function (z, y) {
                if (y == google.maps.GeocoderStatus.OK) {
                    var x = z[0].geometry.location;
                    w = x.lat() + "," + x.lng();
                    arr = w.split(",");
                    v.attr("data-latlng", w);
                    d(w, arr, v.text())
                }
            })
        }
    });
    function j(w) {
        function x() {
            var z = "";
            for (i in w) {
                z += '<li><h5 class="outlet-name" data-addr="' + w[i].addr + '" data-latlng="' + w[i].latlng + '">' + w[i].name + '</h5><div class="outlet-attr">    <p>' + w[i].addr + "</p>    <p><span>" + w[i].tel + '</span> <a class="view-map" href="#">查看地图»</a> <a class="search-path" href="#">公交/驾车»</a></p></div></li>'
            }
            f(".outlet-list ul").html(z);
            f(".outlet-attr:first").show();
            f(".outlet-name:first").addClass("outlet-show")
        }

        var y, v;
        if (w[0]["latlng"] != "") {
            y = w[0]["latlng"];
            v = y.split(",");
            x();
            d(y, v, w[0]["name"])
        } else {
            m.geocode({address:w[0]["addr"]}, function (B, A) {
                if (A == google.maps.GeocoderStatus.OK) {
                    var z = B[0].geometry.location;
                    y = z.lat() + "," + z.lng();
                    v = y.split(",");
                    w[0]["latlng"] = y;
                    x();
                    d(y, v, w[0]["name"])
                }
            })
        }
    }

    f(".view-map").live("click", function (v) {
        v.preventDefault();
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
        if (o == undefined) {
            o = q("big_gmap", 15, b.latlng, b.title)
        }
        o.map.setCenter(b.latlng);
        o.marker.setMap(null);
        o.marker = new google.maps.Marker({position:b.latlng, map:o.map, animation:google.maps.Animation.DROP, title:b.title})
    });
    f(".search-path").live("click", function (v) {
        v.preventDefault();
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
    f("#outlet-page").delegate("a", "click", function (v) {
        v.preventDefault();
        if (f(this).hasClass("next-page")) {
            p++;
            f.getJSON(e, "currPage=" + p + "&pageSize=" + t, function (w) {
                j(w.outletList);
                if (c <= 2) {
                    u.html('<a class="prev-page" href="#">上一页</a>')
                } else {
                    if (p == c) {
                        u.html('<a class="prev-page" href="#">上一页</a>')
                    } else {
                        u.html('<a class="prev-page" href="#">上一页</a> | <a class="next-page" href="#">下一页</a>')
                    }
                }
            })
        }
        if (f(this).hasClass("prev-page")) {
            p--;
            f.getJSON(e, "currPage=" + p + "&pageSize=" + t, function (w) {
                j(w.outletList);
                if (p == 1) {
                    u.html('<a class="next-page" href="#">下一页</a>')
                } else {
                    u.html('<a class="prev-page" href="#">上一页</a> | <a class="next-page" href="#">下一页</a>')
                }
            })
        }
    });
    var l = {currPage:1, pageSize:5};

    function k(y, x) {
        var w = "";
        if (y > 1) {
            w += '<a class="prev" href="javascript:void(0)" data-page="' + (y - 1) + '"><i></i>上一页</a>'
        } else {
            w += '<span class="prev"><i></i>上一页</span>'
        }
        if (x <= 10) {
            for (var v = 1; v <= x; v++) {
                if (y == v) {
                    w += '<span class="curr">' + v + "</span>"
                } else {
                    w += '<a href="javascript:void(0)" data-page="' + v + '">' + v + "</a>"
                }
            }
        } else {
            if (y < 4) {
                for (var v = 1; v < y; v++) {
                    w += '<a href="javascript:void(0)" data-page="' + v + '">' + v + "</a>"
                }
                w += '<span class="curr">' + y + "</span>";
                w += '<a href="javascript:void(0)" data-page="' + (Number(y) + 1) + '">' + (Number(y) + 1) + "</a>";
                w += '<a href="javascript:void(0)" data-page="' + (Number(y) + 2) + '">' + (Number(y) + 2) + "</a>";
                w += '<span class="omit">...</span>';
                w += '<a href="javascript:void(0)" data-page="' + x + '">' + x + "</a>"
            }
            if (y >= 4 && (y <= x - 3)) {
                w += '<a href="javascript:void(0)" data-page="1">1</a>';
                w += '<span class="omit">...</span>';
                w += '<a href="javascript:void(0)" data-page="' + (Number(y) - 2) + '">' + (Number(y) - 2) + "</a>";
                w += '<a href="javascript:void(0)" data-page="' + (Number(y) - 1) + '">' + (Number(y) - 1) + "</a>";
                w += '<span class="curr">' + y + "</span>";
                w += '<a href="javascript:void(0)" data-page="' + (Number(y) + 1) + '">' + (Number(y) + 1) + "</a>";
                w += '<a href="javascript:void(0)" data-page="' + (Number(y) + 2) + '">' + (Number(y) + 2) + "</a>";
                w += '<span class="omit">...</span>';
                w += '<a href="javascript:void(0)" data-page="' + x + '">' + x + "</a>"
            }
            if (y > x - 3) {
                w += '<a href="javascript:void(0)" data-page="1">1</a>';
                w += '<span class="omit">...</span>';
                w += '<a href="javascript:void(0)" data-page="' + (Number(y) - 2) + '">' + (Number(y) - 2) + "</a>";
                w += '<a href="javascript:void(0)" data-page="' + (Number(y) - 1) + '">' + (Number(y) - 1) + "</a>";
                w += '<span class="curr">' + y + "</span>";
                for (var v = y + 1; v <= x; v++) {
                    w += '<a href="javascript:void(0)" data-page="' + v + '">' + v + "</a>"
                }
            }
        }
        if (y < x) {
            w += '<a class="next" href="javascript:void(0)" data-page="' + (Number(y) + 1) + '"><i></i>下一页</a>'
        } else {
            w += '<span class="next"><i></i>下一页</span>'
        }
        return w
    }

    function h() {
        f.getJSON(s, "currPage=" + l.currPage + "&pageSize=" + l.pageSize, function (x) {
            var w = x.list, v = "";
            l.pageCount = Math.ceil(x.total / l.pageSize);
            if (w.length > 0) {
                for (i in w) {
                    v += '<li><dl class="question"><dt>咨询内容：</dt><dd>' + w[i].question + '<span class="date">' + w[i].qdate + '</span></dd></dl><dl class="answer"><dt>客服回复：</dt><dd>' + w[i].answer + '<span class="date">' + w[i].adate + "</span></dd></dl></li>"
                }
                f(".consult-list").html(v);
                f("#consult .pagination").html(k(l.currPage, l.pageCount))
            }
        })
    }

    f("#consult .pagination").delegate("a", "click", function (v) {
        v.preventDefault();
        l.currPage = f(this).attr("data-page");
        h()
    });
    h();
    f("#submit").click(function (A) {
        A.preventDefault();
        var x = f("#question").val(), v = f("#mobile"), z = v.length == 1 ? v.val() : "", w = f("#consult-form .error"), B, y = function (C) {
            w.html(C).show();
            clearTimeout(B);
            B = setTimeout(function () {
                w.hide()
            }, 3000)
        };
        if (x == "") {
            y("请输入咨询的问题");
            return
        }
        if (z != "" && !(/^(1\d{10})$/i).test(z)) {
            y("请输入正确的手机号");
            return
        }
        f.get("/user-question", {content:x, mobile:z, goodsId:a}, function (C) {
            if (C.error == "") {
                y("您的咨询发表成功，请耐心等待回复，谢谢")
            } else {
                y(C.error)
            }
        })
    });
    (new GoTop()).init({pageWidth:960, nodeId:"go-top", nodeWidth:24, distanceToBottom:100, distanceToPage:10, hideRegionHeight:130, text:""})
});