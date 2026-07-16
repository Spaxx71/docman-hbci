import hbci4java.existdb.OnlineBanking;
import hbci4java.existdb.OnlineBankingAccount;

import java.io.Console;
import java.util.*;
import java.util.Arrays;
import java.util.List;
import java.util.Calendar;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

import java.util.Date;

public class Test {

	public static void main(String[] args) throws Exception {
		Console console = System.console();
		Integer pPort = 443;

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		Date pEndDate = cal.getTime();
        cal.add((Calendar.DAY_OF_MONTH), -90);
        Date pStartDate = cal.getTime();

		String strUsername = "bix-accounting";
		String strPassword = "bix-acc";
		String auth = strUsername + ":" + strPassword;
		byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
		String strAuthHeaderValue = "Basic " + new String(encodedAuth);


		OnlineBanking myBanking = new OnlineBanking();

		//String strSend = "https://docman.bintellix.com/api/bintellix/hbci/176982701/send.xml":
		//String strReceive = "https://docman.bintellix.com/api/bintellix/hbci/176982701/receive.xml";
		String strSend = null;
		String strReceive = null;

		// (String AccountID, String Country, Integer Port, String Blz, 
        //String BankAccount, String BankUserID, String CustomerID,
        //String Secret, String Pin, 
        //String MessageUrl, String ResponseUrl, String AuthHeaderValue, Boolean Debug)


		/* 
		System.out.println("### Initialize Account 4a");
		String strPin4a = console.readLine("Bitte PIN 4a eingeben:");
		String strInitialize4 = myBanking.initialize("Account4a","DE",pPort,"70070024","DEUTDEDBMUC","DE147007002401234123400","1234123400","2201234123400","2201234123400","Geheim",901,strPin4a,strSend,strReceive,strAuthHeaderValue,true);

		System.out.println("\n### Get Transactions: Account 4a");
		String strGetTransactions4a = myBanking.getTransactions("Account4a",pStartDate,pEndDate);
		System.out.println(strGetTransactions4a);
		myBanking.close("Account4a");
		*/

	
		System.out.println("### Initialize Account 6");
		// String strPin6 = console.readLine("Bitte PIN 6 eingeben:");
		String strInitialize6 = myBanking.initialize("Account6","DE",pPort,"12030000","BYLADEM1001","DE63120300000001999333","1032665638","1032665638","1032665638","Geheim",901,"",strSend,strReceive,strAuthHeaderValue,true);

		System.out.println("\n### Get Transactions: Account 6");
		String strGetTransactions6 = myBanking.getTransactions("Account6",pStartDate,pEndDate);
		System.out.println(strGetTransactions6);
		myBanking.close("Account6");
		

		/* BANK TRANSFER

		System.out.println("### Initialize Account 1");
		String strPin1 = console.readLine("Bitte PIN 1 eingeben:");
		String strInitialize1 = myBanking.initialize("Account1","DE",pPort,"DEUTDEDBMUC","DE587007002401234971400","70070024","1234971400","2201234971400","2201234971400","Geheim",901,strPin1,strSend,strReceive,strAuthHeaderValue,true);

		System.out.println("\n### Bank Transfer Account 1");

		String strCreateTransfer = myBanking.arangeBankTransfer("Account1","DE","DEUTDBMUC","DE92700700240176982700","Eduard Huber",new Date(),100.00,"EUR","KtoAusgl.");
		System.out.println(strCreateTransfer);
		myBanking.close("Account1");
		*/


	}
}