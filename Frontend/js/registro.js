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
    
        // ALTERAÇÃO 2: TRATAMENTO DO E-MAIL (.toLowerCase)
        // Converte o e-mail para letras minúsculas antes de enviar ao banco.
        // Isso evita que a Regex rígida do Java (@Email) rejeite letras maiúsculas.

        //ALTERAÇÃO 3: ATUALIZAÇÃO DA VALIDAÇÃO NO FRONTEND
        // Agora o 'username' também é obrigatório. Também adicionamos uma trava 
        // prévia para o tamanho da senha, poupando requisições desnecessárias.


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
            // 1. Monta os dados exatamente como o RegistroDTO.java espera no backend
            const dadosRegistro = {
                username: username, // CORRECAO DOS NOMES DOS CAMPOS PARA REGISTRO 
                email: email,
                senha: senha
            };

            // --- INÍCIO DO TESTE DE CONFIRMAÇÃO ---
            // Nota: Se o front rodar em porta diferente do back (ex: 5173 e 8080), lembre de usar a URL completa 'http://localhost:8080/api/usuarios/register'
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
}); // Fim do DOMContentLoaded