package models.payment.sina;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignAndVerify {
/*
	private static final Logger logger = LoggerFactory.getLogger(SignAndVerify.class);

	public static String sign(String alg, String content, PrivateKey privateKey) throws SignatureException,
			InvalidKeyException, NoSuchAlgorithmException {
		logger.trace("content will be signed:{}", content);
		Signature signalg = Signature.getInstance(alg);
		signalg.initSign(privateKey);
		signalg.update(content.getBytes(Charsets.UTF_8));
		byte[] signature = signalg.sign();
		return Base64.encodeBase64String(signature);
	}

	public static boolean verify(String alg, String signature, String content, PublicKey publicKey)
			throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
		Signature verifyalg = Signature.getInstance(alg);
		verifyalg.initVerify(publicKey);

		verifyalg.update(content.getBytes(Charsets.UTF_8));

		return verifyalg.verify(Base64.decodeBase64(signature));
	}

*/
}
