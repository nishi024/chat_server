CABEZAS/RAJAPAKSA
È un ChatServer client/server multithread basato su console che utilizza la programmazione Java Socket. Un server ascolta le richieste di connessione dai client attraverso la rete o anche dalla stessa macchina. I client sanno come connettersi al server tramite un indirizzo IP e un numero di porta. Dopo essersi connesso al server, il cliente può scegliere il proprio nome utente nella chat room. Il client invia un messaggio, il messaggio viene inviato al server utilizzando ObjectOutputStream in java. Dopo aver ricevuto il messaggio dal client, il server trasmette il messaggio.

Sul console prompt: Se il portNumber non è specificato, viene utilizzato 6789 Se il serverAddress non è specificato, viene utilizzato "localHost" Se il nome utente non è specificato, viene utilizzato "Anonymous"

Server

per avviare l'applicazione

java Server
java Server portNumber
Chat

client console:

Messaggio di inizializzazione
LISTA riceve la lista dei utenti connessi
MESSAGE messaggi normali
CHIUDI per disconnessione dal server