$(function($) {
    // 焦点图
    $('#slides').slides({
        play:5000,
        pause:2500,
        slideSpeed:500,
        hoverPause:true
    });

    // tab切换
    $('.tab-hd a').mouseover(function(){
        $('.tab-hd .active').removeClass('active');
        $(this).addClass('active');
        var i = $('.tab-hd a').index($(this));
        $('.tab-bd .goods').hide();
        $($('.tab-bd .goods')[i]).show();
    });

    // 手风琴
    $(".accordion dt").mouseover(function () {
        $(this).next("dd").slideToggle(0).siblings("dd:visible").slideUp(0);
        $(this).slideToggle(0);
        $(this).siblings("dt:hidden").slideDown(0);
    });

    // 商圈
    $('.goods').delegate('li', 'mouseover', function(){
        $(this).find('.region').addClass('hover');
    })
    .delegate('li', 'mouseout', function(){
        $(this).find('.region').removeClass('hover');
    });

    // 回顶部
    (new GoTop()).init({
        pageWidth           :960,
        nodeId              :'go-top',
        nodeWidth           :24,
        distanceToBottom    :100,
        distanceToPage      :10,
        hideRegionHeight    :130,
        text                :''
    });

    // 友情链接滚动
    var _wrap=$('#j_links'),    //定义滚动区域
        _interval=5000,         //定义滚动间隙时间
        _moving;                //需要清除的动画
    _wrap.hover(function(){
        clearInterval(_moving); //当鼠标在滚动区域中时,停止滚动
    },function(){
        _moving=setInterval(function(){
            var _field=_wrap.find('li:first'),  //此变量不可放置于函数起始处,li:first取值是变化的
                _h=_field.height(); //取得每次滚动高度(多行滚动情况下,此变量不可置于开始处,否则会有间隔时长延时)
            _field.animate({marginTop:-_h+'px'},600,function(){ //通过取负margin值,隐藏第一行
                _field.css('marginTop',0).appendTo(_wrap);  //隐藏后,将该行的margin值置零,并插入到最后,实现无缝滚动
            })
        },_interval)    //滚动间隔时间取决于_interval
    }).trigger('mouseleave');   //函数载入时,模拟执行mouseleave,即自动滚动
    

});
