
var cityTreeCallbacks = {}
$.extend(cityTreeCallbacks,getSinglePathCallbacks("city-tree"),getOnclickCallback("city"))
var cityTreeSettings = {
    async: {
        enable: true,
        url:"/jd-ztree/city",
        autoParam:["id", "type"]
    },
    check:{
        enable:true
    },
    data: {
        simpleDate:{
            enable: false
        }
    },
    view: {
        showIcon: false,
        showLine: false,
        dblClickExpand: false
    },
    callback: cityTreeCallbacks
};

var groupTreeCallbacks = {}
$.extend(groupTreeCallbacks,getSinglePathCallbacks("group-tree"),getOnclickCallback("group"))
var groupTreeSettings = {
    async: {
        enable: true,
        url:"/jd-ztree/group",
        autoParam:["id"]
    },
    check:{
        enable:true,
        chkStyle:"radio",
        radioType:"all"
    },
    data: {
        simpleDate:{
            enable: false
        }
    },
    view: {
        showIcon: false,
        showLine: false,
        dblClickExpand: false
    },
    callback: groupTreeCallbacks
};


$(function(){
    //上传图片
    KindEditor.ready(function (K) {
        var editor = K.editor({
            allowFileManager:true,
            uploadJson:'/goods/images'
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
    $("#inputTeamTitle, #inputTitle").each(function(){
        var ele = $(this);
        var maxLength = parseInt(ele.attr('data-max-length'));
        var monitor = $('#' + ele.attr('id') + '-monitor');
        ele.keyup(function (){
            var length =  ele.val().length;
            monitor.text('(' + length + '/' + maxLength + ')');
            monitor.css('color', length > maxLength ? 'red' : 'green');
        });
        ele.keyup();
    });

    $.fn.zTree.init($("#city-tree"), cityTreeSettings, defaultCityData);
    $("#city-show").click(showTreeFunc('city'));
    $.fn.zTree.init($("#group-tree"), groupTreeSettings);
    $("#group-show").click(showTreeFunc('group'));

    KindEditor.create('textarea',
    {
        filterMode:false,
        allowFileManager:false
    });

    var tree = $.fn.zTree.getZTreeObj("city-tree");
    var manul_areas = $("#manul-areas").text().split(',');
    var areas_left = [];
    var area_names = [];
    for (var i = 0; i < manul_areas.length; i++) {
        var node = tree.getNodeByParam('name', manul_areas[i], null);
        if (node) {
            tree.checkNode(node, true,false);
            area_names.push(node.name);
        }else{
            areas_left.push(manul_areas[i]);
        }
    }
    $("#manul-areas").text(areas_left.join(','));
    $("#city-show").val(area_names.join(','));


    $("#submit").click(function(){
        var groups = $.fn.zTree.getZTreeObj("group-tree").getCheckedNodes(true);
        if (groups.length == 0) {
            alert('请选择分类');return false;
        }
        $("#groupId-value").val(groups[0].getParentNode().id);
        console.log(groups[0].getParentNode().id)

        var cityTree = $.fn.zTree.getZTreeObj("city-tree");
        var areas = cityTree.getCheckedNodes(true);
        if (areas.length == 0) {
            alert('请选择区域商圈');return false;
        }
        $('#city-value').val(areas[0].getParentNode().getParentNode().id);
        var areaValue = []
        for (var i = 0; i < areas.length;i ++) {
            areaValue.push(areas[i].getParentNode().id + '-' + areas[i].id)
        }
        $("#city-areas").val(areaValue.join(','));

        return true;
    });
});

var defaultCityData =[
        {
            name:"上海",
            id: 34,
            type: "city",
            nocheck: true,
            isParent:true,
            children:[
                         {id:'1257',name:'黄浦区',isParent:true,nocheck:true,type:'district',children:[{id:'2140',name:'外滩',isParent:false,nocheck:false,type:'area'},{id:'2141',name:'人民广场',isParent:false,nocheck:false,type:'area'},{id:'2142',name:'南京东路',isParent:false,nocheck:false,type:'area'},{id:'2143',name:'城隍庙',isParent:false,nocheck:false,type:'area'},{id:'2144',name:'老西门',isParent:false,nocheck:false,type:'area'},{id:'2145',name:'董家渡',isParent:false,nocheck:false,type:'area'},{id:'2295',name:'黄浦其他',isParent:false,nocheck:false,type:'area'}]},
                         {id:'1258',name:'徐汇区',isParent:true,nocheck:true,type:'district',children:[{id:'2146',name:'徐家汇',isParent:false,nocheck:false,type:'area'},{id:'2147',name:'万体馆',isParent:false,nocheck:false,type:'area'},{id:'2148',name:'衡山路',isParent:false,nocheck:false,type:'area'},{id:'2149',name:'肇嘉浜路沿线',isParent:false,nocheck:false,type:'area'},{id:'2150',name:'龙华',isParent:false,nocheck:false,type:'area'},{id:'2151',name:'漕河泾',isParent:false,nocheck:false,type:'area'},{id:'2152',name:'上海南站',isParent:false,nocheck:false,type:'area'},{id:'2296',name:'徐汇其他',isParent:false,nocheck:false,type:'area'},{id:'3357',name:'复兴西路',isParent:false,nocheck:false,type:'area'},{id:'3786',name:'音乐学院',isParent:false,nocheck:false,type:'area'}]},
                         {id:'1259',name:'长宁区',isParent:true,nocheck:true,type:'district',children:[{id:'2162',name:'虹桥',isParent:false,nocheck:false,type:'area'},{id:'2163',name:'天山',isParent:false,nocheck:false,type:'area'},{id:'2164',name:'古北',isParent:false,nocheck:false,type:'area'},{id:'2165',name:'中山公园',isParent:false,nocheck:false,type:'area'},{id:'2166',name:'新华路',isParent:false,nocheck:false,type:'area'},{id:'2167',name:'北新泾',isParent:false,nocheck:false,type:'area'},{id:'2297',name:'长宁其他',isParent:false,nocheck:false,type:'area'},{id:'3359',name:'虹桥机场',isParent:false,nocheck:false,type:'area'}]},
                         {id:'1260',name:'静安区',isParent:true,nocheck:true,type:'district',children:[{id:'2200',name:'南京西路',isParent:false,nocheck:false,type:'area'},{id:'2201',name:'静安寺',isParent:false,nocheck:false,type:'area'},{id:'2204',name:'曹家渡',isParent:false,nocheck:false,type:'area'},{id:'2205',name:'同乐坊',isParent:false,nocheck:false,type:'area'},{id:'2298',name:'静安其他',isParent:false,nocheck:false,type:'area'}]},
                         {id:'1261',name:'普陀区',isParent:true,nocheck:true,type:'district',children:[{id:'2169',name:'真如',isParent:false,nocheck:false,type:'area'},{id:'2170',name:'武宁地区',isParent:false,nocheck:false,type:'area'},{id:'2171',name:'长寿路',isParent:false,nocheck:false,type:'area'},{id:'2172',name:'华师大',isParent:false,nocheck:false,type:'area'},{id:'2173',name:'曹阳地区',isParent:false,nocheck:false,type:'area'},{id:'2174',name:'梅川路',isParent:false,nocheck:false,type:'area'},{id:'2175',name:'中山北路',isParent:false,nocheck:false,type:'area'},{id:'2176',name:'曹家渡',isParent:false,nocheck:false,type:'area'},{id:'2299',name:'普陀其他',isParent:false,nocheck:false,type:'area'},{id:'3787',name:'曹杨地区',isParent:false,nocheck:false,type:'area'}]},
                         {id:'1262',name:'闸北区',isParent:true,nocheck:true,type:'district',children:[{id:'2195',name:'大宁地区',isParent:false,nocheck:false,type:'area'},{id:'2196',name:'彭浦新村',isParent:false,nocheck:false,type:'area'},{id:'2197',name:'闸北公园',isParent:false,nocheck:false,type:'area'},{id:'2199',name:'北区汽车站',isParent:false,nocheck:false,type:'area'},{id:'2300',name:'闸北其他',isParent:false,nocheck:false,type:'area'},{id:'3360',name:'火车站',isParent:false,nocheck:false,type:'area'}]},
                         {id:'1263',name:'虹口区',isParent:true,nocheck:true,type:'district',children:[{id:'2183',name:'曲阳地区',isParent:false,nocheck:false,type:'area'},{id:'2184',name:'海宁路',isParent:false,nocheck:false,type:'area'},{id:'2185',name:'七浦路',isParent:false,nocheck:false,type:'area'},{id:'2186',name:'临平路',isParent:false,nocheck:false,type:'area'},{id:'2187',name:'江湾镇',isParent:false,nocheck:false,type:'area'},{id:'2188',name:'鲁迅公园',isParent:false,nocheck:false,type:'area'},{id:'2301',name:'虹口其他',isParent:false,nocheck:false,type:'area'},{id:'3361',name:'四川北路',isParent:false,nocheck:false,type:'area'},{id:'3790',name:'北外滩',isParent:false,nocheck:false,type:'area'}]},
                         {id:'1264',name:'杨浦区',isParent:true,nocheck:true,type:'district',children:[{id:'2177',name:'五角场',isParent:false,nocheck:false,type:'area'},{id:'2178',name:'大学区',isParent:false,nocheck:false,type:'area'},{id:'2180',name:'控江地区',isParent:false,nocheck:false,type:'area'},{id:'2181',name:'平凉路',isParent:false,nocheck:false,type:'area'},{id:'2182',name:'黄兴公园',isParent:false,nocheck:false,type:'area'},{id:'2302',name:'杨浦其他',isParent:false,nocheck:false,type:'area'},{id:'3572',name:'中原地区',isParent:false,nocheck:false,type:'area'}]},
                         {id:'1265',name:'闵行区',isParent:true,nocheck:true,type:'district',children:[{id:'2153',name:'虹桥镇',isParent:false,nocheck:false,type:'area'},{id:'2154',name:'虹梅路',isParent:false,nocheck:false,type:'area'},{id:'2155',name:'七宝',isParent:false,nocheck:false,type:'area'},{id:'2156',name:'莘庄',isParent:false,nocheck:false,type:'area'},{id:'2157',name:'南方商城',isParent:false,nocheck:false,type:'area'},{id:'2158',name:'春申地区',isParent:false,nocheck:false,type:'area'},{id:'2159',name:'老闵行',isParent:false,nocheck:false,type:'area'},{id:'2160',name:'东兰路',isParent:false,nocheck:false,type:'area'},{id:'2161',name:'龙柏地区',isParent:false,nocheck:false,type:'area'},{id:'2313',name:'闵行其他',isParent:false,nocheck:false,type:'area'}]},
                         {id:'1266',name:'宝山区',isParent:true,nocheck:true,type:'district',children:[{id:'2206',name:'大华地区',isParent:false,nocheck:false,type:'area'},{id:'2207',name:'吴淞',isParent:false,nocheck:false,type:'area'},{id:'2208',name:'庙行镇',isParent:false,nocheck:false,type:'area'},{id:'2209',name:'上海大学',isParent:false,nocheck:false,type:'area'},{id:'2304',name:'宝山其他',isParent:false,nocheck:false,type:'area'}]},
                         {id:'1267',name:'嘉定区',isParent:true,nocheck:true,type:'district',children:[{id:'2213',name:'南翔',isParent:false,nocheck:false,type:'area'},{id:'2214',name:'安亭',isParent:false,nocheck:false,type:'area'},{id:'2215',name:'嘉定镇',isParent:false,nocheck:false,type:'area'},{id:'2216',name:'江桥',isParent:false,nocheck:false,type:'area'},{id:'2305',name:'嘉定其他',isParent:false,nocheck:false,type:'area'}]},
                         {id:'1268',name:'浦东新区',isParent:true,nocheck:true,type:'district',children:[{id:'2124',name:'陆家嘴',isParent:false,nocheck:false,type:'area'},{id:'2125',name:'八佰伴',isParent:false,nocheck:false,type:'area'},{id:'2128',name:'世纪公园',isParent:false,nocheck:false,type:'area'},{id:'2129',name:'外高桥',isParent:false,nocheck:false,type:'area'},{id:'2130',name:'金桥',isParent:false,nocheck:false,type:'area'},{id:'2131',name:'张江',isParent:false,nocheck:false,type:'area'},{id:'2132',name:'塘桥',isParent:false,nocheck:false,type:'area'},{id:'2134',name:'川沙',isParent:false,nocheck:false,type:'area'},{id:'2135',name:'三林镇',isParent:false,nocheck:false,type:'area'},{id:'2136',name:'金杨地区',isParent:false,nocheck:false,type:'area'},{id:'2137',name:'周浦',isParent:false,nocheck:false,type:'area'},{id:'2138',name:'惠南镇',isParent:false,nocheck:false,type:'area'},{id:'2139',name:'源深体育中心',isParent:false,nocheck:false,type:'area'},{id:'2306',name:'浦东新其他',isParent:false,nocheck:false,type:'area'},{id:'3362',name:'上南地区',isParent:false,nocheck:false,type:'area'},{id:'3789',name:'碧云社区',isParent:false,nocheck:false,type:'area'}]},
                         {id:'1269',name:'金山区',isParent:true,nocheck:true,type:'district',children:[{id:'2218',name:'其他',isParent:false,nocheck:false,type:'area'},{id:'2307',name:'金山其他',isParent:false,nocheck:false,type:'area'}]},
                         {id:'1270',name:'松江区',isParent:true,nocheck:true,type:'district',children:[{id:'2210',name:'松江镇',isParent:false,nocheck:false,type:'area'},{id:'2211',name:'松江大学城',isParent:false,nocheck:false,type:'area'},{id:'2212',name:'九亭',isParent:false,nocheck:false,type:'area'},{id:'2308',name:'松江其他',isParent:false,nocheck:false,type:'area'},{id:'2309',name:'青浦其他',isParent:false,nocheck:false,type:'area'},{id:'3788',name:'佘山',isParent:false,nocheck:false,type:'area'}]},
                         {id:'1271',name:'青浦区',isParent:true,nocheck:true,type:'district',children:[{id:'2217',name:'朱家角',isParent:false,nocheck:false,type:'area'},{id:'3770',name:'东方绿洲',isParent:false,nocheck:false,type:'area'}]},
                         {id:'1272',name:'奉贤区',isParent:true,nocheck:true,type:'district',children:[{id:'2219',name:'其他',isParent:false,nocheck:false,type:'area'}]},
                         {id:'1273',name:'崇明县',isParent:true,nocheck:true,type:'district',children:[{id:'2168',name:'其他',isParent:false,nocheck:false,type:'area'}]},
                         {id:'2189',name:'卢湾区',isParent:true,nocheck:true,type:'district',children:[{id:'2190',name:'新天地',isParent:false,nocheck:false,type:'area'},{id:'2191',name:'打浦桥',isParent:false,nocheck:false,type:'area'},{id:'2192',name:'淮海路',isParent:false,nocheck:false,type:'area'},{id:'2193',name:'瑞金宾馆区',isParent:false,nocheck:false,type:'area'},{id:'2310',name:'卢湾其他',isParent:false,nocheck:false,type:'area'}]},
                         {id:'2225',name:'其他',isParent:true,nocheck:true,type:'district',children:[{id:'2226',name:'其他',isParent:false,nocheck:false,type:'area'}]}
                     ]

        }
];
