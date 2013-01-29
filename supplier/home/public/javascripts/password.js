function ChangeQD() {
    var pwQiangdu = document.getElementById("pwQiangdu");//强度内容框
    var password = document.getElementById("encryptedPassword");
    var ls = f_CalcPwdRank(password.value);
    //pwQiangdu.innerHTML="强度"+ls;
    switch (ls) {
        case 0:    //不显示
        case 1:    //弱
        case 2:    //中
        case 3:    //强
            showPwRank(ls);
            break;
        default:
            showPwRank(3);
    }
}
//密码强度检测函数
function f_CalcPwdRank(l_Content) {
    if (l_Content.length < 6 || /^[0-9]{1,8}$/.test(l_Content)) {
        showPwRank(0);
        return 0;
    }
    var ls = 0;
    if (l_Content.match(/[a-z]/g)) {
        ls++;
    }
    if (l_Content.match(/[A-Z]/g)) {
        ls++;
    }
    if (l_Content.match(/[0-9]/g)) {
        ls++;
    }
    if (l_Content.match(/[^a-zA-Z0-9]/g)) {
        ls++;
    }
    if (l_Content.length < 8 && ls > 1) {
        ls = 1;
    }
    if (ls > 3) {
        ls = 3;
    }
    return ls;
}
//----显示强度状态
function showPwRank(pwRank) {
    //js<a href="/Web/Wytx/" target="_blank" class="innerlink">特效</a>代码：/<a href="/Web/Rjkf/" class="innerlink">软件</a>开发
    var obj = document.getElementById("pwQiangdu");
    var qdbox = document.getElementById("qdbox");
    var password = document.getElementById("encryptedPassword")//密码输入框
    switch (pwRank) {
        case 0:
            qdbox.className = "qdbox_hongse";
            obj.className = "qdhongse";
            obj.innerHTML = "很弱";
            if (password.value.length < 1) {
                qdbox.className = "qdbox_hongse setClose";
            } else {
                qdbox.className = "qdbox_hongse setShow";
            }
            break;
        case 1:
            qdbox.className = "qdbox_cengse";
            obj.innerHTML = "一般";
            obj.className = "qdcengse";
            break;
        case 2:
            qdbox.className = "qdbox_lanse";
            obj.innerHTML = "良好";
            obj.className = "qdlanse";
            break;
        case 3:
            qdbox.className = "qdbox_luse";
            obj.innerHTML = "超强";
            obj.className = "qdluse";
            break;
    }
}
