var KTVOrder = (function  () {
    var weekNames = ["一", "二", "三", "四", "五", "六", "日"];
    var roomTypes = ["mini", "small", "middle", "large", "deluxe"];
    var roomTypeNames = {"mini":"迷你包","small":"小包","middle":"中包","large":"大包","deluxe":"豪华包"}

    function KTVOrder(){
        return  init(
            this instanceof KTVOrder ? this : new KTVOrder(),
            arguments);
    }

    function init(ktv, args) {
        if (args.length != 1) { return ktv}
        var arg = args[0]; 
        ktv.wrapperId = arg.wrapperId;
        ktv.viewMode = arg.viewMode;
        ktv.summaryId = arg.summaryId;
        ktv.amountId = arg.amountId;

        ktv.day = new XDate(new XDate(arg.day).toString("yyyy-MM-dd"));
        ktv.dataUrl = arg.dataUrl;
        ktv.shopId = arg.shopId;

        $("#" + ktv.wrapperId + " .wk-order-days .wk-order-day").each(function(index){
            var ele = $(this);
            var day = ktv.day.clone().addDays(index);
            ele.text(day.toString("MM-dd") + " 周" + weekNames[dayStartsWithMonday(day.getDay())-1]);
            ele.attr("data-day", day.toString("yyyy-MM-dd"));
            ele.click(function(){
                var dataDay = ele.attr("data-day");
                if (ktv.day.diffDays(new XDate(dataDay)) == 0) {return;}
                ktv.loadScheduleDataFor(dataDay);
            });
        });

        ktv.rooms =  {};
         // ktv.rooms like this:
         /*
        {
            "mini":{
                "div":"<div>",
                "durations":{
                    "3":{
                        "div":"<div>",
                        "rooms":[
                            {
                                "div":"<div>",
                                "holders":[
                                    {
                                        "startTime":8,
                                        "type": "ordered"
                                    }
                                ]
                            },
                        ]
                    }
                }
            }
        }
        */
        
        for (var i = 0; i < roomTypes.length; i++) {
            var roomType = roomTypes[i];
            ktv.rooms[roomType] = {
                "div": $("#"+ktv.wrapperId + " .room-"+roomType).first(),
                "durations":{}
            };
        };

        ktv.selected = [];


        ktv.loadScheduleDataFor(ktv.day);
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

        $("#" + ktv.wrapperId + " .wk-order-days .wk-order-day").each(function(){
            var ele = $(this);
            if (new XDate(ele.attr("data-day")).diffDays(ktv.day) == 0) {
                ele.addClass("wk-order-day-selected");
            }else{
                ele.removeClass("wk-order-day-selected");
            }

        });

        $.post(
            ktv.dataUrl,
            {
                "shop.id":ktv.shopId,
                day:ktv.day.toString("yyyy-MM-dd")
            },
            function(data){
                ktv.dataLoaded(data);
            }
        );
    };

    function toggleSelected(ktv, ele) {
        var roomId = Number(ele.attr("data-room-id")); 
        var time = ele.attr("data-time");

        var index = -1;
        var selected;
        for (var i = 0; i < ktv.selected.length; i++) {
            selected = ktv.selected[i];
            if (selected.roomId == roomId && selected.time == time) {
                index = i;
                break;
            }
        }
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
            amount += ktv.selected[i].price;
        }
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
        }
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

    /**
        人生苦短，找到一个可以依靠的room。在所有指定roomType的rooms中，从上到下找到第一个avaliable的
    **/
    proto.findAvaliableRoom = function(roomType, startTime, duration, replaceHolder) {
        var ktv = this; 
        var rt = ktv.rooms[roomType];
        var dr = rt.durations[duration];
        if (!dr) {
            dr = {
                "div":$("<div>",{duration:duration}),
                "rooms":[]
            }
            rt.div.append(dr.div);
            rt.durations[duration] = dr;
        };
        var room;
        //遍历该类型的rooms
        for (var i = 0; i < dr.rooms.length; i++) {
            var r = dr.rooms[i];
            var conflict = false;

            for (var j = 0; j < r.holders.length; j++) {
                Things[i]
            };


            for (var j = 0; j < duration; j++) {
                for (var m = 0; m < r.holders.length; m++) {
                    var holder = r.holders[m];
                    if ((startTime+j) == holder.startTime) {
                        conflict = true;
                        break;
                    };
                };
                if (conflict) {
                    break;
                };
            };

/*
            if (conflict && holderIgnore) {
                if (holderIgnore == r.type) {

                };
            };
            */

            if (!conflict) {
                room = r;
                break;
            };
        };
        //没找到可用的room，就新建一个
        if (!room) {
            room = {
                "holders":[],
                "div" : $("<div/>", {
                    "class": "wk-order-room",
                    // "data-room-id": room.id,
                    "data-room-type": roomType,
                    html: $("<div/>", {
                        "class": "wk-pri",
                        text: roomTypeNames[roomType]
                    })
                })
            };
            dr.div.append(room.div);
            dr.rooms.push(room);
        };
        return room;
    }

    proto.dataLoaded = function (data) {
        var ktv = this;
        //清空所有房间大类下面的子房间，及相关数据
        for (var i = 0; i < roomTypes.length; i++) {
            var roomType = roomTypes[i];
            ktv.rooms[roomType]["div"].empty();
            ktv.rooms[roomType]["durations"] = {};
        };

        if (!ktv.viewMode) {
            $("#" + ktv.summaryId).empty();
        };
        ktv.selected = [];

        //画上有价格的格子
        for (i = 0; i < data.schedules.length; i++) {
            var schedule = data.schedules[i];
            var dayOfWeeks = schedule.dayOfWeeks.split(",");
            if ($.inArray(dayStartsWithMonday(ktv.day.getDay()).toString(), dayOfWeeks) < 0) {
                continue;//星期不符
            }
            var startTimes = schedule.startTimes.split(",");

            for (var j = 0; j < startTimes.length; j++) {
                var startTime = Number(startTimes[j]);
                for (var k = 0; k < schedule.roomCount; k++) {
                    var room = ktv.findAvaliableRoom(schedule.roomType.toLowerCase(), startTime, schedule.duration);

                    var priceCell = $("<div/>", {
                        "class": "wk-order-room-cell wk-order-room-price",
                        // "data-room-id":roomId,
                        "data-time":(startTime<10 ? "0"+startTime : startTime) + ":00",
                        "data-price": schedule.price,
                        text: "￥" + schedule.price,
                        css:{
                            "top":"2px",
                            "left": (60 + 4 + (startTime-8)*44) + "px",
                            "width":(44*schedule.duration - 4) + "px"
                        }
                    })
                    if (!ktv.viewMode) {
                        priceCell.click(function(){togglePriceCell(ktv, $(this))});
                    };

                    room["div"].append(priceCell);
                    for (var m = 0; m < schedule.duration; m++) {
                        room["holders"].push({
                            "startTime": startTime+m,
                            "type":"schedule"
                        });
                    };
                };
            };
        };

        //先画上已预订的格子
        for (i = 0; i < data.orders.length; i++) {
            var order = data.orders[i];
            var scheduleTime = Number(order.scheduledTime);

            var room = ktv.findAvaliableRoom(order.roomType.toLowerCase(), scheduleTime, order.duration);
            room["div"].append(
                $("<div/>", {
                    "class": "wk-order-room-cell wk-order-room-reserved",
                    css:{
                        "top":"2px",
                        "left": (60 + 4 + (scheduleTime-8)*44) + "px",
                        "width":(44*order.duration - 4) + "px"
                    }
                })
            );
            for (var m = 0; m < order.duration; m++) {
                room["holders"].push(scheduleTime+m);
                room["holders"].push({
                    "startTime": scheduleTime+m,
                    "type":"order"
                });
            };
        }


        /*
        //补上空的格子
        $("#" + ktv.wrapperId + " [data-room-type]").each(function(){
            var ele = $(this);
            var roomId = Number(ele.attr("data-room-id"));
            for (var i = 8; i <= 23; i++) {
                if ($.inArray(i, ktv.rooms[roomId]) >= 0) {
                    continue;//已经有块了，或者是被预订，或者是有价格的
                }
                ele.append(
                    $("<div/>", {
                        "class": "wk-order-room-cell wk-order-room-blank",
                        css:{
                            "top":"2px",
                            "left": (60 + 4 + (i-8)*44) + "px"
                        }
                    })
                );
            }

        });
        */
    };
    return KTVOrder;
})();

