package it.fi.meucci;


import java.net.*;
import java.io.*;
import java.util.*;



public class Client  {
	
	
	private String notif = " *** ";

	//  I/O
	private ObjectInputStream sInput;		
	private ObjectOutputStream sOutput;		
	private Socket socket;					
	
	private String server, username;	
	private int port;					

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}


	Client(String server, int port, String username) {
		this.server = server;
		this.port = port;
		this.username = username;
	}
	

	public boolean start() {//inizialiazza la chat
		// connessione al server
		try {
			socket = new Socket(server, port);
		} 
		
		catch(Exception ec) {//errore durante la connessione
			display("Errore connessione al server:" + ec);
			return false;
		}
		
		String msg = "Connesione accettata " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);
	
		//datagram stream
		try
		{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			display("Crea un nuovo Input/output Stream: " + eIO);
			return false;
		}

		// crea il thread per restare ascolto dal server
		new ListenFromServer().start();
		// manda il nome utente al server
		try
		{
			sOutput.writeObject(username);
		}
		catch (IOException eIO) {
			display("Login : " + eIO);
			disconnect();
			return false;
		}
		return true;
	}


	private void display(String msg) {//manda un messaggio al console

		System.out.println(msg);
		
	}
	
	
	void sendMessage(ChatMessage msg) {//manda il messaggio al server
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}


	private void disconnect() {
		try { 
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {}
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {}
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {}
			
	}

	public static void main(String[] args) {
		// default values if not entered
		int portNumber = 6789;
		String serverAddress = "localhost";
		String userName = "Anonymous";
		Scanner scan = new Scanner(System.in);
		
		System.out.println("Inserire il nome: ");
		userName = scan.nextLine();

		// different case according to the length of the arguments.
		switch(args.length) {
			case 3:
				// for > javac Client username portNumber serverAddr
				serverAddress = args[2];
			case 2:
				// for > javac Client username portNumber
				try {
					portNumber = Integer.parseInt(args[1]);
				}
				catch(Exception e) {
					System.out.println("porta non valida");
					System.out.println(": > java Client [username] [portNumber] [serverAddress]");
					return;
				}
			case 1: 
				userName = args[0];
			case 0:
				break;
			default:
				System.out.println(": > java Client [username] [portNumber] [serverAddress]");
			return;
		}
		// create the Client object
		Client client = new Client(serverAddress, portNumber, userName);
		// try to connect to the server and return if not connected
		if(!client.start())
			return;
		
		System.out.println("\nSei nella chat.");
		System.out.println("Instructions:");
		System.out.println("1. Inserire il messaggio da inviare");
		System.out.println("3. Type '/lista' per visualizzare la lista dei client connessi ");
		System.out.println("4. Type '/chiudi' per la disconnessione dal server");
		
		
		while(true) {//loop infinito dall'utente
			System.out.print("> ");
			
			String msg = scan.nextLine();//legge  il messaggio del utente
			
			if(msg.equalsIgnoreCase("/chiudi")) {//chiude se il messaggio è CHIUDI
				client.sendMessage(new ChatMessage(ChatMessage.CHIUDI, ""));
				break;
			}
			
			else if(msg.equalsIgnoreCase("/lista")) {// per vedere chi è connesso al server
				client.sendMessage(new ChatMessage(ChatMessage.LISTA, ""));				
			}
			
			else {//messaggio normale
				client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
			}
		}
		
		scan.close();
		client.disconnect();	
	}

	// aspetta un messaggio dal server
	 
	class ListenFromServer extends Thread {

		public void run() {
			while(true) {
				try {
					// legge il messaggio di input 
					String msg = (String) sInput.readObject();
					// stampa il messaggio 
					System.out.println(msg);
					System.out.print("> ");
				}
				catch(IOException e) {
					display(notif + "Server ha chiuso la connessione: " + e + notif);
					break;
				}
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
}

