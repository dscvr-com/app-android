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

    private static final int TEXTURE_SIZE = Math.min(2048, GLES20.GL_MAX_TEXTURE_SIZE);
    private static final int PREVIEW_TEXTURE_SIZE = Math.min(256, GLES20.GL_MAX_TEXTURE_SIZE);

    private static final int CUBE_TEXTURE_SIZE = 1024;

    private static final int SUB_X = 0;
    private static final int SUB_Y = 0;
    private static final int SUB_D = 1;
    private static final int PX_D = CUBE_TEXTURE_SIZE;

    public static String buildImageUrl(String id, int width, int height) {
        String urlPartToSign = String.format("%sx%s/%s/original/%s.jpg", width, height, S3_URL, id);

        return getSignedUrl(urlPartToSign);
    }

    public static String buildTextureUrl(String id) {
        return buildSquareUrl(id, TEXTURE_SIZE);
    }

    public static String buildPreviewTextureUrl(String id) {
        return buildSquareUrl(id, PREVIEW_TEXTURE_SIZE);
    }

    private static String buildSquareUrl(String id, int length) {
        String urlPartToSign = String.format("0x0/filters:square(%s)/%s/original/%s.jpg", length, S3_URL, id);
        return getSignedUrl(urlPartToSign);
    }

    public static String buildCubeUrl(String id, int face) {
        String urlPartToSign = String.format("0x0/filters:cube(%s,%s,%s,%s,%s)/%s/original/%s.jpg", face, SUB_X, SUB_Y, SUB_D, PX_D, S3_URL, id);
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
