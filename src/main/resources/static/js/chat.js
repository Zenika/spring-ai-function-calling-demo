const conversation = document.getElementById('conversation');
const userInput = document.getElementById('userInput');
const conversationIdInput = document.getElementById('conversationId');

function newUUID() {
    return crypto.randomUUID();
}

conversationIdInput.value = conversationIdInput.value || newUUID();

function addMessage(text, isUser) {
    const div = document.createElement('div');
    div.className = isUser ? 'user-message' : 'agent-message';
    div.innerHTML = '<pre>' + text + '</pre>';
    conversation.appendChild(div);
    conversation.scrollTop = conversation.scrollHeight;
}

function sendMessage() {
    const message = userInput.value.trim();
    if (!message) return;
    const conversationId = conversationIdInput.value;
    console.log("Sending message:", message);
    console.log("Conversation ID:", conversationId);

    addMessage(message, true);
    userInput.value = '';

    // first create div for the agent response
    const responseDiv = document.createElement('div');
    responseDiv.className = 'agent-message';
    responseDiv.innerHTML = '<pre></pre>';
    conversation.appendChild(responseDiv);
    const chatResponseContent = responseDiv.querySelector('pre');
    conversation.scrollTop = conversation.scrollHeight;

    fetch('/conversationsAsync', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            conversationId: conversationId,
            message: message
        })
    }).then(response => {
        if (response.status != 200) {
            chatResponseContent.textContent = 'erreur de l\'agent';
            return;
        }
        const reader = response.body.getReader();
        let textDecoder = new TextDecoder();
        let messageLength = 0;

        function readStream() {
            reader.read().then(({done, value}) => {
                if (done) {
                    if (messageLength === 0) {
                        chatResponseContent.textContent = 'Aucun résultat trouvé';
                    } else {
                        responseDiv.innerHTML = marked.parse(chatResponseContent.textContent);
                    }
                    conversation.scrollTop = conversation.scrollHeight;
                    return;
                }
                const text = textDecoder.decode(value);
                chatResponseContent.textContent += text;
                messageLength += text.length;
                conversation.scrollTop = conversation.scrollHeight;
                readStream();
            });
        }

        readStream();
    });
}

userInput.addEventListener('keypress', function (e) {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        sendMessage();
    }
});