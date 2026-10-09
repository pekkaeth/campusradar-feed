<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/common/header.jspf" %>
<div class="container mt-4" style="max-width:620px;">
  <h3>Start an activity</h3>
  <p class="text-muted">It disappears by itself when the time is up.</p>
  <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>

  <form method="post" action="${pageContext.request.contextPath}/feed/create" class="card card-body shadow-sm">
    <div class="mb-3"><label class="form-label">What's the plan?</label>
      <input type="text" name="title" class="form-control" minlength="3" maxlength="120"
             placeholder="Cricket near the sports ground" value="<c:out value='${param.title}'/>" required></div>

    <div class="row">
      <div class="col-md-6 mb-3"><label class="form-label">Category</label>
        <select name="category" class="form-select">
          <option>Sports</option><option>Study</option><option>Music</option><option>Food</option>
          <option>Gaming</option><option>Coding</option><option>Other</option>
        </select></div>
      <div class="col-md-6 mb-3"><label class="form-label">How many people needed?</label>
        <input type="number" name="slots" class="form-control" min="1" max="50"
               value="${empty param.slots ? 3 : param.slots}" required></div>
    </div>

    <div class="row">
      <div class="col-md-6 mb-3"><label class="form-label">Place</label>
        <select name="locationId" class="form-select">
          <option value="">Other (type below)</option>
          <c:forEach items="${locations}" var="l"><option value="${l[0]}"><c:out value="${l[1]}"/></option></c:forEach>
        </select></div>
      <div class="col-md-6 mb-3"><label class="form-label">Or type a place</label>
        <input type="text" name="place" class="form-control" maxlength="100"
               placeholder="Behind the library" value="<c:out value='${param.place}'/>"></div>
    </div>

    <div class="mb-3"><label class="form-label">How long will it last?</label>
      <select name="minutes" class="form-select">
        <option value="30">30 minutes</option>
        <option value="60" selected>1 hour</option>
        <option value="120">2 hours</option>
        <option value="240">4 hours</option>
      </select></div>

    <div class="mb-3"><label class="form-label">Details (optional)</label>
      <textarea name="description" class="form-control" rows="2" maxlength="300"><c:out value="${param.description}"/></textarea></div>

    <button class="btn btn-primary">Go live</button>
    <a class="btn btn-link" href="${pageContext.request.contextPath}/feed/list">Cancel</a>
  </form>
</div>
<%@ include file="/WEB-INF/common/footer.jspf" %>