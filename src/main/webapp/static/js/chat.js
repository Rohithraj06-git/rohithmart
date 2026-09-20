/**
 * RohithMart AI Chat Assistant Client
 */
document.addEventListener('DOMContentLoaded', function() {
    'use strict';

    const chatFab = document.getElementById('ai-chat-btn');
    const chatPanel = document.getElementById('ai-chat-panel');
    const chatClose = document.getElementById('ai-chat-close');
    const chatForm = document.getElementById('ai-chat-form');
    const chatInput = document.getElementById('ai-chat-input');
    const chatMessages = document.getElementById('ai-chat-messages');

    if (!chatFab || !chatPanel || !chatForm) return;

    chatFab.addEventListener('click', function() {
        chatPanel.classList.toggle('hidden');
        if (!chatPanel.classList.contains('hidden')) {
            chatInput.focus();
        }
    });

    chatClose.addEventListener('click', function() {
        chatPanel.classList.add('hidden');
    });

    chatForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        const text = chatInput.value.trim();
        if (!text) return;

        // Append user bubble
        appendBubble(text, 'user');
        chatInput.value = '';

        // Append loading placeholder
        const loadingBubble = appendBubble('Thinking...', 'ai');

        try {
            const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1)) || '';
            const res = await RohithMart.fetchJson(contextPath + '/api/chat', {
                method: 'POST',
                body: { message: text }
            });

            if (res.ok && res.data && res.data.reply) {
                loadingBubble.textContent = res.data.reply;
            } else if (res.data && res.data.error && res.data.error.message) {
                loadingBubble.textContent = res.data.error.message;
            } else {
                loadingBubble.textContent = "I'm sorry, I am currently unable to process your request. Please try again later.";
            }
        } catch (err) {
            loadingBubble.textContent = "Unable to connect to assistant service.";
        }

        chatMessages.scrollTop = chatMessages.scrollHeight;
    });

    function appendBubble(text, role) {
        const bubble = document.createElement('div');
        bubble.className = `chat-bubble ${role}`;
        bubble.textContent = text;
        chatMessages.appendChild(bubble);
        chatMessages.scrollTop = chatMessages.scrollHeight;
        return bubble;
    }
});
