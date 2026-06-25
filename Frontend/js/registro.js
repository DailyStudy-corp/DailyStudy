document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('registerForm');
    const emailInput = document.getElementById('emailRegistro');
    const senhaInput = document.getElementById('senhaRegistro');
    const mensagemSucesso = document.getElementById('mensagemSucesso');
    const mensagemErro = document.getElementById('mensagemErroRegistro');
    const usernameInput = document.getElementById('usernameRegistro'); // NOVA LINHA PARA O CAMPO DE USERNAME

    registerForm.addEventListener('submit', async (e) => { 
        e.preventDefault();
    // 2. CAPTURA E TRATAMENTO DOS VALORES DOS INPUTS
        const username = usernameInput ? usernameInput.value.trim() : '';  //LINHA NOVA DA SOLUCAO DE REGISTRO
        const senha = senhaInput.value.trim();
        const email = emailInput.value.trim();
    
        

        if (!username || !email || !senha) {
            mensagemErro.textContent = 'Preencha todos os campos obrigatórios (Usuário, E-mail e Senha).';
            mensagemErro.style.display = 'block';
            return;
        }
          
        if (senha.length < 8) {
            mensagemErro.textContent = 'A senha deve ter no mínimo 8 caracteres.';
            mensagemErro.style.display = 'block';
            return;
        }
          
        // Limpa mensagens anteriores antes de tentar um novo envio
        mensagemErro.style.display = 'none';
        if (mensagemSucesso) mensagemSucesso.style.display = 'none';

        try {
            // 1. Monta os dados exatamente como o RegistroDTO.java espera no backend.
            const dadosRegistro = {
                username: username, // CORRECAO DOS NOMES DOS CAMPOS PARA REGISTRO 
                email: email,
                senha: senha
            };

            // --- INÍCIO DO TESTE DE CONFIRMAÇÃO ---
            const response = await fetch('http://localhost:8080/api/usuarios/registro', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(dadosRegistro)
            });
            
            // Se a resposta for OK (Status 200 a 299)
            if (response.ok) {
                console.log("---------------------------------------------------");
                console.log("Sucesso: os dados foram enviados e salvos no banco de dados.");
                console.log("---------------------------------------------------");

                // ── ALTERACAO 1: Perfil inicial ajustado com as novas chaves snake_case ──
                const novoPerfilInicial = {
                    name: username, 
                    role: 'Estudante · Daily Study', 
                    bio: 'Apaixonado por aprender. Sempre estudando algo novo a cada dia.',
                    img_perfil: null,
                    banner_perfil: null
                };

                // Salva o objeto transformado em texto na chave 'ds_profile' do localStorage
                localStorage.setItem('ds_profile', JSON.stringify(novoPerfilInicial));

                // Exibe mensagem de sucesso visual na tela
                if (mensagemSucesso) mensagemSucesso.style.display = 'block';
                registerForm.reset();
                
                // Redireciona para o login apos 1.5 segundos
                setTimeout(() => {
                    if (mensagemSucesso) mensagemSucesso.style.display = 'none';
                    window.location.href = 'login.html'; 
                }, 1500);

            } else {
                // Caso o banco recuse o registro (ex: email já existe), o backend retorna status de erro (ex: 400)
                console.log("FALHA: O backend retornou um erro durante o registro.");
                
                const erroServidor = await response.json();
                console.error('Erro do servidor:', erroServidor);
                
                mensagemErro.textContent = erroServidor.message || 'Erro ao registrar. Tente novamente.';
                mensagemErro.style.display = 'block';
            }

        } catch (error) {
            console.error('Erro de rede ou servidor offline:', error);
            mensagemErro.textContent = 'Não foi possível conectar ao servidor. O backend está rodando?';
            mensagemErro.style.display = 'block';
        }
    }); // Fim do addEventListener

    // ── LÓGICA DO OLHINHO ADICIONADA AQUI ──
    const togglePasswordRegisterBtn = document.getElementById('togglePasswordRegister');
    const eyeIconRegister = document.getElementById('eyeIconRegister');

    if (togglePasswordRegisterBtn && senhaInput && eyeIconRegister) {
        // Definição dos caminhos internos de cada estado do SVG
        const eyeOpenPathsRegister = '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>';
        const eyeClosedPathsRegister = '<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>';

        togglePasswordRegisterBtn.addEventListener('click', () => {
            if (senhaInput.type === 'password') {
                senhaInput.type = 'text';
                eyeIconRegister.innerHTML = eyeClosedPathsRegister; // Desenha o risco
                togglePasswordRegisterBtn.style.color = 'var(--marrom-claro)'; 
            } else {
                senhaInput.type = 'password';
                eyeIconRegister.innerHTML = eyeOpenPathsRegister; // Volta ao normal
                togglePasswordRegisterBtn.style.color = '#ccc';
            }
        });
    }
    // ── FIM DA LÓGICA DO OLHINHO ──

}); // Fim do DOMContentLoaded