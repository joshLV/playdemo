var categories = {
    "餐饮美食	": "自助餐/西餐/火锅/川菜/东北菜/湘菜/粤菜/江浙菜/私房菜/香锅烧烤/日本料理/韩国料理/快餐/蛋糕甜品/其他",
    "休闲娱乐	": "KTV/运动健身/温泉洗浴/酒吧/游艺电玩/其他",
    "电影演出": "电影/赛事/演唱会/话剧/演艺/其他",
    "美容保健	": "美发/美甲/美容美体/保健按摩/体检/口腔/其他",
    "摄影写真": "婚纱摄影/儿童摄影/孕妇摄影/个性写真/全家福/证件照/其他",
    "商旅/出游":"机票/火车票/邮轮/酒店客栈/旅游线路/景点门票/租车/签证/其他",
    "教育培训	": "外语/升学/考级/特长/亲子/ 职业教育/远程教育/其他",
    "本地超市": "瓜果蔬菜/海鲜肉类/粮油米面/酒水饮料/零食干货/日常用品/鲜花礼品/其他",
    "其他":    "汽车美容/宠物服务/家政服务/婚庆服务/预订服务/验光配镜/其他",
    "充值/游戏":"移动/联通/电信/游戏点卡/虚拟货币/装备交易	",
    "彩票":   "双色球/3D/大乐透/排列三/竞彩足球/竞彩篮球",
    "保险":   "车险/意外险/医疗险/旅游险/少儿险/财产险"
}
function buildZTreeSource(categories) {
    var result = [];
    for(var key in categories) {
        var p = {};
        p.name = key;
        p.children =[];
        p.nocheck = true;
        var subCategories = categories[key].split('/');
        for (var i = 0; i< subCategories.length; i ++) {
            var c = {};
            c.name = subCategories[i]
            p.children.push(c)
        }
        result.push(p)
    }
    return result;
}
function getTreeSettings(treeId) {
    var treeCallbacks = {}
    $.extend(treeCallbacks,getSinglePathCallbacks(treeId),getAutoExpandCallbacks(treeId))

    return {
        check:{
            enable:true,
            chkStyle:"radio",
            radioType:"all"
        },
        view: {
            showIcon: false,
            showLine: false,
            dblClickExpand: false
        },
        callback: treeCallbacks
    };
}

$(function(){
    $("#inputSrcImage").blur(function(){
        $("#imgSrcImage").attr("src",$(this).val());
    });

    KindEditor.create('textarea[name="detail"]',
    {
        filterMode:false,
        allowFileManager:false
    });
    //初始化分类tree
    $.fn.zTree.init($("#categoryTree"), getTreeSettings("categoryTree"), buildZTreeSource(categories));
});