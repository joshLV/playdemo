<%
' ���ܣ���ʱ���������˿����ܽӿڽ���ҳ
' �汾��3.3
' ���ڣ�2012-07-17
' ˵����
' ���´���ֻ��Ϊ�˷����̻����Զ��ṩ���������룬�̻����Ը����Լ���վ����Ҫ�����ռ����ĵ���д,����һ��Ҫʹ�øô��롣
' �ô������ѧϰ���о�֧�����ӿ�ʹ�ã�ֻ���ṩһ���ο���
	
' /////////////////ע��/////////////////
' ������ڽӿڼ��ɹ������������⣬���԰��������;�������
' 1���̻��������ģ�https://b.alipay.com/support/helperApply.htm?action=consultationApply�����ύ���뼯��Э�������ǻ���רҵ�ļ�������ʦ������ϵ��Э�����
' 2���̻��������ģ�http://help.alipay.com/support/232511-16307/0-16307.htm?sh=Y&info_type=9��
' 3��֧������̳��http://club.alipay.com/read-htm-tid-8681712.html��
' /////////////////////////////////////

%>

<!--#include file="class/alipay_submit.asp"-->

<%
'/////////////////////�������/////////////////////

        '�������첽֪ͨҳ��·��
        notify_url = "http://www.xxx.com/refund_fastpay_by_platform_pwd-CSHARP-GBK/notify_url.asp"
        '��http://��ʽ������·�����������?id=123�����Զ������
        '�˿������
        refund_date = Request.Form("WIDrefund_date")
        '�����ʽ����[4λ]-��[2λ]-��[2λ] Сʱ[2λ 24Сʱ��]:��[2λ]:��[2λ]���磺2007-10-01 13:13:13
        '���κ�
        batch_no = Request.Form("WIDbatch_no")
        '�����ʽ����������[8λ]+���к�[3��24λ]���磺201008010000001
        '�˿����
        batch_num = Request.Form("WIDbatch_num")
        '�������detail_data��ֵ�У���#���ַ����ֵ�������1�����֧��1000�ʣ�����#���ַ����ֵ�����999����
        '�˿���ϸ����
        detail_data = Request.Form("WIDdetail_data")
        '��������ʽ��μ��ӿڼ����ĵ�

'/////////////////////�������/////////////////////

'���������������
sParaTemp = Array("service=refund_fastpay_by_platform_pwd","partner="&partner,"_input_charset="&input_charset  ,"notify_url="&notify_url   ,"refund_date="&refund_date   ,"batch_no="&batch_no   ,"batch_num="&batch_num   ,"detail_data="&detail_data  )

'��������
Set objSubmit = New AlipaySubmit
sHtml = objSubmit.BuildRequestForm(sParaTemp, "get", "ȷ��")
response.Write sHtml


%>
<html>
<head>
	<META http-equiv=Content-Type content="text/html; charset=gb2312">
<title>֧������ʱ���������˿����ܽӿ�</title>
</head>
<body>
</body>
</html>
