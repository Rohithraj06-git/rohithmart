/**
 * RohithMart Checkout Functionality
 * Manages customer details validation, mock payment selection, and submission loading state.
 */
document.addEventListener('DOMContentLoaded', function() {
    'use strict';

    const checkoutForm = document.getElementById('checkout-form');
    const paymentRadios = document.querySelectorAll('input[name="paymentMethod"]');
    const mockCardSection = document.getElementById('mock-card-details');

    // 1. Payment Method Switching
    paymentRadios.forEach(radio => {
        radio.addEventListener('change', function() {
            document.querySelectorAll('.payment-card-box').forEach(box => box.classList.remove('selected'));
            this.closest('.payment-card-box')?.classList.add('selected');

            if (mockCardSection) {
                if (this.value === 'MOCK_CARD') {
                    mockCardSection.style.display = 'block';
                } else {
                    mockCardSection.style.display = 'none';
                }
            }
        });
    });

    // 2. Checkout Validation & Loading State
    if (checkoutForm) {
        checkoutForm.addEventListener('submit', function(e) {
            const fullName = document.getElementById('fullName');
            const street = document.getElementById('shippingAddress');
            const city = document.getElementById('city');
            const postalCode = document.getElementById('postalCode');

            if (fullName && !fullName.value.trim()) {
                e.preventDefault();
                RohithMart.showToast('Please enter your full name.', 'error');
                fullName.focus();
                return;
            }

            if (street && !street.value.trim()) {
                e.preventDefault();
                RohithMart.showToast('Please enter your shipping street address.', 'error');
                street.focus();
                return;
            }

            if (city && !city.value.trim()) {
                e.preventDefault();
                RohithMart.showToast('Please enter your city.', 'error');
                city.focus();
                return;
            }

            if (postalCode && !postalCode.value.trim()) {
                e.preventDefault();
                RohithMart.showToast('Please enter your postal code.', 'error');
                postalCode.focus();
                return;
            }

            const submitBtn = checkoutForm.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.classList.add('loading');
                submitBtn.textContent = 'Securing & Placing Order...';
            }
        });
    }
});
