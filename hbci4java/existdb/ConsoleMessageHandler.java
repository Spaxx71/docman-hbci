package hbci4java.existdb;

import java.io.IOException;
import java.io.Console;

public class ConsoleMessageHandler extends MessageHandler {

	public  ConsoleMessageHandler() {
	}

	public String readMessage() {
		Console console = System.console();
		return console.readLine("Eingabe:");
	}

    public void sendMessage(String message) {
		System.out.println(message);
    }
}