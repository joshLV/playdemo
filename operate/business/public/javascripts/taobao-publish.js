var locations = {
    "上海":["上海","崇明","朱家角"],
    "浙江":["杭州","温州", "千岛湖", "舟山","安吉","慈溪", "定海", "奉化", "海盐", "黄岩", "湖州", "嘉兴", "金华",
        "临安", "丽水", "宁波", "瓯海", "平湖", "衢州", "江山", "瑞安", "绍兴", "嵊州", "台州", "温岭"]
}
ktvProvinces=["上海", "浙江"];
ktvCities = ["上海", "杭州", "温州"];

var locationProvinceSelect = $("#location_province");
var locationCitySelect = $("#location_city");
function refreshCities(province) {
    locationCitySelect.empty();
    var cities = locations[province];
    for (j = 0; j < cities.length; j++) {
        var city = cities[j];
        locationCitySelect.append($("<option>", {"text":city, "value":city}))
    }
}

$(function(){
    var i,j,k,m,n;

    for (i = 0; i< props.length; i++) {
        var prop = props[i];
        if(prop.name == '品牌') {
            //初始化品牌
            var propValues = prop.propValues;
            var select = $("<select>", {name:"ktvBrand", "style":"width:auto"});
            for (j =0; j < propValues.length; j++) {
                var pv = propValues[j];
                var op = $("<option>", {"text":pv.name, "value":pv.vid});
                select.append(op);
            }
//            select.append($("<option>", {"text":"自定义", "value":"-1"}));
            $("#brand_field").append(select);
            $("#ktvBrandPid").val(prop.pid);
        }else if(prop.name == '优惠券适用省份') {
            var propValues = prop.propValues;
            $("#ktvProvincePid").val(prop.pid)
            var div = $("#ktv_provinces");
            for (j =0; j < propValues.length; j++) {
                var pv = propValues[j];
                if($.inArray(pv.name, ktvProvinces) >=0) {
                    div.append($("<input>",{type:"checkbox", name:"ktvProvinces", value:pv.vid}))
                    div.append($("<span>",{text:pv.name}))
                }
            }
        }else if (prop.name == '优惠券适用城市') {
            var propValues = prop.propValues;
            $("#ktvCityPid").val(prop.pid)
            var div = $("#ktv_cities");
            for (j =0; j < propValues.length; j++) {
                var pv = propValues[j];
                if($.inArray(pv.name, ktvCities) >=0) {
                    div.append($("<input>",{type:"checkbox", name:"ktvCities", value:pv.vid}))
                    div.append($("<span>",{text:pv.name}))
                }
            }
        }else if (prop.name == '使用截止日期') {

        }else if (prop.name == '面值(元)') {
            $("#faceValuePid").val(prop.pid);
        }
    }

    i =0;
    //初始化宝贝所在省份
    for (var province in locations) {
        locationProvinceSelect.append($("<option>", {"text": province, "value": province}))
        if (i ==0) {
            refreshCities(province);
        }
        i ++;
    }
    //宝贝所在省份改变
    locationProvinceSelect.change(function() {
        var province = $("#location_province option:selected").val();
        refreshCities(province);
    })

    //富文本编辑器
    KindEditor.create('textarea',
            {
                uploadJson: '/ktv/taobao/img-upload/' + $("#resalerId").val(),
                allowImageRemote:false,
                afterBlur: function(){this.sync()}
            });

    //上传主图
    KindEditor.ready(function (K) {
        var editor = K.editor({
            allowImageRemote:false,
            uploadJson:'/ktv/taobao/img-upload/' + $("#resalerId").val()
        });

        K('#buttonProdImg').click(function () {
            editor.loadPlugin('image', function () {
                editor.plugin.imageDialog({
                    showRemote:false,
                    imageUrl:K('#inputProdImg').val(),
                    clickFn:function (url, title, width, height, border, align) {
                        K('#inputProdImg').val(url);
                        K('#imgProdImg').attr("src", url);
                        editor.hideDialog();
                    }
                });
            });
        });
    });

    $("#createButton").click(function(){
        if(!$("#title").val()){
            alert("请填写宝贝标题");return;
        }
//        if(!$("#inputProdImg").val()) {
//            alert("请上传主图");return;
//        }
        if ($("input[name='ktvProvinces']:checked").length == 0) {
            alert("请选择适用省份");return;
        }
        if ($("input[name='ktvCities']:checked").length == 0) {
            alert("请选择适用城市");return;
        }
        if($("#prodDescription").val().length <=5) {
            alert("请填写宝贝描述，并大于5个字符");return;
        }
        $("#form").submit();
    });
});