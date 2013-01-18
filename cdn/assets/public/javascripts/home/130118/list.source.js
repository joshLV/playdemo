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

    (new GoTop()).init({
        pageWidth           :960,
        nodeId              :'go-top',
        nodeWidth           :24,
        distanceToBottom    :100,
        distanceToPage      :10,
        hideRegionHeight    :130,
        text                :''
    });
});
