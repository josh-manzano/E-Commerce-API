package org.yearup.data.mysql;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.*;

import javax.sql.DataSource;
import java.util.List;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao{
    private JdbcTemplate jdbcTemplate;

    public MySqlShoppingCartDao(DataSource dataSource) {
        super(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    //gets all products in users cart
    @Override
    public ShoppingCart getByUserId(int userId)
    {
        String sql =
                """
                SELECT sc.product_id ,
                sc.quantity ,
                p.name ,
                p.price ,
                p.category_id ,
                p.description ,
                p.color ,
                p.image_url ,
                p.stock ,
                p.featured
                FROM shopping_cart sc
                JOIN products p ON p.product_id = sc.product_id
                WHERE sc.user_id = ?;
                """;

        ShoppingCart cart = new ShoppingCart(); // empty cart

        try
        {
            List<ShoppingCartItem> items = jdbcTemplate.query(sql, (rs, rowNum) -> {

// builds product
                Product product = new Product(
                        rs.getInt ("product_id"),
                        rs.getString("name"),
                        rs.getBigDecimal("price"),
                        rs.getInt ("category_id"),
                        rs.getString("description"),
                        rs.getString("color"),
                        rs.getInt ("stock"),
                        rs.getBoolean("featured"),
                        rs.getString("image_url")
                );

// builds item in cart
                ShoppingCartItem item = new ShoppingCartItem();
                item.setProduct(product);
                item.setQuantity(rs.getInt("quantity"));

                return item;
            }, userId);

            for (ShoppingCartItem item : items)
                cart.add(item);

            return cart;
        }
        catch (DataAccessException ex)
        {
            ex.printStackTrace();
            return cart; // return an empty cart
        }
    }

    @Override
    public void addItem(int userId, int productId, int quantity)
    {
        String sql =
                """
                INSERT INTO shopping_cart (user_id, product_id, quantity)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity); 
                """; // updates quantity if the item is already in cart
        jdbcTemplate.update(sql, userId, productId, quantity);
    }

    @Override
    public void updateQuantity(int userId, int productId, int quantity)
    {
        String sql =
                """
                UPDATE shopping_cart
                SET quantity = ?
                WHERE user_id = ?
                AND product_id = ?;
                """;
        jdbcTemplate.update(sql, quantity, userId, productId);
    }

    @Override
    public void removeItem(int userId, int productId)
    {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ? AND product_id = ?;";
        jdbcTemplate.update(sql, userId, productId);
    }

    @Override
    public void clearCart(int userId)
    {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?;";
        jdbcTemplate.update(sql, userId);
    }
}
