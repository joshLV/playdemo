<%
/* *
 *���ܣ���ʱ���˽��׽ӿڽ���ҳ
 *�汾��3.3
 *���ڣ�2012-08-14
 *˵����
 *���´���ֻ��Ϊ�˷����̻����Զ��ṩ���������룬�̻����Ը����Լ���վ����Ҫ�����ռ����ĵ���д,����һ��Ҫʹ�øô��롣
 *�ô������ѧϰ���о�֧�����ӿ�ʹ�ã�ֻ���ṩһ���ο���

 *************************ע��*****************
 *������ڽӿڼ��ɹ������������⣬���԰��������;�������
 *1���̻��������ģ�https://b.alipay.com/support/helperApply.htm?action=consultationApply�����ύ���뼯��Э�������ǻ���רҵ�ļ�������ʦ������ϵ��Э�����
 *2���̻��������ģ�http://help.alipay.com/support/232511-16307/0-16307.htm?sh=Y&info_type=9��
 *3��֧������̳��http://club.alipay.com/read-htm-tid-8681712.html��
 *�������ʹ����չ���������չ���ܲ�������ֵ��
 **********************************************
 */
%>
<%@ page language="java" contentType="text/html; charset=gbk" pageEncoding="gbk"%>
<%@ page import="com.alipay.config.*"%>
<%@ page import="com.alipay.util.*"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.Map"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=gbk">
		<title>֧������ʱ���˽��׽ӿ�</title>
	</head>
	<%
		////////////////////////////////////�������//////////////////////////////////////

		//֧������
		String payment_type = "1";
		//��������޸�
		//�������첽֪ͨҳ��·��
		String notify_url = "http://www.xxx.com/create_direct_pay_by_user-JAVA-GBK/notify_url.jsp";
		//��http://��ʽ������·�������ܼ�?id=123�����Զ������
		//ҳ����תͬ��֪ͨҳ��·��
		String return_url = "http://www.xxx.com/create_direct_pay_by_user-JAVA-GBK/return_url.jsp";
		//��http://��ʽ������·�������ܼ�?id=123�����Զ������������д��http://localhost/
		//����֧�����ʻ�
		String seller_email = new String(request.getParameter("WIDseller_email").getBytes("ISO-8859-1"),"GBK");
		//����
		//�̻�������
		String out_trade_no = new String(request.getParameter("WIDout_trade_no").getBytes("ISO-8859-1"),"GBK");
		//�̻���վ����ϵͳ��Ψһ�����ţ�����
		//��������
		String subject = new String(request.getParameter("WIDsubject").getBytes("ISO-8859-1"),"GBK");
		//����
		//������
		String total_fee = new String(request.getParameter("WIDtotal_fee").getBytes("ISO-8859-1"),"GBK");
		//����
		//��������
		String body = new String(request.getParameter("WIDbody").getBytes("ISO-8859-1"),"GBK");
		//��Ʒչʾ��ַ
		String show_url = new String(request.getParameter("WIDshow_url").getBytes("ISO-8859-1"),"GBK");
		//����http://��ͷ������·�������磺http://www.xxx.com/myorder.html
		//������ʱ���
		String anti_phishing_key = "";
		//��Ҫʹ����������ļ�submit�е�query_timestamp����
		//�ͻ��˵�IP��ַ
		String exter_invoke_ip = new String(request.getParameter("WIDexter_invoke_ip").getBytes("ISO-8859-1"),"GBK");
		//�Ǿ�����������IP��ַ���磺221.0.0.1
		
		
		//////////////////////////////////////////////////////////////////////////////////
		
		//������������������
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "create_direct_pay_by_user");
        sParaTemp.put("partner", AlipayConfig.partner);
        sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("payment_type", payment_type);
		sParaTemp.put("notify_url", notify_url);
		sParaTemp.put("return_url", return_url);
		sParaTemp.put("seller_email", seller_email);
		sParaTemp.put("out_trade_no", out_trade_no);
		sParaTemp.put("subject", subject);
		sParaTemp.put("total_fee", total_fee);
		sParaTemp.put("body", body);
		sParaTemp.put("show_url", show_url);
		sParaTemp.put("anti_phishing_key", anti_phishing_key);
		sParaTemp.put("exter_invoke_ip", exter_invoke_ip);
		
		//��������
		String sHtmlText = AlipaySubmit.buildRequest(sParaTemp,"get","ȷ��");
		out.println(sHtmlText);
	%>
	<body>
	</body>
</html>
