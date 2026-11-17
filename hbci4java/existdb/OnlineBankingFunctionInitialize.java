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
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;
import org.exist.xquery.value.StringValue;

public class OnlineBankingFunctionInitialize extends BasicFunction {

	public final static FunctionSignature signature = new FunctionSignature(
		new QName("initialize", OnlineBankingModule.NAMESPACE_URI, OnlineBankingModule.PREFIX),
		"Initialize an HBCI/FinTS account session",
		new SequenceType[] {
				new FunctionParameterSequenceType("AccountID", Type.STRING, Cardinality.EXACTLY_ONE, "AccountID"),
				new FunctionParameterSequenceType("Country", Type.STRING, Cardinality.EXACTLY_ONE, "Country DE"),
				new FunctionParameterSequenceType("Port", Type.INTEGER, Cardinality.ZERO_OR_ONE, "Port 443"),
				new FunctionParameterSequenceType("BLZ", Type.LONG, Cardinality.EXACTLY_ONE, "BLZ"),
				new FunctionParameterSequenceType("BIC", Type.STRING, Cardinality.EXACTLY_ONE, "BIC"),
				new FunctionParameterSequenceType("IBAN", Type.STRING, Cardinality.EXACTLY_ONE, "IBAN"),
				new FunctionParameterSequenceType("BankAccount", Type.LONG, Cardinality.EXACTLY_ONE,"Account number"),
				new FunctionParameterSequenceType("BankUserID", Type.STRING, Cardinality.EXACTLY_ONE, "User ID"),
				new FunctionParameterSequenceType("CustomerID", Type.STRING, Cardinality.EXACTLY_ONE, "Customer ID"),
				new FunctionParameterSequenceType("Secret", Type.STRING, Cardinality.EXACTLY_ONE, "Passport passphrase"),
				new FunctionParameterSequenceType("AuthType", Type.INTEGER, Cardinality.EXACTLY_ONE, "SecMech 9xx"),
				new FunctionParameterSequenceType("Pin", Type.STRING, Cardinality.EXACTLY_ONE, "Online PIN"),
				new FunctionParameterSequenceType("MessageURL", Type.STRING, Cardinality.ZERO_OR_ONE, "Message URL (PUT)"),
				new FunctionParameterSequenceType("ResponseURL", Type.STRING, Cardinality.ZERO_OR_ONE, "Response URL (GET)"),
				new FunctionParameterSequenceType("Authentication", Type.STRING, Cardinality.ZERO_OR_ONE, "Authentication Header"),
				new FunctionParameterSequenceType("PassportPath", Type.STRING, Cardinality.ZERO_OR_ONE, "Persistent passport file path"),
				new FunctionParameterSequenceType("Mode", Type.STRING, Cardinality.ZERO_OR_ONE, "interactive or auto")
					},
		new FunctionReturnSequenceType(Type.STRING, Cardinality.ZERO_OR_MORE, "initialize result"));

	public OnlineBankingFunctionInitialize(XQueryContext context) {
		super(context, signature);
	}

	public Sequence eval(Sequence[] args, Sequence contextSequence) throws XPathException {
		ValueSequence result = new ValueSequence();
		if (args == null)
			return Sequence.EMPTY_SEQUENCE;

		String pAccountID = args[0].getStringValue();
		String pCountry = args[1].getStringValue();
		Integer pPort = args[2].isEmpty() ? 443 : Integer.parseInt(args[2].getStringValue());
		String pBlz = args[3].getStringValue();
		String pBic = args[4].getStringValue();
		String pIban = args[5].getStringValue();
		String pBankAccount = args[6].getStringValue();
		String pBankUserID = args[7].getStringValue();
		String pCustomerID = args[8].getStringValue();
		String pSecret = args[9].getStringValue();
		Integer pAuthType = Integer.parseInt(args[10].getStringValue());
		String pPin = args[11].getStringValue();
		String pMessageURL = args[12].isEmpty() ? null : args[12].getStringValue();
		String pResponseURL = args[13].isEmpty() ? null : args[13].getStringValue();
		String pAuthentication = args[14].isEmpty() ? null : args[14].getStringValue();
		String pPassportPath = args.length > 15 && !args[15].isEmpty() ? args[15].getStringValue() : null;
		String pMode = args.length > 16 && !args[16].isEmpty() ? args[16].getStringValue() : "interactive";
		Boolean pDebug = false;

		try {
			String bankStatement = OnlineBanking.initialize(pAccountID, pCountry, pPort, pBlz, pBic, pIban,
					pBankAccount, pBankUserID, pCustomerID, pSecret, pAuthType, pPin, pMessageURL, pResponseURL,
					pAuthentication, pPassportPath, pMode, pDebug);
			result.add(new StringValue(bankStatement));
		} catch (Exception e) {
			result.add(new StringValue("OnlineBanking.initialize failed: " + e.toString()));
		}
		return result;
	}
}
