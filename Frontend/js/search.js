/*
  search.js
  ---------
  Implementa a busca de posts e perfis.

  Conceitos importantes usados aqui:

  1. Normalização: antes de comparar textos, removemos acentos e
     colocamos tudo em minúsculas. Assim "João" == "joao" na busca.

  2. Debounce: aguarda 300ms após o usuário parar de digitar antes
     de executar a busca. Evita processar cada tecla individualmente.

  3. Scoring: posts onde o termo aparece no início recebem pontuação
     maior. Isso ordena os resultados do mais ao menos relevante.

  4. Highlight: o termo encontrado é envolvido com <mark> no HTML
     para aparecer destacado visualmente nos resultados.

  Depende de: storage.js, profile.js, ui.js
*/

const Search = (() => {

  // Timer do debounce (guardado para poder cancelar se o usuário continuar digitando)
  let debounceTimer = null;

  // Delay em milissegundos — padrão da indústria para buscas em tempo real
  const DEBOUNCE_DELAY = 300;


  // ── Normalização ─────────────────────────────────────────────

  // Remove acentos e converte para minúsculas.
  // "São Paulo" → "sao paulo" | "REACT.js" → "react.js"
  function normalize(text) {
    if (!text) return '';
    return text
      .toLowerCase()
      .normalize('NFD')                   // separa "ã" em "a" + "~"
      .replace(/[\u0300-\u036f]/g, '')    // remove os acentos separados
      .trim();
  }

  // Escapa caracteres especiais de HTML para prevenir XSS.
  function escapeHTML(text) {
    const div = document.createElement('div');
    div.appendChild(document.createTextNode(text || ''));
    return div.innerHTML;
  }

  // Escapa caracteres especiais de RegExp para usar texto do usuário em regex.
  // Ex: "c++" sem escape quebraria o new RegExp()
  function escapeRegex(text) {
    return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }


  // ── Chama no backend (relevância) ─────────────────────────────────────

  // Calcula uma pontuação para um texto em relação à query.
  // Quanto maior o score, mais relevante é o resultado.
  //
  // Pontuação:
  //   0 → não encontrou nada
  //   2 → encontrou em algum lugar do texto
  //   5 → texto começa com a query
  //   3 → query aparece no início de alguma palavra

  //curica: Lógica é parecida com isso, so que agora uma busca de verdade
  async function buscarBackend(query) {
    const response = await fetch(`/api/search?q=${encodeURIComponent(query)}`, {
      headers: {...Auth.headers()} ,
    });

    if (!response.ok) {
      throw new Error(`Sem resposta: ${response.status}`);
    }

    return response.json();
  }

  async function buscarPosts(query) {
    
    const resultado = await buscarBackend(query);
    return resultado.posts;
  }

  // Descobre em qual campo a query bateu, só pra escolher o badge do card.
  // Aproximação (substring, não a mesma tokenização do índice)
  function campoCorrespondente(usuario, query) {

    const q = normalize(query);

    if (normalize(usuario.username).includes(q))
      return {classe: 'match-name', label: 'usuário'};

    if (normalize(usuario.cargo).includes(q))
      return {classe: 'match-bio', label: 'cargo'};

    return {classe: 'match-bio', label: 'bio'};
  }


  // ── Renderiza os resultados ──────────────────────────────────────────

  // Busca no nome e bio do usuário.
  // O nome tem peso dobrado por ser a informação principal.
  // Retorna array com 0 ou 1 resultado (só há um usuário atualmente).
  async function executeSearch(rawQuery) {

    const query = rawQuery.trim();
    const resultsEl = document.getElementById('searchResults');
    const placeholder = document.getElementById('searchPlaceholder');
    const countEl = document.getElementById('searchResultCount');

    if (!resultsEl) return;

    // Campo vazio: mostra o placeholder com as sugestões
    if (!query) {
      placeholder?.classList.remove('hidden');
      resultsEl.innerHTML = '';
      countEl?.classList.add('hidden');
      return;
    }

    placeholder?.classList.add('hidden');
    resultsEl.innerHTML = '';

    let resultado;
    try {
      resultado = await buscarBackend(query);
    } catch (err) {
      console.error('Erro na busca:', err);
      resultsEl.innerHTML = `
        <div class="search-empty">
          <div class="search-empty-icon">⚠️</div>
          <p>Não foi possível buscar agora.</p>
          <span>Tente novamente em instantes.</span>
        </div>
      `;
      countEl?.classList.add('hidden');
      return;
    }

    const { usuarios, posts } = resultado;
    const totalResults = usuarios.length + posts.length;

    // Atualiza o contador de resultados
    if (countEl) {
      countEl.textContent = totalResults === 0
        ? 'Sem resultados'
        : `${totalResults} resultado${totalResults !== 1 ? 's' : ''}`;
      countEl.classList.remove('hidden');
    }

    // Nenhum resultado encontrado
    if (totalResults === 0) {
      resultsEl.innerHTML = `
        <div class="search-empty">
          <div class="search-empty-icon">🔍</div>
          <p>Nenhum resultado para <strong>"${escapeHTML(query)}"</strong></p>
          <span>Tente palavras diferentes ou verifique a ortografia.</span>
        </div>
      `;
      return;
    }

    // Renderiza seção de usuários (aparecem primeiro)
    if (usuarios.length > 0) {
      const section = document.createElement('div');
      section.className = 'search-section';
      section.innerHTML = `<h3 class="search-section-title">Usuários</h3>`;

      usuarios.forEach(usuario => {
        section.appendChild(createProfileCard({ usuario, query }));
      });

      resultsEl.appendChild(section);
    }

    // Renderiza seção de posts
    if (posts.length > 0) {
      const section = document.createElement('div');
      section.className = 'search-section';
      section.innerHTML = `<h3 class="search-section-title">Postagens (${posts.length})</h3>`;

      posts.forEach(post => {
        section.appendChild(createPostCard({ post, query }));
      });

      resultsEl.appendChild(section);
    }
  }

  function createProfileCard({usuario, query}) {

    const card = document.createElement('div');
    card.className = 'search-result-card search-result-profile';

    const initials = Profile.getInitials(usuario.username);
    const avatarHTML = usuario.img_perfil
    ? `<img src="${usuario.img_perfil}" alt="Foto de ${escapeHTML(usuario.username)}"/>`
      : escapeHTML(initials);

    const campo = campoCorrespondente(usuario, query);
    const matchBadge = `<span class="match-badge ${campo.classe}">${campo.label}</span>`;

    card.innerHTML = `
      <div class="result-ava">${avatarHTML}</div>
      <div class="result-body">
        <div class="result-name">
          ${highlight(usuario.username, query)}
          ${matchBadge}
        </div>
        ${usuario.bio
          ? `<div class="result-bio">${highlight(usuario.bio, query)}</div>`
          : ''}
      </div>
      <div class="result-arrow">→</div>
    `;

    card.addEventListener('click', () => {
      clearSearch();
      Profile.openProfile(usuario.username);
    });

    return card;
  }

  function createPostCard({post, query}) {

    const card = document.createElement('div');
    card.className = 'search-result-card search-result-post';

    const snippet = extractSnippet(post.content, query);
    const dateStr = new Date(post.dataCriacao).toLocaleDateString('pt-BR', {
      day: 'numeric', month: 'short',
    });

    card.innerHTML = `
      <div class="result-post-body">
        <div class="result-post-text">${highlight(snippet, query)}</div>
        <div class="result-post-meta">
          <span>${escapeHTML(post.autorUsername)}</span>
          <span class="result-dot">·</span>
          <time>${dateStr}</time>
          ${post.mediaUrl ? '<span class="result-has-img">📷</span>' : ''}
        </div>
      </div>
    `;

    card.addEventListener('click', () => {
      UI.activateTab('feed');
      clearSearch();
      // Aguarda o feed renderizar antes de destacar o post
      setTimeout(() => highlightPostInFeed(post.id), 100);
    });

    return card;
  }

  // Rola até o post no feed e aplica animação de destaque.
  function highlightPostInFeed(postId) {
    const postElement = document.querySelector(`[data-id="${postId}"]`);
    if (!postElement) return;

    postElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
    postElement.classList.add('post-search-highlight');

    setTimeout(() => postElement.classList.remove('post-search-highlight'), 2000);
  }

  // ── Highlight ────────────────────────────────────────────────

  // Envolve as ocorrências da query no texto com <mark class="search-highlight">.
  // O texto é escapado antes para prevenir XSS.
  function highlight(text, query) {
    if (!text || !query) return escapeHTML(text || '');

    const escaped = escapeHTML(text);

    try {
      const regex = new RegExp(`(${escapeRegex(query)})`, 'gi');
      return escaped.replace(regex, '<mark class="search-highlight">$1</mark>');
    } catch {
      return escaped;  // fallback se a regex falhar
    }
  }

  // Extrai um trecho do texto ao redor da primeira ocorrência da query.
  // Evita mostrar textos muito longos nos cards de resultado.
  function extractSnippet(text, query, maxLength = 120) {
    if (!text) return '';
    if (text.length <= maxLength) return text;

    const normalizedText  = normalize(text);
    const normalizedQuery = normalize(query);
    const matchIndex = normalizedText.indexOf(normalizedQuery);

    if (matchIndex === -1) {
      return text.substring(0, maxLength) + '…';
    }

    // Centraliza o trecho ao redor do match
    const padding = Math.floor((maxLength - query.length) / 2);
    const start   = Math.max(0, matchIndex - padding);
    const end     = Math.min(text.length, matchIndex + query.length + padding);

    const snippet = text.substring(start, end);
    const prefix  = start > 0             ? '…' : '';
    const suffix  = end   < text.length   ? '…' : '';

    return prefix + snippet + suffix;
  }

  // ── Debounce e handler principal ─────────────────────────────

  // Recebe o texto digitado, aplica debounce e executa a busca.
  // Também sincroniza todos os campos de busca com o mesmo valor.
  function handleSearchInput(query) {
    // Sincroniza os dois campos de busca (feed e aba de busca)
    document.querySelectorAll('.search-input').forEach(input => {
      if (input.value !== query) input.value = query;
    });

    // Aguarda o usuário parar de digitar antes de buscar
    if (debounceTimer) clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => executeSearch(query), DEBOUNCE_DELAY);
  }


  // ── Limpar busca ─────────────────────────────────────────────

  // Reseta tudo: campos, resultados, contadores.
  function clearSearch() {
    document.querySelectorAll('.search-input').forEach(input => {
      input.value = '';
    });

    const resultsEl  = document.getElementById('searchResults');
    const placeholder = document.getElementById('searchPlaceholder');
    const countEl    = document.getElementById('searchResultCount');

    if (resultsEl)   resultsEl.innerHTML = '';
    if (placeholder) placeholder.classList.remove('hidden');
    if (countEl)     countEl.classList.add('hidden');
  }

  // ── API pública ──────────────────────────────────────────────

  return {
    normalize,
    highlight,
    handleSearchInput,
    clearSearch,
  };

})();
