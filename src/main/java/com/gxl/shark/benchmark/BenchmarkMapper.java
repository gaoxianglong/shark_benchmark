package com.gxl.shark.benchmark;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class BenchmarkMapper implements RowMapper<Benchmark> {
	@Override
	public Benchmark mapRow(ResultSet rs, int rowNum) throws SQLException {
		rs.getInt("b_id");
		return null;
	}
}
