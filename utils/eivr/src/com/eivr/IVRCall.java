package com.eivr;
/***************加载需要的类文件，自定义的jar文件可以放到v\WebRoot\WEB-INF\lib**************/

import com.lhw.qo.dbo.DoRequestI;
import com.lhw.qo.dbo.DoRequestLocal;
import com.lhw.qo.dbo.DoResponse;
import com.lhw.qo.dbo.DoResponseI;
import com.lhw.qo.dbo.DoService;
import com.lhw.qo.util.DateHelper;
import com.lhw.qo.util.MyHashMap;
import com.lhw.qo.web.QueryRequestI;
import com.lhw.qo.web.QueryRequestLocal;
import com.lhw.qo.web.QueryService;
import com.lhw.util.CExecutorI;
import com.lhw.util.CRequest;
import com.lhw.util.CResponse;
import com.lhw.util.MyHttpGet;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

/**
 * ************加载需要的类文件结束******************************
 */
public class IVRCall implements CExecutorI {

    //定义日志对象log;将在安装目录的cont\logs\v.log文件里看到运行日志
    static Logger log = Logger.getLogger(IVRCall.class.getName());

    //定义数据修改操作使用对象doService
    DoService doService = new DoService();
    //定义数据库查询对象queryService;
    QueryService queryService = new QueryService();

