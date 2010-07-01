package com.springsource.greenhouse.updates;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import org.springframework.jdbc.core.JdbcTemplate;
//import com.springsource.greenhouse.signin.GreenhouseUserDetails;
import org.springframework.jdbc.core.RowMapper;

public class GreenhouseUpdatesService {

	   private JdbcTemplate jdbcTemplate;
		
		@Inject
		public GreenhouseUpdatesService(JdbcTemplate jdbcTemplate) {
			this.jdbcTemplate = jdbcTemplate;
		}
		
		public void createUpdate(String updateText) {
			jdbcTemplate.update("insert into update (text, updateTimestamp) values (?, ?)", updateText, System.currentTimeMillis());
		}
		
//		public void createUpdate(String updateText, GreenhouseUserDetails details) {						
//			if (details != null) {
//				jdbcTemplate.update("insert into update (text, updateTimestamp) values (?, ?, userId)", updateText, System.currentTimeMillis(), details.getEntityId());
//			}
//		}
		
		public List<Update> getUpdates() {
			 return jdbcTemplate.query("select u.text, u.updateTimestamp from Update u", updateMapper);
		}
		
		private RowMapper<Update> updateMapper = new RowMapper<Update>() {
			public Update mapRow(ResultSet rs, int row) throws SQLException {
				Update update = new Update();
				update.setText(rs.getString("text"));
				update.setTimestamp(rs.getLong("updateTimestamp"));
				return update;
			}
		};
}