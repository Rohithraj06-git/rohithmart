/**
 * RohithMart Checkout Functionality
 */
document.addEventListener('DOMContentLoaded', function() {
    'use strict';

    const checkoutForm = document.getElementById('checkout-form');
    if (checkoutForm) {
        checkoutForm.addEventListener('submit', function(e) {
            const submitBtn = checkoutForm.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = 'Processing Order...';
            }
        });
    }
});
