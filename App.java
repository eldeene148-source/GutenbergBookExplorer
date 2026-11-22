import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class App {

    static Scanner input = new Scanner(System.in);

    // Fixed-size arrays
    static Book[] books = new Book[100];
    static Book[] readingList = new Book[20];

    static int bookCount = 0;
    static int readingCount = 0;

    // Book class
    static class Book {
        String title;
        String author;
        String url;

        Book(String title, String author, String url) {
            this.title = title;
            this.author = author;
            this.url = url;
        }
    }

    // Read and validate user input
    static int readNumber() {
        try {
            return Integer.parseInt(input.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // Retrieve books from Project Gutenberg
    static void exploreBooks() {

        try {
            System.out.println("\nConnecting to Books to Scrape...");

            Document doc = Jsoup.connect(
                "https://books.toscrape.com/"
            ).userAgent("Mozilla/5.0").timeout(15000).get();

            Elements links = doc.select("article.product_pod h3 a");

            bookCount = 0;

            for (Element link : links) {

                if (bookCount >= books.length) {
                    break;
                }

                String title = link.attr("title").trim();
                if (title.isEmpty()) {
                    title = link.text().trim();
                }
                String url = link.absUrl("href");

                if (title.isEmpty() || url.isEmpty()) {
                    continue;
                }

                // Avoid duplicate books
                boolean duplicate = false;

                for (int i = 0; i < bookCount; i++) {
                    if (books[i].url.equals(url)) {
                        duplicate = true;
                        break;
                    }
                }

                if (duplicate) {
                    continue;
                }

                // Extract author from the book detail page
                String author = "Unknown";

                try {
                    Document details = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .timeout(15000)
                        .get();

                    Element authorLabel = details.selectFirst(
                        "table.table-striped tr:has(th:matchesOwn(?i:author)) td"
                    );

                    if (authorLabel != null) {
                        author = authorLabel.text().trim();
                    }
                } catch (Exception detailError) {
                    // Keep the book with Unknown author if its detail page fails.
                }

                books[bookCount] =
                    new Book(title, author, url);

                bookCount++;
            }

            System.out.println("\nBooks Retrieved Successfully!");

            if (bookCount < 10) {
                System.out.println(
                    "Warning: Fewer than 10 books were retrieved."
                );
            }

            displayBooks();

        } catch (Exception e) {
            System.out.println(
                "Connection Error: " + e.getMessage()
            );
        }
    }

    // Display all retrieved books
    static void displayBooks() {

        if (bookCount == 0) {
            System.out.println("No books available.");
            return;
        }

        System.out.println("\n===== AVAILABLE BOOKS =====");

        for (int i = 0; i < bookCount; i++) {
            System.out.println("\n" + (i + 1) + ". "
                    + books[i].title);
            System.out.println("Author: " + books[i].author);
            System.out.println("URL: " + books[i].url);
        }
    }

    // Make sure books are loaded
    static void ensureBooksLoaded() {
        if (bookCount == 0) {
            exploreBooks();
        }
    }

    // Search by title
    static void searchBook(String title) {

        ensureBooksLoaded();

        if (bookCount == 0) return;

        boolean found = false;

        System.out.println("\n===== SEARCH RESULTS =====");

        for (int i = 0; i < bookCount; i++) {

            if (books[i].title.toLowerCase()
                    .contains(title.toLowerCase())) {

                System.out.println("\nTitle: " + books[i].title);
                System.out.println("Author: " + books[i].author);
                System.out.println("URL: " + books[i].url);

                found = true;
            }
        }

        if (!found) {
            System.out.println("No matching books found.");
        }
    }

    // Overloaded search method
    static void searchBook(String keyword,
                           boolean searchByAuthor) {

        ensureBooksLoaded();

        if (bookCount == 0) return;

        boolean found = false;

        System.out.println("\n===== SEARCH RESULTS =====");

        for (int i = 0; i < bookCount; i++) {

            String value;

            if (searchByAuthor) {
                value = books[i].author;
            } else {
                value = books[i].title;
            }

            if (value.toLowerCase()
                    .contains(keyword.toLowerCase())) {

                System.out.println("\nTitle: " + books[i].title);
                System.out.println("Author: " + books[i].author);
                System.out.println("URL: " + books[i].url);

                found = true;
            }
        }

        if (!found) {
            System.out.println("No matching books found.");
        }
    }

    // Add a book to reading list
    static void addBook() {

        ensureBooksLoaded();

        if (bookCount == 0) return;

        if (readingCount >= readingList.length) {
            System.out.println("Reading list is full!");
            return;
        }

        displayBooks();

        System.out.print("\nEnter book number to add: ");
        int choice = readNumber();

        if (choice < 1 || choice > bookCount) {
            System.out.println("Invalid book number!");
            return;
        }

        Book selected = books[choice - 1];

        // Check duplicates
        for (int i = 0; i < readingCount; i++) {

            if (readingList[i].url.equals(selected.url)) {
                System.out.println("Book already exists!");
                return;
            }
        }

        readingList[readingCount] = selected;
        readingCount++;

        System.out.println("Book added successfully!");
    }

    // Display reading list
    static void viewReadingList() {

        System.out.println("\n===== MY READING LIST =====");

        if (readingCount == 0) {
            System.out.println("Reading list is empty.");
            return;
        }

        for (int i = 0; i < readingCount; i++) {

            System.out.println("\n" + (i + 1) + ". "
                    + readingList[i].title);

            System.out.println(
                "Author: " + readingList[i].author
            );
        }
    }

    // Remove a book
    static void removeBook() {

        viewReadingList();

        if (readingCount == 0) return;

        System.out.print("\nEnter book number to remove: ");
        int choice = readNumber();

        if (choice < 1 || choice > readingCount) {
            System.out.println("Invalid choice!");
            return;
        }

        for (int i = choice - 1; i < readingCount - 1; i++) {
            readingList[i] = readingList[i + 1];
        }

        readingList[readingCount - 1] = null;
        readingCount--;

        System.out.println("Book removed successfully!");
    }

    // Display book details
    static void displayBookDetails() {

        ensureBooksLoaded();

        if (bookCount == 0) return;

        displayBooks();

        System.out.print("\nEnter book number: ");
        int choice = readNumber();

        if (choice < 1 || choice > bookCount) {
            System.out.println("Invalid choice!");
            return;
        }

        Book selected = books[choice - 1];

        System.out.println("\n===== BOOK DETAILS =====");
        System.out.println("Title: " + selected.title);
        System.out.println("Author: " + selected.author);
        System.out.println("Gutenberg URL: " + selected.url);
    }

    // Main method
    public static void main(String[] args) {

        int choice;

        do {

            System.out.println("\n================================");
            System.out.println("       BOOK EXPLORER");
            System.out.println("================================");
            System.out.println("1. Explore Books");
            System.out.println("2. Search by Title");
            System.out.println("3. Search by Author");
            System.out.println("4. Add Book to Reading List");
            System.out.println("5. View Reading List");
            System.out.println("6. Remove Book");
            System.out.println("7. Display Book Details");
            System.out.println("0. Exit");

            System.out.print("\nEnter your choice: ");

            choice = readNumber();

            if (choice == 1) {
                exploreBooks();

            } else if (choice == 2) {

                System.out.print("Enter book title: ");
                String title = input.nextLine();

                searchBook(title);

            } else if (choice == 3) {

                System.out.print("Enter author name: ");
                String author = input.nextLine();

                searchBook(author, true);

            } else if (choice == 4) {
                addBook();

            } else if (choice == 5) {
                viewReadingList();

            } else if (choice == 6) {
                removeBook();

            } else if (choice == 7) {
                displayBookDetails();

            } else if (choice == 0) {
                System.out.println("Thank you for using Gutenberg Book Explorer!");

            } else {
                System.out.println("Invalid choice! Try again.");
            }

        } while (choice != 0);

        input.close();
    }
}
