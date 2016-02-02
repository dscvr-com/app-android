package co.optonaut.optonaut.util;

import android.opengl.GLES20;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2015-11-28
 */
public class ImageUrlBuilder {
    private static final String OPTO_SPACE = "http://opto.space/";
    private static final String THUMBOR_URL = "http://images.optonaut.co";
    //private static final String S3_URL = "optonaut-ios-beta-staging.s3.amazonaws.com";
    private static final String S3_URL = "resources.optonaut.co.s3.amazonaws.com";
    private static final String SECURITY_KEY = "lBgF7SQaW3TDZ75ZiCuPXIDyWoADA6zY3KUkro5i";

    // TODO: use special formula, use different HFOV for VRmode and normal feed
    // private static final int CUBE_TEXTURE_SIZE = Math.min(1024, GLES20.GL_MAX_TEXTURE_SIZE);
    // private static final int CUBE_TEXTURE_SIZE = Math.min((int) (Constants.getInstance().getDisplayMetrics().widthPixels * 360 / (Constants.getInstance().getHFOV() * Math.PI)), GLES20.GL_MAX_TEXTURE_SIZE);

    private static final int SUB_X = 0;
    private static final int SUB_Y = 0;
    private static final int SUB_D = 1;

    public static String buildImageUrl(String personId, String assetId, int width, int height) {
        String urlPartToSign = String.format("%sx%s/%s/persons/%s/%s.jpg", width, height, S3_URL, personId, assetId);

        return getSignedUrl(urlPartToSign);
    }

    public static String buildCubeUrl(String id, boolean isLeftId, int face, float hfov) {
        String sideLetter = isLeftId ? "l" : "r";
        String urlPartToSign = String.format(("0x0/filters:subface(%s,%s,%s,%s)/%s/textures/%s/%s%s.jpg"), SUB_X, SUB_Y, SUB_D, getPixelDimension(hfov), S3_URL, id, sideLetter, face);
        String signedUrl = getSignedUrl(urlPartToSign);
        Timber.v("hfov: %s, cube size: %s, url: %s", hfov, getPixelDimension(hfov), signedUrl);
        return signedUrl;
    }

    private static int getPixelDimension(float hfov) {
        return Math.min((int) (Constants.getInstance().getDisplayMetrics().widthPixels * 360 / (hfov * Math.PI)), GLES20.GL_MAX_TEXTURE_SIZE);
    }


    public static String buildWebViewerUrl(String share_alias) {
        return OPTO_SPACE + share_alias;
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
