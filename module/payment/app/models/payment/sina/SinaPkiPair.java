package models.payment.sina;

import org.apache.commons.codec.binary.Base64;
import play.Logger;
import play.libs.Codec;
import play.vfs.VirtualFile;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;


public class SinaPkiPair {
	public static String signMsg( String signMsg) {
		String base64 = "";
		try {
            VirtualFile file = VirtualFile.open(SinaConfig.PRIVATE_KEY_PATH);
            byte[] keyBytes = Codec.decodeBASE64(file.contentAsString());
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);

            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initSign(privateK);
            signature.update(signMsg.getBytes("utf-8"));

            base64 = Codec.encodeBASE64(signature.sign());

		} catch (Exception ex) {
            Logger.error(ex, "load private key of sina / sina failed:" + SinaConfig.PRIVATE_KEY_PATH);
		}
		return base64;
	}
	public static  boolean notifyResult(String val, String msg) {
		boolean flag = false;
		try {
			String path = SinaConfig.PUBLIC_KEY_PATH;

			InputStream inStream = new FileInputStream(path);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
			//获得公钥
			PublicKey pk = cert.getPublicKey();
			//签名
			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initVerify(pk);
			signature.update(val.getBytes("utf-8"));
			//解码
			sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
			flag = signature.verify(decoder.decodeBuffer(msg));
		} catch (Exception e) {
            Logger.error(e, "load public key of 99bill / sina failed:" + SinaConfig.PUBLIC_KEY_PATH);
		}
		return flag;
	}
}

