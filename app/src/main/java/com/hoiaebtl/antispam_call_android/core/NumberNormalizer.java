package com.hoiaebtl.antispam_call_android.core;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import android.util.Log;

public class NumberNormalizer {
    public static String normalize(String rawNumber, String defaultRegion) {
        if (rawNumber == null || rawNumber.isEmpty()) {
            return rawNumber;
        }
        try {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(rawNumber, defaultRegion);
            // Ép mọi định dạng (+84 hoặc 09) về chuẩn chuỗi quốc gia 09x... liên viết liền để so khớp DB
            String formatted = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
            return formatted.replace(" ", "");
        } catch (NumberParseException e) {
            Log.e("NumberNormalizer", "Không thể chuẩn hóa số: " + rawNumber, e);
            // Fallback trả về nguyên gốc nếu bị lỗi parse
            return rawNumber.replaceAll("[^0-9+]", "");
        }
    }
}
