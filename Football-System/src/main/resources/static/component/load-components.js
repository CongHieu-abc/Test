window.addEventListener("DOMContentLoaded", () => {
    includeHTML("header", "component/header.html");
    includeHTML("footer", "component/footer.html");
});

function includeHTML(id, file) {
    fetch(file)
        .then((response) => response.text())
        .then((data) => {
            document.getElementById(id).innerHTML = data;
        });
}
