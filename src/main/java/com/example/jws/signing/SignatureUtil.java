package com.example.jws.signing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;

public class SignatureUtil {

	private static String[] CRIT = new String[] { "iat" };
	private static JWSAlgorithm alg = JWSAlgorithm.RS256;
	private static final String kID = "dGhlIHF1aWNrIGJyb3duIGZveCBqdW1wcyBvdmVyIHRoZSBsYXp5IGRvZw";//This sample Kid 

	public static void main(String arg[]) {
		try {
			RSAPrivateKey rasPrivateKey = loadTestPrivateKey();
			String file = "src/main/resources/RequestPayload.json";
			String json = readFileAsString(file);
			// For iat
			// Identifies the time at which the message was signed. It can be used to
			// determine the age of the signature. Its value must be a number containing a
			// NumericDate value (epoch timestamp generated based on UTC timestamp). Signer
			// must always populate it. Verifier might choose to validate it to prevent
			// replay attacks.
			Long iat = System.currentTimeMillis() / 1000L;
			String signedRequest = createSignature(rasPrivateKey, json, kID, iat.toString());
			System.out.println("signedRequest " + signedRequest);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String readFileAsString(String file) throws Exception {
		return new String(Files.readAllBytes(Paths.get(file)));
	}

	// Load signing key to and convert it into PKCS8EncodedKeySpec
	private static RSAPrivateKey loadTestPrivateKey()
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		System.out.println("Executing-Loading_Private_Key");
		InputStream certIn = SignatureUtil.class.getClassLoader().getResourceAsStream("signing-private-key.pem");
		return (RSAPrivateKey) KeyFactory.getInstance("RSA")
				.generatePrivate(new PKCS8EncodedKeySpec(PemFileContentReader.getContent(certIn)));
	}

	// Creating the signature with the key, payload, kid, iat. And returns JWS
	// signature

	private static String createSignature(RSAPrivateKey key, String payload, String kid, String iat) throws Exception {
		System.out.println("Executing-Creating_Signature");
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("alg", alg.getName());
		map.put("kid", kid);
		map.put("iat", iat);
		map.put("crit", CRIT);

		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
		final String header = Base64.getUrlEncoder().withoutPadding().encodeToString(gson.toJson(map).getBytes());

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(header.getBytes());
		os.write(0x2e); // ascii for "."
		os.write(payload.getBytes());

		// please note that the new JWSHeader(alg) just selects the algorithm to use to
		// sign the request with.
		return header + ".." + new RSASSASigner(key).sign(new JWSHeader(alg), os.toByteArray());
	}

}
