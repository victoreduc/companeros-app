(function () {
    var panelOpen = false;

    function updateBadge() {
        fetch('/api/notifications/unread-count')
            .then(function (r) { return r.json(); })
            .then(function (data) {
                var badge = document.getElementById('notif-badge');
                if (!badge) return;
                if (data.count > 0) {
                    badge.textContent = data.count > 99 ? '99+' : data.count;
                    badge.style.display = 'flex';
                } else {
                    badge.style.display = 'none';
                }
            })
            .catch(function () {});
    }

    function typeIcon(type) {
        if (type === 'TASK_ASSIGNED') return '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"></path><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"></path></svg>';
        if (type === 'SHOPPING_ADDED') return '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="21" r="1"></circle><circle cx="20" cy="21" r="1"></circle><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path></svg>';
        if (type === 'HOUSE_JOIN') return '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="23" y1="11" x2="17" y2="11"></line></svg>';
        if (type === 'HOUSE_LEAVE') return '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="18" y1="8" x2="23" y2="13"></line><line x1="23" y1="8" x2="18" y2="13"></line></svg>';
        return '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>';
    }

    function formatTime(dateStr) {
        var date = new Date(dateStr);
        var diff = Math.floor((Date.now() - date.getTime()) / 1000);
        if (diff < 60) return 'Ahora mismo';
        if (diff < 3600) return 'Hace ' + Math.floor(diff / 60) + ' min';
        if (diff < 86400) return 'Hace ' + Math.floor(diff / 3600) + ' h';
        return 'Hace ' + Math.floor(diff / 86400) + ' días';
    }

    function escapeHtml(text) {
        var d = document.createElement('div');
        d.appendChild(document.createTextNode(text));
        return d.innerHTML;
    }

    function loadNotifications() {
        var list = document.getElementById('notif-list');
        if (!list) return;
        list.innerHTML = '<div class="notif-empty">Cargando...</div>';

        fetch('/api/notifications')
            .then(function (r) { return r.json(); })
            .then(function (notifications) {
                var markAllBtn = document.getElementById('mark-all-btn');
                var hasUnread = notifications.some(function (n) { return !n.read; });
                if (markAllBtn) markAllBtn.style.display = hasUnread ? 'block' : 'none';

                if (notifications.length === 0) {
                    list.innerHTML = '<div class="notif-empty">No tienes notificaciones</div>';
                    return;
                }
                list.innerHTML = notifications.map(function (n) {
                    return '<div class="notif-item' + (n.read ? '' : ' notif-unread') + '">' +
                        '<div class="notif-icon notif-icon-' + n.type.toLowerCase() + '">' + typeIcon(n.type) + '</div>' +
                        '<div class="notif-body">' +
                        '<p class="notif-msg">' + escapeHtml(n.message) + '</p>' +
                        '<p class="notif-time">' + formatTime(n.createdAt) + '</p>' +
                        '</div>' +
                        (n.read ? '' : '<div class="notif-dot"></div>') +
                        '</div>';
                }).join('');
            })
            .catch(function () {
                var list = document.getElementById('notif-list');
                if (list) list.innerHTML = '<div class="notif-empty">Error al cargar</div>';
            });
    }

    window.toggleNotifPanel = function () {
        var panel = document.getElementById('notif-panel');
        if (!panel) return;
        panelOpen = !panelOpen;
        if (panelOpen) {
            var btn = document.getElementById('bell-btn');
            var rect = btn.getBoundingClientRect();
            var vw = window.innerWidth;
            var vh = window.innerHeight;
            var panelW = 320;
            var panelH = 480;

            panel.style.top = '';
            panel.style.bottom = '';
            panel.style.left = '';
            panel.style.right = '';

            if (vw >= 1024) {
                // Desktop: left sidebar — open panel to the right of the button
                var top = Math.max(8, Math.min(rect.top, vh - panelH - 8));
                panel.style.top = top + 'px';
                panel.style.left = (rect.right + 10) + 'px';
            } else if (vw >= 768) {
                // Tablet: top nav — open panel downward, aligned with button right edge
                panel.style.top = (rect.bottom + 8) + 'px';
                var left = Math.min(rect.right - panelW, vw - panelW - 8);
                panel.style.left = Math.max(8, left) + 'px';
            } else {
                // Mobile: bottom bar — open panel upward, centered on button
                panel.style.bottom = (vh - rect.top + 8) + 'px';
                var left = rect.left + rect.width / 2 - panelW / 2;
                panel.style.left = Math.max(8, Math.min(left, vw - panelW - 8)) + 'px';
            }

            panel.style.display = 'flex';
            loadNotifications();
        } else {
            panel.style.display = 'none';
        }
    };

    window.markAllNotifRead = function () {
        fetch('/api/notifications/mark-all-read', { method: 'POST' })
            .then(function () {
                updateBadge();
                loadNotifications();
            })
            .catch(function () {});
    };

    document.addEventListener('click', function (e) {
        if (!panelOpen) return;
        var wrapper = document.getElementById('nav-notif-wrapper');
        var panel = document.getElementById('notif-panel');
        if (wrapper && !wrapper.contains(e.target) && panel && !panel.contains(e.target)) {
            panelOpen = false;
            panel.style.display = 'none';
        }
    });

    updateBadge();
    setInterval(updateBadge, 30000);
})();
