package hills.sms.texter.util;

import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;


/**
 * Created by alex on 11/5/2014.
 */

public class PhoneNumberFormatter {

    public static class InvalidNumberException extends Throwable{
        public InvalidNumberException(String s){
            super(s);
        }
    }

    public static boolean isValidNumber(String number) {
        return number.matches("^\\+[0-9]{10,}");
    }

    private static String impreciseFormatNumber(String number, String localNumber)
            throws InvalidNumberException
    {
        number = number.replaceAll("[^0-9+]", "");

        if (number.charAt(0) == '+')
            return number;

        if (localNumber.charAt(0) == '+')
            localNumber = localNumber.substring(1);

        if (localNumber.length() == number.length() || number.length() > localNumber.length())
            return "+" + number;

        int difference = localNumber.length() - number.length();

        return "+" + localNumber.substring(0, difference) + number;
    }

    public static String formatNumberInternational(String number) {
        try {
            PhoneNumberUtil util     = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber parsedNumber = util.parse(number, null);
            return util.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        } catch (NumberParseException e) {
            Log.w("PhoneNumberFormatter", e);
            return number;
        }
    }

    public static String formatNumber(String number, String localNumber)
            throws InvalidNumberException
    {
        if (number.contains("@")) {
            throw new InvalidNumberException("Possible attempt to use email address.");
        }

        number = number.replaceAll("[^0-9+]", "");

        if (number.length() == 0) {
            throw new InvalidNumberException("No valid characters found.");
        }

        if (number.charAt(0) == '+')
            return number;

        try {
            PhoneNumberUtil util          = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber localNumberObject = util.parse(localNumber, null);

            String localCountryCode       = util.getRegionCodeForNumber(localNumberObject);
            Log.w("PhoneNumberFormatter", "Got local CC: " + localCountryCode);

            Phonenumber.PhoneNumber numberObject      = util.parse(number, localCountryCode);
            return util.format(numberObject, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            Log.w("PhoneNumberFormatter", e);
            return impreciseFormatNumber(number, localNumber);
        }
    }

    public static String getRegionDisplayName(String regionCode) {
        return (regionCode == null || regionCode.equals("ZZ") || regionCode.equals(PhoneNumberUtil.REGION_CODE_FOR_NON_GEO_ENTITY))
                ? "Unknown country" : new Locale("", regionCode).getDisplayCountry(Locale.getDefault());
    }

    public static String formatE164(String countryCode, String number) {
        try {
            PhoneNumberUtil util     = PhoneNumberUtil.getInstance();
            int parsedCountryCode    = Integer.parseInt(countryCode);
            Phonenumber.PhoneNumber parsedNumber = util.parse(number,
                    util.getRegionCodeForCountryCode(parsedCountryCode));

            return util.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException npe) {
            Log.w("CreateAccountActivity", npe);
        } catch (NumberFormatException nfe) {
            Log.w("CreateAccountActivity", nfe);
        }

        return "+"                                                     +
                countryCode.replaceAll("[^0-9]", "").replaceAll("^0*", "") +
                number.replaceAll("[^0-9]", "");
    }

    public static String getInternationalFormatFromE164(String e164number) {
        try {
            PhoneNumberUtil util     = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber parsedNumber = util.parse(e164number, null);
            return util.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        } catch (NumberParseException e) {
            Log.w("PhoneNumberFormatter", e);
            return e164number;
        }
    }


}
