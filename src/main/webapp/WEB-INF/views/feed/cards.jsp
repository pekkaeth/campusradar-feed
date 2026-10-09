<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>
<div class="row g-3">
  <c:forEach items="${activities}" var="a">
    <div class="col-md-6 col-lg-4">
      <div class="card shadow-sm h-100">
        <div class="card-body d-flex flex-column">
          <div class="d-flex justify-content-between align-items-start">
            <h5 class="card-title mb-1"><c:out value="${a.title}"/></h5>
            <span class="badge bg-secondary"><c:out value="${a.category}"/></span>
          </div>
          <p class="text-muted small mb-2"><c:out value="${a.description}"/></p>
          <p class="small mb-1">📍 <c:out value="${a.place}"/></p>
          <p class="small mb-1">🙋 Hosted by <strong><c:out value="${a.hostName}"/></strong></p>
          <p class="small mb-2">⏳
            <c:choose>
              <c:when test="${a.minutesLeft >= 60}">${a.minutesLeft / 60}h ${a.minutesLeft % 60}m left</c:when>
              <c:otherwise>${a.minutesLeft} min left</c:otherwise>
            </c:choose>
          </p>
          <c:choose>
            <c:when test="${a.spotsLeft > 0}">
              <p class="fw-semibold text-success mb-2">Need ${a.spotsLeft} more · ${a.joinedCount}/${a.slotsNeeded} joined</p>
            </c:when>
            <c:otherwise>
              <p class="fw-semibold text-danger mb-2">Full · ${a.joinedCount}/${a.slotsNeeded} joined</p>
            </c:otherwise>
          </c:choose>
          <div class="mt-auto">
            <c:choose>
              <c:when test="${a.hostId == sessionScope.user.id}">
                <span class="badge bg-success me-2">You are the host</span>
                <button class="btn btn-outline-danger btn-sm" data-action="cancel" data-id="${a.id}">Cancel</button>
              </c:when>
              <c:when test="${a.joinedByMe}">
                <span class="badge bg-primary me-2">You joined</span>
                <button class="btn btn-outline-secondary btn-sm" data-action="leave" data-id="${a.id}">Leave</button>
              </c:when>
              <c:when test="${a.spotsLeft == 0}">
                <button class="btn btn-secondary btn-sm" disabled>Full</button>
              </c:when>
              <c:otherwise>
                <button class="btn btn-success btn-sm" data-action="join" data-id="${a.id}">JOIN</button>
              </c:otherwise>
            </c:choose>
          </div>
        </div>
      </div>
    </div>
  </c:forEach>
</div>
<c:if test="${empty activities and empty error}">
  <div class="text-center text-muted py-5">
    <div class="fs-1">🌙</div>
    <p class="mb-1">Nothing is happening right now.</p>
    <p>Be the first to start something!</p>
  </div>
</c:if>