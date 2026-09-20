/**
 * RohithMart Cart Functionality
 */
document.addEventListener('DOMContentLoaded', function() {
    'use strict';

    // Quantity change listeners in cart
    const qtyInputs = document.querySelectorAll('.cart-qty-input');
    qtyInputs.forEach(input => {
        input.addEventListener('change', async function() {
            const cartItemId = this.dataset.cartItemId;
            const newQty = parseInt(this.value, 10);
            if (newQty < 1) return;

            const res = await RohithMart.fetchJson('/rohithmart/api/v1/cart/items/' + cartItemId, {
                method: 'PUT',
                body: { quantity: newQty }
            });

            if (res.ok && res.data.success) {
                window.location.reload();
            } else {
                RohithMart.showToast(res.data?.error?.message || 'Failed to update quantity', 'error');
            }
        });
    });

    // Quick add to cart buttons from product grids
    const addBtns = document.querySelectorAll('.add-to-cart-btn');
    addBtns.forEach(btn => {
        btn.addEventListener('click', async function() {
            const productId = this.dataset.productId;
            const res = await RohithMart.fetchJson('/rohithmart/api/v1/cart/items', {
                method: 'POST',
                body: { productId: parseInt(productId, 10), quantity: 1 }
            });

            if (res.ok && res.data.success) {
                RohithMart.showToast('Item added to cart!', 'success');
            } else if (res.status === 401) {
                window.location.href = '/rohithmart/auth/login';
            } else {
                RohithMart.showToast(res.data?.error?.message || 'Unable to add product to cart', 'error');
            }
        });
    });
});
