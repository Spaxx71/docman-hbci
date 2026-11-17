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
import org.exist.xquery.value.StringValue;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;

public class OnlineBankingFunctionResetPassport extends BasicFunction {

	public final static FunctionSignature signature = new FunctionSignature(
			new QName("resetPassport", OnlineBankingModule.NAMESPACE_URI, OnlineBankingModule.PREFIX),
			"Deletes the persisted passport file for an account (explicit reset only).",
			new SequenceType[] {
					new FunctionParameterSequenceType("AccountID", Type.STRING, Cardinality.EXACTLY_ONE, "AccountID"),
					new FunctionParameterSequenceType("PassportPath", Type.STRING, Cardinality.ZERO_OR_ONE,
							"Optional passport path")
			},
			new FunctionReturnSequenceType(Type.STRING, Cardinality.ZERO_OR_MORE, "reset result"));

	public OnlineBankingFunctionResetPassport(XQueryContext context) {
		super(context, signature);
	}

	public Sequence eval(Sequence[] args, Sequence contextSequence) throws XPathException {
		ValueSequence result = new ValueSequence();
		if (args == null) {
			return Sequence.EMPTY_SEQUENCE;
		}
		String accountId = args[0].getStringValue();
		String passportPath = args.length > 1 && !args[1].isEmpty() ? args[1].getStringValue() : null;
		try {
			result.add(new StringValue(OnlineBanking.resetPassport(accountId, passportPath)));
		} catch (Exception e) {
			result.add(new StringValue("OnlineBanking.resetPassport failed: " + e.toString()));
		}
		return result;
	}
}
