<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
	<title>֧������ʱ���˽��׽ӿڽӿ�</title>
</head>
<?php
/* *
 * ���ܣ���ʱ���˽��׽ӿڽ���ҳ
 * �汾��3.3
 * �޸����ڣ�2012-07-23
 * ˵����
 * ���´���ֻ��Ϊ�˷����̻����Զ��ṩ���������룬�̻����Ը����Լ���վ����Ҫ�����ռ����ĵ���д,����һ��Ҫʹ�øô��롣
 * �ô������ѧϰ���о�֧�����ӿ�ʹ�ã�ֻ���ṩһ���ο���

 *************************ע��*************************
 * ������ڽӿڼ��ɹ������������⣬���԰��������;�������
 * 1���̻��������ģ�https://b.alipay.com/support/helperApply.htm?action=consultationApply�����ύ���뼯��Э�������ǻ���רҵ�ļ�������ʦ������ϵ��Э�����
 * 2���̻��������ģ�http://help.alipay.com/support/232511-16307/0-16307.htm?sh=Y&info_type=9��
 * 3��֧������̳��http://club.alipay.com/read-htm-tid-8681712.html��
 * �������ʹ����չ���������չ���ܲ�������ֵ��
 */

require_once("alipay.config.php");
require_once("lib/alipay_submit.class.php");

/**************************�������**************************/

        //֧������
        $payment_type = "1";
        //��������޸�
        //�������첽֪ͨҳ��·��
        $notify_url = "http://www.xxx.com/create_direct_pay_by_user-PHP-GBK/notify_url.php";
        //��http://��ʽ������·�������ܼ�?id=123�����Զ������
        //ҳ����תͬ��֪ͨҳ��·��
        $return_url = "http://www.xxx.com/create_direct_pay_by_user-PHP-GBK/return_url.php";
        //��http://��ʽ������·�������ܼ�?id=123�����Զ������������д��http://localhost/
        //����֧�����ʻ�
        $seller_email = $_POST['WIDseller_email'];
        //����
        //�̻�������
        $out_trade_no = $_POST['WIDout_trade_no'];
        //�̻���վ����ϵͳ��Ψһ�����ţ�����
        //��������
        $subject = $_POST['WIDsubject'];
        //����
        //������
        $total_fee = $_POST['WIDtotal_fee'];
        //����
        //��������
        $body = $_POST['WIDbody'];
        //��Ʒչʾ��ַ
        $show_url = $_POST['WIDshow_url'];
        //����http://��ͷ������·�������磺http://www.xxx.com/myorder.html
        //������ʱ���
        $anti_phishing_key = "";
        //��Ҫʹ����������ļ�submit�е�query_timestamp����
        //�ͻ��˵�IP��ַ
        $exter_invoke_ip = $_POST['WIDexter_invoke_ip'];
        //�Ǿ�����������IP��ַ���磺221.0.0.1


/************************************************************/

//����Ҫ����Ĳ������飬����Ķ�
$parameter = array(
		"service" => "create_direct_pay_by_user",
		"partner" => trim($alipay_config['partner']),
		"payment_type"	=> $payment_type,
		"notify_url"	=> $notify_url,
		"return_url"	=> $return_url,
		"seller_email"	=> $seller_email,
		"out_trade_no"	=> $out_trade_no,
		"subject"	=> $subject,
		"total_fee"	=> $total_fee,
		"body"	=> $body,
		"show_url"	=> $show_url,
		"anti_phishing_key"	=> $anti_phishing_key,
		"exter_invoke_ip"	=> $exter_invoke_ip,
		"_input_charset"	=> trim(strtolower($alipay_config['input_charset']))
);

//��������
$alipaySubmit = new AlipaySubmit($alipay_config);
$html_text = $alipaySubmit->buildRequestForm($parameter,"get", "ȷ��");
echo $html_text;

?>
</body>
</html>