package it.fi.meucci;


import java.io.*;


public class ChatMessage implements Serializable {

	// The different types of message sent by the Client
	// /LISTA riceve la lista dei utenti connessi 
	// MESSAGE messaggi normali
	// /CHIUDI per disconnessione dal server 
	static final int LISTA = 0, MESSAGE = 1, CHIUDI = 2;
	private int type;
	private String message;
	
	
	ChatMessage(int type, String message) {
		this.type = type;
		this.message = message;
	}
	
	int getType() {
		return type;
	}

	String getMessage() {
		return message;
	}
}
