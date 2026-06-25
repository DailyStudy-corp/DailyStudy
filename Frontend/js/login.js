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
            const response = await fetch('http://localhost:8080/api/usuarios/login', {
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

    // ── LÓGICA DO OLHINHO ADICIONADA AQUI ──
    const togglePasswordBtn = document.getElementById('togglePassword');
    const eyeIcon = document.getElementById('eyeIcon');

    if (togglePasswordBtn && senhaInput && eyeIcon) {
        // Definição dos caminhos internos de cada estado do SVG
        const eyeOpenPaths = '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>';
        const eyeClosedPaths = '<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>';

        togglePasswordBtn.addEventListener('click', () => {
            if (senhaInput.type === 'password') {
                senhaInput.type = 'text';
                eyeIcon.innerHTML = eyeClosedPaths; // Desenha o risco
                togglePasswordBtn.style.color = 'var(--marrom-claro)'; // Dá o destaque visual na cor ativa
            } else {
                senhaInput.type = 'password';
                eyeIcon.innerHTML = eyeOpenPaths; // Volta para o olho aberto normal
                togglePasswordBtn.style.color = '#ccc';
            }
        });
    }
    // ── FIM DA LÓGICA DO OLHINHO ──

}); // fim do DOMContentLoaded