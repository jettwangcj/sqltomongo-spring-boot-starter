package com.rrtv.util;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

public class StringUtils extends org.apache.commons.lang3.StringUtils {

    private static final Logger logger = LoggerFactory.getLogger(StringUtils.class);

    public static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * get the md5 hash of a string
     *
     * @param str
     * @return
     */
    public static String md5(String str) {

        return md5(str, DEFAULT_ENCODING);
    }

    public static String md5(String str, String encoding) {

        if (str == null) {
            return null;
        }

        MessageDigest messageDigest;

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes(encoding));
        } catch (NoSuchAlgorithmException e) {
            if (logger.isErrorEnabled()) {
                logger.error("MD5 algorithm not supported", e);
            }
            return str;
        } catch (UnsupportedEncodingException e) {
            if (logger.isErrorEnabled()) {
                logger.error(formatMessage("Encoding \"{0}\" not supported for MD5 algorithm.", DEFAULT_ENCODING), e);
            }
            return str;
        }

        byte[] byteArray = messageDigest.digest();

        return toHexString(byteArray);
    }

    public static String toHexString(byte[] byteArray) {
        StringBuilder sha1StrBuff = new StringBuilder();

        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
                sha1StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            } else {
                sha1StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
            }
        }

        return sha1StrBuff.toString();
    }

    public static String formatMessage(String msg, Object... args) {
        if (StringUtils.isBlank(msg) || ArrayUtils.getLength(args) == 0) {
            return msg;
        } else {
            return MessageFormat.format(msg, args);
        }
    }

}
