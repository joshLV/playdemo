var selectedHours = [];
var selectedDays =[];

var startDayEle = $("#startDay");
var endDayEle = $("#endDay");
var dayPreview = $("#dayPreview");
var hoursCount = 0;
//点击添加日期
function addDateRange(startDayVal, endDayVal){
    var sa = startDayVal.split("-");
    var ea = endDayVal.split("-");
    var startDay = new Date(sa[0], Number(sa[1])-1, sa[2]);
    var endDay = new Date(ea[0], Number(ea[1])-1, ea[2]);
    if(selectedDays.length == 0){
        dayPreview.empty();
    }
    var text = dateToHan(startDay) + " - " + dateToHan(endDay);
    dayPreview.append(
        $("<div>", {style:"margin-bottom:4px;"})
            .append( $("<span>",{text: text}))
            .append($("<span>",{text:"x", "class":"delDay", "data-start":startDayVal,"data-end":endDayVal, click:delDayClick}))
    );
    selectedDays.push([startDay, endDay]);
    var newStartDay = new Date(endDay);
    newStartDay.setDate(endDay.getDate() + 1);
    startDayEle.val(newStartDay.getFullYear() + "-" + (newStartDay.getMonth()+1) + "-" + newStartDay.getDate());
    endDayEle.val(startDayEle.val());
    resetDayInput();
}

function toggleHour(ele) {
    var hour = Number(ele.attr("data-hour").substring(0, 2));
    var index = Number(ele.attr("data-index"));

    var i = $.inArray(hour, selectedHours);
    if (i >= 0) {
        selectedHours.splice(i, 1);
        ele.removeClass("hour-selected");
        ele.text(ele.attr("data-hour"));
        ele.animate({width: "40px"}, 100);
    } else {
        var eleSelectedDuration = $("#priceStrategy_product_id option:selected");
        var duration = Number(eleSelectedDuration.attr("data-duration"));
        if (hour < 8 && hour + duration > 8) {
            return;
        }
        for (var j = 1; j < duration; j++) {
            var h = hour+j >= 24 ? hour+j - 24 : hour+j;
            var k = $.inArray(h, selectedHours);
            if (k >= 0) {
                return;
            }
        }
        selectedHours.push(hour);
        ele.addClass("hour-selected");
        start = hour < 10 ? "0" + hour : hour;
        end = hour+duration;
        end = end >= 24 ? end - 24 : end;
        end = end < 10 ? "0" + end : end;
        ele.text(start + ":00 - " + end + ":00");
        ele.animate({width: (duration * 44 - 4) + "px"}, 80);
    }
    resetHourInput();
}

