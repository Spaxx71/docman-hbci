package hbci4java.existdb;

import java.io.File;
import java.io.Console;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRKUms.UmsLine;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.callback.AbstractHBCICallback;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.BankInfo;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.manager.HBCIVersion;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Calendar;
import java.nio.charset.StandardCharsets;



/**
 * Container zum Verwalten und Steuern aller Accounts.
 * 
 */
public class OnlineBanking {

    private static Map<String, Object> accountMap = new LinkedHashMap<>();

    private static final java.text.SimpleDateFormat isoDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");

    private static final java.text.SimpleDateFormat isoDateTimeFormat = new java.text.SimpleDateFormat(
            "yyyy-MM-dd'T'hh:mm:ss");

    public OnlineBanking () {

    }

    /**
     * Main-Methode.
     * 
     * @param args
     * @throws Exception
     */
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
        if (args.length == 8) {
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
        //String startDate = isoDateFormat.format(pStartDate);
        cal.add(Calendar.DAY_OF_MONTH, +14);
        Date pEndDate = cal.getTime();
        //String EndDate = isoDateFormat.format(pEndDate);

        
        Console console = System.console();
        // BankUser = console.readLine("BankUser:");
        // BankAccount = console.readLine("BankAccount:");
        // Blz = console.readLine("BLZ:");
        // Port = console.readLine("Port:");
        // StartDate = console.readLine("Begin Datum (YYYY-MM-TT):");
        // EndDate = console.readLine("Ende Datum (YYYY-MM-TT):");
        pPin = console.readLine("Bitte PIN eingeben:");

        OnlineBanking myBanking = new OnlineBanking();
        System.out.println(myBanking.initialize("Account1","DE",pPort,pBlz,pBic,pIban,pBankAccount,pBankUserID,pCustomerID,"Geheim",pAuthType,pPin,pMessageUrl,pResponseUrl,pAuthHeaderValue,true));
        System.out.println(myBanking.getTransactions("Account1",pStartDate,pEndDate));

        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
    }

    /**
     * initialize
     * 
     * @param args
     * @throws Exception
     */
    public static String initialize (String AccountID, String Country, Integer Port, String Blz, 
        String Bic, String Iban,
        String BankAccount, String BankUserID, String CustomerID,
        String Secret, Integer AuthType, String Pin, 
        String MessageUrl, String ResponseUrl, String AuthHeaderValue, Boolean Debug) throws Exception {

        if (accountMap.get(AccountID) != null) 
            return "Account ID '" + AccountID + "' already exist.";
        else {
            OnlineBankingAccount myAccount = new OnlineBankingAccount (Country, Port, Blz, Bic, Iban, BankAccount, BankUserID, CustomerID,
                Secret, AuthType, Pin, "/tmp/passport_"+AccountID+".dat", MessageUrl, ResponseUrl, AuthHeaderValue, Debug);
            accountMap.put(AccountID, myAccount);
            return myAccount.initialize();
        }
    }
    
    /**
     * getTransactions
     * 
     * @param args
     * @throws Exception
     */
    public static String getTransactions(String AccountID, Date StartDate, Date EndDate) throws Exception {
        OnlineBankingAccount myAccount = (OnlineBankingAccount)accountMap.get(AccountID);
        if (myAccount == null) 
            return "Account ID '" + AccountID + "' does not exist.";
        else
            return myAccount.getTransactions(StartDate, EndDate);
    }

    /**
     * createTransfer
     * 
     * @param args
     * @throws Exception
     */
    public static String arangeBankTransfer(String AccountID, String RecipientCountry, String RecipientBic, String RecipientIban, String RecipientName, Date Date, double CurrencyAmount, String CurrencyUnit, String UsageText) throws Exception {
        OnlineBankingAccount myAccount = (OnlineBankingAccount)accountMap.get(AccountID);
        if (myAccount == null) 
            return "Account ID '" + AccountID + "' does not exist.";
        else
            return myAccount.arangeBankTransfer(RecipientCountry, RecipientBic, RecipientIban, RecipientName, Date, CurrencyAmount, CurrencyUnit, UsageText);
    }

    /**
     * close
     * 
     * @param args
     * @throws Exception
     */
    public static String close(String AccountID) throws Exception {
        OnlineBankingAccount myAccount = (OnlineBankingAccount)accountMap.get(AccountID);
        if (myAccount == null) 
            return "Account ID '" + AccountID + "' does not exist.";
        else {
            accountMap.remove(AccountID);
            myAccount.close();
        }

        return "Account ID '" + AccountID + "' has been closed.";
    }
}
