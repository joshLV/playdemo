$(function (d) {
    d("#slides").slides({play:5000, pause:2500, slideSpeed:500, hoverPause:true});
    d(".tab-hd a").mouseover(function () {
        d(".tab-hd .active").removeClass("active");
        d(this).addClass("active");
        var e = d(".tab-hd a").index(d(this));
        d(".tab-bd .goods").hide();
        d(d(".tab-bd .goods")[e]).show()
    });
    d(".accordion dt").mouseover(function () {
        d(this).next("dd").slideToggle(0).siblings("dd:visible").slideUp(0);
        d(this).slideToggle(0);
        d(this).siblings("dt:hidden").slideDown(0)
    });
    d(".goods").delegate("li", "mouseover",function () {
        d(this).find(".region").addClass("hover")
    }).delegate("li", "mouseout", function () {
        d(this).find(".region").removeClass("hover")
    });
    (new GoTop()).init({pageWidth:960, nodeId:"go-top", nodeWidth:24, distanceToBottom:100, distanceToPage:10, hideRegionHeight:130, text:""});
    var b = d("#j_links"), a = 5000, c;
    b.hover(function () {
        clearInterval(c)
    },function () {
        c = setInterval(function () {
            var e = b.find("li:first"), f = e.height();
            e.animate({marginTop:-f + "px"}, 600, function () {
                e.css("marginTop", 0).appendTo(b)
            })
        }, a)
    }).trigger("mouseleave")
});