package hbci4java.existdb;

/**
 * Thrown in auto mode when the bank requires interactive SCA (TAN).
 */
public class ScaRequiredException extends RuntimeException {
    private final String challengeType;
    private final String challengeXml;

    public ScaRequiredException(String challengeType, String challengeXml) {
        super("SCA_REQUIRED:" + challengeType);
        this.challengeType = challengeType;
        this.challengeXml = challengeXml;
    }

    public String getChallengeType() {
        return challengeType;
    }

    public String getChallengeXml() {
        return challengeXml;
    }
}
