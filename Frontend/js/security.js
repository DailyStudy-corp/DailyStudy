const Security = (() => {

  const BUCKET_URL = /^https:\/\/objectstorage\.sa-saopaulo-1\.oraclecloud\.com\/n\/grnkbnebxpin\/b\/dailystudy-media(-dev)?\/o\/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/;

  // TODO: remover após a migração dos dados (base64 deixa de existir)
  const DATA_IMAGE = /^data:image\/(png|jpe?g|webp|gif);base64,[A-Za-z0-9+/=]+$/i;

  function safeImage(url) {
    if (!url) return null;
    return (BUCKET_URL.test(url) || DATA_IMAGE.test(url)) ? url : null;
  }

  return { safeImage };

})();