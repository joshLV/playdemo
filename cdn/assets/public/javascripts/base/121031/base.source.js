jQuery(document).ready(function(){
    // 加入收藏夹 {{{
    var addToFavorite = function(url, title) {
        try {
            window.external.AddFavorite(url, title);
        } catch(e) {
            try {
                window.sidebar.addPanel(title, url, '');
            } catch(e) {
                alert('对不起，您的浏览器不支持自动收藏，请使用Ctrl+D进行手动收藏！');
            }
        }
    };
    $('#favorite').click(function(evt) {
        evt.preventDefault();
        var url = window.location,
            title = document.title;
        addToFavorite(url, title);
    });
    // }}}

    // minCarts {{{
    var cart = $('#cart'),
        cartBox = $('#cart .cart-box'),
        cartBoxBd = $('#cart .cart-box-bd');

    cart.mouseover(function(){
        if ($('#reload').val() == 'true') {
            cartBoxBd.load("/carts/tops", function(data) {
                cartBox.addClass('hover');
                $('#reload').val(false);
            });
        } else {
            cartBox.addClass('hover');
        }
    })
    .mouseout(function(){
        cartBox.removeClass('hover');
    });

    $('.goods-del').live('click', function (ev) {
        ev.preventDefault();
        var buyCount = parseInt($(this).attr("count")),
            goods_id = $(this).attr("goods_id"),
            sale_price = $(this).attr("sale_price"),
            all_number = parseInt($("#all_number").html()),
            all_price = $("#all_price").html();
        $.ajax({
            type:'DELETE',
            url:'/carts/' + goods_id,
            success:function (data) {
                var oldSize = parseInt($("#cart-count").html());
                var currentCount = oldSize - buyCount;
                $("#cart-count").html(currentCount);
                if (currentCount == 0) {
                    cartBoxBd.html('<div class="emptycart">你的购物车里没有任何商品。</div><div class="totalcart">共<em>0</em>件商品，共计<em>0.00</em>元</div>');
                } else {
                    $("#row_" + goods_id).animate({height: '0'}, 100, function(){
                        $(this).remove();
                    });
                    $("#all_number").html(all_number - buyCount);
                    $("#all_price").html((all_price - (sale_price * buyCount)).toFixed(2));
                }
            }
        });
    });
    // }}}

    var cate = $('#cate'),
        item = $('#cate .item'),
        line = $('#cate .line'),
        more = $('#cate .more');
    item.live('mouseover', function(){
        var that = $(this);
        item.removeClass('active');
        that.addClass('active');
        more.hide();
        that.children('.more').show();
        line.hide();
        that.children('.line').show().height(that.height() - 2);
    })
    .live('mouseout', function(){
        $(this).removeClass('active');
        more.hide();
        line.hide();
    });

    // item.delegate('.item', 'mouseover', function(){
        // var that = $(this);
        // item.find('.active').removeClass('active');
        // that.addClass('active');
        // more.hide();
        // that.children('.more').show();
        // line.hide();
        // that.children('.line').show().height(that.height() - 2);
    // })
    // .delegate('.item', 'mouseout', function(){
        // $(this).removeClass('active');
        // more.hide();
        // line.hide();
    // });


    // item.on('mouseover', '.item', function(){
        // var that = $(this);
        // item.find('.active').removeClass('active');
        // that.addClass('active');
        // more.hide();
        // that.children('.more').show();
        // line.hide();
        // that.children('.line').show().height(that.height() - 2);
    // })
    // .on('mouseout', '.item', function(){
        // $(this).removeClass('active');
        // more.hide();
        // line.hide();
    // });

    !$('body.home').length && $('#cate').mouseover(function(){
        $('#cate .menu').show();
    }).mouseout(function(){
        $('#cate .menu').hide();
    });
});

