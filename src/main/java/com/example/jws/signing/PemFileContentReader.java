package com.example.jws.signing;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

/**
 * Gets the input stream and removes the header and footer that are not needed.
 * Decodes the base64 contents into raw bytes
 */
final class PemFileContentReader {

	private static final Pattern PEM_FILE_PATTERN = Pattern.compile("(?m)(?s)^---*BEGIN.*---*$(.*)^---*END.*---*$.*");

	static byte[] getContent(final InputStream resource) throws IOException {
		String pem = new String(IOUtils.toByteArray(resource), Charset.defaultCharset());
		String encoded = PEM_FILE_PATTERN.matcher(pem).replaceFirst("$1");
		return Base64.decodeBase64(encoded);
	}

}
