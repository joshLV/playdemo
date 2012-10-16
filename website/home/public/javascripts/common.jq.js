;(function($){
var ie = navigator.userAgent.toLowerCase().match(/msie ([\d.]+)/);

/**Drag end */
$.common = {
	 //居中显示
	 center:function(obj){
		 center(obj);
	 },
	 //拖拽移动
	 drag:function(dragObj,controlObj){
		 f = new Drag();
	     f.register(dragObj,controlObj);
	 },
	 alert:function(opts){		 
		 opts = $.extend({}, $.popup.defaults,opts || {});
		 opts.title = "系统提示信息";
		 $.popup.alert(opts);
	 },
	 confirm:function(opts){
		 opts = $.extend({}, $.popup.defaults,opts || {});
		 opts.title = "系统提示信息";
		 $.popup.confirm(opts);
	 },
	 dialog:function(opts){		 
		 opts = $.extend({}, $.popup.defaults,opts || {});
		 if (opts.title == '' || typeof opts.title == 'undefined') {
			opts.title = "系统提示信息";
		 }
		 if (typeof opts.content == 'undefined') {
			 opts.content = ' ';
		 }
		 $.popup.dialog(opts);
	 },
	 win:function(opts){
		$.AjaxWin.show(opts);
	 },
	 loading:{
		show:function(opts){
			opts = $.extend({}, $.popup.defaults,opts || {});
			var autotime = opts.autotime;
			if(opts.auto) setTimeout(function(){
				var popup_opts = {step:8,time:3,end:0,
					callback:function(){
						$.popup.hide();
					}
				}
			    $('#popup_container_id').unopacity(popup_opts);
			},opts.autotime);
			opts.css.width   = "300px";
			opts.css.height  = "45px";
		    $.popup.hide();
			$.popup.loading(opts);
		},
		hide:function(){
		    var popup_opts = {step:8,time:3,end:0,
				callback:function(){
					$.popup.hide();
				}
		    }
			$('#popup_container_id').unopacity(popup_opts);
		}
	 }
}

$.popup = {
	alert:function(opts){
		Alert(opts);
	},
	loading:function(opts){
		loading(opts);
	},
	confirm:function(opts){
		Confirm(opts);
	},
	dialog:function(opts){
		Dialog(opts);
	},
	hide:function(){
		$('#popupbox').remove();
		$('#popup_overlay').remove();
	},
	defaults:{
		title:"自动义标题",
		message:'',
		certain:"确定",
		cancel:"取消",
		auto:false,
		autotime:777,
		css:{ 
			zIndex:2003,
			opacity:1.0,
			width:'270px',
			height:"120px"
		}
	}
}
//遮罩层
$.overlay = {
	show:function(overlayCss){
		$.overlay.hide();
		overlayCss = $.extend({}, $.overlay.defaults,overlayCss || {});
		var _popup_overlay = $('<div id="popup_overlay" class="popup_overlay"></div>');
		$("body").append(_popup_overlay);
		$(_popup_overlay).css(overlayCss);
		$.overlay.resize();

		var iframe_panel = create_iframe_panel($(_popup_overlay).outerWidth(),$(_popup_overlay).outerHeight());
		$(iframe_panel).css("opacity",'0.0')
		$(_popup_overlay).append(iframe_panel);
		$(window).bind("resize",function(){$.overlay.resize();});
		$(window).bind("scroll",function(){$.overlay.resize();});
	},
	hide:function(){
		$(window).bind("resize",function(){});
		$(window).bind("scroll",function(){});
	},
	resize:function(){
        var doc = $(document.documentElement)[0];
	    $("#popup_overlay").css({
				width:(doc.scrollLeft==0?doc.clientWidth:doc.scrollWidth)+"px",
				// height:(doc.scrollTop==0?doc.clientHeight:doc.scrollHeight)+"px"
				height:(doc.scrollTop==0?$(document).height():doc.scrollHeight)+"px"
		});
	},
	defaults:{
		opacity: 0.5,
		width: '100%',
		top: '0px',
		left: '0px',
		zIndex: 2000
    }
};
// 透明渐变
$.fn.opacity = function(opts){
	 var timer = null;
	 opts = $.extend({
         step:10,//步长
	     time:50,//变化时间间隔
	     end:100,
	     callback:function(){} 
     },opts || {});
	 return this.each(function(){
		 clearTimeout(timer);   
		 var opacity_obj = $(this);
		 //获取当前渐变值 百分比
	     var _cur_opacity = $(opacity_obj).css("opacity") * 100;
	     if(_cur_opacity >=opts.end){
		     if(opts.callback) opts.callback(true);
			 return false;
	     }else{
		     _cur_opacity = (_cur_opacity + opts.step) >=opts.end ? opts.end:(_cur_opacity + opts.step);
		     $(opacity_obj).css("opacity",_cur_opacity/100);
		     timer = setTimeout(function(){$(opacity_obj).opacity(opts);},opts.time);
	     }					   
     })
}
//透明渐变 非透明 - 透明
$.fn.unopacity = function(opts){
	 var timer = null;
	 opts = $.extend({
         step:10,//步长
	     time:50,//变化时间间隔
	     end:0,
	     callback:function(){} 
     },opts || {});
	 return this.each(function(){
		 var opacity_obj = $(this);
		 clearTimeout(timer);   
		 //获取当前渐变值 换成百分制
	     var _cur_opacity = $(opacity_obj).css("opacity") * 100;
	     if(_cur_opacity <= opts.end){
			 if(opts.callback) opts.callback(true);
			 return false;
	     }else{
		     _cur_opacity = (_cur_opacity - opts.step) <= opts.end ? opts.end:(_cur_opacity - opts.step);
			 var opacity  = _cur_opacity/100;
			 $(opacity_obj).css("opacity",opacity);
		     timer = setTimeout(function(){$(opacity_obj).unopacity(opts);},opts.time);
	     }					   
     })
}

/**
*一百券弹出框
*/
function Dialog(opts) {
	$.popup.hide();
	//遮罩层
	$.overlay.show({});
	
	//提示信息容器
	var _popup_content = $("#" + opts.id);
	
	//提示信息容器
	var _popup_container = $('<div id="popupbox" class="popup-container"></div>');
	$('body').append($(_popup_container));
	$(_popup_content).show();
	var strhtml = _popup_content.html();
	
	var outerHeight = $("#" + opts.id + " :first").outerHeight();
	var outerWidth = $("#" + opts.id + " :first").outerWidth();
	
	if (parseInt(outerHeight) == 0 ) {
		outerHeight = parseInt(opts.height);
	}
	
	if (parseInt(outerWidth) == 0 ) {
		outerWidth = parseInt(opts.width);
	}
	
	_popup_content.html("");
	//将结构体封装在这个变量中
	var indiv = "<div class='box'>"
        +"  <div class='uhlbox_t clearfix'><b>"+ opts.title +"</b><a href='javascript:void(0);' class='close' id='popup_close' title='关闭'>X</a></div>"
        +"  <div class='uhlbox_c'>"+ opts.content +"</div>"
        +"</div>";

	$(_popup_container).html(indiv);

	$("#popupbox .uhlbox_c").html(strhtml);
	
	// $(_popup_container).css({"height":outerHeight + 67,"width":outerWidth,"zIndex":"2000"});
	$(_popup_container).css({"width":outerWidth,"zIndex":"2000"});
	$.common.center($(_popup_container));
	$(_popup_container).show();
	
	$("#popup_close").click(function(){
		$.popup.hide();
		_popup_content.html(strhtml);
		_popup_content.hide();
		if(opts.callback) opts.callback(true);
	});
}


//提示信息
function Alert(opts){
	$.popup.hide();
	//遮罩层
	$.overlay.show({});
	var _popup_container_id = "popup_container_id";
	//提示信息容器
	var _popup_container = $('<div id="'+_popup_container_id+'" class="popup-container"></div>');
	$('body').append($(_popup_container));
	//提示信息框架
	var _popup_obj  = $('<div class="popup"></div>');
	//提示信息标题
	var _popup_title = $('<h1 class="popup-title">'+opts.title+'</h1>');
	//提示信息内容框架
	var _popup_content = $('<div class="popup-content popup-alert"></div>');
	//提示内容
	var _popup_message = $('<div class="popup-message">'+opts.message+'</div>');
	//确定按钮
	$(_popup_content).append(_popup_message);
	$(_popup_obj).append(_popup_title);
	$(_popup_obj).append(_popup_content);
	$(_popup_container).append(_popup_obj);
	$(_popup_message).after('<div  class="popup-panel"><input type="button" value="'+opts.certain+'" id="popup_certain" class="popup-btn" /></div>');
	$("#popup_certain").click(function(){
		$.popup.hide();
		if(opts.callback) opts.callback(true);
	});	
	
	$(_popup_obj).css(opts.css);
	var iframe_panel =  create_iframe_panel($(_popup_container).outerWidth(),$(_popup_container).outerHeight());
	$(iframe_panel).css("opacity",'0.0');
	$(_popup_container).append(iframe_panel);
	$.common.drag(_popup_container[0],_popup_title[0]);
	$.common.center(_popup_container[0]);
	$(_popup_container).show();
}

function Confirm(opts){
	$.popup.hide();
	//遮罩层
	$.overlay.show({});
	var _popup_container_id = "popup_container_id";
	//提示信息容器
	var _popup_container = $('<div id="'+_popup_container_id+'" class="popup-container"></div>');
	$('body').append($(_popup_container));
	//提示信息框架
	var _popup_obj  = $('<div class="popup"></div>');
	//提示信息标题
	var _popup_title = $('<h1 class="popup-title">'+opts.title+'</h1>');
	//提示信息内容框架
	var _popup_content = $('<div class="popup-content popup-alert"></div>');
	//提示内容
	var _popup_message = $('<div class="popup-message">'+opts.message+'</div>');
	//确定按钮
	$(_popup_content).append(_popup_message);
	$(_popup_obj).append(_popup_title);
	$(_popup_obj).append(_popup_content);
	$(_popup_container).append(_popup_obj);
	
	$(_popup_message).after('<div  class="popup-panel"><input type="button" value="'+opts.certain+'" id="popup_certain" class="popup-btn" /> <input type="button" value="'+opts.cancel+'" id="popup_cancel" class="popup-btn" /></div>');
	//取消操作
	$("#popup_cancel").click(function(){
		 $.popup.hide();
		 if(opts.cclcallback) opts.cclcallback(true);
		
	});
	//确定
	$("#popup_certain").click(function(){
		$.popup.hide();
		if(opts.callback) opts.callback(true);
	});
	
	$(_popup_obj).css(opts.css);
	var iframe_panel =  create_iframe_panel($(_popup_container).outerWidth(),$(_popup_container).outerHeight());
	$(iframe_panel).css("opacity",'0.0');
	$(_popup_container).append(iframe_panel);
	$.common.drag(_popup_container[0],_popup_title[0]);
	$.common.center(_popup_container[0]);
	$(_popup_container).show();
	
}

//加载条
function loading(opts){
	$.popup.hide();
	//遮罩层
    $.overlay.show({});
	var _popup_container_id = "popup_container_id";
	//提示信息容器
	var _popup_container = $('<div id="'+_popup_container_id+'" class="popup-container"></div>');
	$('body').append($(_popup_container));
	//提示信息框架
	var _popup_obj  = $('<div class="popup-loading"></div>');
	//提示信息内容框架
	var _popup_content = $('<div class="popup-loading-content"></div>');
	//提示内容
	var _popup_message = $('<div class="popup-loading-message">'+opts.message+'</div>');
	$(_popup_content).append(_popup_message);
	$(_popup_obj).append(_popup_content);
	$(_popup_container).append(_popup_obj);
	$(_popup_obj).css(opts.css);
	var iframe_panel =  create_iframe_panel($(_popup_container).outerWidth(),$(_popup_container).outerHeight());
	$(_popup_container).append(iframe_panel);
	$.common.center(_popup_container[0]);
	$(_popup_container).show();
}

$.AjaxWin = {
	show:function(opts){
		AjaxWinInstall(opts)
	},
	hide:function(win_id){
		$('#win_'+win_id).remove();
	},
	defaults:{
		winid:Math.random(10000),
		title:"自动义标题",
		zindex:1000,
		css:{ width:"400px",height:"300px"}
	}
}

//生成窗体
function AjaxWinInstall(opts){
		opts = $.extend({}, $.AjaxWin.defaults, opts || {});
		var winid = opts.winid;
		_winObj = $("#win_"+winid)[0];
		_winid = "win_"+winid;
	    if(!_winObj){
			
			var _wincontainer = $('<div id="'+_winid+'" class="win-container"></div>');
			var _winObj = $('<div class="win"></div>');
			$(_winObj).css(opts.css);
			$(_wincontainer).css("zindex",opts.zindex);
			//设置窗体头部
			var _wintitleid = _winid + "_title";
		    var _winTitleObj = $('<div id="'+_wintitleid+'" class="win-title"><span class="win-title-text">'+opts.title+'</span><span class="win-close" onclick="$.AjaxWin.hide(\''+winid+'\');">关闭</span></div>');
		    $(_winObj).append($(_winTitleObj));
			//内容框
			var _wincontentid = _winid + "_content";
			var _wincontentObj = $('<div id='+_wincontentid+' class="win-content"></div>');
			$(_wincontentObj).append('<img src="images/default/busy.gif" /> 加载中');
			$(_winObj).append($(_wincontentObj));
			$(_wincontainer).append(_winObj);
			$('body').append($(_wincontainer));
			$.common.drag(_wincontainer[0],_winTitleObj[0]);
            $.common.center(_wincontainer[0]);
			$(_wincontainer).show();
			
			//设置内容框高度
			var content_height = $(_winObj).height() - $(_winTitleObj).outerHeight();
			$(_wincontentObj).css("height",content_height+"px");
			
			//加载URL 内容
			$.ajax({
				   type: "POST",
				   url: opts.url,
				   cache:false,
				   data:opts.data,
				   success:function(response){
					   $(_wincontentObj).html(response);
					   $.common.center(_wincontainer[0]);
				   },
				   error:function() {
					  $(_wincontentObj).html("加载失败！");
				   }
			 });
			  var iframe_panel = create_iframe_panel($(_wincontainer).outerWidth(),$(_wincontainer).outerHeight());
			  iframe_panel.css('opacity','0.0');
			  $(_wincontainer).append(iframe_panel);
		}else{
			$.common.center(_wincontainer[0]);
		}
		
}

/*
 * 生成遮罩select d的框架
*/
function create_iframe_panel(width,height){
	  var iframe_panel = $('<iframe  style="z-index:-1;border:none;margin:0;padding:0;position:absolute;width:'+width+';height:'+height+';top:0;left:0;background-color:transparent" src="javascript:false;"></iframe>');
	  //iframe_panel.css("opacity",'0.0');
	  return iframe_panel;
}

//居中
function center(obj){
	//浏览器滚动位置
	 var _windowWidth  = document.documentElement.clientWidth;   
	 var _windowHeight = document.documentElement.clientHeight;   
	 var _objHeight    = $(obj).outerHeight();   
	 var _objWidth     = $(obj).outerWidth();  
	 $(obj).css({   
		  "position": "absolute",   
          "top": (_windowHeight - _objHeight)/2+$(document).scrollTop() + "px",   
		  "left":(_windowWidth - _objWidth)/2 + "px"
	 }); 
}
/*
*拖拽对象
*/
var Drag = function(){
	//拖拽控制区域
	var _controlObj;
	//拖拽对象
	var _dragObj;
	//拖动状态
	var _state = false;
	//鼠标最后位置
	var _mouseLastPostion;
	//拖拽对象当前位置
	var _dragObjCurrentPostion;
	//鼠标经过
	var dragMouseOverHandler = function(e){
		$(_controlObj).css("cursor", "move");
		if(e.preventDefault)
			e.preventDefault();
		else
			e.returnValue = false;
	}
	//鼠标按下
	var dragMouseDownHandler = function(e){
		if(_dragObj){
			e = window.event?window.event:e;
			_state = true;
			_dragObjCurrentPostion = {
			  x:$(_dragObj).offset().left,
			  y:$(_dragObj).offset().top
			};
			_mouseLastPostion = {
			   x:e.clientX - _dragObjCurrentPostion.x,
			   y:e.clientY - _dragObjCurrentPostion.y
			};
			$(document).bind("mousemove", dragMouseMoveHandler);
			$(document).bind("mouseup", dragMouseUpHandler);
			$(document).bind("selectstart", function(e){return false;});
			if(e.preventDefault)
				e.preventDefault();
			else
				e.returnValue = false;
		}
	}
	
	//鼠标移动
	var dragMouseMoveHandler = function(e){
		if(_state){
			 e = window.event?window.event:e;
			 _dragObjCurrentPostion = {
		        x:e.clientX - _mouseLastPostion.x,
		        y:e.clientY - _mouseLastPostion.y
		     };
			 $(_dragObj).css("left", _dragObjCurrentPostion.x + "px");
			 $(_dragObj).css("top", _dragObjCurrentPostion.y + "px");
			 if(e.preventDefault)
				e.preventDefault();
			 else
				e.returnValue = false;
		}
	}
    //鼠标松开
	var dragMouseUpHandler = function(e){
		if(_state){
			if(_controlObj){
				$(_controlObj.document).unbind("mousemove");
				$(_controlObj.document).unbind("mouseup");
				$(_controlObj.document).css('opacity','1');
		    }
			_state = false;
		}
	}
    this.register = function(dragObj, controlObj){
			_dragObj = dragObj;
			_controlObj = controlObj;
			//注册事件
			$(_controlObj).bind("mousedown", dragMouseDownHandler);
			$(_controlObj).bind("mouseover", dragMouseOverHandler);	
	}
}

})(jQuery);
