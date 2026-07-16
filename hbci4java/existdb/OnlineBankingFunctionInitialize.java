package hbci4java.existdb;

import org.exist.dom.QName;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;

import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.FunctionReturnSequenceType;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.SequenceIterator;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;
import org.exist.xquery.value.StringValue;

import org.xmldb.api.base.CompiledExpression;

/*
exist-db extension function
https://exist-db.org/exist/apps/doc/extensions.xml
*/
public class OnlineBankingFunctionInitialize extends BasicFunction {

	public final static FunctionSignature signature = new FunctionSignature(
		new QName("initialize", OnlineBankingModule.NAMESPACE_URI, OnlineBankingModule.PREFIX),
		"This funtion sets the account",
		new SequenceType[] {
				new FunctionParameterSequenceType("AccountID", Type.STRING, Cardinality.EXACTLY_ONE, "AccountID ACC123"),
				new FunctionParameterSequenceType("Country", Type.STRING, Cardinality.EXACTLY_ONE, "Country DE"),
				new FunctionParameterSequenceType("Port", Type.INTEGER, Cardinality.ZERO_OR_ONE, "Port 443"),
				new FunctionParameterSequenceType("BLZ", Type.LONG, Cardinality.EXACTLY_ONE, "BLZ 10020030"),
				new FunctionParameterSequenceType("BIC", Type.STRING, Cardinality.EXACTLY_ONE, "BIC DEDBDEUBA"),
				new FunctionParameterSequenceType("IBAN", Type.STRING, Cardinality.EXACTLY_ONE, "IBAN DE991234567123456789"),
				new FunctionParameterSequenceType("BankAccount", Type.LONG, Cardinality.EXACTLY_ONE,"Account 12345600"),
				new FunctionParameterSequenceType("BankUserID", Type.STRING, Cardinality.EXACTLY_ONE, "User 123456"),
				new FunctionParameterSequenceType("CustomerID", Type.STRING, Cardinality.EXACTLY_ONE, "User 123456"),
				new FunctionParameterSequenceType("Secret", Type.STRING, Cardinality.EXACTLY_ONE, "Secret Geheim"),
				new FunctionParameterSequenceType("AuthType", Type.INTEGER, Cardinality.EXACTLY_ONE, "AuthType 901"),
				new FunctionParameterSequenceType("Pin", Type.STRING, Cardinality.EXACTLY_ONE, "PIN 12345"),
				new FunctionParameterSequenceType("MessageURL", Type.STRING, Cardinality.ZERO_OR_ONE,
						"Message URL (PUT)"),
				new FunctionParameterSequenceType("ResponseURL", Type.STRING, Cardinality.ZERO_OR_ONE,
						"Response URL (GET)"),
				new FunctionParameterSequenceType("Authentication", Type.STRING, Cardinality.ZERO_OR_ONE,
						"Authentication Header")
					},
		new FunctionReturnSequenceType(Type.STRING, Cardinality.ZERO_OR_MORE, "the account"));

	public OnlineBankingFunctionInitialize(XQueryContext context) {
		super(context, signature);
	}

	public Sequence eval(Sequence[] args, Sequence contextSequence) throws XPathException {
		ValueSequence result = new ValueSequence(); 
		
		// is argument the empty sequence? 
		if (args == null) 
			return	Sequence.EMPTY_SEQUENCE; 

		if (false) {
			// iterate through the argument sequence and echo each item 
			for (SequenceIterator i = args[0].iterate(); i.hasNext();) { 
				String str = i.nextItem().getStringValue(); 
				result.add(new StringValue("Param: " + str));
			}
		}

		/*
			MemTreeBuilder builder = context.getDocumentBuilder();
			builder.startDocument();
			builder.startElement(new QName("hello", null, null), null);
			builder.characters(args[0].getStringValue());
			builder.endElement();
			builder.endDocument();
			return (Sequence) builder.getDocument().getDocumentElement();
		*/

		String pAccountID = args[0].getStringValue();
		String pCountry = args[1].getStringValue();
		Integer pPort = Integer.parseInt(args[2].getStringValue());
		String pBlz = args[3].getStringValue();	
		String pBic = args[4].getStringValue();	
		String pIban = args[5].getStringValue();	
		String pBankAccount = args[6].getStringValue();
		String pBankUserID = args[7].getStringValue();
		String pCustomerID = args[8].getStringValue();
		String pSecret = args[9].getStringValue();
		Integer pAuthType = Integer.parseInt(args[10].getStringValue());
		String pPin = args[11].getStringValue();
		String pMessageURL = args[12].getStringValue();
		String pResponseURL = args[13].getStringValue();
		String pAuthentication = args[14].getStringValue();
		Boolean pDebug = false;

		String bankStatement = null;
		try {
			/* "Account1","DE",pPort,pBlz,pBankAccount,pBankUser,"Geheim",pPin,pMessageUrl,pResponseUrl,pAuthHeaderValue */
			bankStatement = OnlineBanking.initialize(pAccountID, pCountry, pPort, pBlz, pBic, pIban, pBankAccount, pBankUserID, pCustomerID, pSecret, pAuthType, pPin, pMessageURL, pResponseURL, pAuthentication, pDebug);
			result.add(new StringValue(bankStatement));
		}
		catch (Exception e) {
			result.add(new StringValue("OnlineBanking.initialize(\""+pAccountID+"\", \""+pCountry+"\", \""+pPort+"\", \""+pBlz+"\", \""+pBic+"\", \""+pIban+"\", \""+pBankAccount+"\", \""+pBankUserID+"\", \""+pCustomerID+"\", \""+pSecret+"\", \""+pAuthType+"\", \""+pPin+"\", \""+pMessageURL+"\", \""+pResponseURL+"\", \""+pAuthentication+"\")"));
			result.add(new StringValue("Es ist ein unerwarteter Fehler beim Aufruf der Funktion aufgetreten: " + e.toString()));
		}
		return result;
	}
}