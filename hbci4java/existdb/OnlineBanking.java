package hbci4java.existdb;

import java.io.File;
import java.io.Console;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * Container for managing online banking accounts.
 */
public class OnlineBanking {

    private static Map<String, Object> accountMap = new LinkedHashMap<>();

    private static final java.text.SimpleDateFormat isoDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");

    public OnlineBanking() {
    }

    public static String defaultPassportPath(String accountId) {
        String base = System.getProperty("exist.home");
        if (base == null || base.length() == 0) {
            base = System.getProperty("user.home");
        }
        File dir = new File(base, "docman-passports");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, "passport_" + accountId + ".dat").getAbsolutePath();
    }

    public static void main(String[] args) throws Exception {
        Calendar cal = Calendar.getInstance();

        Integer pPort = 443;
        String pBlz = args[1];
        String pBic = args[2];
        String pIban = args[3];
        String pBankAccount = args[4];
        String pBankUserID = args[5];
        String pCustomerID = args[6];

        Integer pAuthType = 901;
        String pPin = null;

        String pMessageUrl = null;
        String pResponseUrl = null;
        String pUsername = null;
        String pPassword = null;

        String pAuthHeaderValue = "";
        if (args.length >= 11) {
            pMessageUrl = args[7];
            pResponseUrl = args[8];
            pUsername = args[9];
            pPassword = args[10];
            String auth = pUsername + ":" + pPassword;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            pAuthHeaderValue = "Basic " + new String(encodedAuth);
        }

        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_MONTH, -15);
        Date pStartDate = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, +14);
        Date pEndDate = cal.getTime();

        Console console = System.console();
        pPin = console.readLine("Bitte PIN eingeben:");

        System.out.println(initialize("Account1", "DE", pPort, pBlz, pBic, pIban, pBankAccount, pBankUserID,
                pCustomerID, "Geheim", pAuthType, pPin, pMessageUrl, pResponseUrl, pAuthHeaderValue,
                defaultPassportPath("Account1"), "interactive", true));
        System.out.println(getTransactions("Account1", pStartDate, pEndDate));
    }

    public static String initialize(String AccountID, String Country, Integer Port, String Blz,
            String Bic, String Iban, String BankAccount, String BankUserID, String CustomerID, String Secret,
            Integer AuthType, String Pin, String MessageUrl, String ResponseUrl, String AuthHeaderValue,
            String PassportPath, String Mode, Boolean Debug) throws Exception {

        if (accountMap.get(AccountID) != null) {
            return "Account ID '" + AccountID + "' already exist.";
        }

        String passport = PassportPath;
        if (passport == null || passport.trim().length() == 0) {
            passport = defaultPassportPath(AccountID);
        }
        String mode = Mode;
        if (mode == null || mode.trim().length() == 0) {
            mode = "interactive";
        }

        OnlineBankingAccount myAccount = new OnlineBankingAccount(Country, Port, Blz, Bic, Iban, BankAccount,
                BankUserID, CustomerID, Secret, AuthType, Pin, passport, MessageUrl, ResponseUrl, AuthHeaderValue,
                mode, Debug);
        accountMap.put(AccountID, myAccount);
        return myAccount.initialize();
    }

    public static String getTransactions(String AccountID, Date StartDate, Date EndDate) throws Exception {
        OnlineBankingAccount myAccount = (OnlineBankingAccount) accountMap.get(AccountID);
        if (myAccount == null) {
            return "Account ID '" + AccountID + "' does not exist.";
        }
        try {
            return myAccount.getTransactions(StartDate, EndDate);
        } catch (ScaRequiredException sca) {
            return "<Error code=\"SCA_REQUIRED\" type=\"" + ChallengeMedia.escapeXml(sca.getChallengeType())
                    + "\">" + sca.getChallengeXml() + "</Error>";
        }
    }

    public static String arangeBankTransfer(String AccountID, String RecipientCountry, String RecipientBic,
            String RecipientIban, String RecipientName, Date Date, double CurrencyAmount, String CurrencyUnit,
            String UsageText) throws Exception {
        OnlineBankingAccount myAccount = (OnlineBankingAccount) accountMap.get(AccountID);
        if (myAccount == null) {
            return "Account ID '" + AccountID + "' does not exist.";
        }
        try {
            return myAccount.arangeBankTransfer(RecipientCountry, RecipientBic, RecipientIban, RecipientName, Date,
                    CurrencyAmount, CurrencyUnit, UsageText);
        } catch (ScaRequiredException sca) {
            return "<Error code=\"SCA_REQUIRED\" type=\"" + ChallengeMedia.escapeXml(sca.getChallengeType())
                    + "\">" + sca.getChallengeXml() + "</Error>";
        }
    }

    public static String close(String AccountID) throws Exception {
        OnlineBankingAccount myAccount = (OnlineBankingAccount) accountMap.get(AccountID);
        if (myAccount == null) {
            return "Account ID '" + AccountID + "' does not exist.";
        }
        accountMap.remove(AccountID);
        myAccount.close();
        return "Account ID '" + AccountID + "' has been closed.";
    }

    public static String resetPassport(String AccountID, String PassportPath) throws Exception {
        closeQuiet(AccountID);
        String path = PassportPath;
        if (path == null || path.trim().length() == 0) {
            path = defaultPassportPath(AccountID);
        }
        File f = new File(path);
        boolean deleted = false;
        if (f.exists()) {
            deleted = f.delete();
        }
        return "<ResetPassport account=\"" + ChallengeMedia.escapeXml(AccountID) + "\" path=\""
                + ChallengeMedia.escapeXml(path) + "\" deleted=\"" + deleted + "\"/>";
    }

    private static void closeQuiet(String AccountID) {
        try {
            if (accountMap.get(AccountID) != null) {
                close(AccountID);
            }
        } catch (Exception ignored) {
        }
    }
}
