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
  // Ex: "User"  
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
  function fillimg_perfil(element, name, imageUrl) {
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
  
  function syncUI() {
    const profile = Storage.getProfile();
    if (!profile) return;

    // Alt 1 Isola o elemento do composebox
     const composeAva = document.getElementById('composeAva');
     if (composeAva) {
        fillimg_perfil(composeAva, profile.name || profile.username, profile.img_perfil);
      }

    // Avatar grande na aba de perfil
    const profileAvaBig= document.getElementById('profileAvaBig');
    if (profileAvaBig) {
      fillimg_perfil(profileAvaBig, profile.name || profile.username, profile.img_perfil);
    } 
  
    // Nome, cargo e bio
    const nameEl = document.getElementById('pName');
    const bioEl  = document.getElementById('pBio');

   // Alt 2 - Carrega o profile.username como fallback na hora de editar o form

    if (nameEl) nameEl.textContent = profile.name || profile.username || '';
    if (bioEl)  bioEl.textContent  = profile.bio || '';
    
    // Sincroniza o cargo (p-role) na tela inicial puxando o objeto do perfil
    const roleEl = document.getElementById('pRole');
    if (roleEl) roleEl.textContent = profile.role || 'Estudante · Daily Study';
  
    // Alt 3 - Nova logica para renderizar o banner.
    const bannerZone = document.getElementById('bannerZone');
    if (bannerZone) {
      if (profile.banner_perfil) {
        // Aplica o Base64 direto como fundo do bannerZone
        bannerZone.style.backgroundImage = `url('${profile.banner_perfil}')`;
        bannerZone.style.backgroundSize = 'cover';
        bannerZone.style.backgroundPosition = 'center';
      } else {
        // Se não tiver imagem no perfil, remove o fundo para manter o padrão do CSS
        bannerZone.style.backgroundImage = 'none';  
     }
   } 
  }      
  // ── Formulário de edição ─────────────────────────────────────

  // Abre o formulário preenchido com os dados atuais.
  function openEditForm() {
    const profile = Storage.getProfile();

    document.getElementById('eName').value = profile.name || profile.username || '';
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
      const response = await fetch('http://localhost:8080/api/usuarios/me/perfil', {
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


    //ALTERACAO 6 - CAPTURA DE IMAGEM E  PAYLOAD UNIFICADO PARA AVATAR E BANNER
    try {
      const dataUrl = await readFileAsDataUrl(file);
      const token = localStorage.getItem('token');
      const currentProfile = Storage.getProfile();
           
      // alt 5 - Passou de ava e banner para - img perfil e banner_perfil
      const payload = {
        img_perfil: type === 'img_perfil' ? dataUrl : (currentProfile.img_perfil || null),
        banner_perfil: type === 'banner_perfil' ? dataUrl : (currentProfile.banner_perfil || null)
      };
                                      //localhost:8080/api/usuarios/me - Caso nao seja o caminho abaixo da API, alterar para este
      const response = await fetch('http://localhost:8080/api/usuarios/me/img_perfil', {
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
      //Alterado de avatar para img_perfil
      if (type === 'img_perfil') {
        Storage.patchProfile({ img_perfil: dataUrl });
        UI.showToast('Foto de perfil atualizada! 📸', 'ok');
      } else if (type === 'banner_perfil') {
        Storage.patchProfile({ banner_perfil: dataUrl });
        UI.showToast('Banner updated! 🖼️', 'ok');
      }

      syncUI();

    } catch (err) {
      console.error('Erro no upload:', err);
      UI.showToast(err.message || 'Erro ao carregar a imagem.', 'err');
    }
  }

  function setReadOnly(isReadOnly) {
    const btnEditP = document.getElementById('btnEditP');
    if(btnEditP) btnEditP.style.display = isReadOnly ? 'none' : '';

    document.getElementById('profileAvaBig')?.classList.toggle('readOnly-ava', isReadOnly);
    document.getElementById('bannerZone')?.classList.toggle('readOnly-banner', isReadOnly);
  }

  function openProfile(username) {
    const myUsername = Storage.getProfile().username;
  
    UI.activateTab('profile');
  
    if (username === myUsername) {
      syncUI();
      setReadOnly(false);
      Posts.renderProfilePosts();
      return;
    }
  
    loadProfileVisited(username);
  }

  async function loadProfileVisited(username) {
    const token = localStorage.getItem('token');
  
    try {
      const response = await fetch(
        `http://localhost:8080/api/usuarios/perfil/${encodeURIComponent(username)}`,
        { headers: { 'Authorization': `Bearer ${token}` } }
      );
  
      if (!response.ok) {
        throw new Error('Falha ao carregar o perfil do usuário.');
      }
  
      const perfil = await response.json();
      renderProfileVisited(perfil);
  
    } catch (err) {
      console.error('Erro ao carregar perfil visitado:', err);
      UI.showToast('Não foi possível carregar este perfil.', 'err');
    }
  }

  function renderProfileVisited(perfil){
    document.getElementById('pName').textContent = perfil.username;
    document.getElementById('pRole').textContent = perfil.cargo || 'Estudante';
    document.getElementById('pBio').textContent = perfil.bio || '';

    fillimg_perfil(document.getElementById('profileAvaBig'), perfil.username, perfil.img_perfil);

    const bannerZone = document.getElementById('bannerZone');
    if (perfil.banner_perfil) {
      bannerZone.style.backgroundImage = `url('${perfil.banner_perfil}')`;
    bannerZone.style.backgroundSize = 'cover';
    bannerZone.style.backgroundPosition = 'center';
  } else {
    bannerZone.style.backgroundImage = 'none';
  }
    const posts = perfil.posts || [];
    Posts.renderExternalPosts(posts, 'profileFeed', 'profileEmpty');
    Posts.updateStats(posts);
    
    setReadOnly(true);
  }




  // ── API pública ──────────────────────────────────────────────

  return {
    get:               Storage.getProfile,
    getInitials,
    readFileAsDataUrl,
    fillimg_perfil,
    syncUI,
    openEditForm,
    closeEditForm,
    saveEditForm,
    handleImageUpload,
    openProfile,
  };

})();