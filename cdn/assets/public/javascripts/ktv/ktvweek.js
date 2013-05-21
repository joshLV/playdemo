var KTVWeek = (function  () {
    var weekNames = ["一", "二", "三", "四", "五", "六", "日"];
    var colors = ["carrot","amethyst", "concrete", "emerland"];

    function KTVWeek(){
        return  init(
            this instanceof KTVWeek ? this : new KTVWeek(),
            arguments);
    };

    function init(ktv, args) {
        if (args.length != 1) { return ktv};
        ktv.wrapperId = args[0].wrapperId;
        ktv.dataUrl = args[0].dataUrl;
        ktv.shop = args[0].shop;
        ktv.roomType = args[0].roomType;
        ktv.product = args[0].product;
        ktv.colorIndex = -1;
        $("#" + ktv.wrapperId + " .wk-button-previous").click(function(){
            var previousWeekDay = ktv.monday.addDays(-7);
            ktv.loadWeekDataFor(previousWeekDay);
        });
        $("#" + ktv.wrapperId + " .wk-button-next").click(function(){
            var nextWeekDay = ktv.monday.addDays(7);
            ktv.loadWeekDataFor(nextWeekDay);
        });
        $("#" + ktv.wrapperId + " .wk-button-thisweek").click(function(){
            if (!$(this).hasClass("wk-button-disabled")) {
                ktv.loadWeekDataFor(new Date());
            }
        });

        ktv.loadWeekDataFor(args[0].startDay);
        return ktv;
    }

    function dayStartsWithMonday(day) {
        return day == 0 ? 7 : day;
    }

    function nextColor(ktv) {
        ktv.colorIndex += 1;
        if (ktv.colorIndex == colors.length) { ktv.colorIndex = 0};
        return colors[ktv.colorIndex];

    }

    var proto = KTVWeek.prototype;
    proto.loadWeekDataFor = function(date) {
        if(date == undefined) {
            date = new Date();
        }
        var today = new XDate(date);
        var ktv = this;
        ktv.monday = new XDate().setWeek(today.getWeek());
        ktv.sunday = new XDate(ktv.monday).addDays(6);
        ktv.colorIndex = -1;
        $.post(
            ktv.dataUrl,
            {
                startDay: ktv.monday.toString("yyyy-MM-dd"),
                endDay: ktv.sunday.toString("yyyy-MM-dd"),
                "shop.id": ktv.shop,
                "roomType": ktv.roomType,
                "product.id": ktv.product
            },
            function(data){
                ktv.dataLoaded(data);
            }
        );
    }

    proto.dataLoaded = function (data) {
        var monday = this.monday;
        var sunday = this.sunday;
        if (monday.diffDays(new XDate().setWeek(new XDate().getWeek())) == 0) {
            $("#" + this.wrapperId + " .wk-button-thisweek").addClass("wk-button-disabled");
        }else{
            $("#" + this.wrapperId + " .wk-button-thisweek").removeClass("wk-button-disabled");
        }
        //初始化星期显示
        $("#"+this.wrapperId+" .wk-dayname").each(function(index){ $(this).text(new XDate(monday).addDays(index).toString("M/d （周" + weekNames[index] + "）")); });
        $("#" +this.wrapperId+" .wk-show-range").text(monday.toString("M月d日") + " - " + sunday.toString("M月d日"));
        var pricecellsEle = $("#" + this.wrapperId + " .wk-pricecells").first();
        pricecellsEle.empty();

        //逐个解析价格设置
        for (var i = 0; i < data.length; i++) {
            var strategy = data[i];
            var startDay = new XDate(strategy.startDay);
            var endDay = new XDate(strategy.endDay);
            var diffDays = startDay.diffDays(endDay);
            var weekdays = strategy.dayOfWeeks.split(",");

            var startTimes = data[i].startTimes.split(",");
            var duration = data[i].duration;

            var color = nextColor(this);

            //遍历设置中的天
            for (var j = 0; j<= diffDays; j++) {
                var day = new XDate(startDay).addDays(j);
                if (this.monday.diffDays(day) < 0 || this.sunday.diffDays(day) > 0) {
                    continue;//不在当前视图范围
                };
                if ($.inArray(dayStartsWithMonday(day.getDay()).toString(), weekdays) < 0) {
                    continue;//星期不符
                };
                //遍历设置中的时间段
                for (var k = 0; k < startTimes.length; k++) {
                    var startTime = Number(startTimes[k]);
                    var top = (startTime-8)*40 + 5;
                    var left = (dayStartsWithMonday(day.getDay())-1)*100 + 5;
                    pricecellsEle.append($("<div>",{
                        "class":"wk-pricecell wk-pricecell-" + color,
                        "data-color":color,
                        "data-price":"￥" + data[i].price,                      
                        "style":"top:"+top+"px;left:" + left+"px;height:" + (duration*40-10)+"px;line-height:" + (duration*40-10)+"px",
                        "data-id":data[i].id,
                        text:"￥"+data[i].price
                    }));
                };

            };

        };
        // 鼠标滑入滑出价格单元格
        $(".wk-pricecell").hover(
            function() {
                var ele = $(this);
                ele.html("<a href=\"/ktv/price-schedule/" + ele.attr("data-id") + "/edit\">编辑</a>")
                $(".wk-pricecell-"+ele.attr("data-color")).not(this).stop().animate({opacity:0.25},100);
            },
            function() {
                var ele = $(this);
                ele.text(ele.attr("data-price"));
                $(".wk-pricecell-"+ele.attr("data-color")).not(this).stop().animate({opacity:1}, 400);
            }
        );
    }
    return KTVWeek;
})();
