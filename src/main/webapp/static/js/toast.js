/**
 * RohithMart Toast Notification Utility
 */
(function() {
    'use strict';

    window.RohithMart = window.RohithMart || {};

    RohithMart.showToast = function(message, type = 'success') {
        const container = document.getElementById('toast-container');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast toast-${type} show`;
        toast.setAttribute('role', 'alert');

        const msgSpan = document.createElement('span');
        msgSpan.className = 'toast-message';
        msgSpan.textContent = message;

        const closeBtn = document.createElement('button');
        closeBtn.type = 'button';
        closeBtn.className = 'toast-close';
        closeBtn.innerHTML = '&times;';
        closeBtn.onclick = function() {
            toast.remove();
        };

        toast.appendChild(msgSpan);
        toast.appendChild(closeBtn);
        container.appendChild(toast);

        setTimeout(() => {
            if (toast.parentElement) {
                toast.remove();
            }
        }, 4000);
    };

    // Auto-dismiss existing flash toasts after 4 seconds
    document.addEventListener('DOMContentLoaded', function() {
        const existingToasts = document.querySelectorAll('.toast');
        existingToasts.forEach(t => {
            setTimeout(() => {
                if (t.parentElement) t.remove();
            }, 4000);
        });
    });
})();
