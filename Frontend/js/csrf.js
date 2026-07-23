const Csrf = (() => {

    //Ele recebe o cookie emitido pelo Spring Security, e monta a header com as chamadas da API(POST,PUT,DELETE)
    function getCookie(name) {
        const match = document.cookie.match(new RegExp('(?:^|; )' + name + '=([^;]*)'));
        return match ? decodeURIComponent(match[1]) : null;
    }

    function headers() {
        const token = getCookie('XSRF-TOKEN');
        return token ? {'X-XSRF-TOKEN' : token} : {};
    }

    return {headers};
})();