jQuery(document).ready(function () {
    var d = function (i, k) {
        try {
            window.external.AddFavorite(i, k)
        } catch (j) {
            try {
                window.sidebar.addPanel(k, i, "")
            } catch (j) {
                alert("对不起，您的浏览器不支持自动收藏，请使用Ctrl+D进行手动收藏！")
            }
        }
    };
    $("#favorite").click(function (i) {
        i.preventDefault();
        var j = window.location, k = document.title;
        d(j, k)
    });
    var h = $("#cart"), g = $("#cart .cart-box"), c = $("#cart .cart-box-bd");
    h.mouseover(function () {
        if ($("#reload").val() == "true") {
            c.load("/carts/tops", function (i) {
                g.addClass("hover");
                $("#reload").val(false)
            })
        } else {
            g.addClass("hover")
        }
    }).mouseout(function () {
        g.removeClass("hover")
    });
    $(".goods-del").live("click", function (l) {
        l.preventDefault();
        var j = parseInt($(this).attr("count")), k = $(this).attr("goods_id"), n = $(this).attr("sale_price"), m = parseInt($("#all_number").html()), i = $("#all_price").html();
        $.ajax({type:"DELETE", url:"/carts/" + k, success:function (q) {
            var p = parseInt($("#cart-count").html());
            var o = p - j;
            $("#cart-count").html(o);
            if (o == 0) {
                c.html('<div class="emptycart">你的购物车里没有任何商品。</div><div class="totalcart">共<em>0</em>件商品，共计<em>0.00</em>元</div>')
            } else {
                $("#row_" + k).animate({height:"0"}, 100, function () {
                    $(this).remove()
                });
                $("#all_number").html(m - j);
                $("#all_price").html((i - (n * j)).toFixed(2));
                $("#cart-js").html(m - j)
            }
        }})
    });
    var f = $("#cate"), e = $("#cate .item"), a = $("#cate .line"), b = $("#cate .more");
    e.live("mouseover",function () {
        var i = $(this);
        e.removeClass("active");
        i.addClass("active");
        b.hide();
        i.children(".more").show();
        a.hide();
        i.children(".line").show().height(i.height() - 2)
    }).live("mouseout", function () {
        $(this).removeClass("active");
        b.hide();
        a.hide()
    });
    !$("body.home").length && $("#cate").mouseover(function () {
        $("#cate .menu").show()
    }).mouseout(function () {
        $("#cate .menu").hide()
    })
});
GoTop = function () {
    this.config = {pageWidth:960, nodeId:"go-top", nodeWidth:50, distanceToBottom:120, distanceToPage:20, hideRegionHeight:90, text:""};
    this.cache = {topLinkThread:null}
};
GoTop.prototype = {init:function (b) {
    this.config = b || this.config;
    var a = this;
    jQuery(window).scroll(function () {
        a._scrollScreen({_self:a})
    });
    jQuery(window).resize(function () {
        a._resizeWindow({_self:a})
    });
    a._insertNode({_self:a})
}, _insertNode:function (c) {
    var a = c._self;
    var b = jQuery('<a id="' + a.config.nodeId + '" href="#">' + a.config.text + "</a>");
    b.appendTo(jQuery("body"));
    if (jQuery.scrollTo) {
        b.click(function () {
            jQuery.scrollTo({top:0}, 400);
            return false
        })
    }
    var d = a._getDistanceToBottom({_self:a});
    if (/MSIE 6/i.test(navigator.userAgent)) {
        b.css({display:"none", position:"absolute", right:d + "px"})
    } else {
        b.css({display:"none", position:"fixed", right:d + "px", top:(jQuery(window).height() - a.config.distanceToBottom) + "px"})
    }
}, _scrollScreen:function (c) {
    var a = c._self;
    var b = jQuery("#" + a.config.nodeId);
    if (jQuery(document).scrollTop() <= a.config.hideRegionHeight) {
        clearTimeout(a.cache.topLinkThread);
        b.hide();
        return
    }
    if (/MSIE 6/i.test(navigator.userAgent)) {
        clearTimeout(a.cache.topLinkThread);
        b.hide();
        a.cache.topLinkThread = setTimeout(function () {
            var d = jQuery(document).scrollTop() + jQuery(window).height() - a.config.distanceToBottom;
            b.css({top:d + "px"}).fadeIn()
        }, 400)
    } else {
        b.fadeIn()
    }
}, _resizeWindow:function (c) {
    var a = c._self;
    var b = jQuery("#" + a.config.nodeId);
    var d = a._getDistanceToBottom({_self:a});
    var e = jQuery(window).height() - a.config.distanceToBottom;
    if (/MSIE 6/i.test(navigator.userAgent)) {
        e += jQuery(document).scrollTop()
    }
    b.css({right:d + "px", top:e + "px"})
}, _getDistanceToBottom:function (b) {
    var a = b._self;
    var c = parseInt((jQuery(window).width() - a.config.pageWidth + 1) / 2 - a.config.nodeWidth - a.config.distanceToPage, 10);
    if (c < 10) {
        c = 10
    }
    return c
}};