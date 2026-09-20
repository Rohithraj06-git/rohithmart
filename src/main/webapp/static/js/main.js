/**
 * RohithMart Core JavaScript Client
 */
(function() {
    'use strict';

    // Global namespace
    window.RohithMart = window.RohithMart || {};

    // Standard JSON Fetch Wrapper
    RohithMart.fetchJson = async function(url, options = {}) {
        options.headers = options.headers || {};
        options.headers['Accept'] = 'application/json';
        if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
            options.headers['Content-Type'] = 'application/json';
            options.body = JSON.stringify(options.body);
        }

        try {
            const response = await fetch(url, options);
            const data = await response.json();
            return { ok: response.ok, status: response.status, data };
        } catch (err) {
            console.error('Network or parsing error:', err);
            return { ok: false, status: 0, error: { message: 'Network connection failed' } };
        }
    };
})();
