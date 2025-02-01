(function () {
  const fileInputElement = document.getElementById("avatar-file");
  fileInputElement.addEventListener('change', (evt) => {
    const image = new FileReader();

    image.addEventListener('load', e => {
      const previewImg = document.createElement("img");
      previewImg.classList.add("jenkins-avatar")
      previewImg.id = "avatar-preview"
      previewImg.classList.add("icon-xlg")
      previewImg.src = e.target.result;
      document.getElementById("avatar-preview").replaceWith(previewImg);
      document.getElementById("avatar-existing").value = "present"
    });
    image.readAsDataURL(evt.target.files[0]);
  })

  document.getElementById('avatar-remove-button')
    .addEventListener('click', e => {
      document.getElementById("avatar-existing").value = ""

      const previewImg = document.getElementById("avatar-initial").cloneNode(true)
      previewImg.classList.remove("jenkins-hidden")

      previewImg.classList.add("jenkins-avatar")
      previewImg.id = "avatar-preview"
      previewImg.classList.add("icon-xlg")
      document.getElementById("avatar-preview").replaceWith(previewImg);

      fileInputElement.value = ""
    })
})();