const Security = (() => {

function safeImage(url) {
    if (!url) return null;
    const dataImage = /^data:image\/(png|jpe?g|webp);base64,/.test(url)
    const httpUrl = /^https:\/\/[^"'<>\s]+$/.test(url);
    return (dataImage || httpUrl) ? url : null;
  }

  return {
    safeImage,
  };

})();  