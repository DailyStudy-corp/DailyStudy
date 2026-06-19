/*
  profile.js
  ----------
  Gerencia tudo relacionado ao perfil do usuário:
  - Exibir nome, bio, avatar e banner na tela
  - Upload de foto de perfil e banner (converte para Base64)
  - Abrir/fechar/salvar o formulário de edição

  Depende de: storage.js (deve ser carregado antes no HTML)
*/

const Profile = (() => {

  // ── Funções auxiliares ───────────────────────────────────────

  // Gera as iniciais do nome para usar como fallback do avatar.
  // Ex: "João da Silva" → "JS" | "Ana" → "A"
  function getInitials(name) {
    const words = (name || 'U').trim().split(/\s+/).filter(Boolean);

    if (words.length === 1) {
      return words[0][0].toUpperCase();
    }

    const firstLetter = words[0][0].toUpperCase();
    const lastLetter  = words[words.length - 1][0].toUpperCase();
    return firstLetter + lastLetter;
  }

  // Preenche um elemento de avatar com foto ou iniciais.
  // Se tiver foto, insere uma <img> dentro do elemento.
  // Se não tiver, coloca o texto com as iniciais.
  function fillAvatar(element, name, imageUrl) {
    if (!element) return;

    element.innerHTML = '';  // limpa conteúdo anterior

    if (imageUrl) {
      const img = document.createElement('img');
      img.src = imageUrl;
      img.alt = `Foto de ${name}`;
      element.appendChild(img);
    } else {
      element.textContent = getInitials(name);
    }
  }

  // Lê um arquivo de imagem e retorna uma Promise com a Data URL (Base64).
  // Necessário para salvar imagens no localStorage.
  function readFileAsDataUrl(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload  = event => resolve(event.target.result);
      reader.onerror = ()    => reject(new Error('Falha ao ler o arquivo.'));
      reader.readAsDataURL(file);
    });
  }


  // ── Sincronização da UI ──────────────────────────────────────

  // Atualiza todos os elementos visuais com os dados atuais do perfil.
  // Chamada sempre que o perfil muda (editar nome, trocar foto, etc.).
  function syncUI() {
    const profile = Storage.getProfile();

    // Avatar no compose box
    fillAvatar(
      document.getElementById('composeAva'),
      profile.name,
      profile.avatarUrl
    );

    // Avatar grande na aba de perfil
    fillAvatar(
      document.getElementById('profileAvaBig'),
      profile.name,
      profile.avatarUrl
    );

    // Nome, cargo e bio
    const nameEl = document.getElementById('pName');
    const bioEl  = document.getElementById('pBio');
    if (nameEl) nameEl.textContent = profile.name;
    if (bioEl)  bioEl.textContent  = profile.bio || '';
    
    // Sincroniza o cargo (p-role) na tela inicial puxando o objeto do perfil
    const roleEl = document.getElementById('pRole');
    if (roleEl) roleEl.textContent = profile.role || 'Estudante · Daily Study';
  
    // Banner: mostra a imagem se existir, esconde se não existir
    const bannerImg = document.getElementById('bannerImg');
    if (bannerImg) {
      if (profile.bannerUrl) {
        bannerImg.src = profile.bannerUrl;
        bannerImg.classList.remove('hidden');
      } else {
        bannerImg.src = '';
        bannerImg.classList.add('hidden');
      }
    }
  }


  // ── Formulário de edição ─────────────────────────────────────

  // Abre o formulário preenchido com os dados atuais.
  function openEditForm() {
    const profile = Storage.getProfile();

    document.getElementById('eName').value = profile.name;
    document.getElementById('eBio').value  = profile.bio || '';
    
    // Carrega o cargo atual ou o valor padrão no campo de edição
    const eRoleInput = document.getElementById('eRole');
    if (eRoleInput) {
      eRoleInput.value = profile.role || 'Estudante · Daily Study';
    }

    document.getElementById('editForm').classList.remove('hidden');
    document.getElementById('btnEditP').style.display = 'none';

    document.getElementById('eName').focus();
  }

  // Fecha o formulário sem salvar.
  function closeEditForm() {
    document.getElementById('editForm').classList.add('hidden');
    document.getElementById('btnEditP').style.display = '';
  }

  // Valida e salva as alterações de nome e bio.
  async function saveEditForm() {
    const name = document.getElementById('eName').value.trim();
    const bio  = document.getElementById('eBio').value.trim();
    // Captura o valor digitado no input do cargo
    const role = document.getElementById('eRole') ? document.getElementById('eRole').value.trim() : '';

    if (!name) {
      UI.showToast('O nome não pode estar vazio.', 'err');
      document.getElementById('eName').focus();
      return;
    }
    
    UI.showToast('Salvando alterações…');
    
    try {
      const token = localStorage.getItem('token');
      // Adicionado await para esperar a resposta da API corretamente
      const response = await fetch('http://localhost:8080/api/usuarios/meu-perfil', {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`, // Padronizado o 'Authorization' com 'A' maiúsculo
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ 
          username: name,  
          cargo: role, 
          bio: bio
        })
      });

      if (!response.ok) {
        throw new Error('Falha ao atualizar o perfil no servidor.');
      }

      Storage.patchProfile({ name, role, bio });
      syncUI();
      closeEditForm();

      // Re-renderiza posts para atualizar o nome do autor nos cards
      Posts.renderFeed();
      Posts.renderProfilePosts();

      UI.showToast('Perfil updated!', 'ok');

    } catch (err) {
      console.error('Erro ao salvar o formulário:', err);
      UI.showToast(err.message || 'Erro ao sincronizar com o servidor.', 'err');
    }
  }


  // ── Upload de imagens ────────────────────────────────────────

  // Processa o arquivo selecionado e salva como avatar ou banner.
  // type deve ser 'avatar' ou 'banner'.
  async function handleImageUpload(file, type) {
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      UI.showToast('Por favor, selecione apenas imagens.', 'err');
      return;
    }

    UI.showToast('Carregando imagem…');

    try {
      const dataUrl = await readFileAsDataUrl(file);
      const token = localStorage.getItem('token'); // Corrigido para 'localStorage' com S maiúsculo
      const currentProfile = Storage.getProfile();
           
      // Corrigida a estrutura do payload que estava solta no código anterior
      const payload = {
        img_perfil: type === 'avatar' ? dataUrl : currentProfile.avatarUrl,
        banner_perfil: type === 'banner' ? dataUrl : currentProfile.bannerUrl
      };
      
      const response = await fetch('http://localhost:8080/api/usuarios/meu-perfil/imagem', {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        throw new Error('Falha ao atualizar a imagem no servidor.');
      }
      
      if (type === 'avatar') {
        Storage.patchProfile({ avatarUrl: dataUrl });
        UI.showToast('Foto de perfil atualizada! 📸', 'ok');
      } else {
        Storage.patchProfile({ bannerUrl: dataUrl });
        UI.showToast('Banner updated! 🖼️', 'ok');
      }

      syncUI();

    } catch (err) {
      console.error('Erro no upload:', err);
      UI.showToast(err.message || 'Erro ao carregar a imagem.', 'err');
    }
  }


  // ── API pública ──────────────────────────────────────────────

  return {
    get:               Storage.getProfile,
    getInitials,
    readFileAsDataUrl,
    fillAvatar,
    syncUI,
    openEditForm,
    closeEditForm,
    saveEditForm,
    handleImageUpload,
  };

})();