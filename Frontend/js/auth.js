const Auth = (() => {
    // Chave única para o token em toda a aplicação
    const TOKEN_KEY = 'ds_token';

    function getToken() {
        return localStorage.getItem(TOKEN_KEY);
    }

    function setToken(token) {
        localStorage.setItem(TOKEN_KEY, token);
    }

    function clearToken() {
        localStorage.removeItem(TOKEN_KEY);
    }

    function headers() {
        const token = getToken();
        return token ? { 'Authorization': `Bearer ${token}` } : {};
    }

    return { getToken, setToken, clearToken, headers };
})();