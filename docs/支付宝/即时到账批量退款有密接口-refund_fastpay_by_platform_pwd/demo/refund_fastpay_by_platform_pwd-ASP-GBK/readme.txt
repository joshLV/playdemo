
            �q�����������������������������������������������r
    ����������           ֧��������ʾ���ṹ˵��             ����������
            �t�����������������������������������������������s 
��                                                                  
��       �ӿ����ƣ�֧������ʱ���������˿����ܽӿڣ�refund_fastpay_by_platform_pwd��
�� ��    ����汾��3.3
         �������ԣ�ASP
         ��    Ȩ��֧�������й������缼�����޹�˾
��       �� �� �ߣ�֧�����̻���ҵ������֧����
         ��ϵ��ʽ���̻�����绰0571-88158090

    ������������������������������������������������������������������


��������������
 �����ļ��ṹ
��������������

refund_fastpay_by_platform_pwd-CSHARP-GBK
  ��
  ��class�����������������������������������������ļ���
  ��  ��
  ��  ��alipay_core.asp������������������������֧�����ӿڹ��ú����ļ�
  ��  ��
  ��  ��alipay_md5.asp ������������������������MD5ǩ�������ļ�
  ��  ��
  ��  ��alipay_notify.asp����������������������֧����֪ͨ�������ļ�
  ��  ��
  ��  ��alipay_submit.asp����������������������֧�������ӿ������ύ���ļ�
  ��  ��
  ��  ��alipay_config.asp�������������������������������ļ�
  ��
  ��log������������������������������������������־�ļ���
  ��
  ��alipayapi.asp������������������������������֧�����ӿ�����ļ�
  ��
  ��index.asp����������������������������������֧�����������ҳ��
  ��
  ��notify_url.asp �����������������������������������첽֪ͨҳ���ļ�
  ��
  ��readme.txt ��������������������������������ʹ��˵���ı�

��ע���
��Ҫ���õ��ļ��ǣ�
alipay_config.asp
alipayinterface.asp
notify_url.asp



������������������
 ���ļ������ṹ
������������������

alipay_core.asp

Function CreateLinkstring(sPara)
���ܣ�����������Ԫ�أ����ա�����=����ֵ����ģʽ�á�&���ַ�ƴ�ӳ��ַ���
���룺Array  sPara ��Ҫƴ�ӵ�����
�����String ƴ������Ժ���ַ���

Function CreateLinkstringUrlEncode(sPara)
���ܣ�����������Ԫ�أ����ա�����=����ֵ����ģʽ�á�&���ַ�ƴ�ӳ��ַ��������Ҷ�����URLENCODE����
���룺Array  sPara ��Ҫƴ�ӵ�����
�����String ƴ������Ժ���ַ���

Function FilterPara(sPara)
���ܣ���ȥ�����еĿ�ֵ��ǩ������
���룺Array  sPara ǩ��������
�����Array  ȥ����ֵ��ǩ�����������ǩ��������

Function SortPara(sPara)
���ܣ�����������
���룺Array  sPara ����ǰ������
�����Array  ����������

Function Md5Sign(prestr, key, input_charset)
���ܣ�MD5ǩ��
���룺String prestr ��Ҫǩ�����ַ���
      String key ˽Կ
      String input_charset �����ʽ
�����String ǩ�����

Function Md5Verify(prestr, sign, key, input_charset)
���ܣ�MD5ǩ��
���룺String prestr ��Ҫǩ�����ַ���
      String sign ǩ�����
      String key ˽Կ
      String input_charset �����ʽ
�����String ǩ�����

Function LogResult(sWord)
���ܣ�д��־��������ԣ�����վ����Ҳ���Ըĳɴ������ݿ⣩
���룺String sWord Ҫд����־����ı�����

Function GetDateTimeFormat()
���ܣ���ȡ��ǰʱ��
��ʽ����[4λ]-��[2λ]-��[2λ] Сʱ[2λ 24Сʱ��]:��[2λ]:��[2λ]���磺2007-10-01 13:13:13
�����String ʱ���ʽ�����
˵��������

