;jQuery(function($) {
    "use strict";
    // blockUI 默认样式
    $.blockUI.defaults.css = {
        padding:	0,
        margin:		0,
        color:		'#000',
        textAlign:  'left',
        border:     '3px solid #cecece',
        backgroundColor:'#fff',
        cursor:     'default'
    };
    $.blockUI.defaults.overlayCSS = {
        backgroundColor:	'#000',
        opacity:			0.6,
        cursor:				'default'
    };

    var outletUrl = location.hash.indexOf('debug') != -1 ?
        'template/outletList.php' :
        '/goods/'+ $('#goodsId').val() +'/shops'; 

    var minGmap, bigGmap,
        minMarker = {},
        currPage = 1, pageSize = 5, totalPage;

    var infowindow = new google.maps.InfoWindow({}),

        latlngToStr = function(obj) {
            return obj.lat() +','+ obj.lng();
        },

        latlngToObj = function(str) {
            var arr = str.split(','),
                obj = new google.maps.LatLng(arr[0], arr[1]);
            return obj;
        },

        createMap = function(id, zoom, latlng) {
            var options = {
                zoom: zoom,
                center: latlng,
                zoomControl: true,
                panControl:  false,
                scaleControl: false,
                overviewMapControl: false,
                streetViewControl: false,
                mapTypeControl: false,
                mapTypeId: google.maps.MapTypeId.ROADMAP
            };
            var map = new google.maps.Map(document.getElementById(id), options);
            return map;
        },

        changeMarker = function(latlng, title) {
            minMarker.latlng = latlng;
            minMarker.title = title;

            if (minGmap === undefined) {
                minGmap = createMap('min-gmap', 15, latlng);
            }
            if (minGmap.marker) {
                minGmap.marker.setMap(null);
            }

            minGmap.setCenter(latlng);
            minGmap.marker = new google.maps.Marker({
                map: minGmap,
                position: latlng,
                animation: google.maps.Animation.DROP
            });
        },

        showMap = function(outletList) {
            var latlng = latlngToObj(outletList[0]['latlng']);
            showOutletList(outletList);
            changeMarker(latlng, outletList[0]['name']);
        },

        getOutletList = function(url, callback) {
            $.getJSON(url, function(data) {
                if (data.status == 0) {
                    callback(data);
                } else {
                    $('#outlet').hide();
                }
            });
        },

        fitHtml = function(outletList) {
            var i, html = '';
            for (i in outletList) {
                html += '<li>'+
                    '<h5 class="outlet-name" data-addr="'+ outletList[i].addr +'" data-latlng="'+ outletList[i].latlng +'" data-index="'+ i +'">'+ outletList[i].name +'</h5>'+
                    '<div class="outlet-attr">'+
                        '<p>'+ outletList[i].addr +'</p>'+
                        '<p><span>'+ outletList[i].tel +'</span> <a class="view-map" data-index="'+ i +'" href="#">查看地图»</a> <a class="search-path" href="#">公交/驾车»</a></p>'+
                    '</div>'+
                    '</li>';
            }
            return html;
        },

        showOutletList = function(outletList) {
            $('.outlet-list ul').html( fitHtml(outletList) );
            $('.outlet-attr:first').show();
            $('.outlet-name:first').addClass('outlet-show');
        },

        createMarker = function(obj) {
            var marker = new google.maps.Marker({
                position: latlngToObj(obj.latlng),
                map: bigGmap
            });

            var contentString = '<div class="map-bubble">'+
                    '<h4>'+ obj.name +'</h4>'+
                    '<p>'+ obj.addr +'</p>'+
                    '<p>'+ obj.tel +'</p>'+
                    '<form action="http://ditu.google.cn/maps" method="get" target="_blank">'+
                        '<input type="hidden" name="daddr" value="'+ obj.latlng +'">'+
                        '起点：<input type="text" name="saddr" class="saddr"> '+
                        '<button type="submit" name="dirflg" value="r"> 公交 </button> '+
                        '<button type="submit" name="dirflg" value="d"> 驾车 </button>'+
                    '</form>'+
                '</div>';

            google.maps.event.addListener(marker, 'click', function(obj) {
                infowindow.setContent(contentString); 
                infowindow.open(bigGmap, marker);
                // if (obj) {
                    // $('.outletDiv li').removeClass('curr');
                    // console.log( $("li[data-latlng='"+ latlngToStr(obj.latLng) +"']") );
                    // $("li[data-latlng='"+ latlngToStr(obj.latLng) +"']").addClass('curr');
                // }
            });

            return marker;
        },

        // 门店列表分页
        outletListPaging = function(currPage, totalPage) {
            var pageLink,
                prevPage = '<a class="prev-page" href="#">上一页</a>',
                nextPage = '<a class="next-page" href="#">下一页</a>';

            if (totalPage > 1) {
                if ( currPage == 1 ) {
                    pageLink = nextPage;
                } else if (currPage == totalPage) {
                    pageLink = prevPage;
                } else {
                    pageLink = prevPage +' | '+ nextPage;
                }
                $('#outlet-page').html(pageLink);
            }
        },

        initialize = function() {
            getOutletList(outletUrl +'?currPage='+ currPage +'&pageSize='+ pageSize, function(data) {
                showMap(data.outletList);

                $('#outlet-total-num').text(data.totalOutlet);
                totalPage = Math.ceil(data.totalOutlet/5);
                outletListPaging(currPage, totalPage);
            });
        };

    // 切换店面地址
    $('#outlet .outlet-name').live('click', function() {
        var _this = $(this),
            str = _this.attr('data-latlng'),
            index = _this.attr('data-index');

        $('.view-all-outlet').attr('data-index', index);

        // 手风琴效果
        $('.outlet-name').removeClass('outlet-show');
        _this.addClass('outlet-show');
        $('.outlet-attr:visible').slideUp(100);
        _this.siblings().slideToggle(100);

        var latlng = latlngToObj(str);
        changeMarker(latlng, _this.text());
    });

    // 查看地图
    $('.view-map, .view-all-outlet').live('click', function(e) {
        e.preventDefault();
        var index = (currPage - 1) * pageSize + Number($(this).attr('data-index'));

        getOutletList(outletUrl, function(data) {
            var allOutlet = data.outletList;

            // 显示地图、门店弹出层
            $.blockUI({
                css: {
                    width: 920,
                    top: $(window).height()/2 - 284 +'px',
                    left: $(window).width()/2 - 463 +'px'
                },
                focusInput: false,
                message: $('<div id="map-box">'+
                    '<a class="close" href="javascript:void(0)" hidefocus="true"></a>'+
                    '<h3>查看地图</h3>'+
                    '<div class="mapDiv">'+
                        '<div id="big-gmap" style="width:650px;height:500px;"></div>'+
                        '<p class="warm">提醒：地图标注位置仅供参考，具体情况以实际道路标识信息为准</p>'+
                    '</div>'+
                    '<ul class="outletDiv">'+ fitHtml(allOutlet) +'</ul>'+
                '</div>')
            });
            $('.blockOverlay, .close').attr('title', '单击关闭').click($.unblockUI); 

            bigGmap = createMap('big-gmap', 10, minMarker.latlng);

            var markers = [];
            for (var i = 0, len = allOutlet.length; i < len; i++) {
                markers[i] = createMarker(allOutlet[i]);
            }

            var list = $('.outletDiv li');
            list.eq(index).addClass('curr');
            google.maps.event.trigger(markers[index], 'click');

            list.live('click', function(){
                var _this = $(this),
                    _index = _this.index();

                list.removeClass('curr');
                _this.addClass('curr');

                google.maps.event.trigger(markers[_index], 'click');
            });
        });
    });

    // 门店分页事件
    $('#outlet-page').delegate('a', 'click', function(e) {
        e.preventDefault();
        if ($(this).hasClass('next-page')) {
            currPage++;
        } else {
            currPage--;
        }
        getOutletList(outletUrl +'?currPage='+ currPage +'&pageSize='+ pageSize, function(data) {
            showMap(data.outletList);
            outletListPaging(currPage, totalPage);
        });
    });

    // 公交/驾车
    $('.search-path').live('click', function(e) {
        e.preventDefault();
        $.blockUI({
            css: {
                width: 320,
                top: $(window).height()/2 - 94 +'px',
                left: $(window).width()/2 - 160 +'px'
            },
            focusInput: false,
            message: $('<div id="map-search">'+
                '<a class="close" href="javascript:void(0)" hidefocus="true"></a>'+
                '<h3>查询路线</h3>'+
                '<form action="http://ditu.google.cn/maps" method="get" target="_blank">'+
                    '<ul>'+
                        '<li><span class="text">目的地</span> '+ minMarker.title +'<input type="hidden" name="daddr" value="'+ latlngToStr(minMarker.latlng) +'"></li>'+
                        '<li><span class="text">出行方式</span> <input type="radio" name="dirflg" checked value="r">公交 <input type="radio" name="dirflg" value="d">驾车</li>'+
                        '<li><span class="text">出发地</span> <input type="text" name="saddr" class="saddr"></li>'+
                        '<li><button type="submit" class="btn">查询</button></li>'+
                    '</ul>'+
                '</form>'+
            '</div>')
        });
        $('.blockOverlay, .close').attr('title','单击关闭').click($.unblockUI); 
    });

    initialize();
});
