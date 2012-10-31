jQuery(function($) {
    // 右边广告切换
    $('#switch').slides({
        play:4000,
        pause:2000,
        slideSpeed:300,
        hoverPause:true
    });
    $('.goods').delegate('li', 'mouseover', function(){
        $(this).find('.region').addClass('hover');
    })
    .delegate('li', 'mouseout', function(){
        $(this).find('.region').removeClass('hover');
    });
});
