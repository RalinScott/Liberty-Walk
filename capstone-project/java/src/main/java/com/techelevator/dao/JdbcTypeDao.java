package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.Attraction;
import com.techelevator.model.Badge;
import com.techelevator.model.Type;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTypeDao implements TypeDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTypeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Type> getTypes() {
        List<Type> types = new ArrayList<>();
        String sql = "SELECT type_id, name" +
                " FROM type ORDER BY name ASC";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            while (results.next()) {
                Type type = mapRowToType(results);
                types.add(type);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return types;
    }

    @Override
    public Type getTypeById(int id) {
        Type type = null;
        String sql = "SELECT type_id, name " +
                " FROM type WHERE type_id = ?";

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);
            if (results.next()) {
                type = mapRowToType(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return type;
    }

   @Override
    public Type getTypeByName(String name) {
        if (name == null) throw new IllegalArgumentException("name cannot be null");
        Type type = null;
        String sql = "SELECT type_id, name " +
                " FROM type WHERE name ILIKE ?";
        try {
            SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, name);
            if (rowSet.next()) {
                type = mapRowToType(rowSet);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return type;
    }

    @Override
    public String getTypeByAttractionId(int id) {
        String typeName = null;
        String sql = "SELECT t.name " +
                " FROM type t JOIN attraction a ON a.type_id = t.type_id WHERE a.attraction_id =?";
        try {
            SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, id);
            if (rowSet.next()) {
                typeName = rowSet.getString("name");
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return typeName;
    }


    @Override
    public Type updateType(Type type) {
        Type updatedType = null;
        String sql = "UPDATE type SET name=?\n" +
                "\tWHERE type_id=?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, type.getName(), type.getId());

            if (rowsAffected == 0) {
                throw new DaoException("Zero rows affected, expected at least one");
            } else {
                updatedType = getTypeById(type.getId());
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }

        return updatedType;
    }

    @Override
    public Type createType(Type type) {
        Type newType = null;
        String insertTypeSql = "INSERT INTO type (" +
                " name) " +
                " VALUES (?)" +
                " RETURNING type_id";

        try {
            int newTypeId = jdbcTemplate.queryForObject(insertTypeSql, int.class, type.getName());
            newType = getTypeById(newTypeId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return newType;
    }

    @Override
    public int deleteTypeById(int id) {
        int numberOfRows = 0;
        String typeDeleteSql = "DELETE FROM type WHERE type_id = ?";
        try {
            numberOfRows = jdbcTemplate.update(typeDeleteSql, id);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return numberOfRows;
    }

    private Type mapRowToType(SqlRowSet rs) {
        Type type = new Type();
        type.setId(rs.getInt("type_id"));
        type.setName(rs.getString("name"));
        return type;
    }
}
