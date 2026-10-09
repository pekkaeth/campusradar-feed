<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/common/header.jspf" %>
<div class="container mt-4">
  <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
    <h3 class="mb-0">🔴 What's happening now?</h3>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/feed/create">+ Start an activity</a>
  </div>

  <c:if test="${not empty param.msg}"><div class="alert alert-success"><c:out value="${param.msg}"/></div></c:if>
  <div id="notice" class="alert d-none" role="alert"></div>

  <div class="d-flex flex-wrap gap-2 mb-3" id="filters">
    <button class="btn btn-sm btn-dark" data-cat="All">All</button>
    <button class="btn btn-sm btn-outline-dark" data-cat="Sports">Sports</button>
    <button class="btn btn-sm btn-outline-dark" data-cat="Study">Study</button>
    <button class="btn btn-sm btn-outline-dark" data-cat="Music">Music</button>
    <button class="btn btn-sm btn-outline-dark" data-cat="Food">Food</button>
    <button class="btn btn-sm btn-outline-dark" data-cat="Gaming">Gaming</button>
    <button class="btn btn-sm btn-outline-dark" data-cat="Coding">Coding</button>
    <button class="btn btn-sm btn-outline-dark" data-cat="Other">Other</button>
  </div>

  <div id="feed"><div class="text-muted">Loading...</div></div>
  <p class="small text-muted mt-3">Updates automatically every 5 seconds · <span id="updated"></span></p>
</div>

<script>
  var base = '${pageContext.request.contextPath}';
  var category = 'All';
  var feedBox = document.getElementById('feed');
  var notice = document.getElementById('notice');

  function showNotice(ok, text) {
    notice.className = 'alert ' + (ok ? 'alert-success' : 'alert-warning');
    notice.textContent = text;
    setTimeout(function () { notice.className = 'alert d-none'; }, 4000);
  }

  function loadFeed() {
    fetch(base + '/feed/data?category=' + encodeURIComponent(category), { credentials: 'same-origin' })
      .then(function (r) {
        if (r.status === 401) { location.reload(); return null; }
        return r.text();
      })
      .then(function (html) {
        if (html === null) return;
        feedBox.innerHTML = html;
        document.getElementById('updated').textContent = 'last update ' + new Date().toLocaleTimeString();
      })
      .catch(function () { /* network blip: try again at the next tick */ });
  }

  // one click handler for every JOIN / Leave / Cancel button
  feedBox.addEventListener('click', function (e) {
    var btn = e.target.closest('button[data-action]');
    if (!btn) return;
    btn.disabled = true;
    fetch(base + '/feed/' + btn.dataset.action, {
      method: 'POST',
      credentials: 'same-origin',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: 'id=' + encodeURIComponent(btn.dataset.id)
    })
      .then(function (r) { return r.json(); })
      .then(function (d) { showNotice(d.ok, d.message); loadFeed(); })
      .catch(function () { showNotice(false, 'Network problem. Please try again.'); loadFeed(); });
  });

  document.getElementById('filters').addEventListener('click', function (e) {
    var b = e.target.closest('button[data-cat]');
    if (!b) return;
    category = b.dataset.cat;
    document.querySelectorAll('#filters button').forEach(function (x) {
      x.className = 'btn btn-sm ' + (x === b ? 'btn-dark' : 'btn-outline-dark');
    });
    loadFeed();
  });

  loadFeed();
  setInterval(function () { if (!document.hidden) loadFeed(); }, 5000);
</script>
<%@ include file="/WEB-INF/common/footer.jspf" %>