/*
  posts.js
  --------
  Gerencia tudo relacionado às postagens:
  - Renderizar o feed e a lista de posts no perfil
  - Criar um novo post (texto + imagem opcional)
  - Abrir o modal de edição e salvar a edição
  - Excluir um post com confirmação
  - Gerenciar a imagem pendente no compose box
  - Atualizar o contador de caracteres

  Depende de: storage.js, profile.js (devem ser carregados antes)
*/

const Posts = (() => {

  // ID do post que está sendo editado no modal (null quando fechado)
  let editingPostId = null;

  // Imagem selecionada no compose, ainda não publicada (null se vazia)
  let pendingImageUrl = null;

  let activeCommentPostId = null; // ID do post atualmente aberto no modal de comentários

  // ── Helpers ──────────────────────────────────────────────────

  // Formata uma data ISO para texto amigável em português.
  // "Hoje às 14:32" | "Ontem às 09:00" | "15 de jan. às 09:00"
  function formatDate(isoString) {
    const date      = new Date(isoString);
    const today     = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(today.getDate() - 1);

    const time = date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });

    if (date.toDateString() === today.toDateString()) {
      return `Hoje às ${time}`;
    }

    if (date.toDateString() === yesterday.toDateString()) {
      return `Ontem às ${time}`;
    }

    const dayMonth = date.toLocaleDateString('pt-BR', { day: 'numeric', month: 'short' });
    return `${dayMonth} às ${time}`;
  }

  // Escapa caracteres HTML para prevenir XSS.
  // Sempre usar ao inserir texto do usuário via innerHTML.
  function escapeHTML(text) {
    const div = document.createElement('div');
    div.appendChild(document.createTextNode(text));
    return div.innerHTML;
  }


  // ── Criação de cards ─────────────────────────────────────────

  // Cria e retorna o elemento HTML de um único post.
  // Cada card tem: cabeçalho (avatar, nome, data, ações) + texto + imagem + rodapé.

  //curica: Alterei as variaveis para ficar igual do backend, e fui renomeando onde estavam inseridas
  function createPostCard(post) {
    const autorNome  = post.autorUsername; 
    const autorFoto = post.autorImg;
    const initials = Profile.getInitials(autorNome);

    const card = document.createElement('article');
    card.className  = 'post-card';
    card.dataset.id = post.id;  // usado para encontrar o card no DOM depois

    // Monta o HTML do avatar (foto ou iniciais)
    const avatarHTML = autorFoto
      ? `<img src="${autorFoto}" alt="Foto de ${escapeHTML(autorNome)}"/>`
      : escapeHTML(initials);

    // Monta a imagem do post, se houver
    const imageHTML = post.mediaUrl
      ? `<div class="post-image">
           <img src="${post.mediaUrl}" alt="Imagem da postagem" data-action="lightbox" title="Clique para ampliar" loading="lazy"/>
         </div>`
      : '';

    // Monta a tag "editado", se o post foi modificado
    const editedHTML = post.dataEdicao
      ? `<p class="post-edited-tag">editado ${formatDate(post.dataEdicao)}</p>`
      : '';

    // Formata a data completa para o rodapé
    const fullDate = new Date(post.dataCriacao).toLocaleString('pt-BR', {
      day: 'numeric', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });

  // Alteracao GUI - Inicio (Adicionando os botoes de interacao e wrapper clicavel no texto do post)
  const heartClass = post.curtidoPeloUsuario ? 'int-btn like-btn active' : 'int-btn like-btn';

    card.innerHTML = `
      <div class="post-head">
        <div class="post-ava" data-action="profile">${avatarHTML}</div>
        <div class="post-meta">
          <div class="post-author">${escapeHTML(autorNome)}</div>
          <div class="post-date">${formatDate(post.dataCriacao)}</div>
        </div>
        <div class="post-actions">
          <button class="pa-btn"     data-action="edit"   title="Editar">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
            </svg>
          </button>
          <button class="pa-btn del" data-action="delete" title="Excluir">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="3 6 5 6 21 6"/>
              <path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4h6v2"/>
            </svg>
          </button>
        </div>
      </div>
      
      <div class="post-clickable-area" data-action="open-post" style="cursor: pointer;">
        <p class="post-text">${escapeHTML(post.content)}</p>
        ${imageHTML}
      </div>
      
      ${editedHTML}

      <div class="post-interactions" style="display: flex; gap: var(--sp-md, 16px); padding-top: var(--sp-sm, 8px); margin-bottom: var(--sp-xs, 4px);">
        <button class="int-btn reply-btn" data-action="reply" title="Responder">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
          </svg>
          <span class="int-count">${post.totalComentarios || 0}</span>
        </button>

        <button class="int-btn repost-btn" data-action="repost" title="Republicar">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="17 1 21 5 17 9"/>
            <path d="M3 11V9a4 4 0 0 1 4-4h14"/>
            <polyline points="7 23 3 19 7 15"/>
            <path d="M21 13v2a4 4 0 0 1-4 4H3"/>
          </svg>
          <span class="int-count">${post.qtdRepublicacoes || 0}</span>
        </button>

        <button class="${heartClass}" data-action="like" title="Curtir">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
          </svg>
          <span class="int-count">${post.totalCurtidas || 0}</span>
        </button>
      </div>

      <div class="post-footer">
        <time class="post-ts">${fullDate}</time>
      </div>
    
    `;

    // Listener único no card que detecta em qual botão foi clicado
    // (técnica chamada "event delegation")
   card.addEventListener('click', event => {
      const button = event.target.closest('[data-action]');
      if (!button) return;

      const action = button.dataset.action;

      // Isso evita que o clique no "Like" ou "Responder" acione o "open-post" do card
     
      if (action === 'like' || action === 'reply' || action === 'repost' || action === 'edit' || action === 'delete') {
        event.stopPropagation(); 
      }

      if (action === 'edit')      openEditModal(post.id, post.content);
      if (action === 'delete')    openDeleteModal(post.id);
      if (action === 'lightbox')  UI.openLightbox(post.mediaUrl);
      if (action === 'profile')   UI.activateTab('profile');

      if (action === 'like')      Posts.handleLikeToggle(post.id, button);
      if (action === 'reply')     Posts.handleOpenPostModal(post.id);
      if (action === 'open-post') Posts.handleOpenPostModal(post.id);
      if (action === 'repost')    UI.showToast('Recurso em desenvolvimento!', 'ok');
    });

    return card;
  }


  // ── Renderização ─────────────────────────────────────────────

  // Renderiza todos os posts no feed principal.
  // Chamada ao inicializar, publicar, editar ou excluir.

  /*curica: Implementei o endpoint do post, coloquei o token para autorizar
            o fetch ja vem com metodo GET padrao, entao nao tive que declarar
  */
  async function renderFeed() {
    const feedEl   = document.getElementById('feedList');
    const emptyEl  = document.getElementById('feedEmpty');
    const badgeEl  = document.getElementById('postBadge');
    const token = localStorage.getItem('token');

    feedEl.innerHTML = '';

    try {
      const response = await fetch('http://localhost:8080/api/posts', {
        headers: {'Authorization': `Bearer ${token}`}
      });

      if (!response.ok) throw new Error('Erro ao carregar feed');

      const posts = await response.json();

      feedEl.innerHTML = '';
    

    if (posts.length === 0) {
      emptyEl.classList.remove('hidden');
      badgeEl.textContent = '0 posts';
      return;
    }

    emptyEl.classList.add('hidden');
    badgeEl.textContent = `${posts.length} post${posts.length !== 1 ? 's' : ''}`;

    posts.forEach(post => feedEl.appendChild(createPostCard(post)));
  } catch (err) {
    feedEl.innerHTML = '';
    UI.showToast('Erro ao carregar o feed.', 'err')
  }
  }

  // Renderiza os posts na aba de perfil.
  // Funciona da mesma forma que renderFeed, mas em outro container.

  //curica: Aqui a mesma coisa que o de cima, porem nao esta funcionando corretamente, tem que revisar
  //curica: Atualizacao: Agora tem um endpoint especifico, e a logica nao fica no front
  async function renderProfilePosts() {
    const container = document.getElementById('profileFeed');
    const emptyEl   = document.getElementById('profileEmpty');
    const token = localStorage.getItem('token');

    try {
      const response = await fetch('http://localhost:8080/api/posts/mine', {
        headers: {'Authorization': `Bearer ${token}`},
      });

      if (!response.ok) throw new Error();

      const meusPosts = await response.json();

      container.innerHTML = '';

    //curica: aqui nao ta funcionando tambem
    if (meusPosts.length === 0) {
      emptyEl.classList.remove('hidden');
    } else {
      emptyEl.classList.add('hidden');
    meusPosts.forEach(post => container.appendChild(createPostCard(post)));
    }

    updateStats(meusPosts);

  } catch {
    UI.showToast('Erro ao carregar seus posts', 'err');
  }
}

  // Atualiza os números nos cards de estatísticas do perfil.
  function updateStats(posts) {

    const statPostsEl = document.getElementById('statPosts');
    if (statPostsEl) statPostsEl.textContent = posts.length;

    // Dias únicos com pelo menos um post
    // Set() elimina datas duplicadas automaticamente
    const uniqueDays  = new Set(posts.map(p => new Date(p.dataCriacao).toDateString()));
    const statDaysEl  = document.getElementById('statDays');
    if (statDaysEl) statDaysEl.textContent = uniqueDays.size;
  }


  // ── Ações de post ────────────────────────────────────────────

  // Lê o texto e a imagem pendente, cria o post e atualiza a tela.

  //curica: aqui é a mesma coisa, tive que apenas colocar o token pra validar e o fetch
  async function handlePublish() {
    const input = document.getElementById('postInput');
    const text  = input.value.trim();

    if (!text) return;

    const token = localStorage.getItem('token');

    try {
      const response = await fetch ('http://localhost:8080/api/posts', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          content: text,
          mediaUrl: pendingImageUrl
        })
      });

      if (!response.ok) throw new Error('Erro ao publicar');

      // Limpa o compose
      input.value = '';
      clearPendingImage();
      updateCharCounter('charCount', input, 500);
      document.getElementById('btnPost').disabled = true;

      renderFeed();
      // ── ALTERACAO: Correção no updateStats() vazio que quebrava o fluxo, agora puxando a lista atualizada ──
      const profileResponse = await fetch('http://localhost:8080/api/posts/mine', { headers: {'Authorization': `Bearer ${token}`} });
      if (profileResponse.ok) updateStats(await profileResponse.json());
      UI.showToast('Postagem publicada! 🎉', 'ok');

      // Scrolla suavemente para o topo do feed para ver o novo post
      document.getElementById('feedList').scrollIntoView({ behavior: 'smooth', block: 'start' });

    } catch (err) {
      UI.showToast(err.message, 'err');
    }
  }

  // Abre o modal com o texto atual do post para edição.
  function openEditModal(postId, currentText) {
    editingPostId = postId;

    const textarea = document.getElementById('editTa');
    textarea.value = currentText;
    updateCharCounter('editCount', textarea, 500);

    UI.openModal();
    textarea.focus();
  }

  // Salva o texto editado no modal.

  //curica: Coloquei o endpoint, com a variavel do id do post que esta sendo editado
  async function handleSaveEdit() {
    if (!editingPostId) return;

    const textarea = document.getElementById('editTa');
    const newText  = textarea.value.trim();

    if (!newText) {
      UI.showToast('O texto não pode estar vazio.', 'err');
      return;
    }

    const token = localStorage.getItem('token');

    try {
      const response = await fetch (`http://localhost:8080/api/posts/${editingPostId}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ content: newText })
      });

      if(!response.ok) throw new Error('Erro ao editar post');

      editingPostId = null;
      UI.closeModal();
      await renderFeed();
      await renderProfilePosts();
      UI.showToast('Post atualizado!', 'ok');

    } catch (err) {
      UI.showToast(err.message, 'err');
    }
  }

  // Pede confirmação antes de excluir o post.

  //curica: Coloquei o endpoint para excluir um post, com a variavel do id // Modal para exclusao do post junto com a logica e o endpoint do bot
  let postIdToDelete = null;  // ID do post que será excluído
   
  function openDeleteModal(postId) {
    postIdToDelete = postId;
    const modal = document.getElementById('deleteModalBackdrop');
    if (modal) {
      modal.classList.remove('hidden');
    }
  }
    function closeDeleteModal() {
    postIdToDelete = null;
    const modal = document.getElementById('deleteModalBackdrop');
    if (modal) {
      modal.classList.add('hidden');
     }
  } 
    async function executePostDeletion() {
      if (!postIdToDelete) return;

      const token = localStorage.getItem('token');
      try {
    const response = await fetch(`http://localhost:8080/api/posts/${postIdToDelete}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    });
   
    if (!response.ok) throw new Error('Erro ao excluir o post');
     closeDeleteModal();
     await renderFeed();
    await renderProfilePosts();
    UI.showToast('Postagem excluída.', 'err');

  } catch (err) {
    UI.showToast(err.message, 'err');
  }
}

  // ── ALTERAÇÃO: Verificação de elementos para não dar erro se rodar em telas sem esses listeners específicos ──
  document.getElementById('deleteModalCancelBtn')?.addEventListener('click', closeDeleteModal);
  document.getElementById('deleteModalCloseBtn')?.addEventListener('click', closeDeleteModal);
  document.getElementById('deleteModalConfirmBtn')?.addEventListener('click', executePostDeletion);

  async function confirmAndDelete(postId) {
    const confirmed = window.confirm('Deseja excluir esta postagem? Esta ação não pode ser desfeita.');
    if (!confirmed) return;

    const token = localStorage.getItem('token');

    try {
      const response = await fetch(`http://localhost:8080/api/posts/${postId}`, {
        method: 'DELETE',
        headers: {'Authorization': `Bearer ${token}`}
      });

      if (!response.ok) throw new Error('Erro ao excluir o post');

      await renderFeed();
      await renderProfilePosts();
      UI.showToast('Postagem excluída.', 'err');

    } catch (err) {
      UI.showToast(err.message, 'err');
    }
  }


  // ── Imagem no compose ────────────────────────────────────────

  // Converte o arquivo selecionado para Base64 e mostra a pré-visualização.
  async function handleImageSelect(file) {
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      UI.showToast('Por favor, selecione apenas imagens.', 'err');
      return;
    }

    UI.showToast('Carregando imagem…');

    try {
      const dataUrl    = await Profile.readFileAsDataUrl(file);
      pendingImageUrl  = dataUrl;

      // Mostra a pré-visualização
      document.getElementById('composePreviewImg').src = dataUrl;
      document.getElementById('composeImgPreview').classList.remove('hidden');
      document.querySelector('.img-tool-btn').classList.add('has-image');

      UI.showToast('Imagem pronta para publicar!', 'ok');

    } catch (err) {
      console.error('Erro ao carregar imagem:', err);
      UI.showToast('Erro ao carregar a imagem.', 'err');
    }
  }

  // Remove a imagem pendente e limpa a pré-visualização.
  function clearPendingImage() {
    pendingImageUrl = null;

    const previewContainer = document.getElementById('composeImgPreview');
    const previewImg       = document.getElementById('composePreviewImg');
    const imgInput         = document.getElementById('imgInput');
    const imgToolBtn       = document.querySelector('.img-tool-btn');

    if (previewContainer) previewContainer.classList.add('hidden');
    if (previewImg)       previewImg.src = '';
    if (imgInput)         imgInput.value = '';  // permite selecionar o mesmo arquivo de novo
    if (imgToolBtn)       imgToolBtn.classList.remove('has-image');
  }


  // ── Contador de caracteres ───────────────────────────────────

  // Atualiza o contador e muda a cor conforme o limite se aproxima.
  // counterId → ID do elemento span que mostra o número
  // inputEl   → o textarea monitorado
  // maxLength → limite máximo de caracteres
  function updateCharCounter(counterId, inputEl, maxLength) {
    const counterEl = document.getElementById(counterId);
    if (!counterEl || !inputEl) return;

    const remaining = maxLength - inputEl.value.length;
    counterEl.textContent = remaining;

    counterEl.classList.remove('warn', 'danger');
    if (remaining <= 20) counterEl.classList.add('danger');
    else if (remaining <= 80) counterEl.classList.add('warn');
  }

  // Alteracao 3 Gui: Funcoes de suporte para o like e abertura de modal
  async function handleLikeToggle(postId, buttonEl) {
    const token = localStorage.getItem('token');
    
    // Procura o elemento do contador (.int-count) dentro do botão ou próximo a ele
    const countEl = buttonEl.querySelector('.int-count');
    
    try {
      const response = await fetch(`http://localhost:8080/api/posts/${postId}/curtida`, {
        method: 'POST',
        headers: { 
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) throw new Error("Nao foi possivel curtir essa porra");
      const data = await response.json(); 

      // Alinhamento exato com o seu CurtidaDTO(boolean curtido, long total) do Java
     if (data.curtido) {
        buttonEl.classList.add('active');
      } else {
        buttonEl.classList.remove('active');
      }

      // Atualiza o número do contador na tela com o valor retornado do banco
      if (countEl) {
        countEl.textContent = data.total;
      }
    } catch (err) {
      console.error('Erro ao curtir post:', err);
    }
  }

  // ── ALTERAÇÃO: Fechamento completo e implementação real da chamada de comentários do back para o modal ──
   async function handleOpenPostModal(postId) {
    UI.showToast(`Carregando respostas...`, 'ok');
    const token = localStorage.getItem('token');
    activeCommentPostId = postId;
    
    try {
      // 1 requisição = post principal + comentários juntos!
      const response = await fetch(`http://localhost:8080/api/posts/${postId}/detalhes`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });

      if (!response.ok) throw new Error('Erro ao carregar post');

      const { post, comentarios } = await response.json();

      // Renderiza o post principal no modal
      const originalPostContainer = document.getElementById('commentModalOriginalPost');
      if (originalPostContainer) {
        originalPostContainer.innerHTML = '';

        // Cria HTML do post principal
        const autorImg = post.autorImg 
          ? `<img src="${post.autorImg}" alt="Foto de ${escapeHTML(post.autorUsername)}"/>`
          : escapeHTML(post.autorUsername.substring(0, 2).toUpperCase());

        const imageHTML = post.mediaUrl
          ? `<div class="post-image"><img src="${post.mediaUrl}" alt="Imagem do post"/></div>`
          : '';

        const postHTML = `
          <article class="post-card modal-post">
            <div class="post-head">
              <div class="post-ava">${autorImg}</div>
              <div class="post-meta">
                <div class="post-author">${escapeHTML(post.autorUsername)}</div>
                <div class="post-date">${formatDate(post.dataCriacao)}</div>
              </div>
            </div>
            <p class="post-text">${escapeHTML(post.content)}</p>
            ${imageHTML}
          </article>
        `;

        originalPostContainer.innerHTML = postHTML;
      }

      // Renderiza os comentários
      const repliesListContainer = document.getElementById('commentModalRepliesList');
      if (repliesListContainer) {
        repliesListContainer.innerHTML = '';

        if (comentarios.length === 0) {
          repliesListContainer.innerHTML = `<p style="color: var(--ink-3, #999); text-align: center; font-size: 0.9rem; padding: var(--sp-md) 0;">Nenhuma resposta ainda. Seja o primeiro a comentar!</p>`;
        } else {
          comentarios.forEach(reply => {
            repliesListContainer.appendChild(createPostCard(reply, true));
          });
        }
      }

      // Abre o modal na tela
      UI.openCommentModal();

    } catch (err) {
      console.error('Erro ao carregar post:', err);
      UI.showToast('Erro ao carregar os comentários do post.', 'err');
    }
  }
  // ── API pública ──────────────────────────────────────────────

return {
    renderFeed,
    renderProfilePosts,
    updateStats,
    handlePublish,
    openEditModal,
    handleSaveEdit,
    handleImageSelect,
    clearPendingImage,
    updateCharCounter,
    handleLikeToggle,
    handleOpenPostModal,
    getActiveCommentPostId: () => activeCommentPostId,
  };

})();