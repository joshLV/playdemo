<%
' ������AlipaySubmit
' ���ܣ�֧�������ӿ������ύ��
' ��ϸ������֧�������ӿڱ�HTML�ı�����ȡԶ��HTTP����
' �汾��3.3
' �޸����ڣ�2012-07-13
' ˵����
' ���´���ֻ��Ϊ�˷����̻����Զ��ṩ���������룬�̻����Ը����Լ���վ����Ҫ�����ռ����ĵ���д,����һ��Ҫʹ�øô��롣
' �ô������ѧϰ���о�֧�����ӿ�ʹ�ã�ֻ���ṩһ���ο�
%>

<!--#include file="alipay_config.asp"-->
<!--#include file="alipay_core.asp"-->

<%

'֧�������ص�ַ���£�
GATEWAY_NEW = "https://mapi.alipay.com/gateway.do?"

Class AlipaySubmit

	''
	' ����ǩ�����
	' param sParaSort ��ǩ��������
	' return ǩ������ַ���
	Private Function BuildRequestMysign(sParaSort)
			
		'����������Ԫ�أ����ա�����=����ֵ����ģʽ�á�&���ַ�ƴ�ӳ��ַ���
		prestr = CreateLinkstring(sParaSort)
		
		'���ǩ�����
		 Select Case sign_type
		 	Case "MD5" BuildRequestMysign = Md5Sign(prestr,key,input_charset)
			Case Else BuildRequestMysign = ""
		 End Select
	End Function

	''
	' ����Ҫ�����֧�����Ĳ�������
	' param sParaTemp ����ǰ�Ĳ�������
	' return Ҫ����Ĳ�������
	Private Function BuildRequestPara(sParaTemp)
		Dim mysign
		'����ǩ����������
		sPara = FilterPara(sParaTemp)
		
		'�����������������
		sParaSort = SortPara(sPara)
		
		'���ǩ�����
		mysign = BuildRequestMysign(sParaSort)
		
		'ǩ�������ǩ����ʽ���������ύ��������
		nCount = ubound(sParaSort)
		Redim Preserve sParaSort(nCount+1)
		sParaSort(nCount+1) = "sign="&mysign
		Redim Preserve sParaSort(nCount+2)
		sParaSort(nCount+2) = "sign_type="&sign_type

		BuildRequestPara = sParaSort
	End Function
	
	''
	' ����Ҫ�����֧�����Ĳ��������ַ���
	' param sParaTemp ����ǰ�Ĳ�������
	' return Ҫ����Ĳ��������ַ���
	Private Function BuildRequestParaToString(sParaTemp)
		Dim sRequestData
		'��ǩ�������������
		sPara = BuildRequestPara(sParaTemp)
		'�Ѳ�����������Ԫ�أ����ա�����=����ֵ����ģʽ�á�&���ַ�ƴ�ӳ��ַ��������Ҷ�����urlencode���봦��
		sRequestData = CreateLinkStringUrlEncode(sPara)
		
		BuildRequestParaToString = sRequestData
	End Function

	''
	' ���������Ա�HTML��ʽ���죨Ĭ�ϣ�
	' param sParaTemp ����ǰ�Ĳ�������
	' param sMethod �ύ��ʽ������ֵ��ѡ��post��get
	' param sButtonValue ȷ�ϰ�ť��ʾ����
	' return �ύ��HTML�ı�
	Public Function BuildRequestForm(sParaTemp, sMethod, sButtonValue)
		Dim sHtml, nCount
		'�������������
		sPara = BuildRequestPara(sParaTemp)
		
		sHtml = "<form id='alipaysubmit' name='alipaysubmit' action='"& GATEWAY_NEW &"_input_charset="&input_charset&"' method='"&sMethod&"'>"
		
		nCount = ubound(sPara)
		For i = 0 To nCount
			'��sPara���������Ԫ�ظ�ʽ��������=ֵ���ָ��
			iPos = Instr(sPara(i),"=")			'���=�ַ���λ��
			nLen = Len(sPara(i))				'����ַ�������
			sItemName = left(sPara(i),iPos-1)	'��ñ�����
			sItemValue = right(sPara(i),nLen-iPos)'��ñ�����ֵ
		
			sHtml = sHtml & "<input type='hidden' name='"& sItemName &"' value='"& sItemValue &"'/>"
		next

		'submit��ť�ؼ��벻Ҫ����name����
		'submit��ťĬ������Ϊ����ʾ
		sHtml = sHtml & "<input type='submit' value='"&sButtonValue&"' style='display:none;'></form>"
		
		sHtml = sHtml & "<script>document.forms['alipaysubmit'].submit();</script>"
		
		BuildRequestForm = sHtml
	End Function
	
	''
	' ����������ģ��Զ��HTTP��GET����ʽ���첢��ȡ֧����XML���ʹ�����
	' param sParaTemp ����ǰ�Ĳ�������
	' param sParaNode Ҫ�����XML�ڵ���
	' return ֧��������XMLָ���ڵ�����
	Public Function BuildRequestHttpXml(sParaTemp, sParaNode)
		Dim sUrl, objHttp, objXml, nCount, sParaXml()
		nCount = ubound(sParaNode)
		
		'��������������ַ���
		sRequestData = BuildRequestParaToString(sParaTemp)
		'���������ַ
		sUrl = GATEWAY_NEW & sRequestData

		'��ȡԶ������
		Set objHttp=Server.CreateObject("Microsoft.XMLHTTP")
		'���Microsoft.XMLHTTP���У���ô���滻����������д��볢��
		'Set objHttp = Server.CreateObject("Msxml2.ServerXMLHTTP.3.0")
		'objHttp.setOption 2, 13056
		objHttp.open "GET", sUrl, False, "", ""
		objHttp.send()
		Set objXml=Server.CreateObject("Microsoft.XMLDOM")
		objXml.Async=true
		objXml.ValidateOnParse=False
		objXml.Load(objHttp.ResponseXML)
		Set objHttp = Nothing
		
		set objXmlData = objXml.getElementsByTagName("alipay").item(0)
		If Isnull(objXmlData.selectSingleNode("alipay")) Then
			Redim Preserve sParaXml(1)
			sParaXml(0) = "���󣺷Ƿ�XML��ʽ����"
		Else
			If objXmlData.selectSingleNode("is_success").text = "T" Then
				For i = 0 To nCount
					Redim Preserve sParaXml(i+1)
					sParaXml(i) = objXmlData.selectSingleNode(sParaNode(i)).text
				Next
			Else
				Redim Preserve sParaXml(1)
				sParaXml(0) = "����"&objXmlData.selectSingleNode("error").text
			End If
		End If
		
		BuildRequestHttpXml = sParaXml
	End Function
	
	''
	' ����������ģ��Զ��HTTP��GET����ʽ���첢��ȡ֧�������������ʹ�����
	' param sParaTemp ����ǰ�Ĳ�������
	' return ֧����������
	Public Function BuildRequestHttpWord(sParaTemp)
		Dim sUrl, objHttp, sResponseTxt
		
		'��������������ַ���
		sRequestData = BuildRequestParaToString(sParaTemp)
		'���������ַ
		sUrl = GATEWAY_NEW & sRequestData

		'��ȡԶ������
		Set objHttp=Server.CreateObject("Microsoft.XMLHTTP")
		'���Microsoft.XMLHTTP���У���ô���滻����������д��볢��
		'Set objHttp = Server.CreateObject("Msxml2.ServerXMLHTTP.3.0")
		'objHttp.setOption 2, 13056
		objHttp.open "GET", sUrl, False, "", ""
		objHttp.send()
		sResponseTxt = objHttp.ResponseText
		Set objHttp = Nothing
		
		BuildRequestHttpWord = sResponseTxt
	End Function

	''
	' ���ڷ����㣬����֧����������ӿ�(query_timestamp)����ȡʱ����Ĵ�����
	' ע�⣺Զ�̽���XML������IIS�����������й�
	' return ʱ����ַ���
	Public Function Query_timestamp()
		Dim sUrl, encrypt_key
		sUrl = GATEWAY_NEW &"service=query_timestamp&partner="&partner
		encrypt_key = ""
		
		Dim objHttp, objXml
		Set objHttp=Server.CreateObject("Microsoft.XMLHTTP")
		'���Microsoft.XMLHTTP���У���ô���滻����������д��볢��
		'Set objHttp = Server.CreateObject("Msxml2.ServerXMLHTTP.3.0")
		'objHttp.setOption 2, 13056
		objHttp.open "GET", sUrl, False, "", ""
		objHttp.send()
		Set objXml=Server.CreateObject("Microsoft.XMLDOM")
		objXml.Async=true
		objXml.ValidateOnParse=False
		objXml.Load(objHttp.ResponseXML)
		Set objHttp = Nothing
		
		Set objXmlData = objXml.getElementsByTagName("encrypt_key")  '�ڵ������
		If Isnull(objXml.getElementsByTagName("encrypt_key")) Then
			encrypt_key = ""
		Else
			encrypt_key = objXmlData.item(0).childnodes(0).text
		End If

		Query_timestamp = encrypt_key
	End Function

End Class

%>