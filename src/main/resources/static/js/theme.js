document.addEventListener('DOMContentLoaded', () => {
    const themeSwitch = document.getElementById('theme-switch');
    const userTheme = localStorage.getItem('theme');
    const systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

    // Sincroniza el interruptor con el tema actual
    if (userTheme === 'dark-mode' || (!userTheme && systemDark)) {
        themeSwitch.checked = true;
    }

    themeSwitch.addEventListener('change', function(event) {
        if(event.target.checked) {
            document.documentElement.classList.add('dark-mode');
            localStorage.setItem('theme', 'dark-mode');
        } else {
            document.documentElement.classList.remove('dark-mode');
            localStorage.setItem('theme', 'light-mode');
        }
    });
});
