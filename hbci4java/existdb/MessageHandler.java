package hbci4java.existdb;

abstract class MessageHandler {

    abstract String readMessage();

    abstract void sendMessage(String message);

    void sendChallenge(String challengeXml) {
        sendMessage(challengeXml);
    }
}