    public int execute(CRequest cr) {   //方法execute;
        try {
            //系统变量及自定义变量的生命周期是 一次客户的电话呼入到挂机结束
            //取系统变量
            String ch = (String) cr.getParameter("_ch_");//来电外线通道号
            String call_date = (String) cr.getParameter("_call_date_");//来电日期
            String start_time = (String) cr.getParameter("_start_time_");//来电时间
            String phone = (String) cr.getParameter("_phone_"); //来电号码
            String vo_id = (String) cr.getParameter("vo_id"); //取接口编码;
            log.debug("IVRMebTest  vo_id=" + vo_id);

            String _para_ = (String) cr.getParameter("_para_"); //此值由push2flow服务，调用者传入的数据
            log.debug("IVRMebTest  _para_=" + _para_);
            //取系统变量结束

            //取流程中的自定义值输入按键
            String c_meb = (String) cr.getParameter("C_MEB");//创建会员时的会员号
            log.debug("IVRMebTest  创建会员号=" + c_meb);//打出日志
            String c_pwd = (String) cr.getParameter("C_PWD");//创建会员时的密码
            log.debug("IVRMebTest  创建会员密码=" + c_pwd);
            String c_scr = (String) cr.getParameter("C_SCR");//创建会员时的积分
            log.debug("IVRMebTest  创建会员积分=" + c_scr);

            String c_meb_no = (String) cr.getParameter("C_MEB_NO");//会员登录时的会员号
            log.debug("IVRMebTest  登录会员号=" + c_meb_no);
            String c_password = (String) cr.getParameter("C_PASSWORD");//会员登录时的密码
            log.debug("IVRMebTest  登录会员密码=" + c_password);

            String c_mody_pwd = (String) cr.getParameter("C_MODY_PWD");//修改密码时输入的新密码
            log.debug("IVRMebTest  新密码=" + c_mody_pwd);


            String _para2_ = (String) cr.getParameter("_para2_"); //取自定义当前记忆值
            log.debug("IVRMebTest  _para2_=" + _para2_);

            if ("V_VERIFY".equals(vo_id)) {
                // 电话验证
                String coupon = (String) cr.getParameter("C_COUPON");
                Long timestamp = System.currentTimeMillis() / 1000;
                String sign = getSign(timestamp);
                //response = GET("/tel-verify?caller=" + phone + "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);

                if (phone == null || phone.length() < 3) {
                    phone = "8015";
                } else {
                    phone = phone.trim();
                }
                if (phone.length() == 8) {
                    phone = "021" + phone;
                }
                log.info(" call /tel-verify2?caller=" + phone + "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
                MyHttpGet httpget = new MyHttpGet();
                CRequest req = new CRequest();
                req.setParameter("caller", phone);
                req.setParameter("coupon", coupon);
                req.setParameter("timestamp", timestamp.toString());
                req.setParameter("sign", sign);
                CResponse rep = null;
                String url = "http://api.quanfx.com/tel-verify2?pt=2";
                if ("18016488329".equals(phone) || "13472581853".equals(phone)) {
                    url = "http://test1.quanfx.com/tel-verify2?pt=2";
                }
                try {//向URL发送...
                    rep = httpget.sendRequest(req, url);
                } catch (Exception e) {
                    log.debug("调用验证接口失败 " + e);
                    cr.setParameter("TTS", "调用验证接口失败");
                    cr.rcode = 2;
                    return 0;
                }
                //取应答数据
                String reps = rep.getmsg();
                //系统变量： _data_将可以通过交互式E-IVR返回给坐席端时用，通过app_config.xml中的popCRURL接收并处理_data_,比如可将一个密码实时验证结果返回给坐席界面
                // 是否会调用popCRURL还得看流程中根据rcode的设置，如果后续流程有_F_SEND2AGENT_ 才会打开新的窗口即popCRURL
                cr.setParameter("_data_", reps);
                log.debug("resp = (" + reps + ")");

                String[] results = reps.split("\\|");

                log.debug("results.length=" + results.length);

                int rcode = Integer.parseInt(results[0]);
                cr.rcode = rcode;
                if (results.length > 1) {
                    cr.setParameter("TTS", results[1]);
                }
                return 0;
            } else if (vo_id.equals("V_TTS")) {   //播放一段TTS

                cr.setParameter("TTS", "您的余额是一百一十一元五角二分");

                cr.rcode = 0; //此值非常重要,将被流程所判断使用
                return 0;
            } else if (vo_id.equals("V_CREATE")) {   //判断vo_id值为V_CREATE时创建会员
                /**********基于LQuery模式进行编程,先查询输入的会员号是否已存在**************/
                QueryRequestI qr = new QueryRequestLocal("qo_IVRmeb");   //v\WebRoot\sc\qoaccess\qo_IVRmeb.xml
                qr.setParameter("m_no", (String) cr.getParameter("C_MEB"));//查询条件m_no等于刚刚输入的会员号.
                MyHashMap mebhp = queryService.getOneRowValue(qr);
                //如何未查询到结果,mebhp为null,如果查询到记录，表示已存在相同的会员号。
                if (mebhp != null) {
                    log.debug("IVRMebTest  会员号已存在");
                    cr.setParameter("TTS", "您输入的会员号已存在");
                    cr.rcode = 0; //此值非常重要,将被流程所判断使用
                    return 0;
                }

                /*********基于LQuery模式进行编程,如会员号不存在,就插入数据库**********/
                log.debug("IVRMebTest  创建会员");
                DoResponseI dresp = new DoResponse();
                DoRequestI dr = new DoRequestLocal("do_ivr");//do_id为do_ivr
                dr.setParameter("action", "Insert");//action为insert
                dr.setParameter("m_no", c_meb);
                dr.setParameter("m_scr", c_scr);
                dr.setParameter("m_paw", c_pwd);
                int cnt = doService.execute(dr, dresp); //执行插入数据库
                log.debug("IVRMebTest  会员创建成功");
                cr.setParameter("TTS", "会员创建成功"); //TTS将被流程的动态TTS模块播放
                cr.rcode = 0;

                cr.setParameter("_para2_", "222"); //填入自定义记忆值
                return 0;
            } else if (vo_id.equals("V_CHECK")) {                  //会员登录密码验证

                QueryRequestI qr = new QueryRequestLocal("qo_IVRmeb"); //qo_id为qo_IVRmeb
                qr.setParameter("m_no", c_meb_no);    //设置条件m_no等于输入的会员号
                try {
                    MyHashMap mebhp = queryService.getOneRowValue(qr); //取出会员记录
                    log.debug("密码为::::: " + mebhp.get("m_paw"));
                    if (mebhp.get("m_paw").equals(c_password)) {
                        cr.rcode = 0;                             //验证密码成功
                        //如果验证成功在IVR表里重置来电时间及电话
                        DoRequestI dr = new DoRequestLocal("do_ivr");//do_id为do_ivr,v\WebRoot\sc\doaccess\do_ivr.xml
                        dr.setParameter("action", "updateByIndexField");//action为updateByIndexField
                        dr.setParameter("indexId", c_meb_no);        //主键为会员号c_meb_no
                        dr.setParameter("call_date", DateHelper.getCurDate());
                        dr.setParameter("start_time", DateHelper.getCurTime());
                        dr.setParameter("phone", phone);
                        DoResponseI dresp = new DoResponse();
                        int cnt = doService.execute(dr, dresp); //执行修改数据库

                        return 0;
                    } else {
                        cr.rcode = 1;                            //验证密码失败
                        return 0;
                    }
                } catch (Exception e) {
                    cr.setParameter("TTS", "用户不存在");
                    cr.rcode = 1;
                    return 0;
                }


            } else if (vo_id.equals("V_SCR")) {                            //查询会员积分
                QueryRequestI qr = new QueryRequestLocal("qo_IVRmeb");
                qr.setParameter("m_no", c_meb_no);            //设置条件m_no等于输入的会员号
                MyHashMap mebhp = queryService.getOneRowValue(qr);
                cr.rcode = 0;
                cr.setParameter("TTS", "您的积分为" + mebhp.get("m_scr"));
                return 0;
            } else if (vo_id.equals("V_MODY_PWD")) {                //密码修改
                DoResponseI dresp = new DoResponse();
                DoRequestI dr = new DoRequestLocal("do_ivr");
                dr.setParameter("action", "updateByIndexField");//action为updateByIndexField
                dr.setParameter("indexId", c_meb_no);        //主键为会员号c_meb_no
                dr.setParameter("m_no", c_meb_no);
                dr.setParameter("m_paw", c_mody_pwd); //新密码
                int cnt = doService.execute(dr, dresp); //执行修改数据库
                cr.rcode = 0;
                cr.setParameter("TTS", "密码修改成功");
                return 0;
            } else if (vo_id.equals("V_CHECK_HTTP")) {
                //利用一个第三方提供的URL服务验证密码,这种模式更加方便
                //本例子由第三方提供一个测试用URL,如何输入会员号100密码100则返回0，否则返回1，代表密码验证失败
                MyHttpGet httpget = new MyHttpGet();

                CRequest req = new CRequest();
                req.setParameter("vo_id", "V_CHECK_HTTP");
                req.setParameter("c_meb_no", c_meb_no);
                req.setParameter("c_password", c_password);
                CResponse rep = new CResponse(0);
                String url = "http://www3.xxx.cn:6666/v/vack";
                try {//向URL发送...
                    rep = httpget.sendRequest(req, url);
                } catch (Exception e) {
                    log.debug("密码验证发送失败 " + e);
                    cr.rcode = 1;
                    return 0;
                }
                //取应答数据
                String reps = rep.getmsg();
                //系统变量： _data_将可以通过交互式E-IVR返回给坐席端时用，通过app_config.xml中的popCRURL接收并处理_data_,比如可将一个密码实时验证结果返回给坐席界面
                // 是否会调用popCRURL还得看流程中根据rcode的设置，如果后续流程有_F_SEND2AGENT_ 才会打开新的窗口即popCRURL
                cr.setParameter("_data_", reps);
                if (reps.startsWith("0")) {
                    cr.rcode = 0; //验证密码成功
                    return 0;
                } else {
                    cr.rcode = 1; //密码错误
                    return 0;
                }

            } else if (vo_id.equals("V_INPUTPWD")) {   //仅仅接收密码，再返回给坐席端处理
                cr.setParameter("_data_", c_password);
                cr.rcode = 0; //接收密码成功
                return 0;
            } else if (vo_id.equals("V_TESTIVR")) {

                QueryRequestI qr = new QueryRequestLocal("qo_IVRmeb");   //v\WebRoot\sc\qoaccess\qo_IVRmeb.xml
                qr.setParameter("m_no", (String) cr.getParameter("C_MEB"));//查询条件m_no等于刚刚输入的会员号.
                MyHashMap mebhp = queryService.getOneRowValue(qr);
                //如何未查询到结果,mebhp为null,如果查询到记录，表示已存在相同的会员号。
                if (mebhp != null) {
                    DoResponseI dresp = new DoResponse();
                    DoRequestI dr = new DoRequestLocal("do_ivr");
                    dr.setParameter("action", "updateByIndexField");//action为updateByIndexField
                    dr.setParameter("indexId", c_meb);        //主键为会员号c_meb_no
                    dr.setParameter("m_no", c_meb);
                    dr.setParameter("m_paw", c_pwd);
                    dr.setParameter("call_date", DateHelper.getCurDate());
                    dr.setParameter("start_time", DateHelper.getCurTime());
                    dr.setParameter("phone", phone);
                    int cnt = doService.execute(dr, dresp); //执行修改数据库
                    log.debug("IVRMebTest  会员信息更新");

                    cr.rcode = 0;
                    return 0;
                }


                log.debug("IVRMebTest  增加一行");
                DoResponseI dresp = new DoResponse();
                DoRequestI dr = new DoRequestLocal("do_ivr");//do_id为do_ivr
                dr.setParameter("action", "Insert");//action为insert
                dr.setParameter("m_no", c_meb);
                dr.setParameter("m_scr", c_scr);
                dr.setParameter("m_paw", c_pwd);
                dr.setParameter("call_date", DateHelper.getCurDate());
                dr.setParameter("start_time", DateHelper.getCurTime());
                dr.setParameter("phone", phone);
                int cnt = doService.execute(dr, dresp); //执行插入数据库
                log.debug("IVRMebTest  会员创建成功");
                cr.setParameter("TTS", "会员创建成功"); //TTS将被流程的动态TTS模块播放
                cr.rcode = 0;

                cr.setParameter("_para2_", "222"); //填入自定义记忆值
                return 0;
            }


        } catch (Exception e) {                             //捕捉异常
            //log.error("IVRCallTest  error! "+e);
            cr.setParameter("TTS", e.toString());
            cr.rcode = 0;
        }
        return 0;
    }

    public static final String REEB_APP_KEY = "exos8BHw";

    /**
     * 得到call调用的时间Sign
     */
    private static String getSign(long timestamp) {
        return DigestUtils.md5Hex(REEB_APP_KEY + timestamp);
    }
}
