<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
	<title>֧������ʱ���������˿����ܽӿڽӿ�</title>
</head>
<?php
/* *
 * ���ܣ���ʱ���������˿����ܽӿڽ���ҳ
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

        //�������첽֪ͨҳ��·��
        $notify_url = "http://www.xxx.com/refund_fastpay_by_platform_pwd-PHP-GBK/notify_url.php";
        //��http://��ʽ������·�����������?id=123�����Զ������
        //�˿������
        $refund_date = $_POST['WIDrefund_date'];
        //�����ʽ����[4λ]-��[2λ]-��[2λ] Сʱ[2λ 24Сʱ��]:��[2λ]:��[2λ]���磺2007-10-01 13:13:13
        //���κ�
        $batch_no = $_POST['WIDbatch_no'];
        //�����ʽ����������[8λ]+���к�[3��24λ]���磺201008010000001
        //�˿����
        $batch_num = $_POST['WIDbatch_num'];
        //�������detail_data��ֵ�У���#���ַ����ֵ�������1�����֧��1000�ʣ�����#���ַ����ֵ�����999����
        //�˿���ϸ����
        $detail_data = $_POST['WIDdetail_data'];
        //��������ʽ��μ��ӿڼ����ĵ�


/************************************************************/

//����Ҫ����Ĳ������飬����Ķ�
$parameter = array(
		"service" => "refund_fastpay_by_platform_pwd",
		"partner" => trim($alipay_config['partner']),
		"notify_url"	=> $notify_url,
		"refund_date"	=> $refund_date,
		"batch_no"	=> $batch_no,
		"batch_num"	=> $batch_num,
		"detail_data"	=> $detail_data,
		"_input_charset"	=> trim(strtolower($alipay_config['input_charset']))
);

//��������
$alipaySubmit = new AlipaySubmit($alipay_config);
$html_text = $alipaySubmit->buildRequestForm($parameter,"get", "ȷ��");
echo $html_text;

?>
</body>
</html>