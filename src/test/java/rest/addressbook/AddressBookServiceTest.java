package rest.addressbook;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.junit.After;
import org.junit.Test;

import rest.addressbook.AddressBook;
import rest.addressbook.ApplicationConfig;
import rest.addressbook.Person;
import java.util.List;
/**
 * A simple test suite
 *
 */
public class AddressBookServiceTest {

	HttpServer server;

	@Test
	public void serviceIsAlive() throws IOException {
		// Prepare server
		AddressBook ab = new AddressBook();
		launchServer(ab);

		// Request the address book
		Client client = ClientBuilder.newClient();
		Response response = client.target("http://localhost:8282/contacts")
				.request().get();
		assertEquals(200, response.getStatus());
		//assertEquals(0, response.readEntity(AddressBook.class).getPersonList()
		//		.size());
		List<Person> listaPersonas = response.readEntity(AddressBook.class).getPersonList();
		assertEquals(0, listaPersonas.size());
		//////////////////////////////////////////////////////////////////////
		// Verify that GET /contacts is well implemented by the service, i.e
		// test that it is safe and idempotent
		//////////////////////////////////////////////////////////////////////
		// New respoonse
		Response responseCheck = client.target("http://localhost:8282/contacts")
				.request().get();
		assertEquals(200, responseCheck.getStatus());
		List<Person> listaPersonas2 = responseCheck.readEntity(AddressBook.class).getPersonList();
		// Same list of distint responses
		assertEquals(listaPersonas, listaPersonas2);
	}

	@Test
	public void createUser() throws IOException {
		// Prepare server
		AddressBook ab = new AddressBook();
		launchServer(ab);

		//Get initial list
		Client client = ClientBuilder.newClient();
		Response response = client.target("http://localhost:8282/contacts")
				.request().get();
		List<Person> initialList = response.readEntity(AddressBook.class).getPersonList();
				
		// Prepare data
		Person juan = new Person();
		juan.setName("Juan");
		URI juanURI = URI.create("http://localhost:8282/contacts/person/1");

		// Create a new user
		response = client.target("http://localhost:8282/contacts")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(juan, MediaType.APPLICATION_JSON));

		assertEquals(201, response.getStatus());
		assertEquals(juanURI, response.getLocation());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		Person juanUpdated = response.readEntity(Person.class);
		assertEquals(juan.getName(), juanUpdated.getName());
		assertEquals(1, juanUpdated.getId());
		assertEquals(juanURI, juanUpdated.getHref());

		// Check the list updated
		response = client.target("http://localhost:8282/contacts")
				.request().get();
		List<Person> updateList = response.readEntity(AddressBook.class).getPersonList();
		assertNotEquals(initialList, updateList);

		// Check that the new user exists
		response = client.target("http://localhost:8282/contacts/person/1")
				.request(MediaType.APPLICATION_JSON).get();
		assertEquals(200, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		juanUpdated = response.readEntity(Person.class);
		assertEquals(juan.getName(), juanUpdated.getName());
		assertEquals(1, juanUpdated.getId());
		assertEquals(juanURI, juanUpdated.getHref());

		//////////////////////////////////////////////////////////////////////
		// Verify that POST /contacts is well implemented by the service, i.e
		// test that it is not safe and not idempotent
		//////////////////////////////////////////////////////////////////////

		Person ruben = new Person();
		ruben.setName("Ruben");
		URI rubenURI = URI.create("http://localhost:8282/contacts/person/2");

		// Create a new user
		response = client.target("http://localhost:8282/contacts")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(ruben, MediaType.APPLICATION_JSON));

		assertEquals(201, response.getStatus());
		assertEquals(rubenURI, response.getLocation());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		Person rubenUpdated = response.readEntity(Person.class);
		assertEquals(ruben.getName(), rubenUpdated.getName());
		assertEquals(2, rubenUpdated.getId());
		assertEquals(rubenURI, rubenUpdated.getHref());

		response = client.target("http://localhost:8282/contacts")
				.request().get();
		List<Person> updateList2 = response.readEntity(AddressBook.class).getPersonList();
		
