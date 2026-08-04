const RateLimit = (() => {
    let activeTimer = null;

    //Checa se é um erro 429(muitas requisicoes)
    function isRateLimited(response) {
        return response.status === 429;
    }

    function getRetryAfterSeconds(response, fallbackSeconds = 30) {
        const header = response.headers.get('Retry-After');
        const parsed = parseInt(header, 10);
        
        return Number.isFinite(parsed) && parsed > 0 ? parsed : fallbackSeconds;
    }

    function handleRateLimit(response, {button, messageEl}) {
        if (activeTimer) clearInterval(activeTimer);

        let secondsLeft = getRetryAfterSeconds(response);

        button.disabled = true;
        updateMessage(messageEl, secondsLeft);

        activeTimer = setInterval(() => {
            secondsLeft -= 1;

            if (secondsLeft <= 0) {
                clearInterval(activeTimer);
                activeTimer = null;
                button.disabled = false;
                if (messageEl) messageEl.style.display = 'none';

                return;
            }

            updateMessage(messageEl, secondsLeft);
        }, 1000);
    }

    function updateMessage(messageEl, secondsLeft) {
        if (!messageEl) return;
        messageEl.textContent = `Muitas tentativas. Tente novamente em ${secondsLeft}s`;
        messageEl.style.display = 'block';
    }

    return {isRateLimited, handleRateLimit};
})();