// (function($){
    // var goToTopTime;
    // $.fn.goToTop=function(options){
        // var opts = $.extend({},$.fn.goToTop.def,options);
        // var $window=$(window);
        // $body = (window.opera) ? (document.compatMode == "CSS1Compat" ? $('html') : $('body')) : $('html,body'); // opera fix
        // $(this).hide();
        // var $this=$(this);
        // clearTimeout(goToTopTime);
        // goToTopTime=setTimeout(function(){
            // var controlLeft;
            // if ($window.width() > opts.pageHeightJg * 2 + opts.pageWidth) {
                // controlLeft = ($window.width() - opts.pageWidth) / 2 + opts.pageWidth + opts.pageWidthJg;
            // }else{
                // controlLeft = $window.width()- opts.pageWidthJg-$this.width();
            // }
            // var cssfixedsupport=$.browser.msie && parseFloat($.browser.version) < 7;//判断是否ie6
            // var controlTop=$window.height() - $this.height()-opts.pageHeightJg;
            // controlTop=cssfixedsupport ? $window.scrollTop() + controlTop : controlTop;
            // var shouldvisible=( $window.scrollTop() >= opts.startline )? true : false;
            
            // if (shouldvisible){
                // $this.stop().show(opts.showBtntime);
            // }else{
                // $this.stop().hide(opts.showBtntime);
            // }
            
            // $this.css({
                // position: cssfixedsupport ? 'absolute' : 'fixed',
                // top: controlTop,
                // left: controlLeft
            // });
        // },500);
        
        // $(this).click(function(event){
            // $body.stop().animate( { scrollTop: $(opts.targetObg).offset().top}, opts.duration);
            // $(this).blur();
            // event.preventDefault();
            // event.stopPropagation();
        // });
    // };
    
    // $.fn.goToTop.def={
        // pageWidth:960,//页面宽度
        // pageWidthJg:10,//按钮和页面的间隔距离
        // pageHeightJg:210,//按钮和页面底部的间隔距离
        // startline:200,//出现回到顶部按钮的滚动条scrollTop距离
        // duration:200,//回到顶部的速度时间
        // showBtntime:1,//显示\隐藏回到顶部按钮的速度时间
        // targetObg:"body"//目标位置
    // };
// })(jQuery);
// $(function(){
    // $('<a href="#" class="go-top"><img src="http://img.uhcdn.com/base/go-top.png" alt="" /></a>').appendTo("body");
    // $(".go-top").goToTop({});
    // $(window).bind('scroll resize',function(){
        // $(".go-top").goToTop({});
    // });
// });


/**
 * Copyright (c) 2007-2012 Ariel Flesler - aflesler(at)gmail(dot)com | http://flesler.blogspot.com
 * Dual licensed under MIT and GPL.
 * @author Ariel Flesler
 * @version 1.4.3.1
;(function($){var h=$.scrollTo=function(a,b,c){$(window).scrollTo(a,b,c)};h.defaults={axis:'xy',duration:parseFloat($.fn.jquery)>=1.3?0:1,limit:true};h.window=function(a){return $(window)._scrollable()};$.fn._scrollable=function(){return this.map(function(){var a=this,isWin=!a.nodeName||$.inArray(a.nodeName.toLowerCase(),['iframe','#document','html','body'])!=-1;if(!isWin)return a;var b=(a.contentWindow||a).document||a.ownerDocument||a;return/webkit/i.test(navigator.userAgent)||b.compatMode=='BackCompat'?b.body:b.documentElement})};$.fn.scrollTo=function(e,f,g){if(typeof f=='object'){g=f;f=0}if(typeof g=='function')g={onAfter:g};if(e=='max')e=9e9;g=$.extend({},h.defaults,g);f=f||g.duration;g.queue=g.queue&&g.axis.length>1;if(g.queue)f/=2;g.offset=both(g.offset);g.over=both(g.over);return this._scrollable().each(function(){if(e==null)return;var d=this,$elem=$(d),targ=e,toff,attr={},win=$elem.is('html,body');switch(typeof targ){case'number':case'string':if(/^([+-]=)?\d+(\.\d+)?(px|%)?$/.test(targ)){targ=both(targ);break}targ=$(targ,this);if(!targ.length)return;case'object':if(targ.is||targ.style)toff=(targ=$(targ)).offset()}$.each(g.axis.split(''),function(i,a){var b=a=='x'?'Left':'Top',pos=b.toLowerCase(),key='scroll'+b,old=d[key],max=h.max(d,a);if(toff){attr[key]=toff[pos]+(win?0:old-$elem.offset()[pos]);if(g.margin){attr[key]-=parseInt(targ.css('margin'+b))||0;attr[key]-=parseInt(targ.css('border'+b+'Width'))||0}attr[key]+=g.offset[pos]||0;if(g.over[pos])attr[key]+=targ[a=='x'?'width':'height']()*g.over[pos]}else{var c=targ[pos];attr[key]=c.slice&&c.slice(-1)=='%'?parseFloat(c)/100*max:c}if(g.limit&&/^\d+$/.test(attr[key]))attr[key]=attr[key]<=0?0:Math.min(attr[key],max);if(!i&&g.queue){if(old!=attr[key])animate(g.onAfterFirst);delete attr[key]}});animate(g.onAfter);function animate(a){$elem.animate(attr,f,g.easing,a&&function(){a.call(this,e,g)})}}).end()};h.max=function(a,b){var c=b=='x'?'Width':'Height',scroll='scroll'+c;if(!$(a).is('html,body'))return a[scroll]-$(a)[c.toLowerCase()]();var d='client'+c,html=a.ownerDocument.documentElement,body=a.ownerDocument.body;return Math.max(html[scroll],body[scroll])-Math.min(html[d],body[d])};function both(a){return typeof a=='object'?a:{top:a,left:a}}})(jQuery);
 */

