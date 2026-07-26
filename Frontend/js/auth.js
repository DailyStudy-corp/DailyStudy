const Auth = (() => {
    function getToken () {
        return sessionStorage.getItem('ds_token');
    }

    function setToken(token){
       sessionStorage.setItem('ds_token', token);
    }

    function clearToken(token){
        sessionStorage.removeItem('ds_token');
    }

    function headers(){
        const token = getToken();

        return token ? { 'Authorization': `Bearer ${token}` } : {};
    }

    return { getToken, setToken, clearToken, headers };
})();