		// Check the three list not equals
		assertNotEquals(updateList, updateList2);
		assertNotEquals(initialList, updateList2);


	}

	@Test
	public void createUsers() throws IOException {
		// Prepare server
		AddressBook ab = new AddressBook();
		Person salvador = new Person();
		salvador.setName("Salvador");
		salvador.setId(ab.nextId());
		ab.getPersonList().add(salvador);
		launchServer(ab);

		// Get initial list
		Client client = ClientBuilder.newClient();
		Response response = client.target("http://localhost:8282/contacts")
				.request().get();
		List<Person> initialList = response.readEntity(AddressBook.class).getPersonList();
		// Prepare data
		Person juan = new Person();
		juan.setName("Juan");
		URI juanURI = URI.create("http://localhost:8282/contacts/person/2");
		Person maria = new Person();
		maria.setName("Maria");
		URI mariaURI = URI.create("http://localhost:8282/contacts/person/3");

		// Create a user
		response = client.target("http://localhost:8282/contacts")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(juan, MediaType.APPLICATION_JSON));
		assertEquals(201, response.getStatus());
		assertEquals(juanURI, response.getLocation());

		// Create a second user
		response = client.target("http://localhost:8282/contacts")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(maria, MediaType.APPLICATION_JSON));
		assertEquals(201, response.getStatus());
		assertEquals(mariaURI, response.getLocation());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		Person mariaUpdated = response.readEntity(Person.class);
		assertEquals(maria.getName(), mariaUpdated.getName());
		assertEquals(3, mariaUpdated.getId());
		assertEquals(mariaURI, mariaUpdated.getHref());

		// Check that the new user exists
		response = client.target("http://localhost:8282/contacts/person/3")
				.request(MediaType.APPLICATION_JSON).get();
		assertEquals(200, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		mariaUpdated = response.readEntity(Person.class);
		assertEquals(maria.getName(), mariaUpdated.getName());
		assertEquals(3, mariaUpdated.getId());
		assertEquals(mariaURI, mariaUpdated.getHref());

		// Check that other response of maria is equal at previus maria response
		response = client.target("http://localhost:8282/contacts/person/3")
				.request(MediaType.APPLICATION_JSON).get();
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		Person mariaUpdated2 = response.readEntity(Person.class);
		assertEquals(mariaUpdated2.getName(), mariaUpdated.getName());
		assertEquals(mariaUpdated2.getId(), mariaUpdated.getId());
		assertEquals(mariaUpdated2.getHref(), mariaUpdated.getHref());

		//////////////////////////////////////////////////////////////////////
		// Verify that GET /contacts/person/3 is well implemented by the service, i.e
		// test that it is safe and idempotent
		//////////////////////////////////////////////////////////////////////
		response = client.target("http://localhost:8282/contacts")
				.request().get();
		List<Person> finalList = response.readEntity(AddressBook.class).getPersonList();
		// Check te lists not same
		assertNotEquals(initialList, finalList);

		response = client.target("http://localhost:8282/contacts")
				.request().get();
		List<Person> testList = response.readEntity(AddressBook.class).getPersonList();
		// Check the same list, lists not modified
		assertEquals(testList.size(), finalList.size());
	}

	@Test
	public void listUsers() throws IOException {

		// Prepare server
		AddressBook ab = new AddressBook();
		Person salvador = new Person();
		salvador.setName("Salvador");
		Person juan = new Person();
		juan.setName("Juan");
		ab.getPersonList().add(salvador);
		ab.getPersonList().add(juan);
		launchServer(ab);

		// Test list of contacts
		Client client = ClientBuilder.newClient();
		Response response = client.target("http://localhost:8282/contacts")
				.request(MediaType.APPLICATION_JSON).get();
		assertEquals(200, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		AddressBook addressBookRetrieved = response
				.readEntity(AddressBook.class);
		assertEquals(2, addressBookRetrieved.getPersonList().size());
		assertEquals(juan.getName(), addressBookRetrieved.getPersonList()
				.get(1).getName());

		//////////////////////////////////////////////////////////////////////
		// Verify that POST is well implemented by the service, i.e
		// test that it is not safe and not idempotent
		//////////////////////////////////////////////////////////////////////
		
		// Check list is updated
		Person ruben = new Person();
		ruben.setName("Ruben");
		List<Person> initialList = addressBookRetrieved.getPersonList();
		response = client.target("http://localhost:8282/contacts")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(ruben, MediaType.APPLICATION_JSON));
		response = client.target("http://localhost:8282/contacts").request().get();
	 	List<Person> finalList = response.readEntity(AddressBook.class).getPersonList(); 
		// compare initialList(without Ruben) and finalList(with Ruben)
		assertNotEquals(initialList, finalList);
	}

	@Test
	public void updateUsers() throws IOException {
		// Prepare server
		AddressBook ab = new AddressBook();
		Person salvador = new Person();
		salvador.setName("Salvador");
		salvador.setId(ab.nextId());
		Person juan = new Person();
		juan.setName("Juan");
		juan.setId(ab.getNextId());
		URI juanURI = URI.create("http://localhost:8282/contacts/person/2");
		ab.getPersonList().add(salvador);
		ab.getPersonList().add(juan);
		launchServer(ab);

		// Update Maria
		Person maria = new Person();
		maria.setName("Maria");
		Client client = ClientBuilder.newClient();
		Response response = client.target("http://localhost:8282/contacts")
				.request(MediaType.APPLICATION_JSON).get();
		List<Person> initialList = response.readEntity(AddressBook.class).getPersonList();	

		response = client
				.target("http://localhost:8282/contacts/person/2")
				.request(MediaType.APPLICATION_JSON)
				.put(Entity.entity(maria, MediaType.APPLICATION_JSON));
		assertEquals(200, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		Person juanUpdated = response.readEntity(Person.class);
		assertEquals(maria.getName(), juanUpdated.getName());
		assertEquals(2, juanUpdated.getId());
		assertEquals(juanURI, juanUpdated.getHref());

		// Verify that the update is real
		response = client.target("http://localhost:8282/contacts/person/2")
				.request(MediaType.APPLICATION_JSON).get();
		assertEquals(200, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		Person mariaRetrieved = response.readEntity(Person.class);
		assertEquals(maria.getName(), mariaRetrieved.getName());
		assertEquals(2, mariaRetrieved.getId());
		assertEquals(juanURI, mariaRetrieved.getHref());

		// Verify that only can be updated existing values
		response = client.target("http://localhost:8282/contacts/person/3")
				.request(MediaType.APPLICATION_JSON)
				.put(Entity.entity(maria, MediaType.APPLICATION_JSON));
		assertEquals(400, response.getStatus());

		//////////////////////////////////////////////////////////////////////
		// Verify that PUT /contacts/person/2 is well implemented by the service, i.e
		// test that it is idempotent
		//////////////////////////////////////////////////////////////////////
		response = client.target("http://localhost:8282/contacts")
				.request(MediaType.APPLICATION_JSON).get();
		List<Person> finalList = response.readEntity(AddressBook.class).getPersonList();

		// Check same size of list
		assertEquals(initialList.size(), finalList.size());
	}

	@Test
	public void deleteUsers() throws IOException {
		// Prepare server
		AddressBook ab = new AddressBook();
		Person salvador = new Person();
		salvador.setName("Salvador");
		salvador.setId(1);
		Person juan = new Person();
		juan.setName("Juan");
		juan.setId(2);
		ab.getPersonList().add(salvador);
		ab.getPersonList().add(juan);
		launchServer(ab);


		// Delete a user
		Client client = ClientBuilder.newClient();
		Response response = client
				.target("http://localhost:8282/contacts/person/2").request()
				.delete();
		assertEquals(204, response.getStatus());

		// Verify that the user has been deleted
		response = client.target("http://localhost:8282/contacts/person/2")
				.request().delete();
		assertEquals(404, response.getStatus());

		//////////////////////////////////////////////////////////////////////
		// Verify that DELETE /contacts/person/2 is well implemented by the service, i.e
		// test that it is idempotent
		//////////////////////////////////////////////////////////////////////
		
		// try to delete Juan, other time
		Response response2 = client
				.target("http://localhost:8282/contacts/person/2").request()
				.delete();
		assertEquals(404, response2.getStatus());
	}

	@Test
	public void findUsers() throws IOException {
		// Prepare server
		AddressBook ab = new AddressBook();
		Person salvador = new Person();
		salvador.setName("Salvador");
		salvador.setId(1);
		Person juan = new Person();
		juan.setName("Juan");
		juan.setId(2);
		ab.getPersonList().add(salvador);
		ab.getPersonList().add(juan);
		launchServer(ab);

		// Test user 1 exists
		Client client = ClientBuilder.newClient();
		Response response = client
				.target("http://localhost:8282/contacts/person/1")
				.request(MediaType.APPLICATION_JSON).get();
		assertEquals(200, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		Person person = response.readEntity(Person.class);
		assertEquals(person.getName(), salvador.getName());
		assertEquals(person.getId(), salvador.getId());
		assertEquals(person.getHref(), salvador.getHref());

		// Test user 2 exists
		response = client.target("http://localhost:8282/contacts/person/2")
				.request(MediaType.APPLICATION_JSON).get();
		assertEquals(200, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		person = response.readEntity(Person.class);
		assertEquals(person.getName(), juan.getName());
		assertEquals(2, juan.getId());
		assertEquals(person.getHref(), juan.getHref());

		// Test user 3 exists
		response = client.target("http://localhost:8282/contacts/person/3")
				.request(MediaType.APPLICATION_JSON).get();
		assertEquals(404, response.getStatus());
	}

	private void launchServer(AddressBook ab) throws IOException {
		URI uri = UriBuilder.fromUri("http://localhost/").port(8282).build();
		server = GrizzlyHttpServerFactory.createHttpServer(uri,
				new ApplicationConfig(ab));
		server.start();
	}

	@After
	public void shutdown() {
		if (server != null) {
			server.shutdownNow();
		}
		server = null;
	}

}
