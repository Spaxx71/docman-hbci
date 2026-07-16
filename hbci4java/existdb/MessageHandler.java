package hbci4java.existdb;

import java.io.IOException;
import java.net.URL;

abstract class MessageHandler {

	abstract String readMessage();

    abstract void sendMessage(String message);
}