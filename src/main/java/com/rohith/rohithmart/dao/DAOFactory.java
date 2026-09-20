package com.rohith.rohithmart.dao;

/**
 * Factory class for instantiating and retrieving DAO components.
 * Satisfies Capstone Section 12 Factory pattern requirement.
 */
public class DAOFactory {

    private static final UserDAO USER_DAO = new UserDAOImpl();
    private static final ProductDAO PRODUCT_DAO = new ProductDAOImpl();
    private static final OrderDAO ORDER_DAO = new OrderDAOImpl();
    private static final CartItemDAO CART_ITEM_DAO = new CartItemDAOImpl();
    private static final ReviewDAO REVIEW_DAO = new ReviewDAOImpl();

    private DAOFactory() {
    }

    public static UserDAO getUserDAO() {
        return USER_DAO;
    }

    public static ProductDAO getProductDAO() {
        return PRODUCT_DAO;
    }

    public static OrderDAO getOrderDAO() {
        return ORDER_DAO;
    }

    public static CartItemDAO getCartItemDAO() {
        return CART_ITEM_DAO;
    }

    public static ReviewDAO getReviewDAO() {
        return REVIEW_DAO;
    }
}
