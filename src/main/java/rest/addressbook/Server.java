package rest.addressbook;

import java.io.IOException;
import java.net.URI;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

public class Server {
	private static final Logger LOGGER = Grizzly.logger(Server.class);
	
	public static void main(String[] args) {
		LOGGER.setLevel(Level.FINER);
		AddressBook ab = new AddressBook();
		
		// Some dummy data
		Person salvador = new Person();
		salvador.setName("Salvador");
		salvador.setId(ab.nextId());
		Person juan = new Person();
		juan.setName("Juan");
		juan.setId(ab.getNextId());
		ab.getPersonList().add(salvador);
		ab.getPersonList().add(juan);
		
		URI uri = UriBuilder.fromUri("http://localhost/").port(8080).build();
		HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri,
				new ApplicationConfig(ab));
		try (Scanner scan = new Scanner(System.in)){
			server.start();
			LOGGER.info("Press 's'+'enter' to shutdown now the server...");
			while(!scan.nextLine().equals("s"));
		} catch (IOException ioe) {
			LOGGER.log(Level.SEVERE, ioe.toString(), ioe);
		} finally {
			LOGGER.info("Shuting now");
			server.shutdownNow();
			LOGGER.info("Server stopped");
		}
	}
}
