package hello;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

public class BookingService {
	private final static Logger log = LoggerFactory.getLogger(BookingService.class);

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Transactional
	public void books(String... persons) {
		for (String person : persons) {
			log.info("Booking " + person + " in a seat...");
			jdbcTemplate.update("INSERT INTO bookings(first_name) VALUES (?)", person);
		}
	}

	public List<String> findAllBookings() {
		return jdbcTemplate.query("SELECT first_name FROM bookings", new RowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getString("first_name");
			}
		});
	}

}