Function GetDateTime()
���ܣ���ȡ��ǰʱ��
��ʽ����[4λ]��[2λ]��[2λ]Сʱ[2λ 24Сʱ��]��[2λ]��[2λ]���磺20071001131313
�����String ʱ���ʽ�����

Function DelStr(Str)
���ܣ����������ַ�
���룺String Str Ҫ�����˵��ַ���
�����String �ѱ����˵������ַ���

��������������������������������������������������������������

alipay_md5.asp

Public Function MD5(sMessage,input_charset)
���ܣ�MD5ǩ��
���룺String sMessage Ҫǩ�����ַ���
      String input_charset �����ʽ��utf-8��gbk
�����String ǩ�����

��������������������������������������������������������������

alipay_notify.asp

Public Function VerifyNotify()
���ܣ����notify_url��֤��Ϣ�Ƿ���֧���������ĺϷ���Ϣ
�����Bool  ��֤�����true/false

Public Function VerifyReturn()
���ܣ����return_url��֤��Ϣ�Ƿ���֧���������ĺϷ���Ϣ
�����Bool  ��֤�����true/false

Private Function GetSignVeryfy(sParaTemp)
���ܣ����ݷ�����������Ϣ������ǩ�����
���룺Array sParaTemp ֪ͨ�������Ĳ�������
��������ɵ�ǩ�����

Private Function GetResponse(notify_id)
���ܣ���ȡԶ�̷�����ATN���
���룺string notify_id ֪ͨУ��ID
�����������ATN����ַ���

Private Function GetRequestGet()
���ܣ���ȡ֧����GET����֪ͨ��Ϣ�����ԡ�������=����ֵ������ʽ�������
�����Array  request��������Ϣ��ɵ�����

Private Function GetRequestPost()
���ܣ���ȡ֧����POST����֪ͨ��Ϣ�����ԡ�������=����ֵ������ʽ�������
�����Array  request��������Ϣ��ɵ�����

��������������������������������������������������������������

alipay_submit.asp

Private Function BuildRequestMysign(sParaSort)
���ܣ�����ǩ�����
���룺Array sParaSort ��ǩ��������
�����String ǩ������ַ���

Private Function BuildRequestPara(sParaTemp)
���ܣ�����Ҫ�����֧�����Ĳ�������
���룺Array sParaTemp ����ǰ�Ĳ�������
�����Array Ҫ����Ĳ�������

Private Function BuildRequestParaToString(sParaTemp)
���ܣ�����Ҫ�����֧�����Ĳ�������
���룺Array sParaTemp ����ǰ�Ĳ�������
�����String Ҫ����Ĳ��������ַ���

Public Function BuildRequestForm(sParaTemp, sMethod, sButtonValue)
���ܣ����������Ա�HTML��ʽ���죨Ĭ�ϣ�
���룺Array sParaTemp ����ǰ�Ĳ�������
      string sMethod �ύ��ʽ������ֵ��ѡ��post��get
      string sButtonValue ȷ�ϰ�ť��ʾ����
�����String �ύ��HTML�ı�

Public Function BuildRequestHttpXml(sParaTemp, sParaNode)
���ܣ�����������ģ��Զ��HTTP��GET����ʽ���첢��ȡ֧����XML���ʹ�����
���룺Array sParaTemp ����ǰ�Ĳ�������
      Array sParaNode Ҫ�����XML�ڵ���
�����Array ֧��������XMLָ���ڵ�����

Public Function BuildRequestHttpWord(sParaTemp)
���ܣ�����������ģ��Զ��HTTP��GET����ʽ���첢��ȡ֧�������������ʹ�����
���룺Array sParaTemp ����ǰ�Ĳ�������
�����String ֧����������

Public Function Query_timestamp()
���ܣ����ڷ����㣬���ýӿ�query_timestamp����ȡʱ����Ĵ�����
�����String ʱ����ַ���



��������������������
 �������⣬��������
��������������������

����ڼ���֧�����ӿ�ʱ�������ʻ�������⣬��ʹ����������ӣ��ύ���롣
https://b.alipay.com/support/helperApply.htm?action=supportHome
���ǻ���ר�ŵļ���֧����ԱΪ������




