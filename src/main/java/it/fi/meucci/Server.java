package it.fi.meucci;


import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

// the server that can be run as a console
public class Server {
	private static int uniqueId;//id unico
	private ArrayList<ClientThread> al;//Array di contenimento lista
	private SimpleDateFormat sdf;//il tempo di connessione di un client 
	private int port;
	private boolean keepGoing;//controlla se il server è ancora in escuzione
	private String notif = " *** ";
	
	
	public Server(int port) {//costruttore che riceve come parametro la porta da ascoltare per la connessione
		this.port = port;
		sdf = new SimpleDateFormat("HH:mm:ss");
		al = new ArrayList<ClientThread>();//Array per tenere la lista dei client
	}
	
	public void start() {
		keepGoing = true;
		//crea il server socket e aspetta le richeste di connessione 
		try 
		{
			ServerSocket serverSocket = new ServerSocket(port);

			
			while(keepGoing) //loop infinto del server (finche non viene chiuso)
			{
				display("Server waiting for Clients on port " + port + ".");
				
				
				Socket socket = serverSocket.accept();
				if(!keepGoing)//interrome se il server si stoppa
					break;
				// se un client è connesso, crea questo thread
				ClientThread t = new ClientThread(socket);
				al.add(t);//viene aggiunto alla lista
				
				t.start();
			}
			// rova a chiudere il server
			try {
				serverSocket.close();
				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
					// chiude datastream e socket
					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {
					}
				}
			}
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}
	
	// per fermare il server
	protected void stop() {
		keepGoing = false;
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
		}
	}
	
	// Display evento al  console
	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		System.out.println(time);
	}
	
	// un messaggio broadcast a tutti client
	private synchronized boolean broadcast(String message) {
		// aggiugge timestamp al messaggio
		String time = sdf.format(new Date());
		
		// controlla se il messaggio privato
		String[] w = message.split(" ",3);
		
		boolean isPrivate = false;
		if(w[1].charAt(0)=='@') 
			isPrivate=true;
		
		
		// se il messaggio è in privato, lo manda solo all'utente menzionato
		if(isPrivate==true)
		{
			String tocheck=w[1].substring(1, w[1].length());
			
			message=w[0]+w[2];
			String messageLf = time + " " + message + "\n";
			boolean found=false;
			for(int y=al.size(); --y>=0;)
			{
				ClientThread ct1=al.get(y);
				String check=ct1.getUsername();
				if(check.equals(tocheck))
				{
					//prova a scrivere al client, se fallisce viene rimosso dalla chat
					if(!ct1.writeMsg(messageLf)) {
						al.remove(y);
						display("Disconnected Client " + ct1.username + " removed from list.");
					}
					// utente trovato, manda il messaggio
					found=true;
					break;
				}
				
				
				
			}
			// menziona l'utente non trovato, return false
			if(found!=true)
			{
				return false; 
			}
		}
		//se il messaggio in broadcast
		else
		{
			String messageLf = time + " " + message + "\n";
			// stamp il messaggio
			System.out.print(messageLf);
			
			// loop per la remozione del client
			// perche si era disconnesso 
			for(int i = al.size(); --i >= 0;) {
				ClientThread ct = al.get(i);
				// rimuove il client dalla lista se il messaggio è irraggiungibile
				if(!ct.writeMsg(messageLf)) {
					al.remove(i);
					display("Client disconnesso" + ct.username + " rimosso dalla lista.");
				}
			}
		}
		return true;
		
		
	}

	// se client manda il messaggip CHIUDI viene disconnesso
	synchronized void remove(int id) {
		
		String disconnectedClient = "";
		//scannerizza l'array list finche non viene trovato l'ID
		for(int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			// remuove se lo viene trovato
			if(ct.id == id) {
				disconnectedClient = ct.getUsername();
				al.remove(i);
				break;
			}
		}
		broadcast(notif + disconnectedClient + " ha abbandonato la chatroom." + notif);
	}
	

	public static void main(String[] args) {
		int portNumber = 6789;
		switch(args.length) {
			case 1:
				try {
					portNumber = Integer.parseInt(args[0]);
				}
				catch(Exception e) {
					System.out.println("Numero di porta non valida.");
					System.out.println("Usage is: > java Server [portNumber]");
					return;
				}
			case 0:
				break;
			default:
				System.out.println("Usage is: > java Server [portNumber]");
				return;
				
		}
		// crea il sever object e lo fa partire
		Server server = new Server(portNumber);
		server.start();
	}

	// istanza di questo thread avvia tutti i client
	class ClientThread extends Thread {
		// socket per prendere messaggi dai client
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		// il mio di unico (facilita la disconnesione)
		int id;
		// nomeutente del client
		String username;
		// message object per ricevere message e io suo type
		ChatMessage cm;
		// timestamp
		String date;

		// Construttore
		ClientThread(Socket socket) {
			// Id di tipo unico
			id = ++uniqueId;
			this.socket = socket;
			//crea entrambe Data Stream
			System.out.println("Thread trying to create Object Input/Output Streams");
			try
			{
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// legge il username
				username = (String) sInput.readObject();
				broadcast(notif + username + " si è aggiunto alla chatroom." + notif);
			}
			catch (IOException e) {
				display("Exception  new Input/output Stream: " + e);
				return;
			}
			catch (ClassNotFoundException e) {
			}
            date = new Date().toString() + "\n";
		}
		
		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		// loop infinto per leggere e mandare il messaggio
		public void run() {
			// loop finche CHIUDE
			boolean keepGoing = true;
			while(keepGoing) {
				// legge la stringa
				try {
					cm = (ChatMessage) sInput.readObject();
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				// prende il messaggio da chatmessage dal object ricevuto
				String message = cm.getMessage();

				// azione basati su un tipo
				switch(cm.getType()) {

				case ChatMessage.MESSAGE:
					boolean confirmation =  broadcast(username + ": " + message);
					if(confirmation==false){
						String msg = notif + "Sorry. No such user exists." + notif;
						writeMsg(msg);
					}
					break;
				case ChatMessage.CHIUDI:
					display(username + " disconnessione con il messaggio /chiudi.");
					keepGoing = false;
					break;
				case ChatMessage.LISTA:
					writeMsg("Lista dei utenti connessi " + sdf.format(new Date()) + "\n");
					// manda la lista dei client attivi
					for(int i = 0; i < al.size(); ++i) {
						ClientThread ct = al.get(i);
						writeMsg((i+1) + ") " + ct.username + " da " + ct.date);
					}
					break;
				}
			}
			// fine loop disconnette e rimuove dalla lista
			remove(id);
			close();
		}
		
		// chiude tutto
		private void close() {
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		// write a String to the Client output stream
		private boolean writeMsg(String msg) {
			// if Client is still connected send the message to it
			if(!socket.isConnected()) {
				close();
				return false;
			}
			// scrive il messaggio al stream
			try {
				sOutput.writeObject(msg);
			}
			// se ce un errore il messaggio, viene avvertito l'utente
			catch(IOException e) {
				display(notif + "Errore nel mandare il messaggio " + username + notif);
				display(e.toString());
			}
			return true;
		}
	}
}

