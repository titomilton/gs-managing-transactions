package hello;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import org.junit.Assert;

@SpringBootApplication
public class Application {
	private static final Logger log = LoggerFactory.getLogger(Application.class);

	@Bean
	BookingService bookingService() {
		return new BookingService();
	}

	@Bean
	JdbcTemplate jdbcTemplate(DataSource dataSource) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		log.info("Creating tables");
		jdbcTemplate.execute("DROP TABLE bookings IF EXISTS");
		jdbcTemplate.execute("CREATE TABLE bookings(" + " id SERIAL, first_name VARCHAR(5) NOT NULL) ");
		return jdbcTemplate;
	}

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(Application.class, args);
		BookingService bookingService = ctx.getBean(BookingService.class);
		bookingService.books("Alice", "Bob", "Carol");
		Assert.assertEquals("First booking should work with no problem", 3, bookingService.findAllBookings().size());

		try {
			bookingService.books("Chris", "Samuel");
		} catch (RuntimeException e) {
			log.info("v--- The following exception is expect because 'Samuel' is too big for the DB ---v");
			log.error(e.getMessage());
		}

		for (String person : bookingService.findAllBookings()) {
			log.info("So far, " + person + " is booked.");
		}
		log.info(
				"You shouldn't see Chris or Samuel. Samuel violated DB contraints, and Chris was rolled back in the same TX");
		Assert.assertEquals("'Samuel' should have triggered a rollback", 3, bookingService.findAllBookings().size());

		try {
			bookingService.books("Buddy", null);
		} catch (RuntimeException e) {
			log.info("v--- The following exception is expect because null is not valid for the DB ---v");
			log.error(e.getMessage());
		}
		
		for (String person : bookingService.findAllBookings()) {
			log.info("So far, " + person + " is booked.");
		}
		log.info("You shouldn't see Buddy or null. null violated DB constraints, and Buddy was rolled back in the same TX");
		Assert.assertEquals("'null' should have triggered a rollback", 3, bookingService
				.findAllBookings().size());
	}
}
