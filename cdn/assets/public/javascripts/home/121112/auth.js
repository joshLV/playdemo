jQuery(function ($) {
    var win = $(window),
        winWidth = win.width(),
        winHeight = win.height(),
        docHeight = $('body').height(),
    // trigger = !!trigger ? $('.'+ trigger) : $('.j_auth'), // 默认class为j_auth
        trigger = $('.j_auth');

    trigger.click(function (e) {
        e.preventDefault();

        var isRegister = $(this).hasClass('signup'),
            iframeSrc = !isRegister ? '../single/minLogin.php' : '/register?embed=true';

        // 插入遮罩层
        if ($('#auth_mask').length == 0) {
            $('body').append('<div id="auth_mask" style="position:absolute;top:0;left:0;background-color:#000;opacity:.25;filter:alpha(opacity=25);z-index:9998;"></div>');
            $('#auth_mask').css({
                'width':winWidth,
                'height':docHeight < winHeight ? winHeight : docHeight
            });
        } else {
            $('#auth_mask').show();
        }

        // 插入浮层
        if ($('#auth_box').length == 0) {
            $('body').append('<div id="auth_box" style="position:absolute;z-index:9999;"><a id="close-auth" style="position: absolute; top: 14px; right: 11px; width: 16px; height: 16px; background: url(//img.uhcdn.com/images/y/close.png) no-repeat 0 0;" href="javascript:void(0)" title="关闭"></a><iframe scrolling="no" allowtransparency="true" marginwidth="0" marginheight="0" frameborder="0" border="0" width="425" height="405" style="overflow:hidden;" src="' + iframeSrc + '"></iframe></div>');

            // 只在第一次插入浮层时，绑定关闭事件
            $('#close-auth').click(function () {
                $('#auth_mask').hide();
                $('#auth_box').hide();
            });

        } else {
            $('#auth_box').show();
        }

        // 居中浮层
        var ypos = winHeight / 2 - 205 + $(document).scrollTop(),
            xpos = winWidth / 2 - 212;
        $('#auth_box').css({
            'top':ypos > 0 ? ypos : 0,
            'left':xpos > 0 ? xpos : 0
        });
    });
});