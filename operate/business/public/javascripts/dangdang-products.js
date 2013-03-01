var categories = {
    "餐饮美食": "中餐、西餐、自助餐、亚洲美食、快餐、蛋糕甜品、其他",
    "休闲娱乐": "KTV、温泉/洗浴、酒吧、游艺/电玩、运动健身、其他",
    "电影演出": "电影票、演出、演唱会、话剧、音乐会、体育赛事、其他",
    "酒店":    "五星/豪华、四星/高档、三星/舒适、经济/客栈",
    "门票旅游": "旅游线路、景点门票、旅游卡劵、游船邮轮、租车、签证、高尔夫、其他",
    "美容保健": "美发、美甲、美容/spa、瘦身/纤体、保健按摩、体检、口腔、其他",
    "摄影写真": "婚纱摄影、儿童摄影、孕妇摄影、写真摄影、情侣摄影、全家福、证件照、彩扩冲印、其他",
    "教育培训": "语言培训、职业资质、学历教育、亲子培训、电脑培训、在线教学、其他",
    "综合服务": "汽车美容、宠物服务、家政服务、婚庆服务、验光配镜、其他"
}
function buildZTreeSource(categories) {
    var result = [];
    for(var key in categories) {
        var p = {};
        p.name = key;
        p.children =[];
        p.nocheck = true;
        var subCategories = categories[key].split('、');
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
    $.extend(treeCallbacks,getSinglePathCallbacks(treeId+"-tree"),getOnclickCallback(treeId))

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
        uploadJson:'/goods/images',
        filterMode:false,
        allowFileManager:false
    });
    //初始化分类tree
    $.fn.zTree.init($("#category-tree"), getTreeSettings("category"), buildZTreeSource(categories));
    $("#category-show").click(showTreeFunc('category'));

    $("#submit").click(function(){
        var categoryTree = $.fn.zTree.getZTreeObj('category-tree');
        var checkedNodes = categoryTree.getCheckedNodes(true)
        if (checkedNodes.length==0) {
            alert('请选择分类');return false;
        }
        $("#input-category_name1").val(checkedNodes[0].name);
        $("#input-category_name2").val(checkedNodes[0].getParentNode().name);

    });
});