$(function($) {
    $('#slides').slides({
        play:5000,
        pause:2500,
        slideSpeed:500,
        hoverPause:true
    });

    $('.tab-hd a').mouseover(function(){
        $('.tab-hd .active').removeClass('active');
        $(this).addClass('active');
        var i = $('.tab-hd a').index($(this));
        $('.tab-bd .goods').hide();
        $($('.tab-bd .goods')[i]).show();
    });

    // $(".accordion dt:first").hide();
    // $(".accordion dd:not(:first)").hide();
    $(".accordion dt").mouseover(function () {
        $(this).next("dd").slideToggle(0).siblings("dd:visible").slideUp(0);
        $(this).slideToggle(0);
        $(this).siblings("dt:hidden").slideDown(0);
    });

    $('.goods').delegate('li', 'mouseover', function(){
        $(this).find('.region').addClass('hover');
    })
    .delegate('li', 'mouseout', function(){
        $(this).find('.region').removeClass('hover');
    });
});
