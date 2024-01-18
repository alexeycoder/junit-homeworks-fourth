package edu.alexey.junit.homeworks.fourth.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCollection;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookServiceTest {

	private BookRepository bookRepoMock;
	private BookService bookService;

	@BeforeEach
	void setUp() {
		this.bookRepoMock = mock(BookRepository.class);
		this.bookService = new BookService(bookRepoMock);
	}

	@Test
	void findAllBooksReturnsAllStoredBooks() {

		List<Book> books = IntStream.rangeClosed(1, 10)
				.mapToObj(i -> new Book("" + i, "Title" + i, "Author" + i))
				.toList();

		// setup mock
		doReturn(books).when(bookRepoMock).findAll();

		// do test
		var result = bookService.findAllBooks();
		assertThatCollection(result)
				.containsExactlyInAnyOrderElementsOf(books);
	}

	@Test
	void findAllBooksReturnsEmptyListIfNoBooks() {

		// setup mock
		doReturn(List.<Book>of()).when(bookRepoMock).findAll();

		// do test
		assertThatCollection(bookService.findAllBooks()).isEmpty();
	}

	/**
	 * Ожидаем, что сервис возвращает пустой список, если репозиторий вернул null по
	 * каким-либо причина.
	 */
	@Test
	void findAllBooksReturnsEmptyListIfRepoGivesNull() {

		// setup mock
		doReturn(null).when(bookRepoMock).findAll();

		// do test
		var result = bookService.findAllBooks();
		assertThatCollection(result).isNotNull().isEmpty();
	}

	@Test
	void findBookByIdReturnsNullIfNotFound() {

		// setup mock
		doReturn(null).when(bookRepoMock).findById(anyString());

		// do test
		BookService bookService = new BookService(bookRepoMock);
		assertNull(bookService.findBookById("123"));
	}

	@Test
	void findBookByIdReturnsCorrectBook() {

		Book bookA = new Book("A", "TitleA", "AuthorA");
		Book bookB = new Book("B", "TitleB", "AuthorB");

		// setup mock
		doReturn(bookA).when(bookRepoMock).findById("A");
		doReturn(bookB).when(bookRepoMock).findById("B");

		// do test
		assertThat(bookService.findBookById("A")).usingRecursiveComparison().isEqualTo(bookA);
		assertThat(bookService.findBookById("B")).usingRecursiveComparison().isEqualTo(bookB);
	}

	/**
	 * Убеждаемся, что методы сервиса не отлавливают (подавляют), а выбрасывают
	 * (пропускают) ровно те же исключения, что возникают в методах зависимости.
	 */
	@Test
	void serviceMethodsMustPropagateReposExceptions() {

		class SomeCustomException extends RuntimeException {
			private static final long serialVersionUID = 1L;
		}

		// setup mock
		when(bookRepoMock.findAll()).thenThrow(new SomeCustomException());
		when(bookRepoMock.findById(any())).thenThrow(new SomeCustomException());

		// do test
		assertThrows(SomeCustomException.class, () -> bookService.findAllBooks());
		assertThrows(SomeCustomException.class, () -> bookService.findBookById(""));
	}

	/**
	 * Передача недопустимого id (null, empty- или blank-стоки) должна выбрасывать
	 * соответствующее исключение.
	 */
	@Test
	void findBookByIdThrowsExceptionIfIllegalId() {

		// do test
		assertThrows(IllegalArgumentException.class, () -> bookService.findBookById(""));
		assertThrows(IllegalArgumentException.class, () -> bookService.findBookById("   "));
		assertThrows(NullPointerException.class, () -> bookService.findBookById(null));
	}

}
