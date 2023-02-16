package com.distribuida.rest;

import com.distribuida.clientes.authors.AuthorRestProxy;
import com.distribuida.clientes.authors.AuthorsCliente;
import com.distribuida.db.Book;
import com.distribuida.dtos.BookDto;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.ok;


@Path("/books")

public class BookRest {


    @PersistenceContext(unitName = "test")
    private EntityManager entityManager;

    @RestClient
    @Inject
    AuthorRestProxy proxyAuthor;
    @GET
    @Operation(summary = "Find all books",
            description = "This is used when you want to list all books. (\"app1\"books\")")
    @RequestBody(
            name = "books",
            description = "Conveys the new greeting prefix to use in building books",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Book.class),
                    examples = @ExampleObject(
                            name = "\"author\":\"author1\",\"id\":1,\"isbn\":\"11-11\",\"price\":20.0,\"title\":\"title1\"",
                            summary = "Example book ",
                            value = "book message")))
    @Produces(MediaType.APPLICATION_JSON)
    public List<Book> findAll() {
        return entityManager.createNamedQuery("getBooks", Book.class).getResultList();
    }

    @GET
    @Path("/{id}")

    @Operation(summary = "Find  book by id",
            description = "This can only be used when the book for the search exists.")
    @APIResponse(description = "Simple JSON containing the greeting",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Book.class)))
    @Produces(MediaType.APPLICATION_JSON)
    public Book findById(@PathParam("id") String id) {
        try {
            return entityManager.find(Book.class, Integer.valueOf(id));
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Unable to find book with ID " + id);
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete  book by id",
            description = "This can only be used when the book for the search exists.")
    @APIResponse(description = "Simple JSON containing the greeting",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Book.class)))
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(Transactional.TxType.REQUIRED)
    public void delete(@PathParam("id") String id) {
        Book book = findById(id);
        entityManager.remove(book);
    }

    @POST
    @Operation(summary = "Create  book ",
            description = "This is used to add a new book.")
    @APIResponse(description = "Simple JSON containing the greeting",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Book.class)))
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional(Transactional.TxType.REQUIRED)
    public void create(Book book) {
        try {
            entityManager.persist(book);
        } catch (Exception e) {
            throw new BadRequestException("Unable to create book with ID " + book.getId());
        }
    }
    @PUT
    @Path("/{id}")
    @Operation(summary = "Update  book by id",
            description = "This is used to update a  book.")
    @APIResponse(description = "Simple JSON containing the greeting",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Book.class)))
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional(Transactional.TxType.REQUIRED)
    public void update(@PathParam("id") String id, Book book) {
        try {
             Book bookS = findById(id);
             bookS.setAuthor(book.getAuthor());
             bookS.setTitle(book.getTitle());
             bookS.setIsbn(book.getIsbn());
             bookS.setPrice(book.getPrice());
             entityManager.persist(bookS);
        } catch (Exception e) {
            throw new BadRequestException("Unable to update book with ID " + book.getId());
        }
    }

    @GET
    @Path("/all")
    @Operation(summary = "Find all  books by author",
            description = "This is used to list all  books by author.")
    public List<BookDto> findAllCompleto() throws Exception {
        var books = entityManager.createNamedQuery("getBooks", Book.class).getResultList();


        List<BookDto> ret = books.stream()
                .map(s -> {
                    System.out.println("*********buscando " + s.getId() );

                    AuthorsCliente author = proxyAuthor.findById(s.getId().longValue());
                    return new BookDto(
                            s.getId(),
                            s.getIsbn(),
                            s.getTitle(),
                            s.getAuthor(),
                            s.getPrice(),
                            String.format("%s, %s", author.getLastName(), author.getFirtName())
                    );
                })
                .collect(Collectors.toList());

        return ret;
    }



}
