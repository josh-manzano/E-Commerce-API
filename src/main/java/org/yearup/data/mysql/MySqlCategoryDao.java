package org.yearup.data.mysql;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao
{
    public MySqlCategoryDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public List<Category> getAllCategories() {
        // get all categories
        String sql = "SELECT * FROM categories;";
        try{
            return jdbcTemplate.query(sql, (rs, numRow) -> mapRow(rs));
        }catch (DataAccessException e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Category getById(int categoryId)
    {
        // get category by id
        String sql = "SELECT * FROM categories WHERE category_id = ?;";
        try{
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRow(rs), categoryId);
        }catch (DataAccessException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Category create(Category category) {
        // create a new category
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?);";
        try {
            jdbcTemplate.update(sql, category.getName(), category.getDescription());
            return category;
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void update(int categoryId, Category category)
    {
        // update category
        String sql = "UPDATE categories SET name = ?, description = ? WHERE category_id = ?;";
        try{
            jdbcTemplate.update(sql, category.getName(), category.getDescription(), categoryId);
        }catch (DataAccessException e){
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int categoryId)
    {
        // delete category
        String sql = "DELETE FROM categories WHERE category_id = ?;";
        try{
            jdbcTemplate.update(sql, categoryId);
        }catch (DataAccessException e){
            e.printStackTrace();
        }
    }

    private Category mapRow(ResultSet row) throws SQLException
    {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        Category category = new Category()
        {{
            setCategoryId(categoryId);
            setName(name);
            setDescription(description);
        }};

        return category;
    }

}
