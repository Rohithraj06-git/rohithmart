/**
 * RohithMart Cart Functionality
 * Authoritative server-driven cart updates, quantity controls, and item removal.
 */
document.addEventListener('DOMContentLoaded', function() {
    'use strict';

    const contextPath = RohithMart.getContextPath();

    // 1. Quantity change listeners in cart
    const qtyInputs = document.querySelectorAll('.cart-qty-input');
    qtyInputs.forEach(input => {
        let previousValue = parseInt(input.value, 10);

        input.addEventListener('focus', function() {
            previousValue = parseInt(this.value, 10);
        });

        input.addEventListener('change', async function() {
            const cartItemId = this.dataset.cartItemId;
            const newQty = parseInt(this.value, 10);

            if (isNaN(newQty) || newQty < 1) {
                this.value = previousValue;
                RohithMart.showToast('Quantity must be at least 1.', 'error');
                return;
            }

            const row = this.closest('tr');
            if (row) row.style.opacity = '0.5';

            try {
                const res = await RohithMart.fetchJson(contextPath + '/api/cart/update', {
                    method: 'POST',
                    body: { cartItemId: parseInt(cartItemId, 10), quantity: newQty }
                });

                if (res.ok && res.data) {
                    previousValue = newQty;
                    RohithMart.showToast('Cart quantity updated.', 'success');

                    // Update live totals from authoritative server response
                    if (res.data.total !== undefined) {
                        const totalEl = document.querySelector('.total-amount');
                        if (totalEl) {
                            totalEl.textContent = '$' + Number(res.data.total).toFixed(2);
                        }
                    }

                    // Update item subtotal from authoritative response
                    if (res.data.items && row) {
                        const updatedItem = res.data.items.find(i => String(i.id) === String(cartItemId));
                        if (updatedItem && updatedItem.subtotal !== undefined) {
                            const subtotalTd = row.querySelector('.item-subtotal');
                            if (subtotalTd) {
                                subtotalTd.textContent = '$' + Number(updatedItem.subtotal).toFixed(2);
                            }
                        }
                    }

                    if (res.data.itemCount !== undefined) {
                        RohithMart.updateCartBadge(res.data.itemCount);
                    }
                } else {
                    // Stock error or validation failure: revert input
                    this.value = previousValue;
                    const errMsg = res.data?.message || 'Failed to update quantity. Check available stock.';
                    RohithMart.showToast(errMsg, 'error');
                }
            } catch (err) {
                this.value = previousValue;
                RohithMart.showToast('Network error while updating quantity.', 'error');
            } finally {
                if (row) row.style.opacity = '1';
            }
        });
    });

    // 2. Remove Item with Confirmation Dialog
    const removeButtons = document.querySelectorAll('.btn-remove-cart-item');
    removeButtons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const cartItemId = this.dataset.cartItemId;
            const productName = this.dataset.productName || 'this product';

            RohithMart.Modal.confirm(
                'Remove Cart Item',
                `Are you sure you want to remove "${productName}" from your shopping cart?`,
                async function() {
                    const res = await RohithMart.fetchJson(contextPath + '/api/cart/remove', {
                        method: 'POST',
                        body: { cartItemId: parseInt(cartItemId, 10) }
                    });

                    if (res.ok) {
                        RohithMart.showToast('Item removed from cart.', 'success');
                        setTimeout(() => {
                            window.location.reload();
                        }, 400);
                    } else {
                        RohithMart.showToast(res.data?.message || 'Could not remove item.', 'error');
                    }
                },
                'Remove Item',
                true
            );
        });
    });
});
