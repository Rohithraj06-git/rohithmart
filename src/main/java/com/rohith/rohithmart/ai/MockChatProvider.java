package com.rohith.rohithmart.ai;

import java.util.Locale;

/**
 * Canned FAQ provider that operates without network access or API credentials.
 * Implements Capstone Section 11/17 requirement for offline/mock domain questions.
 */
public class MockChatProvider implements ChatProvider {

    @Override
    public String getReply(String userMessage, String context) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "How can I assist you with your RohithMart shopping today?";
        }

        String msg = userMessage.toLowerCase(Locale.ROOT);

        if (msg.contains("return") || msg.contains("refund")) {
            return "RohithMart offers a 30-day hassle-free return policy for eligible products in original condition.";
        } else if (msg.contains("shipping") || msg.contains("delivery")) {
            return "Standard delivery typically takes 2-4 business days. Track your shipments under 'My Orders'.";
        } else if (msg.contains("seller") || msg.contains("sell")) {
            return "To sell on RohithMart, register an account as a 'Seller' and access the Seller Portal to list your items.";
        } else if (msg.contains("payment") || msg.contains("pay") || msg.contains("checkout")) {
            return "Checkout currently supports simulated instant card payments and Cash on Delivery mock confirmations.";
        } else if (msg.contains("order") || msg.contains("status")) {
            return "You can check the real-time status of your orders (Pending, Confirmed, Shipped, Delivered) in the 'My Orders' section.";
        } else if (msg.contains("contact") || msg.contains("support") || msg.contains("help")) {
            return "You can reach RohithMart support anytime by emailing support@rohithmart.com.";
        } else if (msg.contains("category") || msg.contains("categories") || msg.contains("products")) {
            return "We currently feature Electronics, Fashion, Home & Living, and Books from verified sellers.";
        }

        return "Thank you for asking! For specific order inquiries, please visit 'My Orders' or contact support@rohithmart.com.";
    }
}
