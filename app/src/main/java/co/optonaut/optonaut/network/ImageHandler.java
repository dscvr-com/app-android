package co.optonaut.optonaut.network;

import android.opengl.GLES20;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Nilan Marktanner
 * @date 2015-11-28
 */
public class ImageHandler {
    private static final String THUMBOR_URL = "http://images.optonaut.co";
    private static final String S3_URL = "optonaut-ios-beta-staging.s3.amazonaws.com";
    private static final String SECURITY_KEY = "lBgF7SQaW3TDZ75ZiCuPXIDyWoADA6zY3KUkro5i";

    // use maximal texture size for now
    private static final int TEXTURE_SIZE = Math.min(8192, GLES20.GL_MAX_TEXTURE_SIZE);

    public static String buildImageUrl(String id, int width, int height) {
        String urlPartToSign = String.format("%sx%s/%s/original/%s.jpg", width, height, S3_URL, id);

        return getSignedUrl(urlPartToSign);
    }

    public static String buildTextureUrl(String id) {
        String urlPartToSign = String.format("0x0/filters:square(%s)/%s/original/%s.jpg", TEXTURE_SIZE, S3_URL, id);

        return getSignedUrl(urlPartToSign);
    }

    private static String getSignedUrl(String urlPartToSign) {
        try {
            String hmacUrlPart = hmacSha1(urlPartToSign, SECURITY_KEY);
            return String.format("%s/%s/%s", THUMBOR_URL, hmacUrlPart, urlPartToSign);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String hmacSha1(String value, String key)
            throws UnsupportedEncodingException, NoSuchAlgorithmException,
            InvalidKeyException {
        String type = "HmacSHA1";
        SecretKeySpec secret = new SecretKeySpec(key.getBytes(), type);
        Mac mac = Mac.getInstance(type);
        mac.init(secret);
        byte[] bytes = mac.doFinal(value.getBytes());
        return Base64.encodeToString(bytes, Base64.URL_SAFE);
    }

}
