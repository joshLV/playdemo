//给定一个ztree的id 返回这个tree所对应的 构造单链条节点所需的callback函数
//注：单链条 是指同一时间，一个tree只有一条路径是展开的
function getSinglePathCallbacks(treeId) {
    var curExpandNode = null;
    function beforeExpand(treeId, treeNode) {
        var pNode = curExpandNode ? curExpandNode.getParentNode():null;
        var treeNodeP = treeNode.parentTId ? treeNode.getParentNode():null;
        var zTree = $.fn.zTree.getZTreeObj(treeId);
        for(var i=0, l=!treeNodeP ? 0:treeNodeP.children.length; i<l; i++ ) {
            if (treeNode !== treeNodeP.children[i]) {
                zTree.expandNode(treeNodeP.children[i], false);
            }
        }
        while (pNode) {
            if (pNode === treeNode) {
                break;
            }
            pNode = pNode.getParentNode();
        }
        if (!pNode) {
            singlePath(treeNode);
        }

    }
    function singlePath(newNode) {
        if (newNode === curExpandNode) return;
        if (curExpandNode && curExpandNode.open==true) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            if (newNode.parentTId === curExpandNode.parentTId) {
                zTree.expandNode(curExpandNode, false);
            } else {
                var newParents = [];
                while (newNode) {
                    newNode = newNode.getParentNode();
                    if (newNode === curExpandNode) {
                        newParents = null;
                        break;
                    } else if (newNode) {
                        newParents.push(newNode);
                    }
                }
                if (newParents!=null) {
                    var oldNode = curExpandNode;
                    var oldParents = [];
                    while (oldNode) {
                        oldNode = oldNode.getParentNode();
                        if (oldNode) {
                            oldParents.push(oldNode);
                        }
                    }
                    if (newParents.length>0) {
                        zTree.expandNode(oldParents[Math.abs(oldParents.length-newParents.length)-1], false);
                    } else {
                        zTree.expandNode(oldParents[oldParents.length-1], false);
                    }
                }
            }
        }
        curExpandNode = newNode;
    }

    function onExpand(event, treeId, treeNode) {
        curExpandNode = treeNode;
    }

    return {
        beforeExpand: beforeExpand,
        onExpand: onExpand,
    }
};
//返回指定ztree所需要的单击自动展开callback函数
function getAutoExpandCallbacks(treeId) {
    function onClick(e,treeId, treeNode) {
        var zTree = $.fn.zTree.getZTreeObj(treeId);
        if(treeNode.isParent){
            zTree.expandNode(treeNode, null, null, null, true);
        }else{
            zTree.checkNode(treeNode);
        }
    }
    return {
        onClick: onClick
    }
}
/**
 * 返回点击时的callback函数,拥有以下功能
 * 选择父节点时自动展开，选择叶子节点时自动选中/取消选中 radio/checkbox
 * 自动更新显示给用户的输入框的值
 * 自动更新隐藏input的值
 * 自动更新用来保存选择路径（从root到所选节点的每一个节点）的隐藏input的值
**/
function getOnclickCallback(id) {
    function onClick(e,treeId, treeNode) {
        var zTree = $.fn.zTree.getZTreeObj(treeId);
        if(treeNode.isParent){
            zTree.expandNode(treeNode, null, null, null, true);
        }else{
            if (treeNode.checked) {
                //点击了checkbox
                zTree.selectNode(treeNode);
            }else{
                //选择文字或者取消选择（撤销取消选择的行为）
                zTree.checkNode(treeNode);
            }
            var checkedNodes = zTree.getCheckedNodes();
            $("#" + id + "-show").val(buildCheckedStr(checkedNodes, ",", "name"))
            $("#" + id + "-value").val(buildCheckedStr(checkedNodes, ",", "id"))
            var nodeChain = $("#" + id + "-nodeChain");
            if (nodeChain) {
                nodeChain.val(buildTreeChain(checkedNodes));
            }
        }
    }
    return {
        onCheck: onClick,
        onClick: onClick
    }
}

function showTreeFunc(id) {
    var inputShow = $("#" + id + "-show");
    var treeFrame = $("#" + id + "-treeFrame");
    function showTree() {
        var inputShowOffset = inputShow.offset();
        treeFrame.css( {
            left:inputShowOffset.left + "px",
            top:inputShowOffset.top + inputShow.outerHeight() + "px"})
        .slideDown("fast");

        $("body").bind("mousedown", onBodyDown);
    }
    function hideTree() {
        treeFrame.fadeOut("fast");
        $("body").unbind("mousedown", onBodyDown);
    }
    function onBodyDown(event) {
        if (!(event.target.id == (id +"-treeFrame") || $(event.target).parents("#" + id + "-treeFrame").length>0)) {
            hideTree();
        }
    }
    return showTree;
}

//传入一个node,和一个存储节点链表的数组
function insertParentToChain(node, chain) {
    if (!node.parentTId) return;
    var parentNode = node.getParentNode()
    var nodeIndex = chain.indexOf(node.id)
    chain.splice(nodeIndex,0,parentNode.id)
    insertParentToChain(parentNode, chain)
}

function buildTreeChain(nodes) {
    var chain = [];
    for(var i = 0; i < nodes.length; i ++ ) {
        chain[chain.length] = nodes[i].id;
        insertParentToChain(nodes[i], chain);
    }
    return chain;
}

function buildCheckedStr(nodes, sep, property) {
    var str = "";
    for(var i = 0; i < nodes.length; i ++ ) {
        if (i != 0) {
            str += sep;
        }
        str += nodes[i][property];
    }
    return str;
}

