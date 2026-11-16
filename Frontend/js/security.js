const Security = (() => {

  const BUCKET_URL = /^https:\/\/objectstorage\.sa-saopaulo-1\.oraclecloud\.com\/n\/grnkbnebxpin\/b\/dailystudy-media(-dev)?\/o\/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/;

  function safeImage(url) {
    if (!url) return null;
    return (BUCKET_URL.test(url)) ? url : null;
  }

  return { safeImage };

})();