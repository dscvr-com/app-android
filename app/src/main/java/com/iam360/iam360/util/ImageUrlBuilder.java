package com.iam360.iam360.util;

import android.opengl.GLES20;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.iam360.iam360.model.Optograph;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2015-11-28
 */
public class ImageUrlBuilder {
//    private static final String OPTO_SPACE = "http://opto.space/";
//    private static final String OPTO_SPACE = "http://share.iam360.io/";
    private static final String OPTO_SPACE = "https://wow.dscvr.com/";
//    private static final String OPTO_SPACE = "http://192.168.1.69:8004/";
//    private static final String THUMBOR_URL = "http://images.optonaut.co";
    private static final String THUMBOR_URL = "http://images.dscvr.com";
//    private static final String THUMBOR_URL = "http://photos.dscvr.com";
//    private static final String THUMBOR_URL = "http://192.168.1.69:11000";
    //private static final String S3_URL = "optonaut-ios-beta-staging.s3.amazonaws.com";
//    private static final String S3_URL = "resources.optonaut.co.s3.amazonaws.com";
    private static final String S3_URL = "resources.staging-iam360.io.s3.amazonaws.com";
//    private static final String S3_URL = "bucket.dscvr.com";

    private static final String URL = "https://s3-ap-southeast-1.amazonaws.com/resources.staging-iam360.io/textures";
    private static final String SECURITY_KEY = "lBgF7SQaW3TDZ75ZiCuPXIDyWoADA6zY3KUkro5i";
    private static final String IMAGE_BUCKET_URL = "https://bucket.dscvr.com/textures/";
    private static final String S3_FULL_URL = "https://s3-ap-southeast-1.amazonaws.com/resources.staging-iam360.io/textures/";

    // TODO: use special formula, use different HFOV for VRmode and normal feed
    private static final int CUBE_TEXTURE_SIZE = Math.min(1024, GLES20.GL_MAX_TEXTURE_SIZE);
    // private static final int CUBE_TEXTURE_SIZE = Math.min((int) (Constants.getInstance().getDisplayMetrics().widthPixels * 360 / (Constants.getInstance().getHFOV() * Math.PI)), GLES20.GL_MAX_TEXTURE_SIZE);

    private static final int SUB_X = 0;
    private static final int SUB_Y = 0;
    private static final int SUB_D = 1;
    private static final int PX_D = CUBE_TEXTURE_SIZE;

    public static String buildImageUrl(String personId, String assetId, int width, int height) {
        String urlPartToSign = String.format("%sx%s/%s/persons/%s/%s.jpg", width, height, S3_URL, personId, assetId);

        return getSignedUrl(urlPartToSign);
    }


    public static String buildSmallCubeUrl(Optograph optograph, boolean isLeftId, int face) {
        String id = optograph.getId();
        if (!optograph.is_local()) {
//            http://images.iam360.io/E89S0VsHDDD5cVmeEtLrawAyhfM=/0x0/filters:subface(0,0,1,1024)/resources.staging-iam360.io.s3.amazonaws.com/textures/05353858-493d-46b4-be79-ed759f427e82/l0.jpg
//            https://images.iam360.io/C501kCsTsnd-WoV_nqT_zApYiYM=/100x100/resources.staging-iam360.io.s3.amazonaws.com/textures/93ab6abc-2e6b-4abb-838e-5efb707699bb/l0.jpg
            String sideLetter = isLeftId ? "l" : "r";
            String urlPartToSign = String.format(("100x100/%s/textures/%s/%s%s.jpg"), S3_URL, id, sideLetter, face);
            String signedUrl = getSignedUrl(urlPartToSign);
            return signedUrl;
        } else {
            String side = isLeftId ? "left/" : "right/";
            String path = CameraUtils.PERSISTENT_STORAGE_PATH + id + "/" + side + face + ".jpg";
            Timber.v("local optograph path: %s", path);
            return path;
        }
    }

    public static String buildCubeUrl(Optograph optograph, boolean isLeftId, int face) {
        String id = optograph.getId();
        if (!optograph.is_local()) {
            String sideLetter = isLeftId ? "l" : "r";
//            String urlPartToSign = String.format(("0x0/filters:subface(%s,%s,%s,%s)/%s/textures/%s/%s%s.jpg"), SUB_X, SUB_Y, SUB_D, PX_D, S3_URL, id, sideLetter, face);
//            String signedUrl = getSignedUrl(urlPartToSign);
//             return signedUrl;
            // this two line for direct call
            String urlPartToSign = String.format(("%s/%s/%s%s.jpg"), URL, id, sideLetter, face);
            return urlPartToSign;
        } else {
            String side = isLeftId ? "left/" : "right/";
            String path = CameraUtils.PERSISTENT_STORAGE_PATH + id + "/" + side + face + ".jpg";
            Timber.v("local optograph path: %s", path);
            return path;
        }
    }

    public static String buildPlaceholderUrl(Optograph optograph, boolean isLeftId, int face) {
        String id = optograph.getId();
        if (!optograph.is_local()) {
            String sideLetter = isLeftId ? "l" : "r";
            String urlPartToSign = String.format(("0x0/filters:subface(%s,%s,%s,%s)/%s/textures/%s/%s%s.jpg"), SUB_X, SUB_Y, SUB_D, PX_D, S3_URL, id, sideLetter, face);
//            String urlPartToSign = String.format(("0x0/filters:subface(%s,%s,%s,%s)/%s/textures/%s/placeholder.jpg"), SUB_X, SUB_Y, SUB_D, PX_D, S3_URL, id);
            String signedUrl = getSignedUrl(urlPartToSign);
            return signedUrl;
        } else {
            String side = isLeftId ? "left/" : "right/";
            String path = CameraUtils.PERSISTENT_STORAGE_PATH + id + "/" + side + face + ".jpg";
            Timber.v("local optograph path: %s", path);
            return path;
        }

    }

    public static String buildWebViewerUrl(String share_alias) {
        return OPTO_SPACE + share_alias;
    }

    public static String buildImagePreviewUrl(String optoId) {
        return S3_FULL_URL + optoId + "/frame1.jpg";
    }

    public static String buildVideoUrl(String optoId) {
        return S3_FULL_URL + optoId + "/pan.mp4";
    }

    private static String getSignedUrl(String urlPartToSign) {
        try {
            String hmacUrlPart = hmacSha1(urlPartToSign, SECURITY_KEY);
//            return String.format("%s/%s/%s", THUMBOR_URL, hmacUrlPart, urlPartToSign);
            return String.format("%s/%s/%s", THUMBOR_URL, "unsafe", urlPartToSign);
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