/*
Author: mg12
Update: 2012/05/04
Author URI: http://www.neoease.com/
*/
GoTop = function() {

    this.config = {
        pageWidth           :960,        // 页面宽度
        nodeId              :'go-top',   // Go Top 节点的 ID
        nodeWidth           :50,         // Go Top 节点宽度
        distanceToBottom    :120,        // Go Top 节点上边到页面底部的距离
        distanceToPage      :20,         // Go Top 节点左边到页面右边的距离
        hideRegionHeight    :90,         // 隐藏节点区域的高度 (该区域从页面顶部开始)
        text                :''          // Go Top 的文本内容
    };

	this.cache = {
		topLinkThread		:null		 // 显示 Go Top 节点的线程变量 (用于 IE6)
	}
};

GoTop.prototype = {

	init: function(config) {
		this.config = config || this.config;
		var _self = this;

		// 滚动屏幕, 修改节点位置和显示状态
		jQuery(window).scroll(function() {
			_self._scrollScreen({_self:_self});
		});

		// 改变屏幕尺寸, 修改节点位置
		jQuery(window).resize(function() {
			_self._resizeWindow({_self:_self});
		});

		// 在页面中插入节点
		_self._insertNode({_self:_self});
	},

	/**
	 * 在页面中插入节点
	 */
	_insertNode: function(args) {
		var _self = args._self;

		// 插入节点并绑定节点事件, 当节点被点击, 用 0.4 秒的时间滚动到页面顶部
		var topLink = jQuery('<a id="' + _self.config.nodeId + '" href="#">' + _self.config.text + '</a>');
		topLink.appendTo(jQuery('body'));
		if(jQuery.scrollTo) {
			topLink.click(function() {
				jQuery.scrollTo({top:0}, 400);
				return false;
			});
		}

		// 节点到屏幕右边的距离
		var right = _self._getDistanceToBottom({_self:_self});

		// IE6 (不支持 position:fixed) 的样式
		if(/MSIE 6/i.test(navigator.userAgent)) {
			topLink.css({
				'display': 'none',
				'position': 'absolute',
				'right': right + 'px'
			});

		// 其他浏览器的样式
		} else {
			topLink.css({
				'display': 'none',
				'position': 'fixed',
				'right': right + 'px',
				'top': (jQuery(window).height() - _self.config.distanceToBottom) + 'px'
			});
		}
	},

	/**
	 * 修改节点位置和显示状态
	 */
	_scrollScreen: function(args) {
		var _self = args._self;

		// 当节点进入隐藏区域, 隐藏节点
		var topLink = jQuery('#' + _self.config.nodeId);
		if(jQuery(document).scrollTop() <= _self.config.hideRegionHeight) {
			clearTimeout(_self.cache.topLinkThread);
			topLink.hide();
			return;
		}

		// 在隐藏区域之外, IE6 中修改节点在页面中的位置, 并显示节点
		if(/MSIE 6/i.test(navigator.userAgent)) {
			clearTimeout(_self.cache.topLinkThread);
			topLink.hide();

			_self.cache.topLinkThread = setTimeout(function() {
				var top = jQuery(document).scrollTop() + jQuery(window).height() - _self.config.distanceToBottom;
				topLink.css({'top': top + 'px'}).fadeIn();
			}, 400);

		// 在隐藏区域之外, 其他浏览器显示节点
		} else {
			topLink.fadeIn();
		}
	},

	/**
	 * 修改节点位置
	 */
	_resizeWindow: function(args) {
		var _self = args._self;

		var topLink = jQuery('#' + _self.config.nodeId);

		// 节点到屏幕右边的距离
		var right = _self._getDistanceToBottom({_self:_self});

		// 节点到屏幕顶部的距离
		var top = jQuery(window).height() - _self.config.distanceToBottom;
		// IE6 中使用到页面顶部的距离取代
		if(/MSIE 6/i.test(navigator.userAgent)) {
			top += jQuery(document).scrollTop();
		}

		// 重定义节点位置
		topLink.css({
			'right': right + 'px',
			'top': top + 'px'
		});
	},

	/**
	 * 获取节点到屏幕右边的距离
	 */
	_getDistanceToBottom: function(args) {
		var _self = args._self;

		// 节点到屏幕右边的距离 = (屏幕宽度 - 页面宽度 + 1 "此处 1px 用于消除偏移" ) / 2 - 节点宽度 - 节点左边到页面右边的宽度 (20px), 如果到右边距离屏幕边界不小于 10px
		var right = parseInt((jQuery(window).width() - _self.config.pageWidth + 1)/2 - _self.config.nodeWidth - _self.config.distanceToPage, 10);
		if(right < 10) {
			right = 10;
		}

		return right;
	}
};