//刷新显示 已选择的日期
function resetDayInput(){
    var days = [];
    $.each(selectedDays, function(index, entry){
        days.push(dateToHan(entry[0])+"-"+dateToHan(entry[1]));
    });
    $("#inputDays").val(days.join(";"));
}
function resetHourInput() {
    selectedHours.sort(function(a,b){return a-b})
    $("#inputStartTimes").val(selectedHours.join(","));
    var hourPreview = $("#hourPreview");
    var eleSelectedDuration = $("#priceStrategy_product_id option:selected");
    var duration = Number(eleSelectedDuration.attr("data-duration"));
    if(selectedHours.length == 0){
        hourPreview.text("未选择时间段，请点击以上所列时间。【可多选】");
    }else{
        hourPreview.empty();
        $.each(selectedHours, function(index, hour){
            var end = hour+duration;
            end = end >= 24? end -24 : end;
            hourPreview.append(
                $("<span>",{text: fill2(hour) + ":00 - " + fill2(end) + ":00", style:"padding-right:40px;"})
            );
            if(((index + 1)%4) == 0){
                hourPreview.append($("<br>"));
            }
        });
    }
}
//重设已选择的时间
function resetSelectedHours() {
    selectedHours = [];
    $(".hour").each(function () {
        var ele = $(this);
        ele.removeClass("hour-selected");
        ele.removeClass("hour-selected");
        ele.text(ele.attr("data-hour"));
        ele.animate({width: "40px"}, 100);
    });
    resetHourInput();
}
function fill2(n){
    if (n < 10) return "0" + n;
    return "" + n;
}
function dateToHan(date) {
    return date.getFullYear()+"年" + fill2(date.getMonth()+1) + "月" + fill2(date.getDate()) + "日";
}
//点击删除已选日期
function delDayClick() {
    var ele = $(this);
    var sa = ele.attr("data-start").split("-");
    var ea = ele.attr("data-end").split("-");
    var startTime = new Date(sa[0], Number(sa[1])-1, sa[2]).getTime();
    var endTime = new Date(ea[0], Number(ea[1])-1, ea[2]).getTime();
    var index = -1;
    for(var i = 0 ; i < selectedDays.length; i ++){
        var entry = selectedDays[i];
        if(startTime == entry[0].getTime() && endTime == entry[1].getTime()){
            index = i;
            break;
        }
    }
    if (index >=0 ) {
        selectedDays.splice(index, 1);
        ele.unbind("click");
        ele.parent().remove();
        if(selectedDays.length==0) {
            $("#dayPreview").text("未添加，请选择日期范围，并点击添加。一次可【添加多个】日期范围");
        }
        resetDayInput();
    }
}
$(function () {
    hoursCount = $("#timeRangeBox .hour").length;
    $("#durationPer").text($("#priceStrategy_product_id option:selected").attr("data-duration"));
    //左右移动时间范围
    $(".switch_time_range").click(function(){
        $("#timeRangeBox").animate({left: $(this).attr("data-offset")},100);
    });
    var startDayEle = $("#startDay");
    var endDayEle = $("#endDay");
    var dayPreview = $("#dayPreview");
    //点击添加日期
    $("#addDate").click(function(){
        var startDayVal = startDayEle.val()
        var endDayVal = endDayEle.val()
        if(!startDayVal || !endDayVal){
            alert("请选择日期");return;
        }
        var sa = startDayVal.split("-");
        var ea = endDayVal.split("-");
        var startDay = new Date(sa[0], Number(sa[1])-1, sa[2]);
        var endDay = new Date(ea[0], Number(ea[1])-1, ea[2]);
        if(startDay.getTime() > endDay.getTime()){
            alert("结束日期不可小于开始日期");return;
        }
        var today = new Date();
        today = new Date(today.getFullYear() , today.getMonth() , today.getDate());
        if(endDay < today) {
            alert("结束日期必须大于等于今天");return;
        }
        //检查冲突
        var startDayTime = startDay.getTime();
        var endDayTime = endDay.getTime();

        var conflict = false;
        $.each(selectedDays, function(index, entry){
            if (startDayTime <= entry[1].getTime() && endDayTime >=entry[0].getTime()){
                conflict = true;
                alert("与 " + dateToHan(entry[0]) + " - " + dateToHan(entry[1]) + " 日期范围重合，请重新选择");
                conflict = true;
                return false;
            }
        } );
        if(conflict) {
            return;
        }

        addDateRange(startDayVal, endDayVal);
    });
    //设置统一的门店数量
    $("#roomCountAll").keyup(function () {
        $("#shopGroup input[name^=shop]").val($(this).val());
    });
    //切换时长radio
    $("#priceStrategy_product_id").change(function () {
        var ele = $("#priceStrategy_product_id option:selected");
        $("#durationPer").text(ele.attr("data-duration"));
        resetSelectedHours();
    });
    //点击时间点
    $(".hour").click(function () {
        var ele = $(this);
        toggleHour(ele);
    });
    $("#createButton").click(function () {
        var errorEle = $("#errorMsg");
        errorEle.hide();

        var price = $("#price").val();
        if (!price) {
            errorEle.text("请填写价格").show();
            return;
        }

        var inputDays = $("#inputDays").val();
        if (!inputDays) {
            errorEle.text("请选择预订日期范围，并点击添加").show();
            return;
        }

        var inputStartTimes = $("#inputStartTimes").val();
        if (!inputStartTimes) {
            errorEle.text("请选择预订时段").show();
            return;
        }

        var roomType = $("input[name='priceStrategy.roomType']:checked").val();
        if (!roomType) {
            errorEle.text("请选择包厢类型").show();
            return;
        }

        var shopIds = [];
        $("#shopGroup input[name^='shop-']").each(function () {
            var ele = $(this);
            if (ele.val() && $.trim(ele.val()) && ele.val() != 0 ) {
                shopIds.push($.trim(ele.val()));
            }
        });
        if (shopIds.length == 0) {
            errorEle.text("请至少为一个门店设置其包厢数量").show();
            return
        }

        //检查是否有冲突
        $.ajax({
            type:'POST',
            url:'/ktv/price-schedule/collision-detect',
            data:{
                "priceStrategy.product.id": $("#priceStrategy_product_id").val(),
                "priceStrategy.roomType": roomType,
                "days":inputDays,
                "shopIds": shopIds.join(","),
                "startTimes": inputStartTimes,
                "excludeProductId":$("#excludeProductId").val()
            },
            success:function (data) {
                if (!data.isOk) {
                    if (data.error) {
                        errorEle.text(data.error).show();
                    } else if (data.scheduleId) {
                        errorEle.empty()
                            .append($("<span>", {text:"与已有的价格策略冲突，"}))
                            .append($("<a>", {target:"_blank", text:"点击查看", href:"/ktv/price-schedule/" + data.scheduleId}))
                            .append($("<span>", {text:"发生冲突的价格策略"})).show();
                    }
                }else{
                    $("#creationForm").submit();
                }
            },
            error:function(data) {
                errorEle.text("服务器发生错误").show();
            }
        });
    });
});

