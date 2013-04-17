var data = {
    "rooms":[
        {
            "id":2,
            "name":"小包厢1号",
            "type":1
        },
        {
            "id":3,
            "name":"小包厢2号",
            "type":1
        }
    ],
    "prices":[
        {
            "startDay":"2013-04-11",
            "endDay":"2013-04-18",
            "weekday":"1,3,4,5",
            "startTime":"10:00",
            "endTime":"12:00",
            "price":"30",
            "roomType":1
        },
        {
            "startDay":"2013-04-08",
            "endDay":"2013-04-19",
            "weekday":"1,2,3,5",
            "startTime":"14:00",
            "endTime":"16:00",
            "price":"50",
            "roomType":1
        },
        {
            "startDay":"2013-04-10",
            "endDay":"2013-04-19",
            "weekday":"1,2,3,4,5",
            "startTime":"18:00",
            "endTime":"22:00",
            "price":"60",
            "roomType":1
        }
    ],
    "schedules":[
        {
            "roomId":2,
            "roomTime":"12:00"
        },
        {
            "roomId":2,
            "roomTime":"13:00"
        }
    ]
};

var KTVOrder = (function  () {
    var colors = ["carrot","amethyst", "alizarin", "emerland"];
    var weekNames = ["一", "二", "三", "四", "五", "六", "日"];

    function KTVOrder(){
        return  init(
            this instanceof KTVOrder ? this : new KTVOrder(),
            arguments);
    };

    function init(ktv, args) {
        if (args.length != 1) { return ktv};
        ktv.wrapperId = args[0].wrapperId;
        ktv.summaryId = args[0].summaryId;
        ktv.amountId = args[0].amountId;

        ktv.day = new XDate(new XDate(args[0].day).toString("yyyy-MM-dd"));
        ktv.dataUrl = args[0].dataUrl;

        ktv.rooms = new Array();
        ktv.selected = new Array();

        $("#" + ktv.wrapperId + " .wk-order-days .wk-order-day").each(function(index){
            var ele = $(this);
            var day = ktv.day.clone().addDays(index);
            ele.text(day.toString("MM-dd") + " 周" + weekNames[dayStartsWithMonday(day.getDay())-1]);
            ele.attr("data-day", day.toString("yyyy-MM-dd"));
            ele.click(function(){
                var dataDay = ele.attr("data-day");
                if (ktv.day.diffDays(new XDate(dataDay)) == 0) {return;};
                ktv.loadScheduleDataFor(dataDay);
            });
        });

        ktv.loadScheduleDataFor(args[0].startDay);
        return ktv;
    }

    function dayStartsWithMonday(day) {
        return day == 0 ? 7 : day;
    }

    var proto = KTVOrder.prototype;
    proto.loadScheduleDataFor = function(date) {
        if(date == undefined) {
            date = new Date();
        }
        var ktv = this;
        ktv.day = new XDate(new XDate(date).toString("yyyy-MM-dd"));

        $("#" + ktv.wrapperId + " .wk-order-days .wk-order-day").each(function(index){
            var ele = $(this);
            if (new XDate(ele.attr("data-day")).diffDays(ktv.day) == 0) {
                ele.addClass("wk-order-day-selected");
            }else{
                ele.removeClass("wk-order-day-selected");
            }

        });

        this.dataLoaded(data);
        /*
        ktv.monday = new XDate().setWeek(today.getWeek());
        ktv.sunday = new XDate(ktv.monday).addDays(6);
        $.post(
            ktv.dataUrl,
            {startDay: ktv.monday.toString("yyyy-MM-dd"), endDay: ktv.sunday.toString("yyyy-MM-dd")},
            function(data){
                ktv.dataLoaded(data);
            }
        );
        */
    }

    function toggleSelected(ktv, ele) {
        var roomId = Number(ele.attr("data-room-id")); 
        var time = ele.attr("data-time");

        var index = -1;
        for (var i = 0; i < ktv.selected.length; i++) {
            var selected = ktv.selected[i]
            if (selected) {
                if (selected.roomId == roomId && selected.time == time) {
                    index = i;
                    break;
                };
            };
        };
        //更新保存的选择信息
        if (index >= 0) {
            ktv.selected.splice(index, 1);
        }else {
            ktv.selected.push({
                roomId: roomId,
                time:time,
                price:Number(ele.attr("data-price"))
            });
        }

        //更新总价
        var amount = 0;
        for (i = 0; i < ktv.selected.length; i++) {
            var selected = ktv.selected[i]
            if (selected) {
                amount += selected.price;
            };
        };
        $("#" + ktv.amountId).text("￥" + amount);
        return index;
    }

    function closePriceSummary(ktv, ele) {
        var index = toggleSelected(ktv, ele);
        if (index >=0) {
            var roomId = Number(ele.attr("data-room-id")); 
            var time = ele.attr("data-time");
            $("#" + ktv.wrapperId + " [data-room-id='" + roomId + "'][data-time='" + time + "']").removeClass("wk-order-room-selected");
            ele.remove();
        };

    }

    function togglePriceCell(ktv, ele) {
        var index = toggleSelected(ktv, ele);

        var roomId = Number(ele.attr("data-room-id")); 
        var time = ele.attr("data-time");
        var price = Number(ele.attr("data-price"));

        if (index >= 0) {
            ele.removeClass("wk-order-room-selected");
            $("#" + ktv.summaryId + " [data-room-id='" + roomId + "'][data-time='" + time + "']").remove();
        }else {
            ele.addClass("wk-order-room-selected");
            var roomName = $(".wk-order-room[data-room-id='" + roomId + "'] .wk-pri").text();

            $("#" + ktv.summaryId).append(
                $("<div/>", {
                    "class":"wk-selected",
                    "data-room-id": roomId,
                    "data-time":time,
                    "data-price":price
                }).append(
                    $("<span/>").css("float", "left").text(time + " " + roomName)
                    .append($("<span/>").addClass("wk-summary-price").text(price) )
                    .append($("<span/>").text("元"))
                ).append(
                    $("<span/>",{
                        "class":"wk-summary-close",
                        text:"x"
                    }).click(function(){closePriceSummary(ktv, $(this).parent())})
                )
            );
        }
    }

    proto.dataLoaded = function (data) {
        //你好
        var ktv = this;
        var roomsEle = $("#" + ktv.wrapperId + " .rooms");

        roomsEle.empty();
        $("#" + ktv.summaryId).empty();
        ktv.selected = new Array();
        ktv.rooms = new Array();

        //创建房间
        for (var i = 0; i < data.rooms.length; i++) {
            var room = data.rooms[i];
            ktv.rooms[room.id] = new Array();
            roomsEle.append(
                $("<div/>", {
                    "class": "wk-order-room",
                    "data-room-id": room.id,
                    "data-room-type": room.type,
                    html: $("<div/>", {
                        "class": "wk-pri",
                        text: room.name                       
                    })
                })
            );
        };
        //先画上已预订的格子
        for (i = 0; i < data.schedules.length; i++) {
            var schedule = data.schedules[i];
            var time = Number(schedule.roomTime.substring(0, schedule.roomTime.indexOf(":")));
            ktv.rooms[schedule.roomId].push(time);
            $("#" + ktv.wrapperId + " [data-room-id='" + schedule.roomId + "']").append(
                $("<div/>", {
                    "class": "wk-order-room-cell wk-order-room-reserved",
                    css:{
                        "top":"2px",
                        "left": (60 + 4 + (time-8)*44) + "px"
                    }
                })
            );
        };

        //画上有价格的格子
        for (i = 0; i < data.prices.length; i++) {
            var price = data.prices[i];
            var startDay = new XDate(price.startDay);
            var endDay = new XDate(price.endDay);
            if (startDay.diffDays(ktv.day) < 0 || endDay.diffDays(ktv.day) > 0 ) {
                continue;//日期不符
            };
            var weekdays = price.weekday.split(",");
            if ($.inArray(dayStartsWithMonday(ktv.day.getDay()).toString(), weekdays) < 0) {
                continue;//星期不符
            };
            var starTime = Number(price.startTime.substring(0, price.startTime.indexOf(":")));
            var endTime = Number(price.endTime.substring(0, price.endTime.indexOf(":")));

            for (var j = starTime; j < endTime; j++) {
                $("#" + ktv.wrapperId + " [data-room-type='" + price.roomType + "']").each(function(){
                    var ele = $(this);
                    var roomId = Number(ele.attr("data-room-id"));
                    if ($.inArray(j, ktv.rooms[roomId]) >= 0) {
                        return true;//已经被预订
                    };
                    ktv.rooms[roomId].push(j);

                    ele.append(
                        $("<div/>", {
                            "class": "wk-order-room-cell wk-order-room-price",
                            "data-room-id":roomId,
                            "data-time":j + ":00",
                            "data-price": price.price,
                            text: "￥" + price.price,
                            css:{
                                "top":"2px",
                                "left": (60 + 4 + (j-8)*44) + "px"
                            },
                            click:function(){togglePriceCell(ktv, $(this))}
                        })
                    );
                });
            };
        };
        //补上空的格子
        $("#" + ktv.wrapperId + " [data-room-type]").each(function(){
            var ele = $(this);
            var roomId = Number(ele.attr("data-room-id"));
            for (var i = 8; i <= 23; i++) {
                if ($.inArray(i, ktv.rooms[roomId]) >= 0) {
                    continue;//已经有块了，或者是被预订，或者是有价格的
                };
                ele.append(
                    $("<div/>", {
                        "class": "wk-order-room-cell wk-order-room-blank",
                        css:{
                            "top":"2px",
                            "left": (60 + 4 + (i-8)*44) + "px"
                        }
                    })
                );
            };

        });
    }
    return KTVOrder;
})();

