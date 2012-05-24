package models.payment.kuaiqian;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import play.Logger;
import play.Play;


public class KuaiqianPkiPair {
	public static String signMsg( String signMsg) {
		String base64 = "";
		try {
			// 密钥仓库
			KeyStore ks = KeyStore.getInstance("PKCS12");

			// 读取密钥仓库（相对路径）
			String privateKeyPath = KuaiqianConfig.PRIVATE_KEY_PATH;
			FileInputStream ksfis = new FileInputStream(privateKeyPath);
			BufferedInputStream ksbufin = new BufferedInputStream(ksfis);

			char[] keyPwd = KuaiqianConfig.KEY_PWD.toCharArray();
			ks.load(ksbufin, keyPwd);
			// 从密钥仓库得到私钥
			PrivateKey priK = (PrivateKey) ks.getKey("test-alias", keyPwd);
			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initSign(priK);
			signature.update(signMsg.getBytes("utf-8"));
			sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
			base64 = encoder.encode(signature.sign());

		} catch(FileNotFoundException e){
            Logger.error(e,"can not find private key file of 99bill kuaiqian:" + KuaiqianConfig.PRIVATE_KEY_PATH);
		}catch (Exception ex) {
            Logger.error(ex, "load private key of 99bill / kuaiqian failed:" + KuaiqianConfig.PRIVATE_KEY_PATH);
		}
		return base64;
	}
	public static  boolean enCodeByCer( String val, String msg) {
		boolean flag = false;
		try {
			String path = KuaiqianConfig.PUBLIC_KEY_PATH;

			InputStream inStream = new FileInputStream(path);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
			//获得公钥
			PublicKey pk = cert.getPublicKey();
			//签名
			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initVerify(pk);
			signature.update(val.getBytes());
			//解码
			sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
			flag = signature.verify(decoder.decodeBuffer(msg));
		} catch (Exception e) {
            Logger.error(e, "load public key of 99bill / kuaiqian failed:" + KuaiqianConfig.PRIVATE_KEY_PATH);
		}
		return flag;
	}
}

