/**
  *  @module
  *  @author lanqi
  */
  
KISSY.add("starGrade",function(S){
	var S = KISSY, DOM = S.DOM, Event = S.Event,
	debug = (-1 === window.location.toString().indexOf('__debug')) ? false : true;
	
	function inputEvent() {}
	S.augment(inputEvent, S.EventTarget);
	S.augment(inputEvent, {
		change: function() {
			this.fire('valueChange');
		}
	});
	var target = new inputEvent();
	var olderGrade = {} ; 
	
	function defaultState() {
		S.one('#respondText') && S.one('#respondText').addClass('hidden');
		S.one('textarea') && S.one('textarea').removeClass('higher');
		S.one('p.error') && S.one('p.error').addClass('hidden');
		S.one('p.tip') && S.one('p.tip').addClass('hidden');
		S.one('p.ok') && S.one('p.ok').addClass('hidden');
		S.one('#judeApi .subRespond') && S.one('#judeApi .subRespond').addClass('hidden');
		S.one('#cancel') && S.one('#cancel').addClass('hidden');
		if(S.one('#submit')){
			S.one('#submit').attr('disabled', 'true');
			S.one('#submit').addClass('btnActive');
		}
	}
	
	S.grade = {
		init: function(fun) {
			var that = this ;
			S.ready(function(){
				S.each(that.module['base'], function(item) {
					S.isFunction(item) && item.call(that);
				});
			});
		},
		
		module: {
			base: [
				/*
				 * 打分
				 *
				 */
				function() {
					if(!DOM.get('#judeApi')){
						return;
					}
					var div = DOM.get('#judeApi'), starList = DOM.query('div.scorePart', div), btn = S.one('#submit'), text = DOM.get('div.respondText'), tip = DOM.create('<div class="pop hidden"></div>'), popMsg = {'1':'1分-非常不满意','2':'2分-不满意','3':'3分-一般','4':'4分-满意','5':'5分-很满意'};
					
					document.body.appendChild(tip);
						starList && S.each(starList, function(el) {
							var starItem = DOM.query('div.markStar', el), clear = DOM.query('div.clearStar', el), firstScore;
							
							// 点击评分
							S.each(starItem, function(star) {
								Event.on(star, 'click', function() {
									var rel = DOM.attr(star, 'rel');
									if(!firstScore || rel >= parseInt(DOM.attr(firstScore, 'rel'))) {
										S.each(starItem, function(el) {
											if(parseInt(DOM.attr(el, 'rel')) <= rel) {
												DOM.addClass(el,"clickStar");
											}
										});
										firstScore = star;
									}else if( rel < parseInt(DOM.attr(firstScore, 'rel'))){
										S.each(starItem, function(el) {
											if(parseInt(DOM.attr(el, 'rel')) > rel) {
												DOM.removeClass(el,"clickStar");
											}
										});
										firstScore = star;
									}
									DOM.val(DOM.get('input', el), rel);
									target.change();
									validate();
								});
								starTip(star, tip, el);
							});
							
							
							S.each(clear, function(star) {
								Event.on(star, 'mouseover', function(e) {
									e.halt();
									var tar = e.target ;
									DOM.text(tip, '删除评分');
									offset = DOM.offset(tar);
									DOM.css(tip, "left", offset.left+"px");
									DOM.css(tip, "top", offset.top+DOM.height(star)+"px");
									DOM.removeClass(tip, 'hidden');
								});
								Event.on(star, 'mouseout' , function(e) {
									DOM.addClass(tip, 'hidden');
								});
							});
							function starTip(tarElem, tipELem, parent){
								Event.on(tarElem, 'mouseover' , function(e) {
									e.halt();
									var tar = e.target, index = DOM.attr(tarElem, 'rel');
									if(index){ DOM.text(tipELem, popMsg[index]) };
									if(DOM.hasClass(tar, 'markStar')){
										offset = DOM.offset(el);
										DOM.css(tipELem, "left", offset.left+15+"px");
										DOM.css(tipELem, "top", (offset.top+DOM.height(el))-5+"px");
										DOM.removeClass(tipELem, 'hidden');
										var itemList = DOM.query('.markStar', parent);
										itemList && S.each(itemList, function(e){
											if(DOM.attr(e, 'rel') && parseInt(DOM.attr(e, 'rel')) <= parseInt(index)){
												DOM.addClass(e, 'hoverStar');
											}
										});
									}
								});
								Event.on(tarElem, 'mouseout' , function(e) {
									DOM.addClass(tipELem, 'hidden');
									var itemList = DOM.query('.markStar', parent);
									itemList && S.each(itemList, function(e){
										DOM.removeClass(e, 'hoverStar');
									});
									DOM.addClass(tipELem, 'hidden');
								});
							}
							
							// 清除评分
							S.each(clear, function(e) {
								Event.on(e, 'click', function(){
									S.each(starItem, function(item){
										DOM.removeClass(item, 'clickStar');
									});
									DOM.val(DOM.get('input', el), '');
									validate();
								});
							});
						});	
						
					var linkDiv = DOM.get('div.APIgory'), sublink = DOM.query('a.APIgoryItem', linkDiv), mask = DOM.query('s', linkDiv), menuTip = DOM.create('<div class="pop-tip hidden"></div>');
					document.body.appendChild(menuTip);
					if(!linkDiv){
						return;
					}
					mask && S.each(mask, function(item){
						Event.on(item, 'mouseover', function(e){
							var parent = DOM.prev(item, '.APIgory-content'), 
								text1 = DOM.text(DOM.get('a', parent)),
								text2 = DOM.text(DOM.get('p', parent));
							e.halt();
							var tar = e.target ;
						//	DOM.text(menuTip, text);
							menuTip.innerHTML = text1 + "<br/>" + text2;
							offset = DOM.offset(tar);
							DOM.css(menuTip, "left", offset.left+ DOM.width(tar)-70+"px");
							DOM.css(menuTip, "top", offset.top+44+"px");
							DOM.removeClass(menuTip, 'hidden');
						});
						Event.on(item, 'mouseout' , function(e) {
							DOM.addClass(menuTip, 'hidden');
						});
					});
					
					target.on('valueChange', function(){
						var scoreinput = DOM.query('input', div), cnt = 0;
						scoreinput && S.each(scoreinput, function(e){
							if(DOM.val(e) && DOM.val(e) <= 3) { cnt ++;}
						});
						DOM.removeClass(text, 'hidden');
						if( cnt > 0 ){
							DOM.removeClass(text, 'hidden');
						}
						else{
							DOM.addClass(text, 'hidden');
						}
						
					});
					
					Event.on(text, 'keyup', function(){
						S.one('textarea') && S.one('textarea').removeClass('higher');
						S.one('p.error').addClass('hidden');
						S.one('p.error').text('');
						validate();
					});
					
					
					function validate(){
						var cnt = 0 ;
						defaultState();
						starList && S.each(starList, function(el){
							if(!DOM.val(DOM.get('input',el))){
								cnt ++;
							}
						});
						if( 0 != cnt ){
							S.one('textarea') && S.one('textarea').removeClass('higher');
							S.one('p.error').removeClass('hidden');
							S.one('p.error').text('您还没有完成全部评分');
							return;
						}						
						if(!S.one('div.respondText').hasClass('hidden') && !S.one('textarea').val()){
							S.one('textarea') && S.one('textarea').addClass('higher');
							S.one('p.error').removeClass('hidden');
							S.one('p.error').text('您还没有告诉我您所遇到的问题');
							return;
						}
						
						if(btn){
							DOM.attr(btn, 'disabled', false);
							btn.removeClass('btnActive');
						}
					}
					
					
				},
				
				function() {
					var div = DOM.get('#judeApi'), starList = DOM.query('div.scorePart', div),resultdiv = DOM.get('#showResult'), bkList = DOM.query('div.scorePart', resultdiv),rebtn = S.one('#submit'),text = DOM.get('div.respondText'), msg = DOM.get('.message', resultdiv), btn = S.one('#submit'), ar ={};
				
					if(!DOM.get('#showGrade')){
						return;
					}
					
					S.ready(function(S){
						ajaxValidate('init',S.one('#getDataUrl').text(),null);
						//ajaxValidate('init','text-data/data2.json',null);
					});
					
					var showDiv = DOM.get('#showGrade'), totalGrade = DOM.get('.api-grade-title'), showStar = DOM.query('div.scorePart', showDiv), link = DOM.get('#grade', showDiv);
					
					// 评价
					Event.on(link, 'click', function(e){
						e.preventDefault();	
						ajaxValidate('isLogin',S.one('#isLoginUrl').text(), null);
						//ajaxValidate('isLogin','text-data/data.json',null);
						// ajax验证
					});
					
					// 查看评分
					S.all('a.linkGrade') &&  S.all('a.linkGrade').on('click', function(e){
						e.preventDefault();
						ajaxValidate('init',S.one('#getDataUrl').text(),null);
		                //ajaxValidate('init','text-data/data2.json',null);
					});
					
					// 提交评分
					btn && btn.on('click', function(){
						starList && S.each(starList, function(el){
							ar[DOM.attr(el, 'rel')] = DOM.val(DOM.get('input',el));
						});
						if(S.one('textarea').val()){
							ar['reason']=S.one('textarea').val();
						}
						ajaxValidate('submit',S.one('#judeApiUrl').text(), ar);
						//ajaxValidate('submit','text-data/data1.json', ar);
					});
					
					var msgText = { 'success':'<p class="ok ">非常感谢，您的评分已提交！</p>','error_today_done':'<p>非常感谢您的反馈，</p><p>您今天已经提交了评分，不能重复提交。</p>','error_not_isv':'<p>您好，API评分系统目前只针对开发者开放</p><p>如果您对我们有什么意见或者建议，请通过反馈入口给我们，非常感谢您的支持！</p>','error_system':'似乎现在服务出错了，请您再次尝试提交评价'};
					
					
					/*展示最新评分*/
					function showGrade(){
						if(!totalGrade && !olderGrade) { return ; }
						DOM.text(DOM.get('.strong', totalGrade), olderGrade['sumScore']);
						DOM.text(DOM.get('.total', totalGrade), olderGrade['commentatorCount']);
						var result = {};
						S.each(olderGrade, function(val ,i){
							result[i] = val;	
						});
						
						var showDiv = DOM.get('#showGrade'), showStar = DOM.query('.scorePart', showDiv) ;
						
						showStar && S.each(showStar, function(item){
							var value = result[DOM.attr(item,'scoreName')],blue = DOM.get('.blue',item);
							if(value){
								var i = parseFloat(value),s = Math.ceil(i),width = i * 16 + (s-1) * 9, eachScore = DOM.get('.total',item);
								eachScore && DOM.text(eachScore, i.toFixed(1));
								width = width+ 2;
								DOM.css(blue, 'width', width);
							}
						});
					}
					
					/*提交评价验证*/
					function ajaxValidate(type, url, data){
						var tTime=(new Date()).getTime();
						if(data){
							data["tttRadom"]=tTime;
						}else{
							data={};
							data["tttRadom"]=tTime;
						}
						S.io.getJSON(url, data, function(result){
							S.each(result, function(item, i) {
								if(type=='submit'){
									if(item['resultCode']=="success"){
										olderGrade = item['apiScore'];
									}
									S.one('#judeApi').addClass('hidden');
									S.one('#showResult').removeClass('hidden');
									renderStar(ar);
									var resultMsg=msgText[item['resultCode']];
									if(!resultMsg){
										 resultMsg=msgText['error_system'];
									}
									DOM.html(msg,resultMsg);
									
								}
								else if(type =='init'){
									if('success' == item['resultCode']){
										DOM.html(DOM.get('.subRespond',showDiv),'');
										DOM.addClass(resultdiv, 'hidden');
										DOM.addClass(div, 'hidden');
										DOM.removeClass(showDiv, 'hidden');
										olderGrade = item['apiScore'];
										showGrade();
									}
									else{
										// 系统错误，请重试
										DOM.html(DOM.get('.subRespond',showDiv),'<p>系统错误，请重试</p>');
									}
								}else if(type =='isLogin'){
									if(item['resultCode']=='true'){
										DOM.addClass(showDiv, 'hidden');
										DOM.removeClass(div, 'hidden');
									}
									else{
										/*登陆框*/
										popup && popup.show();
										//修复ie6登陆框旁有空iframe问题
										//var loginIframe=S.one('#loginFm');
//										var loginIframe=login;
//										alert(S.one('#loginFm')==loginIframe);
//										alert("loginIframe "+loginIframe);
//										alert("loginIframe.nextSibling="+loginIframe.nextSibling);
//										if(loginIframe&&loginIframe.nextSibling){
//											DOM.addClass(loginIframe.nextSibling, 'hidden');
//										}
										setTimeout(function(){
											if(S.all('iframe', loginDiv)){
												var iframes = S.all('iframe', loginDiv);
												S.each(iframes, function(item){
													if(!S.one(item).attr('id')){														
														S.one(item).remove();
													}
												});
											}
										}, 500)
									// 连接服务器错误 S.one('.tip').removeClass('hidden');
									}
								}
							});
						});
					}
					
					function renderStar(value){
						if(S.isArray(value)) {
							bkList && S.each(bkList, function(item, i){
								var star = DOM.query('.markStar',item);
								for(var j = 0 ; j < value[i]; j++){
									S.one(star[j]).addClass('blackStar');
								}
							});
	
						}
					}
					//var returnUrl="http%3A%2F%2Fwww.taobao.com";
					var returnUrl=S.one('#returnUrl').text();
					var tbLoginUrl=S.one('#tbLoginUrl').text();
					var loginDiv = DOM.create('<div id="loginDiv"></div>'),login = DOM.create("<iframe id='loginFm' class='loginFm' src='"+tbLoginUrl+"?style=mini&redirectURL="+returnUrl+"&full_redirect=true' width='100%' height='100%'><iframe>"), popup;
					
					DOM.append(login, loginDiv);
					DOM.append(loginDiv, 'body');
					
					S.use('ua,overlay', function(S, UA,O) {
						popup = new O.Popup({
							srcNode: "#loginDiv",
							closable: true,
							elStyle:{
								position:UA.ie == 6 ? "absolute" : "fixed",
								background:"transparent"
							},
							align: {
								points: ['cc', 'cc'],
								offset: [0, -60] 
							},
							effect: {
								effect:"fade",
								duration:0.5
							}
						});

						if (UA.ie == 6) {
							Event.on(window, "scroll", function() {
								if (popup.get("visible"))
									popup.center();
							});
						}
					});
				},
			
				function() {
	/*			// 锚点跳转
					var els = DOM.query('.sub-title','ul.api-sub-title');
					S.each(els, function(item){
						Event.on(item, 'click', function(e){
							e.stopPropagation();
							e.preventDefault();
							var tar = e.target, id = DOM.attr(tar, 'link-href'), tarElem = DOM.get(id) ;
							
							if(!tarElem){
								return;
							}
							var box = document.getElementById('bd') , boxTop = DOM.offset(box).top, tarTop = DOM.offset(tarElem).top, length = box.scrollTop + tarTop - boxTop, speed = 10, run = length;
							
							setTimeout(function(){
								box.scrollTop = box.scrollTop + speed ;
								run = run - speed;
								speed *= 1.5;
								if(run <= 0){
									box.scrollTop = length ;
								}
								else{      
									setTimeout(arguments.callee, 25);
								}
							},25); 
						});
					});
	*/				
					// 回到顶部
					var back = DOM.create('<div>'), box = DOM.get('#bd');
		//			DOM.text(back, '回到顶部');
					DOM.addClass(back, 'backTop');
					DOM.append(back, 'body');
					Event.on(back,'click', function(){
						var speed = 10 ;
						setTimeout(function(){
							var now = document.documentElement.scrollTop ;
							window.scrollTo(0, now-speed) ;
							speed *= 1.1;
							if(document.documentElement.scrollTop <= 0){
								window.scrollTo(0,0);
							}
							else{      
								setTimeout(arguments.callee, 25);
							}
						},25);
					});
				
				
				}
			]
		}
		
	}
	
});