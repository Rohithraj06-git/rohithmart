package com.rohith.rohithmart.service;

/**
 * Factory providing singleton instances of service layer implementations.
 * Satisfies Capstone Section 12 Factory pattern requirement.
 */
public class ServiceFactory {

    private static final AuthService AUTH_SERVICE = new AuthServiceImpl();
    private static final UserService USER_SERVICE = new UserServiceImpl();
    private static final ProductService PRODUCT_SERVICE = new ProductServiceImpl();
    private static final CartService CART_SERVICE = new CartServiceImpl();
    private static final OrderService ORDER_SERVICE = new OrderServiceImpl();
    private static final ReviewService REVIEW_SERVICE = new ReviewServiceImpl();
    private static final ChatService CHAT_SERVICE = new ChatServiceImpl();

    private ServiceFactory() {
    }

    public static AuthService getAuthService() {
        return AUTH_SERVICE;
    }

    public static UserService getUserService() {
        return USER_SERVICE;
    }

    public static ProductService getProductService() {
        return PRODUCT_SERVICE;
    }

    public static CartService getCartService() {
        return CART_SERVICE;
    }

    public static OrderService getOrderService() {
        return ORDER_SERVICE;
    }

    public static ReviewService getReviewService() {
        return REVIEW_SERVICE;
    }

    public static ChatService getChatService() {
        return CHAT_SERVICE;
    }
}
