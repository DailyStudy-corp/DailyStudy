const ImageUpload = (() => {

  async function upload(file) {
    const form = new FormData();
    form.append('arquivo', file);

    const response = await fetch('/api/imagens', {
      method: 'POST',
      headers: { ...Auth.headers() },
      body: form
    });

    if (!response.ok) {
      const erro = await response.json().catch(() => null);
      throw new Error(erro?.message || 'Não foi possível enviar a imagem.');
    }

    const data = await response.json();
    return data.url;
  }

  return { upload };

})();