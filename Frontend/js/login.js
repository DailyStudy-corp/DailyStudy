document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const emailInput = document.getElementById('email');
    const senhaInput = document.getElementById('senha');
    const mensagemErro = document.getElementById('mensagemErro');

    if (mensagemErro) {
        mensagemErro.style.display = 'none';
    }

    loginForm.addEventListener('submit', async (e) => { // Adicionamos async aqui
        e.preventDefault();

        const emailDigitado = emailInput.value.trim();
        const senhaDigitada = senhaInput.value.trim();

        // Limpa mensagens anteriores
        mensagemErro.style.display = 'none';

        try {
            // 1. Monta os dados exatamente como o LoginDTO.java espera no backend
            const dadosLogin = {
                email: emailDigitado,
                senha: senhaDigitada
            };

            // 2. Faz a requisição HTTP POST para o endpoint de login do seu amigo
            // Nota: Confirme com ele se a rota é exatamente '/api/usuarios/login' ou '/api/login'
            const response = await fetch('/api/usuarios/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(dadosLogin)
            });

            // 3. Trata a resposta do backend
            if (response.ok) {
                const dadosResponse = await response.json();
                
                // O backend do seu amigo provavelmente retorna um objeto com o token (ex: dadosResponse.token)
                const token = dadosResponse.token; 

                // Salva o estado de autenticado e o token para usar nas próximas páginas
                localStorage.setItem('isAuthenticated', 'true');
                localStorage.setItem('token', token); // Isso vai ser vital para o feed de posts depois

                // Redireciona para a home (ajuste o nome do arquivo se necessário, ex: home.html)
                window.location.href = 'home.html';
            } else {
                // Se o backend retornar 401 (Não autorizado) ou 404 (Não encontrado)
                mensagemErro.textContent = 'E-mail ou senha incorretos.';
                mensagemErro.style.display = 'block';
            }

        } catch (error) {
            // Se o backend estiver desligado ou houver erro de rede
            console.error('Erro ao conectar com o backend:', error);
            mensagemErro.textContent = 'Erro ao conectar ao servidor. Tente novamente mais tarde.';
            mensagemErro.style.display = 'block';
        }
    });
});